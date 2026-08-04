package com.heima.content.service.impl;

import com.heima.apis.notification.INotificationClient;
import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.content.mapper.ApCommentMapper;
import com.heima.content.mapper.UserBehaviorRecordMapper;
import com.heima.content.service.BailianAiService;
import com.heima.model.article.pojos.ApComment;
import com.heima.model.audit.AuditContext;
import com.heima.model.audit.AuditEntityType;
import com.heima.model.audit.AuditResult;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentAuditServiceTest {

    @Mock
    private ApCommentMapper apCommentMapper;

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @Mock
    private INotificationClient notificationClient;

    @Mock
    private GreenImageScanPlus greenImageScan;

    @InjectMocks
    private CommentAuditService commentAuditService;

    private ApComment createComment(Long id, Integer userId, String content) {
        ApComment comment = new ApComment();
        comment.setId(id);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setArticleId(1001L);
        return comment;
    }

    @BeforeEach
    void setUp() {
        // setUp中不设置AI服务stub，由具体需要audit()的测试方法自行设置
    }

    private void setupBailianAiService(boolean isViolation, String violationType, String violationReason) {
        BailianAiService aiService = mock(BailianAiService.class);
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("is_violation", isViolation);
        if (violationType != null) aiResult.put("violation_type", violationType);
        if (violationReason != null) aiResult.put("violation_reason", violationReason);
        when(aiService.checkViolation(anyLong(), any(), anyString())).thenReturn(aiResult);
        ReflectionTestUtils.setField(commentAuditService, "bailianAiService", aiService);
    }

    @Test
    void handlePassed_ShouldSendCommentNotification_WhenTargetUserExists() {
        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(1001L)
            .withTargetUserId(200);

        commentAuditService.handlePassed(context);

        verify(notificationClient).createNotification(anyMap());
    }

    @Test
    void handlePassed_ShouldNotSendNotification_WhenCommentNotFound() {
        when(apCommentMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, 999L, 100L)
            .withUserId(100)
            .withTargetUserId(200);

        commentAuditService.handlePassed(context);

        verify(notificationClient, never()).createNotification(anyMap());
    }

    @Test
    void handlePassed_ShouldNotSendNotification_WhenTargetUserIdIsNull() {
        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100);

        commentAuditService.handlePassed(context);

        verify(notificationClient, never()).createNotification(anyMap());
    }

    @Test
    void handlePassed_ShouldHandleNotificationClientException() {
        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(1001L)
            .withTargetUserId(200);

        when(notificationClient.createNotification(anyMap()))
            .thenThrow(new RuntimeException("通知服务异常"));

        // 不应抛出异常
        commentAuditService.handlePassed(context);
    }

    @Test
    void handleFailed_ShouldDeleteCommentAndUpdateBehaviorRecord() {
        Long commentId = 1L;
        Long targetId = 1001L;
        ApComment comment = createComment(commentId, 100, "违规评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setUserId(100);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(targetId);

        commentAuditService.handleFailed(context, "包含违规内容");

        verify(apCommentMapper).deleteById(commentId);
        verify(behaviorRecordMapper).updateById(record);
        assertEquals(0, record.getStatus().intValue());
        verify(notificationClient).createNotification(anyMap());
    }

    @Test
    void handleFailed_ShouldNotSendNotification_WhenCommentNotFound() {
        when(apCommentMapper.selectById(anyLong())).thenReturn(null);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, 999L, 100L);
        commentAuditService.handleFailed(context, "违规");

        verify(apCommentMapper, never()).deleteById(any());
        verify(notificationClient, never()).createNotification(anyMap());
    }

    @Test
    void handleFailed_ShouldHandleNotificationClientException() {
        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "违规评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(1001L);

        when(notificationClient.createNotification(anyMap()))
            .thenThrow(new RuntimeException("通知服务异常"));

        // 不应抛出异常
        commentAuditService.handleFailed(context, "违规");
        verify(apCommentMapper).deleteById(commentId);
    }

    @Test
    void asyncAuditComment_ShouldPass_WhenContentIsValid() throws Exception {
        setupBailianAiService(false, null, null);

        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "正常评论");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withContent("正常评论")
            .withTargetType(1)
            .withTargetId(1001L)
            .withTargetUserId(200);

        // 直接测试audit方法（asyncAuditComment中有Thread.sleep，不适合单元测试直接调用）
        AuditResult result = commentAuditService.audit(context);

        assertTrue(result.isPassed());
        verify(apCommentMapper).selectById(commentId);
    }

    @Test
    void asyncAuditComment_ShouldFail_WhenContentIsViolation() {
        setupBailianAiService(true, "广告", "包含广告信息");

        Long commentId = 1L;
        Long targetId = 1001L;
        ApComment comment = createComment(commentId, 100, "广告评论");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setUserId(100);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withContent("广告评论")
            .withTargetType(1)
            .withTargetId(targetId);

        AuditResult result = commentAuditService.audit(context);

        assertFalse(result.isPassed());
        verify(apCommentMapper).deleteById(commentId);
    }

    @Test
    void handlePassed_ShouldHandleNullNotificationClient() {
        // 设置notificationClient为null
        ReflectionTestUtils.setField(commentAuditService, "notificationClient", null);

        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "评论内容");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(1001L)
            .withTargetUserId(200);

        // 不应抛出异常
        commentAuditService.handlePassed(context);
    }

    @Test
    void handleFailed_ShouldHandleNullNotificationClient() {
        ReflectionTestUtils.setField(commentAuditService, "notificationClient", null);

        Long commentId = 1L;
        ApComment comment = createComment(commentId, 100, "违规评论");
        when(apCommentMapper.selectById(commentId)).thenReturn(comment);

        AuditContext context = new AuditContext(AuditEntityType.COMMENT, commentId, 100L)
            .withUserId(100)
            .withTargetType(1)
            .withTargetId(1001L);

        // 不应抛出异常
        commentAuditService.handleFailed(context, "违规");
        verify(apCommentMapper).deleteById(commentId);
    }
}