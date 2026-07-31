package com.heima.article.controller.v1;

import com.heima.article.service.ArticleManageService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleManageControllerTest {

    @Mock
    private ArticleManageService articleManageService;

    @InjectMocks
    private ArticleManageController controller;

    // ==================== list() tests ====================

    @Test
    void testListDefaultParams() {
        when(articleManageService.list(null, 1, 10, null, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, null, null);

        assertEquals(200, result.getCode());
        verify(articleManageService).list(null, 1, 10, null, null);
    }

    @Test
    void testListWithStatusAndTitle() {
        when(articleManageService.list(null, 1, 10, "0", "test"))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, "0", "test");

        assertEquals(200, result.getCode());
        verify(articleManageService).list(null, 1, 10, "0", "test");
    }

    @Test
    void testListWithCustomPageSize() {
        when(articleManageService.list(null, 2, 20, null, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(2, 20, null, null);

        assertEquals(200, result.getCode());
        verify(articleManageService).list(null, 2, 20, null, null);
    }

    @Test
    void testListServiceReturnsError() {
        when(articleManageService.list(null, 1, 10, null, null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN));

        ResponseResult result = controller.list(1, 10, null, null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    // ==================== statistics() tests ====================

    @Test
    void testStatistics() {
        when(articleManageService.statistics(null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.statistics();

        assertEquals(200, result.getCode());
        verify(articleManageService).statistics(null);
    }

    @Test
    void testStatisticsServiceReturnsError() {
        when(articleManageService.statistics(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.statistics();

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== delete() tests ====================

    @Test
    void testDelete() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", 1L);

        when(articleManageService.deleteArticle(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.delete(body);

        assertEquals(200, result.getCode());
        verify(articleManageService).deleteArticle(1L);
    }

    @Test
    void testDeleteNotFound() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", 999L);

        when(articleManageService.deleteArticle(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.delete(body);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeleteNullId() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", null);

        when(articleManageService.deleteArticle(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.delete(body);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }
}