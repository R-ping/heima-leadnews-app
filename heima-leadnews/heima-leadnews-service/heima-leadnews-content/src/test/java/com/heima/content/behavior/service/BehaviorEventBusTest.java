package com.heima.content.behavior.service;

import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehaviorEventBusTest {

    @Mock
    private BehaviorHandler followHandler;

    @Mock
    private BehaviorPostProcessor postProcessor;

    @InjectMocks
    private BehaviorEventBus eventBus;

    @BeforeEach
    void setUp() {
        // 模拟handler列表
        List<BehaviorHandler> handlers = Collections.singletonList(followHandler);
        // 无法直接设置私有字段，使用反射
        try {
            var handlerField = BehaviorEventBus.class.getDeclaredField("handlerList");
            handlerField.setAccessible(true);
            handlerField.set(eventBus, handlers);

            var postProcessorField = BehaviorEventBus.class.getDeclaredField("postProcessorList");
            postProcessorField.setAccessible(true);
            postProcessorField.set(eventBus, Collections.singletonList(postProcessor));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(followHandler.getType()).thenReturn(BehaviorType.FOLLOW_USER);
        when(postProcessor.getOrder()).thenReturn(0);

        eventBus.init();
    }

    @Test
    void execute_ShouldSucceed_WhenHandlerFound() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.success(BehaviorType.FOLLOW_USER, "关注成功")
            .withNewRecord(true);

        when(followHandler.execute(any())).thenReturn(handlerResult);

        ResponseResult response = eventBus.execute(context);

        assertNotNull(response);
        assertEquals(200, response.getCode());
    }

    @Test
    void execute_ShouldReturnError_WhenHandlerNotFound() {
        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);

        ResponseResult response = eventBus.execute(context);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
        assertTrue(response.getMessage().contains("不支持的行为类型"));
    }

    @Test
    void execute_ShouldReturnError_WhenHandlerReturnsFailure() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.failure(BehaviorType.FOLLOW_USER, "不能关注自己");

        when(followHandler.execute(any())).thenReturn(handlerResult);

        ResponseResult response = eventBus.execute(context);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
    }

    @Test
    void execute_ShouldReturnError_WhenNullContext() {
        ResponseResult response = eventBus.execute(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), response.getCode());
    }

    @Test
    void execute_ShouldReturnError_WhenNullBehaviorType() {
        BehaviorContext context = new BehaviorContext(null, 100);

        ResponseResult response = eventBus.execute(context);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), response.getCode());
    }

    @Test
    void execute_ShouldTriggerPostProcessors_WhenNewRecord() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.success(BehaviorType.FOLLOW_USER, "关注成功")
            .withNewRecord(true);

        when(followHandler.execute(any())).thenReturn(handlerResult);

        eventBus.execute(context);

        verify(postProcessor).postProcess(any(), any());
    }

    @Test
    void execute_ShouldNotTriggerPostProcessors_WhenDuplicateRecord() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.duplicate(BehaviorType.FOLLOW_USER);

        when(followHandler.execute(any())).thenReturn(handlerResult);

        eventBus.execute(context);

        verify(postProcessor, never()).postProcess(any(), any());
    }

    @Test
    void execute_ShouldHandleException_FromHandler() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);

        when(followHandler.execute(any())).thenThrow(new RuntimeException("数据库异常"));

        ResponseResult response = eventBus.execute(context);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
        assertTrue(response.getMessage().contains("行为处理异常"));
    }

    @Test
    void rollback_ShouldSucceed_WhenHandlerFound() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.success(BehaviorType.UNFOLLOW_USER, "取消关注成功");

        when(followHandler.rollback(any())).thenReturn(handlerResult);

        ResponseResult response = eventBus.rollback(context);

        assertNotNull(response);
        assertEquals(200, response.getCode());
    }

    @Test
    void rollback_ShouldReturnError_WhenHandlerNotFound() {
        BehaviorContext context = new BehaviorContext(BehaviorType.LIKE_ARTICLE, 100);

        ResponseResult response = eventBus.rollback(context);

        assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), response.getCode());
    }

    @Test
    void rollback_ShouldReturnError_WhenNullContext() {
        ResponseResult response = eventBus.rollback(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), response.getCode());
    }

    @Test
    void postProcessor_ShouldHandleException_WithoutBreakingChain() {
        BehaviorContext context = new BehaviorContext(BehaviorType.FOLLOW_USER, 100);
        BehaviorResult handlerResult = BehaviorResult.success(BehaviorType.FOLLOW_USER, "关注成功")
            .withNewRecord(true);

        when(followHandler.execute(any())).thenReturn(handlerResult);
        doThrow(new RuntimeException("后置处理器异常")).when(postProcessor).postProcess(any(), any());

        // 不应抛出异常，应正常返回
        ResponseResult response = eventBus.execute(context);

        assertEquals(200, response.getCode());
        verify(postProcessor).postProcess(any(), any());
    }
}