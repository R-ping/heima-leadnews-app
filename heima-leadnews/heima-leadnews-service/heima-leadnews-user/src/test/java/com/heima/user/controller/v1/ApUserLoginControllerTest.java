package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.SocialBindDto;
import com.heima.user.service.ApUserService;
import com.heima.user.service.SocialLoginService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApUserLoginController 单元测试")
class ApUserLoginControllerTest {

    @Mock
    private ApUserService apUserService;
    @Mock
    private SocialLoginService socialLoginService;

    @InjectMocks
    private ApUserLoginController loginController;

    // ==================== login ====================

    @Nested
    @DisplayName("login 方法测试")
    class LoginTests {

        @Test
        @DisplayName("手机号验证码登录/注册")
        void shouldLoginWithPhoneCode() {
            LoginDto dto = new LoginDto();
            dto.setPhoneOrEmail("13800138000");
            ResponseResult expected = ResponseResult.okResult("token");
            when(apUserService.allLoginAuth(dto, "phoneCode")).thenReturn(expected);

            ResponseResult result = loginController.login(dto);

            assertSame(expected, result);
            verify(apUserService).allLoginAuth(dto, "phoneCode");
        }

        @Test
        @DisplayName("手机号+密码登录")
        void shouldLoginWithPhonePassword() {
            LoginDto dto = new LoginDto();
            dto.setPhoneOrEmail("13800138000");
            dto.setPassword("password123");
            ResponseResult expected = ResponseResult.okResult("token");
            when(apUserService.allLoginAuth(dto, "phonePass")).thenReturn(expected);

            ResponseResult result = loginController.login(dto);

            assertSame(expected, result);
            verify(apUserService).allLoginAuth(dto, "phonePass");
        }

        @Test
        @DisplayName("邮箱+密码登录")
        void shouldLoginWithEmailPassword() {
            LoginDto dto = new LoginDto();
            dto.setPhoneOrEmail("test@example.com");
            dto.setPassword("password123");
            ResponseResult expected = ResponseResult.okResult("token");
            when(apUserService.allLoginAuth(dto, "emailPass")).thenReturn(expected);

            ResponseResult result = loginController.login(dto);

            assertSame(expected, result);
            verify(apUserService).allLoginAuth(dto, "emailPass");
        }

        @Test
        @DisplayName("登录 - phoneOrEmail为空且无密码")
        void shouldLoginWithEmptyPhoneOrEmail() {
            LoginDto dto = new LoginDto();
            dto.setPhoneOrEmail("");
            ResponseResult expected = ResponseResult.okResult("token");
            when(apUserService.allLoginAuth(dto, "phoneCode")).thenReturn(expected);

            ResponseResult result = loginController.login(dto);

            assertSame(expected, result);
            verify(apUserService).allLoginAuth(dto, "phoneCode");
        }
    }

    // ==================== socialBind ====================

    @Nested
    @DisplayName("socialBind 方法测试")
    class SocialBindTests {

        @Test
        @DisplayName("正常绑定社交账号")
        void shouldBindSocialAccountSuccessfully() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");
            dto.setPhone("13800138000");
            dto.setCode("1234");

            ResponseResult expected = ResponseResult.okResult();
            when(socialLoginService.socialBind(dto)).thenReturn(expected);

            ResponseResult result = loginController.socialBind(dto);

            assertSame(expected, result);
            verify(socialLoginService).socialBind(dto);
        }

        @Test
        @DisplayName("社交绑定 - platform为空")
        void shouldReturnErrorWhenPlatformIsBlank() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("");
            dto.setPlatformUid("github123");
            dto.setPhone("13800138000");
            dto.setCode("1234");

            ResponseResult result = loginController.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
            verify(socialLoginService, never()).socialBind(any());
        }

        @Test
        @DisplayName("社交绑定 - platformUid为空")
        void shouldReturnErrorWhenPlatformUidIsBlank() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("");
            dto.setPhone("13800138000");
            dto.setCode("1234");

            ResponseResult result = loginController.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交绑定 - phone为空")
        void shouldReturnErrorWhenPhoneIsBlank() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");
            dto.setPhone("");
            dto.setCode("1234");

            ResponseResult result = loginController.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }

        @Test
        @DisplayName("社交绑定 - code为空")
        void shouldReturnErrorWhenCodeIsBlank() {
            SocialBindDto dto = new SocialBindDto();
            dto.setPlatform("github");
            dto.setPlatformUid("github123");
            dto.setPhone("13800138000");
            dto.setCode("");

            ResponseResult result = loginController.socialBind(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_REQUIRE.getCode(), result.getCode());
        }
    }

    // ==================== getCode ====================

    @Nested
    @DisplayName("getCode 方法测试")
    class GetCodeTests {

        @Test
        @DisplayName("正常获取验证码 - login场景")
        void shouldGetCodeForLogin() {
            when(socialLoginService.checkSocialBind("13800138000", "github", "login"))
                    .thenReturn("1234");

            ResponseResult result = loginController.getCode("13800138000", "github", "login");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals("1234", result.getData());
        }

        @Test
        @DisplayName("正常获取验证码 - bind场景")
        void shouldGetCodeForBind() {
            when(socialLoginService.checkSocialBind("13800138000", "github", "bind"))
                    .thenReturn("5678");

            ResponseResult result = loginController.getCode("13800138000", "github", "bind");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals("5678", result.getData());
        }

        @Test
        @DisplayName("获取验证码 - phone为空")
        void shouldReturnErrorWhenPhoneIsBlank() {
            ResponseResult result = loginController.getCode("", "github", "login");

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        }

        @Test
        @DisplayName("获取验证码 - platform为空")
        void shouldReturnErrorWhenPlatformIsBlank() {
            ResponseResult result = loginController.getCode("13800138000", "", "login");

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        }

        @Test
        @DisplayName("获取验证码 - 手机号已绑定其他账号")
        void shouldReturnErrorWhenPhoneAlreadyBound() {
            when(socialLoginService.checkSocialBind("13800138000", "github", "bind"))
                    .thenReturn(null);

            ResponseResult result = loginController.getCode("13800138000", "github", "bind");

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SOCIAL_PHONE_BOUND_OTHER.getCode(), result.getCode());
        }
    }
}