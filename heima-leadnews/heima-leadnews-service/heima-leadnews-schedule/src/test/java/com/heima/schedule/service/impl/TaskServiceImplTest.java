package com.heima.schedule.service.impl;

import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl 单元测试")
class TaskServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CacheService cacheService;

    @Mock
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final Long TEST_TASK_ID = 1001L;

    // ==================== addTask 测试 ====================

    @Nested
    @DisplayName("addTask 方法测试")
    class AddTaskTests {

        private Task validTask;

        @BeforeEach
        void setUp() {
            validTask = new Task();
            validTask.setTaskType(1);
            validTask.setPriority(1);
            validTask.setExecuteTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
            validTask.setFirstExecInterval(1000L);
            validTask.setObjExecInterval(5000L);
            validTask.setParameters(new byte[]{1, 2, 3});
        }

        @Test
        @DisplayName("正常添加任务 - DB插入成功并发送延迟消息")
        void shouldAddTaskSuccessfully() {
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            assertEquals(TEST_TASK_ID, validTask.getTaskId());
            verify(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));
            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("DB插入抛出异常 - 不发送延迟消息")
        void shouldNotSendDelayMsgWhenDbInsertThrowsException() {
            when(taskinfoLogsMapper.insert(any(TaskinfoLogs.class)))
                    .thenThrow(new RuntimeException("DB error"));

            taskService.addTask(validTask);

            assertNull(validTask.getTaskId());
            verify(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("DB插入返回0 - 不发送延迟消息")
        void shouldNotSendDelayMsgWhenDbInsertReturnsZero() {
            when(taskinfoLogsMapper.insert(any(TaskinfoLogs.class))).thenReturn(0);

            taskService.addTask(validTask);

            assertNull(validTask.getTaskId());
            verify(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("executeTime在过去 - delay应为0")
        void shouldSetDelayToZeroWhenExecuteTimeInPast() {
            validTask.setExecuteTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("executeTime在一小时内 - delay应为firstExecInterval")
        void shouldSetDelayToFirstExecIntervalWhenWithinOneHour() {
            validTask.setExecuteTime(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
            validTask.setFirstExecInterval(5000L);
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("executeTime超过一小时 - delay应为0")
        void shouldSetDelayToZeroWhenBeyondOneHour() {
            validTask.setExecuteTime(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000));
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("executeTime等于当前时间 - delay应为0")
        void shouldSetDelayToZeroWhenExecuteTimeEqualsNow() {
            // 设置 executeTime 为当前时间之前（确保 <= nowTime）
            validTask.setExecuteTime(new Date(System.currentTimeMillis()));
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }

        @Test
        @DisplayName("executeTime刚好一小时后 - delay应为firstExecInterval")
        void shouldSetDelayToFirstExecIntervalWhenExactlyOneHour() {
            validTask.setExecuteTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
            validTask.setFirstExecInterval(3000L);
            doAnswer(invocation -> {
                TaskinfoLogs logs = invocation.getArgument(0);
                logs.setTaskId(TEST_TASK_ID);
                return 1;
            }).when(taskinfoLogsMapper).insert(any(TaskinfoLogs.class));

            taskService.addTask(validTask);

            verify(rabbitTemplate).convertAndSend(
                    eq("delay.exchange"), eq("task.delay"), anyString(), any(MessagePostProcessor.class));
        }
    }

    // ==================== consumerTask 测试 ====================

    @Nested
    @DisplayName("consumerTask 方法测试")
    class ConsumerTaskTests {

        @Test
        @DisplayName("正常消费任务 - 更新状态为EXECUTED")
        void shouldUpdateStatusToExecuted() {
            TaskinfoLogs logs = new TaskinfoLogs();
            logs.setTaskId(TEST_TASK_ID);
            logs.setStatus(ScheduleConstants.SCHEDULED);
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(logs);
            when(taskinfoLogsMapper.updateById(any(TaskinfoLogs.class))).thenReturn(1);

            taskService.consumerTask(TEST_TASK_ID);

            ArgumentCaptor<TaskinfoLogs> captor = ArgumentCaptor.forClass(TaskinfoLogs.class);
            verify(taskinfoLogsMapper).updateById(captor.capture());
            assertEquals(ScheduleConstants.EXECUTED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("taskId对应的记录不存在 - selectById返回null导致NPE被包装为RuntimeException")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(null);

            assertThrows(RuntimeException.class, () -> taskService.consumerTask(TEST_TASK_ID));
        }

        @Test
        @DisplayName("DB更新异常 - 应抛出RuntimeException")
        void shouldThrowRuntimeExceptionWhenDbUpdateFails() {
            TaskinfoLogs logs = new TaskinfoLogs();
            logs.setTaskId(TEST_TASK_ID);
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(logs);
            when(taskinfoLogsMapper.updateById(any(TaskinfoLogs.class)))
                    .thenThrow(new RuntimeException("DB update error"));

            assertThrows(RuntimeException.class, () -> taskService.consumerTask(TEST_TASK_ID));
        }

        @Test
        @DisplayName("taskId为null - 应抛出异常")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // selectById(null) 的行为由 MyBatis-Plus 决定，mock 让其返回 null
            when(taskinfoLogsMapper.selectById(null)).thenReturn(null);

            assertThrows(RuntimeException.class, () -> taskService.consumerTask(null));
        }
    }

    // ==================== failTask 测试 ====================

    @Nested
    @DisplayName("failTask 方法测试")
    class FailTaskTests {

        @Test
        @DisplayName("正常标记失败 - 更新状态为FAIL")
        void shouldUpdateStatusToFail() {
            TaskinfoLogs logs = new TaskinfoLogs();
            logs.setTaskId(TEST_TASK_ID);
            logs.setStatus(ScheduleConstants.EXECUTED);
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(logs);
            when(taskinfoLogsMapper.updateById(any(TaskinfoLogs.class))).thenReturn(1);

            taskService.failTask(TEST_TASK_ID);

            ArgumentCaptor<TaskinfoLogs> captor = ArgumentCaptor.forClass(TaskinfoLogs.class);
            verify(taskinfoLogsMapper).updateById(captor.capture());
            assertEquals(ScheduleConstants.FAIL, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("taskId对应的记录不存在 - 应抛出RuntimeException")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(null);

            assertThrows(RuntimeException.class, () -> taskService.failTask(TEST_TASK_ID));
        }

        @Test
        @DisplayName("DB更新异常 - 应抛出RuntimeException")
        void shouldThrowRuntimeExceptionWhenDbUpdateFails() {
            TaskinfoLogs logs = new TaskinfoLogs();
            logs.setTaskId(TEST_TASK_ID);
            when(taskinfoLogsMapper.selectById(TEST_TASK_ID)).thenReturn(logs);
            when(taskinfoLogsMapper.updateById(any(TaskinfoLogs.class)))
                    .thenThrow(new RuntimeException("DB update error"));

            assertThrows(RuntimeException.class, () -> taskService.failTask(TEST_TASK_ID));
        }

        @Test
        @DisplayName("taskId为null - 应抛出异常")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            when(taskinfoLogsMapper.selectById(null)).thenReturn(null);

            assertThrows(RuntimeException.class, () -> taskService.failTask(null));
        }
    }
}