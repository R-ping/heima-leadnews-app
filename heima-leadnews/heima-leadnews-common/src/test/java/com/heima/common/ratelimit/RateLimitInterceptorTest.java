package com.heima.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimitInterceptor 单元测试 — 验证限流逻辑
 */
@DisplayName("API限流拦截器测试")
class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("登录接口 — 10次内应放行")
    void testLoginRateLimitWithinLimit() throws Exception {
        request.setRequestURI("/api/v1/login/login_auth");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            assertTrue(interceptor.preHandle(request, response, null),
                "第" + (i + 1) + "次请求应被放行");
        }
    }

    @Test
    @DisplayName("登录接口 — 超过10次应拒绝")
    void testLoginRateLimitExceeded() throws Exception {
        request.setRequestURI("/api/v1/login/login_auth");
        request.setRemoteAddr("192.168.1.2");

        // 前10次放行
        for (int i = 0; i < 10; i++) {
            assertTrue(interceptor.preHandle(request, response, null));
        }
        // 第11次拒绝
        assertFalse(interceptor.preHandle(request, response, null));
        assertEquals(429, response.getStatus());
    }

    @Test
    @DisplayName("上传接口 — 20次内应放行")
    void testUploadRateLimitWithinLimit() throws Exception {
        request.setRequestURI("/wemedia/api/v1/material/upload_picture");
        request.setRemoteAddr("192.168.1.3");

        for (int i = 0; i < 20; i++) {
            assertTrue(interceptor.preHandle(request, response, null),
                "第" + (i + 1) + "次上传请求应被放行");
        }
    }

    @Test
    @DisplayName("上传接口 — 超过20次应拒绝")
    void testUploadRateLimitExceeded() throws Exception {
        request.setRequestURI("/wemedia/api/v1/material/upload_picture");
        request.setRemoteAddr("192.168.1.4");

        for (int i = 0; i < 20; i++) {
            assertTrue(interceptor.preHandle(request, response, null));
        }
        assertFalse(interceptor.preHandle(request, response, null));
        assertEquals(429, response.getStatus());
    }

    @Test
    @DisplayName("普通接口 — 不限流")
    void testNormalEndpointNoRateLimit() throws Exception {
        request.setRequestURI("/api/v1/article/list");
        request.setRemoteAddr("192.168.1.5");

        // 普通接口调用100次都应放行
        for (int i = 0; i < 100; i++) {
            assertTrue(interceptor.preHandle(request, response, null));
        }
    }

    @Test
    @DisplayName("不同IP — 互不影响")
    void testDifferentIpIndependent() throws Exception {
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.setRequestURI("/api/v1/login/login_auth");
        req1.setRemoteAddr("10.0.0.1");

        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setRequestURI("/api/v1/login/login_auth");
        req2.setRemoteAddr("10.0.0.2");

        // IP1 用完10次
        for (int i = 0; i < 10; i++) {
            assertTrue(interceptor.preHandle(req1, response, null));
        }
        assertFalse(interceptor.preHandle(req1, response, null));

        // IP2 不受影响
        assertTrue(interceptor.preHandle(req2, response, null));
    }

    @Test
    @DisplayName("X-Forwarded-For 代理IP识别")
    void testProxyIpDetection() throws Exception {
        request.setRequestURI("/api/v1/login/login_auth");
        request.addHeader("X-Forwarded-For", "172.16.0.1, 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");

        // 应使用 X-Forwarded-For 中的第一个IP
        assertTrue(interceptor.preHandle(request, response, null));
    }
}