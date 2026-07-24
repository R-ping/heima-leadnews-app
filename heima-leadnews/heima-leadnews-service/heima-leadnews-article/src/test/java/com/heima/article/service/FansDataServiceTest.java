package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FansDataServiceTest {

    @Autowired
    private FansDataService fansDataService;

    private static final String START_DATE = "2024-01-01";
    private static final String END_DATE = "2024-12-31";

    @Test
    public void testGetFansStatistics() {
        ResponseResult result = fansDataService.getFansStatistics(START_DATE, END_DATE);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetFansTrend() {
        ResponseResult result = fansDataService.getFansTrend(START_DATE, END_DATE, 7);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testGetFansList() {
        ResponseResult result = fansDataService.getFansList(1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    public void testFollowFans() {
        ResponseResult result = fansDataService.followFans(1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}