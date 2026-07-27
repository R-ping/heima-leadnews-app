package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleRecommendService;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleHomeController 单元测试")
class ArticleHomeControllerTest {

    @Mock
    private ApArticleService apArticleService;

    @Mock
    private ApArticleRecommendService apArticleRecommendService;

    @InjectMocks
    private ArticleHomeController articleHomeController;

    @Nested
    @DisplayName("load() - 加载首页")
    class LoadTests {

        @Test
        @DisplayName("正常加载首页，返回成功结果")
        void shouldReturnSuccessWhenLoadHome() {
            ArticleHomeDto dto = new ArticleHomeDto();
            dto.setSize(10);
            dto.setTag("__all__");

            ResponseResult expected = ResponseResult.okResult("home_data");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.load(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
        }

        @Test
        @DisplayName("DTO为空时，正常委托给service处理")
        void shouldDelegateToServiceWhenDtoIsEmpty() {
            ArticleHomeDto dto = new ArticleHomeDto();

            ResponseResult expected = ResponseResult.okResult("empty_result");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.load(dto);

            assertNotNull(result);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Service返回错误结果时，正确透传")
        void shouldPropagateErrorResult() {
            ArticleHomeDto dto = new ArticleHomeDto();
            dto.setTag("tech");

            ResponseResult errorResult = ResponseResult.errorResult(500, "服务异常");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(errorResult);

            ResponseResult result = articleHomeController.load(dto);

            assertNotNull(result);
            assertEquals(500, result.getCode());
            assertEquals("服务异常", result.getMessage());
        }
    }

    @Nested
    @DisplayName("loadmore() - 加载更多")
    class LoadMoreTests {

        @Test
        @DisplayName("正常加载更多，使用LOADTYPE_LOAD_MORE")
        void shouldUseLoadMoreType() {
            ArticleHomeDto dto = new ArticleHomeDto();
            dto.setSize(5);
            dto.setMinBehotTime(new Date());

            ResponseResult expected = ResponseResult.okResult("more_data");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.loadmore(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
        }

        @Test
        @DisplayName("带minBehotTime的加载更多请求")
        void shouldPassMinBehotTime() {
            ArticleHomeDto dto = new ArticleHomeDto();
            Date minTime = new Date();
            dto.setMinBehotTime(minTime);
            dto.setSize(10);

            ResponseResult expected = ResponseResult.okResult("data");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.loadmore(dto);

            assertNotNull(result);
            verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
        }
    }

    @Nested
    @DisplayName("loadnew() - 加载最新")
    class LoadNewTests {

        @Test
        @DisplayName("正常加载最新，使用LOADTYPE_LOAD_NEW")
        void shouldUseLoadNewType() {
            ArticleHomeDto dto = new ArticleHomeDto();
            dto.setSize(10);
            dto.setTag("__all__");

            ResponseResult expected = ResponseResult.okResult("new_data");
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_NEW)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.loadnew(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
        }

        @Test
        @DisplayName("load()和loadnew()使用不同的loadType")
        void shouldUseDifferentLoadTypes() {
            ArticleHomeDto dto = new ArticleHomeDto();

            ResponseResult loadMoreResult = ResponseResult.okResult("more");
            ResponseResult loadNewResult = ResponseResult.okResult("new");

            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_MORE)))
                    .thenReturn(loadMoreResult);
            when(apArticleService.load(any(ArticleHomeDto.class), eq(ArticleConstants.LOADTYPE_LOAD_NEW)))
                    .thenReturn(loadNewResult);

            ResponseResult result1 = articleHomeController.load(dto);
            ResponseResult result2 = articleHomeController.loadnew(dto);

            assertEquals(loadMoreResult, result1);
            assertEquals(loadNewResult, result2);
        }
    }

    @Nested
    @DisplayName("recommend() - 推荐文章")
    class RecommendTests {

        @Test
        @DisplayName("正常推荐文章，返回成功结果")
        void shouldReturnRecommendResult() {
            ArticleRecommendDto dto = new ArticleRecommendDto();
            dto.setChannel("__all__");
            dto.setSize(5);

            ResponseResult expected = ResponseResult.okResult("recommend_data");
            when(apArticleRecommendService.recommend(any(ArticleRecommendDto.class)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.recommend(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apArticleRecommendService).recommend(dto);
        }

        @Test
        @DisplayName("推荐请求带seed参数")
        void shouldPassSeedToRecommendService() {
            ArticleRecommendDto dto = new ArticleRecommendDto();
            dto.setChannel("tech");
            dto.setSize(10);
            dto.setSeed(12345L);
            dto.setPage(0);

            ResponseResult expected = ResponseResult.okResult("paged_data");
            when(apArticleRecommendService.recommend(any(ArticleRecommendDto.class)))
                    .thenReturn(expected);

            ResponseResult result = articleHomeController.recommend(dto);

            assertNotNull(result);
            verify(apArticleRecommendService).recommend(dto);
        }

        @Test
        @DisplayName("推荐服务返回空列表时，正确透传")
        void shouldPropagateEmptyRecommendResult() {
            ArticleRecommendDto dto = new ArticleRecommendDto();
            dto.setChannel("99999");

            ResponseResult emptyResult = ResponseResult.okResult(null);
            when(apArticleRecommendService.recommend(any(ArticleRecommendDto.class)))
                    .thenReturn(emptyResult);

            ResponseResult result = articleHomeController.recommend(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
        }
    }
}