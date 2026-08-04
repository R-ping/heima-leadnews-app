package com.heima.content.service.pins.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.content.service.article.impl.AbstractAuditService;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.pins.ApPinsMapper;
import com.heima.model.pins.pojos.ApPins;
import com.heima.model.audit.AuditContext;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 沸点审核服务
 * 审核流程：AI违规检测 → 图片审核 → 更新状态
 */
@Slf4j
@Service
public class PinsAuditService extends AbstractAuditService {

    @Autowired
    private ApPinsMapper apPinsMapper;

    @Autowired(required = false)
    private INotificationClient notificationClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handlePassed(AuditContext context) {
        ApPins pins = apPinsMapper.selectById(context.getEntityId());
        if (pins == null) {
            log.error("沸点不存在, pinsId={}", context.getEntityId());
            return;
        }
        pins.setStatus(ApPins.Status.PUBLISHED.getCode());
        pins.setReviewTime(new Date());
        apPinsMapper.updateById(pins);
        log.info("沸点审核通过, pinsId={}", context.getEntityId());
    }

    @Override
    protected void handleFailed(AuditContext context, String reason) {
        ApPins pins = apPinsMapper.selectById(context.getEntityId());
        if (pins == null) {
            log.error("沸点不存在, pinsId={}", context.getEntityId());
            return;
        }
        pins.setStatus(ApPins.Status.FAIL.getCode());
        pins.setReason(reason);
        apPinsMapper.updateById(pins);

        // 发送审核失败系统通知
        sendModerationFailNotification(pins, reason);
        log.info("沸点审核未通过, pinsId={}, reason={}", context.getEntityId(), reason);
    }

    /**
     * 发送审核失败系统通知
     */
    private void sendModerationFailNotification(ApPins pins, String reason) {
        try {
            if (notificationClient == null) {
                log.warn("通知服务不可用，跳过发送审核失败通知, pinsId={}", pins.getId());
                return;
            }

            String message = String.format(
                "你的沸点因违反社区规范已被删除。内容: %s",
                reason != null ? reason : "违反社区规范"
            );

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("pinsId", String.valueOf(pins.getId()));
            contentMap.put("message", message);
            contentMap.put("notification_type", "system");

            Map<String, Object> params = new HashMap<>();
            params.put("userId", pins.getAuthorId());
            params.put("type", 4); // 系统通知
            params.put("sourceId", String.valueOf(pins.getId()));
            params.put("content", objectMapper.writeValueAsString(contentMap));

            ResponseResult result = notificationClient.createNotification(params);
            if (result != null && result.getCode() == 200) {
                log.info("沸点审核失败通知已发送, pinsId={}, authorId={}", pins.getId(), pins.getAuthorId());
            }
        } catch (Exception e) {
            log.error("发送沸点审核失败通知异常, pinsId={}", pins.getId(), e);
        }
    }
}