package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("沸点服务测试")
class ApPinsServiceImplTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @InjectMocks
    private ApPinsServiceImpl apPinsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apPinsService, "baseMapper", apPinsMapper);
    }

    // ==================== findList ====================

    @Test
    @DisplayName("查询沸点列表 - 正常返回分页数据")
    void testFindList_Success() {
        ApPins pins = ApPins.builder().id(1L).content("测试沸点").status((byte) 1).build();
        List<ApPins> records = Collections.singletonList(pins);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(
                new Page<ApPins>(1, 10, 1) {{
                    setRecords(records);
                }}
        );

        ResponseResult result = apPinsService.findList(1, 10, (byte) 1);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询沸点列表 - status为null查询全部")
    void testFindList_NullStatus() {
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(
                new Page<ApPins>(1, 10, 0) {{
                    setRecords(Collections.emptyList());
                }}
        );

        ResponseResult result = apPinsService.findList(1, 10, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== deleteById ====================

    @Test
    @DisplayName("删除沸点 - id为null返回参数错误")
    void testDeleteById_NullId() {
        ResponseResult result = apPinsService.deleteById(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - 删除成功")
    void testDeleteById_Success() {
        when(apPinsMapper.deleteById(1L)).thenReturn(1);

        ResponseResult result = apPinsService.deleteById(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - 数据不存在")
    void testDeleteById_NotFound() {
        when(apPinsMapper.deleteById(999L)).thenReturn(0);

        ResponseResult result = apPinsService.deleteById(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updateStatus ====================

    @Test
    @DisplayName("更新沸点状态 - id为null返回参数错误")
    void testUpdateStatus_NullId() {
        ResponseResult result = apPinsService.updateStatus(null, (byte) 1, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新沸点状态 - status为null返回参数错误")
    void testUpdateStatus_NullStatus() {
        ResponseResult result = apPinsService.updateStatus(1L, null, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新沸点状态 - 沸点不存在")
    void testUpdateStatus_NotFound() {
        when(apPinsMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = apPinsService.updateStatus(999L, (byte) 1, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新沸点状态 - 更新成功")
    void testUpdateStatus_Success() {
        ApPins pins = ApPins.builder().id(1L).content("测试沸点").status((byte) 0).build();
        when(apPinsMapper.selectById(1L)).thenReturn(pins);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = apPinsService.updateStatus(1L, (byte) 1, "审核通过");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}