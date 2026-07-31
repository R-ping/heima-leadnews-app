package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.*;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.TopicSquareDto;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.TopicDetailVO;
import com.heima.model.article.vos.TopicRecommendVO;
import com.heima.model.article.vos.TopicSquareVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TopicServiceImplTest {

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private TopicRelationMapper topicRelationMapper;

    @Mock
    private UserTopicPostMapper userTopicPostMapper;

    @Mock
    private TopicCircleRelationMapper topicCircleRelationMapper;

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private TopicServiceImpl topicService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(topicService, "baseMapper", topicMapper);
    }

    // ==================== recommend() tests ====================

    @Test
    void testRecommend() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(topic));

        Map<String, Object> result = topicService.recommend(0, 5);

        assertEquals(1, result.get("total"));
        @SuppressWarnings("unchecked")
        List<TopicRecommendVO> list = (List<TopicRecommendVO>) result.get("list");
        assertEquals(5, list.size());
        assertEquals("Java", list.get(0).getName());
    }

    @Test
    void testRecommendEmpty() {
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        Map<String, Object> result = topicService.recommend(0, 5);

        assertEquals(0, result.get("total"));
        @SuppressWarnings("unchecked")
        List<TopicRecommendVO> list = (List<TopicRecommendVO>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testRecommendSecondPage() {
        ApTopic t1 = buildTopic(1L, "Java", "hot");
        ApTopic t2 = buildTopic(2L, "Python", "hot");
        ApTopic t3 = buildTopic(3L, "Go", "hot");
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(t1, t2, t3));

        Map<String, Object> result = topicService.recommend(1, 2);

        @SuppressWarnings("unchecked")
        List<TopicRecommendVO> list = (List<TopicRecommendVO>) result.get("list");
        assertEquals(2, list.size());
    }

    @Test
    void testRecommendNullFields() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setBadge(null);
        topic.setParticipantCount(null);
        topic.setViewCount(null);
        when(topicMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(topic));

        Map<String, Object> result = topicService.recommend(0, 5);

        @SuppressWarnings("unchecked")
        List<TopicRecommendVO> list = (List<TopicRecommendVO>) result.get("list");
        assertEquals("", list.get(0).getBadge());
        assertEquals(0L, list.get(0).getParticipantCount().longValue());
        assertEquals(0L, list.get(0).getViewCount().longValue());
    }

    // ==================== square() tests ====================

    @Test
    void testSquare() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        TopicSquareDto dto = new TopicSquareDto();
        dto.setSort("hot");
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        @SuppressWarnings("unchecked")
        List<TopicSquareVO> list = (List<TopicSquareVO>) result.get("list");
        assertEquals(1, list.size());
        assertEquals(20L, result.get("cursor"));
        assertFalse((Boolean) result.get("has_more"));
    }

    @Test
    void testSquareHasMore() {
        List<ApTopic> topics = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            topics.add(buildTopic((long) i, "Topic" + i, "hot"));
        }
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 21) {{
                    setRecords(topics);
                }});

        TopicSquareDto dto = new TopicSquareDto();
        dto.setSort("hot");
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        @SuppressWarnings("unchecked")
        List<TopicSquareVO> list = (List<TopicSquareVO>) result.get("list");
        assertEquals(20, list.size());
        assertTrue((Boolean) result.get("has_more"));
    }

    @Test
    void testSquareWithKeyword() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 0));

        TopicSquareDto dto = new TopicSquareDto();
        dto.setKeyword("Java");
        dto.setSort("new");
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        @SuppressWarnings("unchecked")
        List<TopicSquareVO> list = (List<TopicSquareVO>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testSquareWithBlankKeyword() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 0));

        TopicSquareDto dto = new TopicSquareDto();
        dto.setKeyword("   ");
        dto.setSort("hot");
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        assertNotNull(result);
    }

    @Test
    void testSquareDefaultSort() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 0));

        TopicSquareDto dto = new TopicSquareDto();
        dto.setSort(null);
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        assertNotNull(result);
    }

    @Test
    void testSquareDefaultCursor() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 0));

        TopicSquareDto dto = new TopicSquareDto();
        dto.setCursor(null);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        assertNotNull(result);
    }

    @Test
    void testSquareZeroSize() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 0));

        TopicSquareDto dto = new TopicSquareDto();
        dto.setSize(0);

        Map<String, Object> result = topicService.square(dto);

        assertNotNull(result);
    }

    @Test
    void testSquareNullFields() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setDescription(null);
        topic.setParticipantCount(null);
        topic.setViewCount(null);
        topic.setPostCount(null);
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 21, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        TopicSquareDto dto = new TopicSquareDto();
        dto.setCursor(0L);
        dto.setSize(20);

        Map<String, Object> result = topicService.square(dto);

        @SuppressWarnings("unchecked")
        List<TopicSquareVO> list = (List<TopicSquareVO>) result.get("list");
        assertEquals("", list.get(0).getDescription());
        assertEquals(0L, list.get(0).getParticipantCount().longValue());
        assertEquals(0L, list.get(0).getViewCount().longValue());
        assertEquals(0L, list.get(0).getPostCount().longValue());
    }

    // ==================== detail() tests ====================

    @Test
    void testDetail() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setDescription("Java desc");
        topic.setCoverImage("cover.png");
        topic.setType(2);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicCircleRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        TopicDetailVO result = topicService.detail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Java", result.getName());
        assertEquals("Java desc", result.getDescription());
        assertEquals("cover.png", result.getCoverImage());
        assertEquals(Integer.valueOf(2), result.getType());
        assertEquals(4, result.getAvailableTabs().size());
    }

    @Test
    void testDetailType1() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setType(1);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicCircleRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        TopicDetailVO result = topicService.detail(1L);

        assertEquals(3, result.getAvailableTabs().size());
        assertTrue(result.getAvailableTabs().contains("pin"));
        assertFalse(result.getAvailableTabs().contains("article"));
    }

    @Test
    void testDetailNotFound() {
        when(topicMapper.selectById(999L)).thenReturn(null);

        TopicDetailVO result = topicService.detail(999L);

        assertNull(result);
    }

    @Test
    void testDetailNullType() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setType(null);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicCircleRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        TopicDetailVO result = topicService.detail(1L);

        assertEquals(Integer.valueOf(1), result.getType());
        assertFalse(result.getAvailableTabs().contains("article"));
    }

    @Test
    void testDetailWithCircleRelations() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        when(topicMapper.selectById(1L)).thenReturn(topic);
        TopicCircleRelation rel = new TopicCircleRelation();
        rel.setCircleId(10L);
        rel.setTopicId(1L);
        when(topicCircleRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(rel));

        TopicDetailVO result = topicService.detail(1L);

        assertEquals(1, result.getCircleInfo().size());
        assertEquals(10L, result.getCircleInfo().get(0).getCircleId().longValue());
    }

    @Test
    void testDetailNullFields() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setDescription(null);
        topic.setCoverImage(null);
        topic.setBadge(null);
        topic.setType(null);
        topic.setViewCount(null);
        topic.setParticipantCount(null);
        topic.setPostCount(null);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicCircleRelationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        TopicDetailVO result = topicService.detail(1L);

        assertEquals("Java", result.getName());
        assertEquals("", result.getDescription());
        assertEquals("", result.getCoverImage());
        assertEquals("", result.getBadge());
        assertEquals(0L, result.getViewCount().longValue());
        assertEquals(0L, result.getParticipantCount().longValue());
        assertEquals(0L, result.getPostCount().longValue());
    }

    // ==================== feed() tests ====================

    @Test
    void testFeedPin() {
        ApPins pin = buildPin(1L);
        Page<ApPins> page = new Page<>(1, 11, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = topicService.feed(1L, "new", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
        assertEquals("pin", list.get(0).get("type"));
    }

    @Test
    void testFeedPinHot() {
        ApPins pin = buildPin(1L);
        Page<ApPins> page = new Page<>(1, 11, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = topicService.feed(1L, "hot", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
    }

    @Test
    void testFeedPinHasMore() {
        List<ApPins> pins = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            pins.add(buildPin((long) i));
        }
        Page<ApPins> page = new Page<>(1, 11, 11);
        page.setRecords(pins);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = topicService.feed(1L, "new", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(10, list.size());
        assertTrue((Boolean) result.get("has_more"));
    }

    @Test
    void testFeedPinEmpty() {
        Page<ApPins> page = new Page<>(1, 11, 0);
        page.setRecords(new ArrayList<>());
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = topicService.feed(1L, "new", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testFeedArticle() {
        TopicRelation rel = new TopicRelation();
        rel.setTargetId(100L);
        rel.setTargetType(1);
        rel.setTopicId(1L);
        when(topicRelationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<TopicRelation>(1, 11, 1) {{
                    setRecords(Collections.singletonList(rel));
                }});

        Map<String, Object> result = topicService.feed(1L, "article", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
        assertEquals("article", list.get(0).get("type"));
        assertEquals(100L, list.get(0).get("id"));
    }

    @Test
    void testFeedArticleHasMore() {
        List<TopicRelation> rels = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            TopicRelation rel = new TopicRelation();
            rel.setTargetId((long) i);
            rel.setTargetType(1);
            rel.setTopicId(1L);
            rels.add(rel);
        }
        when(topicRelationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<TopicRelation>(1, 11, 11) {{
                    setRecords(rels);
                }});

        Map<String, Object> result = topicService.feed(1L, "article", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(10, list.size());
        assertTrue((Boolean) result.get("has_more"));
    }

    @Test
    void testFeedPinNullFields() {
        ApPins pin = new ApPins();
        pin.setId(1L);
        pin.setUserId(null);
        pin.setUserName(null);
        pin.setUserAvatar(null);
        pin.setContent(null);
        pin.setLikes(null);
        pin.setComment(null);
        Page<ApPins> page = new Page<>(1, 11, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = topicService.feed(1L, "new", 0, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals("", list.get(0).get("userName"));
        assertEquals("", list.get(0).get("userAvatar"));
    }

    // ==================== incrView() tests ====================

    @Test
    void testIncrViewFirstTime() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(1L);
        when(cacheService.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setViewCount(100L);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));
    }

    @Test
    void testIncrViewRateLimited() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(6L);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));

        verify(topicMapper, never()).selectById(anyLong());
    }

    @Test
    void testIncrViewAtBoundary() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(5L);
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setViewCount(100L);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));
    }

    @Test
    void testIncrViewTopicNotFound() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(1L);
        when(cacheService.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(topicMapper.selectById(999L)).thenReturn(null);

        assertDoesNotThrow(() -> topicService.incrView(999L, 1L));

        verify(topicMapper, never()).updateById(any(ApTopic.class));
    }

    @Test
    void testIncrViewNullViewCount() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(1L);
        when(cacheService.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setViewCount(null);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));
    }

    @Test
    void testIncrViewSecondCall() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(2L);
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setViewCount(100L);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));
    }

    @Test
    void testIncrViewNullCount() {
        when(cacheService.incrBy(anyString(), anyLong())).thenReturn(null);
        ApTopic topic = buildTopic(1L, "Java", "hot");
        topic.setViewCount(100L);
        when(topicMapper.selectById(1L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        assertDoesNotThrow(() -> topicService.incrView(1L, 1L));
    }

    // ==================== search() tests ====================

    @Test
    void testSearch() {
        ApTopic topic = buildTopic(1L, "Java", "hot");
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        List<TopicRecommendVO> result = topicService.search("Java", 10);

        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getName());
    }

    @Test
    void testSearchEmptyKeyword() {
        List<TopicRecommendVO> result = topicService.search(null, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchBlankKeyword() {
        List<TopicRecommendVO> result = topicService.search("  ", 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchEmptyResult() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 0));

        List<TopicRecommendVO> result = topicService.search("unknown", 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchNullFields() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setBadge(null);
        topic.setParticipantCount(null);
        topic.setViewCount(null);
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        List<TopicRecommendVO> result = topicService.search("Java", 10);

        assertEquals("", result.get(0).getBadge());
        assertEquals(0L, result.get(0).getParticipantCount().longValue());
        assertEquals(0L, result.get(0).getViewCount().longValue());
    }

    // ==================== Helper ====================

    private ApTopic buildTopic(Long id, String name, String badge) {
        ApTopic topic = new ApTopic();
        topic.setId(id);
        topic.setName(name);
        topic.setBadge(badge);
        topic.setParticipantCount(100L);
        topic.setViewCount(1000L);
        topic.setPostCount(50);
        topic.setDescription("description");
        topic.setCoverImage("cover.png");
        topic.setType(1);
        topic.setStatus(1);
        topic.setIsRecommend(1);
        topic.setRecommendSort(1);
        return topic;
    }

    private ApPins buildPin(Long id) {
        ApPins pin = new ApPins();
        pin.setId(id);
        pin.setUserId(1L);
        pin.setUserName("user");
        pin.setUserAvatar("avatar.png");
        pin.setContent("content");
        pin.setLikes(10);
        pin.setComment(5);
        pin.setCreatedTime(new Date());
        return pin;
    }
}