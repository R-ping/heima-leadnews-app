package com.heima.article.service.impl;

import com.heima.model.article.pojos.ApArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章相似度服务测试")
class ArticleSimilarityServiceImplTest {

    @Mock
    private ArticleEmbeddingServiceImpl embeddingService;

    @InjectMocks
    private ArticleSimilarityServiceImpl similarityService;

    // ==================== checkSimilarity ====================

    @Test
    @DisplayName("检查相似度 - embedding生成失败返回默认结果")
    void testCheckSimilarity_EmbeddingNull() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";

        when(embeddingService.generateEmbedding(content)).thenReturn(null);

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("isSimilar"));
        assertEquals(0.0, result.get("maxSimilarity"));
        assertNull(result.get("similarArticleId"));
        verify(embeddingService, never()).saveEmbedding(anyLong(), any(double[].class));
    }

    @Test
    @DisplayName("检查相似度 - 无相似文章")
    void testCheckSimilarity_NoSimilarArticles() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";
        double[] embedding = new double[]{0.1, 0.2, 0.3};

        when(embeddingService.generateEmbedding(content)).thenReturn(embedding);
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(Collections.emptyList());
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("isSimilar"));
        assertEquals(0.0, result.get("maxSimilarity"));
        verify(embeddingService, times(1)).saveEmbedding(eq(1001L), any(double[].class));
    }

    @Test
    @DisplayName("检查相似度 - 找到相似文章（排除自身）")
    void testCheckSimilarity_FoundSimilar() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";
        double[] embedding = new double[]{0.1, 0.2, 0.3};

        List<Object[]> similarArticles = new ArrayList<>();
        similarArticles.add(new Object[]{1002L, 0.92});

        when(embeddingService.generateEmbedding(content)).thenReturn(embedding);
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(similarArticles);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(true, result.get("isSimilar"));
        assertEquals(0.92, result.get("maxSimilarity"));
        assertEquals(1002L, result.get("similarArticleId"));
        verify(embeddingService, times(1)).saveEmbedding(eq(1001L), any(double[].class));
    }

    @Test
    @DisplayName("检查相似度 - 相似文章是自身，排除后无相似")
    void testCheckSimilarity_SelfSimilar() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";
        double[] embedding = new double[]{0.1, 0.2, 0.3};

        List<Object[]> similarArticles = new ArrayList<>();
        similarArticles.add(new Object[]{1001L, 0.95});

        when(embeddingService.generateEmbedding(content)).thenReturn(embedding);
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(similarArticles);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("isSimilar"));
        assertEquals(0.0, result.get("maxSimilarity"));
        assertNull(result.get("similarArticleId"));
    }

    @Test
    @DisplayName("检查相似度 - embeddingService.findSimilarArticles返回null")
    void testCheckSimilarity_NullSimilarList() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";
        double[] embedding = new double[]{0.1, 0.2, 0.3};

        when(embeddingService.generateEmbedding(content)).thenReturn(embedding);
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(null);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("isSimilar"));
    }

    @Test
    @DisplayName("检查相似度 - 发生异常时返回默认结果")
    void testCheckSimilarity_Exception() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        String content = "测试内容";

        when(embeddingService.generateEmbedding(content)).thenThrow(new RuntimeException("Embedding failed"));

        Map<String, Object> result = similarityService.checkSimilarity(article, content);

        assertNotNull(result);
        assertEquals(false, result.get("isSimilar"));
        assertEquals(0.0, result.get("maxSimilarity"));
    }
}