package com.heima.article.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleAiAnalysisMapper;
import com.heima.common.bailian.DashScopeClient;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleAiAnalysis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("百炼AI服务测试")
class BailianAiServiceImplTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private ApArticleAiAnalysisMapper aiAnalysisMapper;

    @InjectMocks
    private BailianAiServiceImpl bailianAiService;

    // ==================== analyzeArticle ====================

    @Test
    @DisplayName("分析文章 - 内容为空返回默认结果")
    void testAnalyzeArticle_EmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "");

        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals(0, result.get("titleRelevanceScore"));
        assertEquals(0, result.get("qualityScore"));
        assertEquals(true, result.get("isTechContent"));
    }

    @Test
    @DisplayName("分析文章 - 内容为null返回默认结果")
    void testAnalyzeArticle_NullContent() {
        ApArticle article = new ApArticle();
        article.setId(1001L);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, null);

        assertNotNull(result);
        assertEquals(false, result.get("success"));
    }

    @Test
    @DisplayName("分析文章 - 完整分析成功")
    void testAnalyzeArticle_Success() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "这是一篇测试文章的内容";

        String titleResp = "{\"score\": 85, \"reason\": \"标题与内容基本匹配\"}";
        String qualityResp = "{\"quality_score\": 80, \"originality_score\": 75, \"logic_score\": 85, \"clarity_score\": 80, \"comment\": \"内容质量良好\"}";
        String techResp = "{\"is_tech\": true, \"confidence\": 0.95, \"reason\": \"属于技术文章\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(titleResp, qualityResp, techResp);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(85, result.get("titleRelevanceScore"));
        assertEquals(80, result.get("qualityScore"));
        assertEquals(true, result.get("isTechContent"));
        verify(dashScopeClient, times(3)).callGeneration(anyString(), anyString());
    }

    @Test
    @DisplayName("分析文章 - AI返回无效JSON时降级处理")
    void testAnalyzeArticle_InvalidJson() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "测试内容";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn("这不是有效的JSON");
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        // JSON解析失败时分数保持默认值
        assertEquals(0, result.get("titleRelevanceScore"));
    }

    @Test
    @DisplayName("分析文章 - AI调用异常时不影响流程")
    void testAnalyzeArticle_Exception() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "测试内容";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenThrow(new RuntimeException("API调用失败"));

        Map<String, Object> result = bailianAiService.analyzeArticle(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("success"));
    }

    @Test
    @DisplayName("分析文章 - 内容超过4000字符截断")
    void testAnalyzeArticle_TruncatedContent() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("a");
        }
        String content = sb.toString();

        String titleResp = "{\"score\": 70, \"reason\": \"标题与内容匹配\"}";
        String qualityResp = "{\"quality_score\": 60, \"originality_score\": 50, \"logic_score\": 70, \"clarity_score\": 60, \"comment\": \"一般\"}";
        String techResp = "{\"is_tech\": false, \"confidence\": 0.3, \"reason\": \"非技术\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenReturn(titleResp, qualityResp, techResp);
        when(aiAnalysisMapper.delete(any(QueryWrapper.class))).thenReturn(1);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
    }

    // ==================== checkViolation ====================

    @Test
    @DisplayName("违规检测 - 内容为空返回不违规")
    void testCheckViolation_EmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(1001L);

        Map<String, Object> result = bailianAiService.checkViolation(article, "");

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(false, result.get("is_violation"));
    }

    @Test
    @DisplayName("违规检测 - 检测到违规内容")
    void testCheckViolation_ViolationFound() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("违规文章");
        String content = "违规内容";

        String violationResp = "{\"is_violation\": true, \"violation_type\": \"色情低俗\", \"violation_reason\": \"包含不当内容\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(violationResp);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("is_violation"));
        assertEquals("色情低俗", result.get("violation_type"));
        assertEquals("包含不当内容", result.get("violation_reason"));
    }

    @Test
    @DisplayName("违规检测 - 未检测到违规")
    void testCheckViolation_NoViolation() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("正常文章");
        String content = "正常内容";

        String violationResp = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(violationResp);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(false, result.get("is_violation"));
    }

    @Test
    @DisplayName("违规检测 - AI调用异常时降级通过")
    void testCheckViolation_Exception() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "测试内容";

        when(dashScopeClient.callGeneration(anyString(), anyString()))
                .thenThrow(new RuntimeException("API调用失败"));

        Map<String, Object> result = bailianAiService.checkViolation(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(false, result.get("is_violation"));
    }

    @Test
    @DisplayName("违规检测 - 已有分析记录时更新")
    void testCheckViolation_ExistingRecord() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "测试内容";

        ApArticleAiAnalysis existing = new ApArticleAiAnalysis();
        existing.setId(1L);
        existing.setArticleId(1001L);
        existing.setRawResponse("existing data");

        String violationResp = "{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}";

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(violationResp);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(existing);
        when(aiAnalysisMapper.updateById(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(false, result.get("is_violation"));
        verify(aiAnalysisMapper, times(1)).updateById(any(ApArticleAiAnalysis.class));
    }

    @Test
    @DisplayName("违规检测 - AI返回markdown代码块包裹的JSON")
    void testCheckViolation_MarkdownJsonResponse() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        String content = "测试内容";

        String violationResp = "```json\n{\"is_violation\": false, \"violation_type\": \"\", \"violation_reason\": \"\"}\n```";

        when(dashScopeClient.callGeneration(anyString(), anyString())).thenReturn(violationResp);
        when(aiAnalysisMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(aiAnalysisMapper.insert(any(ApArticleAiAnalysis.class))).thenReturn(1);

        Map<String, Object> result = bailianAiService.checkViolation(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(false, result.get("is_violation"));
    }
}