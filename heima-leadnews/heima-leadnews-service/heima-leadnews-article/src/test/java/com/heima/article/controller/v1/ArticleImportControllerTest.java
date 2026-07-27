package com.heima.article.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleImportController 单元测试")
class ArticleImportControllerTest {

    @InjectMocks
    private ArticleImportController articleImportController;

    @Nested
    @DisplayName("importMarkdown() - 导入Markdown文件")
    class ImportMarkdownTests {

        @Test
        @DisplayName("正常导入Markdown文件，提取标题和内容")
        void shouldImportMarkdownFileWithTitle() {
            String content = "# 测试文章标题\n\n这是第一段内容。\n\n这是第二段内容。";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.md", "text/markdown",
                    content.getBytes(StandardCharsets.UTF_8));

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("导入Markdown文件，使用文件名作为标题")
        void shouldUseFilenameAsTitleWhenNoHeading() {
            String content = "这是没有标题的内容。\n没有#开头的行。";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "my-article.md", "text/markdown",
                    content.getBytes(StandardCharsets.UTF_8));

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
        }

        @Test
        @DisplayName("文件为null时，返回400错误")
        void shouldReturnError400WhenFileIsNull() {
            ResponseResult result = articleImportController.importMarkdown(null);

            assertNotNull(result);
            assertEquals(400, result.getCode());
            assertEquals("文件不能为空", result.getMessage());
        }

        @Test
        @DisplayName("文件为空时，返回400错误")
        void shouldReturnError400WhenFileIsEmpty() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.md", "text/markdown", new byte[0]);

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(400, result.getCode());
            assertEquals("文件不能为空", result.getMessage());
        }

        @Test
        @DisplayName("文件不是.md格式时，返回400错误")
        void shouldReturnError400WhenNotMarkdownFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain",
                    "content".getBytes(StandardCharsets.UTF_8));

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(400, result.getCode());
            assertEquals("仅支持.md格式文件", result.getMessage());
        }

        @Test
        @DisplayName("无扩展名的文件，返回400错误")
        void shouldReturnError400WhenNoExtension() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test", "text/plain",
                    "content".getBytes(StandardCharsets.UTF_8));

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("Markdown文件多级标题，取第一个#标题")
        void shouldExtractFirstHeadingAsTitle() {
            String content = "# 主标题\n## 副标题\n\n正文内容。";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.md", "text/markdown",
                    content.getBytes(StandardCharsets.UTF_8));

            ResponseResult result = articleImportController.importMarkdown(file);

            assertNotNull(result);
            assertEquals(200, result.getCode());
        }

        @Test
        @DisplayName("文件读取异常时，返回500错误")
        void shouldReturnError500WhenReadFails() throws IOException {
            MultipartFile mockFile = new MockMultipartFile(
                    "file", "test.md", "text/markdown",
                    "content".getBytes(StandardCharsets.UTF_8)) {
                @Override
                public InputStream getInputStream() throws IOException {
                    throw new IOException("读取失败");
                }
            };

            ResponseResult result = articleImportController.importMarkdown(mockFile);

            assertNotNull(result);
            assertEquals(500, result.getCode());
        }
    }
}