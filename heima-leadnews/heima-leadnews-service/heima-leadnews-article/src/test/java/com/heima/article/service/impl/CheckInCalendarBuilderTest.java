package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCheckInMapper;
import com.heima.article.mapper.SignInConfigMapper;
import com.heima.model.article.pojos.ApCheckIn;
import com.heima.model.article.pojos.SignInConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("签到日历构建器测试")
class CheckInCalendarBuilderTest {

    @Mock
    private ApCheckInMapper checkInMapper;

    @Mock
    private SignInConfigMapper signInConfigMapper;

    @InjectMocks
    private CheckInCalendarBuilder calendarBuilder;

    // ==================== buildCalendarData ====================

    @Test
    @DisplayName("构建日历数据 - 当月无签到记录")
    void testBuildCalendarData_NoCheckIns() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        when(checkInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(signInConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Map<String, Object> result = calendarBuilder.buildCalendarData(1L, year, month, 0);

        assertNotNull(result);
        assertEquals(year, result.get("year"));
        assertEquals(month, result.get("month"));
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        assertFalse(days.isEmpty());
        assertEquals(year, result.get("year"));
    }

    @Test
    @DisplayName("构建日历数据 - 有签到记录")
    void testBuildCalendarData_WithCheckIns() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        LocalDate checkInDate = today.withDayOfMonth(Math.min(5, today.lengthOfMonth()));

        ApCheckIn checkIn = new ApCheckIn();
        checkIn.setId(1L);
        checkIn.setUserId(1L);
        checkIn.setCheckInDate(Date.valueOf(checkInDate));
        checkIn.setRewardPoints(10);

        when(checkInMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(checkIn));
        when(signInConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result = calendarBuilder.buildCalendarData(1L, year, month, 1);

        assertNotNull(result);
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        // 验证签到日期状态
        boolean foundSigned = days.stream().anyMatch(d -> "SIGNED".equals(d.get("status")));
        assertTrue(foundSigned);
    }

    @Test
    @DisplayName("构建日历数据 - 包含签到配置")
    void testBuildCalendarData_WithConfig() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        SignInConfig config = new SignInConfig();
        config.setId(1L);
        config.setDayOfMonth(1);
        config.setIsActive(1);
        config.setExtraLabel("奖励翻倍");

        when(checkInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(signInConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(config));

        Map<String, Object> result = calendarBuilder.buildCalendarData(1L, year, month, 0);

        assertNotNull(result);
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        assertFalse(days.isEmpty());
    }

    @Test
    @DisplayName("构建日历数据 - 过去月份无补签卡")
    void testBuildCalendarData_PastMonth() {
        int year = 2024;
        int month = 1;

        when(checkInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(signInConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Map<String, Object> result = calendarBuilder.buildCalendarData(1L, year, month, 0);

        assertNotNull(result);
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        assertEquals(31, days.size());
        // 所有日期状态应为 MISSED
        boolean allMissed = days.stream().allMatch(d -> "MISSED".equals(d.get("status")));
        assertTrue(allMissed);
    }

    @Test
    @DisplayName("构建日历数据 - 未来月份所有日期为NORMAL")
    void testBuildCalendarData_FutureMonth() {
        int year = 2030;
        int month = 1;

        when(checkInMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(signInConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Map<String, Object> result = calendarBuilder.buildCalendarData(1L, year, month, 0);

        assertNotNull(result);
        List<Map<String, Object>> days = (List<Map<String, Object>>) result.get("days");
        assertNotNull(days);
        // 未来月份不与当月匹配，所有日期状态应为 NORMAL
        boolean allNormal = days.stream().allMatch(d -> "NORMAL".equals(d.get("status")));
        assertTrue(allNormal);
    }
}