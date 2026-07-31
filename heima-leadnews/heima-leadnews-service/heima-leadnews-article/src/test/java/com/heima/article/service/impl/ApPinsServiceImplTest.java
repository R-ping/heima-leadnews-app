package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApPinsServiceImplTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @InjectMocks
    private ApPinsServiceImpl apPinsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apPinsService, "baseMapper", apPinsMapper);
    }

    // ==================== findList() tests ====================

    @Test
    void testFindListAll() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = apPinsService.findList(1, 10, null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testFindListByStatus() {
        ApPins pin = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = apPinsService.findList(1, 10, (byte) 1);

        assertEquals(200, result.getCode());
    }

    @Test
    void testFindListEmpty() {
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = apPinsService.findList(1, 10, null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testFindListWithStatusNull() {
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = apPinsService.findList(1, 10, null);

        assertEquals(200, result.getCode());
    }

    // ==================== deleteById() tests ====================

    @Test
    void testDeleteByIdSuccess() {
        when(apPinsMapper.deleteById(eq(1L))).thenReturn(1);

        ResponseResult result = apPinsService.deleteById(1L);

        assertEquals(200, result.getCode());
    }

    @Test
    void testDeleteByIdNullId() {
        ResponseResult result = apPinsService.deleteById(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testDeleteByIdNotFound() {
        when(apPinsMapper.deleteById(eq(999L))).thenReturn(0);

        ResponseResult result = apPinsService.deleteById(999L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== updateStatus() tests ====================

    @Test
    void testUpdateStatusSuccess() {
        ApPins pin = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = apPinsService.updateStatus(1L, ApPins.Status.PUBLISHED.getCode(), "审核通过");

        assertEquals(200, result.getCode());
        assertEquals(ApPins.Status.PUBLISHED.getCode(), pin.getStatus().byteValue());
        assertEquals("审核通过", pin.getReason());
    }

    @Test
    void testUpdateStatusReject() {
        ApPins pin = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = apPinsService.updateStatus(1L, ApPins.Status.FAIL.getCode(), "内容违规");

        assertEquals(200, result.getCode());
        assertEquals(ApPins.Status.FAIL.getCode(), pin.getStatus().byteValue());
        assertEquals("内容违规", pin.getReason());
    }

    @Test
    void testUpdateStatusNullId() {
        ResponseResult result = apPinsService.updateStatus(null, (byte) 1, "reason");

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUpdateStatusNullStatus() {
        ResponseResult result = apPinsService.updateStatus(1L, null, "reason");

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUpdateStatusPinsNotFound() {
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = apPinsService.updateStatus(1L, (byte) 1, "reason");

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testUpdateStatusUpdateFailed() {
        ApPins pin = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(0);

        ResponseResult result = apPinsService.updateStatus(1L, (byte) 1, "reason");

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
    }

    @Test
    void testUpdateStatusWithoutReason() {
        ApPins pin = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = apPinsService.updateStatus(1L, ApPins.Status.PUBLISHED.getCode(), null);

        assertEquals(200, result.getCode());
        assertNull(pin.getReason());
    }

    // ==================== Helper ====================

    private ApPins buildPin(Long id, ApPins.Status status) {
        ApPins pin = new ApPins();
        pin.setId(id);
        pin.setAuthorId(1L);
        pin.setAuthorName("author" + id);
        pin.setContent("content " + id);
        pin.setLikes(0);
        pin.setComment(0);
        pin.setShare(0);
        pin.setStatus(status.getCode());
        pin.setIsDeleted(false);
        pin.setCreatedTime(new Date());
        pin.setPublishTime(new Date());
        return pin;
    }
}