package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginResultVo;
import com.heima.model.user.dtos.SocialAuthDto;
import com.heima.model.user.dtos.SocialBindDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserSocial;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserSocialMapper;
import com.heima.user.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialLoginServiceImpl 单元测试")
class SocialLoginServiceImplTest {

    @Mock
    private ApUserSocialMapper apUserSocialMapper;
    @Mock
    private ApUserMapper apUserMapper;
    @Mock
    private TokenService tokenService;
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private SocialLoginServiceImpl socialLoginService;

    // ==================== socialAuth ====================

    @Nested
    @DisplayName("socialAuth 方法测试")
    class SocialAuthTests {

        @Test
        @DisplayName("社交认证 - platform为空字符串")
        void shouldReturnErrorWhenPlatformIsBlank() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("");
            dto.setPlatformUid("12345");

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交认证 - platformUid为空字符串")
        void shouldReturnErrorWhenPlatformUidIsBlank() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("github");
            dto.setPlatformUid("");

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交认证 - platform和platformUid都为null")
        void shouldReturnErrorWhenBothParamsAreNull() {
            SocialAuthDto dto = new SocialAuthDto();

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交认证 - 已绑定用户，直接登录成功")
        void shouldLoginDirectlyWhenAlreadyBound() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");

            ApUserSocial social = new ApUserSocial();
            social.setUserId(1001);
            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(social);

            ApUser user = new ApUser();
            user.setId(1001);
            user.setNickname("GitHub用户");
            user.setPhone("13800138000");
            user.setImage("avatar_1");
            user.setStatus(true);
            when(apUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(user);

            LoginResultVo loginResult = LoginResultVo.builder()
                    .status("login")
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .userId(1001)
                    .build();
            when(tokenService.generateDualToken(eq(1001), eq("GitHub用户"), eq("13800138000"), eq("avatar_1")))
                    .thenReturn(loginResult);

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(loginResult, result.getData());
        }

        @Test
        @DisplayName("社交认证 - 已绑定但用户不存在")
        void shouldReturnErrorWhenUserNotFound() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");

            ApUserSocial social = new ApUserSocial();
            social.setUserId(1001);
            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(social);

            when(apUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交认证 - 已绑定但用户被锁定")
        void shouldReturnErrorWhenUserIsLocked() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");

            ApUserSocial social = new ApUserSocial();
            social.setUserId(1001);
            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(social);

            ApUser user = new ApUser();
            user.setId(1001);
            user.setStatus(false);
            when(apUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(user);

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交认证 - 未绑定用户，返回need_bind状态")
        void shouldReturnNeedBindForNewUser() {
            SocialAuthDto dto = new SocialAuthDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github_new");

            when(apUserSocialMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            ResponseResult result = socialLoginService.socialAuth(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            LoginResultVo data = (LoginResultVo) result.getData();
            assertNotNull(data);
            assertEquals("need_bind", data.getStatus());
            assertEquals("github", data.getPlatform());
            assertNotNull(data.getPlatformUid());
            // 验证platformUid已被加密
            assertNotEquals("github_new", data.getPlatformUid());
        }
    }

    // ==================== socialBind ====================

    @Nested
    @DisplayName("socialBind 方法测试")
    class SocialBindTests {

        @Test
        @DisplayName("社交绑定 - 验证码错误")
        void shouldReturnErrorWhenCodeIsWrong() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("encryptedUid");
            dto.setPhone("13800138000");
            dto.setCode("1234");

            when(cacheService.get("socialBind:github:13800138000")).thenReturn("5678");

            ResponseResult result = socialLoginService.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.LOGIN_CODE_ERROR.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交绑定 - 验证码缓存为空")
        void shouldReturnErrorWhenCodeCacheIsEmpty() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("encryptedUid");
            dto.setPhone("13800138000");
            dto.setCode("1234");

            when(cacheService.get("socialBind:github:13800138000")).thenReturn(null);

            ResponseResult result = socialLoginService.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.LOGIN_CODE_ERROR.getCode(), result.getCode());
        }
    }

    // ==================== checkSocialBind ====================

    @Nested
    @DisplayName("checkSocialBind 方法测试")
    class CheckSocialBindTests {

        @Test
        @DisplayName("检查绑定 - tag为bind且已绑定，返回null")
        void shouldReturnNullWhenAlreadyBoundForBind() {
            String phone = "13800138000";
            String platform = "github";
            ApUserSocial existing = new ApUserSocial();
            existing.setPhone(phone);

            when(socialLoginService.getOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(existing);

            String result = socialLoginService.checkSocialBind(phone, platform, "bind");

            assertNull(result);
        }

        @Test
        @DisplayName("检查绑定 - tag为bind且未绑定，返回验证码")
        void shouldReturnCodeWhenNotBoundForBind() {
            String phone = "13800138000";
            String platform = "github";

            when(socialLoginService.getOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            String result = socialLoginService.checkSocialBind(phone, platform, "bind");

            assertNotNull(result);
            assertEquals(4, result.length());
            verify(cacheService).setEx(eq("socialBind:github:13800138000"), anyString(), eq(5L), any());
        }

        @Test
        @DisplayName("检查绑定 - tag为login，直接返回验证码")
        void shouldReturnCodeForLogin() {
            String phone = "13800138000";
            String platform = "github";

            String result = socialLoginService.checkSocialBind(phone, platform, "login");

            assertNotNull(result);
            assertEquals(4, result.length());
            verify(cacheService).setEx(eq("socialBind:github:13800138000"), anyString(), eq(5L), any());
        }
    }
}