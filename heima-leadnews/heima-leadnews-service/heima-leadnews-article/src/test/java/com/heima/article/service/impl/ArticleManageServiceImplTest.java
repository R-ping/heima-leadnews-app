package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章管理服务测试")
class ArticleManageServiceImplTest {

    @Mock
    private ApArticleMapper apArticleMapper;

    @InjectMocks
    private ArticleManageServiceImpl articleManageService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMockedStatic;
    private ApUser testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleManageService, "baseMapper", apArticleMapper);
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
    @DisplayName("查询文章列表 - 用户未登录返回需要登录")
    void testList_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.list(null, 1, 10, null, null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询文章列表 - 使用当前登录用户作为作者")
    void testList_UseCurrentUser() {
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticle> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = articleManageService.list(null, 1, 10, null, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询文章列表 - 指定作者ID查询成功")
    void testList_SpecifiedAuthor() {
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticle> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = articleManageService.list(2L, 1, 10, null, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询文章列表 - 按状态筛选published")
    void testList_FilterByStatusPublished() {
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticle> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = articleManageService.list(1L, 1, 10, "published", null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询文章列表 - 按标题模糊搜索")
    void testList_FilterByTitle() {
        when(apArticleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApArticle> p = inv.getArgument(0);
            p.setRecords(Collections.emptyList());
            p.setTotal(0);
            return p;
        });

        ResponseResult result = articleManageService.list(1L, 1, 10, null, "Spring");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== statistics ====================

    @Test
    @DisplayName("统计文章数据 - 用户未登录返回需要登录")
    void testStatistics_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.statistics(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("统计文章数据 - 统计成功返回各项计数")
    void testStatistics_Success() {
        when(apArticleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        ResponseResult result = articleManageService.statistics(null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("total"));
        assertTrue(data.containsKey("published"));
        assertTrue(data.containsKey("reviewing"));
        assertTrue(data.containsKey("rejected"));
    }

    // ==================== deleteArticle ====================

    @Test
    @DisplayName("删除文章 - 用户未登录返回需要登录")
    void testDeleteArticle_NeedLogin() {
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除文章 - id为null返回参数错误")
    void testDeleteArticle_NullId() {
        ResponseResult result = articleManageService.deleteArticle(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除文章 - 文章不存在返回数据不存在")
    void testDeleteArticle_NotFound() {
        when(apArticleMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = articleManageService.deleteArticle(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除文章 - 不是作者无权限删除返回数据不存在")
    void testDeleteArticle_NotAuthor() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setAuthorId(2L);
        article.setIsDeleted(false);
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除文章 - 删除成功设置逻辑删除")
    void testDeleteArticle_Success() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setAuthorId(1L);
        article.setIsDeleted(false);
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        ResponseResult result = articleManageService.deleteArticle(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(article.getIsDeleted());
    }
}