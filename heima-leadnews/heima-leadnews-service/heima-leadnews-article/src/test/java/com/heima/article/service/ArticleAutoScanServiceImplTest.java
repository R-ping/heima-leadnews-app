package com.heima.article.service;

import com.heima.article.ArticleApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ArticleApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ArticleAutoScanService单元测试")
class ArticleAutoScanServiceImplTest {

    @Autowired
    private ArticleAutoScanService articleAutoScanService;

    @Test
    @Order(1)
    @DisplayName("自动审核 - 不存在的文章")
    void testAutoScanArticle_NonExistent() throws Exception {
        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(99999999L);
        Boolean result = future.get(10, TimeUnit.SECONDS);
        assertFalse(result);
    }

    @Test
    @Order(2)
    @DisplayName("自动审核 - 空文章ID")
    void testAutoScanArticle_NullId() throws Exception {
        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(null);
        Boolean result = future.get(10, TimeUnit.SECONDS);
        assertFalse(result);
    }

    @Test
    @Order(3)
    @DisplayName("自动审核 - 异步执行验证")
    void testAutoScanArticle_Async() throws Exception {
        CompletableFuture<Boolean> future = articleAutoScanService.autoScanArticle(99999000L);
        assertNotNull(future);
        // 异步方法应立即返回Future，不应阻塞
        assertFalse(future.isDone() || future.isCancelled() || future.isCompletedExceptionally());
    }
}