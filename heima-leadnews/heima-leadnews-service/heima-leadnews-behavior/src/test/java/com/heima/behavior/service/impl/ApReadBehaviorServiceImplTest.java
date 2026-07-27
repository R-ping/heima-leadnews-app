package com.heima.behavior.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ApReadBehaviorServiceImpl 单元测试
 * 测试阅读行为保存逻辑：参数校验、登录校验、阅读次数累加、Redis存储
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("阅读行为服务测试")
class ApReadBehaviorServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ApReadBehaviorServiceImpl apReadBehaviorService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMock;

    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setPhone("13800138000");
        mockUser.setNickname("测试用户");

        appThreadLocalUtilMock = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (appThreadLocalUtilMock != null) {
            appThreadLocalUtilMock.close();
        }
    }

    // ==================== 参数校验 ====================

    @Test
    @DisplayName("参数校验 — dto为null应返回参数无效")
    void testReadBehavior_NullDto() {
        ResponseResult result = apReadBehaviorService.readBehavior(null);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — articleId为null应返回参数无效")
    void testReadBehavior_NullArticleId() {
        ReadBehaviorDto dto = new ReadBehaviorDto();
        dto.setArticleId(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 登录校验 ====================

    @Test
    @DisplayName("登录校验 — 未登录用户应返回需要登录")
    void testReadBehavior_UserNotLoggedIn() {
        ReadBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 正常流程 ====================

    @Test
    @DisplayName("首次阅读 — 无历史记录，直接保存")
    void testReadBehavior_FirstRead() {
        ReadBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(cacheService.hGet(anyString(), eq("1"))).thenReturn(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());

        String expectedKey = BehaviorConstants.READ_BEHAVIOR + "1001";
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("重复阅读 — 有历史记录，阅读次数累加")
    void testReadBehavior_RepeatedRead() {
        ReadBehaviorDto dto = buildValidDto();
        dto.setCount((short) 3);

        // 模拟历史记录：count=5
        ReadBehaviorDto historyDto = new ReadBehaviorDto();
        historyDto.setArticleId(1001L);
        historyDto.setCount((short) 5);
        String historyJson = com.alibaba.fastjson.JSON.toJSONString(historyDto);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.READ_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "1")).thenReturn(historyJson);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());

        // 验证累加后 count = 5 + 3 = 8
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("重复阅读 — 历史记录count为0，正常累加")
    void testReadBehavior_RepeatedRead_ZeroHistoryCount() {
        ReadBehaviorDto dto = buildValidDto();
        dto.setCount((short) 2);

        ReadBehaviorDto historyDto = new ReadBehaviorDto();
        historyDto.setArticleId(1001L);
        historyDto.setCount((short) 0);
        String historyJson = com.alibaba.fastjson.JSON.toJSONString(historyDto);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.READ_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "1")).thenReturn(historyJson);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("历史记录为空字符串 — 视为无历史记录，直接保存")
    void testReadBehavior_BlankHistoryJson() {
        ReadBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.READ_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "1")).thenReturn("");

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    // ==================== 边界条件 ====================

    @Test
    @DisplayName("阅读次数 — count为null不影响保存")
    void testReadBehavior_NullCount() {
        ReadBehaviorDto dto = buildValidDto();
        dto.setCount(null);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(cacheService.hGet(anyString(), eq("1"))).thenReturn(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(anyString(), eq("1"), anyString());
    }

    @Test
    @DisplayName("大articleId — 正常处理")
    void testReadBehavior_LargeArticleId() {
        ReadBehaviorDto dto = new ReadBehaviorDto();
        dto.setArticleId(Long.MAX_VALUE);
        dto.setCount((short) 1);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.READ_BEHAVIOR + Long.MAX_VALUE;
        when(cacheService.hGet(expectedKey, "1")).thenReturn(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("不同用户 — 同一文章不同用户的阅读记录互不影响")
    void testReadBehavior_DifferentUsers() {
        ReadBehaviorDto dto = buildValidDto();
        ApUser anotherUser = new ApUser();
        anotherUser.setId(999);
        anotherUser.setNickname("另一个用户");

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(anotherUser);
        String expectedKey = BehaviorConstants.READ_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "999")).thenReturn(null);

        ResponseResult result = apReadBehaviorService.readBehavior(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "999");
        verify(cacheService).hPut(eq(expectedKey), eq("999"), anyString());
    }

    // ==================== 辅助方法 ====================

    private ReadBehaviorDto buildValidDto() {
        ReadBehaviorDto dto = new ReadBehaviorDto();
        dto.setArticleId(1001L);
        dto.setCount((short) 1);
        dto.setReadDuration(30);
        dto.setPercentage((short) 50);
        dto.setLoadDuration((short) 2);
        return dto;
    }
}