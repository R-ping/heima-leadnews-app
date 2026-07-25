package com.heima.article.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCheckInMapper;
import com.heima.article.mapper.UserSignInSummaryMapper;
import com.heima.model.article.pojos.ApCheckIn;
import com.heima.model.article.pojos.UserSignInSummary;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class CheckInServiceTest {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private ApCheckInMapper checkInMapper;

    @Autowired
    private UserSignInSummaryMapper summaryMapper;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    public void setUp() {
        // 清理今日测试数据，避免重复签到冲突
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        LambdaQueryWrapper<ApCheckIn> query = new LambdaQueryWrapper<>();
        query.eq(ApCheckIn::getUserId, TEST_USER_ID);
        query.eq(ApCheckIn::getCheckInDate, today);
        checkInMapper.delete(query);

        // 重置签到汇总
        LambdaQueryWrapper<UserSignInSummary> summaryQuery = new LambdaQueryWrapper<>();
        summaryQuery.eq(UserSignInSummary::getUserId, TEST_USER_ID);
        UserSignInSummary summary = summaryMapper.selectOne(summaryQuery);
        if (summary != null) {
            summary.setCurrentConsecutiveDays(0);
            summary.setLastSignDate(null);
            summary.setTotalOre(0L);
            summary.setTotalSignedDays(0);
            summaryMapper.updateById(summary);
        }
    }

    // ==================== getCheckInStats ====================

    @Test
    @Order(2)
    public void testGetCheckInStats() {
        Map<String, Object> result = checkInService.getCheckInStats(TEST_USER_ID);
        assertNotNull(result);
        assertNotNull(result.get("consecutiveDays"));
        assertNotNull(result.get("totalDays"));
        assertNotNull(result.get("totalPoints"));
        assertNotNull(result.get("patchCardCount"));
        assertNotNull(result.get("todaySigned"));
    }

    // ==================== getCheckInRecords ====================

    @Test
    @Order(3)
    public void testGetCheckInRecords() {
        java.time.LocalDate today = java.time.LocalDate.now();
        Map<String, Object> result = checkInService.getCheckInRecords(TEST_USER_ID, today.getYear(), today.getMonthValue());
        assertNotNull(result);
        assertNotNull(result.get("days"));
        assertEquals(today.getYear(), result.get("year"));
        assertEquals(today.getMonthValue(), result.get("month"));
    }

    // ==================== getCheckInTasks ====================

    @Test
    @Order(4)
    public void testGetCheckInTasks() {
        Map<String, Object> result = checkInService.getCheckInTasks(TEST_USER_ID);
        assertNotNull(result);
        assertNotNull(result.get("list"));
    }

    // ==================== doCheckIn ====================

    @Test
    @Order(5)
    public void testDoCheckIn() {
        Map<String, Object> result = checkInService.doCheckIn(TEST_USER_ID);
        assertNotNull(result);
        assertTrue(result.containsKey("success"));
        if ((Boolean) result.get("success")) {
            assertNotNull(result.get("reward"));
        }
    }

    // ==================== doRetroactive ====================

    @Test
    @Order(7)
    public void testDoRetroactive_FutureDate() {
        // 补签未来日期应失败
        java.time.LocalDate future = java.time.LocalDate.now().plusDays(1);
        Map<String, Object> result = checkInService.doRetroactive(TEST_USER_ID, future.toString());
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("只能补签过去的日期", result.get("message"));
    }

    @Test
    @Order(8)
    public void testDoRetroactive_TodayDate() {
        // 补签今天应失败
        java.time.LocalDate today = java.time.LocalDate.now();
        Map<String, Object> result = checkInService.doRetroactive(TEST_USER_ID, today.toString());
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("只能补签过去的日期", result.get("message"));
    }

    @Test
    @Order(9)
    public void testDoRetroactive_PastDate() {
        // 补签过去日期（如果已签到则会提示已签到，如果没有补签卡也会提示）
        java.time.LocalDate past = java.time.LocalDate.now().minusDays(2);
        Map<String, Object> result = checkInService.doRetroactive(TEST_USER_ID, past.toString());
        assertNotNull(result);
        assertTrue(result.containsKey("success"));
    }
}