package com.heima.content.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.content.mapper.ApArticleAiAnalysisMapper;
import com.heima.common.bailian.DashScopeClient;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleAiAnalysis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BailianAiServiceImplTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private ApArticleAiAnalysisMapper aiAnalysisMapper;

    @InjectMocks
    private BailianAiServiceImpl bailianAiService;

    // ==================== analyzeArticle() tests ====================

    @Test
    void testAnalyzeArticleSuccess() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test Article Title");

        String titleResponse = "{\"score\": 85, \"reason\": \"标题相关\"}";
        String qualityResponse = "{\"quality_score\": 80, \"originality_score\": 75, \"logic_score\": 85, \"clarity_score\": 80, \"comment\": \"质量不错\"}";
        String techResponse = "{\"is_tech\": true, \"confidence\": 0.95, \"reason\": \"技术文章\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(titleResponse, qualityResponse, techResponse);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "test content for analysis");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(80, result.get("qualityScore"));
        assertTrue((Boolean) result.get("isTechContent"));
    }

    @Test
    void testAnalyzeArticleEmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "");

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void testAnalyzeArticleNullContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, null);

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void testAnalyzeArticleWithNullTitleResponses() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle(null);

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(null);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAnalyzeArticleWithPartialResponses() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String titleResponse = "{\"score\": 90, \"reason\": \"good\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(titleResponse, null, null);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAnalyzeArticleException() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenThrow(new RuntimeException("API error"));

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "content");

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void testAnalyzeArticleWithLongContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String longContent = "a".repeat(5000);
        String titleResponse = "{\"score\": 70, \"reason\": \"ok\"}";
        String qualityResponse = "{\"quality_score\": 65, \"originality_score\": 60, \"logic_score\": 70, \"clarity_score\": 65, \"comment\": \"ok\"}";
        String techResponse = "{\"is_tech\": true, \"confidence\": 0.8, \"reason\": \"tech\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(titleResponse, qualityResponse, techResponse);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, longContent);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    // ==================== checkViolation() tests ====================

    @Test
    void testCheckViolationNotViolation() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String violationResponse = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, "clean content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationIsViolation() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String violationResponse = "{\"is_violation\": true, \"violation_type\": \"色情低俗\", \"violation_reason\": \"包含色情内容\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, "bad content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("is_violation"));
        assertEquals("色情低俗", result.get("violation_type"));
    }

    @Test
    void testCheckViolationEmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        Map<String, Object> result = bailianAiService.checkViolation(article, "");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationNullContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        Map<String, Object> result = bailianAiService.checkViolation(article, null);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationNullResponse() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(null);

        Map<String, Object> result = bailianAiService.checkViolation(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationException() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenThrow(new RuntimeException("API error"));

        Map<String, Object> result = bailianAiService.checkViolation(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationWithExistingAnalysis() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String violationResponse = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);

        ApArticleAiAnalysis existing = new ApArticleAiAnalysis();
        existing.setId(1L);
        existing.setArticleId(1L);
        existing.setRawResponse("old response");
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);
        when(aiAnalysisMapper.updateById(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testCheckViolationWithLongContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String longContent = "a".repeat(5000);
        String violationResponse = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, longContent);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("is_violation"));
    }

    @Test
    void testCheckViolationSaveException() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("Test");

        String violationResponse = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("DB error"));

        Map<String, Object> result = bailianAiService.checkViolation(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testCheckViolationWithNullTitle() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle(null);

        String violationResponse = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";
        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(violationResponse);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, "content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }
}