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
class PublishArticleBehaviorHandlerTest {

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private PublishArticleBehaviorHandler handler;

    @Test
    void getType_ShouldReturnPublishArticle() {
        assertEquals(BehaviorType.PUBLISH_ARTICLE, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenParamsValid() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.withTarget(1, 1001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("发布文章成功", result.getMessage());
        assertEquals(1001L, (Long) result.getDataValue("articleId"));

        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldReturnDuplicate_WhenAlreadyExists() {
        UserBehaviorRecord existing = new UserBehaviorRecord();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setBehaviorType(BehaviorType.PUBLISH_ARTICLE.getCode());
        existing.setTargetId(1001L);
        existing.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        verify(behaviorRecordMapper, never()).insert(any(UserBehaviorRecord.class));
    }

    @Test
    void execute_ShouldFail_WhenUserIdNull() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, null);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void execute_ShouldFail_WhenTargetIdNull() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldReturnSuccess() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("不支持撤销", result.getMessage());
    }

    @Test
    void execute_ShouldSetTargetUserId() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.withTarget(1, 1001L).withTargetUser(200);

        handler.execute(context);

        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>argThat(record ->
            record.getTargetUserId() != null && record.getTargetUserId() == 200
        ));
    }

    @Test
    void execute_ShouldSetCorrectBehaviorType() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_ARTICLE, 100);
        context.withTarget(1, 1001L);

        handler.execute(context);

        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>argThat(record ->
            BehaviorType.PUBLISH_ARTICLE.getCode().equals(record.getBehaviorType())
        ));
    }
}