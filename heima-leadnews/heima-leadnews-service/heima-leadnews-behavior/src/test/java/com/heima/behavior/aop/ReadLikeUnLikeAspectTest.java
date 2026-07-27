package com.heima.behavior.aop;

import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadLikeUnLikeAspect 单元测试")
class ReadLikeUnLikeAspectTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private ReadLikeUnLikeAspect aspect;

    // ==================== readAndLikeUnLike ====================

    @Nested
    @DisplayName("readAndLikeUnLike 方法测试")
    class ReadAndLikeUnLikeTests {

        @Test
        @DisplayName("点赞行为 - operation=0（点赞），发送Kafka消息add=1")
        void shouldSendLikeMessageWithAddOne() throws Throwable {
            LikesBehaviorDto dto = new LikesBehaviorDto();
            dto.setArticleId(1001L);
            dto.setOperation((short) 0);

            Object[] args = new Object[]{dto};
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.proceed()).thenReturn(ResponseResult.okResult());

            Object result = aspect.readAndLikeUnLike(joinPoint);

            assertNotNull(result);
            verify(kafkaTemplate).send(eq("hot.article.score.topic"), anyString());
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("点赞行为 - operation=1（取消点赞），发送Kafka消息add=-1")
        void shouldSendUnlikeMessageWithAddMinusOne() throws Throwable {
            LikesBehaviorDto dto = new LikesBehaviorDto();
            dto.setArticleId(2001L);
            dto.setOperation((short) 1);

            Object[] args = new Object[]{dto};
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.proceed()).thenReturn(ResponseResult.okResult());

            Object result = aspect.readAndLikeUnLike(joinPoint);

            assertNotNull(result);
            verify(kafkaTemplate).send(eq("hot.article.score.topic"), anyString());
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("阅读行为 - 发送Kafka消息add=1")
        void shouldSendReadMessageWithAddOne() throws Throwable {
            ReadBehaviorDto dto = new ReadBehaviorDto();
            dto.setArticleId(3001L);

            Object[] args = new Object[]{dto};
            when(joinPoint.getArgs()).thenReturn(args);
            when(joinPoint.proceed()).thenReturn(ResponseResult.okResult());

            Object result = aspect.readAndLikeUnLike(joinPoint);

            assertNotNull(result);
            verify(kafkaTemplate).send(eq("hot.article.score.topic"), anyString());
            verify(joinPoint).proceed();
        }

        @Test
        @DisplayName("参数为null - 返回PARAM_INVALID错误")
        void shouldReturnErrorWhenArgsAreNull() throws Throwable {
            Object[] args = new Object[]{null};
            when(joinPoint.getArgs()).thenReturn(args);

            Object result = aspect.readAndLikeUnLike(joinPoint);

            assertNotNull(result);
            assertTrue(result instanceof ResponseResult);
            ResponseResult responseResult = (ResponseResult) result;
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), responseResult.getCode());
            verify(joinPoint, never()).proceed();
        }

        @Test
        @DisplayName("参数数组为空 - 返回PARAM_INVALID错误")
        void shouldReturnErrorWhenArgsEmpty() throws Throwable {
            Object[] args = new Object[]{};
            when(joinPoint.getArgs()).thenReturn(args);

            Object result = aspect.readAndLikeUnLike(joinPoint);

            assertNotNull(result);
            assertTrue(result instanceof ResponseResult);
            verify(joinPoint, never()).proceed();
        }
    }
}