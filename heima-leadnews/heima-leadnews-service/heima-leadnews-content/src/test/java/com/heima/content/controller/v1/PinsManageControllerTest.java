package com.heima.content.controller.v1;

import com.heima.content.service.PinsService;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
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
class PinsManageControllerTest {

    @Mock
    private PinsService pinsService;

    @InjectMocks
    private PinsManageController controller;

    // ==================== list() tests ====================

    @Test
    void testList() {
        when(pinsService.list(isNull(), eq(1), eq(10), eq("published")))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, "published");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListAll() {
        when(pinsService.list(isNull(), eq(1), eq(10), isNull()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListDefaultParams() {
        when(pinsService.list(isNull(), eq(1), eq(10), isNull()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list(1, 10, null);

        assertEquals(200, result.getCode());
    }

    // ==================== statistics() tests ====================

    @Test
    void testStatistics() {
        when(pinsService.statistics(isNull()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.statistics();

        assertEquals(200, result.getCode());
    }

    // ==================== create() tests ====================

    @Test
    void testCreate() {
        ApPins pins = new ApPins();
        pins.setContent("test content");
        when(pinsService.createPins(any(ApPins.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.create(pins);

        assertEquals(200, result.getCode());
    }

    // ==================== delete() tests ====================

    @Test
    void testDelete() {
        Map<String, Long> body = new HashMap<>();
        body.put("id", 1L);
        when(pinsService.deletePins(1L))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.delete(body);

        assertEquals(200, result.getCode());
    }

    @Test
    void testDeleteWithNullId() {
        Map<String, Long> body = new HashMap<>();
        when(pinsService.deletePins(isNull()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.delete(body);

        assertEquals(200, result.getCode());
    }
}