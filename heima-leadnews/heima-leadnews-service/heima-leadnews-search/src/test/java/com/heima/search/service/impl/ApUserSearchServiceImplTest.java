package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ApUserSearchServiceImpl 单元测试
 * 测试用户搜索历史记录的增删查
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户搜索历史服务测试")
class ApUserSearchServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ApUserSearchServiceImpl apUserSearchService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;

    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1001);
        mockUser.setNickname("测试用户");
        mockUser.setPhone("13800138000");

        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (threadLocalMock != null) {
            threadLocalMock.close();
        }
    }

    // ==================== insert 方法 ====================

    @Test
    @DisplayName("insert — 新增关键词（历史记录不足10条）")
    void testInsert_NewKeyword() {
        String keyword = "新关键词";
        Integer userId = 1001;

        // 查询无已有记录
        when(mongoTemplate.findOne(any(Query.class), eq(ApUserSearch.class))).thenReturn(null);
        // 查询历史总数
        List<ApUserSearch> existingList = new ArrayList<>();
        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class))).thenReturn(existingList);

        apUserSearchService.insert(keyword, userId);

        verify(mongoTemplate).findOne(any(Query.class), eq(ApUserSearch.class));
        verify(mongoTemplate).find(any(Query.class), eq(ApUserSearch.class));
        verify(mongoTemplate).save(any(ApUserSearch.class));
    }

    @Test
    @DisplayName("insert — 已有关键词则更新创建时间")
    void testInsert_ExistingKeyword() {
        String keyword = "已有关键词";
        Integer userId = 1001;

        ApUserSearch existing = new ApUserSearch();
        existing.setId("abc123");
        existing.setUserId(userId);
        existing.setKeyword(keyword);
        existing.setCreatedTime(new Date(System.currentTimeMillis() - 3600000));

        when(mongoTemplate.findOne(any(Query.class), eq(ApUserSearch.class))).thenReturn(existing);

        apUserSearchService.insert(keyword, userId);

        verify(mongoTemplate).findOne(any(Query.class), eq(ApUserSearch.class));
        verify(mongoTemplate).save(existing);
        // 不应该去查询总数或替换
        verify(mongoTemplate, never()).find(any(Query.class), eq(ApUserSearch.class));
        verify(mongoTemplate, never()).findAndReplace(any(), any(ApUserSearch.class));
    }

    @Test
    @DisplayName("insert — 历史记录满10条时替换最旧记录")
    void testInsert_AtLimitReplaceOldest() {
        String keyword = "新关键词";
        Integer userId = 1001;

        when(mongoTemplate.findOne(any(Query.class), eq(ApUserSearch.class))).thenReturn(null);

        // 构造10条历史记录
        List<ApUserSearch> existingList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApUserSearch item = new ApUserSearch();
            item.setId("id_" + i);
            item.setUserId(userId);
            item.setKeyword("历史关键词_" + i);
            item.setCreatedTime(new Date(System.currentTimeMillis() - (10 - i) * 3600000L));
            existingList.add(item);
        }
        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class))).thenReturn(existingList);

        apUserSearchService.insert(keyword, userId);

        verify(mongoTemplate).findOne(any(Query.class), eq(ApUserSearch.class));
        verify(mongoTemplate).find(any(Query.class), eq(ApUserSearch.class));
        // 替换最旧一条（列表最后一条）
        verify(mongoTemplate).findAndReplace(any(Query.class), any(ApUserSearch.class));
        verify(mongoTemplate, never()).save(any(ApUserSearch.class));
    }

    @Test
    @DisplayName("insert — 历史记录为null时新增")
    void testInsert_NullHistoryList() {
        String keyword = "新关键词";
        Integer userId = 1001;

        when(mongoTemplate.findOne(any(Query.class), eq(ApUserSearch.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class))).thenReturn(null);

        apUserSearchService.insert(keyword, userId);

        verify(mongoTemplate).save(any(ApUserSearch.class));
    }

    // ==================== findUserSearch 方法 ====================

    @Test
    @DisplayName("findUserSearch — 已登录用户返回搜索历史")
    void testFindUserSearch_Success() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        List<ApUserSearch> mockList = new ArrayList<>();
        ApUserSearch item = new ApUserSearch();
        item.setId("id1");
        item.setUserId(1001);
        item.setKeyword("测试搜索");
        item.setCreatedTime(new Date());
        mockList.add(item);

        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class))).thenReturn(mockList);

        ResponseResult result = apUserSearchService.findUserSearch();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        @SuppressWarnings("unchecked")
        List<ApUserSearch> data = (List<ApUserSearch>) result.getData();
        assertEquals(1, data.size());
        assertEquals("测试搜索", data.get(0).getKeyword());
    }

    @Test
    @DisplayName("findUserSearch — 未登录返回需要登录")
    void testFindUserSearch_NotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = apUserSearchService.findUserSearch();

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(mongoTemplate, never()).find(any(Query.class), eq(ApUserSearch.class));
    }

    @Test
    @DisplayName("findUserSearch — 已登录但无历史返回空列表")
    void testFindUserSearch_EmptyHistory() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        when(mongoTemplate.find(any(Query.class), eq(ApUserSearch.class))).thenReturn(new ArrayList<>());

        ResponseResult result = apUserSearchService.findUserSearch();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<ApUserSearch> data = (List<ApUserSearch>) result.getData();
        assertTrue(data.isEmpty());
    }

    // ==================== delUserSearch 方法 ====================

    @Test
    @DisplayName("delUserSearch — 正常删除历史记录")
    void testDelUserSearch_Success() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        HistorySearchDto dto = new HistorySearchDto();
        dto.setId("record_123");

        ResponseResult result = apUserSearchService.delUserSearch(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(mongoTemplate).remove(any(Query.class), eq(ApUserSearch.class));
    }

    @Test
    @DisplayName("delUserSearch — id为null返回参数错误")
    void testDelUserSearch_NullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        HistorySearchDto dto = new HistorySearchDto();
        dto.setId(null);

        ResponseResult result = apUserSearchService.delUserSearch(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verify(mongoTemplate, never()).remove(any(Query.class), eq(ApUserSearch.class));
    }

    @Test
    @DisplayName("delUserSearch — dto为null抛出NPE（由Controller层保证参数非null）")
    void testDelUserSearch_NullDto() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        assertThrows(NullPointerException.class, () -> apUserSearchService.delUserSearch(null));
    }

    @Test
    @DisplayName("delUserSearch — 未登录返回需要登录")
    void testDelUserSearch_NotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        HistorySearchDto dto = new HistorySearchDto();
        dto.setId("record_123");

        ResponseResult result = apUserSearchService.delUserSearch(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
        verify(mongoTemplate, never()).remove(any(Query.class), eq(ApUserSearch.class));
    }

    @Test
    @DisplayName("delUserSearch — 未登录且id为null时先返回参数错误")
    void testDelUserSearch_NullId_NotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        HistorySearchDto dto = new HistorySearchDto();
        dto.setId(null);

        ResponseResult result = apUserSearchService.delUserSearch(dto);

        assertNotNull(result);
        // id为null优先判断，返回参数错误
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }
}