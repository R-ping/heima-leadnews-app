package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApAssociateWordsService;
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
@DisplayName("ApAssociateWordsController 单元测试")
class ApAssociateWordsControllerTest {

    @Mock
    private ApAssociateWordsService apAssociateWordsService;

    @InjectMocks
    private ApAssociateWordsController controller;

    // ==================== search ====================

    @Nested
    @DisplayName("search 方法测试")
    class SearchTests {

        @Test
        @DisplayName("正常搜索联想词")
        void shouldSearchAssociateWordsSuccessfully() {
            UserSearchDto dto = new UserSearchDto();
            dto.setSearchWords("java");
            ResponseResult expected = ResponseResult.okResult("associate-list");
            when(apAssociateWordsService.search(dto)).thenReturn(expected);

            ResponseResult result = controller.search(dto);

            assertSame(expected, result);
            verify(apAssociateWordsService).search(dto);
        }

        @Test
        @DisplayName("搜索联想词 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            UserSearchDto dto = new UserSearchDto();
            ResponseResult expected = ResponseResult.errorResult(500, "搜索联想词失败");
            when(apAssociateWordsService.search(dto)).thenReturn(expected);

            ResponseResult result = controller.search(dto);

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("搜索联想词 - 空关键词")
        void shouldHandleEmptySearchWords() {
            UserSearchDto dto = new UserSearchDto();
            dto.setSearchWords("");
            ResponseResult expected = ResponseResult.okResult(null);
            when(apAssociateWordsService.search(dto)).thenReturn(expected);

            ResponseResult result = controller.search(dto);

            assertSame(expected, result);
            verify(apAssociateWordsService).search(dto);
        }

        @Test
        @DisplayName("搜索联想词 - 参数为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数无效");
            when(apAssociateWordsService.search(null)).thenReturn(expected);

            ResponseResult result = controller.search(null);

            assertNotNull(result);
            verify(apAssociateWordsService).search(null);
        }
    }
}