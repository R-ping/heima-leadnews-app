package com.heima.notification.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserInterceptor 单元测试")
class UserInterceptorTest {

    @Mock
    private SessionManager sessionManager;

    @InjectMocks
    private UserInterceptor userInterceptor;

    // ==================== preSend ====================

    @Nested
    @DisplayName("preSend 方法测试")
    class PreSendTests {

        @Test
        @DisplayName("CONNECT 命令带 userId - 设置 Principal")
        void shouldSetUserWhenConnectWithUserId() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.setNativeHeader("userId", "1001");
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
            MessageChannel channel = mock(MessageChannel.class);

            Message<?> result = userInterceptor.preSend(message, channel);

            assertNotNull(result);
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            Principal principal = resultAccessor.getUser();
            assertNotNull(principal);
            assertEquals("1001", principal.getName());
        }

        @Test
        @DisplayName("CONNECT 命令不带 userId - 不设置 Principal")
        void shouldNotSetUserWhenConnectWithoutUserId() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
            MessageChannel channel = mock(MessageChannel.class);

            Message<?> result = userInterceptor.preSend(message, channel);

            assertNotNull(result);
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            assertNull(resultAccessor.getUser());
        }

        @Test
        @DisplayName("非 CONNECT 命令 - 不处理")
        void shouldNotProcessNonConnectCommand() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setNativeHeader("userId", "1001");
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
            MessageChannel channel = mock(MessageChannel.class);

            Message<?> result = userInterceptor.preSend(message, channel);

            assertNotNull(result);
            StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
            assertNull(resultAccessor.getUser());
        }

        @Test
        @DisplayName("DISCONNECT 命令 - 不处理")
        void shouldNotProcessDisconnectCommand() {
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
            Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
            MessageChannel channel = mock(MessageChannel.class);

            Message<?> result = userInterceptor.preSend(message, channel);

            assertNotNull(result);
        }
    }

    // ==================== StompPrincipal ====================

    @Nested
    @DisplayName("StompPrincipal 内部类测试")
    class StompPrincipalTests {

        @Test
        @DisplayName("getName 返回构造时传入的名称")
        void shouldReturnName() {
            UserInterceptor.StompPrincipal principal = new UserInterceptor.StompPrincipal("1001");

            assertEquals("1001", principal.getName());
        }

        @Test
        @DisplayName("不同实例的 getName 返回不同值")
        void shouldReturnDifferentNames() {
            UserInterceptor.StompPrincipal p1 = new UserInterceptor.StompPrincipal("1001");
            UserInterceptor.StompPrincipal p2 = new UserInterceptor.StompPrincipal("2002");

            assertEquals("1001", p1.getName());
            assertEquals("2002", p2.getName());
        }
    }
}