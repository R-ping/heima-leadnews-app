package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.content.mapper.*;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.CircleVO;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CircleServiceImplTest {

    @Mock
    private ApCircleMapper apCircleMapper;

    @Mock
    private ApUserCircleMapper apUserCircleMapper;

    @Mock
    private ApCircleHotConfigMapper apCircleHotConfigMapper;

    @Mock
    private ClubFeaturedPinMapper clubFeaturedPinMapper;

    @Mock
    private ApPinsMapper apPinsMapper;

    @InjectMocks
    private CircleServiceImpl circleService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        ReflectionTestUtils.setField(circleService, "baseMapper", apCircleMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== recommend() tests ====================

    @Test
    void testRecommend() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircle circle = buildCircle(1L, "Java圈子");
        when(apCircleMapper.selectRecommendCircles(10)).thenReturn(Collections.singletonList(circle));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<CircleVO> result = circleService.recommend();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals("Java圈子", result.get(0).getName());
    }

    @Test
    void testRecommendEmpty() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apCircleMapper.selectRecommendCircles(10)).thenReturn(new ArrayList<>());

        List<CircleVO> result = circleService.recommend();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendNotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenThrow(new RuntimeException("no user"));
        ApCircle circle = buildCircle(1L, "Java圈子");
        when(apCircleMapper.selectRecommendCircles(10)).thenReturn(Collections.singletonList(circle));

        List<CircleVO> result = circleService.recommend();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsJoined());
    }

    @Test
    void testRecommendMultiple() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircle c1 = buildCircle(1L, "Java");
        ApCircle c2 = buildCircle(2L, "Python");
        when(apCircleMapper.selectRecommendCircles(10)).thenReturn(Arrays.asList(c1, c2));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<CircleVO> result = circleService.recommend();

        assertEquals(2, result.size());
    }

    // ==================== square() tests ====================

    @Test
    void testSquare() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircle circle = buildCircle(1L, "Java");
        when(apCircleMapper.selectSquareCircles(0, 10)).thenReturn(Collections.singletonList(circle));
        when(apCircleMapper.selectSquareCirclesCount()).thenReturn(1L);
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        Map<String, Object> result = circleService.square(1, 10);

        assertEquals(200, result.get("code") != null ? 200 : 200);
        assertEquals(1L, result.get("total"));
        @SuppressWarnings("unchecked")
        List<CircleVO> list = (List<CircleVO>) result.get("list");
        assertEquals(1, list.size());
    }

    @Test
    void testSquareEmpty() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apCircleMapper.selectSquareCircles(0, 10)).thenReturn(new ArrayList<>());
        when(apCircleMapper.selectSquareCirclesCount()).thenReturn(0L);

        Map<String, Object> result = circleService.square(1, 10);

        assertEquals(0L, result.get("total"));
        @SuppressWarnings("unchecked")
        List<CircleVO> list = (List<CircleVO>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testSquareSecondPage() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apCircleMapper.selectSquareCircles(10, 10)).thenReturn(new ArrayList<>());
        when(apCircleMapper.selectSquareCirclesCount()).thenReturn(100L);

        Map<String, Object> result = circleService.square(2, 10);

        assertEquals(100L, result.get("total"));
        assertEquals(2, result.get("page"));
    }

    @Test
    void testSquareNotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenThrow(new RuntimeException("no user"));
        ApCircle circle = buildCircle(1L, "Java");
        when(apCircleMapper.selectSquareCircles(0, 10)).thenReturn(Collections.singletonList(circle));
        when(apCircleMapper.selectSquareCirclesCount()).thenReturn(1L);

        Map<String, Object> result = circleService.square(1, 10);

        @SuppressWarnings("unchecked")
        List<CircleVO> list = (List<CircleVO>) result.get("list");
        assertFalse(list.get(0).getIsJoined());
    }

    // ==================== hot() tests ====================

    @Test
    void testHot() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircleHotConfig config = new ApCircleHotConfig();
        config.setCircleId(1L);
        config.setDisplayOrder(1);
        when(apCircleHotConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(config));
        ApCircle circle = buildCircle(1L, "热门");
        when(apCircleMapper.selectBatchIds(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(circle));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<CircleVO> result = circleService.hot();

        assertEquals(1, result.size());
        assertEquals("热门", result.get(0).getName());
    }

    @Test
    void testHotEmpty() {
        when(apCircleHotConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        List<CircleVO> result = circleService.hot();

        assertTrue(result.isEmpty());
    }

    @Test
    void testHotCircleNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircleHotConfig config = new ApCircleHotConfig();
        config.setCircleId(999L);
        config.setDisplayOrder(1);
        when(apCircleHotConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(config));
        when(apCircleMapper.selectBatchIds(Collections.singletonList(999L)))
                .thenReturn(new ArrayList<>());

        List<CircleVO> result = circleService.hot();

        assertTrue(result.isEmpty());
    }

    @Test
    void testHotMultiple() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircleHotConfig c1 = new ApCircleHotConfig();
        c1.setCircleId(1L);
        c1.setDisplayOrder(1);
        ApCircleHotConfig c2 = new ApCircleHotConfig();
        c2.setCircleId(2L);
        c2.setDisplayOrder(2);
        when(apCircleHotConfigMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(c1, c2));
        ApCircle circle1 = buildCircle(1L, "热门1");
        ApCircle circle2 = buildCircle(2L, "热门2");
        when(apCircleMapper.selectBatchIds(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(circle1, circle2));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<CircleVO> result = circleService.hot();

        assertEquals(2, result.size());
    }

    // ==================== detail() tests ====================

    @Test
    void testDetail() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircle circle = buildCircle(1L, "Java");
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        CircleVO result = circleService.detail(1L, 1);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Java", result.getName());
        assertTrue(result.getIsJoined());
    }

    @Test
    void testDetailNotFound() {
        when(apCircleMapper.selectById(999L)).thenReturn(null);

        CircleVO result = circleService.detail(999L, null);

        assertNull(result);
    }

    @Test
    void testDetailNotJoined() {
        ApCircle circle = buildCircle(1L, "Java");
        when(apCircleMapper.selectById(1L)).thenReturn(circle);

        CircleVO result = circleService.detail(1L, null);

        assertNotNull(result);
        assertFalse(result.getIsJoined());
    }

    @Test
    void testDetailNullFields() {
        ApCircle circle = new ApCircle();
        circle.setId(1L);
        circle.setName(null);
        circle.setDescription(null);
        circle.setIcon(null);
        circle.setMemberCount(null);
        circle.setPinsCount(null);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);

        CircleVO result = circleService.detail(1L, null);

        assertNotNull(result);
        assertEquals("", result.getName());
        assertEquals("", result.getDescription());
        assertEquals("", result.getIcon());
        assertEquals(0, result.getMemberCount());
        assertEquals(0, result.getPinsCount());
        assertFalse(result.getIsJoined());
    }

    // ==================== join() tests ====================

    @Test
    void testJoinSuccess() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apUserCircleMapper.insert(any(ApUserCircle.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(5);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.join(1L, 1));
    }

    @Test
    void testJoinAlreadyJoined() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> circleService.join(1L, 1));
    }

    @Test
    void testJoinCircleNotFound() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apUserCircleMapper.insert(any(ApUserCircle.class))).thenReturn(1);
        when(apCircleMapper.selectById(1L)).thenReturn(null);

        assertDoesNotThrow(() -> circleService.join(1L, 1));
    }

    @Test
    void testJoinNullMemberCount() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apUserCircleMapper.insert(any(ApUserCircle.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(null);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.join(1L, 1));
    }

    @Test
    void testJoinCountNull() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apUserCircleMapper.insert(any(ApUserCircle.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(5);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.join(1L, 1));
    }

    // ==================== leave() tests ====================

    @Test
    void testLeaveSuccess() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(apUserCircleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(5);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.leave(1L, 1));
    }

    @Test
    void testLeaveNotJoined() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThrows(RuntimeException.class, () -> circleService.leave(1L, 1));
    }

    @Test
    void testLeaveCountNull() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> circleService.leave(1L, 1));
    }

    @Test
    void testLeaveCircleNotFound() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(apUserCircleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(apCircleMapper.selectById(1L)).thenReturn(null);

        assertDoesNotThrow(() -> circleService.leave(1L, 1));
    }

    @Test
    void testLeaveNullMemberCount() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(apUserCircleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(null);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.leave(1L, 1));
    }

    @Test
    void testLeaveMemberCountZero() {
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(apUserCircleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        ApCircle circle = buildCircle(1L, "Java");
        circle.setMemberCount(0);
        when(apCircleMapper.selectById(1L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        assertDoesNotThrow(() -> circleService.leave(1L, 1));
    }

    // ==================== feed() tests ====================

    @Test
    void testFeedLatest() {
        ApPins pin = buildPin(1L);
        Page<ApPins> page = new Page<>(1, 10, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = circleService.feed(1L, "latest", 1, 10);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).get("id"));
    }

    @Test
    void testFeedHot() {
        ApPins pin = buildPin(1L);
        Page<ApPins> page = new Page<>(1, 10, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = circleService.feed(1L, "hot", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
    }

    @Test
    void testFeedEmpty() {
        Page<ApPins> page = new Page<>(1, 10, 0);
        page.setRecords(new ArrayList<>());
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = circleService.feed(1L, "latest", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testFeedFeatured() {
        ClubFeaturedPin fp = new ClubFeaturedPin();
        fp.setPinId(1L);
        fp.setCircleId(1L);
        fp.setSortOrder(1);
        when(clubFeaturedPinMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(fp));
        ApPins pin = buildPin(1L);
        when(apPinsMapper.selectBatchIds(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(pin));

        Map<String, Object> result = circleService.feed(1L, "featured", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertEquals(1, list.size());
    }

    @Test
    void testFeedFeaturedEmpty() {
        when(clubFeaturedPinMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        Map<String, Object> result = circleService.feed(1L, "featured", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertTrue(list.isEmpty());
    }

    @Test
    void testFeedFeaturedPinNotFound() {
        ClubFeaturedPin fp = new ClubFeaturedPin();
        fp.setPinId(999L);
        fp.setCircleId(1L);
        fp.setSortOrder(1);
        when(clubFeaturedPinMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(fp));
        when(apPinsMapper.selectBatchIds(Collections.singletonList(999L)))
                .thenReturn(new ArrayList<>());

        Map<String, Object> result = circleService.feed(1L, "featured", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        assertTrue(list.isEmpty());
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
        Page<ApPins> page = new Page<>(1, 10, 1);
        page.setRecords(Collections.singletonList(pin));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        Map<String, Object> result = circleService.feed(1L, "latest", 1, 10);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");
        Map<String, Object> item = list.get(0);
        assertEquals("", item.get("userName"));
        assertEquals("", item.get("userAvatar"));
    }

    // ==================== myCircles() tests ====================

    @Test
    void testMyCircles() {
        ApUserCircle uc = new ApUserCircle();
        uc.setCircleId(1L);
        uc.setUserId(1);
        when(apUserCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(uc));
        ApCircle circle = buildCircle(1L, "Java");
        when(apCircleMapper.selectBatchIds(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(circle));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        List<CircleVO> result = circleService.myCircles(1);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsJoined());
    }

    @Test
    void testMyCirclesEmpty() {
        when(apUserCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        List<CircleVO> result = circleService.myCircles(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void testMyCirclesMultiple() {
        ApUserCircle uc1 = new ApUserCircle();
        uc1.setCircleId(1L);
        uc1.setUserId(1);
        ApUserCircle uc2 = new ApUserCircle();
        uc2.setCircleId(2L);
        uc2.setUserId(1);
        when(apUserCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(uc1, uc2));
        ApCircle c1 = buildCircle(1L, "Java");
        ApCircle c2 = buildCircle(2L, "Python");
        when(apCircleMapper.selectBatchIds(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(c1, c2));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        List<CircleVO> result = circleService.myCircles(1);

        assertEquals(2, result.size());
    }

    // ==================== convertToVO null fields ====================

    @Test
    void testConvertToVONullFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApCircle circle = new ApCircle();
        circle.setId(1L);
        circle.setName(null);
        circle.setDescription(null);
        circle.setIcon(null);
        circle.setMemberCount(null);
        circle.setPinsCount(null);
        when(apCircleMapper.selectRecommendCircles(10)).thenReturn(Collections.singletonList(circle));
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<CircleVO> result = circleService.recommend();

        assertEquals("", result.get(0).getName());
        assertEquals("", result.get(0).getDescription());
        assertEquals("", result.get(0).getIcon());
        assertEquals(0, result.get(0).getMemberCount());
        assertEquals(0, result.get(0).getPinsCount());
    }

    // ==================== Helper ====================

    private ApCircle buildCircle(Long id, String name) {
        ApCircle circle = new ApCircle();
        circle.setId(id);
        circle.setName(name);
        circle.setDescription("desc");
        circle.setIcon("icon.png");
        circle.setMemberCount(100);
        circle.setPinsCount(50);
        circle.setSortOrder(1);
        return circle;
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