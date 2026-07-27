package com.heima.article.controller.v1;

import com.heima.article.service.ArticleStatisticsService;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleStatisticsController 单元测试")
class ArticleStatisticsControllerTest {

    @Mock
    private ArticleStatisticsService articleStatisticsService;

    @InjectMocks
    private ArticleStatisticsController articleStatisticsController;

    @Nested
    @DisplayName("getStatistics() - 获取文章统计")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常获取用户文章统计，返回成功结果")
        void shouldReturnUserStatistics() {
            ResponseResult expected = ResponseResult.okResult("user_statistics");
            when(articleStatisticsService.getUserStatistics(eq(1001L))).thenReturn(expected);

            ResponseResult result = articleStatisticsController.getStatistics(1001L);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(articleStatisticsService).getUserStatistics(1001L);
        }

        @Test
        @DisplayName("Service返回错误时，正确透传")
        void shouldPropagateErrorResult() {
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(articleStatisticsService.getUserStatistics(eq(999L))).thenReturn(errorResult);

            ResponseResult result = articleStatisticsController.getStatistics(999L);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
            assertEquals("数据不存在", result.getMessage());
        }

        @Test
        @DisplayName("userId为0时，正常委托给service")
        void shouldDelegateToServiceWhenUserIdIsZero() {
            ResponseResult expected = ResponseResult.okResult("zero_user_stats");
            when(articleStatisticsService.getUserStatistics(eq(0L))).thenReturn(expected);

            ResponseResult result = articleStatisticsController.getStatistics(0L);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleStatisticsService).getUserStatistics(0L);
        }
    }
}