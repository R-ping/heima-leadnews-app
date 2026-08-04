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
class BrowseBehaviorHandlerTest {

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private BrowseBehaviorHandler handler;

    @Test
    void getType_ShouldReturnBrowseArticle() {
        assertEquals(BehaviorType.BROWSE_ARTICLE, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenNewBrowse() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_ARTICLE, 100);
        context.withTarget(1, 1001L).withTargetUser(200);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("browsed"));
        assertEquals("浏览记录成功", result.getMessage());

        verify(behaviorRecordMapper).insert(any(UserBehaviorRecord.class));
    }

    @Test
    void execute_ShouldSucceed_WhenBrowsingPin() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_PIN, 100);
        context.withTarget(2, 2001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("浏览记录成功", result.getMessage());
    }

    @Test
    void execute_ShouldSucceed_WhenBrowsingCourse() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_COURSE, 100);
        context.withTarget(4, 3001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("浏览记录成功", result.getMessage());
    }

    @Test
    void execute_ShouldUpdateTime_WhenAlreadyBrowsedToday() {
        UserBehaviorRecord existing = new UserBehaviorRecord();
        existing.setId(1L);
        existing.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(existing);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("browsed"));

        verify(behaviorRecordMapper).updateById(Mockito.<UserBehaviorRecord>any());
        verify(behaviorRecordMapper, never()).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldFail_WhenParamsMissing() {
        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_ARTICLE, 100);
        context.setTargetId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldSucceed_WhenRecordExists() {
        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("浏览记录已清除", result.getMessage());
        assertEquals(0, record.getStatus().intValue());
        verify(behaviorRecordMapper).updateById(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void rollback_ShouldSucceed_WhenNoRecord() {
        when(behaviorRecordMapper.selectOne(any())).thenReturn(null);

        BehaviorContext context = new BehaviorContext(BehaviorType.BROWSE_ARTICLE, 100);
        context.withTarget(1, 1001L);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals("浏览记录已清除", result.getMessage());
        verify(behaviorRecordMapper, never()).updateById(Mockito.<UserBehaviorRecord>any());
    }
}