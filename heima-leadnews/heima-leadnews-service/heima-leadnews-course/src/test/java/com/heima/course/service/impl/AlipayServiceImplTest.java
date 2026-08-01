package com.heima.course.service.impl;

import com.heima.course.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlipayServiceImplTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private AlipayServiceImpl alipayService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alipayService, "appId", "test_app_id");
        ReflectionTestUtils.setField(alipayService, "gatewayUrl", "https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        ReflectionTestUtils.setField(alipayService, "notifyUrl", "http://localhost/course/api/v1/pay/notify");
        ReflectionTestUtils.setField(alipayService, "returnUrl", "http://localhost/course/pay-result");
    }

    // ==================== generatePayPage() tests ====================

    @Test
    void testGeneratePayPageContainsOrderNo() {
        String html = alipayService.generatePayPage("ORDER123", "测试课程", "99.99");
        assertNotNull(html);
        assertTrue(html.contains("ORDER123"));
        assertTrue(html.contains("测试课程"));
        assertTrue(html.contains("99.99"));
        assertTrue(html.contains("支付宝沙箱支付"));
        assertTrue(html.contains("orderNo=ORDER123"));
    }

    @Test
    void testGeneratePayPageContainsReturnUrl() {
        String html = alipayService.generatePayPage("ORDER456", "课程购买", "50.00");
        assertTrue(html.contains("http://localhost/course/pay-result"));
    }

    @Test
    void testGeneratePayPageContainsNotifyEndpoint() {
        String html = alipayService.generatePayPage("ORDER789", "课程", "10.00");
        assertTrue(html.contains("/course/api/v1/pay/notify"));
    }

    @Test
    void testGeneratePayPageContainsSandboxLabel() {
        String html = alipayService.generatePayPage("ORDER", "课程", "1.00");
        assertTrue(html.contains("沙箱环境"));
    }

    // ==================== handleNotify() tests ====================

    @Test
    void testHandleNotifySuccess() {
        boolean result = alipayService.handleNotify("TRADE123", "ORDER123", "99.99", "TRADE_SUCCESS");
        assertTrue(result);
        verify(orderService).handlePaySuccess("ORDER123", "TRADE123");
    }

    @Test
    void testHandleNotifyNonSuccessStatus() {
        boolean result = alipayService.handleNotify("TRADE123", "ORDER123", "99.99", "WAIT_BUYER_PAY");
        assertFalse(result);
        verify(orderService, never()).handlePaySuccess(any(), any());
    }

    @Test
    void testHandleNotifyTradeClosed() {
        boolean result = alipayService.handleNotify("TRADE123", "ORDER123", "99.99", "TRADE_CLOSED");
        assertFalse(result);
        verify(orderService, never()).handlePaySuccess(any(), any());
    }

    // ==================== verifySign() tests ====================

    @Test
    void testVerifySign() {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", "ORDER123");
        params.put("trade_no", "TRADE123");
        boolean result = alipayService.verifySign(params);
        assertTrue(result);
    }
}