package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginResultVo;
import com.heima.model.user.dtos.RefreshTokenDto;
import com.heima.user.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenController 单元测试")
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenController tokenController;

    // ==================== refreshToken ====================

    @Nested
    @DisplayName("refreshToken 方法测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("正常刷新Token - 返回成功")
        void shouldRefreshTokenSuccessfully() {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken("valid-refresh-token");

            LoginResultVo loginResult = LoginResultVo.builder()
                    .status("login")
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .userId(1)
                    .build();
            when(tokenService.refreshToken("valid-refresh-token")).thenReturn(loginResult);

            ResponseResult result = tokenController.refreshToken(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(loginResult, result.getData());
        }

        @Test
        @DisplayName("刷新Token - refresh_token无效返回错误")
        void shouldReturnErrorWhenRefreshTokenInvalid() {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken("invalid-token");

            when(tokenService.refreshToken("invalid-token")).thenReturn(null);

            ResponseResult result = tokenController.refreshToken(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.TOKEN_INVALID.getCode(), result.getCode());
            assertEquals("refresh_token无效或已过期", result.getMessage());
        }

        @Test
        @DisplayName("刷新Token - refreshToken为null")
        void shouldReturnErrorWhenRefreshTokenIsNull() {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken(null);

            when(tokenService.refreshToken(null)).thenReturn(null);

            ResponseResult result = tokenController.refreshToken(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.TOKEN_INVALID.getCode(), result.getCode());
        }
    }

    // ==================== logout ====================

    @Nested
    @DisplayName("logout 方法测试")
    class LogoutTests {

        @Test
        @DisplayName("正常登出 - 返回成功")
        void shouldLogoutSuccessfully() {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken("token-to-revoke");

            doNothing().when(tokenService).revokeRefreshToken("token-to-revoke");

            ResponseResult result = tokenController.logout(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
            verify(tokenService).revokeRefreshToken("token-to-revoke");
        }

        @Test
        @DisplayName("登出 - refreshToken为空也正常执行")
        void shouldHandleEmptyRefreshToken() {
            RefreshTokenDto dto = new RefreshTokenDto();
            dto.setRefreshToken("");

            doNothing().when(tokenService).revokeRefreshToken("");

            ResponseResult result = tokenController.logout(dto);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        }
    }
}