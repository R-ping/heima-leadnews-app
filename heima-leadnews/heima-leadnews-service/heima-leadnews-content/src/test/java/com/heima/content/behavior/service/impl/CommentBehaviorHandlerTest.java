package com.heima.content.behavior.service.impl;

import com.heima.content.mapper.UserBehaviorRecordMapper;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class CommentBehaviorHandlerTest {

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private CommentBehaviorHandler handler;

    @Test
    void getType_ShouldReturnCommentArticle() {
        assertEquals(BehaviorType.COMMENT_ARTICLE, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenCommentingArticle() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COMMENT_ARTICLE, 100);
        context.withTarget(1, 1001L)
            .withTargetUser(200)
            .withExtra("commentId", 500L)
            .withExtra("commentContent", "好文章！");

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("评论成功", result.getMessage());
        assertEquals(Long.valueOf(500L), result.getDataValue("commentId"));

        verify(behaviorRecordMapper).insert(any(UserBehaviorRecord.class));
    }

    @Test
    void execute_ShouldSucceed_WhenCommentingPin() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COMMENT_PIN, 100);
        context.withTarget(2, 2001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("评论成功", result.getMessage());

        verify(behaviorRecordMapper).insert(any(UserBehaviorRecord.class));
    }

    @Test
    void execute_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COMMENT_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldSucceed_WhenCommentExists() {
        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        BehaviorContext context = new BehaviorContext(BehaviorType.COMMENT_ARTICLE, 100);
        context.withTarget(1, 1001L)
            .withExtra("commentId", 500L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("评论已删除", result.getMessage());
        assertEquals(0, record.getStatus().intValue());
        verify(behaviorRecordMapper).updateById(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void rollback_ShouldSucceed_WhenNoCommentId() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COMMENT_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("评论已删除", result.getMessage());
        verify(behaviorRecordMapper, never()).selectOne(any());
        verify(behaviorRecordMapper, never()).updateById(Mockito.<UserBehaviorRecord>any());
    }
}