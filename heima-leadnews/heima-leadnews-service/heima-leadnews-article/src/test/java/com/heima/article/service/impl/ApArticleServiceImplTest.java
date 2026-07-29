package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.heima.article.mapper.*;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.mess.ArticleVisitStreamMess;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章服务测试")
class ApArticleServiceImplTest {

    @Mock
    private ApArticleMapper apArticleMapper;

    @Mock
    private ApArticleConfigMapper apArticleConfigMapper;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @Mock
    private ArticleFreemarkerService articleFreemarkerService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private ApArticleEventMapper apArticleEventMapper;

    @InjectMocks
    private ApArticleServiceImpl apArticleService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ApArticle.class);
        TableInfoHelper.initTableInfo(assistant, ApArticleContent.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apArticleService, "baseMapper", apArticleMapper);
    }

    // ==================== load ====================

    @Test
    @DisplayName("加载文章列表 - 默认参数")
    void testLoad_DefaultParams() {
        ArticleHomeDto dto = new ArticleHomeDto();
        Short type = 1;

        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        article.setViews(0);
        article.setLikes(0);
        article.setComment(0);
        article.setCollection(0);

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.singletonList(article));

        ResponseResult result = apArticleService.load(dto, type);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();
        assertNotNull(data);
        assertEquals(1, data.size());
    }

    @Test
    @DisplayName("加载文章列表 - 空列表返回空数组")
    void testLoad_EmptyList() {
        ArticleHomeDto dto = new ArticleHomeDto();
        Short type = 1;

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.emptyList());

        ResponseResult result = apArticleService.load(dto, type);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("加载文章列表 - size超过50限制")
    void testLoad_SizeExceeded() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(100);
        Short type = 1;

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.emptyList());

        ResponseResult result = apArticleService.load(dto, type);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        // size应该被限制为50
        assertEquals(50, dto.getSize().intValue());
    }

    // ==================== saveArticle ====================

    @Test
    @DisplayName("保存文章 - dto为null返回参数错误")
    void testSaveArticle_NullDto() {
        ResponseResult result = apArticleService.saveArticle(null, 0);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("保存文章 - 新建文章")
    void testSaveArticle_New() {
        ArticleDto dto = new ArticleDto();
        dto.setTitle("新文章");
        dto.setContent("文章内容");

        when(apArticleMapper.insert(any(ApArticle.class))).thenAnswer(inv -> {
            ApArticle article = inv.getArgument(0);
            article.setId(1001L);
            return 1;
        });
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);

        ResponseResult result = apArticleService.saveArticle(dto, 0);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1001L, result.getData());
    }

    @Test
    @DisplayName("保存文章 - 更新已有文章")
    void testSaveArticle_Update() {
        ArticleDto dto = new ArticleDto();
        dto.setId(1001L);
        dto.setTitle("更新文章");
        dto.setContent("更新内容");

        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);
        when(apArticleContentMapper.update(any(ApArticleContent.class), any(QueryWrapper.class))).thenReturn(1);

        ResponseResult result = apArticleService.saveArticle(dto, 0);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1001L, result.getData());
    }

    // ==================== generateArticleEvent ====================

    @Test
    @DisplayName("生成文章事件 - article为null返回false")
    void testGenerateArticleEvent_NullArticle() {
        boolean result = apArticleService.generateArticleEvent(null, 0);

        assertFalse(result);
    }

    @Test
    @DisplayName("生成文章事件 - 文章不存在返回false")
    void testGenerateArticleEvent_ArticleNotFound() {
        ApArticle article = new ApArticle();
        article.setId(999L);

        when(apArticleMapper.selectById(999L)).thenReturn(null);

        boolean result = apArticleService.generateArticleEvent(article, 0);

        assertFalse(result);
        verify(apArticleMapper, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("生成文章事件 - 成功")
    void testGenerateArticleEvent_Success() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");

        when(apArticleMapper.selectById(1001L)).thenReturn(article);

        boolean result = apArticleService.generateArticleEvent(article, 0);

        assertTrue(result);
        verify(apArticleEventMapper, times(1)).insertArticleEvent(any(ArticleEvent.class));
    }

    // ==================== updateScore ====================

    @Test
    @DisplayName("更新文章分数 - 文章不存在记录警告日志")
    void testUpdateScore_ArticleNotFound() {
        ArticleVisitStreamMess message = new ArticleVisitStreamMess();
        message.setArticleId(999L);
        message.setCollect(1);
        message.setComment(1);
        message.setLike(1);
        message.setView(10);

        when(apArticleMapper.selectById(999L)).thenReturn(null);

        apArticleService.updateScore(message);

        verify(apArticleMapper, never()).updateById(any(ApArticle.class));
    }

    @Test
    @DisplayName("更新文章分数 - 正常更新")
    void testUpdateScore_Success() {
        ArticleVisitStreamMess message = new ArticleVisitStreamMess();
        message.setArticleId(1001L);
        message.setCollect(1);
        message.setComment(1);
        message.setLike(1);
        message.setView(10);

        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setViews(100);
        article.setLikes(10);
        article.setComment(5);
        article.setCollection(2);

        when(apArticleMapper.selectById(1001L)).thenReturn(article);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        apArticleService.updateScore(message);

        // updateArticle 调用一次 getById + updateById, updateScore 再调用一次 updateById
        verify(apArticleMapper, atLeast(2)).selectById(1001L);
        verify(apArticleMapper, atLeast(2)).updateById(any(ApArticle.class));
    }

    // ==================== listByAuthorId ====================

    @Test
    @DisplayName("根据作者ID查询文章列表 - 基本查询")
    void testListByAuthorId_ByAuthorId() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);

        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("作者文章");
        article.setViews(0);
        article.setLikes(0);
        article.setComment(0);
        article.setCollection(0);

        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(article));

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("作者文章", result.get(0).get("title"));
    }

    @Test
    @DisplayName("根据作者ID查询文章列表 - 带标签筛选")
    void testListByAuthorId_WithTags() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);
        dto.setTags(Arrays.asList("44", "45"));

        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("根据作者ID查询文章列表 - 带频道ID筛选")
    void testListByAuthorId_WithChannelId() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);
        dto.setChannelId(2);

        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}