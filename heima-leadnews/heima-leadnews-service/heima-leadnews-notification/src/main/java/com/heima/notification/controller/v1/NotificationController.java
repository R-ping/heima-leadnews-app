package com.heima.notification.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.NotificationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.notification.service.NotificationService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private Long getUserId() {
        ApUser user = AppThreadLocalUtil.getUser();
        return user != null ? user.getId().longValue() : null;
    }

    @GetMapping
    public ResponseResult list(NotificationDto dto) {
        return notificationService.list(dto);
    }

    @PostMapping("/actions/reply")
    public ResponseResult reply(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        Long commentId = Long.valueOf(body.get("comment_id").toString());
        String content = (String) body.get("content");
        return notificationService.reply(userId, commentId, content);
    }

    @PostMapping("/actions/like")
    public ResponseResult like(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        Long commentId = Long.valueOf(body.get("comment_id").toString());
        return notificationService.toggleLike(userId, commentId);
    }

    @PostMapping("/actions/follow-back")
    public ResponseResult followBack(@RequestBody Map<String, Object> body) {
        Long userId = getUserId();
        Long followerId = Long.valueOf(body.get("follower_id").toString());
        return notificationService.followBack(userId, followerId);
    }

    @GetMapping("/unread-count")
    public ResponseResult unreadCount() {
        return notificationService.unreadCount(getUserId());
    }

    @PostMapping("/mark-all-read")
    public ResponseResult markAllRead() {
        return notificationService.markAllRead(getUserId());
    }

    /**
     * Feign内部接口：创建通知
     */
    @PostMapping("/feign/create")
    public ResponseResult createNotification(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer type = Integer.valueOf(params.get("type").toString());
        String sourceId = params.get("sourceId") != null ? params.get("sourceId").toString() : null;
        String content = params.get("content") != null ? params.get("content").toString() : null;
        return notificationService.createNotification(userId, type, sourceId, content);
    }

    /**
     * Feign内部接口：增加未读计数
     */
    @PostMapping("/feign/incr-unread")
    public void incrUnread(@RequestParam("userId") Long userId) {
        notificationService.incrUnreadCache(userId);
    }
}