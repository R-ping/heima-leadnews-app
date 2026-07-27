package com.heima.article.controller.v1;

import com.heima.article.service.ContentDataService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentDataController 单元测试")
class ContentDataControllerTest {

    @Mock
    private ContentDataService contentDataService;

    @InjectMocks
    private ContentDataController contentDataController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1001);
        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (threadLocalMock != null) {
            threadLocalMock.close();
        }
    }

    @Nested
    @DisplayName("articleStatistics() - 文章统计")
    class ArticleStatisticsTests {

        @Test
        @DisplayName("正常获取文章统计，返回成功结果")
        void shouldReturnArticleStatistics() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("article_stats");
            when(contentDataService.getArticleStatistics(eq(1001L), eq("2026-07-01"), eq("2026-07-27")))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.articleStatistics("2026-07-01", "2026-07-27");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getArticleStatistics(1001L, "2026-07-01", "2026-07-27");
        }

        @Test
        @DisplayName("未登录用户，userId为0")
        void shouldUseZeroUserIdWhenNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
            ResponseResult expected = ResponseResult.okResult("guest_stats");
            when(contentDataService.getArticleStatistics(eq(0L), eq("2026-07-01"), eq("2026-07-27")))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.articleStatistics("2026-07-01", "2026-07-27");

            assertNotNull(result);
            verify(contentDataService).getArticleStatistics(0L, "2026-07-01", "2026-07-27");
        }
    }

    @Nested
    @DisplayName("articleTrend() - 文章趋势")
    class ArticleTrendTests {

        @Test
        @DisplayName("正常获取文章趋势，返回成功结果")
        void shouldReturnArticleTrend() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("article_trend");
            when(contentDataService.getArticleTrend(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(7)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.articleTrend("2026-07-01", "2026-07-27", 7);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getArticleTrend(1001L, "2026-07-01", "2026-07-27", 7);
        }
    }

    @Nested
    @DisplayName("articleDetail() - 文章明细")
    class ArticleDetailTests {

        @Test
        @DisplayName("正常获取文章明细，返回成功结果")
        void shouldReturnArticleDetail() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("article_detail");
            when(contentDataService.getArticleDetail(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.articleDetail("2026-07-01", "2026-07-27", 1, 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getArticleDetail(1001L, "2026-07-01", "2026-07-27", 1, 10);
        }
    }

    @Nested
    @DisplayName("columnStatistics() - 专栏统计")
    class ColumnStatisticsTests {

        @Test
        @DisplayName("正常获取专栏统计，返回成功结果")
        void shouldReturnColumnStatistics() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("column_stats");
            when(contentDataService.getColumnStatistics(eq(1001L), eq("2026-07-01"), eq("2026-07-27")))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.columnStatistics("2026-07-01", "2026-07-27");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getColumnStatistics(1001L, "2026-07-01", "2026-07-27");
        }
    }

    @Nested
    @DisplayName("columnTrend() - 专栏趋势")
    class ColumnTrendTests {

        @Test
        @DisplayName("正常获取专栏趋势，返回成功结果")
        void shouldReturnColumnTrend() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("column_trend");
            when(contentDataService.getColumnTrend(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(7)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.columnTrend("2026-07-01", "2026-07-27", 7);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getColumnTrend(1001L, "2026-07-01", "2026-07-27", 7);
        }
    }

    @Nested
    @DisplayName("columnDetail() - 专栏明细")
    class ColumnDetailTests {

        @Test
        @DisplayName("正常获取专栏明细，返回成功结果")
        void shouldReturnColumnDetail() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("column_detail");
            when(contentDataService.getColumnDetail(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.columnDetail("2026-07-01", "2026-07-27", 1, 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getColumnDetail(1001L, "2026-07-01", "2026-07-27", 1, 10);
        }
    }

    @Nested
    @DisplayName("pinStatistics() - 动态统计")
    class PinStatisticsTests {

        @Test
        @DisplayName("正常获取动态统计，返回成功结果")
        void shouldReturnPinStatistics() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("pin_stats");
            when(contentDataService.getPinStatistics(eq(1001L), eq("2026-07-01"), eq("2026-07-27")))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.pinStatistics("2026-07-01", "2026-07-27");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getPinStatistics(1001L, "2026-07-01", "2026-07-27");
        }
    }

    @Nested
    @DisplayName("pinTrend() - 动态趋势")
    class PinTrendTests {

        @Test
        @DisplayName("正常获取动态趋势，返回成功结果")
        void shouldReturnPinTrend() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("pin_trend");
            when(contentDataService.getPinTrend(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(7)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.pinTrend("2026-07-01", "2026-07-27", 7);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getPinTrend(1001L, "2026-07-01", "2026-07-27", 7);
        }
    }

    @Nested
    @DisplayName("pinDetail() - 动态明细")
    class PinDetailTests {

        @Test
        @DisplayName("正常获取动态明细，返回成功结果")
        void shouldReturnPinDetail() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("pin_detail");
            when(contentDataService.getPinDetail(eq(1001L), eq("2026-07-01"), eq("2026-07-27"), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = contentDataController.pinDetail("2026-07-01", "2026-07-27", 1, 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(contentDataService).getPinDetail(1001L, "2026-07-01", "2026-07-27", 1, 10);
        }
    }
}