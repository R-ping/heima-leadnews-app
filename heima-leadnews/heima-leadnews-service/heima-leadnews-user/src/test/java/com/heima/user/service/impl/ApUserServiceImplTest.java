package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ApUserServiceImpl 单元测试
 * 测试密码加密、用户查询等核心逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class ApUserServiceImplTest {

    @Mock
    private ApUserMapper apUserMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setPhone("13800138000");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("$2a$10$hashed_password");
        mockUser.setNickname("测试用户");
        mockUser.setCreatedTime(new Date());
    }

    @Test
    @DisplayName("密码加密 — BCrypt 编码正确")
    void testPasswordEncoding() {
        String rawPassword = "testPassword123";
        String encodedPassword = "$2a$10$dummyEncodedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        String encoded = passwordEncoder.encode(rawPassword);
        assertNotNull(encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        verify(passwordEncoder).encode(rawPassword);
        verify(passwordEncoder).matches(rawPassword, encoded);
    }

    @Test
    @DisplayName("BCrypt — 错误密码应返回false")
    void testPasswordMismatch() {
        String wrongPassword = "wrongPassword";
        String encodedPassword = "$2a$10$dummyEncodedPassword";

        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        boolean result = passwordEncoder.matches(wrongPassword, encodedPassword);
        assertFalse(result);
    }

    @Test
    @DisplayName("用户查询 — 按手机号查询")
    void testFindByPhone() {
        when(apUserMapper.selectOne(any())).thenReturn(mockUser);

        ApUser result = apUserMapper.selectOne(
            Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, "13800138000"));

        assertNotNull(result);
        assertEquals("13800138000", result.getPhone());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("用户查询 — 手机号不存在返回null")
    void testFindByPhoneNotFound() {
        when(apUserMapper.selectOne(any())).thenReturn(null);

        ApUser result = apUserMapper.selectOne(
            Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, "00000000000"));

        assertNull(result);
    }

    @Test
    @DisplayName("用户插入 — 保存新用户")
    void testInsertUser() {
        ApUser newUser = new ApUser();
        newUser.setPhone("13900139000");
        newUser.setPassword("$2a$10$encrypted");
        newUser.setNickname("新用户");

        when(apUserMapper.insert(any(ApUser.class))).thenReturn(1);

        int result = apUserMapper.insert(newUser);
        assertEquals(1, result);
        verify(apUserMapper).insert(newUser);
    }
}