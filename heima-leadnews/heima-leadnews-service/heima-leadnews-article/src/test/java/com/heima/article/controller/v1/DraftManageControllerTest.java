package com.heima.article.controller.v1;

import com.heima.article.service.DraftManageService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DraftManageController 单元测试")
class DraftManageControllerTest {

    @Mock
    private DraftManageService draftManageService;

    @InjectMocks
    private DraftManageController draftManageController;

    @Nested
    @DisplayName("list() - 查询草稿列表")
    class ListTests {

        @Test
        @DisplayName("正常查询草稿列表，返回成功结果")
        void shouldReturnDraftList() {
            ResponseResult expected = ResponseResult.okResult("draft_list");
            when(draftManageService.list(isNull(), eq(1), eq(10), isNull()))
                    .thenReturn(expected);

            ResponseResult result = draftManageController.list(1, 10, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(draftManageService).list(null, 1, 10, null);
        }

        @Test
        @DisplayName("按标题搜索草稿")
        void shouldSearchByTitle() {
            ResponseResult expected = ResponseResult.okResult("searched_drafts");
            when(draftManageService.list(isNull(), eq(1), eq(10), eq("测试")))
                    .thenReturn(expected);

            ResponseResult result = draftManageController.list(1, 10, "测试");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(draftManageService).list(null, 1, 10, "测试");
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_drafts");
            when(draftManageService.list(isNull(), eq(1), eq(10), isNull()))
                    .thenReturn(expected);

            ResponseResult result = draftManageController.list(1, 10, null);

            assertNotNull(result);
            verify(draftManageService).list(null, 1, 10, null);
        }
    }

    @Nested
    @DisplayName("delete() - 删除草稿")
    class DeleteTests {

        @Test
        @DisplayName("正常删除草稿，返回成功结果")
        void shouldDeleteDraftSuccessfully() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 100L);
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(draftManageService.deleteDraft(eq(100L))).thenReturn(expected);

            ResponseResult result = draftManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(draftManageService).deleteDraft(100L);
        }

        @Test
        @DisplayName("删除不存在的草稿，透传错误")
        void shouldPropagateErrorWhenDeleteNotFound() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(draftManageService.deleteDraft(eq(999L))).thenReturn(errorResult);

            ResponseResult result = draftManageController.delete(body);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }

    @Nested
    @DisplayName("count() - 获取草稿数量")
    class CountTests {

        @Test
        @DisplayName("正常获取草稿数量，返回成功结果")
        void shouldReturnDraftCount() {
            ResponseResult expected = ResponseResult.okResult("draft_count");
            when(draftManageService.list(isNull(), eq(1), eq(1), isNull()))
                    .thenReturn(expected);

            ResponseResult result = draftManageController.count();

            assertNotNull(result);
            assertEquals(expected, result);
            verify(draftManageService).list(null, 1, 1, null);
        }
    }
}