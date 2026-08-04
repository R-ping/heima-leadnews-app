package com.heima.content.behavior.service.impl;

import com.heima.content.mapper.ApBehaviorLikesMapper;
import com.heima.content.mapper.UserBehaviorRecordMapper;
import com.heima.model.article.pojos.ApBehaviorLikes;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import org.junit.jupiter.api.BeforeEach;
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
class LikeBehaviorHandlerTest {

    @Mock
    private ApBehaviorLikesMapper apBehaviorLikesMapper;

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private LikeBehaviorHandler handler;

    @Test
    void getType_ShouldReturnLikeArticle() {
        assertEquals(BehaviorType.LIKE_ARTICLE, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenLikeArticle() {
        when(apBehaviorLikesMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.withTarget(1, 1001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("liked"));
        assertEquals("点赞成功", result.getMessage());

        verify(apBehaviorLikesMapper).insert(Mockito.<ApBehaviorLikes>any());
        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldSucceed_WhenLikePin() {
        when(apBehaviorLikesMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_PIN, 100);
        context.withTarget(2, 2001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("点赞成功", result.getMessage());
    }

    @Test
    void execute_ShouldReturnDuplicate_WhenAlreadyLiked() {
        ApBehaviorLikes existing = new ApBehaviorLikes();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setEntryId(1001L);
        existing.setOperation(0);
        when(apBehaviorLikesMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("liked"));

        verify(apBehaviorLikesMapper, never()).insert(Mockito.<ApBehaviorLikes>any());
        verify(behaviorRecordMapper, never()).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldSucceed_WhenLiked() {
        ApBehaviorLikes existing = new ApBehaviorLikes();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setEntryId(1001L);
        existing.setOperation(0);
        when(apBehaviorLikesMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertFalse((Boolean) result.getDataValue("liked"));
        assertEquals("取消点赞成功", result.getMessage());

        assertEquals(1, existing.getOperation().intValue());
        verify(apBehaviorLikesMapper).updateById(existing);
    }

    @Test
    void rollback_ShouldFail_WhenNotLiked() {
        when(apBehaviorLikesMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("未点赞", result.getMessage());
    }

    @Test
    void rollback_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }
}