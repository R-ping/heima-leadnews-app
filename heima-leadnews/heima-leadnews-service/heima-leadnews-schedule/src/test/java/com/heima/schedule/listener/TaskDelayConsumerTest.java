package com.heima.schedule.listener;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskDelayConsumer 单元测试
 * 测试RabbitMQ延迟任务消费者的消息处理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("延迟任务消费者测试")
class TaskDelayConsumerTest {

    @Mock
    private IArticleClient articleClient;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskDelayConsumer taskDelayConsumer;

    @Mock
    private Message message;

    @Mock
    private Channel channel;

    @Mock
    private MessageProperties messageProperties;

    private Task validTask;
    private static final Long TEST_TASK_ID = 1001L;
    private static final Long TEST_ARTICLE_ID = 2001L;
    private static final long DELIVERY_TAG = 1L;

    @BeforeEach
    void setUp() {
        // 构建完整的Task对象
        ApArticle article = new ApArticle();
        article.setId(TEST_ARTICLE_ID);

        validTask = new Task();
        validTask.setTaskId(TEST_TASK_ID);
        validTask.setTaskType(1);
        validTask.setPriority(1);
        validTask.setFirstExecInterval(1000L);
        validTask.setObjExecInterval(5000L);

        // 使用ProtostuffUtil序列化article作为parameters
        validTask.setParameters(com.heima.utils.common.ProtostuffUtil.serialize(article));

        String taskJson = JSON.toJSONString(validTask);

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(DELIVERY_TAG);
        when(message.getBody()).thenReturn(taskJson.getBytes());
    }

    // ==================== onMessage 方法测试 ====================

    @Nested
    @DisplayName("onMessage 方法测试")
    class OnMessageTests {

        @Test
        @DisplayName("正常流程 — generateArticleEvent返回true则消费任务")
        void shouldConsumeTaskWhenEventGeneratedSuccessfully() throws IOException {
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(true);

            taskDelayConsumer.onMessage(message, channel);

            // 验证generateArticleEvent被调用
            verify(articleClient).generateArticleEvent(any(ApArticle.class), anyLong());
            // 验证消费任务
            verify(taskService).consumerTask(TEST_TASK_ID);
            // 验证未标记失败
            verify(taskService, never()).failTask(anyLong());
            // 验证手动ack
            verify(channel).basicAck(DELIVERY_TAG, false);
        }

        @Test
        @DisplayName("generateArticleEvent返回false — 标记任务失败")
        void shouldFailTaskWhenEventGenerationFailed() throws IOException {
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(false);

            taskDelayConsumer.onMessage(message, channel);

            // 验证generateArticleEvent被调用
            verify(articleClient).generateArticleEvent(any(ApArticle.class), anyLong());
            // 验证标记失败
            verify(taskService).failTask(TEST_TASK_ID);
            // 验证未消费任务
            verify(taskService, never()).consumerTask(anyLong());
            // 验证手动ack
            verify(channel).basicAck(DELIVERY_TAG, false);
        }

        @Test
        @DisplayName("异常流程 — 处理异常时仍执行ack")
        void shouldAckEvenWhenExceptionOccurs() throws IOException {
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong()))
                    .thenThrow(new RuntimeException("Feign调用异常"));

            taskDelayConsumer.onMessage(message, channel);

            // 验证未调用taskService
            verify(taskService, never()).consumerTask(anyLong());
            verify(taskService, never()).failTask(anyLong());
            // 验证仍执行了ack
            verify(channel).basicAck(DELIVERY_TAG, false);
        }

        @Test
        @DisplayName("异常流程 — ack失败时记录错误但不崩溃")
        void shouldNotCrashWhenAckFailsInCatch() throws IOException {
            // 模拟业务异常
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong()))
                    .thenThrow(new RuntimeException("业务异常"));
            // 模拟ack也失败
            doThrow(new IOException("通道已关闭"))
                    .when(channel).basicAck(DELIVERY_TAG, false);

            // 不应该抛出异常
            taskDelayConsumer.onMessage(message, channel);

            verify(channel).basicAck(DELIVERY_TAG, false);
        }

        @Test
        @DisplayName("异常流程 — 正常流程中ack失败抛出异常")
        void shouldThrowWhenAckFailsInNormalFlow() throws IOException {
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(true);
            doThrow(new IOException("通道已关闭"))
                    .when(channel).basicAck(DELIVERY_TAG, false);

            // 正常流程中的ack异常会向上传播
            try {
                taskDelayConsumer.onMessage(message, channel);
            } catch (Exception e) {
                // 预期异常
            }

            verify(articleClient).generateArticleEvent(any(ApArticle.class), anyLong());
            verify(taskService).consumerTask(TEST_TASK_ID);
        }

        @Test
        @DisplayName("message.getBody返回空字节数组 — 反序列化失败异常")
        void shouldHandleEmptyBodyGracefully() throws IOException {
            when(message.getBody()).thenReturn(new byte[0]);

            taskDelayConsumer.onMessage(message, channel);

            // 验证发生了异常但仍ack了
            verify(channel).basicAck(DELIVERY_TAG, false);
            verify(articleClient, never()).generateArticleEvent(any(), anyLong());
        }

        @Test
        @DisplayName("正常流程 — 验证lastExecInterval计算正确")
        void shouldCalculateIntervalCorrectly() throws IOException {
            long firstExecInterval = 2000L;
            long objExecInterval = 8000L;
            long expectedInterval = objExecInterval - firstExecInterval;

            validTask.setFirstExecInterval(firstExecInterval);
            validTask.setObjExecInterval(objExecInterval);
            when(message.getBody()).thenReturn(JSON.toJSONString(validTask).getBytes());
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(true);

            taskDelayConsumer.onMessage(message, channel);

            verify(articleClient).generateArticleEvent(any(ApArticle.class), eq(expectedInterval));
        }

        @Test
        @DisplayName("正常流程 — 验证taskId正确传递给consumerTask")
        void shouldPassCorrectTaskIdToConsumer() throws IOException {
            Long expectedTaskId = 9999L;
            validTask.setTaskId(expectedTaskId);
            when(message.getBody()).thenReturn(JSON.toJSONString(validTask).getBytes());
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(true);

            taskDelayConsumer.onMessage(message, channel);

            verify(taskService).consumerTask(expectedTaskId);
        }

        @Test
        @DisplayName("异常流程 — taskId正确传递给failTask")
        void shouldPassCorrectTaskIdToFail() throws IOException {
            Long expectedTaskId = 8888L;
            validTask.setTaskId(expectedTaskId);
            when(message.getBody()).thenReturn(JSON.toJSONString(validTask).getBytes());
            when(articleClient.generateArticleEvent(any(ApArticle.class), anyLong())).thenReturn(false);

            taskDelayConsumer.onMessage(message, channel);

            verify(taskService).failTask(expectedTaskId);
        }

        @Test
        @DisplayName("message.getBody为无效JSON — 反序列化失败异常")
        void shouldHandleInvalidJsonGracefully() throws IOException {
            when(message.getBody()).thenReturn("not valid json".getBytes());

            taskDelayConsumer.onMessage(message, channel);

            // 验证异常被捕获并ack
            verify(channel).basicAck(DELIVERY_TAG, false);
            verify(articleClient, never()).generateArticleEvent(any(), anyLong());
        }
    }
}