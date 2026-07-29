package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleAuditRecordMapper;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleSimilarityService;
import com.heima.article.service.ArticleTaskService;
import com.heima.article.service.BailianAiService;
import com.heima.article.service.LevelService;
import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.common.aliyun.GreenTextScanPlus;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleAutoScanServiceImplTest {

    @Mock
    private ApArticleMapper apArticleMapper;
    @Mock
    private ApArticleContentMapper apArticleContentMapper;
    @Mock
    private ApArticleConfigMapper apArticleConfigMapper;
    @Mock
    private GreenImageScanPlus greenImageScan;
    @Mock
    private GreenTextScanPlus greenTextScan;
    @Mock
    private BailianAiService bailianAiService;
    @Mock
    private ArticleSimilarityService articleSimilarityService;
    @Mock
    private LevelService levelService;
    @Mock
    private ArticleTaskService articleTaskService;
    @Mock
    private ApArticleAuditRecordMapper apArticleAuditRecordMapper;

    @InjectMocks
    private ArticleAutoScanServiceImpl articleAutoScanService;

    private ApArticle article;
    private ApArticleContent articleContent;

    @BeforeEach
    void setUp() {
        article = new ApArticle();
        article.setId(1L);
        article.setTitle("测试文章");
        article.setAuthorId(100L);
        article.setStatus(ApArticle.Status.SUBMIT.getCode());
        article.setPublishTime(new Date());
        article.setIsDeleted(false);

        articleContent = new ApArticleContent();
        articleContent.setArticleId(1L);
        articleContent.setContent("测试内容");
    }

    @Test
    void testTextScanHigh_ShouldSaveAuditRecordAndNotSetIsDeleted() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> textScanResult = new HashMap<>();
        textScanResult.put("level", "high");
        when(greenTextScan.greeTextScan(anyString())).thenReturn(textScanResult);

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then
        // 验证审计记录已写入
        verify(apArticleAuditRecordMapper, times(1)).insert(any());
        // 验证文章状态更新为 FAIL
        verify(apArticleMapper, times(1)).updateById(argThat(a ->
            a.getStatus().equals(ApArticle.Status.FAIL.getCode())
        ));
        // 验证 isDeleted 仍为 false
        verify(apArticleMapper, times(1)).updateById(argThat(a ->
            !a.getIsDeleted()
        ));
    }

    @Test
    void testTextScanMedium_ShouldSaveAuditRecord() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> textScanResult = new HashMap<>();
        textScanResult.put("level", "medium");
        when(greenTextScan.greeTextScan(anyString())).thenReturn(textScanResult);

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then
        verify(apArticleAuditRecordMapper, times(1)).insert(any());
        verify(apArticleMapper, times(1)).updateById(argThat(a ->
            a.getStatus().equals(ApArticle.Status.FAIL.getCode()) && !a.getIsDeleted()
        ));
    }

    @Test
    void testAuditPass_ShouldCallExperiencePoints() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> textScanResult = new HashMap<>();
        textScanResult.put("level", "normal");
        when(greenTextScan.greeTextScan(anyString())).thenReturn(textScanResult);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 10);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString())).thenReturn(expResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then
        verify(levelService, times(1)).recordActionWithLimit(eq(100L), eq("publish_article"), anyString());
    }

    @Test
    void testExperiencePointsException_ShouldNotAffectAuditFlow() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> textScanResult = new HashMap<>();
        textScanResult.put("level", "normal");
        when(greenTextScan.greeTextScan(anyString())).thenReturn(textScanResult);

        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
            .thenThrow(new RuntimeException("经验值服务异常"));

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        // When - should not throw exception
        articleAutoScanService.autoScanArticle(1L);

        // Then - task scheduling should still be called
        verify(articleTaskService, times(1)).addArticleToTask(anyLong(), any());
    }

    @Test
    void testArticleNotInSubmitStatus_ShouldSkipAudit() {
        // Given
        article.setStatus(ApArticle.Status.PUBLISHED.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then - no audit actions should be called
        verify(greenTextScan, never()).greeTextScan(anyString());
        verify(apArticleAuditRecordMapper, never()).insert(any());
    }
}