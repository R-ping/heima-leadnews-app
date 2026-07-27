package com.heima.article.service.impl;

import com.heima.article.mapper.ChannelMapper;
import com.heima.model.article.pojos.ApChannel;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("频道服务测试")
class ChannelServiceImplTest {

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private ChannelServiceImpl channelService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(channelService, "baseMapper", channelMapper);
    }

    // ==================== findAll ====================

    @Test
    @DisplayName("查询所有频道 - 正常返回频道列表")
    void testFindAll_Success() {
        ApChannel channel1 = new ApChannel();
        channel1.setId(1);
        channel1.setName("科技");
        ApChannel channel2 = new ApChannel();
        channel2.setId(2);
        channel2.setName("财经");
        List<ApChannel> channels = Arrays.asList(channel1, channel2);
        when(channelMapper.selectList(any())).thenReturn(channels);

        ResponseResult result = channelService.findAll();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        List<ApChannel> data = (List<ApChannel>) result.getData();
        assertNotNull(data);
        assertEquals(2, data.size());
    }

    @Test
    @DisplayName("查询所有频道 - 返回空列表")
    void testFindAll_EmptyList() {
        when(channelMapper.selectList(any())).thenReturn(Collections.emptyList());

        ResponseResult result = channelService.findAll();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        List<ApChannel> data = (List<ApChannel>) result.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }
}