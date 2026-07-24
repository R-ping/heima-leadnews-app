package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ContentDataServiceTest {

    @Autowired
    private ContentDataService contentDataService;

    private static final Long TEST_USER_ID = 1L;
    private static final String START_DATE = "2024-01-01";
    private static final String END_DATE = "2024-12-31";

    // ==================== Article Tests ====================

    @Test
    public void testGetArticleStatistics() {
        ResponseResult result = contentDataService.getArticleStatistics(TEST_USER_ID, START_DATE, END_DATE);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetArticleTrend() {
        ResponseResult result = contentDataService.getArticleTrend(TEST_USER_ID, START_DATE, END_DATE, 7);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetArticleDetail() {
        ResponseResult result = contentDataService.getArticleDetail(TEST_USER_ID, START_DATE, END_DATE, 1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== Column Tests ====================

    @Test
    public void testGetColumnStatistics() {
        ResponseResult result = contentDataService.getColumnStatistics(TEST_USER_ID, START_DATE, END_DATE);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetColumnTrend() {
        ResponseResult result = contentDataService.getColumnTrend(TEST_USER_ID, START_DATE, END_DATE, 7);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetColumnDetail() {
        ResponseResult result = contentDataService.getColumnDetail(TEST_USER_ID, START_DATE, END_DATE, 1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== Pin Tests ====================

    @Test
    public void testGetPinStatistics() {
        ResponseResult result = contentDataService.getPinStatistics(TEST_USER_ID, START_DATE, END_DATE);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetPinTrend() {
        ResponseResult result = contentDataService.getPinTrend(TEST_USER_ID, START_DATE, END_DATE, 7);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetPinDetail() {
        ResponseResult result = contentDataService.getPinDetail(TEST_USER_ID, START_DATE, END_DATE, 1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}