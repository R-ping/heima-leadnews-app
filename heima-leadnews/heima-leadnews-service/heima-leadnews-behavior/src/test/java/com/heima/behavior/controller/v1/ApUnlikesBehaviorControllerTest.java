package com.heima.behavior.controller.v1;

import com.heima.behavior.service.ApUnlikesBehaviorService;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
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
@DisplayName("ApUnlikesBehaviorController 单元测试")
class ApUnlikesBehaviorControllerTest {

    @Mock
    private ApUnlikesBehaviorService apUnlikesBehaviorService;

    @InjectMocks
    private ApUnlikesBehaviorController controller;

    // ==================== unLike ====================

    @Nested
    @DisplayName("unLike 方法测试")
    class UnLikeTests {

        @Test
        @DisplayName("正常记录不喜欢行为")
        void shouldRecordUnlikeSuccessfully() {
            UnLikesBehaviorDto dto = new UnLikesBehaviorDto();
            dto.setArticleId(1001L);
            ResponseResult expected = ResponseResult.okResult();
            when(apUnlikesBehaviorService.unLike(dto)).thenReturn(expected);

            ResponseResult result = controller.unLike(dto);

            assertSame(expected, result);
            verify(apUnlikesBehaviorService).unLike(dto);
        }

        @Test
        @DisplayName("不喜欢行为 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            UnLikesBehaviorDto dto = new UnLikesBehaviorDto();
            ResponseResult expected = ResponseResult.errorResult(500, "记录不喜欢行为失败");
            when(apUnlikesBehaviorService.unLike(dto)).thenReturn(expected);

            ResponseResult result = controller.unLike(dto);

            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("不喜欢行为 - dto为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数无效");
            when(apUnlikesBehaviorService.unLike(null)).thenReturn(expected);

            ResponseResult result = controller.unLike(null);

            assertNotNull(result);
            verify(apUnlikesBehaviorService).unLike(null);
        }
    }
}