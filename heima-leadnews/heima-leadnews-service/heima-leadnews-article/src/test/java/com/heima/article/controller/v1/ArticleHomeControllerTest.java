package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleRecommendService;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleHomeControllerTest {

    @Mock
    private ApArticleService apArticleService;

    @Mock
    private ApArticleRecommendService apArticleRecommendService;

    @InjectMocks
    private ArticleHomeController controller;

    // ==================== load() tests ====================

    @Test
    void testLoad() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("__all__");
        dto.setSize(10);

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.load(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @Test
    void testLoadWithNullDto() {
        when(apArticleService.load(null, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.load(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLoadWithMaxBehotTime() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setMaxBehotTime(new Date());
        dto.setTag("java");

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.load(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @Test
    void testLoadServiceReturnsError() {
        ArticleHomeDto dto = new ArticleHomeDto();

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult result = controller.load(dto);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
    }

    // ==================== loadmore() tests ====================

    @Test
    void testLoadmore() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setMinBehotTime(new Date());

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.loadmore(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @Test
    void testLoadmoreWithNullDto() {
        when(apArticleService.load(null, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.loadmore(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLoadmoreWithChannel() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setTag("spring");
        dto.setSize(20);

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.loadmore(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @Test
    void testLoadmoreServiceReturnsError() {
        ArticleHomeDto dto = new ArticleHomeDto();

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.loadmore(dto);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== loadnew() tests ====================

    @Test
    void testLoadnew() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setMaxBehotTime(new Date());

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.loadnew(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    @Test
    void testLoadnewWithNullDto() {
        when(apArticleService.load(null, ArticleConstants.LOADTYPE_LOAD_NEW))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.loadnew(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLoadnewEmptyDto() {
        ArticleHomeDto dto = new ArticleHomeDto();

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.loadnew(dto);

        assertEquals(200, result.getCode());
        verify(apArticleService).load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    @Test
    void testLoadnewServiceReturnsError() {
        ArticleHomeDto dto = new ArticleHomeDto();

        when(apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN));

        ResponseResult result = controller.loadnew(dto);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    // ==================== recommend() tests ====================

    @Test
    void testRecommend() {
        ArticleRecommendDto dto = new ArticleRecommendDto();
        dto.setChannel("__all__");
        dto.setSize(10);
        dto.setPage(0);

        when(apArticleRecommendService.recommend(dto))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.recommend(dto);

        assertEquals(200, result.getCode());
        verify(apArticleRecommendService).recommend(dto);
    }

    @Test
    void testRecommendWithSeed() {
        ArticleRecommendDto dto = new ArticleRecommendDto();
        dto.setChannel("java");
        dto.setSize(20);
        dto.setPage(1);
        dto.setSeed(12345L);

        when(apArticleRecommendService.recommend(dto))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.recommend(dto);

        assertEquals(200, result.getCode());
        verify(apArticleRecommendService).recommend(dto);
    }

    @Test
    void testRecommendWithNullDto() {
        when(apArticleRecommendService.recommend(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.recommend(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testRecommendServiceReturnsError() {
        ArticleRecommendDto dto = new ArticleRecommendDto();

        when(apArticleRecommendService.recommend(dto))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult result = controller.recommend(dto);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
    }
}