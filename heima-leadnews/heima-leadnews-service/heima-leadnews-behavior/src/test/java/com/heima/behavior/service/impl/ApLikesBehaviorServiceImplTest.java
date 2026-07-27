package com.heima.behavior.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
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
 * ApLikesBehaviorServiceImpl 单元测试
 * 测试点赞/取消点赞行为：参数校验、登录校验、点赞保存、取消点赞删除、重复点赞拦截
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("点赞行为服务测试")
class ApLikesBehaviorServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ApLikesBehaviorServiceImpl apLikesBehaviorService;

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
    void testLike_NullDto() {
        ResponseResult result = apLikesBehaviorService.like(null);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — articleId为null应返回参数无效")
    void testLike_NullArticleId() {
        LikesBehaviorDto dto = new LikesBehaviorDto();
        dto.setArticleId(null);
        dto.setType((short) 0);
        dto.setOperation((short) 0);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — type > 2 应返回参数无效")
    void testLike_TypeGreaterThan2() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 3);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — type < 0 应返回参数无效")
    void testLike_TypeLessThan0() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setType((short) -1);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — operation > 1 应返回参数无效")
    void testLike_OperationGreaterThan1() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setOperation((short) 2);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — operation < 0 应返回参数无效")
    void testLike_OperationLessThan0() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setOperation((short) -1);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 登录校验 ====================

    @Test
    @DisplayName("登录校验 — 未登录用户应返回需要登录")
    void testLike_UserNotLoggedIn() {
        LikesBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 点赞操作（operation=0） ====================

    @Test
    @DisplayName("点赞 — 首次点赞成功保存到Redis")
    void testLike_FirstLike() {
        LikesBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "1")).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("点赞 — 重复点赞应返回已点赞错误")
    void testLike_DuplicateLike() {
        LikesBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "1")).thenReturn("existing_json");

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("已点赞"));
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService, never()).hPut(anyString(), anyString(), anyString());
    }

    // ==================== 取消点赞（operation=1） ====================

    @Test
    @DisplayName("取消点赞 — 正常删除Redis记录")
    void testLike_Unlike() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setOperation((short) 1);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + "1001";

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hDelete(expectedKey, "1");
        verify(cacheService, never()).hGet(anyString(), anyString());
        verify(cacheService, never()).hPut(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("取消点赞 — 即使无记录也不报错")
    void testLike_Unlike_NoExistingRecord() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setOperation((short) 1);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + "1001";

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hDelete(expectedKey, "1");
    }

    // ==================== 边界条件 ====================

    @Test
    @DisplayName("边界值 — type=0（文章）正常处理")
    void testLike_TypeArticle() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 0);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(cacheService.hGet(anyString(), eq("1"))).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @DisplayName("边界值 — type=2（评论）正常处理")
    void testLike_TypeComment() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 2);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(cacheService.hGet(anyString(), eq("1"))).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @DisplayName("边界值 — type=1（动态）正常处理")
    void testLike_TypeDynamic() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 1);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(cacheService.hGet(anyString(), eq("1"))).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
    }

    @Test
    @DisplayName("大articleId — 正常处理")
    void testLike_LargeArticleId() {
        LikesBehaviorDto dto = buildValidDto();
        dto.setArticleId(Long.MAX_VALUE);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + Long.MAX_VALUE;
        when(cacheService.hGet(expectedKey, "1")).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "1");
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("不同用户 — 同一文章点赞/取消点赞互不影响")
    void testLike_DifferentUsers() {
        LikesBehaviorDto dto = buildValidDto();
        ApUser anotherUser = new ApUser();
        anotherUser.setId(999);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(anotherUser);
        String expectedKey = BehaviorConstants.LIKE_BEHAVIOR + "1001";
        when(cacheService.hGet(expectedKey, "999")).thenReturn(null);

        ResponseResult result = apLikesBehaviorService.like(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hGet(expectedKey, "999");
        verify(cacheService).hPut(eq(expectedKey), eq("999"), anyString());
    }

    // ==================== 辅助方法 ====================

    private LikesBehaviorDto buildValidDto() {
        LikesBehaviorDto dto = new LikesBehaviorDto();
        dto.setArticleId(1001L);
        dto.setType((short) 0);
        dto.setOperation((short) 0);
        return dto;
    }
}