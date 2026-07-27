package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleDraftMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleAutoScanService;
import com.heima.article.service.ArticleTaskService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
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

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章草稿服务测试")
class ApArticleDraftServiceImplTest {

    @Mock
    private ApArticleDraftMapper apArticleDraftMapper;

    @Mock
    private ApArticleMapper apArticleMapper;

    @Mock
    private ApArticleConfigMapper apArticleConfigMapper;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @Mock
    private ArticleAutoScanService articleAutoScanService;

    @Mock
    private ArticleTaskService articleTaskService;

    @InjectMocks
    private ApArticleDraftServiceImpl apArticleDraftService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMockedStatic;
    private ApUser testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apArticleDraftService, "baseMapper", apArticleDraftMapper);
        testUser = new ApUser();
        testUser.setId(1);
        testUser.setNickname("测试用户");
        testUser.setImage("https://example.com/avatar.jpg");
        appThreadLocalUtilMockedStatic = mockStatic(AppThreadLocalUtil.class);
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        appThreadLocalUtilMockedStatic.close();
    }

    // ==================== createDraft ====================

    @Test
    @DisplayName("创建草稿 - 创建成功")
    void testCreateDraft_Success() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("测试草稿");
        draft.setContent("这是测试内容");

        when(apArticleDraftMapper.insert(any(ApArticleDraft.class))).thenReturn(1);

        ResponseResult result = apArticleDraftService.createDraft(draft);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(draft.getAuthorId());
        assertEquals(1L, draft.getAuthorId());
    }

    // ==================== updateDraft ====================

    @Test
    @DisplayName("更新草稿 - draftId为null返回参数错误")
    void testUpdateDraft_NullId() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setTitle("更新标题");

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - 草稿不存在返回数据不存在")
    void testUpdateDraft_NotFound() {
        when(apArticleDraftMapper.selectById(999L)).thenReturn(null);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(999L);
        draft.setTitle("更新标题");

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新草稿 - 更新成功")
    void testUpdateDraft_Success() {
        ApArticleDraft existing = new ApArticleDraft();
        existing.setId(1L);
        existing.setAuthorId(1L);
        existing.setTitle("原标题");
        when(apArticleDraftMapper.selectById(1L)).thenReturn(existing);
        when(apArticleDraftMapper.updateById(any(ApArticleDraft.class))).thenReturn(1);

        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("新标题");
        draft.setContent("新内容");

        ResponseResult result = apArticleDraftService.updateDraft(draft);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== publishFromDraft ====================

    @Test
    @DisplayName("从草稿发布文章 - draftId为null返回参数错误")
    void testPublishFromDraft_NullId() {
        ResponseResult result = apArticleDraftService.publishFromDraft(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("从草稿发布文章 - 草稿不存在返回数据不存在")
    void testPublishFromDraft_NotFound() {
        when(apArticleDraftMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = apArticleDraftService.publishFromDraft(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("从草稿发布文章 - 发布成功")
    void testPublishFromDraft_Success() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("测试文章");
        draft.setContent("测试内容");
        draft.setChannelId(1);
        draft.setAuthorId(1L);
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);
        when(apArticleMapper.insert(any(ApArticle.class))).thenAnswer(inv -> {
            ApArticle article = inv.getArgument(0);
            article.setId(1001L);
            return 1;
        });
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);
        doNothing().when(articleAutoScanService).autoScanArticle(anyLong());
        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any(Date.class));

        ResponseResult result = apArticleDraftService.publishFromDraft(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(apArticleMapper, times(1)).insert(any(ApArticle.class));
        verify(articleTaskService, times(1)).addArticleToTask(anyLong(), any(Date.class));
    }

    // ==================== getDraftById ====================

    @Test
    @DisplayName("根据ID获取草稿 - 草稿不存在返回数据不存在")
    void testGetDraftById_NotFound() {
        when(apArticleDraftMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = apArticleDraftService.getDraftById(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("根据ID获取草稿 - 获取成功")
    void testGetDraftById_Success() {
        ApArticleDraft draft = new ApArticleDraft();
        draft.setId(1L);
        draft.setTitle("测试草稿");
        when(apArticleDraftMapper.selectById(1L)).thenReturn(draft);

        ResponseResult result = apArticleDraftService.getDraftById(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    // ==================== listDrafts ====================

    @Test
    @DisplayName("查询草稿列表 - 按作者ID查询成功")
    void testListDrafts_Success() {
        Page<ApArticleDraft> pageParam = new Page<>(1, 10);
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticleDraft> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = apArticleDraftService.listDrafts(1L, 1, 10);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询草稿列表 - authorId为null不筛选")
    void testListDrafts_NullAuthorId() {
        Page<ApArticleDraft> pageParam = new Page<>(1, 10);
        when(apArticleDraftMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticleDraft> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = apArticleDraftService.listDrafts(null, 1, 10);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== deleteDraft ====================

    @Test
    @DisplayName("删除草稿 - 删除成功")
    void testDeleteDraft_Success() {
        when(apArticleDraftMapper.deleteById(1L)).thenReturn(1);

        ResponseResult result = apArticleDraftService.deleteDraft(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}