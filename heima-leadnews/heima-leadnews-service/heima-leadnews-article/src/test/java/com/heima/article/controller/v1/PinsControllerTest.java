package com.heima.article.controller.v1;

import com.heima.article.service.ApPinsService;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PinsController 单元测试")
class PinsControllerTest {

    @Mock
    private ApPinsService apPinsService;

    @InjectMocks
    private PinsController pinsController;

    @Nested
    @DisplayName("findList() - 查询动态列表")
    class FindListTests {

        @Test
        @DisplayName("正常查询动态列表，返回成功结果")
        void shouldReturnPinsList() {
            ResponseResult expected = ResponseResult.okResult("pins_list");
            when(apPinsService.findList(eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = pinsController.findList(1, 10, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apPinsService).findList(1, 10, null);
        }

        @Test
        @DisplayName("按状态筛选动态列表")
        void shouldFilterByStatus() {
            ResponseResult expected = ResponseResult.okResult("filtered_pins");
            when(apPinsService.findList(eq(1), eq(10), eq((byte) 9))).thenReturn(expected);

            ResponseResult result = pinsController.findList(1, 10, (byte) 9);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apPinsService).findList(1, 10, (byte) 9);
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_pins");
            when(apPinsService.findList(eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = pinsController.findList(1, 10, null);

            assertNotNull(result);
            verify(apPinsService).findList(1, 10, null);
        }
    }

    @Nested
    @DisplayName("deleteById() - 删除动态")
    class DeleteByIdTests {

        @Test
        @DisplayName("正常删除动态，返回成功结果")
        void shouldDeletePinsSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(apPinsService.deleteById(eq(100L))).thenReturn(expected);

            ResponseResult result = pinsController.deleteById(100L);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apPinsService).deleteById(100L);
        }

        @Test
        @DisplayName("删除不存在的动态，透传错误")
        void shouldPropagateErrorWhenDeleteNotFound() {
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apPinsService.deleteById(eq(999L))).thenReturn(errorResult);

            ResponseResult result = pinsController.deleteById(999L);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
            verify(apPinsService).deleteById(999L);
        }
    }

    @Nested
    @DisplayName("updateStatus() - 更新动态状态")
    class UpdateStatusTests {

        @Test
        @DisplayName("正常更新动态状态，返回成功结果")
        void shouldUpdateStatusSuccessfully() {
            Map<String, Object> params = new HashMap<>();
            params.put("id", "100");
            params.put("status", "9");
            params.put("reason", "审核通过");
            ResponseResult expected = ResponseResult.okResult("updated");
            when(apPinsService.updateStatus(eq(100L), eq((byte) 9), eq("审核通过")))
                    .thenReturn(expected);

            ResponseResult result = pinsController.updateStatus(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apPinsService).updateStatus(100L, (byte) 9, "审核通过");
        }

        @Test
        @DisplayName("更新状态不提供reason，传递null")
        void shouldPassNullReasonWhenNotProvided() {
            Map<String, Object> params = new HashMap<>();
            params.put("id", "100");
            params.put("status", "2");
            ResponseResult expected = ResponseResult.okResult("updated");
            when(apPinsService.updateStatus(eq(100L), eq((byte) 2), isNull()))
                    .thenReturn(expected);

            ResponseResult result = pinsController.updateStatus(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apPinsService).updateStatus(100L, (byte) 2, null);
        }
    }
}