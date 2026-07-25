package com.heima.user.service.impl;

import com.aliyun.oss.OSS;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.PasswordUpdateDTO;
import com.heima.model.user.dto.PrivacyMessageDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.UserProfileMapper;
import com.heima.user.service.AccountService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("账户服务测试")
public class AccountServiceImplTest {

    @MockBean
    private OSS ossClient;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private ApUser testUser;

    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_NICKNAME = "测试用户";

    // 用于生成唯一测试ID的计数器
    private static int testIdCounter = 900000;

    @BeforeEach
    void setUp() {
        // 创建测试用户并插入数据库
        testUser = new ApUser();
        // 设置一个在 INT UNSIGNED 范围内的唯一 ID，避免 Snowflake ID 超出范围
        testUser.setId(testIdCounter++);
        testUser.setPhone(TEST_PHONE);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setNickname(TEST_NICKNAME);
        testUser.setStatus(true);
        testUser.setFlag((short) 0);
        testUser.setCreatedTime(new Date());
        apUserMapper.insert(testUser);

        // 设置到 ThreadLocal
        AppThreadLocalUtil.setUser(testUser);
    }

    @AfterEach
    void tearDown() {
        // 清理 ThreadLocal
        AppThreadLocalUtil.clear();
        // 清理测试用户
        if (testUser != null && testUser.getId() != null) {
            apUserMapper.deleteById(testUser.getId());
        }
    }

    // ==================== getBindings ====================

    @Test
    @Order(1)
    @DisplayName("获取绑定信息 - 正常返回绑定信息（手机号脱敏）")
    void testGetBindings_Success() {
        ResponseResult result = accountService.getBindings();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @Order(2)
    @DisplayName("获取绑定信息 - 用户未登录返回错误")
    void testGetBindings_NotLogin() {
        AppThreadLocalUtil.clear();
        ResponseResult result = accountService.getBindings();
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updatePassword ====================

    @Test
    @Order(3)
    @DisplayName("修改密码 - 正常修改密码")
    void testUpdatePassword_Success() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword(TEST_PASSWORD);
        dto.setNewPassword("newPassword123");

        ResponseResult result = accountService.updatePassword(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证密码确实已更新
        ApUser updatedUser = apUserMapper.selectById(testUser.getId());
        assertTrue(passwordEncoder.matches("newPassword123", updatedUser.getPassword()));
    }

    @Test
    @Order(4)
    @DisplayName("修改密码 - 旧密码为空返回错误")
    void testUpdatePassword_EmptyOldPassword() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("");
        dto.setNewPassword("newPassword123");

        ResponseResult result = accountService.updatePassword(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("修改密码 - 新密码长度不足返回错误")
    void testUpdatePassword_ShortNewPassword() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword(TEST_PASSWORD);
        dto.setNewPassword("12345");

        ResponseResult result = accountService.updatePassword(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("修改密码 - 旧密码错误返回错误")
    void testUpdatePassword_WrongOldPassword() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword("newPassword123");

        ResponseResult result = accountService.updatePassword(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(7)
    @DisplayName("修改密码 - 用户未登录返回错误")
    void testUpdatePassword_NotLogin() {
        AppThreadLocalUtil.clear();

        PasswordUpdateDTO dto = new PasswordUpdateDTO();
        dto.setOldPassword(TEST_PASSWORD);
        dto.setNewPassword("newPassword123");

        ResponseResult result = accountService.updatePassword(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== deleteAccount ====================

    @Test
    @Order(8)
    @DisplayName("注销账号 - 正常注销（status=0, phone=null）")
    void testDeleteAccount_Success() {
        ResponseResult result = accountService.deleteAccount();
        assertNotNull(result);
        assertEquals(200, result.getCode());

        // 验证用户状态已变更
        ApUser deletedUser = apUserMapper.selectById(testUser.getId());
        assertNotNull(deletedUser);
        assertFalse(deletedUser.getStatus());
        assertNull(deletedUser.getPhone());
    }

    @Test
    @Order(9)
    @DisplayName("注销账号 - 用户未登录返回错误")
    void testDeleteAccount_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = accountService.deleteAccount();
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updatePrivacyMessage ====================

    @Test
    @Order(10)
    @DisplayName("更新私信设置 - 正常设置私信权限")
    void testUpdatePrivacyMessage_Success() {
        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(1); // 我关注的人

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(11)
    @DisplayName("更新私信设置 - scope无效返回错误")
    void testUpdatePrivacyMessage_InvalidScope() {
        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(5); // 超出范围

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(12)
    @DisplayName("更新私信设置 - scope为负数返回错误")
    void testUpdatePrivacyMessage_NegativeScope() {
        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(-1);

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("更新私信设置 - 用户未登录返回错误")
    void testUpdatePrivacyMessage_NotLogin() {
        AppThreadLocalUtil.clear();

        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(0);

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(14)
    @DisplayName("更新私信设置 - scope=0（所有人）")
    void testUpdatePrivacyMessage_ScopeAll() {
        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(0);

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(15)
    @DisplayName("更新私信设置 - scope=3（关闭）")
    void testUpdatePrivacyMessage_ScopeClose() {
        PrivacyMessageDTO dto = new PrivacyMessageDTO();
        dto.setScope(3);

        ResponseResult result = accountService.updatePrivacyMessage(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}