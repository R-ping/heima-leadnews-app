package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleDraftService;
import com.heima.model.article.pojos.ApArticleDraft;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApArticleDraftController 单元测试")
class ApArticleDraftControllerTest {

    @Mock
    private ApArticleDraftService apArticleDraftService;

    @InjectMocks
    private ApArticleDraftController apArticleDraftController;

    @Nested
    @DisplayName("createDraft() - 创建草稿")
    class CreateDraftTests {

        @Test
        @DisplayName("正常创建草稿，返回成功结果")
        void shouldCreateDraftSuccessfully() {
            ApArticleDraft draft = new ApArticleDraft();
            draft.setTitle("测试草稿");
            draft.setContent("草稿内容");
            ResponseResult expected = ResponseResult.okResult("draft_created");
            when(apArticleDraftService.createDraft(any(ApArticleDraft.class))).thenReturn(expected);

            ResponseResult result = apArticleDraftController.createDraft(draft);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).createDraft(draft);
        }

        @Test
        @DisplayName("创建草稿失败时，透传错误")
        void shouldPropagateErrorWhenCreateFails() {
            ApArticleDraft draft = new ApArticleDraft();
            ResponseResult errorResult = ResponseResult.errorResult(501, "创建失败");
            when(apArticleDraftService.createDraft(any(ApArticleDraft.class))).thenReturn(errorResult);

            ResponseResult result = apArticleDraftController.createDraft(draft);

            assertNotNull(result);
            assertEquals(501, result.getCode());
        }
    }

    @Nested
    @DisplayName("updateDraft() - 更新草稿")
    class UpdateDraftTests {

        @Test
        @DisplayName("正常更新草稿，返回成功结果")
        void shouldUpdateDraftSuccessfully() {
            ApArticleDraft draft = new ApArticleDraft();
            draft.setId(100L);
            draft.setTitle("更新后的草稿");
            draft.setContent("更新后的内容");
            ResponseResult expected = ResponseResult.okResult("draft_updated");
            when(apArticleDraftService.updateDraft(any(ApArticleDraft.class))).thenReturn(expected);

            ResponseResult result = apArticleDraftController.updateDraft(draft);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).updateDraft(draft);
        }

        @Test
        @DisplayName("更新不存在的草稿，透传错误")
        void shouldPropagateErrorWhenUpdateNotFound() {
            ApArticleDraft draft = new ApArticleDraft();
            draft.setId(999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apArticleDraftService.updateDraft(any(ApArticleDraft.class))).thenReturn(errorResult);

            ResponseResult result = apArticleDraftController.updateDraft(draft);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }

    @Nested
    @DisplayName("publishFromDraft() - 发布草稿")
    class PublishFromDraftTests {

        @Test
        @DisplayName("正常发布草稿，返回成功结果")
        void shouldPublishFromDraftSuccessfully() {
            Map<String, Long> body = new HashMap<>();
            body.put("draftId", 100L);
            ResponseResult expected = ResponseResult.okResult("published");
            when(apArticleDraftService.publishFromDraft(eq(100L))).thenReturn(expected);

            ResponseResult result = apArticleDraftController.publishFromDraft(body);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).publishFromDraft(100L);
        }

        @Test
        @DisplayName("发布不存在的草稿，透传错误")
        void shouldPropagateErrorWhenPublishNotFound() {
            Map<String, Long> body = new HashMap<>();
            body.put("draftId", 999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apArticleDraftService.publishFromDraft(eq(999L))).thenReturn(errorResult);

            ResponseResult result = apArticleDraftController.publishFromDraft(body);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }

    @Nested
    @DisplayName("getDraftById() - 获取草稿详情")
    class GetDraftByIdTests {

        @Test
        @DisplayName("正常获取草稿详情，返回成功结果")
        void shouldReturnDraftById() {
            ResponseResult expected = ResponseResult.okResult("draft_detail");
            when(apArticleDraftService.getDraftById(eq(100L))).thenReturn(expected);

            ResponseResult result = apArticleDraftController.getDraftById(100L);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).getDraftById(100L);
        }

        @Test
        @DisplayName("草稿不存在时，透传错误")
        void shouldPropagateErrorWhenDraftNotFound() {
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apArticleDraftService.getDraftById(eq(999L))).thenReturn(errorResult);

            ResponseResult result = apArticleDraftController.getDraftById(999L);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }

    @Nested
    @DisplayName("listDrafts() - 查询草稿列表")
    class ListDraftsTests {

        @Test
        @DisplayName("正常查询草稿列表，返回成功结果")
        void shouldReturnDraftList() {
            ResponseResult expected = ResponseResult.okResult("draft_list");
            when(apArticleDraftService.listDrafts(isNull(), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = apArticleDraftController.listDrafts(null, 1, 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).listDrafts(null, 1, 10);
        }

        @Test
        @DisplayName("按作者ID筛选草稿列表")
        void shouldFilterByAuthorId() {
            ResponseResult expected = ResponseResult.okResult("author_drafts");
            when(apArticleDraftService.listDrafts(eq(1001L), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = apArticleDraftController.listDrafts(1001L, 1, 10);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apArticleDraftService).listDrafts(1001L, 1, 10);
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_drafts");
            when(apArticleDraftService.listDrafts(isNull(), eq(1), eq(10)))
                    .thenReturn(expected);

            ResponseResult result = apArticleDraftController.listDrafts(null, 1, 10);

            assertNotNull(result);
            verify(apArticleDraftService).listDrafts(null, 1, 10);
        }
    }

    @Nested
    @DisplayName("deleteDraft() - 删除草稿")
    class DeleteDraftTests {

        @Test
        @DisplayName("正常删除草稿，返回成功结果")
        void shouldDeleteDraftSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(apArticleDraftService.deleteDraft(eq(100L))).thenReturn(expected);

            ResponseResult result = apArticleDraftController.deleteDraft(100L);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apArticleDraftService).deleteDraft(100L);
        }

        @Test
        @DisplayName("删除不存在的草稿，透传错误")
        void shouldPropagateErrorWhenDeleteNotFound() {
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apArticleDraftService.deleteDraft(eq(999L))).thenReturn(errorResult);

            ResponseResult result = apArticleDraftController.deleteDraft(999L);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }
}