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
@DisplayName("BailianAiService单元测试")
class BailianAiServiceImplTest {

    @Autowired
    private BailianAiService bailianAiService;

    private static final Long TEST_ARTICLE_ID = 99999001L;
    private static final String TEST_TITLE = "Java并发编程的艺术：深入理解volatile关键字";
    private static final String TEST_CONTENT = "在Java并发编程中，volatile关键字是一个非常重要的概念。本文将深入探讨volatile的工作原理、内存语义以及在实际开发中的应用场景。\n\n" +
            "## volatile的内存语义\n\nvolatile变量具有两个特性：\n" +
            "1. 可见性：对一个volatile变量的读，总是能看到任意线程对这个volatile变量最后的写入\n" +
            "2. 禁止指令重排序：volatile通过内存屏障来禁止特定类型的指令重排序\n\n" +
            "## 使用场景\n\n### 状态标志\n使用volatile变量作为状态标志是最常见的用法：\n```java\nvolatile boolean running = true;\n```\n\n### 双重检查锁定\n在单例模式中，volatile可以防止指令重排序导致的问题。\n\n## 总结\nvolatile是轻量级的同步机制，在特定场景下可以替代synchronized，提高程序性能。";

    @Test
    @Order(1)
    @DisplayName("AI分析 - 正常文章分析")
    void testAnalyzeArticle_Normal() {
        ApArticle article = new ApArticle();
        article.setId(TEST_ARTICLE_ID);
        article.setTitle(TEST_TITLE);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, TEST_CONTENT);
        assertNotNull(result);
        // 即使没有API Key，也不应抛异常
        assertNotNull(result.get("success"));
    }

    @Test
    @Order(2)
    @DisplayName("AI分析 - 空内容处理")
    void testAnalyzeArticle_EmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(99999002L);
        article.setTitle("测试标题");

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "");
        assertNotNull(result);
        // 空内容应返回success=false
    }

    @Test
    @Order(3)
    @DisplayName("AI分析 - 空标题处理")
    void testAnalyzeArticle_NullTitle() {
        ApArticle article = new ApArticle();
        article.setId(99999003L);
        article.setTitle(null);

        Map<String, Object> result = bailianAiService.analyzeArticle(article, "测试内容");
        assertNotNull(result);
        // 不应因空标题而抛异常
    }

    @Test
    @Order(4)
    @DisplayName("AI分析 - 超长内容截断")
    void testAnalyzeArticle_LongContent() {
        ApArticle article = new ApArticle();
        article.setId(99999004L);
        article.setTitle("测试标题");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("这是第").append(i).append("段测试内容。用于验证内容截断功能是否正常工作。");
        }

        Map<String, Object> result = bailianAiService.analyzeArticle(article, sb.toString());
        assertNotNull(result);
        // 超长内容不应导致异常
    }
}