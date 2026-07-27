package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.search.service.ApUserSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApUserSearchController 单元测试")
class ApUserSearchControllerTest {

    @Mock
    private ApUserSearchService apUserSearchService;

    @InjectMocks
    private ApUserSearchController controller;

    // ==================== findUserSearch ====================

    @Nested
    @DisplayName("findUserSearch 方法测试")
    class FindUserSearchTests {

        @Test
        @DisplayName("正常获取用户搜索历史")
        void shouldFindUserSearchSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("history-list");
            when(apUserSearchService.findUserSearch()).thenReturn(expected);

            ResponseResult result = controller.findUserSearch();

            assertSame(expected, result);
            verify(apUserSearchService).findUserSearch();
        }

        @Test
        @DisplayName("获取搜索历史 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "获取搜索历史失败");
            when(apUserSearchService.findUserSearch()).thenReturn(expected);

            ResponseResult result = controller.findUserSearch();

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("获取搜索历史 - 返回空列表")
        void shouldReturnEmptyList() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(apUserSearchService.findUserSearch()).thenReturn(expected);

            ResponseResult result = controller.findUserSearch();

            assertSame(expected, result);
        }
    }

    // ==================== delUserSearch ====================

    @Nested
    @DisplayName("delUserSearch 方法测试")
    class DelUserSearchTests {

        @Test
        @DisplayName("正常删除搜索历史")
        void shouldDeleteUserSearchSuccessfully() {
            HistorySearchDto dto = new HistorySearchDto();
            ResponseResult expected = ResponseResult.okResult();
            when(apUserSearchService.delUserSearch(dto)).thenReturn(expected);

            ResponseResult result = controller.delUserSearch(dto);

            assertSame(expected, result);
            verify(apUserSearchService).delUserSearch(dto);
        }

        @Test
        @DisplayName("删除搜索历史 - 服务返回错误")
        void shouldReturnErrorWhenDeleteFails() {
            HistorySearchDto dto = new HistorySearchDto();
            ResponseResult expected = ResponseResult.errorResult(500, "删除失败");
            when(apUserSearchService.delUserSearch(dto)).thenReturn(expected);

            ResponseResult result = controller.delUserSearch(dto);

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("删除搜索历史 - 参数为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数无效");
            when(apUserSearchService.delUserSearch(null)).thenReturn(expected);

            ResponseResult result = controller.delUserSearch(null);

            assertNotNull(result);
            verify(apUserSearchService).delUserSearch(null);
        }
    }
}