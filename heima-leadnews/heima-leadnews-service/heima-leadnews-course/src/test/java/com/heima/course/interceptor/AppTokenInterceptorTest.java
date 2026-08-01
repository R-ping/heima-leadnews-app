package com.heima.course.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppTokenInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AppTokenInterceptor interceptor;
    private MockedStatic<AppThreadLocalUtil> threadLocalMock;

    @BeforeEach
    void setUp() {
        interceptor = new AppTokenInterceptor();
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== preHandle() tests ====================

    @Test
    void testPreHandleWithUserId() {
        when(request.getHeader("userId")).thenReturn("123");

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        threadLocalMock.verify(() -> AppThreadLocalUtil.setUser(argThat(user ->
                user.getId() == 123
        )));
    }

    @Test
    void testPreHandleWithoutUserId() {
        when(request.getHeader("userId")).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        threadLocalMock.verify(() -> AppThreadLocalUtil.setUser(any()), never());
    }

    // ==================== afterCompletion() tests ====================

    @Test
    void testAfterCompletion() {
        interceptor.afterCompletion(request, response, null, null);
        threadLocalMock.verify(AppThreadLocalUtil::clear);
    }
}