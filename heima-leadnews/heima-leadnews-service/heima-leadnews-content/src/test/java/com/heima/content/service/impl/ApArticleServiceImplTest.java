package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.content.mapper.ApArticleConfigMapper;
import com.heima.content.mapper.ApArticleContentMapper;
import com.heima.content.mapper.ApArticleEventMapper;
import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apArticleService, "baseMapper", apArticleMapper);
    }

    // ==================== load() tests ====================

    @Test
    void testLoadSuccess() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");
        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.singletonList(article));

        ResponseResult result = apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testLoadWithNullSize() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(null);
        dto.setTag(null);
        dto.setMaxBehotTime(null);
        dto.setMinBehotTime(null);

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.emptyList());

        ResponseResult result = apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);

        assertEquals(200, result.getCode());
        assertEquals(10, dto.getSize().intValue());
        assertEquals(ArticleConstants.DEFAULT_TAG, dto.getTag());
        assertNotNull(dto.getMaxBehotTime());
        assertNotNull(dto.getMinBehotTime());
    }

    @Test
    void testLoadWithSizeExceedsMax() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(100);
        dto.setTag("test");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.emptyList());

        ResponseResult result = apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);

        assertEquals(200, result.getCode());
        assertEquals(50, dto.getSize().intValue());
    }

    @Test
    void testLoadWithInvalidType() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag("test");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        when(apArticleMapper.loadArticleList(any(ArticleHomeDto.class), any(Short.class)))
                .thenReturn(Collections.emptyList());

        ResponseResult result = apArticleService.load(dto, (short) 3);

        assertEquals(200, result.getCode());
    }

    // ==================== saveArticle() tests ====================

    @Test
    void testSaveArticleNew() {
        ArticleDto dto = new ArticleDto();
        dto.setTitle("new article");
        dto.setContent("content");
        dto.setAuthorId(1L);
        dto.setChannelId(1);

        doAnswer(invocation -> {
            ApArticle article = invocation.getArgument(0);
            article.setId(1L);
            return 1;
        }).when(apArticleMapper).insert(any(ApArticle.class));
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);
        doNothing().when(apArticleEventMapper).insertArticleEvent(any(ArticleEvent.class));

        ResponseResult result = apArticleService.saveArticle(dto, 0);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData());
        verify(articleFreemarkerService).buildHTMLAndSend(any(ApArticle.class), anyString(), longThat(x -> true));
    }

    @Test
    void testSaveArticleUpdate() {
        ArticleDto dto = new ArticleDto();
        dto.setId(1L);
        dto.setTitle("updated article");
        dto.setContent("updated content");
        dto.setAuthorId(1L);

        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);
        when(apArticleContentMapper.update(any(ApArticleContent.class), any(QueryWrapper.class))).thenReturn(1);
        doNothing().when(apArticleEventMapper).insertArticleEvent(any(ArticleEvent.class));

        ResponseResult result = apArticleService.saveArticle(dto, 1000);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(articleFreemarkerService).buildHTMLAndSend(any(ApArticle.class), anyString(), longThat(x -> true));
    }

    @Test
    void testSaveArticleNullDto() {
        ResponseResult result = apArticleService.saveArticle(null, 0);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== generateArticleEvent() tests ====================

    @Test
    void testGenerateArticleEventSuccess() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(apArticleMapper.selectById(1L)).thenReturn(article);
        doNothing().when(apArticleEventMapper).insertArticleEvent(any(ArticleEvent.class));

        boolean result = apArticleService.generateArticleEvent(article, 0);

        assertTrue(result);
        verify(articleFreemarkerService).buildHTMLAndSend(any(ApArticle.class), anyString(), longThat(x -> true));
    }

    @Test
    void testGenerateArticleEventNullArticle() {
        boolean result = apArticleService.generateArticleEvent(null, 0);

        assertFalse(result);
    }

    @Test
    void testGenerateArticleEventArticleNotFound() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        when(apArticleMapper.selectById(1L)).thenReturn(null);

        boolean result = apArticleService.generateArticleEvent(article, 0);

        assertFalse(result);
    }

    @Test
    void testGenerateArticleEventException() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        when(apArticleMapper.selectById(1L)).thenReturn(article);
        doThrow(new RuntimeException("DB error")).when(apArticleEventMapper).insertArticleEvent(any(ArticleEvent.class));

        boolean result = apArticleService.generateArticleEvent(article, 0);

        assertFalse(result);
    }

    // ==================== updateScore() tests ====================

    @Test
    void testUpdateScoreSuccess() {
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(1L);
        mess.setView(10);
        mess.setLike(5);
        mess.setCollect(2);
        mess.setComment(1);

        ApArticle existingArticle = new ApArticle();
        existingArticle.setId(1L);
        existingArticle.setViews(100);
        existingArticle.setLikes(50);
        existingArticle.setCollection(20);
        existingArticle.setComment(10);
        existingArticle.setScore(200);

        when(apArticleMapper.selectById(1L)).thenReturn(existingArticle);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        apArticleService.updateScore(mess);

        verify(apArticleMapper, times(2)).updateById(any(ApArticle.class));
    }

    @Test
    void testUpdateScoreArticleNotFound() {
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(1L);

        when(apArticleMapper.selectById(1L)).thenReturn(null);

        apArticleService.updateScore(mess);

        verify(apArticleMapper, never()).updateById(any(ApArticle.class));
    }

    @Test
    void testUpdateScoreWithNullCounts() {
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(1L);
        mess.setView(5);
        mess.setLike(3);

        ApArticle existingArticle = new ApArticle();
        existingArticle.setId(1L);
        existingArticle.setViews(null);
        existingArticle.setLikes(null);
        existingArticle.setCollection(null);
        existingArticle.setComment(null);

        when(apArticleMapper.selectById(1L)).thenReturn(existingArticle);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        apArticleService.updateScore(mess);

        verify(apArticleMapper, times(2)).updateById(any(ApArticle.class));
    }

    // ==================== listByAuthorId() tests ====================

    @Test
    void testListByAuthorIdSuccess() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);
        dto.setChannelId(1);
        dto.setTags(Arrays.asList("tag1", "tag2"));
        dto.setIsDeleted(false);

        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");
        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(article));

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListByAuthorIdWithNullTags() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);

        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testListByAuthorIdWithEmptyTags() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);
        dto.setTags(new ArrayList<>());

        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = apArticleService.listByAuthorId(dto);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}