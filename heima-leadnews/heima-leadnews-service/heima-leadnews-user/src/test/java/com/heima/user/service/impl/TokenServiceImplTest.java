package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.user.dtos.LoginResultVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenServiceImpl 单元测试")
class TokenServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        reset(redisTemplate, valueOperations);
    }

    // ==================== generateDualToken ====================

    @Nested
    @DisplayName("generateDualToken 方法测试")
    class GenerateDualTokenTests {

        @Test
        @DisplayName("正常生成双Token - 所有参数完整")
        void shouldGenerateDualTokenWithAllParams() {
            Integer userId = 1;
            String nickName = "测试用户";
            String phone = "13800138000";
            String image = "avatar_1";

            LoginResultVo result = tokenService.generateDualToken(userId, nickName, phone, image);

            assertNotNull(result);
            assertEquals("login", result.getStatus());
            assertEquals(userId, result.getUserId());
            assertEquals(nickName, result.getNickName());
            assertEquals(phone, result.getPhone());
            assertEquals(image, result.getAvatar());
            assertNotNull(result.getAccessToken());
            assertNotNull(result.getRefreshToken());
            assertFalse(result.getRefreshToken().contains("-"), "refreshToken 不应包含横线");

            // 验证 Redis 存储
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), eq(7L), eq(TimeUnit.DAYS));
            assertTrue(keyCaptor.getValue().startsWith("refresh_token:"));
            assertTrue(keyCaptor.getValue().contains(result.getRefreshToken()));

            @SuppressWarnings("unchecked")
            Map<String, String> storedInfo = JSON.parseObject(valueCaptor.getValue(), Map.class);
            assertEquals(String.valueOf(userId), storedInfo.get("userId"));
            assertEquals(nickName, storedInfo.get("nickName"));
            assertEquals(phone, storedInfo.get("phone"));
            assertEquals(image, storedInfo.get("image"));
        }

        @Test
        @DisplayName("生成双Token - phone为null")
        void shouldGenerateDualTokenWithNullPhone() {
            Integer userId = 2;
            String nickName = "用户二";
            String image = "avatar_2";

            LoginResultVo result = tokenService.generateDualToken(userId, nickName, null, image);

            assertNotNull(result);
            assertEquals("login", result.getStatus());
            assertNull(result.getPhone());
            assertNotNull(result.getAccessToken());
            assertNotNull(result.getRefreshToken());
        }

        @Test
        @DisplayName("生成双Token - nickName为null")
        void shouldGenerateDualTokenWithNullNickName() {
            Integer userId = 3;
            String phone = "13900139000";
            String image = "avatar_3";

            LoginResultVo result = tokenService.generateDualToken(userId, null, phone, image);

            assertNotNull(result);
            assertEquals("login", result.getStatus());
            assertNull(result.getNickName());
            assertNotNull(result.getAccessToken());
            assertNotNull(result.getRefreshToken());
        }

        @Test
        @DisplayName("生成双Token - image为null")
        void shouldGenerateDualTokenWithNullImage() {
            Integer userId = 4;
            String nickName = "用户四";
            String phone = "13700137000";

            LoginResultVo result = tokenService.generateDualToken(userId, nickName, phone, null);

            assertNotNull(result);
            assertEquals("login", result.getStatus());
            assertNull(result.getAvatar());
            assertNotNull(result.getAccessToken());
            assertNotNull(result.getRefreshToken());
        }

        @Test
        @DisplayName("生成双Token - 验证每次生成的refreshToken都不同")
        void shouldGenerateUniqueRefreshTokens() {
            LoginResultVo result1 = tokenService.generateDualToken(1, "用户", "13800138000", "img");
            LoginResultVo result2 = tokenService.generateDualToken(1, "用户", "13800138000", "img");

            assertNotEquals(result1.getRefreshToken(), result2.getRefreshToken());
            assertNotEquals(result1.getAccessToken(), result2.getAccessToken());
        }
    }

    // ==================== refreshToken ====================

    @Nested
    @DisplayName("refreshToken 方法测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("正常刷新Token - refresh_token有效")
        void shouldRefreshTokenSuccessfully() {
            String refreshToken = "abc123def456";
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", "1");
            userInfo.put("nickName", "测试用户");
            userInfo.put("phone", "13800138000");
            userInfo.put("image", "avatar_1");

            when(valueOperations.get("refresh_token:" + refreshToken))
                    .thenReturn(JSON.toJSONString(userInfo));

            LoginResultVo result = tokenService.refreshToken(refreshToken);

            assertNotNull(result);
            assertEquals("login", result.getStatus());
            assertEquals(1, result.getUserId());
            assertEquals("测试用户", result.getNickName());
            // 验证旧token被删除
            verify(redisTemplate).delete("refresh_token:" + refreshToken);
            // 验证新token被存储
            verify(valueOperations, times(1)).set(
                    startsWith("refresh_token:"), anyString(), eq(7L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("刷新Token - refresh_token为null")
        void shouldReturnNullWhenRefreshTokenIsNull() {
            LoginResultVo result = tokenService.refreshToken(null);

            assertNull(result);
            verify(valueOperations, never()).get(anyString());
        }

        @Test
        @DisplayName("刷新Token - refresh_token为空字符串")
        void shouldReturnNullWhenRefreshTokenIsBlank() {
            LoginResultVo result = tokenService.refreshToken("   ");

            assertNull(result);
            verify(valueOperations, never()).get(anyString());
        }

        @Test
        @DisplayName("刷新Token - refresh_token在Redis中不存在")
        void shouldReturnNullWhenRefreshTokenNotFound() {
            String refreshToken = "nonexistent";
            when(valueOperations.get("refresh_token:" + refreshToken)).thenReturn(null);

            LoginResultVo result = tokenService.refreshToken(refreshToken);

            assertNull(result);
            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("刷新Token - 新token与旧token不同")
        void shouldGenerateNewTokensDifferentFromOld() {
            String oldRefreshToken = "old-refresh-token";
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", "1");
            userInfo.put("nickName", "用户");
            userInfo.put("phone", "13800138000");
            userInfo.put("image", "img");

            when(valueOperations.get("refresh_token:" + oldRefreshToken))
                    .thenReturn(JSON.toJSONString(userInfo));

            LoginResultVo result = tokenService.refreshToken(oldRefreshToken);

            assertNotNull(result);
            assertNotEquals(oldRefreshToken, result.getRefreshToken());
        }
    }

    // ==================== revokeRefreshToken ====================

    @Nested
    @DisplayName("revokeRefreshToken 方法测试")
    class RevokeRefreshTokenTests {

        @Test
        @DisplayName("正常吊销refresh_token")
        void shouldRevokeRefreshTokenSuccessfully() {
            String refreshToken = "token-to-revoke";

            tokenService.revokeRefreshToken(refreshToken);

            verify(redisTemplate).delete("refresh_token:" + refreshToken);
        }

        @Test
        @DisplayName("吊销 - refresh_token为null")
        void shouldNotDeleteWhenRefreshTokenIsNull() {
            tokenService.revokeRefreshToken(null);

            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("吊销 - refresh_token为空字符串")
        void shouldNotDeleteWhenRefreshTokenIsBlank() {
            tokenService.revokeRefreshToken("");

            verify(redisTemplate, never()).delete(anyString());
        }
    }
}