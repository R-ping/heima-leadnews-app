package com.heima.article.service.impl;

import com.heima.common.bailian.DashScopeClient;
import com.heima.model.article.pojos.ApArticleEmbedding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleEmbeddingServiceImplTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private JdbcTemplate pgVectorJdbcTemplate;

    @InjectMocks
    private ArticleEmbeddingServiceImpl articleEmbeddingService;

    // ==================== generateEmbedding() tests ====================

    @Test
    void testGenerateEmbeddingSuccess() {
        when(dashScopeClient.callEmbedding(anyString())).thenReturn(new double[]{0.1, 0.2, 0.3});

        double[] result = articleEmbeddingService.generateEmbedding("test content");

        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void testGenerateEmbeddingNullContent() {
        double[] result = articleEmbeddingService.generateEmbedding(null);

        assertNull(result);
    }

    @Test
    void testGenerateEmbeddingEmptyContent() {
        double[] result = articleEmbeddingService.generateEmbedding("");

        assertNull(result);
    }

    @Test
    void testGenerateEmbeddingLongContent() {
        String longContent = "a".repeat(3000);
        when(dashScopeClient.callEmbedding(anyString())).thenReturn(new double[]{0.1, 0.2, 0.3});
        double[] result = articleEmbeddingService.generateEmbedding(longContent);

        assertNotNull(result);
    }

    // ==================== saveEmbedding() tests ====================

    @Test
    void testSaveEmbeddingSuccess() {
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(pgVectorJdbcTemplate.update(anyString(), anyLong())).thenReturn(1);
        when(pgVectorJdbcTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);

        articleEmbeddingService.saveEmbedding(1L, embedding);

        verify(pgVectorJdbcTemplate, times(1)).update(anyString(), anyLong());
        verify(pgVectorJdbcTemplate, times(1)).update(any(PreparedStatementCreator.class));
    }

    @Test
    void testSaveEmbeddingNullEmbedding() {
        articleEmbeddingService.saveEmbedding(1L, null);

        verify(pgVectorJdbcTemplate, never()).update(anyString(), anyLong());
    }

    @Test
    void testSaveEmbeddingEmptyEmbedding() {
        articleEmbeddingService.saveEmbedding(1L, new double[0]);

        verify(pgVectorJdbcTemplate, never()).update(anyString(), anyLong());
    }

    @Test
    void testSaveEmbeddingPgVectorNotConfigured() {
        ArticleEmbeddingServiceImpl service = new ArticleEmbeddingServiceImpl();
        ReflectionTestUtils.setField(service, "dashScopeClient", dashScopeClient);
        // pgVectorJdbcTemplate is null by default

        service.saveEmbedding(1L, new double[]{0.1, 0.2});

        // No exception should be thrown
        assertTrue(true);
    }

    @Test
    void testSaveEmbeddingException() {
        double[] embedding = new double[]{0.1, 0.2};
        when(pgVectorJdbcTemplate.update(anyString(), anyLong())).thenThrow(new RuntimeException("DB error"));

        articleEmbeddingService.saveEmbedding(1L, embedding);

        // Exception should be caught
        assertTrue(true);
    }

    // ==================== getEmbedding() tests ====================

    @Test
    void testGetEmbeddingSuccess() {
        when(pgVectorJdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<ApArticleEmbedding> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getLong("id")).thenReturn(1L);
                    when(rs.getLong("article_id")).thenReturn(1L);
                    when(rs.getString("embedding")).thenReturn("[0.1, 0.2, 0.3]");
                    when(rs.getTimestamp("created_time")).thenReturn(new Timestamp(System.currentTimeMillis()));
                    return extractor.extractData(rs);
                });

        ApArticleEmbedding result = articleEmbeddingService.getEmbedding(1L);

        assertNotNull(result);
        assertEquals(1L, result.getArticleId());
        assertNotNull(result.getEmbedding());
        assertEquals(3, result.getEmbedding().length);
    }

    @Test
    void testGetEmbeddingNotFound() {
        when(pgVectorJdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<ApArticleEmbedding> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });

        ApArticleEmbedding result = articleEmbeddingService.getEmbedding(1L);

        assertNull(result);
    }

    @Test
    void testGetEmbeddingNullVectorStr() {
        when(pgVectorJdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<ApArticleEmbedding> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getLong("id")).thenReturn(1L);
                    when(rs.getLong("article_id")).thenReturn(1L);
                    when(rs.getString("embedding")).thenReturn(null);
                    when(rs.getTimestamp("created_time")).thenReturn(new Timestamp(System.currentTimeMillis()));
                    return extractor.extractData(rs);
                });

        ApArticleEmbedding result = articleEmbeddingService.getEmbedding(1L);

        assertNotNull(result);
        assertNull(result.getEmbedding());
    }

    @Test
    void testGetEmbeddingEmptyVectorStr() {
        when(pgVectorJdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<ApArticleEmbedding> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true);
                    when(rs.getLong("id")).thenReturn(1L);
                    when(rs.getLong("article_id")).thenReturn(1L);
                    when(rs.getString("embedding")).thenReturn("");
                    when(rs.getTimestamp("created_time")).thenReturn(new Timestamp(System.currentTimeMillis()));
                    return extractor.extractData(rs);
                });

        ApArticleEmbedding result = articleEmbeddingService.getEmbedding(1L);

        assertNotNull(result);
        assertNull(result.getEmbedding());
    }

    @Test
    void testGetEmbeddingException() {
        when(pgVectorJdbcTemplate.query(anyString(), any(ResultSetExtractor.class)))
                .thenThrow(new RuntimeException("DB error"));

        ApArticleEmbedding result = articleEmbeddingService.getEmbedding(1L);

        assertNull(result);
    }

    @Test
    void testGetEmbeddingPgVectorNotConfigured() {
        ArticleEmbeddingServiceImpl service = new ArticleEmbeddingServiceImpl();
        ReflectionTestUtils.setField(service, "dashScopeClient", dashScopeClient);

        ApArticleEmbedding result = service.getEmbedding(1L);

        assertNull(result);
    }

    // ==================== findSimilarArticles() tests ====================

    @Test
    void testFindSimilarArticlesSuccess() {
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(pgVectorJdbcTemplate.query(any(PreparedStatementCreator.class), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<List<Object[]>> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(true, true, false);
                    when(rs.getLong("article_id")).thenReturn(2L, 3L);
                    when(rs.getDouble("similarity")).thenReturn(0.92, 0.85);
                    return extractor.extractData(rs);
                });

        List<Object[]> result = articleEmbeddingService.findSimilarArticles(embedding, 5, 0.8);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0)[0]);
        assertEquals(0.92, (Double) result.get(0)[1], 0.001);
    }

    @Test
    void testFindSimilarArticlesEmptyResult() {
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(pgVectorJdbcTemplate.query(any(PreparedStatementCreator.class), any(ResultSetExtractor.class)))
                .thenAnswer(invocation -> {
                    ResultSetExtractor<List<Object[]>> extractor = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.next()).thenReturn(false);
                    return extractor.extractData(rs);
                });

        List<Object[]> result = articleEmbeddingService.findSimilarArticles(embedding, 5, 0.9);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindSimilarArticlesException() {
        double[] embedding = new double[]{0.1, 0.2, 0.3};
        when(pgVectorJdbcTemplate.query(any(PreparedStatementCreator.class), any(ResultSetExtractor.class)))
                .thenThrow(new RuntimeException("DB error"));

        List<Object[]> result = articleEmbeddingService.findSimilarArticles(embedding, 5, 0.8);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindSimilarArticlesPgVectorNotConfigured() {
        ArticleEmbeddingServiceImpl service = new ArticleEmbeddingServiceImpl();
        ReflectionTestUtils.setField(service, "dashScopeClient", dashScopeClient);

        List<Object[]> result = service.findSimilarArticles(new double[]{0.1, 0.2}, 5, 0.8);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}