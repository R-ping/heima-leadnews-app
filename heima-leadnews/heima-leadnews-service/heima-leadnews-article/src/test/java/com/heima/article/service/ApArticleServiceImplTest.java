package com.heima.article.service;

import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("文章服务测试")
public class ApArticleServiceImplTest {

    @Autowired
    private ApArticleService apArticleService;

    // ==================== load ====================

    @Test
    @Order(1)
    @DisplayName("加载文章列表 - 加载更多")
    void testLoad_LoadMore() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(2)
    @DisplayName("加载文章列表 - 加载最新")
    void testLoad_LoadNew() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 2);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("加载文章列表 - size为null使用默认值10")
    void testLoad_NullSize() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(null);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(4)
    @DisplayName("加载文章列表 - size超过50被截断")
    void testLoad_MaxSize() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(100);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("加载文章列表 - 无效type使用默认值")
    void testLoad_InvalidType() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag("__all__");
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 99);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("加载文章列表 - tag为空使用默认值")
    void testLoad_EmptyTag() {
        ArticleHomeDto dto = new ArticleHomeDto();
        dto.setSize(10);
        dto.setTag(null);
        dto.setMaxBehotTime(new Date());
        dto.setMinBehotTime(new Date());

        ResponseResult result = apArticleService.load(dto, (short) 1);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== saveArticle ====================

    @Test
    @Order(7)
    @DisplayName("保存文章 - dto为null返回错误")
    void testSaveArticle_NullDto() {
        ResponseResult result = apArticleService.saveArticle(null, 0);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== listByAuthorId ====================

    @Test
    @Order(8)
    @DisplayName("按作者ID查询文章 - 正常返回")
    void testListByAuthorId_Success() {
        ArticleDto dto = new ArticleDto();
        dto.setAuthorId(1L);
        dto.setIsDeleted(false);

        List<ApArticle> result = apArticleService.listByAuthorId(dto);
        assertNotNull(result);
    }

    @Test
    @Order(9)
    @DisplayName("按作者ID查询文章 - 无authorId返回空")
    void testListByAuthorId_NoAuthorId() {
        ArticleDto dto = new ArticleDto();

        List<ApArticle> result = apArticleService.listByAuthorId(dto);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}