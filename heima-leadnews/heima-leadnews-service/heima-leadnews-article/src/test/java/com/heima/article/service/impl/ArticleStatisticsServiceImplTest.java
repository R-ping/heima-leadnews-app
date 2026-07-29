package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.*;
import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章统计服务测试")
class ArticleStatisticsServiceImplTest {

    @Mock
    private ApFollowMapper apFollowMapper;

    @Mock
    private ApBehaviorLikesMapper apBehaviorLikesMapper;

    @Mock
    private ApCollectionMapper apCollectionMapper;

    @Mock
    private ApArticleMapper apArticleMapper;

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private LevelService levelService;

    @InjectMocks
    private ArticleStatisticsServiceImpl statisticsService;

    // ==================== getUserStatistics ====================

    @Test
    @DisplayName("获取用户统计 - 全零数据用户")
    void testGetUserStatistics_NewUser() {
        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apCollectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(levelService.getUserLevelInfo(1L)).thenReturn(new HashMap<>());

        ResponseResult result = statisticsService.getUserStatistics(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("followCount"));
        assertEquals(0L, data.get("followerCount"));
        assertEquals(0L, data.get("likeCount"));
        assertEquals(0L, data.get("collectCount"));
        assertEquals(0L, data.get("readCount"));
        assertEquals(0, data.get("tagCount"));
        assertEquals(0, data.get("badgeCount"));
        assertEquals(0, data.get("diamondCount"));
    }

    @Test
    @DisplayName("获取用户统计 - 有文章数据的用户")
    void testGetUserStatistics_WithArticles() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setAuthorId(1L);
        article.setViews(100);
        article.setLikes(10);

        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L, 3L);
        when(apCollectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);
        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(article), Collections.singletonList(article));
        when(apBehaviorLikesMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(levelService.getUserLevelInfo(1L)).thenReturn(new HashMap<>());

        ResponseResult result = statisticsService.getUserStatistics(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(5L, data.get("followCount"));
        assertEquals(3L, data.get("followerCount"));
        assertEquals(10L, data.get("likeCount"));
        assertEquals(2L, data.get("collectCount"));
        assertEquals(100L, data.get("readCount"));
    }

    @Test
    @DisplayName("获取用户统计 - 有沸点的用户")
    void testGetUserStatistics_WithPins() {
        ApPins pins = new ApPins();
        pins.setId(1L);
        pins.setAuthorId(1L);

        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L);
        when(apCollectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pins));
        when(apBehaviorLikesMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(levelService.getUserLevelInfo(1L)).thenReturn(new HashMap<>());

        ResponseResult result = statisticsService.getUserStatistics(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("levelInfo"));
    }

    @Test
    @DisplayName("获取用户统计 - 文章views为null不出错")
    void testGetUserStatistics_NullViews() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setAuthorId(1L);
        article.setViews(null);

        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L);
        when(apCollectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apArticleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(article), Collections.singletonList(article));
        when(apBehaviorLikesMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(levelService.getUserLevelInfo(1L)).thenReturn(new HashMap<>());

        ResponseResult result = statisticsService.getUserStatistics(1L);

        assertNotNull(result);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("readCount"));
    }
}