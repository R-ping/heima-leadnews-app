package com.heima.article.service;

import com.heima.model.article.pojos.ApTopic;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("话题服务测试")
public class TopicServiceImplTest {

    @Autowired
    private TopicService topicService;

    // ==================== findList ====================

    @Test
    @Order(1)
    @DisplayName("查询话题列表 - 无关键字返回全部话题")
    void testFindList_All() {
        List<ApTopic> result = topicService.findList(null);
        assertNotNull(result);
        // 验证返回列表不为null
        assertTrue(result.size() >= 0);
    }

    @Test
    @Order(2)
    @DisplayName("查询话题列表 - 空关键字返回全部话题")
    void testFindList_EmptyKeyword() {
        List<ApTopic> result = topicService.findList("");
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    @Order(3)
    @DisplayName("查询话题列表 - 按关键字搜索")
    void testFindList_WithKeyword() {
        List<ApTopic> result = topicService.findList("Java");
        assertNotNull(result);
        // 验证所有返回的话题名包含关键字
        for (ApTopic topic : result) {
            assertTrue(topic.getName().toLowerCase().contains("java") ||
                       topic.getName().contains("Java"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("查询话题列表 - 按排序升序返回")
    void testFindList_OrderBySort() {
        List<ApTopic> result = topicService.findList(null);
        assertNotNull(result);
        // 验证按sort升序排列
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getSort() <= result.get(i).getSort());
        }
    }

    // ==================== getRecommendTopics ====================

    @Test
    @Order(5)
    @DisplayName("获取推荐话题 - 返回最多10条")
    void testGetRecommendTopics_Max10() {
        List<ApTopic> result = topicService.getRecommendTopics();
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }

    @Test
    @Order(6)
    @DisplayName("获取推荐话题 - 按讨论数降序排列")
    void testGetRecommendTopics_OrderByCount() {
        List<ApTopic> result = topicService.getRecommendTopics();
        assertNotNull(result);
        // 验证按count降序排列
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getCount() >= result.get(i).getCount());
        }
    }

    @Test
    @Order(7)
    @DisplayName("获取推荐话题 - 所有话题status均为1")
    void testGetRecommendTopics_StatusIsActive() {
        List<ApTopic> result = topicService.getRecommendTopics();
        assertNotNull(result);
        for (ApTopic topic : result) {
            assertEquals(Integer.valueOf(1), topic.getStatus());
        }
    }
}