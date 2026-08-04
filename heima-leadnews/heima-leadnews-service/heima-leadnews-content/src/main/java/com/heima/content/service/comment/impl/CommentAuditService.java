package com.heima.content.service.comment.impl;

import com.heima.content.service.article.impl.AbstractAuditService;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.comment.ApCommentMapper;
import com.heima.content.mapper.user.UserBehaviorRecordMapper;
import com.heima.content.utils.NotificationHelper;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.audit.AuditContext;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    /**
     * 异步审核评论（延迟5-10秒）
     * 用户发布评论后立即显示，后台异步审核
     * 使用 CompletableFuture.delayedExecutor 避免阻塞 @Async 线程池线程
     */
    @Async
    public void asyncAuditComment(AuditContext context) {
        long delay = 5000 + (long) (Math.random() * 5000);
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始异步审核评论, commentId={}", context.getEntityId());
                com.heima.model.audit.AuditResult result = audit(context);

                if (result.isPassed()) {
                    log.info("评论异步审核通过, commentId={}", context.getEntityId());
                } else {
                    log.info("评论异步审核未通过, commentId={}, reason={}",
                        context.getEntityId(), result.getReason());
                }
            } catch (Exception e) {
                log.error("评论异步审核异常, commentId={}", context.getEntityId(), e);
            }
        }, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS));
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
            NotificationHelper.sendCommentNotification(
                notificationClient,
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
        NotificationHelper.sendViolationNotification(
            notificationClient,
            comment.getUserId().longValue(),
            comment.getId(),
            comment.getContent(),
            reason
        );
    }
}