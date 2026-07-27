package com.heima.article.controller.v1;

import com.heima.article.service.ChannelService;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelController 单元测试")
class ChannelControllerTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelController channelController;

    @Nested
    @DisplayName("findAll() - 查询所有频道")
    class FindAllTests {

        @Test
        @DisplayName("正常查询所有频道，返回成功结果")
        void shouldReturnAllChannels() {
            ResponseResult expected = ResponseResult.okResult("channels_list");
            when(channelService.findAll()).thenReturn(expected);

            ResponseResult result = channelController.findAll();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(channelService).findAll();
        }

        @Test
        @DisplayName("Service返回空列表时，正常返回")
        void shouldReturnEmptyChannelList() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(channelService.findAll()).thenReturn(expected);

            ResponseResult result = channelController.findAll();

            assertNotNull(result);
            assertEquals(200, result.getCode());
        }

        @Test
        @DisplayName("Service返回错误时，正确透传")
        void shouldPropagateErrorResult() {
            ResponseResult errorResult = ResponseResult.errorResult(503, "服务器内部错误");
            when(channelService.findAll()).thenReturn(errorResult);

            ResponseResult result = channelController.findAll();

            assertNotNull(result);
            assertEquals(503, result.getCode());
            assertEquals("服务器内部错误", result.getMessage());
        }
    }
}