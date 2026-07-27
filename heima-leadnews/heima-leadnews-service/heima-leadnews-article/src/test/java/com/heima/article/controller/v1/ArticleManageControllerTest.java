package com.heima.article.controller.v1;

import com.heima.article.service.ArticleManageService;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleManageController 单元测试")
class ArticleManageControllerTest {

    @Mock
    private ArticleManageService articleManageService;

    @InjectMocks
    private ArticleManageController articleManageController;

    @Nested
    @DisplayName("list() - 文章列表查询")
    class ListTests {

        @Test
        @DisplayName("默认分页参数查询，返回成功结果")
        void shouldReturnListWithDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("list_data");
            when(articleManageService.list(isNull(), eq(1), eq(10), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.list(1, 10, null, null);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).list(null, 1, 10, null, null);
        }

        @Test
        @DisplayName("带状态筛选查询")
        void shouldFilterByStatus() {
            ResponseResult expected = ResponseResult.okResult("filtered_list");
            when(articleManageService.list(isNull(), eq(1), eq(10), eq("PUBLISHED"), isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.list(1, 10, "PUBLISHED", null);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).list(null, 1, 10, "PUBLISHED", null);
        }

        @Test
        @DisplayName("带标题搜索查询")
        void shouldSearchByTitle() {
            ResponseResult expected = ResponseResult.okResult("search_result");
            when(articleManageService.list(isNull(), eq(1), eq(10), isNull(), eq("测试标题")))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.list(1, 10, null, "测试标题");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).list(null, 1, 10, null, "测试标题");
        }

        @Test
        @DisplayName("同时带状态和标题筛选")
        void shouldFilterByStatusAndTitle() {
            ResponseResult expected = ResponseResult.okResult("combined_result");
            when(articleManageService.list(isNull(), eq(2), eq(20), eq("DRAFT"), eq("草稿")))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.list(2, 20, "DRAFT", "草稿");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).list(null, 2, 20, "DRAFT", "草稿");
        }

        @Test
        @DisplayName("自定义分页参数")
        void shouldUseCustomPagination() {
            ResponseResult expected = ResponseResult.okResult("page3");
            when(articleManageService.list(isNull(), eq(3), eq(50), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.list(3, 50, null, null);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).list(null, 3, 50, null, null);
        }

        @Test
        @DisplayName("authorId始终传null")
        void shouldAlwaysPassNullAuthorId() {
            ResponseResult expected = ResponseResult.okResult("data");
            when(articleManageService.list(isNull(), eq(1), eq(10), isNull(), isNull()))
                    .thenReturn(expected);

            articleManageController.list(1, 10, null, null);

            verify(articleManageService).list(null, 1, 10, null, null);
        }

        @Test
        @DisplayName("service返回错误时正确透传")
        void shouldPropagateServiceError() {
            ResponseResult errorResult = ResponseResult.errorResult(500, "查询失败");
            when(articleManageService.list(isNull(), eq(1), eq(10), isNull(), isNull()))
                    .thenReturn(errorResult);

            ResponseResult result = articleManageController.list(1, 10, null, null);

            assertNotNull(result);
            assertEquals(500, result.getCode());
            assertEquals("查询失败", result.getMessage());
        }
    }

    @Nested
    @DisplayName("statistics() - 文章统计")
    class StatisticsTests {

        @Test
        @DisplayName("正常获取统计数据")
        void shouldReturnStatistics() {
            ResponseResult expected = ResponseResult.okResult("stats_data");
            when(articleManageService.statistics(isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.statistics();

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).statistics(null);
        }

        @Test
        @DisplayName("authorId始终传null")
        void shouldAlwaysPassNullAuthorIdToStatistics() {
            ResponseResult expected = ResponseResult.okResult("stats");
            when(articleManageService.statistics(isNull()))
                    .thenReturn(expected);

            articleManageController.statistics();

            verify(articleManageService).statistics(null);
        }

        @Test
        @DisplayName("统计服务返回空数据")
        void shouldHandleNullStatisticsData() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(articleManageService.statistics(isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.statistics();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("delete() - 删除文章")
    class DeleteTests {

        @Test
        @DisplayName("正常删除文章")
        void shouldDeleteArticleSuccessfully() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 1001L);

            ResponseResult expected = ResponseResult.okResult("deleted");
            when(articleManageService.deleteArticle(eq(1001L)))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).deleteArticle(1001L);
        }

        @Test
        @DisplayName("删除不存在的文章，传递null id")
        void shouldHandleNullId() {
            Map<String, Long> body = new HashMap<>();
            // body中没有id key，get("id")返回null
            body.put("other", 1L);

            ResponseResult expected = ResponseResult.okResult("deleted");
            when(articleManageService.deleteArticle(isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).deleteArticle(null);
        }

        @Test
        @DisplayName("空body删除请求")
        void shouldHandleEmptyBody() {
            Map<String, Long> body = new HashMap<>();

            ResponseResult expected = ResponseResult.okResult("deleted");
            when(articleManageService.deleteArticle(isNull()))
                    .thenReturn(expected);

            ResponseResult result = articleManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(articleManageService).deleteArticle(null);
        }

        @Test
        @DisplayName("删除服务返回错误")
        void shouldPropagateDeleteError() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 9999L);

            ResponseResult errorResult = ResponseResult.errorResult(404, "文章不存在");
            when(articleManageService.deleteArticle(eq(9999L)))
                    .thenReturn(errorResult);

            ResponseResult result = articleManageController.delete(body);

            assertNotNull(result);
            assertEquals(404, result.getCode());
            assertEquals("文章不存在", result.getMessage());
        }
    }
}