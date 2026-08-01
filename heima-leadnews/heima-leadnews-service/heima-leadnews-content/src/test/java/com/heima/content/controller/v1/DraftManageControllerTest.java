package com.heima.content.controller.v1;

import com.heima.content.service.DraftManageService;
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
class DraftManageControllerTest {

    @Mock
    private DraftManageService draftManageService;

    @InjectMocks
    private DraftManageController controller;

    // ==================== list() tests ====================

    @Test
    void testListDefaultParams() {
        when(draftManageService.list(null, 1, 10, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, null);

        assertEquals(200, result.getCode());
        verify(draftManageService).list(null, 1, 10, null);
    }

    @Test
    void testListWithTitle() {
        when(draftManageService.list(null, 1, 10, "test"))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, "test");

        assertEquals(200, result.getCode());
        verify(draftManageService).list(null, 1, 10, "test");
    }

    @Test
    void testListWithCustomPageSize() {
        when(draftManageService.list(null, 3, 15, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(3, 15, null);

        assertEquals(200, result.getCode());
        verify(draftManageService).list(null, 3, 15, null);
    }

    @Test
    void testListServiceReturnsError() {
        when(draftManageService.list(null, 1, 10, null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN));

        ResponseResult result = controller.list(1, 10, null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    // ==================== delete() tests ====================

    @Test
    void testDelete() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", 1L);

        when(draftManageService.deleteDraft(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.delete(body);

        assertEquals(200, result.getCode());
        verify(draftManageService).deleteDraft(1L);
    }

    @Test
    void testDeleteNotFound() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", 999L);

        when(draftManageService.deleteDraft(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.delete(body);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeleteNullId() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", null);

        when(draftManageService.deleteDraft(null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID));

        ResponseResult result = controller.delete(body);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== count() tests ====================

    @Test
    void testCount() {
        when(draftManageService.list(null, 1, 1, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.count();

        assertEquals(200, result.getCode());
        verify(draftManageService).list(null, 1, 1, null);
    }

    @Test
    void testCountServiceReturnsError() {
        when(draftManageService.list(null, 1, 1, null))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        ResponseResult result = controller.count();

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
    }
}