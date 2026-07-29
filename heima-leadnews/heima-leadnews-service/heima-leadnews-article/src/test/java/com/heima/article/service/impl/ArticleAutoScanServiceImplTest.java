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

import com.heima.model.article.pojos.ApArticleAuditRecord;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    void testAiViolationHigh_ShouldSaveAuditRecordAndNotSetIsDeleted() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "违规内容");
        violationResult.put("violation_reason", "文章内容违反社区规范");
        when(bailianAiService.checkViolation(any(), anyString())).thenReturn(violationResult);

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then
        // 验证审计记录已写入
        verify(apArticleAuditRecordMapper, times(1)).insert(any(ApArticleAuditRecord.class));
        verify(apArticleMapper, times(1)).updateById(argThat((ApArticle a) ->
            a.getStatus().equals(ApArticle.Status.FAIL.getCode()) && !a.getIsDeleted()
        ));
    }

    @Test
    void testAiViolationMedium_ShouldSaveAuditRecord() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "medium");
        violationResult.put("violation_reason", "不确定内容");
        when(bailianAiService.checkViolation(any(), anyString())).thenReturn(violationResult);

        // When
        articleAutoScanService.autoScanArticle(1L);

        // Then
        verify(apArticleAuditRecordMapper, times(1)).insert(any(ApArticleAuditRecord.class));
        verify(apArticleMapper, times(1)).updateById(argThat((ApArticle a) ->
            a.getStatus().equals(ApArticle.Status.FAIL.getCode()) && !a.getIsDeleted()
        ));
    }

    @Test
    void testAuditPass_ShouldCallExperiencePoints() {
        // Given
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(articleContent);

        // AI违规检测通过
        when(bailianAiService.checkViolation(any(), anyString())).thenReturn(null);

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

        // AI违规检测通过
        when(bailianAiService.checkViolation(any(), anyString())).thenReturn(null);

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
        verify(bailianAiService, never()).checkViolation(any(), anyString());
        verify(apArticleAuditRecordMapper, never()).insert(any(ApArticleAuditRecord.class));
    }
}