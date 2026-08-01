package com.heima.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.course.mapper.ApCourseDiscountMapper;
import com.heima.course.mapper.ApCourseMapper;
import com.heima.course.mapper.ApCourseOrderMapper;
import com.heima.course.mapper.ApUserCourseMapper;
import com.heima.course.service.DiscountService;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.article.pojos.ApCourseDiscount;
import com.heima.model.article.pojos.ApCourseOrder;
import com.heima.model.article.pojos.ApUserCourse;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private ApCourseOrderMapper orderMapper;

    @Mock
    private ApCourseMapper courseMapper;

    @Mock
    private ApUserCourseMapper userCourseMapper;

    @Mock
    private ApCourseDiscountMapper discountMapper;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ApCourseOrder mockOrder;

    @BeforeEach
    void setUp() {
        mockOrder = new ApCourseOrder();
        mockOrder.setId(1L);
        mockOrder.setOrderNo("20250101120000ABC123");
        mockOrder.setUserId(1);
        mockOrder.setCourseId(100L);
        mockOrder.setOriginalAmount(new BigDecimal("100.00"));
        mockOrder.setDiscountAmount(BigDecimal.ZERO);
        mockOrder.setPaidAmount(new BigDecimal("100.00"));
        mockOrder.setDiscountCode("");
        mockOrder.setStatus(ApCourseOrder.Status.PENDING.getCode());
        mockOrder.setCreatedTime(new Date());
        mockOrder.setUpdatedTime(new Date());
    }

    @AfterEach
    void tearDown() {
        // No static mocks used in this test
    }

    // ==================== createOrder() tests ====================

    @Test
    void testCreateOrderSuccess() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);
        when(orderMapper.insert(any(ApCourseOrder.class))).thenReturn(1);

        ResponseResult result = orderService.createOrder(100L, null, 1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        ApCourseOrder order = (ApCourseOrder) result.getData();
        assertEquals(new BigDecimal("100.00"), order.getPaidAmount());
        assertEquals(new BigDecimal("100.00"), order.getOriginalAmount());
        assertEquals(BigDecimal.ZERO, order.getDiscountAmount());
        assertEquals(ApCourseOrder.Status.PENDING.getCode(), order.getStatus());
    }

    @Test
    void testCreateOrderNullCourseId() {
        ResponseResult result = orderService.createOrder(null, null, 1L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateOrderNullUserId() {
        ResponseResult result = orderService.createOrder(100L, null, null);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateOrderCourseNotFound() {
        when(courseMapper.selectById(100L)).thenReturn(null);
        ResponseResult result = orderService.createOrder(100L, null, 1L);
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testCreateOrderCourseDeleted() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setIsDeleted(1);
        when(courseMapper.selectById(100L)).thenReturn(course);
        ResponseResult result = orderService.createOrder(100L, null, 1L);
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testCreateOrderWithNullPrice() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(null);
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);
        when(orderMapper.insert(any(ApCourseOrder.class))).thenReturn(1);

        ResponseResult result = orderService.createOrder(100L, null, 1L);
        assertEquals(200, result.getCode());
        ApCourseOrder order = (ApCourseOrder) result.getData();
        assertEquals(BigDecimal.ZERO, order.getOriginalAmount());
    }

    @Test
    void testCreateOrderWithInvalidDiscountCode() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);
        when(discountService.validateDiscount("INVALID", 100L)).thenReturn(null);

        ResponseResult result = orderService.createOrder(100L, "INVALID", 1L);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateOrderWithFixedDiscount() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);

        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setDiscountType(ApCourseDiscount.DiscountType.FIXED.getCode());
        discount.setDiscountValue(new BigDecimal("20.00"));
        when(discountService.validateDiscount("FIXED20", 100L)).thenReturn(discount);
        when(orderMapper.insert(any(ApCourseOrder.class))).thenReturn(1);

        ResponseResult result = orderService.createOrder(100L, "FIXED20", 1L);
        assertEquals(200, result.getCode());
        ApCourseOrder order = (ApCourseOrder) result.getData();
        assertEquals(new BigDecimal("20.00"), order.getDiscountAmount());
        assertEquals(0, new BigDecimal("80.00").compareTo(order.getPaidAmount()));
    }

    @Test
    void testCreateOrderWithPercentageDiscount() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);

        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setDiscountType(ApCourseDiscount.DiscountType.PERCENTAGE.getCode());
        discount.setDiscountValue(new BigDecimal("20"));
        when(discountService.validateDiscount("PCT20", 100L)).thenReturn(discount);
        when(orderMapper.insert(any(ApCourseOrder.class))).thenReturn(1);

        ResponseResult result = orderService.createOrder(100L, "PCT20", 1L);
        assertEquals(200, result.getCode());
        ApCourseOrder order = (ApCourseOrder) result.getData();
        assertEquals(0, new BigDecimal("80.00").compareTo(order.getPaidAmount()));
    }

    @Test
    void testCreateOrderDiscountExceedsAmount() {
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setPrice(new BigDecimal("50.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);

        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setDiscountType(ApCourseDiscount.DiscountType.FIXED.getCode());
        discount.setDiscountValue(new BigDecimal("100.00"));
        when(discountService.validateDiscount("BIG", 100L)).thenReturn(discount);
        when(orderMapper.insert(any(ApCourseOrder.class))).thenReturn(1);

        ResponseResult result = orderService.createOrder(100L, "BIG", 1L);
        assertEquals(200, result.getCode());
        ApCourseOrder order = (ApCourseOrder) result.getData();
        assertEquals(BigDecimal.ZERO, order.getPaidAmount());
    }

    // ==================== getOrderStatus() tests ====================

    @Test
    void testGetOrderStatusSuccess() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        ResponseResult result = orderService.getOrderStatus("20250101120000ABC123");
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetOrderStatusNotFound() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        ResponseResult result = orderService.getOrderStatus("NONEXISTENT");
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== getMyOrders() tests ====================

    @Test
    void testGetMyOrdersSuccess() {
        Page<ApCourseOrder> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(mockOrder));
        page.setTotal(1);
        when(orderMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        ResponseResult result = orderService.getMyOrders(1L, 1, 10);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
        assertEquals(1L, data.get("total"));
    }

    @Test
    void testGetMyOrdersEmpty() {
        Page<ApCourseOrder> page = new Page<>(1, 10, 0);
        page.setRecords(Collections.emptyList());
        when(orderMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        ResponseResult result = orderService.getMyOrders(1L, 1, 10);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("total"));
    }

    // ==================== handlePaySuccess() tests ====================

    @Test
    void testHandlePaySuccessOrderNotFound() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        orderService.handlePaySuccess("NONEXISTENT", "TRADE123");
        verify(orderMapper, never()).updateById(any(ApCourseOrder.class));
    }

    @Test
    void testHandlePaySuccessOrderStatusNotPending() {
        mockOrder.setStatus(ApCourseOrder.Status.PAID.getCode());
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        orderService.handlePaySuccess("ORDER123", "TRADE123");
        verify(orderMapper, never()).updateById(any(ApCourseOrder.class));
    }

    @Test
    void testHandlePaySuccessWithoutDiscount() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setStudyCount(0);
        course.setSalesCount(0);
        course.setTotalRevenue(new BigDecimal("500.00"));
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);
        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userCourseMapper.insert(any(ApUserCourse.class))).thenReturn(1);

        orderService.handlePaySuccess("ORDER123", "TRADE123");

        verify(orderMapper).updateById(Mockito.<ApCourseOrder>argThat(order ->
                order.getStatus() == ApCourseOrder.Status.PAID.getCode()
                        && "TRADE123".equals(order.getTradeNo())
        ));
        verify(courseMapper).updateById(Mockito.<ApCourse>argThat(c ->
                c.getStudyCount() == 1 && c.getSalesCount() == 1
                        && c.getTotalRevenue().compareTo(new BigDecimal("600.00")) == 0
        ));
        verify(userCourseMapper).insert(any(ApUserCourse.class));
    }

    @Test
    void testHandlePaySuccessWithDiscountCode() {
        mockOrder.setDiscountCode("DISC20");
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);

        ApCourseDiscount discount = new ApCourseDiscount();
        discount.setId(1L);
        discount.setCode("DISC20");
        discount.setUsedCount(5);
        when(discountService.getDiscountByCode("DISC20")).thenReturn(discount);

        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setStudyCount(10);
        course.setSalesCount(10);
        course.setTotalRevenue(new BigDecimal("1000.00"));
        course.setPrice(new BigDecimal("100.00"));
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);

        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userCourseMapper.insert(any(ApUserCourse.class))).thenReturn(1);

        orderService.handlePaySuccess("ORDER123", "TRADE123");

        verify(discountMapper).updateById(Mockito.<ApCourseDiscount>argThat(d ->
                d.getUsedCount() == 6
        ));
        verify(courseMapper).updateById(any(ApCourse.class));
        verify(userCourseMapper).insert(any(ApUserCourse.class));
    }

    @Test
    void testHandlePaySuccessDiscountCodeNotFound() {
        mockOrder.setDiscountCode("DISC20");
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        when(discountService.getDiscountByCode("DISC20")).thenReturn(null);

        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setStudyCount(0);
        course.setSalesCount(0);
        course.setTotalRevenue(null);
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);
        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userCourseMapper.insert(any(ApUserCourse.class))).thenReturn(1);

        orderService.handlePaySuccess("ORDER123", "TRADE123");

        verify(discountMapper, never()).updateById(any(ApCourseDiscount.class));
        verify(courseMapper).updateById(Mockito.<ApCourse>argThat(c ->
                c.getStudyCount() == 1 && c.getSalesCount() == 1
                        && c.getTotalRevenue().compareTo(new BigDecimal("100.00")) == 0
        ));
    }

    @Test
    void testHandlePaySuccessCourseNotFound() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        when(courseMapper.selectById(100L)).thenReturn(null);

        orderService.handlePaySuccess("ORDER123", "TRADE123");

        verify(orderMapper).updateById(any(ApCourseOrder.class));
        verify(courseMapper, never()).updateById(any(ApCourse.class));
    }

    @Test
    void testHandlePaySuccessUserCourseAlreadyExists() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);

        ApCourse course = new ApCourse();
        course.setId(100L);
        course.setStudyCount(0);
        course.setSalesCount(0);
        course.setTotalRevenue(BigDecimal.ZERO);
        course.setIsDeleted(0);
        when(courseMapper.selectById(100L)).thenReturn(course);

        ApUserCourse existingUserCourse = new ApUserCourse();
        existingUserCourse.setId(1L);
        existingUserCourse.setIsActive((byte) 0);
        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUserCourse);

        orderService.handlePaySuccess("ORDER123", "TRADE123");

        verify(userCourseMapper).updateById(Mockito.<ApUserCourse>argThat(uc ->
                uc.getIsActive() == (byte) 1 && uc.getAccessType() == 1
        ));
        verify(userCourseMapper, never()).insert(any(ApUserCourse.class));
    }

    // ==================== getByOrderNo() tests ====================

    @Test
    void testGetByOrderNoSuccess() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockOrder);
        ApCourseOrder result = orderService.getByOrderNo("20250101120000ABC123");
        assertNotNull(result);
        assertEquals("20250101120000ABC123", result.getOrderNo());
    }

    @Test
    void testGetByOrderNoNotFound() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        ApCourseOrder result = orderService.getByOrderNo("NONEXISTENT");
        assertNull(result);
    }
}