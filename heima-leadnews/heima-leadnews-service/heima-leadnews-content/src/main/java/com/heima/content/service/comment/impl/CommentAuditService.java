package com.heima.content.service.comment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.content.service.article.impl.AbstractAuditService;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.comment.ApCommentMapper;
import com.heima.content.mapper.user.UserBehaviorRecordMapper;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.audit.AuditContext;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论异步审核服务
 *
 * 审核策略：异步审核
 * 1. 评论发布时立即保存到数据库（用户可见）
 * 2. 异步延迟5-10秒后执行审核
 * 3. 审核通过 → 给内容作者发送"评论"通知
 * 4. 审核违规 → 删除评论，给评论者发送"系统通知"
 */
@Slf4j
@Service
public class CommentAuditService extends AbstractAuditService {

    @Autowired
    private ApCommentMapper apCommentMapper;

    @Autowired(required = false)
    private INotificationClient notificationClient;

    @Autowired
    private UserBehaviorRecordMapper behaviorRecordMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步审核评论（延迟5-10秒）
     * 用户发布评论后立即显示，后台异步审核
     */
    @Async
    public void asyncAuditComment(AuditContext context) {
        try {
            // 延迟5-10秒执行审核，模拟"即时"体验
            long delay = 5000 + (long) (Math.random() * 5000);
            Thread.sleep(delay);

            log.info("开始异步审核评论, commentId={}", context.getEntityId());
            com.heima.model.audit.AuditResult result = audit(context);

            if (result.isPassed()) {
                // 审核通过后，站内信通知由 handlePassed 处理
                log.info("评论异步审核通过, commentId={}", context.getEntityId());
            } else {
                // 审核失败，handleFailed 已处理删除和通知
                log.info("评论异步审核未通过, commentId={}, reason={}",
                    context.getEntityId(), result.getReason());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("评论异步审核被中断, commentId={}", context.getEntityId());
        } catch (Exception e) {
            log.error("评论异步审核异常, commentId={}", context.getEntityId(), e);
        }
    }

    @Override
    protected void handlePassed(AuditContext context) {
        // 审核通过：给内容作者发送评论通知
        ApComment comment = apCommentMapper.selectById(context.getEntityId());
        if (comment == null) {
            log.warn("评论不存在, commentId={}", context.getEntityId());
            return;
        }

        // 查找评论的目标内容作者（通过文章/沸点的作者）
        // 评论的 targetUserId 在 context 中已设置
        if (context.getTargetUserId() != null) {
            sendCommentNotification(
                context.getTargetUserId(),
                context.getUserId(),
                comment.getContent(),
                context.getTargetType(),
                context.getTargetId()
            );
        }
    }

    @Override
    protected void handleFailed(AuditContext context, String reason) {
        // 审核违规：软删除评论 + 发送系统通知给评论者
        ApComment comment = apCommentMapper.selectById(context.getEntityId());
        if (comment == null) {
            return;
        }

        // 软删除评论（通过 is_deleted 字段，但 ap_comment 表没有 is_deleted 字段）
        // 直接物理删除
        apCommentMapper.deleteById(comment.getId());

        // 更新行为记录状态为已撤销
        if (context.getUserId() != null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehaviorRecord> query =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            query.eq(UserBehaviorRecord::getUserId, context.getUserId());
            query.eq(UserBehaviorRecord::getBehaviorType, 
                context.getTargetType() == 1 ? "comment_article" : "comment_pin");
            query.eq(UserBehaviorRecord::getTargetId, context.getTargetId());
            query.eq(UserBehaviorRecord::getStatus, 1);
            UserBehaviorRecord record = behaviorRecordMapper.selectOne(query);
            if (record != null) {
                record.setStatus(0);
                behaviorRecordMapper.updateById(record);
            }
        }

        // 发送系统通知给评论者
        sendViolationNotification(comment, reason);
    }

    /**
     * 发送评论通知给内容作者
     */
    private void sendCommentNotification(Integer targetUserId, Integer fromUserId,
                                          String content, Integer targetType, Long targetId) {
        try {
            if (notificationClient == null) {
                log.warn("通知服务不可用，跳过发送评论通知");
                return;
            }

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("action_user_id", fromUserId);
            contentMap.put("content", truncate(content, 20));
            contentMap.put("target_type", targetType == 1 ? "article" : "pin");
            contentMap.put("target_id", targetId);
            contentMap.put("notification_type", "comment");

            Map<String, Object> params = new HashMap<>();
            params.put("userId", targetUserId.longValue());
            params.put("type", 1); // 评论通知
            params.put("sourceId", String.valueOf(targetId));
            params.put("content", objectMapper.writeValueAsString(contentMap));

            notificationClient.createNotification(params);
            log.info("评论审核通过通知已发送, to={}, commentId={}", targetUserId, targetId);
        } catch (Exception e) {
            log.error("发送评论通知失败, to={}", targetUserId, e);
        }
    }

    /**
     * 发送违规通知给评论者
     */
    private void sendViolationNotification(ApComment comment, String reason) {
        try {
            if (notificationClient == null) {
                log.warn("通知服务不可用，跳过发送违规通知");
                return;
            }

            String message = String.format(
                "你的评论因违反社区规范已被删除。内容: %s",
                truncate(comment.getContent() != null ? comment.getContent() : "", 50)
            );

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("commentId", String.valueOf(comment.getId()));
            contentMap.put("message", message);
            contentMap.put("notification_type", "system");

            Map<String, Object> params = new HashMap<>();
            params.put("userId", comment.getUserId().longValue());
            params.put("type", 4); // 系统通知
            params.put("sourceId", String.valueOf(comment.getId()));
            params.put("content", objectMapper.writeValueAsString(contentMap));

            notificationClient.createNotification(params);
            log.info("评论违规删除通知已发送, to={}, commentId={}", comment.getUserId(), comment.getId());
        } catch (Exception e) {
            log.error("发送评论违规通知失败, commentId={}", comment.getId(), e);
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}