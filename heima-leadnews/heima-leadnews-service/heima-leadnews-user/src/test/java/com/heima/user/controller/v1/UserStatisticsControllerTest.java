package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.UserStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatisticsController 单元测试")
class UserStatisticsControllerTest {

    @Mock
    private UserStatisticsService userStatisticsService;

    @InjectMocks
    private UserStatisticsController userStatisticsController;

    // ==================== getStatistics ====================

    @Nested
    @DisplayName("getStatistics 方法测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常获取用户统计信息")
        void shouldGetStatisticsSuccessfully() {
            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("articleCount", 100);
            statsMap.put("fansCount", 500);
            ResponseResult<Map<String, Object>> expected = ResponseResult.okResult(statsMap);
            when(userStatisticsService.getUserStatistics()).thenReturn(expected);

            ResponseResult<Map<String, Object>> result = userStatisticsController.getStatistics();

            assertSame(expected, result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(userStatisticsService).getUserStatistics();
        }

        @Test
        @DisplayName("获取统计信息 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ResponseResult<Map<String, Object>> expected = ResponseResult.errorResult(500, "获取统计信息失败");
            when(userStatisticsService.getUserStatistics()).thenReturn(expected);

            ResponseResult<Map<String, Object>> result = userStatisticsController.getStatistics();

            assertSame(expected, result);
            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("获取统计信息 - 返回空数据")
        void shouldReturnEmptyData() {
            ResponseResult<Map<String, Object>> expected = ResponseResult.okResult(null);
            when(userStatisticsService.getUserStatistics()).thenReturn(expected);

            ResponseResult<Map<String, Object>> result = userStatisticsController.getStatistics();

            assertSame(expected, result);
            assertNull(result.getData());
        }
    }
}