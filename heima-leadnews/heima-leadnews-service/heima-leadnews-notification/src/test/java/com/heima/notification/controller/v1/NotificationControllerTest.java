package com.heima.notification.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.NotificationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.notification.service.NotificationService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController 单元测试")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;

    @BeforeEach
    void setUp() {
        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
        ApUser mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("测试用户");
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== list ====================

    @Nested
    @DisplayName("list 方法测试")
    class ListTests {

        @Test
        @DisplayName("正常获取通知列表")
        void shouldListNotificationsSuccessfully() {
            NotificationDto dto = new NotificationDto();
            ResponseResult expected = ResponseResult.okResult("notification-list");
            when(notificationService.list(dto)).thenReturn(expected);

            ResponseResult result = controller.list(dto);

            assertSame(expected, result);
            verify(notificationService).list(dto);
        }

        @Test
        @DisplayName("获取通知列表 - 服务返回错误")
        void shouldReturnErrorWhenListFails() {
            NotificationDto dto = new NotificationDto();
            ResponseResult expected = ResponseResult.errorResult(500, "获取通知列表失败");
            when(notificationService.list(dto)).thenReturn(expected);

            ResponseResult result = controller.list(dto);

            assertEquals(500, result.getCode());
        }
    }

    // ==================== reply ====================

    @Nested
    @DisplayName("reply 方法测试")
    class ReplyTests {

        @Test
        @DisplayName("正常回复评论")
        void shouldReplySuccessfully() {
            Map<String, Object> body = new HashMap<>();
            body.put("comment_id", "1001");
            body.put("content", "回复内容");

            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.reply(1L, 1001L, "回复内容")).thenReturn(expected);

            ResponseResult result = controller.reply(body);

            assertSame(expected, result);
            verify(notificationService).reply(1L, 1001L, "回复内容");
        }
    }

    // ==================== like ====================

    @Nested
    @DisplayName("like 方法测试")
    class LikeTests {

        @Test
        @DisplayName("正常点赞评论")
        void shouldLikeSuccessfully() {
            Map<String, Object> body = new HashMap<>();
            body.put("comment_id", "2001");

            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.toggleLike(1L, 2001L)).thenReturn(expected);

            ResponseResult result = controller.like(body);

            assertSame(expected, result);
            verify(notificationService).toggleLike(1L, 2001L);
        }
    }

    // ==================== followBack ====================

    @Nested
    @DisplayName("followBack 方法测试")
    class FollowBackTests {

        @Test
        @DisplayName("正常回关用户")
        void shouldFollowBackSuccessfully() {
            Map<String, Object> body = new HashMap<>();
            body.put("follower_id", "3001");

            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.followBack(1L, 3001L)).thenReturn(expected);

            ResponseResult result = controller.followBack(body);

            assertSame(expected, result);
            verify(notificationService).followBack(1L, 3001L);
        }
    }

    // ==================== unreadCount ====================

    @Nested
    @DisplayName("unreadCount 方法测试")
    class UnreadCountTests {

        @Test
        @DisplayName("正常获取未读计数")
        void shouldGetUnreadCountSuccessfully() {
            ResponseResult expected = ResponseResult.okResult(5);
            when(notificationService.unreadCount(1L)).thenReturn(expected);

            ResponseResult result = controller.unreadCount();

            assertSame(expected, result);
            verify(notificationService).unreadCount(1L);
        }

        @Test
        @DisplayName("获取未读计数 - 返回0")
        void shouldReturnZeroUnread() {
            ResponseResult expected = ResponseResult.okResult(0);
            when(notificationService.unreadCount(1L)).thenReturn(expected);

            ResponseResult result = controller.unreadCount();

            assertEquals(200, result.getCode());
            assertEquals(0, result.getData());
        }
    }

    // ==================== markAllRead ====================

    @Nested
    @DisplayName("markAllRead 方法测试")
    class MarkAllReadTests {

        @Test
        @DisplayName("正常标记全部已读")
        void shouldMarkAllReadSuccessfully() {
            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.markAllRead(1L)).thenReturn(expected);

            ResponseResult result = controller.markAllRead();

            assertSame(expected, result);
            verify(notificationService).markAllRead(1L);
        }

        @Test
        @DisplayName("标记全部已读 - 服务返回错误")
        void shouldReturnErrorWhenMarkAllReadFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "标记已读失败");
            when(notificationService.markAllRead(1L)).thenReturn(expected);

            ResponseResult result = controller.markAllRead();

            assertEquals(500, result.getCode());
        }
    }

    // ==================== createNotification ====================

    @Nested
    @DisplayName("createNotification 方法测试")
    class CreateNotificationTests {

        @Test
        @DisplayName("正常创建通知（Feign内部接口）")
        void shouldCreateNotificationSuccessfully() {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", "1001");
            params.put("type", "1");
            params.put("sourceId", "article-123");
            params.put("content", "通知内容");

            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.createNotification(1001L, 1, "article-123", "通知内容"))
                    .thenReturn(expected);

            ResponseResult result = controller.createNotification(params);

            assertSame(expected, result);
            verify(notificationService).createNotification(1001L, 1, "article-123", "通知内容");
        }

        @Test
        @DisplayName("创建通知 - sourceId和content为null")
        void shouldCreateNotificationWithNullFields() {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", "2001");
            params.put("type", "2");

            ResponseResult expected = ResponseResult.okResult();
            when(notificationService.createNotification(2001L, 2, null, null))
                    .thenReturn(expected);

            ResponseResult result = controller.createNotification(params);

            assertSame(expected, result);
            verify(notificationService).createNotification(2001L, 2, null, null);
        }
    }

    // ==================== incrUnread ====================

    @Nested
    @DisplayName("incrUnread 方法测试")
    class IncrUnreadTests {

        @Test
        @DisplayName("正常增加未读计数（Feign内部接口）")
        void shouldIncrUnreadSuccessfully() {
            doNothing().when(notificationService).incrUnreadCache(1001L);

            controller.incrUnread(1001L);

            verify(notificationService).incrUnreadCache(1001L);
        }

        @Test
        @DisplayName("增加未读计数 - userId为null")
        void shouldHandleNullUserId() {
            doNothing().when(notificationService).incrUnreadCache(null);

            controller.incrUnread(null);

            verify(notificationService).incrUnreadCache(null);
        }
    }
}