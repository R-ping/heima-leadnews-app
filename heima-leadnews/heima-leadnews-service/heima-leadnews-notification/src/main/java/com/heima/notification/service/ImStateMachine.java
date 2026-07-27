package com.heima.notification.service;

import com.heima.model.notification.pojos.ImSession;
import com.heima.notification.mapper.ImMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImStateMachine {

    @Autowired
    private ImMessageMapper imMessageMapper;

    /**
     * 检查用户A是否可以向用户B发送消息
     * S0: 互关 → 无限发送
     * S1: 单向/陌生人，未发过或已被回复 → 允许发送1条
     * S2: 已发送1条，待回复 → 禁止发送
     * S3: B曾回复过A (is_active=true) → 无限发送
     * S4: 互关解除，但is_active=true → 仍无限发送
     */
    public SendPermission checkPermission(Long senderId, Long receiverId, ImSession session) {
        // S3/S4: is_active=true，B曾回复过A，永久有效
        if (session != null && session.getIsActive() != null && session.getIsActive() == 1) {
            return SendPermission.ALLOWED;
        }

        // S0: 检查互关（TODO: 需要Feign调用user服务查询关注关系）
        // 暂时跳过互关检测，后续集成

        // S2: 检查是否已发送消息等待回复
        if (session != null) {
            int sentCount = imMessageMapper.countSentAfterLastReply(session.getId(), senderId, receiverId);
            if (sentCount >= 1) {
                return SendPermission.LIMIT_REACHED;
            }
        }

        // S1: 允许发送
        return SendPermission.ALLOWED_ONCE;
    }

    public enum SendPermission {
        ALLOWED,         // 无限发送
        ALLOWED_ONCE,    // 允许发送1条（S1）
        LIMIT_REACHED    // 已达限制（S2）
    }
}