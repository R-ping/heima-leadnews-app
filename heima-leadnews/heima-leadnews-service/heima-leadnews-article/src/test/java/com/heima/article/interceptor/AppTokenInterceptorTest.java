package com.heima.article.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppTokenInterceptor 单元测试")
class AppTokenInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    private AppTokenInterceptor interceptor;
    private MockedStatic<AppThreadLocalUtil> mockedThreadLocal;

    @BeforeEach
    void setUp() {
        interceptor = new AppTokenInterceptor();
        mockedThreadLocal = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedThreadLocal.close();
    }

    @Nested
    @DisplayName("preHandle() - 请求前置处理")
    class PreHandleTests {

        @Test
        @DisplayName("header中有userId时，应存入ThreadLocal")
        void shouldSetUserToThreadLocalWhenUserIdPresent() throws Exception {
            when(request.getHeader("userId")).thenReturn("12345");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)));
        }

        @Test
        @DisplayName("header中userId为有效数字字符串，ApUser应设置正确的id")
        void shouldSetCorrectUserId() throws Exception {
            when(request.getHeader("userId")).thenReturn("100");

            interceptor.preHandle(request, response, handler);

            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)));
        }

        @Test
        @DisplayName("header中没有userId时，不应调用setUser")
        void shouldNotSetUserWhenUserIdAbsent() throws Exception {
            when(request.getHeader("userId")).thenReturn(null);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)), never());
        }

        @Test
        @DisplayName("header中userId为空字符串时，抛出NumberFormatException")
        void shouldThrowWhenUserIdIsEmpty() {
            when(request.getHeader("userId")).thenReturn("");

            assertThrows(NumberFormatException.class, () -> {
                interceptor.preHandle(request, response, handler);
            });
        }

        @Test
        @DisplayName("无论是否有userId，preHandle总是返回true")
        void shouldAlwaysReturnTrue() throws Exception {
            when(request.getHeader("userId")).thenReturn(null);

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
        }

        @Test
        @DisplayName("有userId时preHandle也返回true")
        void shouldReturnTrueWhenUserIdPresent() throws Exception {
            when(request.getHeader("userId")).thenReturn("1");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
        }

        @Test
        @DisplayName("userId为负数时，正常解析")
        void shouldHandleNegativeUserId() throws Exception {
            when(request.getHeader("userId")).thenReturn("-1");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)));
        }

        @Test
        @DisplayName("userId为0时，正常解析")
        void shouldHandleZeroUserId() throws Exception {
            when(request.getHeader("userId")).thenReturn("0");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)));
        }

        @Test
        @DisplayName("userId为超大数字时，正常解析")
        void shouldHandleLargeUserId() throws Exception {
            when(request.getHeader("userId")).thenReturn("2147483647");

            boolean result = interceptor.preHandle(request, response, handler);

            assertTrue(result);
            mockedThreadLocal.verify(() -> AppThreadLocalUtil.setUser(any(ApUser.class)));
        }
    }

    @Nested
    @DisplayName("postHandle() - 请求后置处理")
    class PostHandleTests {

        @Test
        @DisplayName("postHandle不抛异常")
        void shouldNotThrowException() {
            assertDoesNotThrow(() -> interceptor.postHandle(request, response, handler, null));
        }
    }

    @Nested
    @DisplayName("afterCompletion() - 请求完成处理")
    class AfterCompletionTests {

        @Test
        @DisplayName("正常完成时不抛异常")
        void shouldNotThrowExceptionOnCompletion() {
            assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, handler, null));
        }

        @Test
        @DisplayName("带异常完成时不抛异常")
        void shouldNotThrowExceptionWithException() {
            assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, handler,
                    new RuntimeException("test exception")));
        }
    }
}