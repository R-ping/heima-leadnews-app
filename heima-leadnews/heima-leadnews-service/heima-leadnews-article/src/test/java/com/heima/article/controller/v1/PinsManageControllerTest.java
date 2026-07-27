package com.heima.article.controller.v1;

import com.heima.article.service.PinsService;
import com.heima.model.article.pojos.ApPins;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PinsManageController 单元测试")
class PinsManageControllerTest {

    @Mock
    private PinsService pinsService;

    @InjectMocks
    private PinsManageController pinsManageController;

    @Nested
    @DisplayName("list() - 查询动态列表")
    class ListTests {

        @Test
        @DisplayName("正常查询动态列表，返回成功结果")
        void shouldReturnPinsList() {
            ResponseResult expected = ResponseResult.okResult("pins_list");
            when(pinsService.list(isNull(), eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = pinsManageController.list(1, 10, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(pinsService).list(null, 1, 10, null);
        }

        @Test
        @DisplayName("按状态筛选动态列表")
        void shouldFilterByStatus() {
            ResponseResult expected = ResponseResult.okResult("filtered_pins");
            when(pinsService.list(isNull(), eq(1), eq(10), eq("9"))).thenReturn(expected);

            ResponseResult result = pinsManageController.list(1, 10, "9");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(pinsService).list(null, 1, 10, "9");
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_pins");
            when(pinsService.list(isNull(), eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = pinsManageController.list(1, 10, null);

            assertNotNull(result);
            verify(pinsService).list(null, 1, 10, null);
        }
    }

    @Nested
    @DisplayName("statistics() - 获取动态统计")
    class StatisticsTests {

        @Test
        @DisplayName("正常获取动态统计，返回成功结果")
        void shouldReturnPinsStatistics() {
            ResponseResult expected = ResponseResult.okResult("pins_statistics");
            when(pinsService.statistics(isNull())).thenReturn(expected);

            ResponseResult result = pinsManageController.statistics();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(pinsService).statistics(null);
        }
    }

    @Nested
    @DisplayName("create() - 创建动态")
    class CreateTests {

        @Test
        @DisplayName("正常创建动态，返回成功结果")
        void shouldCreatePinsSuccessfully() {
            ApPins pins = new ApPins();
            pins.setContent("测试动态内容");
            ResponseResult expected = ResponseResult.okResult("created");
            when(pinsService.createPins(any(ApPins.class))).thenReturn(expected);

            ResponseResult result = pinsManageController.create(pins);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(pinsService).createPins(pins);
        }

        @Test
        @DisplayName("创建动态失败时，透传错误")
        void shouldPropagateErrorWhenCreateFails() {
            ApPins pins = new ApPins();
            ResponseResult errorResult = ResponseResult.errorResult(501, "创建失败");
            when(pinsService.createPins(any(ApPins.class))).thenReturn(errorResult);

            ResponseResult result = pinsManageController.create(pins);

            assertNotNull(result);
            assertEquals(501, result.getCode());
        }
    }

    @Nested
    @DisplayName("delete() - 删除动态")
    class DeleteTests {

        @Test
        @DisplayName("正常删除动态，返回成功结果")
        void shouldDeletePinsSuccessfully() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 100L);
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(pinsService.deletePins(eq(100L))).thenReturn(expected);

            ResponseResult result = pinsManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(pinsService).deletePins(100L);
        }

        @Test
        @DisplayName("删除不存在的动态，透传错误")
        void shouldPropagateErrorWhenDeleteNotFound() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(pinsService.deletePins(eq(999L))).thenReturn(errorResult);

            ResponseResult result = pinsManageController.delete(body);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }
}