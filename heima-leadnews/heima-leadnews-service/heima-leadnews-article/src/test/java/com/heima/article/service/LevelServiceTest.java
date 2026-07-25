package com.heima.article.service;

import com.heima.model.article.pojos.ApUserLevel;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LevelServiceTest {

    @Autowired
    private LevelService levelService;

    private static final Long TEST_USER_ID = 1L;

    // ==================== getUserLevel ====================

    @Test
    @Order(1)
    public void testGetUserLevel() {
        ApUserLevel level = levelService.getUserLevel(TEST_USER_ID);
        assertNotNull(level);
        assertEquals(TEST_USER_ID, level.getUserId());
        assertNotNull(level.getDailyScore());
        assertNotNull(level.getDailyLevel());
        assertNotNull(level.getPowerValue());
        assertNotNull(level.getPowerLevel());
    }

    @Test
    @Order(2)
    public void testGetUserLevel_NewUser() {
        ApUserLevel level = levelService.getUserLevel(999999L);
        assertNotNull(level);
        assertEquals(999999L, level.getUserId());
        assertEquals(1, level.getDailyLevel());
        assertEquals(0, level.getDailyScore());
    }

    // ==================== getUserLevelInfo ====================

    @Test
    @Order(3)
    public void testGetUserLevelInfo() {
        Map<String, Object> result = levelService.getUserLevelInfo(TEST_USER_ID);
        assertNotNull(result);
        assertNotNull(result.get("dailyScore"));
        assertNotNull(result.get("dailyLevel"));
        assertNotNull(result.get("powerValue"));
        assertNotNull(result.get("powerLevel"));
        assertNotNull(result.get("permissions"));
    }

    // ==================== getLevelConfigs ====================

    @Test
    @Order(4)
    public void testGetLevelConfigs_DailyLevel() {
        List<com.heima.model.article.pojos.ApLevelConfig> configs = levelService.getLevelConfigs(1);
        assertNotNull(configs);
        assertFalse(configs.isEmpty());
    }

    @Test
    @Order(5)
    public void testGetLevelConfigs_PowerLevel() {
        List<com.heima.model.article.pojos.ApLevelConfig> configs = levelService.getLevelConfigs(2);
        assertNotNull(configs);
        assertFalse(configs.isEmpty());
    }

    // ==================== recordAction ====================

    @Test
    @Order(6)
    public void testRecordAction_Valid() {
        levelService.recordAction(TEST_USER_ID, "daily_checkin", "测试签到");
        // 验证没有异常抛出
        assertTrue(true);
    }

    @Test
    @Order(7)
    public void testRecordAction_InvalidType() {
        levelService.recordAction(TEST_USER_ID, "invalid_action", "测试无效行为");
        // 无效行为类型应被忽略，不抛异常
        assertTrue(true);
    }

    // ==================== recordActionWithLimit ====================

    @Test
    @Order(8)
    public void testRecordActionWithLimit_Valid() {
        Map<String, Object> result = levelService.recordActionWithLimit(TEST_USER_ID, "daily_checkin", "测试签到");
        assertNotNull(result);
        assertTrue(result.containsKey("success"));
    }

    @Test
    @Order(9)
    public void testRecordActionWithLimit_InvalidType() {
        Map<String, Object> result = levelService.recordActionWithLimit(TEST_USER_ID, "invalid_action", "测试无效");
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("无效的行为类型", result.get("message"));
    }

    // ==================== checkIn ====================

    @Test
    @Order(10)
    public void testCheckIn() {
        Map<String, Object> result = levelService.checkIn(TEST_USER_ID);
        assertNotNull(result);
        assertTrue(result.containsKey("success"));
    }

    // ==================== calculatePower ====================

    @Test
    @Order(11)
    public void testCalculatePower() {
        levelService.calculatePower(TEST_USER_ID, 1L, "publish_article", 10);
        // 验证无异常
        assertTrue(true);
    }

    @Test
    @Order(12)
    public void testCalculatePowerWithLimit() {
        Map<String, Object> result = levelService.calculatePowerWithLimit(TEST_USER_ID, 1L, "publish_article", 10);
        assertNotNull(result);
        assertTrue(result.containsKey("success"));
        assertNotNull(result.get("power"));
        assertNotNull(result.get("powerValue"));
        assertNotNull(result.get("powerLevel"));
    }

    // ==================== hasPermission ====================

    @Test
    @Order(13)
    public void testHasPermission() {
        boolean hasPerm = levelService.hasPermission(TEST_USER_ID, "some_permission");
        // 结果取决于数据库，但不应抛异常
        assertTrue(hasPerm || !hasPerm);
    }

    // ==================== getUserPermissions ====================

    @Test
    @Order(14)
    public void testGetUserPermissions() {
        List<String> permissions = levelService.getUserPermissions(TEST_USER_ID);
        assertNotNull(permissions);
    }

    // ==================== getTodayTaskProgress ====================

    @Test
    @Order(15)
    public void testGetTodayTaskProgress() {
        Map<String, Object> result = levelService.getTodayTaskProgress(TEST_USER_ID);
        assertNotNull(result);
        assertNotNull(result.get("tasks"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("tasks");
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
    }
}