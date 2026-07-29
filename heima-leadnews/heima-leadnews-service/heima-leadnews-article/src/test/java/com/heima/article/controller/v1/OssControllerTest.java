package com.heima.article.controller.v1;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PolicyConditions;
import com.heima.article.config.OssConfig;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OssController 单元测试")
class OssControllerTest {

    @Mock
    private OssConfig ossConfig;

    @Mock
    private OSS ossClient;

    @InjectMocks
    private OssController ossController;

    @BeforeEach
    void setUpOssConfig() {
        when(ossConfig.getAccessKeyId()).thenReturn("test-access-key");
        when(ossConfig.getDir()).thenReturn("uploads/");
        when(ossConfig.getHost()).thenReturn("https://test-bucket.oss-cn-hangzhou.aliyuncs.com");
        when(ossConfig.getExpireTime()).thenReturn(300L);
        when(ossConfig.getBucket()).thenReturn("test-bucket");
    }

    @Nested
    @DisplayName("postSignature() - 获取上传签名")
    class PostSignatureTests {

        @Test
        @DisplayName("正常获取上传签名，返回成功结果")
        void shouldReturnPostSignature() {
            when(ossClient.generatePostPolicy(any(java.util.Date.class), any(PolicyConditions.class)))
                    .thenReturn("test-policy");
            when(ossClient.calculatePostSignature(anyString())).thenReturn("test-signature");

            ResponseResult result = ossController.postSignature();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(ossClient).generatePostPolicy(any(java.util.Date.class), any(PolicyConditions.class));
            verify(ossClient).calculatePostSignature("test-policy");
        }

        @Test
        @DisplayName("OSS发生异常时，返回503错误")
        void shouldReturnError503WhenOSSException() {
            when(ossClient.generatePostPolicy(any(java.util.Date.class), any(PolicyConditions.class)))
                    .thenThrow(new OSSException("OSS Error"));

            ResponseResult result = ossController.postSignature();

            assertNotNull(result);
            assertEquals(503, result.getCode());
            assertEquals("获取上传签名失败", result.getMessage());
        }

        @Test
        @DisplayName("Client发生异常时，返回503错误")
        void shouldReturnError503WhenClientException() {
            when(ossClient.generatePostPolicy(any(java.util.Date.class), any(PolicyConditions.class)))
                    .thenThrow(new ClientException("Client Error"));

            ResponseResult result = ossController.postSignature();

            assertNotNull(result);
            assertEquals(503, result.getCode());
            assertEquals("获取上传签名失败", result.getMessage());
        }

        @Test
        @DisplayName("发生未知异常时，返回503错误")
        void shouldReturnError503WhenUnexpectedException() {
            when(ossClient.generatePostPolicy(any(java.util.Date.class), any(PolicyConditions.class)))
                    .thenThrow(new RuntimeException("Unexpected"));

            ResponseResult result = ossController.postSignature();

            assertNotNull(result);
            assertEquals(503, result.getCode());
            assertEquals("获取上传签名失败", result.getMessage());
        }
    }

    @Nested
    @DisplayName("presignedUrl() - 获取预签名URL")
    class PresignedUrlTests {

        @Test
        @DisplayName("正常获取预签名URL，返回成功结果")
        void shouldReturnPresignedUrl() throws MalformedURLException {
            URL mockUrl = new URL("https://test-bucket.oss-cn-hangzhou.aliyuncs.com/uploads/test.jpg?sign=xxx");
            when(ossClient.generatePresignedUrl(eq("test-bucket"), eq("uploads/test.jpg"), any(java.util.Date.class)))
                    .thenReturn(mockUrl);

            ResponseResult result = ossController.presignedUrl("uploads/test.jpg");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());
            verify(ossClient).generatePresignedUrl(eq("test-bucket"), eq("uploads/test.jpg"), any(java.util.Date.class));
        }

        @Test
        @DisplayName("生成预签名URL失败时，返回503错误")
        void shouldReturnError503WhenPresignedUrlFails() {
            when(ossClient.generatePresignedUrl(anyString(), anyString(), any(java.util.Date.class)))
                    .thenThrow(new RuntimeException("Generate failed"));

            ResponseResult result = ossController.presignedUrl("uploads/test.jpg");

            assertNotNull(result);
            assertEquals(503, result.getCode());
            assertEquals("生成签名URL失败", result.getMessage());
        }
    }
}