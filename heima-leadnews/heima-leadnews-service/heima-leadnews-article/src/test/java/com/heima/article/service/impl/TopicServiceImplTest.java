package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.heima.article.mapper.TopicMapper;
import com.heima.model.article.pojos.ApTopic;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("话题服务测试")
class TopicServiceImplTest {

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicServiceImpl topicService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ApTopic.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(topicService, "baseMapper", topicMapper);
    }

    // ==================== findList ====================

    @Test
    @DisplayName("查询话题列表 - 无关键词返回全部有效话题")
    void testFindList_NoKeyword() {
        ApTopic topic1 = new ApTopic();
        topic1.setId(1L);
        topic1.setName("Java");
        topic1.setStatus(1);

        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(topic1));

        List<ApTopic> result = topicService.findList(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
    }

    @Test
    @DisplayName("查询话题列表 - 带关键词模糊搜索")
    void testFindList_WithKeyword() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Spring Boot");
        topic.setStatus(1);

        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(topic));

        List<ApTopic> result = topicService.findList("Spring");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("查询话题列表 - 空关键词字符串")
    void testFindList_EmptyKeyword() {
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ApTopic> result = topicService.findList("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询话题列表 - 无匹配结果")
    void testFindList_NoResults() {
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ApTopic> result = topicService.findList("不存在的关键词");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getRecommendTopics ====================

    @Test
    @DisplayName("获取推荐话题 - 返回热门话题前10条")
    void testGetRecommendTopics_Success() {
        ApTopic topic1 = new ApTopic();
        topic1.setId(1L);
        topic1.setName("热门话题1");
        topic1.setCount(100);
        ApTopic topic2 = new ApTopic();
        topic2.setId(2L);
        topic2.setName("热门话题2");
        topic2.setCount(80);

        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(topic1, topic2));

        List<ApTopic> result = topicService.getRecommendTopics();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("热门话题1", result.get(0).getName());
    }

    @Test
    @DisplayName("获取推荐话题 - 无推荐话题")
    void testGetRecommendTopics_Empty() {
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<ApTopic> result = topicService.getRecommendTopics();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}