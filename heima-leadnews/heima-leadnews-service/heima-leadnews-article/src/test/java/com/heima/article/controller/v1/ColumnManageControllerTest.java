package com.heima.article.controller.v1;

import com.heima.article.service.ColumnService;
import com.heima.model.article.pojos.ApColumn;
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
@DisplayName("ColumnManageController 单元测试")
class ColumnManageControllerTest {

    @Mock
    private ColumnService columnService;

    @InjectMocks
    private ColumnManageController columnManageController;

    @Nested
    @DisplayName("list() - 查询专栏列表")
    class ListTests {

        @Test
        @DisplayName("正常查询专栏列表，返回成功结果")
        void shouldReturnColumnList() {
            ResponseResult expected = ResponseResult.okResult("column_list");
            when(columnService.list(isNull(), eq(1), eq(10), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = columnManageController.list(1, 10, null, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(columnService).list(null, 1, 10, null, null);
        }

        @Test
        @DisplayName("按状态和标题筛选专栏列表")
        void shouldFilterByStatusAndTitle() {
            ResponseResult expected = ResponseResult.okResult("filtered_columns");
            when(columnService.list(isNull(), eq(1), eq(10), eq("9"), eq("测试")))
                    .thenReturn(expected);

            ResponseResult result = columnManageController.list(1, 10, "9", "测试");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(columnService).list(null, 1, 10, "9", "测试");
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_columns");
            when(columnService.list(isNull(), eq(1), eq(10), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = columnManageController.list(1, 10, null, null);

            assertNotNull(result);
            verify(columnService).list(null, 1, 10, null, null);
        }
    }

    @Nested
    @DisplayName("statistics() - 获取专栏统计")
    class StatisticsTests {

        @Test
        @DisplayName("正常获取专栏统计，返回成功结果")
        void shouldReturnColumnStatistics() {
            ResponseResult expected = ResponseResult.okResult("column_statistics");
            when(columnService.statistics(isNull())).thenReturn(expected);

            ResponseResult result = columnManageController.statistics();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(columnService).statistics(null);
        }
    }

    @Nested
    @DisplayName("create() - 创建专栏")
    class CreateTests {

        @Test
        @DisplayName("正常创建专栏，返回成功结果")
        void shouldCreateColumnSuccessfully() {
            ApColumn column = new ApColumn();
            column.setTitle("新专栏");
            ResponseResult expected = ResponseResult.okResult("created");
            when(columnService.createColumn(any(ApColumn.class))).thenReturn(expected);

            ResponseResult result = columnManageController.create(column);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(columnService).createColumn(column);
        }

        @Test
        @DisplayName("创建专栏失败时，透传错误")
        void shouldPropagateErrorWhenCreateFails() {
            ApColumn column = new ApColumn();
            ResponseResult errorResult = ResponseResult.errorResult(501, "创建失败");
            when(columnService.createColumn(any(ApColumn.class))).thenReturn(errorResult);

            ResponseResult result = columnManageController.create(column);

            assertNotNull(result);
            assertEquals(501, result.getCode());
        }
    }

    @Nested
    @DisplayName("update() - 更新专栏")
    class UpdateTests {

        @Test
        @DisplayName("正常更新专栏，返回成功结果")
        void shouldUpdateColumnSuccessfully() {
            ApColumn column = new ApColumn();
            column.setId(100L);
            column.setTitle("更新后的专栏");
            ResponseResult expected = ResponseResult.okResult("updated");
            when(columnService.updateColumn(any(ApColumn.class))).thenReturn(expected);

            ResponseResult result = columnManageController.update(column);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(columnService).updateColumn(column);
        }

        @Test
        @DisplayName("更新不存在的专栏，透传错误")
        void shouldPropagateErrorWhenUpdateNotFound() {
            ApColumn column = new ApColumn();
            column.setId(999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(columnService.updateColumn(any(ApColumn.class))).thenReturn(errorResult);

            ResponseResult result = columnManageController.update(column);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }

    @Nested
    @DisplayName("delete() - 删除专栏")
    class DeleteTests {

        @Test
        @DisplayName("正常删除专栏，返回成功结果")
        void shouldDeleteColumnSuccessfully() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 100L);
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(columnService.deleteColumn(eq(100L))).thenReturn(expected);

            ResponseResult result = columnManageController.delete(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(columnService).deleteColumn(100L);
        }

        @Test
        @DisplayName("删除不存在的专栏，透传错误")
        void shouldPropagateErrorWhenDeleteNotFound() {
            Map<String, Long> body = new HashMap<>();
            body.put("id", 999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(columnService.deleteColumn(eq(999L))).thenReturn(errorResult);

            ResponseResult result = columnManageController.delete(body);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
        }
    }
}