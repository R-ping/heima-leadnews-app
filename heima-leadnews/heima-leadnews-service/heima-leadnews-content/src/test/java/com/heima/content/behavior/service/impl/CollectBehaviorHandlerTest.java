package com.heima.content.behavior.service.impl;

import com.heima.content.mapper.ApCollectionMapper;
import com.heima.content.mapper.UserBehaviorRecordMapper;
import com.heima.model.article.pojos.ApCollection;
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
class CollectBehaviorHandlerTest {

    @Mock
    private ApCollectionMapper apCollectionMapper;

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private CollectBehaviorHandler handler;

    @Test
    void getType_ShouldReturnCollectArticle() {
        assertEquals(BehaviorType.COLLECT_ARTICLE, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenNotCollected() {
        when(apCollectionMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.withTarget(1, 1001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("collected"));
        assertEquals("收藏成功", result.getMessage());

        verify(apCollectionMapper).insert(Mockito.<ApCollection>any());
        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldReturnDuplicate_WhenAlreadyCollected() {
        ApCollection existing = new ApCollection();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setArticleId(1001L);
        when(apCollectionMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("collected"));

        verify(apCollectionMapper, never()).insert(Mockito.<ApCollection>any());
        verify(behaviorRecordMapper, never()).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldSucceed_WhenCollected() {
        ApCollection existing = new ApCollection();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setArticleId(1001L);
        when(apCollectionMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertFalse((Boolean) result.getDataValue("collected"));
        assertEquals("取消收藏成功", result.getMessage());

        verify(apCollectionMapper).deleteById(1L);
    }

    @Test
    void rollback_ShouldFail_WhenNotCollected() {
        when(apCollectionMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("未收藏", result.getMessage());
    }

    @Test
    void rollback_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.COLLECT_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }
}