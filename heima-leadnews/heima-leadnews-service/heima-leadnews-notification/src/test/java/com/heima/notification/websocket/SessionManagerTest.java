package com.heima.notification.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SessionManager 单元测试")
class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    // ==================== userOnline / userOffline / isOnline ====================

    @Nested
    @DisplayName("用户上下线管理测试")
    class OnlineOfflineTests {

        @Test
        @DisplayName("用户上线后 isOnline 返回 true")
        void shouldReturnTrueWhenUserOnline() {
            sessionManager.userOnline(1001L, "session-001");

            assertTrue(sessionManager.isOnline(1001L));
        }

        @Test
        @DisplayName("用户未上线时 isOnline 返回 false")
        void shouldReturnFalseWhenUserNotOnline() {
            assertFalse(sessionManager.isOnline(9999L));
        }

        @Test
        @DisplayName("用户下线后 isOnline 返回 false")
        void shouldReturnFalseAfterUserOffline() {
            sessionManager.userOnline(1001L, "session-001");
            sessionManager.userOffline(1001L);

            assertFalse(sessionManager.isOnline(1001L));
        }

        @Test
        @DisplayName("多用户同时在线")
        void shouldTrackMultipleUsers() {
            sessionManager.userOnline(1001L, "s1");
            sessionManager.userOnline(1002L, "s2");
            sessionManager.userOnline(1003L, "s3");

            assertTrue(sessionManager.isOnline(1001L));
            assertTrue(sessionManager.isOnline(1002L));
            assertTrue(sessionManager.isOnline(1003L));
            assertEquals(3, sessionManager.getOnlineCount());
        }
    }

    // ==================== getSessionId ====================

    @Nested
    @DisplayName("getSessionId 方法测试")
    class GetSessionIdTests {

        @Test
        @DisplayName("在线用户返回正确的 sessionId")
        void shouldReturnSessionIdWhenOnline() {
            sessionManager.userOnline(1001L, "session-abc");

            assertEquals("session-abc", sessionManager.getSessionId(1001L));
        }

        @Test
        @DisplayName("离线用户返回 null")
        void shouldReturnNullWhenOffline() {
            assertNull(sessionManager.getSessionId(9999L));
        }
    }

    // ==================== getOnlineCount ====================

    @Nested
    @DisplayName("getOnlineCount 方法测试")
    class GetOnlineCountTests {

        @Test
        @DisplayName("初始在线人数为 0")
        void shouldReturnZeroInitially() {
            assertEquals(0, sessionManager.getOnlineCount());
        }

        @Test
        @DisplayName("用户上线后计数增加")
        void shouldIncrementCountOnUserOnline() {
            sessionManager.userOnline(1001L, "s1");
            assertEquals(1, sessionManager.getOnlineCount());

            sessionManager.userOnline(1002L, "s2");
            assertEquals(2, sessionManager.getOnlineCount());
        }

        @Test
        @DisplayName("用户下线后计数减少")
        void shouldDecrementCountOnUserOffline() {
            sessionManager.userOnline(1001L, "s1");
            sessionManager.userOnline(1002L, "s2");
            assertEquals(2, sessionManager.getOnlineCount());

            sessionManager.userOffline(1001L);
            assertEquals(1, sessionManager.getOnlineCount());
        }

        @Test
        @DisplayName("重复上线同一用户不增加计数")
        void shouldNotDuplicateCountForSameUser() {
            sessionManager.userOnline(1001L, "s1");
            sessionManager.userOnline(1001L, "s2");

            assertEquals(1, sessionManager.getOnlineCount());
        }

        @Test
        @DisplayName("下线不存在的用户不影响计数")
        void shouldNotAffectCountWhenOfflineUnknownUser() {
            sessionManager.userOnline(1001L, "s1");
            sessionManager.userOffline(9999L);

            assertEquals(1, sessionManager.getOnlineCount());
        }
    }
}