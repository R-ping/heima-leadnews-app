package com.heima.notification.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.notification.dtos.NotificationDto;
import com.heima.model.notification.pojos.Notification;
import com.heima.model.user.pojos.ApUser;
import com.heima.notification.mapper.NotificationMapper;
import com.heima.notification.service.NotificationService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String REDIS_UNREAD_KEY = "notif:unread:";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public ResponseResult list(NotificationDto dto) {
        Long userId = getCurrentUserId();
        Integer type = mapType(dto.getType());
        if (type == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        int size = (dto.getSize() == null || dto.getSize() <= 0) ? 20 : Math.min(dto.getSize(), 50);
        Long cursor = dto.getCursor() != null ? Long.parseLong(dto.getCursor()) : null;

        List<Notification> notifications = notificationMapper.selectByTypeAndCursor(userId, type, cursor, size);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Notification n : notifications) {
            list.add(assembleNotificationData(n, userId));
        }

        boolean hasMore = notifications.size() == size;
        String nextCursor = hasMore && !notifications.isEmpty()
                ? String.valueOf(notifications.get(notifications.size() - 1).getId())
                : null;

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("next_cursor", nextCursor);
        result.put("has_more", hasMore);

        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult reply(Long userId, Long commentId, String content) {
        // TODO: 集成评论服务后，调用评论服务插入子回复
        // 1. 校验评论存在
        // 2. 插入子回复 (parent_id = commentId)
        // 3. 触发新通知给原作者
        return ResponseResult.okResult(null);
    }

    @Override
    public ResponseResult toggleLike(Long userId, Long commentId) {
        // TODO: 集成评论服务后，调用评论点赞Toggle
        // 幂等：已点赞则取消，未点赞则点赞
        return ResponseResult.okResult(null);
    }

    @Override
    public ResponseResult followBack(Long userId, Long followerId) {
        // TODO: 集成用户服务后，调用关注接口
        return ResponseResult.okResult(null);
    }

    @Override
    public ResponseResult unreadCount(Long userId) {
        // 优先从Redis缓存读取
        if (stringRedisTemplate != null) {
            String cached = stringRedisTemplate.opsForValue().get(REDIS_UNREAD_KEY + userId);
            if (cached != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("total", Integer.parseInt(cached));
                return ResponseResult.okResult(result);
            }
        }
        int count = notificationMapper.countUnread(userId);
        // 写回缓存
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue().set(REDIS_UNREAD_KEY + userId, String.valueOf(count), 5, TimeUnit.MINUTES);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", count);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
        // 清除Redis缓存
        if (stringRedisTemplate != null) {
            stringRedisTemplate.delete(REDIS_UNREAD_KEY + userId);
        }
        return ResponseResult.okResult(null);
    }

    @Override
    public ResponseResult createNotification(Long userId, Integer type, String sourceId, String content) {
        if (userId == null || type == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSourceId(sourceId);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationMapper.insert(notification);

        // 更新Redis未读计数
        incrUnreadCache(userId);

        return ResponseResult.okResult(notification.getId());
    }

    @Override
    public void incrUnreadCache(Long userId) {
        if (stringRedisTemplate != null && userId != null) {
            String key = REDIS_UNREAD_KEY + userId;
            stringRedisTemplate.opsForValue().increment(key);
            stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
        }
    }

    private Integer mapType(String type) {
        if (type == null) return null;
        switch (type) {
            case "comment": return 1;
            case "digg": return 2;
            case "follow": return 3;
            case "system": return 4;
            default: return null;
        }
    }

    private Map<String, Object> assembleNotificationData(Notification n, Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("notification_id", String.valueOf(n.getId()));
        data.put("type", getTypeName(n.getType()));
        data.put("is_read", n.getIsRead() == 1);
        data.put("created_at", n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);

        // 解析content JSON中的多态数据
        if (n.getContent() != null) {
            try {
                Map<String, Object> contentMap = objectMapper.readValue(n.getContent(), new TypeReference<Map<String, Object>>() {});
                data.putAll(contentMap);
            } catch (Exception e) {
                data.put("content_preview", n.getContent());
            }
        }
        return data;
    }

    private String getTypeName(Integer type) {
        switch (type) {
            case 1: return "comment";
            case 2: return "digg";
            case 3: return "follow";
            case 4: return "system";
            default: return "unknown";
        }
    }

    private Long getCurrentUserId() {
        ApUser user = AppThreadLocalUtil.getUser();
        return user != null ? user.getId().longValue() : null;
    }
}