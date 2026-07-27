package com.heima.article.controller.v1;

import com.heima.article.service.CheckInService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckInController 单元测试")
class CheckInControllerTest {

    @Mock
    private CheckInService checkInService;

    @InjectMocks
    private CheckInController checkInController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1001);
        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (threadLocalMock != null) {
            threadLocalMock.close();
        }
    }

    @Nested
    @DisplayName("doCheckIn() - 执行签到")
    class DoCheckInTests {

        @Test
        @DisplayName("正常签到，返回成功结果")
        void shouldDoCheckInSuccessfully() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("checkedIn", true);
            when(checkInService.doCheckIn(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.doCheckIn();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).doCheckIn(1001L);
        }

        @Test
        @DisplayName("签到返回连续签到天数")
        void shouldReturnStreakDays() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("checkedIn", true);
            serviceResult.put("streak", 7);
            when(checkInService.doCheckIn(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.doCheckIn();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).doCheckIn(1001L);
        }
    }

    @Nested
    @DisplayName("getDashboard() - 获取签到面板")
    class GetDashboardTests {

        @Test
        @DisplayName("正常获取签到面板，返回成功结果")
        void shouldReturnDashboard() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("totalDays", 30);
            when(checkInService.getDashboard(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.getDashboard();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getDashboard(1001L);
        }
    }

    @Nested
    @DisplayName("doRetroactive() - 补签")
    class DoRetroactiveTests {

        @Test
        @DisplayName("正常补签，返回成功结果")
        void shouldDoRetroactiveSuccessfully() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("retroactive", true);
            when(checkInService.doRetroactive(eq(1001L), eq("2026-07-25"))).thenReturn(serviceResult);

            ResponseResult result = checkInController.doRetroactive("2026-07-25");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).doRetroactive(1001L, "2026-07-25");
        }

        @Test
        @DisplayName("补签日期格式错误时，Service抛出异常向上传播")
        void shouldPropagateErrorWhenInvalidDate() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            when(checkInService.doRetroactive(eq(1001L), eq("invalid")))
                    .thenThrow(new RuntimeException("日期格式无效"));

            assertThrows(RuntimeException.class, () -> checkInController.doRetroactive("invalid"));
        }
    }

    @Nested
    @DisplayName("getCheckInRecords() - 获取签到记录")
    class GetCheckInRecordsTests {

        @Test
        @DisplayName("正常获取指定年月的签到记录")
        void shouldReturnRecordsForSpecificMonth() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("records", "[]");
            when(checkInService.getCheckInRecords(eq(1001L), eq(2026), eq(7))).thenReturn(serviceResult);

            ResponseResult result = checkInController.getCheckInRecords(2026, 7);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getCheckInRecords(1001L, 2026, 7);
        }

        @Test
        @DisplayName("year和month为null时，使用当前年月")
        void shouldUseCurrentYearMonthWhenNull() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("records", "[]");
            when(checkInService.getCheckInRecords(anyLong(), anyInt(), anyInt())).thenReturn(serviceResult);

            ResponseResult result = checkInController.getCheckInRecords(null, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getCheckInRecords(anyLong(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("getCheckInStats() - 获取签到统计")
    class GetCheckInStatsTests {

        @Test
        @DisplayName("正常获取签到统计，返回成功结果")
        void shouldReturnCheckInStats() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("totalCheckIns", 100);
            when(checkInService.getCheckInStats(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.getCheckInStats();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getCheckInStats(1001L);
        }
    }

    @Nested
    @DisplayName("getCheckInTasks() - 获取签到任务")
    class GetCheckInTasksTests {

        @Test
        @DisplayName("正常获取签到任务，返回成功结果")
        void shouldReturnCheckInTasks() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("tasks", "[]");
            when(checkInService.getCheckInTasks(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.getCheckInTasks();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getCheckInTasks(1001L);
        }
    }

    @Nested
    @DisplayName("getTodayStatus() - 获取今日签到状态")
    class GetTodayStatusTests {

        @Test
        @DisplayName("正常获取今日签到状态，返回成功结果")
        void shouldReturnTodayStatus() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("checkedIn", false);
            when(checkInService.getTodayStatus(eq(1001L))).thenReturn(serviceResult);

            ResponseResult result = checkInController.getTodayStatus();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getTodayStatus(1001L);
        }

        @Test
        @DisplayName("用户未登录时，传递null给service")
        void shouldPassNullWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
            Map<String, Object> serviceResult = new HashMap<>();
            serviceResult.put("checkedIn", false);
            when(checkInService.getTodayStatus(null)).thenReturn(serviceResult);

            ResponseResult result = checkInController.getTodayStatus();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(checkInService).getTodayStatus(null);
        }
    }
}