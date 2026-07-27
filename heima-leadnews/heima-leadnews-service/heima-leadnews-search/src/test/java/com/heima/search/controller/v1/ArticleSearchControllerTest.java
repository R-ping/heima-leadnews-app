package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ArticleSearchController 单元测试
 * 测试文章搜索控制器的请求参数校验与转发
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文章搜索控制器测试")
class ArticleSearchControllerTest {

    @Mock
    private ArticleSearchService articleSearchService;

    @InjectMocks
    private ArticleSearchController articleSearchController;

    private UserSearchDto validDto;
    private ResponseResult mockResponse;

    @BeforeEach
    void setUp() {
        validDto = new UserSearchDto();
        validDto.setSearchWords("测试关键词");
        validDto.setPageNum(1);
        validDto.setPageSize(10);
        validDto.setMinBehotTime(new Date());

        mockResponse = ResponseResult.okResult("mock data");
    }

    // ==================== search 方法测试 ====================

    @Nested
    @DisplayName("search 方法测试")
    class SearchMethodTests {

        @Test
        @DisplayName("正常搜索 — 完整参数传递给service")
        void shouldPassValidDtoToService() throws IOException {
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            ResponseResult result = articleSearchController.search(validDto);

            assertNotNull(result);
            assertEquals(mockResponse, result);
            verify(articleSearchService).search(validDto);
        }

        @Test
        @DisplayName("pageSize为0 — 自动设置为默认值10")
        void shouldSetDefaultPageSizeWhenZero() throws IOException {
            validDto.setPageSize(0);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            articleSearchController.search(validDto);

            verify(articleSearchService).search(argThat(dto -> dto.getPageSize() == 10));
        }

        @Test
        @DisplayName("pageSize非0 — 保持原值不变")
        void shouldKeepPageSizeWhenNotZero() throws IOException {
            validDto.setPageSize(20);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            articleSearchController.search(validDto);

            verify(articleSearchService).search(argThat(dto -> dto.getPageSize() == 20));
        }

        @Test
        @DisplayName("minBehotTime为null — 自动设置为当前时间")
        void shouldSetDefaultMinBehotTimeWhenNull() throws IOException {
            validDto.setMinBehotTime(null);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            articleSearchController.search(validDto);

            verify(articleSearchService).search(argThat(dto -> dto.getMinBehotTime() != null));
        }

        @Test
        @DisplayName("minBehotTime非null — 保持原值不变")
        void shouldKeepMinBehotTimeWhenNotNull() throws IOException {
            Date fixedDate = new Date(1000000000L);
            validDto.setMinBehotTime(fixedDate);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            articleSearchController.search(validDto);

            verify(articleSearchService).search(argThat(dto -> dto.getMinBehotTime().equals(fixedDate)));
        }

        @Test
        @DisplayName("pageSize为0且minBehotTime为null — 两个默认值同时设置")
        void shouldSetBothDefaultsWhenBothInvalid() throws IOException {
            validDto.setPageSize(0);
            validDto.setMinBehotTime(null);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            articleSearchController.search(validDto);

            verify(articleSearchService).search(argThat(dto ->
                    dto.getPageSize() == 10 && dto.getMinBehotTime() != null));
        }

        @Test
        @DisplayName("service抛出IOException — 异常向上传播")
        void shouldPropagateIOException() throws IOException {
            when(articleSearchService.search(any(UserSearchDto.class)))
                    .thenThrow(new IOException("ES连接失败"));

            assertThrows(IOException.class, () -> articleSearchController.search(validDto));
            verify(articleSearchService).search(any(UserSearchDto.class));
        }

        @Test
        @DisplayName("service返回null — 原样返回")
        void shouldReturnNullWhenServiceReturnsNull() throws IOException {
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(null);

            ResponseResult result = articleSearchController.search(validDto);

            assertNull(result);
            verify(articleSearchService).search(validDto);
        }

        @Test
        @DisplayName("searchWords为null — 正常传递不做额外处理")
        void shouldPassNullSearchWords() throws IOException {
            validDto.setSearchWords(null);
            when(articleSearchService.search(any(UserSearchDto.class))).thenReturn(mockResponse);

            ResponseResult result = articleSearchController.search(validDto);

            assertNotNull(result);
            verify(articleSearchService).search(validDto);
        }
    }
}