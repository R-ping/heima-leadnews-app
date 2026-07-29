package com.heima.model.article.pojos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApArticle.nullSafeToMap 单元测试")
class ApArticleTest {

    @Test
    @DisplayName("所有字段为 null - String 和 Integer 字段应转为空字符串")
    void testNullSafeToMap_AllFieldsNull() {
        ApArticle article = new ApArticle();
        Map<String, Object> map = article.nullSafeToMap();

        // id 为 null 时不转换（保持 null）
        assertNull(map.get("id"));
        // String 字段 null → ""
        assertEquals("", map.get("title"));
        assertEquals("", map.get("authorName"));
        assertEquals("", map.get("channelName"));
        assertEquals("", map.get("coverImage"));
        assertEquals("", map.get("staticUrl"));
        assertEquals("", map.get("reason"));
        assertEquals("", map.get("authorImage"));
        // Integer 字段 null → ""
        assertEquals("", map.get("channelId"));
        assertEquals("", map.get("likes"));
        assertEquals("", map.get("collection"));
        assertEquals("", map.get("comment"));
        assertEquals("", map.get("views"));
        assertEquals("", map.get("score"));
        assertEquals("", map.get("provinceId"));
        assertEquals("", map.get("cityId"));
        assertEquals("", map.get("countyId"));
        // Byte 字段 null → ""
        assertEquals("", map.get("layout"));
        assertEquals("", map.get("flag"));
        assertEquals("", map.get("status"));
        // Boolean 字段 null → ""（isDeleted 有默认值 false，非 null）
        assertEquals("", map.get("syncStatus"));
        assertEquals("", map.get("origin"));
        assertEquals(false, map.get("isDeleted"));
        // Long 字段 null → ""
        assertEquals("", map.get("authorId"));
        // Date 字段 null → ""
        assertEquals("", map.get("createdTime"));
        assertEquals("", map.get("publishTime"));
        // List 字段 null → ""
        assertEquals("", map.get("tags"));
    }

    @Test
    @DisplayName("字段有值 - 应保留原始值")
    void testNullSafeToMap_FieldsWithValues() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("测试标题");
        article.setAuthorName("作者");
        article.setAuthorId(100L);
        article.setChannelId(10);
        article.setChannelName("科技频道");
        article.setLayout((byte) 1);
        article.setFlag((byte) 2);
        article.setCoverImage("http://img.example.com/cover.jpg");
        article.setLikes(5);
        article.setCollection(3);
        article.setComment(8);
        article.setViews(100);
        article.setScore(85);
        article.setProvinceId(440000);
        article.setCityId(440100);
        article.setCountyId(440106);
        article.setStaticUrl("http://example.com/article/1");
        article.setStatus((byte) 9);
        article.setReason("审核通过");
        article.setAuthorImage("http://img.example.com/avatar.jpg");
        article.setSyncStatus(true);
        article.setOrigin(false);
        article.setIsDeleted(false);

        Map<String, Object> map = article.nullSafeToMap();

        assertEquals(1L, map.get("id"));
        assertEquals("测试标题", map.get("title"));
        assertEquals("作者", map.get("authorName"));
        assertEquals(100L, map.get("authorId"));
        assertEquals(10, map.get("channelId"));
        assertEquals("科技频道", map.get("channelName"));
        assertEquals((byte) 1, map.get("layout"));
        assertEquals((byte) 2, map.get("flag"));
        assertEquals("http://img.example.com/cover.jpg", map.get("coverImage"));
        assertEquals(5, map.get("likes"));
        assertEquals(3, map.get("collection"));
        assertEquals(8, map.get("comment"));
        assertEquals(100, map.get("views"));
        assertEquals(85, map.get("score"));
        assertEquals(440000, map.get("provinceId"));
        assertEquals(440100, map.get("cityId"));
        assertEquals(440106, map.get("countyId"));
        assertEquals("http://example.com/article/1", map.get("staticUrl"));
        assertEquals((byte) 9, map.get("status"));
        assertEquals("审核通过", map.get("reason"));
        assertEquals("http://img.example.com/avatar.jpg", map.get("authorImage"));
        assertEquals(true, map.get("syncStatus"));
        assertEquals(false, map.get("origin"));
        assertEquals(false, map.get("isDeleted"));
    }

    @Test
    @DisplayName("部分字段有值、部分为 null - null 字段应转为空字符串")
    void testNullSafeToMap_MixedNullAndValues() {
        ApArticle article = new ApArticle();
        article.setTitle("部分字段");
        article.setViews(200);
        // 其他字段保持 null

        Map<String, Object> map = article.nullSafeToMap();

        assertEquals("部分字段", map.get("title"));
        assertEquals(200, map.get("views"));
        assertEquals("", map.get("authorName"));
        assertEquals("", map.get("channelName"));
        assertEquals("", map.get("likes"));
        assertEquals("", map.get("staticUrl"));
        assertEquals("", map.get("reason"));
    }

    @Test
    @DisplayName("isDeleted 默认值 false - 应保留默认值")
    void testNullSafeToMap_DefaultIsDeleted() {
        ApArticle article = new ApArticle();
        Map<String, Object> map = article.nullSafeToMap();

        assertEquals(false, map.get("isDeleted"));
    }

    @Test
    @DisplayName("Map 应包含所有声明的字段键")
    void testNullSafeToMap_ContainsAllKeys() {
        ApArticle article = new ApArticle();
        Map<String, Object> map = article.nullSafeToMap();

        assertTrue(map.containsKey("id"));
        assertTrue(map.containsKey("title"));
        assertTrue(map.containsKey("authorId"));
        assertTrue(map.containsKey("authorName"));
        assertTrue(map.containsKey("channelId"));
        assertTrue(map.containsKey("channelName"));
        assertTrue(map.containsKey("layout"));
        assertTrue(map.containsKey("flag"));
        assertTrue(map.containsKey("coverImage"));
        assertTrue(map.containsKey("tags"));
        assertTrue(map.containsKey("likes"));
        assertTrue(map.containsKey("collection"));
        assertTrue(map.containsKey("comment"));
        assertTrue(map.containsKey("views"));
        assertTrue(map.containsKey("score"));
        assertTrue(map.containsKey("provinceId"));
        assertTrue(map.containsKey("cityId"));
        assertTrue(map.containsKey("countyId"));
        assertTrue(map.containsKey("createdTime"));
        assertTrue(map.containsKey("publishTime"));
        assertTrue(map.containsKey("syncStatus"));
        assertTrue(map.containsKey("origin"));
        assertTrue(map.containsKey("staticUrl"));
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("reason"));
        assertTrue(map.containsKey("authorImage"));
        assertTrue(map.containsKey("isDeleted"));
    }
}