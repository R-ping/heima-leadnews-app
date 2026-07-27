package com.heima.notification.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.model.notification.dtos.ImReadDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.notification.service.ImService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImController 单元测试")
class ImControllerTest {

    @Mock
    private ImService imService;

    @InjectMocks
    private ImController imController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1001);
        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== listSessions ====================

    @Nested
    @DisplayName("listSessions 方法测试")
    class ListSessionsTests {

        @Test
        @DisplayName("正常获取会话列表")
        void shouldReturnSessionsWhenUserExists() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(imService.listSessions(1001L)).thenReturn(expected);

            ResponseResult result = imController.listSessions();

            assertSame(expected, result);
            verify(imService).listSessions(1001L);
        }

        @Test
        @DisplayName("用户未登录 - 传入null")
        void shouldPassNullWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
            when(imService.listSessions(null)).thenReturn(ResponseResult.errorResult(401, "未登录"));

            imController.listSessions();

            verify(imService).listSessions(null);
        }
    }

    // ==================== listMessages ====================

    @Nested
    @DisplayName("listMessages 方法测试")
    class ListMessagesTests {

        @Test
        @DisplayName("正常获取消息列表 - 带游标分页")
        void shouldReturnMessagesWithCursor() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(imService.listMessages(eq(1001L), eq(10L), eq(50L), eq(20)))
                    .thenReturn(expected);

            ResponseResult result = imController.listMessages(10L, 50L, 20);

            assertSame(expected, result);
        }

        @Test
        @DisplayName("获取消息列表 - 不带游标")
        void shouldReturnMessagesWithoutCursor() {
            when(imService.listMessages(eq(1001L), eq(10L), isNull(), eq(20)))
                    .thenReturn(ResponseResult.okResult(null));

            imController.listMessages(10L, null, 20);

            verify(imService).listMessages(1001L, 10L, null, 20);
        }

        @Test
        @DisplayName("获取消息列表 - 默认size")
        void shouldUseDefaultSizeWhenNotProvided() {
            imController.listMessages(10L, null, null);

            verify(imService).listMessages(1001L, 10L, null, null);
        }
    }

    // ==================== sendMessage ====================

    @Nested
    @DisplayName("sendMessage 方法测试")
    class SendMessageTests {

        @Test
        @DisplayName("正常发送消息")
        void shouldSendMessageSuccessfully() {
            ImMessageDto dto = new ImMessageDto();
            dto.setReceiverId(2001L);
            dto.setContent("Hello");
            ResponseResult expected = ResponseResult.okResult(null);
            when(imService.sendMessage(eq(1001L), any(ImMessageDto.class))).thenReturn(expected);

            ResponseResult result = imController.sendMessage(dto);

            assertSame(expected, result);
        }

        @Test
        @DisplayName("发送消息 - 内容为空")
        void shouldSendMessageWithEmptyContent() {
            ImMessageDto dto = new ImMessageDto();
            dto.setReceiverId(2001L);
            dto.setContent("");
            when(imService.sendMessage(eq(1001L), any(ImMessageDto.class)))
                    .thenReturn(ResponseResult.errorResult(400, "参数无效"));

            imController.sendMessage(dto);

            verify(imService).sendMessage(1001L, dto);
        }
    }

    // ==================== markRead ====================

    @Nested
    @DisplayName("markRead 方法测试")
    class MarkReadTests {

        @Test
        @DisplayName("正常标记已读")
        void shouldMarkReadSuccessfully() {
            ImReadDto dto = new ImReadDto();
            dto.setSessionId(10L);
            dto.setLastReadId(100L);
            ResponseResult expected = ResponseResult.okResult(null);
            when(imService.markRead(eq(1001L), any(ImReadDto.class))).thenReturn(expected);

            ResponseResult result = imController.markRead(dto);

            assertSame(expected, result);
        }
    }
}