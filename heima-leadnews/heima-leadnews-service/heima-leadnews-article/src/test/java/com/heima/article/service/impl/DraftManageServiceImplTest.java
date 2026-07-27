package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleDraftMapper;
import com.heima.model.article.pojos.ApArticleDraft;
import com.heima.model.common.dtos.ResponseResult;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("草稿管理服务测试")
class DraftManageServiceImplTest {

    @Mock
    private ApArticleDraftMapper apArticleDraftMapper;

    @InjectMocks
    private DraftManageServiceImpl draftManageService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMockedStatic;
    private ApUser testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(draftManageService, "baseMapper", apArticleDraftMapper);
        testUser = new ApUser();
        testUser.setId(1);
        appThreadLocalUtilMockedStatic = mockStatic(AppThreadLocalUtil.class);
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        appThreadLocalUtilMockedStatic.close();
    }

    // ==================== list ====================

    @Test
    @DisplayName("查询草稿列表 - 用户未登录返回需要登录")
    void testList_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.list(null, 1, 10, null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询草稿列表 - 使用当前用户查询成功")
    void testList_UseCurrentUser() {
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticleDraft> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = draftManageService.list(null, 1, 10, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询草稿列表 - 按标题模糊搜索")
    void testList_WithTitleFilter() {
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticleDraft> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = draftManageService.list(1L, 1, 10, "测试");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== deleteDraft ====================

    @Test
    @DisplayName("删除草稿 - 用户未登录返回需要登录")
    void testDeleteDraft_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除草稿 - id为null返回参数错误")
    void testDeleteDraft_NullId() {
        ResponseResult result = draftManageService.deleteDraft(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除草稿 - 草稿不存在返回数据不存在")
    void testDeleteDraft_NotFound() {
        when(apArticleDraftMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = draftManageService.deleteDraft(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除草稿 - 不是作者无权限删除返回数据不存在")
    void testDeleteDraft_NotAuthor() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setAuthorId(2L);
        draft.setIsDeleted(false);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除草稿 - 删除成功设置逻辑删除")
    void testDeleteDraft_Success() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setAuthorId(1L);
        draft.setIsDeleted(false);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(draft.getIsDeleted());
    }

    // ==================== addDraft ====================

    @Test
    @DisplayName("添加草稿 - 用户未登录返回需要登录")
    void testAddDraft_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.addDraft(new ApArticleDraft());

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("添加草稿 - 添加成功")
    void testAddDraft_Success() {
        when(apArticleDraftMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("测试草稿");
        ResponseResult result = draftManageService.addDraft(draft);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1L, draft.getAuthorId());
        assertFalse(draft.getIsDeleted());
    }

    @Test
    @DisplayName("添加草稿 - 超过最大数量自动删除最早草稿")
    void testAddDraft_ExceedMaxLimit() {
        List<ApArticleDraft> drafts = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ApArticleDraft d = new ApArticleDraft();
            d.setId((long) i);
            d.setAuthorId(1L);
            d.setIsDeleted(false);
            drafts.add(d);
        }
        when(apArticleDraftMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(drafts);
        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("新增草稿");
        ResponseResult result = draftManageService.addDraft(draft);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(apArticleDraftMapper, times(1)).updateById(any(ApArticleDraft.class));
    }

    // ==================== updateDraft ====================

    @Test
    @DisplayName("更新草稿 - 用户未登录返回需要登录")
    void testUpdateDraft_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.updateDraft(new ApArticleDraft());

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - id为null返回参数错误")
    void testUpdateDraft_NullId() {
        ResponseResult result = draftManageService.updateDraft(new ApArticleDraft());

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - 草稿不存在返回数据不存在")
    void testUpdateDraft_NotFound() {
        when(apArticleDraftMapper.selectById(999L)).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(999L);
        ResponseResult result = draftManageService.updateDraft(draft);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - 不是作者无权限更新返回数据不存在")
    void testUpdateDraft_NotAuthor() {
        ApArticleDraft existing = new ApArticleDraft();
        existing.setId(1L);
        existing.setAuthorId(2L);
        existing.setIsDeleted(false);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("新标题");
        ResponseResult result = draftManageService.updateDraft(draft);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - 更新成功")
    void testUpdateDraft_Success() {
        ApArticleDraft existing = new ApArticleDraft();
        existing.setId(1L);
        existing.setAuthorId(1L);
        existing.setIsDeleted(false);
        existing.setTitle("原标题");
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("新标题");
        ResponseResult result = draftManageService.updateDraft(draft);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}