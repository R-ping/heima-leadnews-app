package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.PasswordUpdateDTO;
import com.heima.model.user.dto.PrivacyMessageDTO;
import com.heima.user.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController 单元测试")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    // ==================== getBindings ====================

    @Nested
    @DisplayName("getBindings 方法测试")
    class GetBindingsTests {

        @Test
        @DisplayName("正常获取绑定信息")
        void shouldReturnBindingsSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("bindings-data");
            when(accountService.getBindings()).thenReturn(expected);

            ResponseResult result = accountController.getBindings();

            assertSame(expected, result);
            verify(accountService).getBindings();
        }

        @Test
        @DisplayName("获取绑定信息 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "获取绑定信息失败");
            when(accountService.getBindings()).thenReturn(expected);

            ResponseResult result = accountController.getBindings();

            assertEquals(500, result.getCode());
            assertEquals("获取绑定信息失败", result.getMessage());
        }
    }

    // ==================== updatePassword ====================

    @Nested
    @DisplayName("updatePassword 方法测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("正常修改密码")
        void shouldUpdatePasswordSuccessfully() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("oldPass");
            dto.setNewPassword("newPass");
            ResponseResult expected = ResponseResult.okResult();
            when(accountService.updatePassword(any(PasswordUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePassword(dto);

            assertSame(expected, result);
            verify(accountService).updatePassword(dto);
        }

        @Test
        @DisplayName("修改密码 - 旧密码为空")
        void shouldHandleEmptyOldPassword() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("");
            dto.setNewPassword("newPass");
            ResponseResult expected = ResponseResult.errorResult(503, "旧密码不能为空");
            when(accountService.updatePassword(any(PasswordUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePassword(dto);

            assertEquals(503, result.getCode());
            verify(accountService).updatePassword(dto);
        }

        @Test
        @DisplayName("修改密码 - 新密码过短")
        void shouldHandleShortNewPassword() {
            PasswordUpdateDTO dto = new PasswordUpdateDTO();
            dto.setOldPassword("oldPass");
            dto.setNewPassword("123");
            ResponseResult expected = ResponseResult.errorResult(503, "新密码长度不足");
            when(accountService.updatePassword(any(PasswordUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePassword(dto);

            assertEquals(503, result.getCode());
            verify(accountService).updatePassword(dto);
        }

        @Test
        @DisplayName("修改密码 - DTO为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数不能为空");
            when(accountService.updatePassword(null)).thenReturn(expected);

            ResponseResult result = accountController.updatePassword(null);

            assertEquals(400, result.getCode());
            verify(accountService).updatePassword(null);
        }
    }

    // ==================== deleteAccount ====================

    @Nested
    @DisplayName("deleteAccount 方法测试")
    class DeleteAccountTests {

        @Test
        @DisplayName("正常注销账号")
        void shouldDeleteAccountSuccessfully() {
            ResponseResult expected = ResponseResult.okResult();
            when(accountService.deleteAccount()).thenReturn(expected);

            ResponseResult result = accountController.deleteAccount();

            assertSame(expected, result);
            verify(accountService).deleteAccount();
        }

        @Test
        @DisplayName("注销账号 - 服务返回错误")
        void shouldReturnErrorWhenDeleteFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "注销失败");
            when(accountService.deleteAccount()).thenReturn(expected);

            ResponseResult result = accountController.deleteAccount();

            assertEquals(500, result.getCode());
            assertEquals("注销失败", result.getMessage());
        }
    }

    // ==================== updatePrivacyMessage ====================

    @Nested
    @DisplayName("updatePrivacyMessage 方法测试")
    class UpdatePrivacyMessageTests {

        @Test
        @DisplayName("正常更新私信设置 - scope=1（我关注的人）")
        void shouldUpdatePrivacySuccessfully() {
            PrivacyMessageDTO dto = new PrivacyMessageDTO();
            dto.setScope(1);
            ResponseResult expected = ResponseResult.okResult();
            when(accountService.updatePrivacyMessage(any(PrivacyMessageDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(dto);

            assertSame(expected, result);
            verify(accountService).updatePrivacyMessage(dto);
        }

        @Test
        @DisplayName("更新私信设置 - scope=0（所有人）")
        void shouldUpdatePrivacyScopeAll() {
            PrivacyMessageDTO dto = new PrivacyMessageDTO();
            dto.setScope(0);
            ResponseResult expected = ResponseResult.okResult();
            when(accountService.updatePrivacyMessage(any(PrivacyMessageDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(dto);

            assertSame(expected, result);
            verify(accountService).updatePrivacyMessage(dto);
        }

        @Test
        @DisplayName("更新私信设置 - scope=3（关闭）")
        void shouldUpdatePrivacyScopeClose() {
            PrivacyMessageDTO dto = new PrivacyMessageDTO();
            dto.setScope(3);
            ResponseResult expected = ResponseResult.okResult();
            when(accountService.updatePrivacyMessage(any(PrivacyMessageDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(dto);

            assertSame(expected, result);
            verify(accountService).updatePrivacyMessage(dto);
        }

        @Test
        @DisplayName("更新私信设置 - scope无效")
        void shouldHandleInvalidScope() {
            PrivacyMessageDTO dto = new PrivacyMessageDTO();
            dto.setScope(5);
            ResponseResult expected = ResponseResult.errorResult(503, "scope参数无效");
            when(accountService.updatePrivacyMessage(any(PrivacyMessageDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(dto);

            assertEquals(503, result.getCode());
            verify(accountService).updatePrivacyMessage(dto);
        }

        @Test
        @DisplayName("更新私信设置 - scope为null")
        void shouldHandleNullScope() {
            PrivacyMessageDTO dto = new PrivacyMessageDTO();
            dto.setScope(null);
            ResponseResult expected = ResponseResult.errorResult(503, "scope参数不能为空");
            when(accountService.updatePrivacyMessage(any(PrivacyMessageDTO.class))).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(dto);

            assertEquals(503, result.getCode());
            verify(accountService).updatePrivacyMessage(dto);
        }

        @Test
        @DisplayName("更新私信设置 - DTO为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数不能为空");
            when(accountService.updatePrivacyMessage(null)).thenReturn(expected);

            ResponseResult result = accountController.updatePrivacyMessage(null);

            assertEquals(400, result.getCode());
            verify(accountService).updatePrivacyMessage(null);
        }
    }
}