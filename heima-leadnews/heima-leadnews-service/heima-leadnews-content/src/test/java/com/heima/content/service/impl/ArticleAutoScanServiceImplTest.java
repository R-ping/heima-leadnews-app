package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.ApArticleAuditRecordMapper;
import com.heima.content.mapper.ApArticleConfigMapper;
import com.heima.content.mapper.ApArticleContentMapper;
import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.service.ArticleSimilarityService;
import com.heima.content.service.ArticleTaskService;
import com.heima.content.service.BailianAiService;
import com.heima.content.service.LevelService;
import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.common.aliyun.GreenTextScanPlus;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

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
    private INotificationClient notificationClient;

    @Mock
    private ApArticleAuditRecordMapper apArticleAuditRecordMapper;

    @InjectMocks
    private ArticleAutoScanServiceImpl articleAutoScanService;

    private ApArticle buildArticle(Long id, byte status) {
        ApArticle article = new ApArticle();
        article.setId(id);
        article.setTitle("test article " + id);
        article.setAuthorId(1L);
        article.setAuthorName("author");
        article.setStatus(status);
        article.setChannelId(1);
        article.setCoverImage("https://cover.jpg");
        return article;
    }

    // ==================== autoScanArticle() tests ====================

    @Test
    void testAutoScanArticleNotFound() {
        when(apArticleMapper.selectById(1L)).thenReturn(null);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
    }

    @Test
    void testAutoScanArticleNotSubmitStatus() {
        ApArticle article = buildArticle(1L, ApArticle.Status.PUBLISHED.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }

    @Test
    void testAutoScanArticleViolationFound() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("violation content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "色情低俗");
        violationResult.put("violation_reason", "包含色情内容");
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
        ArgumentCaptor<ApArticle> articleCaptor = ArgumentCaptor.forClass(ApArticle.class);
        verify(apArticleMapper).updateById(articleCaptor.capture());
        assertEquals(ApArticle.Status.FAIL.getCode(), articleCaptor.getValue().getStatus());
    }

    @Test
    void testAutoScanArticleViolationCheckException() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenThrow(new RuntimeException("AI error"));

        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(apArticleConfigMapper.insert(any(ApArticleConfig.class))).thenReturn(1);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(null);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }

    @Test
    void testAutoScanArticleImageScanFail() throws Exception {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        List<ApArticleDraft.ContPic> contPics = new ArrayList<>();
        ApArticleDraft.ContPic pic = new ApArticleDraft.ContPic();
        pic.setPicUrl("https://example.com/pic.jpg");
        contPics.add(pic);
        article.setContPics(contPics);

        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content with image");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, String> scanResult = new HashMap<>();
        scanResult.put("level", "high");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
    }

    @Test
    void testAutoScanArticleImageScanMedium() throws Exception {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        List<ApArticleDraft.ContPic> contPics = new ArrayList<>();
        ApArticleDraft.ContPic pic = new ApArticleDraft.ContPic();
        pic.setPicUrl("https://example.com/pic.jpg");
        contPics.add(pic);
        article.setContPics(contPics);

        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content with image");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, String> scanResult = new HashMap<>();
        scanResult.put("level", "medium");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
    }

    @Test
    void testAutoScanArticleImageScanException() throws Exception {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        List<ApArticleDraft.ContPic> contPics = new ArrayList<>();
        ApArticleDraft.ContPic pic = new ApArticleDraft.ContPic();
        pic.setPicUrl("https://example.com/pic.jpg");
        contPics.add(pic);
        article.setContPics(contPics);

        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content with image");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        when(greenImageScan.imageScan(anyString())).thenThrow(new RuntimeException("scan error"));

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
    }

    @Test
    void testAutoScanArticleFullSuccess() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("clean content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 85);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 10);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 5);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), anyInt()))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
        verify(apArticleConfigMapper, times(2)).updateById(any(ApArticleConfig.class));
        verify(articleTaskService).addArticleToTask(anyLong(), any());
    }

    @Test
    void testAutoScanArticleWithHighSimilarity() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 60);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", true);
        similarityResult.put("maxSimilarity", 0.95);
        similarityResult.put("similarArticleId", 2L);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", false);
        expResult.put("message", "今日经验已达上限");
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 2);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(1)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
        ArgumentCaptor<ApArticleConfig> configCaptor = ArgumentCaptor.forClass(ApArticleConfig.class);
        verify(apArticleConfigMapper).updateById(configCaptor.capture());
        assertFalse(configCaptor.getValue().getIsRecommend());
    }

    @Test
    void testAutoScanArticleWithLowQualityScore() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 50);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 5);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
        verify(levelService, never()).calculatePowerWithLimit(anyLong(), anyLong(), anyString(), anyInt());
    }

    @Test
    void testAutoScanArticleWithNotificationClient() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "违规内容");
        violationResult.put("violation_reason", "违规原因");
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap())).thenReturn(ResponseResult.okResult("ok"));

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
        verify(notificationClient).createNotification(anyMap());
    }

    @Test
    void testAutoScanArticleWithNotificationClientFail() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "违规内容");
        violationResult.put("violation_reason", "违规原因");
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap()))
                .thenReturn(ResponseResult.errorResult(500, "error"));

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
        verify(notificationClient).createNotification(anyMap());
    }

    @Test
    void testAutoScanArticleWithNotificationClientException() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", true);
        violationResult.put("violation_type", "违规内容");
        violationResult.put("violation_reason", "违规原因");
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        when(apArticleAuditRecordMapper.insert(any(ApArticleAuditRecord.class))).thenReturn(1);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap()))
                .thenThrow(new RuntimeException("notify error"));

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertFalse(future.join());
    }

    @Test
    void testAutoScanArticleWithEmptyContent() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 70);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 5);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 3);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(1)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }

    @Test
    void testAutoScanArticleWithCoverImage() throws Exception {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        article.setCoverImage("https://cover.jpg");
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content with image");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, String> scanResult = new HashMap<>();
        scanResult.put("level", "pass");
        when(greenImageScan.imageScan(anyString())).thenReturn(scanResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 75);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);
        when(apArticleConfigMapper.updateById(any(ApArticleConfig.class))).thenReturn(1);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 5);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 3);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(1)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }

    @Test
    void testAutoScanArticleWithExpLevelUp() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        article.setAuthorId(1L);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 90);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 10);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 4);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(3)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
        verify(apArticleConfigMapper, times(2)).updateById(any(ApArticleConfig.class));
    }

    @Test
    void testAutoScanArticleWithExpException() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 80);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Level service error"));

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 3);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(3)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }

    @Test
    void testAutoScanArticleWithNullAuthorId() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        article.setAuthorId(null);
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1L);
        content.setContent("content");
        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(content);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 80);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        Map<String, Object> similarityResult = new HashMap<>();
        similarityResult.put("isSimilar", false);
        when(articleSimilarityService.checkSimilarity(any(ApArticle.class), anyString()))
                .thenReturn(similarityResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
        verify(levelService, never()).recordActionWithLimit(anyLong(), anyString(), anyString());
    }

    @Test
    void testAutoScanArticleWithNullContent() {
        ApArticle article = buildArticle(1L, ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);

        when(apArticleContentMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        Map<String, Object> violationResult = new HashMap<>();
        violationResult.put("is_violation", false);
        when(bailianAiService.checkViolation(any(ApArticle.class), anyString()))
                .thenReturn(violationResult);

        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("success", true);
        aiResult.put("qualityScore", 70);
        when(bailianAiService.analyzeArticle(any(ApArticle.class), anyString()))
                .thenReturn(aiResult);

        ApArticleConfig config = new ApArticleConfig(1L);
        when(apArticleConfigMapper.selectOne(any(QueryWrapper.class))).thenReturn(config);

        Map<String, Object> expResult = new HashMap<>();
        expResult.put("success", true);
        expResult.put("score", 5);
        when(levelService.recordActionWithLimit(anyLong(), anyString(), anyString()))
                .thenReturn(expResult);

        Map<String, Object> powerResult = new HashMap<>();
        powerResult.put("success", true);
        powerResult.put("newLevel", 3);
        when(levelService.calculatePowerWithLimit(anyLong(), anyLong(), anyString(), eq(1)))
                .thenReturn(powerResult);

        doNothing().when(articleTaskService).addArticleToTask(anyLong(), any());

        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(1L);

        assertTrue(future.join());
    }
}