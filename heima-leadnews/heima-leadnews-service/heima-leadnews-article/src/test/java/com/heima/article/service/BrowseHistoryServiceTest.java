package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrowseHistoryServiceTest {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    private static final Long TEST_USER_ID = 1L;
    private static final int PAGE = 1;
    private static final int SIZE = 10;

    // ==================== getHistoryList ====================

    @Test
    @Order(1)
    public void testGetHistoryList_Basic() {
        ResponseResult result = browseHistoryService.getHistoryList(TEST_USER_ID, PAGE, SIZE, null);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @Order(2)
    public void testGetHistoryList_WithKeyword() {
        ResponseResult result = browseHistoryService.getHistoryList(TEST_USER_ID, PAGE, SIZE, "测试");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    public void testGetHistoryList_EmptyKeyword() {
        ResponseResult result = browseHistoryService.getHistoryList(TEST_USER_ID, PAGE, SIZE, "");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(4)
    public void testGetHistoryList_WhitespaceKeyword() {
        ResponseResult result = browseHistoryService.getHistoryList(TEST_USER_ID, PAGE, SIZE, "   ");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== reportBrowse ====================

    @Test
    @Order(5)
    public void testReportBrowse_New() {
        ResponseResult result = browseHistoryService.reportBrowse(TEST_USER_ID, 1, 999999L);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(6)
    public void testReportBrowse_Duplicate() {
        // 第二次上报同一记录，应更新浏览时间
        ResponseResult result = browseHistoryService.reportBrowse(TEST_USER_ID, 1, 999999L);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(7)
    public void testReportBrowse_NullUserId() {
        ResponseResult result = browseHistoryService.reportBrowse(null, 1, 1L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(8)
    public void testReportBrowse_NullTargetType() {
        ResponseResult result = browseHistoryService.reportBrowse(TEST_USER_ID, null, 1L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(9)
    public void testReportBrowse_NullTargetId() {
        ResponseResult result = browseHistoryService.reportBrowse(TEST_USER_ID, 1, null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(10)
    public void testReportBrowse_DifferentTypes() {
        // 沸点类型
        ResponseResult result1 = browseHistoryService.reportBrowse(TEST_USER_ID, 2, 999998L);
        assertNotNull(result1);
        assertEquals(200, result1.getCode());

        // 课程类型
        ResponseResult result2 = browseHistoryService.reportBrowse(TEST_USER_ID, 3, 999997L);
        assertNotNull(result2);
        assertEquals(200, result2.getCode());

        // 专栏类型
        ResponseResult result3 = browseHistoryService.reportBrowse(TEST_USER_ID, 4, 999996L);
        assertNotNull(result3);
        assertEquals(200, result3.getCode());
    }

    // ==================== clearHistory ====================

    @Test
    @Order(11)
    public void testClearHistory() {
        // 先上报一条记录
        browseHistoryService.reportBrowse(TEST_USER_ID, 1, 999995L);

        // 清空记录（软删除）
        browseHistoryService.clearHistory(TEST_USER_ID);

        // 清空后查询应返回空列表
        ResponseResult result = browseHistoryService.getHistoryList(TEST_USER_ID, PAGE, SIZE, null);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}