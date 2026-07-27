package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ApReadBehaviorService;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApReadBehaviorController 单元测试")
class ApReadBehaviorControllerTest {

    @Mock
    private ApReadBehaviorService apReadBehaviorService;

    @InjectMocks
    private ApReadBehaviorController controller;

    // ==================== readBehavior ====================

    @Nested
    @DisplayName("readBehavior 方法测试")
    class ReadBehaviorTests {

        @Test
        @DisplayName("正常记录阅读行为")
        void shouldRecordReadBehaviorSuccessfully() {
            ReadBehaviorDto dto = new ReadBehaviorDto();
            dto.setArticleId(1001L);
            ResponseResult expected = ResponseResult.okResult();
            when(apReadBehaviorService.readBehavior(dto)).thenReturn(expected);

            ResponseResult result = controller.readBehavior(dto);

            assertSame(expected, result);
            verify(apReadBehaviorService).readBehavior(dto);
        }

        @Test
        @DisplayName("阅读行为 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ReadBehaviorDto dto = new ReadBehaviorDto();
            ResponseResult expected = ResponseResult.errorResult(500, "记录阅读行为失败");
            when(apReadBehaviorService.readBehavior(dto)).thenReturn(expected);

            ResponseResult result = controller.readBehavior(dto);

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("阅读行为 - dto为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数无效");
            when(apReadBehaviorService.readBehavior(null)).thenReturn(expected);

            ResponseResult result = controller.readBehavior(null);

            assertNotNull(result);
            verify(apReadBehaviorService).readBehavior(null);
        }
    }
}