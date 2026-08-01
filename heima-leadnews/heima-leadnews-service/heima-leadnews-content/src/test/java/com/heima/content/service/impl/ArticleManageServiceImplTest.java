package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.content.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
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
class ArticleManageServiceImplTest {

    @Mock
    private ApArticleMapper apArticleMapper;

    @InjectMocks
    private ArticleManageServiceImpl articleManageService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        ReflectionTestUtils.setField(articleManageService, "baseMapper", apArticleMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== list() tests ====================

    @Test
    void testListSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticle article = buildArticle(1L);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 1) {{
                    setRecords(Collections.singletonList(article));
                }});

        ResponseResult result = articleManageService.list(null, 1, 10, null, null);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testListNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.list(null, 1, 10, null, null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testListWithStatusPublished() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(null, 1, 10, "published", null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithStatusReviewing() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(null, 1, 10, "reviewing", null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithStatusRejected() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(null, 1, 10, "rejected", null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithStatusUnknown() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(null, 1, 10, "unknown", null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithTitleFilter() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(null, 1, 10, null, "testTitle");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithAuthorId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApArticle>(1, 10, 0));

        ResponseResult result = articleManageService.list(2L, 1, 10, null, null);

        assertEquals(200, result.getCode());
    }

    // ==================== statistics() tests ====================

    @Test
    void testStatisticsSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 5L, 3L, 2L);

        ResponseResult result = articleManageService.statistics(null);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(10L, data.get("total"));
        assertEquals(5L, data.get("published"));
        assertEquals(3L, data.get("reviewing"));
        assertEquals(2L, data.get("rejected"));
    }

    @Test
    void testStatisticsNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.statistics(null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testStatisticsWithAuthorId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L, 2L, 1L, 0L);

        ResponseResult result = articleManageService.statistics(2L);

        assertEquals(200, result.getCode());
    }

    // ==================== deleteArticle() tests ====================

    @Test
    void testDeleteArticleSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticle article = buildArticle(1L);
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertEquals(200, result.getCode());
        assertTrue(article.getIsDeleted());
    }

    @Test
    void testDeleteArticleNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testDeleteArticleNullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ResponseResult result = articleManageService.deleteArticle(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testDeleteArticleNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apArticleMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeleteArticleNotOwner() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApArticle article = buildArticle(1L);
        article.setAuthorId(2L);
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== Helper ====================

    private ApArticle buildArticle(Long id) {
        ApArticle article = new ApArticle();
        article.setId(id);
        article.setAuthorId(1L);
        article.setTitle("title " + id);
        article.setStatus(ApArticle.Status.PUBLISHED.getCode());
        article.setIsDeleted(false);
        return article;
    }
}