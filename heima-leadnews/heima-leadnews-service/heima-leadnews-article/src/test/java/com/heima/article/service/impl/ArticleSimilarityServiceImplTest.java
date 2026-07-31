package com.heima.article.service.impl;

import com.heima.model.article.pojos.ApArticle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleSimilarityServiceImplTest {

    @Mock
    private ArticleEmbeddingServiceImpl embeddingService;

    @InjectMocks
    private ArticleSimilarityServiceImpl articleSimilarityService;

    @Test
    void testCheckSimilarityNotSimilar() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(Collections.emptyList());
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertFalse((Boolean) result.get("isSimilar"));
        assertEquals(0.0, (Double) result.get("maxSimilarity"), 0.001);
        assertNull(result.get("similarArticleId"));
    }

    @Test
    void testCheckSimilaritySimilar() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);

        List<Object[]> similarArticles = new ArrayList<>();
        similarArticles.add(new Object[]{2L, 0.92});
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(similarArticles);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("isSimilar"));
        assertEquals(0.92, (Double) result.get("maxSimilarity"), 0.001);
        assertEquals(2L, result.get("similarArticleId"));
    }

    @Test
    void testCheckSimilaritySimilarToSelf() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);

        List<Object[]> similarArticles = new ArrayList<>();
        similarArticles.add(new Object[]{1L, 0.99});
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(similarArticles);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertFalse((Boolean) result.get("isSimilar"));
    }

    @Test
    void testCheckSimilarityNullEmbedding() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        when(embeddingService.generateEmbedding(anyString())).thenReturn(null);

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertFalse((Boolean) result.get("isSimilar"));
    }

    @Test
    void testCheckSimilarityException() {
        ApArticle article = new ApArticle();
        article.setId(1L);

        when(embeddingService.generateEmbedding(anyString())).thenThrow(new RuntimeException("Error"));

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertFalse((Boolean) result.get("isSimilar"));
    }

    @Test
    void testCheckSimilarityWithMultipleSimilarArticles() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(embeddingService.generateEmbedding(anyString())).thenReturn(embedding);

        List<Object[]> similarArticles = new ArrayList<>();
        similarArticles.add(new Object[]{3L, 0.95});
        similarArticles.add(new Object[]{2L, 0.88});
        when(embeddingService.findSimilarArticles(any(double[].class), anyInt(), anyDouble()))
                .thenReturn(similarArticles);
        doNothing().when(embeddingService).saveEmbedding(anyLong(), any(double[].class));

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "test content");

        assertNotNull(result);
        assertTrue((Boolean) result.get("isSimilar"));
        assertEquals(3L, result.get("similarArticleId"));
    }
}