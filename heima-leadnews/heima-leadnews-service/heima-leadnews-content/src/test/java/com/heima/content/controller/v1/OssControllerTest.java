package com.heima.content.controller.v1;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PolicyConditions;
import com.heima.content.config.OssConfig;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URL;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OssControllerTest {

    @Mock
    private OssConfig ossConfig;

    @Mock
    private OSS ossClient;

    @InjectMocks
    private OssController ossController;

    @BeforeEach
    void setUp() {
        when(ossConfig.getExpireTime()).thenReturn(300L);
        when(ossConfig.getDir()).thenReturn("test-dir/");
        when(ossConfig.getAccessKeyId()).thenReturn("test-access-key-id");
        when(ossConfig.getHost()).thenReturn("https://test-bucket.oss-cn-hangzhou.aliyuncs.com");
    }

    // ==================== postSignature() 测试 ====================

    @Test
    void testPostSignatureSuccess() {
        String encodedPolicy = "dGVzdC1wb2xpY3k=";
        String postSignature = "test-signature";

        when(ossClient.generatePostPolicy(any(Date.class), any(PolicyConditions.class))).thenReturn("test-policy");
        when(ossClient.calculatePostSignature(anyString())).thenReturn(postSignature);

        ResponseResult result = ossController.postSignature();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof JSONObject);

        JSONObject data = (JSONObject) result.getData();
        assertEquals("test-access-key-id", data.getString("ossAccessKeyId"));
        assertEquals(postSignature, data.getString("signature"));
        assertEquals("test-dir/", data.getString("dir"));
        assertEquals("https://test-bucket.oss-cn-hangzhou.aliyuncs.com", data.getString("host"));
        assertNotNull(data.getString("policy"));
    }

    @Test
    void testPostSignatureOssException() {
        when(ossClient.generatePostPolicy(any(Date.class), any(PolicyConditions.class)))
                .thenThrow(new OSSException("OSS error"));

        ResponseResult result = ossController.postSignature();

        assertEquals(503, result.getCode());
        assertEquals("获取上传签名失败", result.getMessage());
    }

    @Test
    void testPostSignatureClientException() {
        when(ossClient.generatePostPolicy(any(Date.class), any(PolicyConditions.class)))
                .thenThrow(new ClientException("Client connection error"));

        ResponseResult result = ossController.postSignature();

        assertEquals(503, result.getCode());
        assertEquals("获取上传签名失败", result.getMessage());
    }

    @Test
    void testPostSignatureUnexpectedException() {
        when(ossClient.generatePostPolicy(any(Date.class), any(PolicyConditions.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseResult result = ossController.postSignature();

        assertEquals(503, result.getCode());
        assertEquals("获取上传签名失败", result.getMessage());
    }

    // ==================== presignedUrl() 测试 ====================

    @Test
    void testPresignedUrlSuccess() throws Exception {
        String key = "images/test.jpg";
        URL signedUrl = new URL("https://test-bucket.oss-cn-hangzhou.aliyuncs.com/" + key + "?OSSAccessKeyId=test&Signature=test&Expires=9999999999");

        when(ossConfig.getBucket()).thenReturn("test-bucket");
        when(ossClient.generatePresignedUrl(anyString(), anyString(), any(Date.class))).thenReturn(signedUrl);

        ResponseResult result = ossController.presignedUrl(key);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof JSONObject);

        JSONObject data = (JSONObject) result.getData();
        assertEquals(signedUrl.toString(), data.getString("url"));
    }

    @Test
    void testPresignedUrlException() {
        String key = "images/test.jpg";
        when(ossConfig.getBucket()).thenReturn("test-bucket");
        when(ossClient.generatePresignedUrl(anyString(), anyString(), any(Date.class)))
                .thenThrow(new RuntimeException("OSS unavailable"));

        ResponseResult result = ossController.presignedUrl(key);

        assertEquals(503, result.getCode());
        assertEquals("生成签名URL失败", result.getMessage());
    }
}