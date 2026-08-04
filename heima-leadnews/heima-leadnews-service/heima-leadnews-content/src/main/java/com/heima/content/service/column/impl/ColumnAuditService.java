package com.heima.content.service.column.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.content.service.article.impl.AbstractAuditService;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.column.ApColumnMapper;
import com.heima.model.column.pojos.ApColumn;
import com.heima.model.audit.AuditContext;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 专栏审核服务
 * 审核流程：AI违规检测 → 图片审核 → 更新状态
 */
@Slf4j
@Service
public class ColumnAuditService extends AbstractAuditService {

    @Autowired
    private ApColumnMapper apColumnMapper;

    @Autowired(required = false)
    private INotificationClient notificationClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handlePassed(AuditContext context) {
        ApColumn column = apColumnMapper.selectById(context.getEntityId());
        if (column == null) {
            log.error("专栏不存在, columnId={}", context.getEntityId());
            return;
        }
        column.setStatus(ApColumn.Status.PUBLISHED.getCode());
        apColumnMapper.updateById(column);
        log.info("专栏审核通过, columnId={}", context.getEntityId());
    }

    @Override
    protected void handleFailed(AuditContext context, String reason) {
        ApColumn column = apColumnMapper.selectById(context.getEntityId());
        if (column == null) {
            log.error("专栏不存在, columnId={}", context.getEntityId());
            return;
        }
        column.setStatus(ApColumn.Status.FAIL.getCode());
        apColumnMapper.updateById(column);

        // 发送审核失败通知
        sendModerationFailNotification(column, reason);
        log.info("专栏审核未通过, columnId={}, reason={}", context.getEntityId(), reason);
    }

    private void sendModerationFailNotification(ApColumn column, String reason) {
        try {
            if (notificationClient == null) {
                log.warn("通知服务不可用，跳过发送审核失败通知, columnId={}", column.getId());
                return;
            }

            String message = String.format(
                "你的专栏《%s》因违反社区规范已被拒绝。原因: %s",
                column.getTitle() != null ? column.getTitle() : "无标题",
                reason != null ? reason : "违反社区规范"
            );

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("columnId", String.valueOf(column.getId()));
            contentMap.put("message", message);
            contentMap.put("notification_type", "system");

            Map<String, Object> params = new HashMap<>();
            params.put("userId", column.getAuthorId());
            params.put("type", 4); // 系统通知
            params.put("sourceId", String.valueOf(column.getId()));
            params.put("content", objectMapper.writeValueAsString(contentMap));

            notificationClient.createNotification(params);
            log.info("专栏审核失败通知已发送, columnId={}, authorId={}", column.getId(), column.getAuthorId());
        } catch (Exception e) {
            log.error("发送专栏审核失败通知异常, columnId={}", column.getId(), e);
        }
    }
}