package com.heima.course.controller.v1;

import com.heima.course.service.OrderService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== createOrder() tests ====================

    @Test
    void testCreateOrderSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", "100");
        params.put("discountCode", "CODE20");
        when(orderService.createOrder(100L, "CODE20", 1L))
                .thenReturn(ResponseResult.okResult("success"));

        ResponseResult result = orderController.createOrder(params);
        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateOrderNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", "100");

        ResponseResult result = orderController.createOrder(params);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(orderService, never()).createOrder(any(), any(), any());
    }

    @Test
    void testCreateOrderWithoutDiscountCode() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        Map<String, Object> params = new HashMap<>();
        params.put("courseId", "100");
        when(orderService.createOrder(100L, null, 1L))
                .thenReturn(ResponseResult.okResult("success"));

        ResponseResult result = orderController.createOrder(params);
        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateOrderWithoutCourseId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        Map<String, Object> params = new HashMap<>();
        when(orderService.createOrder(null, null, 1L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = orderController.createOrder(params);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== getOrderStatus() tests ====================

    @Test
    void testGetOrderStatusSuccess() {
        when(orderService.getOrderStatus("ORDER123"))
                .thenReturn(ResponseResult.okResult("status"));

        ResponseResult result = orderController.getOrderStatus("ORDER123");
        assertEquals(200, result.getCode());
    }

    @Test
    void testGetOrderStatusNotFound() {
        when(orderService.getOrderStatus("NONEXISTENT"))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = orderController.getOrderStatus("NONEXISTENT");
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== getMyOrders() tests ====================

    @Test
    void testGetMyOrdersSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(orderService.getMyOrders(1L, 1, 10))
                .thenReturn(ResponseResult.okResult("orders"));

        ResponseResult result = orderController.getMyOrders(1, 10);
        assertEquals(200, result.getCode());
    }

    @Test
    void testGetMyOrdersNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = orderController.getMyOrders(1, 10);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(orderService, never()).getMyOrders(any(), any(), any());
    }
}