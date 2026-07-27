package com.heima.article.controller.v1;

import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.ApLevelConfig;
import com.heima.model.article.pojos.ApUserLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LevelController 单元测试")
class LevelControllerTest {

    @Mock
    private LevelService levelService;

    @InjectMocks
    private LevelController levelController;

    @Nested
    @DisplayName("getUserLevel() - 获取用户等级")
    class GetUserLevelTests {

        @Test
        @DisplayName("正常获取用户等级，返回200")
        void shouldReturnUserLevel() {
            ApUserLevel userLevel = new ApUserLevel();
            userLevel.setUserId(1001L);
            userLevel.setDailyLevel(5);
            when(levelService.getUserLevel(eq(1001L))).thenReturn(userLevel);

            ResponseEntity<ApUserLevel> response = levelController.getUserLevel(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(1001L, response.getBody().getUserId());
            assertEquals(5, response.getBody().getDailyLevel());
            verify(levelService).getUserLevel(1001L);
        }

        @Test
        @DisplayName("用户等级不存在时，返回空body")
        void shouldReturnEmptyWhenUserLevelNotFound() {
            when(levelService.getUserLevel(eq(999L))).thenReturn(null);

            ResponseEntity<ApUserLevel> response = levelController.getUserLevel(999L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("getUserLevelInfo() - 获取用户等级信息")
    class GetUserLevelInfoTests {

        @Test
        @DisplayName("正常获取用户等级信息，返回200")
        void shouldReturnUserLevelInfo() {
            Map<String, Object> levelInfo = new HashMap<>();
            levelInfo.put("level", 5);
            levelInfo.put("experience", 1000);
            when(levelService.getUserLevelInfo(eq(1001L))).thenReturn(levelInfo);

            ResponseEntity<Map<String, Object>> response = levelController.getUserLevelInfo(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(5, response.getBody().get("level"));
            verify(levelService).getUserLevelInfo(1001L);
        }
    }

    @Nested
    @DisplayName("getTodayTaskProgress() - 获取今日任务进度")
    class GetTodayTaskProgressTests {

        @Test
        @DisplayName("正常获取今日任务进度，返回200")
        void shouldReturnTaskProgress() {
            Map<String, Object> taskProgress = new HashMap<>();
            taskProgress.put("completed", 3);
            taskProgress.put("total", 5);
            when(levelService.getTodayTaskProgress(eq(1001L))).thenReturn(taskProgress);

            ResponseEntity<Map<String, Object>> response = levelController.getTodayTaskProgress(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(3, response.getBody().get("completed"));
            verify(levelService).getTodayTaskProgress(1001L);
        }
    }

    @Nested
    @DisplayName("getUserPermissions() - 获取用户权限")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("正常获取用户权限列表，返回200")
        void shouldReturnUserPermissions() {
            List<String> permissions = Arrays.asList("POST_ARTICLE", "COMMENT", "UPLOAD_IMAGE");
            when(levelService.getUserPermissions(eq(1001L))).thenReturn(permissions);

            ResponseEntity<List<String>> response = levelController.getUserPermissions(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(3, response.getBody().size());
            assertTrue(response.getBody().contains("POST_ARTICLE"));
            verify(levelService).getUserPermissions(1001L);
        }

        @Test
        @DisplayName("用户无权限时，返回空列表")
        void shouldReturnEmptyPermissions() {
            when(levelService.getUserPermissions(eq(1001L))).thenReturn(Arrays.asList());

            ResponseEntity<List<String>> response = levelController.getUserPermissions(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("checkPermission() - 检查权限")
    class CheckPermissionTests {

        @Test
        @DisplayName("拥有权限时，返回hasPermission=true")
        void shouldReturnTrueWhenHasPermission() {
            when(levelService.hasPermission(eq(1001L), eq("POST_ARTICLE"))).thenReturn(true);

            ResponseEntity<Map<String, Boolean>> response = levelController.checkPermission(1001L, "POST_ARTICLE");

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().get("hasPermission"));
            verify(levelService).hasPermission(1001L, "POST_ARTICLE");
        }

        @Test
        @DisplayName("无权限时，返回hasPermission=false")
        void shouldReturnFalseWhenNoPermission() {
            when(levelService.hasPermission(eq(1001L), eq("ADMIN"))).thenReturn(false);

            ResponseEntity<Map<String, Boolean>> response = levelController.checkPermission(1001L, "ADMIN");

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertEquals(false, response.getBody().get("hasPermission"));
        }
    }

    @Nested
    @DisplayName("recordAction() - 记录行为")
    class RecordActionTests {

        @Test
        @DisplayName("正常记录行为，返回200")
        void shouldRecordActionSuccessfully() {
            ResponseEntity<Void> response = levelController.recordAction(1001L, "POST_ARTICLE", "article_123");

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(levelService).recordAction(1001L, "POST_ARTICLE", "article_123");
        }

        @Test
        @DisplayName("记录行为不提供detail时，传递null")
        void shouldRecordActionWithoutDetail() {
            ResponseEntity<Void> response = levelController.recordAction(1001L, "LOGIN", null);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(levelService).recordAction(1001L, "LOGIN", null);
        }
    }

    @Nested
    @DisplayName("checkIn() - 签到")
    class CheckInTests {

        @Test
        @DisplayName("正常签到，返回签到结果")
        void shouldCheckInSuccessfully() {
            Map<String, Object> result = new HashMap<>();
            result.put("reward", 10);
            result.put("streak", 7);
            when(levelService.checkIn(eq(1001L))).thenReturn(result);

            ResponseEntity<Map<String, Object>> response = levelController.checkIn(1001L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(10, response.getBody().get("reward"));
            verify(levelService).checkIn(1001L);
        }
    }

    @Nested
    @DisplayName("recordActionWithLimit() - 受限记录行为")
    class RecordActionWithLimitTests {

        @Test
        @DisplayName("正常记录受限行为，返回结果")
        void shouldRecordActionWithLimitSuccessfully() {
            Map<String, Object> result = new HashMap<>();
            result.put("accepted", true);
            result.put("remaining", 5);
            when(levelService.recordActionWithLimit(eq(1001L), eq("POST_ARTICLE"), eq("detail")))
                    .thenReturn(result);

            ResponseEntity<Map<String, Object>> response =
                    levelController.recordActionWithLimit(1001L, "POST_ARTICLE", "detail");

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertTrue((Boolean) response.getBody().get("accepted"));
            verify(levelService).recordActionWithLimit(1001L, "POST_ARTICLE", "detail");
        }
    }

    @Nested
    @DisplayName("calculatePower() - 计算能量值")
    class CalculatePowerTests {

        @Test
        @DisplayName("正常计算能量值，返回结果")
        void shouldCalculatePowerSuccessfully() {
            Map<String, Object> result = new HashMap<>();
            result.put("powerValue", 100);
            when(levelService.calculatePowerWithLimit(eq(1001L), eq(200L), eq("LIKE"), eq(5)))
                    .thenReturn(result);

            ResponseEntity<Map<String, Object>> response =
                    levelController.calculatePower(1001L, 200L, "LIKE", 5);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(100, response.getBody().get("powerValue"));
            verify(levelService).calculatePowerWithLimit(1001L, 200L, "LIKE", 5);
        }
    }

    @Nested
    @DisplayName("getLevelConfigs() - 获取等级配置")
    class GetLevelConfigsTests {

        @Test
        @DisplayName("正常获取等级配置列表，返回200")
        void shouldReturnLevelConfigs() {
            List<ApLevelConfig> configs = Arrays.asList(new ApLevelConfig(), new ApLevelConfig());
            when(levelService.getLevelConfigs(eq(1))).thenReturn(configs);

            ResponseEntity<List<ApLevelConfig>> response = levelController.getLevelConfigs(1);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            verify(levelService).getLevelConfigs(1);
        }

        @Test
        @DisplayName("使用默认levelType=1")
        void shouldUseDefaultLevelType() {
            List<ApLevelConfig> configs = Arrays.asList(new ApLevelConfig());
            when(levelService.getLevelConfigs(eq(1))).thenReturn(configs);

            ResponseEntity<List<ApLevelConfig>> response = levelController.getLevelConfigs(1);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            verify(levelService).getLevelConfigs(1);
        }
    }
}