package com.heima.content.controller.v1;

import com.heima.content.service.ApPinsService;
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
class PinsControllerTest {

    @Mock
    private ApPinsService apPinsService;

    @InjectMocks
    private PinsController controller;

    // ==================== findList() tests ====================

    @Test
    void testFindList() {
        when(apPinsService.findList(1, 10, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.findList(1, 10, null);

        assertEquals(200, result.getCode());
        verify(apPinsService).findList(1, 10, null);
    }

    @Test
    void testFindListWithStatus() {
        when(apPinsService.findList(1, 10, (byte) 1))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.findList(1, 10, (byte) 1);

        assertEquals(200, result.getCode());
    }

    @Test
    void testFindListDefaultParams() {
        when(apPinsService.findList(1, 10, null))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.findList(1, 10, null);

        assertEquals(200, result.getCode());
    }

    // ==================== deleteById() tests ====================

    @Test
    void testDeleteById() {
        when(apPinsService.deleteById(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.deleteById(1L);

        assertEquals(200, result.getCode());
        verify(apPinsService).deleteById(1L);
    }

    @Test
    void testDeleteByIdNotFound() {
        when(apPinsService.deleteById(999L))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST));

        ResponseResult result = controller.deleteById(999L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== updateStatus() tests ====================

    @Test
    void testUpdateStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        params.put("status", (byte) 1);
        params.put("reason", "审核通过");

        when(apPinsService.updateStatus(eq(1L), eq((byte) 1), eq("审核通过")))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.updateStatus(params);

        assertEquals(200, result.getCode());
    }

    @Test
    void testUpdateStatusWithoutReason() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        params.put("status", (byte) 2);

        when(apPinsService.updateStatus(eq(1L), eq((byte) 2), isNull()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.updateStatus(params);

        assertEquals(200, result.getCode());
    }

    @Test
    void testUpdateStatusReject() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 2L);
        params.put("status", (byte) 3);
        params.put("reason", "违规内容");

        when(apPinsService.updateStatus(eq(2L), eq((byte) 3), eq("违规内容")))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.updateStatus(params);

        assertEquals(200, result.getCode());
    }
}