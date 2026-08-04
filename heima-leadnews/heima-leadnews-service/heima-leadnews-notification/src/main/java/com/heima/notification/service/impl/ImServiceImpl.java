package com.heima.notification.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.model.notification.dtos.ImReadDto;
import com.heima.model.notification.pojos.ImMessage;
import com.heima.model.notification.pojos.ImSession;
import com.heima.notification.mapper.ImMessageMapper;
import com.heima.notification.mapper.ImSessionMapper;
import com.heima.notification.service.ImService;
import com.heima.notification.service.ImStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ImServiceImpl implements ImService {

    @Autowired
    private ImSessionMapper imSessionMapper;

    @Autowired
    private ImMessageMapper imMessageMapper;

    @Autowired
    private ImStateMachine imStateMachine;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public ResponseResult listSessions(Long userId) {
        List<ImSession> sessions = imSessionMapper.selectByUserId(userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ImSession s : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("session_id", s.getId());
            item.put("session_key", s.getSessionKey());
            Long peerId = s.getUser1Id().equals(userId) ? s.getUser2Id() : s.getUser1Id();
            item.put("peer_id", peerId);
            item.put("last_message", s.getLastMessage());
            item.put("last_message_at", s.getLastMessageAt() != null ? s.getLastMessageAt().toString() : null);
            int unread = s.getUser1Id().equals(userId)
                    ? (s.getUser1UnreadCount() != null ? s.getUser1UnreadCount() : 0)
                    : (s.getUser2UnreadCount() != null ? s.getUser2UnreadCount() : 0);
            item.put("unread_count", unread);
            item.put("is_active", s.getIsActive() == 1);
            list.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult listMessages(Long userId, Long sessionId, Long cursor, Integer size) {
        int limit = (size == null || size <= 0) ? 20 : Math.min(size, 50);
        List<ImMessage> messages = imMessageMapper.selectBySessionId(sessionId, cursor, limit);
        Collections.reverse(messages);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ImMessage m : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", m.getId());
            item.put("session_id", m.getSessionId());
            item.put("sender_id", m.getSenderId());
            item.put("receiver_id", m.getReceiverId());
            item.put("content", m.getContent());
            item.put("msg_type", m.getMsgType());
            item.put("status", m.getStatus());
            item.put("is_self", m.getSenderId().equals(userId));
            item.put("created_at", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            list.add(item);
        }

        boolean hasMore = !messages.isEmpty() && messages.size() == limit;
        Long nextCursor = hasMore ? messages.get(0).getId() : null;

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("next_cursor", nextCursor);
        result.put("has_more", hasMore);
        return ResponseResult.okResult(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult sendMessage(Long senderId, ImMessageDto dto) {
        Long receiverId = dto.getReceiverId();
        if (receiverId == null || receiverId.equals(senderId)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        String sessionKey = buildSessionKey(senderId, receiverId);

        ImSession session = imSessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            session = new ImSession();
            session.setSessionKey(sessionKey);
            session.setUser1Id(Math.min(senderId, receiverId));
            session.setUser2Id(Math.max(senderId, receiverId));
            session.setIsActive(0);
            session.setUser1UnreadCount(0);
            session.setUser2UnreadCount(0);
            session.setCreatedAt(LocalDateTime.now());
            imSessionMapper.insert(session);
        }

        ImStateMachine.SendPermission permission = imStateMachine.checkPermission(senderId, receiverId, session);
        if (permission == ImStateMachine.SendPermission.LIMIT_REACHED) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "LIMIT_REACHED");
            error.put("message", "由于对方并未关注你，在收到对方回复之前，你最多只能发送1条文字消息");
            return ResponseResult.errorResult(403, "由于对方并未关注你，在收到对方回复之前，你最多只能发送1条文字消息");
        }

        ImMessage message = new ImMessage();
        message.setSessionId(session.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(dto.getContent().trim());
        message.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : 1);
        message.setStatus(0);
        message.setIsDeletedForSender(0);
        message.setIsDeletedForReceiver(0);
        message.setCreatedAt(LocalDateTime.now());
        imMessageMapper.insert(message);

        session.setLastMessage(dto.getContent().trim().length() > 50
                ? dto.getContent().trim().substring(0, 50) + "..."
                : dto.getContent().trim());
        session.setLastMessageAt(LocalDateTime.now());
        if (session.getUser1Id().equals(receiverId)) {
            session.setUser1UnreadCount((session.getUser1UnreadCount() != null ? session.getUser1UnreadCount() : 0) + 1);
        } else {
            session.setUser2UnreadCount((session.getUser2UnreadCount() != null ? session.getUser2UnreadCount() : 0) + 1);
        }
        imSessionMapper.updateById(session);

        Map<String, Object> result = new HashMap<>();
        result.put("message_id", message.getId());
        result.put("status", "sent");
        result.put("created_at", message.getCreatedAt().toString());
        return ResponseResult.okResult(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult markRead(Long userId, ImReadDto dto) {
        if (dto.getSessionId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Long lastReadId = dto.getLastReadId() != null ? dto.getLastReadId() : Long.MAX_VALUE;
        imMessageMapper.markRead(dto.getSessionId(), lastReadId, userId);

        ImSession session = imSessionMapper.selectById(dto.getSessionId());
        if (session != null) {
            if (session.getUser1Id().equals(userId)) {
                session.setUser1UnreadCount(0);
            } else {
                session.setUser2UnreadCount(0);
            }
            if (session.getIsActive() == null || session.getIsActive() == 0) {
                session.setIsActive(1);
            }
            imSessionMapper.updateById(session);
        }

        return ResponseResult.okResult(null);
    }

    private String buildSessionKey(Long uid1, Long uid2) {
        return Math.min(uid1, uid2) + "_" + Math.max(uid1, uid2);
    }
}