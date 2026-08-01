package com.heima.course.controller.v1;

import com.heima.course.service.AlipayService;
import com.heima.course.service.OrderService;
import com.heima.model.article.pojos.ApCourseOrder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayControllerTest {

    @Mock
    private AlipayService alipayService;

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PayController payController;

    // ==================== payPage() tests ====================

    @Test
    void testPayPageSuccess() {
        ApCourseOrder order = new ApCourseOrder();
        order.setOrderNo("ORDER123");
        order.setCourseId(100L);
        order.setPaidAmount(new java.math.BigDecimal("99.99"));
        order.setStatus(ApCourseOrder.Status.PENDING.getCode());
        when(orderService.getByOrderNo("ORDER123")).thenReturn(order);
        when(alipayService.generatePayPage("ORDER123", "课程购买 - 100", "99.99"))
                .thenReturn("<html>pay page</html>");

        String result = payController.payPage("ORDER123");
        assertTrue(result.contains("pay page"));
    }

    @Test
    void testPayPageOrderNotFound() {
        when(orderService.getByOrderNo("NONEXISTENT")).thenReturn(null);
        String result = payController.payPage("NONEXISTENT");
        assertTrue(result.contains("订单不存在"));
    }

    @Test
    void testPayPageOrderStatusNotPending() {
        ApCourseOrder order = new ApCourseOrder();
        order.setOrderNo("ORDER123");
        order.setStatus(ApCourseOrder.Status.PAID.getCode());
        when(orderService.getByOrderNo("ORDER123")).thenReturn(order);

        String result = payController.payPage("ORDER123");
        assertTrue(result.contains("订单状态异常"));
        verify(alipayService, never()).generatePayPage(any(), any(), any());
    }

    @Test
    void testPayPageOrderCancelled() {
        ApCourseOrder order = new ApCourseOrder();
        order.setOrderNo("ORDER123");
        order.setStatus(ApCourseOrder.Status.CANCELLED.getCode());
        when(orderService.getByOrderNo("ORDER123")).thenReturn(order);

        String result = payController.payPage("ORDER123");
        assertTrue(result.contains("订单状态异常"));
    }

    @Test
    void testPayPageOrderRefunded() {
        ApCourseOrder order = new ApCourseOrder();
        order.setOrderNo("ORDER123");
        order.setStatus(ApCourseOrder.Status.REFUNDED.getCode());
        when(orderService.getByOrderNo("ORDER123")).thenReturn(order);

        String result = payController.payPage("ORDER123");
        assertTrue(result.contains("订单状态异常"));
    }

    // ==================== payNotify() tests ====================

    @Test
    void testPayNotifySuccess() {
        when(request.getParameter("trade_no")).thenReturn("TRADE123");
        when(request.getParameter("out_trade_no")).thenReturn("ORDER123");
        when(request.getParameter("total_amount")).thenReturn("99.99");
        when(request.getParameter("trade_status")).thenReturn("TRADE_SUCCESS");
        when(alipayService.handleNotify("TRADE123", "ORDER123", "99.99", "TRADE_SUCCESS"))
                .thenReturn(true);

        String result = payController.payNotify(request);
        assertEquals("success", result);
    }

    @Test
    void testPayNotifyFail() {
        when(request.getParameter("trade_no")).thenReturn("TRADE123");
        when(request.getParameter("out_trade_no")).thenReturn("ORDER123");
        when(request.getParameter("total_amount")).thenReturn("99.99");
        when(request.getParameter("trade_status")).thenReturn("WAIT_BUYER_PAY");
        when(alipayService.handleNotify("TRADE123", "ORDER123", "99.99", "WAIT_BUYER_PAY"))
                .thenReturn(false);

        String result = payController.payNotify(request);
        assertEquals("fail", result);
    }
}