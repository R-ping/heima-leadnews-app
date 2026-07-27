package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.ProfileUpdateDTO;
import com.heima.user.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileController 单元测试")
class UserProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    // ==================== getProfile ====================

    @Nested
    @DisplayName("getProfile 方法测试")
    class GetProfileTests {

        @Test
        @DisplayName("正常获取个人资料")
        void shouldReturnProfileSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("profile-data");
            when(userProfileService.getProfile()).thenReturn(expected);

            ResponseResult result = userProfileController.getProfile();

            assertSame(expected, result);
            verify(userProfileService).getProfile();
        }

        @Test
        @DisplayName("获取个人资料 - 服务返回错误")
        void shouldReturnErrorWhenServiceFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "获取资料失败");
            when(userProfileService.getProfile()).thenReturn(expected);

            ResponseResult result = userProfileController.getProfile();

            assertEquals(500, result.getCode());
            assertEquals("获取资料失败", result.getMessage());
        }
    }

    // ==================== updateProfile ====================

    @Nested
    @DisplayName("updateProfile 方法测试")
    class UpdateProfileTests {

        @Test
        @DisplayName("正常更新个人资料")
        void shouldUpdateProfileSuccessfully() {
            ProfileUpdateDTO dto = new ProfileUpdateDTO();
            dto.setUsername("新昵称");
            dto.setBio("个人简介");
            ResponseResult expected = ResponseResult.okResult();
            when(userProfileService.updateProfile(any(ProfileUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = userProfileController.updateProfile(dto);

            assertSame(expected, result);
            verify(userProfileService).updateProfile(dto);
        }

        @Test
        @DisplayName("更新个人资料 - 部分字段更新")
        void shouldUpdatePartialProfile() {
            ProfileUpdateDTO dto = new ProfileUpdateDTO();
            dto.setBio("仅更新简介");
            ResponseResult expected = ResponseResult.okResult();
            when(userProfileService.updateProfile(any(ProfileUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = userProfileController.updateProfile(dto);

            assertSame(expected, result);
            verify(userProfileService).updateProfile(dto);
        }

        @Test
        @DisplayName("更新个人资料 - 空DTO")
        void shouldHandleEmptyDto() {
            ProfileUpdateDTO dto = new ProfileUpdateDTO();
            ResponseResult expected = ResponseResult.okResult();
            when(userProfileService.updateProfile(any(ProfileUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = userProfileController.updateProfile(dto);

            assertSame(expected, result);
            verify(userProfileService).updateProfile(dto);
        }

        @Test
        @DisplayName("更新个人资料 - DTO为null")
        void shouldHandleNullDto() {
            ResponseResult expected = ResponseResult.errorResult(400, "参数不能为空");
            when(userProfileService.updateProfile(null)).thenReturn(expected);

            ResponseResult result = userProfileController.updateProfile(null);

            assertEquals(400, result.getCode());
            verify(userProfileService).updateProfile(null);
        }

        @Test
        @DisplayName("更新个人资料 - 服务返回错误")
        void shouldReturnErrorWhenUpdateFails() {
            ProfileUpdateDTO dto = new ProfileUpdateDTO();
            dto.setUsername("test");
            ResponseResult expected = ResponseResult.errorResult(503, "更新失败");
            when(userProfileService.updateProfile(any(ProfileUpdateDTO.class))).thenReturn(expected);

            ResponseResult result = userProfileController.updateProfile(dto);

            assertEquals(503, result.getCode());
            assertEquals("更新失败", result.getMessage());
        }
    }

    // ==================== uploadAvatar ====================

    @Nested
    @DisplayName("uploadAvatar 方法测试")
    class UploadAvatarTests {

        @Test
        @DisplayName("正常上传头像")
        void shouldUploadAvatarSuccessfully() {
            MultipartFile file = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", "fake-image-content".getBytes());
            ResponseResult expected = ResponseResult.okResult("https://cdn.example.com/avatar.jpg");
            when(userProfileService.uploadAvatar(any(MultipartFile.class))).thenReturn(expected);

            ResponseResult result = userProfileController.uploadAvatar(file);

            assertSame(expected, result);
            verify(userProfileService).uploadAvatar(file);
        }

        @Test
        @DisplayName("上传头像 - 文件为null")
        void shouldHandleNullFile() {
            ResponseResult expected = ResponseResult.errorResult(400, "文件不能为空");
            when(userProfileService.uploadAvatar(null)).thenReturn(expected);

            ResponseResult result = userProfileController.uploadAvatar(null);

            assertEquals(400, result.getCode());
            verify(userProfileService).uploadAvatar(null);
        }

        @Test
        @DisplayName("上传头像 - 空文件")
        void shouldHandleEmptyFile() {
            MultipartFile file = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);
            ResponseResult expected = ResponseResult.errorResult(400, "文件内容为空");
            when(userProfileService.uploadAvatar(any(MultipartFile.class))).thenReturn(expected);

            ResponseResult result = userProfileController.uploadAvatar(file);

            assertEquals(400, result.getCode());
            verify(userProfileService).uploadAvatar(file);
        }

        @Test
        @DisplayName("上传头像 - 文件类型不支持")
        void shouldHandleUnsupportedFileType() {
            MultipartFile file = new MockMultipartFile(
                    "file", "avatar.gif", "image/gif", "fake-gif".getBytes());
            ResponseResult expected = ResponseResult.errorResult(400, "不支持的文件类型");
            when(userProfileService.uploadAvatar(any(MultipartFile.class))).thenReturn(expected);

            ResponseResult result = userProfileController.uploadAvatar(file);

            assertEquals(400, result.getCode());
            verify(userProfileService).uploadAvatar(file);
        }

        @Test
        @DisplayName("上传头像 - 文件过大")
        void shouldHandleOversizedFile() {
            MultipartFile file = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", new byte[10 * 1024 * 1024]);
            ResponseResult expected = ResponseResult.errorResult(400, "文件大小超过限制");
            when(userProfileService.uploadAvatar(any(MultipartFile.class))).thenReturn(expected);

            ResponseResult result = userProfileController.uploadAvatar(file);

            assertEquals(400, result.getCode());
            verify(userProfileService).uploadAvatar(file);
        }
    }
}