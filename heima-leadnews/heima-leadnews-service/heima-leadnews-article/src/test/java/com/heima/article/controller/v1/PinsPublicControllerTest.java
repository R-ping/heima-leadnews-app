package com.heima.article.controller.v1;

import com.aliyun.oss.OSS;
import com.heima.article.config.OssConfig;
import com.heima.article.service.PinsPublicService;
import com.heima.model.article.dtos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PinsPublicControllerTest {

    @Mock
    private PinsPublicService pinsPublicService;

    @Mock
    private OSS ossClient;

    @Mock
    private OssConfig ossConfig;

    @InjectMocks
    private PinsPublicController controller;

    // ==================== list() tests ====================

    @Test
    void testList() {
        when(pinsPublicService.list(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("latest", 1, 10);

        assertEquals(200, result.getCode());
        verify(pinsPublicService).list("latest", 1, 10);
    }

    @Test
    void testListDefaultParams() {
        when(pinsPublicService.list(eq("latest"), eq(1), eq(10)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListInvalidPage() {
        when(pinsPublicService.list(eq("latest"), eq(1), eq(10)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("latest", 0, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListInvalidSize() {
        when(pinsPublicService.list(eq("latest"), eq(1), eq(10)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("latest", 1, 100);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListHot() {
        when(pinsPublicService.list("hot", 1, 10))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListFollowing() {
        when(pinsPublicService.list("following", 1, 10))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.list("following", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== sidebar() tests ====================

    @Test
    void testSidebar() {
        when(pinsPublicService.sidebar()).thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.sidebar();

        assertEquals(200, result.getCode());
    }

    // ==================== publish() tests ====================

    @Test
    void testPublish() {
        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test content");
        when(pinsPublicService.publish(any(PinsPublishDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishEmptyContent() {
        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("");

        ResponseResult result = controller.publish(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishNullContent() {
        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent(null);

        ResponseResult result = controller.publish(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishContentTooLong() {
        PinsPublishDTO dto = new PinsPublishDTO();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            sb.append("a");
        }
        dto.setContent(sb.toString());

        ResponseResult result = controller.publish(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishMaxLength() {
        PinsPublishDTO dto = new PinsPublishDTO();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        dto.setContent(sb.toString());
        when(pinsPublicService.publish(any(PinsPublishDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.publish(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== like() tests ====================

    @Test
    void testLike() {
        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(true);
        when(pinsPublicService.like(any(PinsLikeDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.like(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testLikeNullPinsId() {
        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(null);

        ResponseResult result = controller.like(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUnlike() {
        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(false);
        when(pinsPublicService.like(any(PinsLikeDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.like(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment() tests ====================

    @Test
    void testCreateComment() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("nice");
        when(pinsPublicService.createComment(any(PinsCommentDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.createComment(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateCommentNullPinsId() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(null);
        dto.setContent("nice");

        ResponseResult result = controller.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentEmptyContent() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("");

        ResponseResult result = controller.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentNullContent() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent(null);

        ResponseResult result = controller.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentTooLong() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            sb.append("a");
        }
        dto.setContent(sb.toString());

        ResponseResult result = controller.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentMaxLength() {
        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("a");
        }
        dto.setContent(sb.toString());
        when(pinsPublicService.createComment(any(PinsCommentDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.createComment(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== commentList() tests ====================

    @Test
    void testCommentList() {
        when(pinsPublicService.commentList(1L, 1, 10))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCommentListDefaultParams() {
        when(pinsPublicService.commentList(eq(1L), eq(1), eq(10)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCommentListInvalidPage() {
        when(pinsPublicService.commentList(eq(1L), eq(1), eq(10)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.commentList(1L, 0, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== share() tests ====================

    @Test
    void testShare() {
        PinsShareDTO dto = new PinsShareDTO();
        dto.setPinsId(1L);
        when(pinsPublicService.share(any(PinsShareDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.share(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testShareNullPinsId() {
        PinsShareDTO dto = new PinsShareDTO();
        dto.setPinsId(null);

        ResponseResult result = controller.share(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== linkPreview() tests ====================

    @Test
    void testLinkPreview() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("https://example.com");
        when(pinsPublicService.linkPreview(any(PinsLinkPreviewDTO.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.linkPreview(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testLinkPreviewNullUrl() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl(null);

        ResponseResult result = controller.linkPreview(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLinkPreviewEmptyUrl() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("");

        ResponseResult result = controller.linkPreview(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== topics() tests ====================

    @Test
    void testTopics() {
        when(pinsPublicService.topics("Java", 1, 20))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.topics("Java", 1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    void testTopicsNoKeyword() {
        when(pinsPublicService.topics(null, 1, 20))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.topics(null, 1, 20);

        assertEquals(200, result.getCode());
    }

    @Test
    void testTopicsInvalidPage() {
        when(pinsPublicService.topics(eq(null), eq(1), eq(20)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.topics(null, 0, 20);

        assertEquals(200, result.getCode());
    }

    // ==================== circles() tests ====================

    @Test
    void testCircles() {
        when(pinsPublicService.circles()).thenReturn(ResponseResult.okResult());

        ResponseResult result = controller.circles();

        assertEquals(200, result.getCode());
    }

    // ==================== uploadImage() tests ====================

    @Test
    void testUploadImage() throws Exception {
        byte[] content = "fake image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", content);

        when(ossConfig.getDir()).thenReturn("pins/");
        when(ossConfig.getBucket()).thenReturn("test-bucket");
        when(ossConfig.getHost()).thenReturn("https://cdn.example.com");
        when(ossClient.putObject(anyString(), anyString(), any(InputStream.class)))
                .thenReturn(null);

        ResponseResult result = controller.uploadImage(file);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("url"));
        assertNotNull(data.get("key"));
    }

    @Test
    void testUploadImageNullFile() {
        ResponseResult result = controller.uploadImage(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUploadImageEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        ResponseResult result = controller.uploadImage(file);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUploadImageWithoutOriginalFilename() throws Exception {
        byte[] content = "fake image".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", null, "image/jpeg", content);

        when(ossConfig.getDir()).thenReturn("pins/");
        when(ossConfig.getBucket()).thenReturn("test-bucket");
        when(ossConfig.getHost()).thenReturn("https://cdn.example.com");
        when(ossClient.putObject(anyString(), anyString(), any(InputStream.class)))
                .thenReturn(null);

        ResponseResult result = controller.uploadImage(file);

        assertEquals(200, result.getCode());
    }
}