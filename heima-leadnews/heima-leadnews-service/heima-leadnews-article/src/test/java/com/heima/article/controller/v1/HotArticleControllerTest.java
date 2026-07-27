package com.heima.article.controller.v1;

import com.heima.article.service.HotService;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.article.vos.HotAuthorVo;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HotArticleController 单元测试")
class HotArticleControllerTest {

    @Mock
    private HotService hotService;

    @InjectMocks
    private HotArticleController hotArticleController;

    @Nested
    @DisplayName("getHotArticles() - 获取热门文章")
    class GetHotArticlesTests {

        @Test
        @DisplayName("正常获取热门文章，返回成功结果")
        void shouldReturnHotArticles() {
            List<HotArticleVo> mockData = Collections.singletonList(new HotArticleVo());
            when(hotService.getHotArticles(eq("tech"), eq(10))).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotArticles("tech", 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotArticles("tech", 10);
        }

        @Test
        @DisplayName("category和limit为null时，正常委托给service")
        void shouldDelegateToServiceWhenParamsAreNull() {
            List<HotArticleVo> mockData = Collections.emptyList();
            when(hotService.getHotArticles(isNull(), isNull())).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotArticles(null, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotArticles(null, null);
        }

        @Test
        @DisplayName("带分页限制获取热门文章")
        void shouldPassLimitParam() {
            List<HotArticleVo> mockData = Collections.singletonList(new HotArticleVo());
            when(hotService.getHotArticles(eq("sports"), eq(5))).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotArticles("sports", 5);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotArticles("sports", 5);
        }
    }

    @Nested
    @DisplayName("getCollectedArticles() - 获取收藏文章")
    class GetCollectedArticlesTests {

        @Test
        @DisplayName("正常获取收藏文章，返回成功结果")
        void shouldReturnCollectedArticles() {
            List<HotArticleVo> mockData = Collections.singletonList(new HotArticleVo());
            when(hotService.getCollectedArticles(eq(10))).thenReturn(mockData);

            ResponseResult result = hotArticleController.getCollectedArticles(10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getCollectedArticles(10);
        }

        @Test
        @DisplayName("limit为null时，正常委托给service")
        void shouldDelegateToServiceWhenLimitIsNull() {
            List<HotArticleVo> mockData = Collections.emptyList();
            when(hotService.getCollectedArticles(isNull())).thenReturn(mockData);

            ResponseResult result = hotArticleController.getCollectedArticles(null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getCollectedArticles(null);
        }
    }

    @Nested
    @DisplayName("getHotAuthors() - 获取热门作者")
    class GetHotAuthorsTests {

        @Test
        @DisplayName("正常获取热门作者，返回成功结果")
        void shouldReturnHotAuthors() {
            List<HotAuthorVo> mockData = Collections.singletonList(new HotAuthorVo());
            when(hotService.getHotAuthors(eq("week"), eq(10))).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotAuthors("week", 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotAuthors("week", 10);
        }

        @Test
        @DisplayName("period和limit为null时，正常委托给service")
        void shouldDelegateToServiceWhenPeriodAndLimitAreNull() {
            List<HotAuthorVo> mockData = Collections.emptyList();
            when(hotService.getHotAuthors(isNull(), isNull())).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotAuthors(null, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotAuthors(null, null);
        }

        @Test
        @DisplayName("按月度获取热门作者")
        void shouldPassMonthPeriod() {
            List<HotAuthorVo> mockData = Collections.singletonList(new HotAuthorVo());
            when(hotService.getHotAuthors(eq("month"), eq(20))).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotAuthors("month", 20);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotAuthors("month", 20);
        }
    }

    @Nested
    @DisplayName("getHotMeta() - 获取热门元数据")
    class GetHotMetaTests {

        @Test
        @DisplayName("正常获取热门元数据，返回成功结果")
        void shouldReturnHotMeta() {
            Map<String, Object> mockData = new HashMap<>();
            mockData.put("key", "value");
            when(hotService.getHotMeta(eq("articles"), eq("tech"), eq("day")))
                    .thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotMeta("articles", "tech", "day");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotMeta("articles", "tech", "day");
        }

        @Test
        @DisplayName("所有参数为null时，正常委托给service")
        void shouldDelegateToServiceWhenAllParamsAreNull() {
            Map<String, Object> mockData = new HashMap<>();
            when(hotService.getHotMeta(isNull(), isNull(), isNull())).thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotMeta(null, null, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotMeta(null, null, null);
        }

        @Test
        @DisplayName("按作者tab获取元数据")
        void shouldGetAuthorsMeta() {
            Map<String, Object> mockData = new HashMap<>();
            when(hotService.getHotMeta(eq("authors"), isNull(), eq("week")))
                    .thenReturn(mockData);

            ResponseResult result = hotArticleController.getHotMeta("authors", null, "week");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(hotService).getHotMeta("authors", null, "week");
        }
    }
}