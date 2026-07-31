package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleDraftMapper;
import com.heima.model.article.pojos.ApArticleDraft;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftManageServiceImplTest {

    @Mock
    private ApArticleDraftMapper apArticleDraftMapper;

    @InjectMocks
    private DraftManageServiceImpl draftManageService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        ReflectionTestUtils.setField(draftManageService, "baseMapper", apArticleDraftMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== list() tests ====================

    @Test
    void testListSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = buildDraft(1L);
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticleDraft>(1, 10, 1) {{
                    setRecords(Collections.singletonList(draft));
                }});

        ResponseResult result = draftManageService.list(null, 1, 10, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testListNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.list(null, 1, 10, null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testListWithTitleFilter() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticleDraft>(1, 10, 1));

        ResponseResult result = draftManageService.list(null, 1, 10, "testTitle");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithAuthorId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticleDraft>(1, 10, 0));

        ResponseResult result = draftManageService.list(2L, 1, 10, null);

        assertEquals(200, result.getCode());
    }

    // ==================== deleteDraft() tests ====================

    @Test
    void testDeleteDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = buildDraft(1L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertEquals(200, result.getCode());
        assertTrue(draft.getIsDeleted());
    }

    @Test
    void testDeleteDraftNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testDeleteDraftNullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ResponseResult result = draftManageService.deleteDraft(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testDeleteDraftNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeleteDraftNotOwner() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = buildDraft(1L);
        draft.setAuthorId(2L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);

        ResponseResult result = draftManageService.deleteDraft(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== addDraft() tests ====================

    @Test
    void testAddDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("test title");
        draft.setContent("test content");
        when(apArticleDraftMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);

        ResponseResult result = draftManageService.addDraft(draft);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testAddDraftNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("test");

        ResponseResult result = draftManageService.addDraft(draft);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testAddDraftExceedsMax() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        List<ApArticleDraft> existingDrafts = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ApArticleDraft d = buildDraft((long) (i + 1));
            d.setUpdatedTime(new Date(System.currentTimeMillis() - (50 - i) * 1000L));
            existingDrafts.add(d);
        }
        when(apArticleDraftMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(existingDrafts);
        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("new draft");
        draft.setContent("new content");

        ResponseResult result = draftManageService.addDraft(draft);

        assertEquals(200, result.getCode());
        verify(apArticleDraftMapper, atLeastOnce()).updateById(any(ApArticleDraft.class));
    }

    // ==================== updateDraft() tests ====================

    @Test
    void testUpdateDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft existing = buildDraft(1L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("updated title");

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testUpdateDraftNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftNoId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(null);

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftDeleted() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft existing = buildDraft(1L);
        existing.setIsDeleted(true);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftNotOwner() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft existing = buildDraft(1L);
        existing.setAuthorId(2L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);

        ResponseResult result = draftManageService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== Helper ====================

    private ApArticleDraft buildDraft(Long id) {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(id);
        draft.setAuthorId(1L);
        draft.setTitle("title " + id);
        draft.setContent("content " + id);
        draft.setIsDeleted(false);
        draft.setCreatedTime(new Date());
        draft.setUpdatedTime(new Date());
        return draft;
    }
}