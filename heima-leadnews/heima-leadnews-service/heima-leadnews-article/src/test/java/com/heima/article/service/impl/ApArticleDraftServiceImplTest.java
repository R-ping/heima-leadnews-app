package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleDraftMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleAutoScanService;
import com.heima.model.article.pojos.*;
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

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApArticleDraftServiceImplTest {

    @Mock
    private ApArticleMapper apArticleMapper;

    @Mock
    private ApArticleConfigMapper apArticleConfigMapper;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @Mock
    private ApArticleDraftMapper apArticleDraftMapper;

    @Mock
    private ArticleAutoScanService articleAutoScanService;

    @InjectMocks
    private ApArticleDraftServiceImpl apArticleDraftService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        mockUser.setImage("https://avatar.jpg");
        ReflectionTestUtils.setField(apArticleDraftService, "baseMapper", apArticleDraftMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== createDraft() tests ====================

    @Test
    void testCreateDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("test title");
        draft.setContent("test content");

        ResponseResult result = apArticleDraftService.createDraft(draft);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    // ==================== updateDraft() tests ====================

    @Test
    void testUpdateDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft existing = new ApArticleDraft();
        existing.setId(1L);
        existing.setAuthorId(1L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("updated title");

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testUpdateDraftNullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(null);

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testUpdateDraftWithAuthorId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft existing = new ApArticleDraft();
        existing.setId(1L);
        existing.setAuthorId(1L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setAuthorId(2L);
        draft.setTitle("title");

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertEquals(200, result.getCode());
    }

    // ==================== publishFromDraft() tests ====================

    @Test
    void testPublishFromDraftSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("published title");
        draft.setAuthorId(1L);
        draft.setChannelId(1);
        draft.setLayout((short) 1);
        draft.setCoverImage("https://cover.jpg");
        draft.setContent("published content");
        draft.setPublishTime(new Date());

        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);
        doAnswer(invocation -> {
            ApArticle article = invocation.getArgument(0);
            article.setId(100L);
            return 1;
        }).when(apArticleMapper).insert(any(ApArticle.class));
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);
        when(articleAutoScanService.autoScanArticle(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(true));

        ResponseResult result = apArticleDraftService.publishFromDraft(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(apArticleDraftMapper).deleteById(1L);
        verify(articleAutoScanService).autoScanArticle(anyLong());
    }

    @Test
    void testPublishFromDraftNullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ResponseResult result = apArticleDraftService.publishFromDraft(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishFromDraftNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = apArticleDraftService.publishFromDraft(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testPublishFromDraftWithNullFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("title");
        draft.setContent("content");

        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);
        doAnswer(invocation -> {
            ApArticle article = invocation.getArgument(0);
            article.setId(100L);
            return 1;
        }).when(apArticleMapper).insert(any(ApArticle.class));
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);
        when(articleAutoScanService.autoScanArticle(anyLong()))
                .thenReturn(CompletableFuture.completedFuture(true));

        ResponseResult result = apArticleDraftService.publishFromDraft(1L);

        assertEquals(200, result.getCode());
    }

    // ==================== getDraftById() tests ====================

    @Test
    void testGetDraftByIdSuccess() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("test");
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);

        ResponseResult result = apArticleDraftService.getDraftById(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testGetDraftByIdNotFound() {
        when(apArticleDraftMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = apArticleDraftService.getDraftById(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== listDrafts() tests ====================

    @Test
    void testListDraftsSuccess() {
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticleDraft>(1, 10, 1));

        ResponseResult result = apArticleDraftService.listDrafts(1L, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testListDraftsWithoutAuthorId() {
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticleDraft>(1, 10, 0));

        ResponseResult result = apArticleDraftService.listDrafts(null, 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== deleteDraft() tests ====================

    @Test
    void testDeleteDraftSuccess() {
        when(apArticleDraftMapper.deleteById(1L)).thenReturn(1);

        ResponseResult result = apArticleDraftService.deleteDraft(1L);

        assertEquals(200, result.getCode());
    }
}