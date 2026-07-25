package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleStatisticsServiceTest {

    @Autowired
    private ArticleStatisticsService articleStatisticsService;

    private static final Long TEST_USER_ID = 1L;

    @Test
    @Order(1)
    public void testGetUserStatistics() {
        ResponseResult result = articleStatisticsService.getUserStatistics(TEST_USER_ID);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @Order(2)
    public void testGetUserStatistics_VerifyFields() {
        ResponseResult result = articleStatisticsService.getUserStatistics(TEST_USER_ID);
        assertEquals(200, result.getCode());

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("followCount"));
        assertNotNull(data.get("followerCount"));
        assertNotNull(data.get("likeCount"));
        assertNotNull(data.get("collectCount"));
        assertNotNull(data.get("readCount"));
        assertNotNull(data.get("levelInfo"));
        assertNotNull(data.get("diamondCount"));
    }
}