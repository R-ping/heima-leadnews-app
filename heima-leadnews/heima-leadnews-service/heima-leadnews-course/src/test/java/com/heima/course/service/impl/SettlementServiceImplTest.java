package com.heima.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.course.mapper.ApCourseOrderMapper;
import com.heima.course.mapper.ApCourseSettlementMapper;
import com.heima.model.article.pojos.ApCourseOrder;
import com.heima.model.article.pojos.ApCourseSettlement;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private ApCourseSettlementMapper settlementMapper;

    @Mock
    private ApCourseOrderMapper orderMapper;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    // ==================== getMonthlyList() tests ====================

    @Test
    void testGetMonthlyListSuccess() {
        ApCourseSettlement settlement = new ApCourseSettlement();
        settlement.setId(1L);
        settlement.setAuthorId(1);
        settlement.setCourseId(100L);
        settlement.setSettlementMonth("2026-07");
        settlement.setTotalSales(new BigDecimal("1000.00"));
        settlement.setPlatformShare(new BigDecimal("300.00"));
        settlement.setAuthorShare(new BigDecimal("700.00"));
        settlement.setOrderCount(10);
        settlement.setStatus(0);
        List<ApCourseSettlement> list = Collections.singletonList(settlement);
        when(settlementMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        ResponseResult result = settlementService.getMonthlyList(1L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
        assertEquals(new BigDecimal("1000.00"), data.get("totalSales"));
        assertEquals(new BigDecimal("300.00"), data.get("totalPlatformShare"));
        assertEquals(new BigDecimal("700.00"), data.get("totalAuthorShare"));
    }

    @Test
    void testGetMonthlyListEmpty() {
        when(settlementMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        ResponseResult result = settlementService.getMonthlyList(1L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
        assertEquals(BigDecimal.ZERO, data.get("totalSales"));
        assertEquals(BigDecimal.ZERO, data.get("totalPlatformShare"));
        assertEquals(BigDecimal.ZERO, data.get("totalAuthorShare"));
    }

    @Test
    void testGetMonthlyListWithNullValues() {
        ApCourseSettlement settlement = new ApCourseSettlement();
        settlement.setId(1L);
        settlement.setTotalSales(null);
        settlement.setPlatformShare(null);
        settlement.setAuthorShare(null);
        List<ApCourseSettlement> list = Collections.singletonList(settlement);
        when(settlementMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(list);

        ResponseResult result = settlementService.getMonthlyList(1L);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(BigDecimal.ZERO, data.get("totalSales"));
        assertEquals(BigDecimal.ZERO, data.get("totalPlatformShare"));
        assertEquals(BigDecimal.ZERO, data.get("totalAuthorShare"));
    }

    // ==================== getSettlementDetail() tests ====================

    @Test
    void testGetSettlementDetailSuccess() {
        ApCourseSettlement settlement = new ApCourseSettlement();
        settlement.setId(1L);
        when(settlementMapper.selectById(1L)).thenReturn(settlement);

        ResponseResult result = settlementService.getSettlementDetail(1L);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetSettlementDetailNotFound() {
        when(settlementMapper.selectById(999L)).thenReturn(null);
        ResponseResult result = settlementService.getSettlementDetail(999L);
        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== executeMonthlySettlement() tests ====================

    @Test
    void testExecuteMonthlySettlementNoOrders() {
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        settlementService.executeMonthlySettlement("2026-07");
        verify(settlementMapper, never()).insert(any(ApCourseSettlement.class));
    }

    @Test
    void testExecuteMonthlySettlementWithOrders() {
        ApCourseOrder order1 = new ApCourseOrder();
        order1.setUserId(1);
        order1.setCourseId(100L);
        order1.setPaidAmount(new BigDecimal("100.00"));
        order1.setStatus(ApCourseOrder.Status.PAID.getCode());

        ApCourseOrder order2 = new ApCourseOrder();
        order2.setUserId(1);
        order2.setCourseId(100L);
        order2.setPaidAmount(new BigDecimal("200.00"));
        order2.setStatus(ApCourseOrder.Status.PAID.getCode());

        ApCourseOrder order3 = new ApCourseOrder();
        order3.setUserId(2);
        order3.setCourseId(200L);
        order3.setPaidAmount(new BigDecimal("300.00"));
        order3.setStatus(ApCourseOrder.Status.PAID.getCode());

        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(order1, order2, order3));

        settlementService.executeMonthlySettlement("2026-07");

        verify(settlementMapper, times(2)).insert(any(ApCourseSettlement.class));
    }

    @Test
    void testExecuteMonthlySettlementSingleOrder() {
        ApCourseOrder order = new ApCourseOrder();
        order.setUserId(1);
        order.setCourseId(100L);
        order.setPaidAmount(new BigDecimal("150.00"));
        order.setStatus(ApCourseOrder.Status.PAID.getCode());

        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(order));

        settlementService.executeMonthlySettlement("2026-07");

        verify(settlementMapper, times(1)).insert(argMatch(settlement -> {
            ApCourseSettlement s = (ApCourseSettlement) settlement;
            return s.getAuthorId() == 1
                    && s.getCourseId().equals(100L)
                    && s.getSettlementMonth().equals("2026-07")
                    && s.getTotalSales().compareTo(new BigDecimal("150.00")) == 0
                    && s.getPlatformShare().compareTo(new BigDecimal("45.00")) == 0
                    && s.getAuthorShare().compareTo(new BigDecimal("105.00")) == 0
                    && s.getOrderCount() == 1
                    && s.getStatus() == 0;
        }));
    }

    private ApCourseSettlement argMatch(java.util.function.Predicate<ApCourseSettlement> predicate) {
        return argThat(o -> predicate.test((ApCourseSettlement) o));
    }
}