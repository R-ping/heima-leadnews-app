package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
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
@DisplayName("ApLikesBehaviorController 单元测试")
class ApLikesBehaviorControllerTest {

    @Mock
    private ApLikesBehaviorService apLikesBehaviorService;

    @InjectMocks
    private ApLikesBehaviorController controller;

    // ==================== like ====================

    @Nested
    @DisplayName("like 方法测试")
    class LikeTests {

        @Test
        @DisplayName("正常点赞")
        void shouldLikeSuccessfully() {
            LikesBehaviorDto dto = new LikesBehaviorDto();
            dto.setArticleId(1001L);
            dto.setOperation((short) 0);
            ResponseResult expected = ResponseResult.okResult();
            when(apLikesBehaviorService.like(dto)).thenReturn(expected);

            ResponseResult result = controller.like(dto);

            assertSame(expected, result);
            verify(apLikesBehaviorService).like(dto);
        }

        @Test
        @DisplayName("取消点赞")
        void shouldUnlikeSuccessfully() {
            LikesBehaviorDto dto = new LikesBehaviorDto();
            dto.setArticleId(1001L);
            dto.setOperation((short) 1);
            ResponseResult expected = ResponseResult.okResult();
            when(apLikesBehaviorService.like(dto)).thenReturn(expected);

            ResponseResult result = controller.like(dto);

            assertSame(expected, result);
            verify(apLikesBehaviorService).like(dto);
        }

        @Test
        @DisplayName("点赞 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            LikesBehaviorDto dto = new LikesBehaviorDto();
            ResponseResult expected = ResponseResult.errorResult(500, "点赞失败");
            when(apLikesBehaviorService.like(dto)).thenReturn(expected);

            ResponseResult result = controller.like(dto);

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("点赞 - dto为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数无效");
            when(apLikesBehaviorService.like(null)).thenReturn(expected);

            ResponseResult result = controller.like(null);

            assertNotNull(result);
            verify(apLikesBehaviorService).like(null);
        }
    }
}