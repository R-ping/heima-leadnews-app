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
class PublishPinBehaviorHandlerTest {

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private PublishPinBehaviorHandler handler;

    @Test
    void getType_ShouldReturnPublishPin() {
        assertEquals(BehaviorType.PUBLISH_PIN, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenParamsValid() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.withTarget(2, 2001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("发布沸点成功", result.getMessage());
        assertEquals(2001L, (Long) result.getDataValue("pinsId"));

        verify(behaviorRecordMapper).insert(any(UserBehaviorRecord.class));
    }

    @Test
    void execute_ShouldReturnDuplicate_WhenAlreadyExists() {
        UserBehaviorRecord existing = new UserBehaviorRecord();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setBehaviorType(BehaviorType.PUBLISH_PIN.getCode());
        existing.setTargetId(2001L);
        existing.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.withTarget(2, 2001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        verify(behaviorRecordMapper, never()).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldFail_WhenUserIdNull() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, null);
        context.withTarget(2, 2001L);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void execute_ShouldFail_WhenTargetIdNull() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldUpdateRecordStatus() {
        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setUserId(100);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.withTarget(2, 2001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("沸点已删除", result.getMessage());
        assertEquals(0, record.getStatus().intValue());
        verify(behaviorRecordMapper).updateById(record);
    }

    @Test
    void rollback_ShouldSucceed_WhenNoRecordFound() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.withTarget(2, 2001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        verify(behaviorRecordMapper, never()).updateById(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void rollback_ShouldHandleNullTargetId() {
        BehaviorContext context = new BehaviorContext(BehaviorType.PUBLISH_PIN, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        verify(behaviorRecordMapper, never()).selectOne(any());
    }
}