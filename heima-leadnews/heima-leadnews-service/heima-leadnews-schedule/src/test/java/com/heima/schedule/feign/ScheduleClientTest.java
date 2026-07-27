package com.heima.schedule.feign;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ScheduleClient 单元测试
 * 测试调度客户端Feign接口的实现
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("调度客户端测试")
class ScheduleClientTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ScheduleClient scheduleClient;

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

    // ==================== addTask 方法测试 ====================

    @Nested
    @DisplayName("addTask 方法测试")
    class AddTaskTests {

        @Test
        @DisplayName("正常添加任务 — 调用taskService.addTask")
        void shouldDelegateToTaskService() {
            scheduleClient.addTask(validTask);

            verify(taskService).addTask(validTask);
        }

        @Test
        @DisplayName("addTask — 验证返回值void")
        void shouldReturnVoid() {
            assertDoesNotThrow(() -> scheduleClient.addTask(validTask));
            verify(taskService).addTask(validTask);
        }

        @Test
        @DisplayName("addTask — taskService.addTask抛出异常时向上传播")
        void shouldPropagateExceptionFromTaskService() {
            doThrow(new RuntimeException("DB写入失败"))
                    .when(taskService).addTask(any(Task.class));

            assertThrows(RuntimeException.class, () -> scheduleClient.addTask(validTask));
            verify(taskService).addTask(validTask);
        }

        @Test
        @DisplayName("addTask — Task为null时传递给service")
        void shouldPassNullTaskToService() {
            scheduleClient.addTask(null);

            verify(taskService).addTask(null);
        }

        @Test
        @DisplayName("addTask — 验证taskService仅被调用一次")
        void shouldCallTaskServiceOnlyOnce() {
            scheduleClient.addTask(validTask);

            verify(taskService, times(1)).addTask(validTask);
        }
    }

    // ==================== addTaskDelayMsg 方法测试 ====================

    @Nested
    @DisplayName("addTaskDelayMsg 方法测试")
    class AddTaskDelayMsgTests {

        @Test
        @DisplayName("addTaskDelayMsg — 当前实现返回null")
        void shouldReturnNull() {
            Task task = new Task();
            task.setTaskType(1);

            Object result = scheduleClient.addTaskDelayMsg(task);

            assertNull(result);
        }

        @Test
        @DisplayName("addTaskDelayMsg — 传入null也返回null")
        void shouldReturnNullForNullInput() {
            Object result = scheduleClient.addTaskDelayMsg(null);

            assertNull(result);
        }

        @Test
        @DisplayName("addTaskDelayMsg — 不调用taskService")
        void shouldNotCallTaskService() {
            scheduleClient.addTaskDelayMsg(validTask);

            verifyNoInteractions(taskService);
        }
    }
}