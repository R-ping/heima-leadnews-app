package com.heima.user.service.impl;

import com.aliyun.oss.OSS;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.ProfileUpdateDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.UserProfile;
import com.heima.model.user.pojos.UserTagRelation;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.UserProfileMapper;
import com.heima.user.mapper.UserTagRelationMapper;
import com.heima.user.service.UserProfileService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户资料服务测试")
public class UserProfileServiceImplTest {

    @MockBean
    private OSS ossClient;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private UserTagRelationMapper userTagRelationMapper;

    private ApUser testUser;

    private static int testIdCounter = 900100;

    @BeforeEach
    void setUp() {
        testUser = new ApUser();
        testUser.setId(testIdCounter++);
        testUser.setPhone("13900000001");
        testUser.setNickname("资料测试用户");
        testUser.setStatus(true);
        testUser.setFlag((short) 0);
        testUser.setCreatedTime(new Date());
        apUserMapper.insert(testUser);

        AppThreadLocalUtil.setUser(testUser);
    }

    @AfterEach
    void tearDown() {
        AppThreadLocalUtil.clear();
        if (testUser != null && testUser.getId() != null) {
            Long userId = testUser.getId().longValue();
            // 清理测试用户的资料
            userProfileMapper.deleteById(userId);
            // 清理测试用户的标签关联
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserTagRelation> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(UserTagRelation::getUserId, userId);
            userTagRelationMapper.delete(wrapper);
            // 清理测试用户
            apUserMapper.deleteById(testUser.getId());
        }
    }

    // ==================== getProfile ====================

    @Test
    @Order(1)
    @DisplayName("获取用户资料 - 正常返回（含标签分组）")
    void testGetProfile_Success() {
        ResponseResult result = userProfileService.getProfile();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof com.heima.model.user.vo.UserProfileVO);
    }

    @Test
    @Order(2)
    @DisplayName("获取用户资料 - 未登录返回错误")
    void testGetProfile_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = userProfileService.getProfile();
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updateProfile ====================

    @Test
    @Order(3)
    @DisplayName("更新用户资料 - 正常更新（含标签）")
    void testUpdateProfile_Success() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("测试用户名12345");
        dto.setCareerDirection("后端开发");
        dto.setPosition("高级工程师");
        dto.setCompany("测试公司");
        dto.setBio("个人简介");
        dto.setTagIds(Arrays.asList(1, 2));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证资料已保存
        UserProfile profile = userProfileMapper.selectById(testUser.getId().longValue());
        assertNotNull(profile);
        assertEquals("测试用户名12345", profile.getUsername());
        assertEquals("后端开发", profile.getCareerDirection());
    }

    @Test
    @Order(4)
    @DisplayName("更新用户资料 - 用户名为空返回错误")
    void testUpdateProfile_EmptyUsername() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("  ");
        dto.setCareerDirection("后端开发");
        dto.setTagIds(Arrays.asList(1));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("更新用户资料 - 用户名太短返回错误")
    void testUpdateProfile_ShortUsername() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("ab");
        dto.setCareerDirection("后端开发");
        dto.setTagIds(Arrays.asList(1));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("更新用户资料 - 用户名太长返回错误")
    void testUpdateProfile_LongUsername() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        // 21个字符
        dto.setUsername("a".repeat(21));
        dto.setCareerDirection("后端开发");
        dto.setTagIds(Arrays.asList(1));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(7)
    @DisplayName("更新用户资料 - 职业方向为空返回错误")
    void testUpdateProfile_EmptyCareerDirection() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("测试用户名12345");
        dto.setCareerDirection("  ");
        dto.setTagIds(Arrays.asList(1));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(8)
    @DisplayName("更新用户资料 - 标签为空返回错误")
    void testUpdateProfile_EmptyTags() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("测试用户名12345");
        dto.setCareerDirection("后端开发");
        dto.setTagIds(Collections.emptyList());

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(9)
    @DisplayName("更新用户资料 - 标签为null返回错误")
    void testUpdateProfile_NullTags() {
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("测试用户名12345");
        dto.setCareerDirection("后端开发");
        dto.setTagIds(null);

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(10)
    @DisplayName("更新用户资料 - 未登录返回错误")
    void testUpdateProfile_NotLogin() {
        AppThreadLocalUtil.clear();

        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername("测试用户名12345");
        dto.setCareerDirection("后端开发");
        dto.setTagIds(Arrays.asList(1));

        ResponseResult result = userProfileService.updateProfile(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(11)
    @DisplayName("更新用户资料 - 更新已存在的资料")
    void testUpdateProfile_UpdateExisting() {
        // 先创建一条资料
        ProfileUpdateDTO dto1 = new ProfileUpdateDTO();
        dto1.setUsername("初始用户名");
        dto1.setCareerDirection("前端开发");
        dto1.setTagIds(Arrays.asList(1));
        userProfileService.updateProfile(dto1);

        // 再更新资料
        ProfileUpdateDTO dto2 = new ProfileUpdateDTO();
        dto2.setUsername("更新后用户名");
        dto2.setCareerDirection("后端开发");
        dto2.setPosition("架构师");
        dto2.setTagIds(Arrays.asList(2, 3));

        ResponseResult result = userProfileService.updateProfile(dto2);
        assertNotNull(result);
        assertEquals(200, result.getCode());

        UserProfile profile = userProfileMapper.selectById(testUser.getId().longValue());
        assertEquals("更新后用户名", profile.getUsername());
        assertEquals("架构师", profile.getPosition());
    }

    // ==================== uploadAvatar ====================

    @Test
    @Order(12)
    @DisplayName("上传头像 - 未登录返回错误")
    void testUploadAvatar_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = userProfileService.uploadAvatar(null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("上传头像 - 文件为空返回错误")
    void testUploadAvatar_NullFile() {
        ResponseResult result = userProfileService.uploadAvatar(null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }
}