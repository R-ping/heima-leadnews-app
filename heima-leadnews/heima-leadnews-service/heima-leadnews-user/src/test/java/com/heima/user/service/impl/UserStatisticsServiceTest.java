package com.heima.user.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.UserStatisticsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserStatisticsServiceTest {

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Test
    @Order(1)
    public void testGetUserStatistics() {
        ResponseResult result = userStatisticsService.getUserStatistics();
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}