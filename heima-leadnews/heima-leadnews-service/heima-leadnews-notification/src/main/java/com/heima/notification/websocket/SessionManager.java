package com.heima.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {

    private final Map<Long, String> onlineUsers = new ConcurrentHashMap<>();

    public void userOnline(Long userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        log.info("User online: userId={}, sessionId={}", userId, sessionId);
    }

    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        log.info("User offline: userId={}", userId);
    }

    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    public String getSessionId(Long userId) {
        return onlineUsers.get(userId);
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }
}