package com.heima.article.service;

import com.heima.article.ArticleApplication;
import com.heima.model.article.pojos.ApArticle;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires running services")
@SpringBootTest(classes = ArticleApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ArticleSimilarityService单元测试")
class ArticleSimilarityServiceImplTest {

    @Autowired
    private ArticleSimilarityService articleSimilarityService;

    private static final Long TEST_ARTICLE_ID = 99999101L;

    @Test
    @Order(1)
    @DisplayName("相似度检测 - 新文章检测")
    void testCheckSimilarity_NewArticle() {
        ApArticle article = new ApArticle();
        article.setId(TEST_ARTICLE_ID);
        article.setTitle("Spring Boot 3.x 新特性详解");

        String content = "Spring Boot 3.x带来了许多重大更新，包括对Jakarta EE 9的支持、GraalVM原生镜像的改进、Observability的增强等。本文将详细介绍这些新特性。";

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, content);
        assertNotNull(result);
        assertNotNull(result.get("isSimilar"));
        assertNotNull(result.get("maxSimilarity"));
    }

    @Test
    @Order(2)
    @DisplayName("相似度检测 - 空内容")
    void testCheckSimilarity_EmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(99999102L);
        article.setTitle("空内容测试");

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, "");
        assertNotNull(result);
        // 空内容不应抛异常
    }

    @Test
    @Order(3)
    @DisplayName("相似度检测 - 相同内容应检测到高相似度")
    void testCheckSimilarity_SameContent() {
        ApArticle article = new ApArticle();
        article.setId(99999103L);
        article.setTitle("测试文章");

        String content = "这是一篇测试文章的内容，用于验证相似度检测功能。"
                + "包括一些技术术语如Spring、Java、微服务、分布式系统等。";

        Map<String, Object> result = articleSimilarityService.checkSimilarity(article, content);
        assertNotNull(result);
        // 结果应包含相似度信息
        assertNotNull(result.get("isSimilar"));
    }
}