package com.heima.content.behavior.service.impl;

import com.heima.content.mapper.ApFollowMapper;
import com.heima.content.mapper.UserBehaviorRecordMapper;
import com.heima.model.article.pojos.ApFollow;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import com.heima.model.behavior.pojos.UserBehaviorRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class FollowBehaviorHandlerTest {

    @Mock
    private ApFollowMapper apFollowMapper;

    @Mock
    private UserBehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private FollowBehaviorHandler handler;

    private BehaviorContext context;

    @BeforeEach
    void setUp() {
        context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        context.withTarget(3, 200L)
            .withTargetUser(200);
    }

    @Test
    void getType_ShouldReturnFollowUser() {
        assertEquals(BehaviorType.FOLLOW_USER, handler.getType());
    }

    @Test
    void execute_ShouldSucceed_WhenNotFollowing() {
        when(apFollowMapper.selectOne(any())).thenReturn(null);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isNewRecord());
        assertEquals("关注成功", result.getMessage());
        assertTrue((Boolean) result.getDataValue("followed"));

        verify(apFollowMapper).insert(Mockito.<ApFollow>any());
        verify(behaviorRecordMapper).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldReturnDuplicate_WhenAlreadyFollowing() {
        ApFollow existing = new ApFollow();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setFollowUserId(200);
        when(apFollowMapper.selectOne(any())).thenReturn(existing);

        BehaviorResult result = handler.execute(context);

        assertTrue(result.isSuccess());
        assertFalse(result.isNewRecord());
        assertTrue((Boolean) result.getDataValue("followed"));
        assertEquals(Long.valueOf(1L), result.getDataValue("followId"));

        verify(apFollowMapper, never()).insert(Mockito.<ApFollow>any());
        verify(behaviorRecordMapper, never()).insert(Mockito.<UserBehaviorRecord>any());
    }

    @Test
    void execute_ShouldFail_WhenSelfFollowing() {
        context.setTargetUserId(100);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("不能关注自己", result.getMessage());
    }

    @Test
    void execute_ShouldFail_WhenParamsMissing() {
        context.setTargetUserId(null);

        BehaviorResult result = handler.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldSucceed_WhenFollowing() {
        ApFollow existing = new ApFollow();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setFollowUserId(200);
        when(apFollowMapper.selectOne(any())).thenReturn(existing);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertFalse((Boolean) result.getDataValue("followed"));
        assertEquals("取消关注成功", result.getMessage());

        verify(apFollowMapper).deleteById(1L);
    }

    @Test
    void rollback_ShouldFail_WhenNotFollowing() {
        when(apFollowMapper.selectOne(any())).thenReturn(null);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("未关注该用户", result.getMessage());
    }

    @Test
    void rollback_ShouldFail_WhenParamsMissing() {
        context.setTargetUserId(null);

        BehaviorResult result = handler.rollback(context);

        assertFalse(result.isSuccess());
        assertEquals("参数不完整", result.getMessage());
    }

    @Test
    void rollback_ShouldUpdateBehaviorRecord_WhenExists() {
        ApFollow existing = new ApFollow();
        existing.setId(1L);
        existing.setUserId(100);
        existing.setFollowUserId(200);
        when(apFollowMapper.selectOne(any())).thenReturn(existing);

        UserBehaviorRecord record = new UserBehaviorRecord();
        record.setId(1L);
        record.setStatus(1);
        when(behaviorRecordMapper.selectOne(any())).thenReturn(record);

        BehaviorResult result = handler.rollback(context);

        assertTrue(result.isSuccess());
        assertEquals(0, record.getStatus().intValue());
        verify(behaviorRecordMapper).updateById(record);
    }
}