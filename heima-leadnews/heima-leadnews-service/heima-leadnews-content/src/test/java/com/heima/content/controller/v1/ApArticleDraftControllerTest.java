package com.heima.content.controller.v1;

import com.heima.content.service.ApArticleDraftService;
import com.heima.model.article.pojos.ApArticleDraft;
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
class ApArticleDraftControllerTest {

    @Mock
    private ApArticleDraftService apArticleDraftService;

    @InjectMocks
    private ApArticleDraftController controller;

    // ==================== createDraft() tests ====================

    @Test
    void testCreateDraft() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("test draft");

        when(apArticleDraftService.createDraft(draft))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.createDraft(draft);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).createDraft(draft);
    }

    @Test
    void testCreateDraftWithNullBody() {
        when(apArticleDraftService.createDraft(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.createDraft(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateDraftEmptyTitle() {
        ApArticleDraft draft = new ApArticleDraft();

        when(apArticleDraftService.createDraft(draft))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.createDraft(draft);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateDraftExceedsMax() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("overflow draft");

        when(apArticleDraftService.createDraft(draft))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.createDraft(draft);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== updateDraft() tests ====================

    @Test
    void testUpdateDraft() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("updated title");

        when(apArticleDraftService.updateDraft(draft))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.updateDraft(draft);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).updateDraft(draft);
    }

    @Test
    void testUpdateDraftNullId() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("no id");

        when(apArticleDraftService.updateDraft(draft))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftNotFound() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(999L);

        when(apArticleDraftService.updateDraft(draft))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== publishFromDraft() tests ====================

    @Test
    void testPublishFromDraft() {
        Map<String, Long> body = new HashMap<>();
        body.put("draftId", 1L);

        when(apArticleDraftService.publishFromDraft(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.publishFromDraft(body);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).publishFromDraft(1L);
    }

    @Test
    void testPublishFromDraftNotFound() {
        Map<String, Long> body = new HashMap<>();
        body.put("draftId", 999L);

        when(apArticleDraftService.publishFromDraft(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.publishFromDraft(body);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testPublishFromDraftNullId() {
        Map<String, Long> body = new HashMap<>();
        body.put("draftId", null);

        when(apArticleDraftService.publishFromDraft(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.publishFromDraft(body);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== getDraftById() tests ====================

    @Test
    void testGetDraftById() {
        when(apArticleDraftService.getDraftById(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.getDraftById(1L);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).getDraftById(1L);
    }

    @Test
    void testGetDraftByIdNotFound() {
        when(apArticleDraftService.getDraftById(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.getDraftById(999L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testGetDraftByIdNull() {
        when(apArticleDraftService.getDraftById(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.getDraftById(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== listDrafts() tests ====================

    @Test
    void testListDraftsDefaultParams() {
        when(apArticleDraftService.listDrafts(null, 1, 10))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.listDrafts(null, 1, 10);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).listDrafts(null, 1, 10);
    }

    @Test
    void testListDraftsWithAuthorId() {
        when(apArticleDraftService.listDrafts(1L, 1, 10))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.listDrafts(1L, 1, 10);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).listDrafts(1L, 1, 10);
    }

    @Test
    void testListDraftsCustomPageSize() {
        when(apArticleDraftService.listDrafts(null, 3, 50))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.listDrafts(null, 3, 50);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).listDrafts(null, 3, 50);
    }

    @Test
    void testListDraftsServiceError() {
        when(apArticleDraftService.listDrafts(null, 1, 10))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult result = controller.listDrafts(null, 1, 10);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
    }

    // ==================== deleteDraft() tests ====================

    @Test
    void testDeleteDraft() {
        when(apArticleDraftService.deleteDraft(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.deleteDraft(1L);

        assertEquals(200, result.getCode());
        verify(apArticleDraftService).deleteDraft(1L);
    }

    @Test
    void testDeleteDraftNotFound() {
        when(apArticleDraftService.deleteDraft(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.deleteDraft(999L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeleteDraftNull() {
        when(apArticleDraftService.deleteDraft(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.deleteDraft(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }
}