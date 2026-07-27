package com.heima.behavior.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
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
 * ApUnlikesBehaviorServiceImpl 单元测试
 * 测试不喜欢/取消不喜欢行为：参数校验、登录校验、不喜欢保存、取消不喜欢删除
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("不喜欢行为服务测试")
class ApUnlikesBehaviorServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ApUnlikesBehaviorServiceImpl apUnlikesBehaviorService;

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
    @DisplayName("参数校验 — dto为null时抛出NullPointerException")
    void testUnLike_NullDto() {
        // 代码中未做 dto==null 判断，直接调用 dto.getArticleId() 会抛出 NPE
        assertThrows(NullPointerException.class, () -> {
            apUnlikesBehaviorService.unLike(null);
        });
        verifyNoInteractions(cacheService);
    }

    @Test
    @DisplayName("参数校验 — articleId为null应返回参数无效")
    void testUnLike_NullArticleId() {
        UnLikesBehaviorDto dto = new UnLikesBehaviorDto();
        dto.setArticleId(null);

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 登录校验 ====================

    @Test
    @DisplayName("登录校验 — 未登录用户应返回需要登录")
    void testUnLike_UserNotLoggedIn() {
        UnLikesBehaviorDto dto = buildValidDto();
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verifyNoInteractions(cacheService);
    }

    // ==================== 不喜欢操作（type=0） ====================

    @Test
    @DisplayName("不喜欢 — type=0保存到Redis")
    void testUnLike_Save() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 0);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
        verify(cacheService, never()).hDelete(anyString(), any());
    }

    @Test
    @DisplayName("不喜欢 — 重复不喜欢仍可覆盖保存")
    void testUnLike_DuplicateSave() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 0);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    // ==================== 取消不喜欢（type!=0） ====================

    @Test
    @DisplayName("取消不喜欢 — type=1删除Redis记录")
    void testUnLike_Delete() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 1);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hDelete(expectedKey, "1");
        verify(cacheService, never()).hPut(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("取消不喜欢 — 即使无记录也不报错")
    void testUnLike_Delete_NoExistingRecord() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 1);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hDelete(expectedKey, "1");
    }

    // ==================== 边界条件 ====================

    @Test
    @DisplayName("type为其他非0值 — 走删除逻辑")
    void testUnLike_TypeOtherValue() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 99); // 任意非0值都走删除逻辑
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hDelete(expectedKey, "1");
        verify(cacheService, never()).hPut(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("大articleId — 正常处理")
    void testUnLike_LargeArticleId() {
        UnLikesBehaviorDto dto = new UnLikesBehaviorDto();
        dto.setArticleId(Long.MAX_VALUE);
        dto.setType((short) 0);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + Long.MAX_VALUE;

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(eq(expectedKey), eq("1"), anyString());
    }

    @Test
    @DisplayName("不同用户 — 同一文章不喜欢记录互不影响")
    void testUnLike_DifferentUsers() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType((short) 0);
        ApUser anotherUser = new ApUser();
        anotherUser.setId(999);

        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(anotherUser);
        String expectedKey = BehaviorConstants.UN_LIKE_BEHAVIOR + "1001";

        ResponseResult result = apUnlikesBehaviorService.unLike(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
        verify(cacheService).hPut(eq(expectedKey), eq("999"), anyString());
    }

    @Test
    @DisplayName("type为null — 自动拆箱抛出NullPointerException")
    void testUnLike_TypeNull() {
        UnLikesBehaviorDto dto = buildValidDto();
        dto.setType(null);
        appThreadLocalUtilMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        // dto.getType() 返回 null Short，与 0 比较时自动拆箱导致 NPE
        assertThrows(NullPointerException.class, () -> {
            apUnlikesBehaviorService.unLike(dto);
        });
        verifyNoInteractions(cacheService);
    }

    // ==================== 辅助方法 ====================

    private UnLikesBehaviorDto buildValidDto() {
        UnLikesBehaviorDto dto = new UnLikesBehaviorDto();
        dto.setArticleId(1001L);
        dto.setType((short) 0);
        return dto;
    }
}