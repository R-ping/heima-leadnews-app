package com.heima.article.controller.v1;

import com.heima.article.service.TagService;
import com.heima.model.article.pojos.ApTag;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagController 单元测试")
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @Nested
    @DisplayName("findList() - 查询标签列表")
    class FindListTests {

        @Test
        @DisplayName("正常查询标签列表，返回成功结果")
        void shouldReturnTagList() {
            List<ApTag> mockData = Collections.singletonList(new ApTag());
            when(tagService.findList("java")).thenReturn(mockData);

            ResponseResult result = tagController.findList("java");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(tagService).findList("java");
        }

        @Test
        @DisplayName("keyword为null时，正常委托给service")
        void shouldDelegateToServiceWhenKeywordIsNull() {
            List<ApTag> mockData = Collections.emptyList();
            when(tagService.findList(isNull())).thenReturn(mockData);

            ResponseResult result = tagController.findList(null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(tagService).findList(null);
        }

        @Test
        @DisplayName("keyword为空字符串时，正常委托给service")
        void shouldDelegateToServiceWhenKeywordIsEmpty() {
            List<ApTag> mockData = Collections.emptyList();
            when(tagService.findList("")).thenReturn(mockData);

            ResponseResult result = tagController.findList("");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(tagService).findList("");
        }

        @Test
        @DisplayName("Service抛出异常时，异常向上传播")
        void shouldPropagateException() {
            when(tagService.findList("invalid")).thenThrow(new RuntimeException("服务异常"));

            assertThrows(RuntimeException.class, () -> tagController.findList("invalid"));
        }
    }
}