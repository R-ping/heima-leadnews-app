package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.mapper.*;
import com.heima.content.service.TopicService;
import com.heima.model.article.dtos.*;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.PinsLinkPreviewVO;
import com.heima.model.article.vos.TopicRecommendVO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
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
class PinsPublicServiceImplTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private ApPinsLikeMapper apPinsLikeMapper;

    @Mock
    private ApPinsCommentMapper apPinsCommentMapper;

    @Mock
    private ApFollowMapper apFollowMapper;

    @Mock
    private ApUserCircleMapper apUserCircleMapper;

    @Mock
    private ApCircleMapper apCircleMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private ApUserLevelMapper apUserLevelMapper;

    @Mock
    private TopicService topicService;

    @Mock
    private INotificationClient notificationClient;

    @InjectMocks
    private PinsPublicServiceImpl pinsPublicService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        mockUser.setImage("https://avatar.jpg");

        // Inject baseMapper manually since @InjectMocks doesn't handle inherited MyBatis-Plus ServiceImpl fields
        ReflectionTestUtils.setField(pinsPublicService, "baseMapper", apPinsMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== list() tests ====================

    @Test
    void testListLatest() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
        assertEquals(1L, data.get("total"));
    }

    @Test
    void testListLatestEmpty() {
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("total"));
    }

    @Test
    void testListHot() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(5);
        pin.setComment(3);
        pin.setShare(2);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pin));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
    }

    @Test
    void testListHotEmpty() {
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, (int) data.get("total"));
    }

    @Test
    void testListFollowingNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsPublicService.list("following", 1, 10);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testListFollowingNoFollows() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apFollowMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("following", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, (int) data.get("total"));
    }

    @Test
    void testListFollowingWithFollows() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApFollow follow = new ApFollow();
        follow.setFollowUserId(2);
        when(apFollowMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(follow));

        ApPins pin = buildPin(2L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("following", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== publish() tests ====================

    @Test
    void testPublishSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test content");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testPublishNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testPublishEmptyContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishNullContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent(null);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testPublishWithTopic() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApTopic topic = new ApTopic();
        topic.setId(100L);
        topic.setPostCount(5);
        when(topicMapper.selectById(100L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setTopicId(100L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithCircle() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApCircle circle = new ApCircle();
        circle.setId(200L);
        circle.setPinsCount(3);
        when(apCircleMapper.selectById(200L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setCircleId(200L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithTopicNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(topicMapper.selectById(100L)).thenReturn(null);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setTopicId(100L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithCircleNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(apCircleMapper.selectById(200L)).thenReturn(null);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setCircleId(200L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithAllFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("full content");
        dto.setImageUrls("url1,url2");
        dto.setTopicTags("tag1,tag2");
        dto.setLinkUrl("https://example.com");
        dto.setLinkTitle("Example Link");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
        ApPins saved = (ApPins) result.getData();
        assertEquals("full content", saved.getContent());
        assertEquals("url1,url2", saved.getImageUrls());
        assertEquals("tag1,tag2", saved.getTopicTags());
        assertEquals("https://example.com", saved.getLinkUrl());
        assertEquals("Example Link", saved.getLinkTitle());
    }

    // ==================== like() tests ====================

    @Test
    void testLikeSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apPinsLikeMapper.insert(any(ApPinsLike.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(5);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(true);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testLikeAlreadyLiked() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPinsLike existLike = new ApPinsLike();
        existLike.setId(1L);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existLike);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(true);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
        verify(apPinsLikeMapper, never()).insert(any(ApPinsLike.class));
    }

    @Test
    void testUnlikeSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPinsLike existLike = new ApPinsLike();
        existLike.setId(10L);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existLike);
        when(apPinsLikeMapper.deleteById(10L)).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(5);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(false);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testUnlikeNotLiked() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(false);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
        verify(apPinsLikeMapper, never()).deleteById(any());
    }

    @Test
    void testLikeNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testLikeNullPinsId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(null);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLikePinsNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apPinsLikeMapper.insert(any(ApPinsLike.class))).thenReturn(1);
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(true);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment() tests ====================

    @Test
    void testCreateCommentSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setComment(3);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("nice post");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateCommentAsReply() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setComment(3);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ApPinsComment parent = new ApPinsComment();
        parent.setId(100L);
        parent.setReplyCount(2);
        when(apPinsCommentMapper.selectById(100L)).thenReturn(parent);
        when(apPinsCommentMapper.updateById(any(ApPinsComment.class))).thenReturn(1);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("reply");
        dto.setParentId(100L);

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCreateCommentNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("test");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentNullPinsId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(null);
        dto.setContent("test");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentEmptyContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreateCommentNullContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent(null);

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== commentList() tests ====================

    @Test
    void testCommentListSuccess() {
        ApPinsComment comment = new ApPinsComment();
        comment.setId(1L);
        comment.setPinsId(1L);
        comment.setContent("good");
        comment.setCreatedTime(new Date());

        when(apPinsCommentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPinsComment>(1, 10, 1) {{
                    setRecords(Collections.singletonList(comment));
                }});
        when(apPinsCommentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("list"));
    }

    @Test
    void testCommentListNullPinsId() {
        ResponseResult result = pinsPublicService.commentList(null, 1, 10);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCommentListWithReplies() {
        ApPinsComment parent = new ApPinsComment();
        parent.setId(1L);
        parent.setPinsId(1L);
        parent.setContent("parent");
        parent.setCreatedTime(new Date());

        ApPinsComment reply = new ApPinsComment();
        reply.setId(2L);
        reply.setPinsId(1L);
        reply.setParentId(1L);
        reply.setContent("reply");
        reply.setCreatedTime(new Date());

        when(apPinsCommentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPinsComment>(1, 10, 1) {{
                    setRecords(Collections.singletonList(parent));
                }});
        when(apPinsCommentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(reply));

        ResponseResult result = pinsPublicService.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== share() tests ====================

    @Test
    void testShareSuccess() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.incrementShare(1L)).thenReturn(1);

        PinsShareDTO dto = new PinsShareDTO();
        dto.setPinsId(1L);

        ResponseResult result = pinsPublicService.share(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testShareNullPinsId() {
        PinsShareDTO dto = new PinsShareDTO();
        dto.setPinsId(null);

        ResponseResult result = pinsPublicService.share(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testSharePinsNotFound() {
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        PinsShareDTO dto = new PinsShareDTO();
        dto.setPinsId(1L);

        ResponseResult result = pinsPublicService.share(dto);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== linkPreview() tests ====================

    @Test
    void testLinkPreviewSuccess() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("https://www.example.com/path");

        ResponseResult result = pinsPublicService.linkPreview(dto);

        assertEquals(200, result.getCode());
        PinsLinkPreviewVO vo = (PinsLinkPreviewVO) result.getData();
        assertEquals("https://www.example.com/path", vo.getUrl());
        assertEquals("www.example.com", vo.getDomain());
    }

    @Test
    void testLinkPreviewNullUrl() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl(null);

        ResponseResult result = pinsPublicService.linkPreview(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLinkPreviewEmptyUrl() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("");

        ResponseResult result = pinsPublicService.linkPreview(dto);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testLinkPreviewInvalidUrl() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("not-a-valid-url");

        ResponseResult result = pinsPublicService.linkPreview(dto);

        assertEquals(200, result.getCode());
        PinsLinkPreviewVO vo = (PinsLinkPreviewVO) result.getData();
        assertEquals("", vo.getDomain());
    }

    // ==================== sidebar() tests ====================

    @Test
    void testSidebarLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L, 8L);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", new ArrayList<TopicRecommendVO>());
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    @Test
    void testSidebarNotLoggedIn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", new ArrayList<TopicRecommendVO>());
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    @Test
    void testSidebarWithFeaturedPins() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        ApPins pin1 = buildPin(1L, ApPins.Status.PUBLISHED);
        pin1.setLikes(10);
        pin1.setComment(5);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(8);
        pin2.setComment(3);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin1, pin2));
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", new ArrayList<TopicRecommendVO>());
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    @Test
    void testSidebarTopicServiceException() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(topicService.recommend(0, 5)).thenThrow(new RuntimeException("service error"));

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    // ==================== topics() tests ====================

    @Test
    void testTopicsWithKeyword() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName("Java");
        topic.setPostCount(10);

        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        ResponseResult result = pinsPublicService.topics("Java", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testTopicsWithoutKeyword() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 0));

        ResponseResult result = pinsPublicService.topics(null, 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testTopicsEmptyKeyword() {
        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 0));

        ResponseResult result = pinsPublicService.topics("  ", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== circles() tests ====================

    @Test
    void testCircles() {
        ApCircle parent = new ApCircle();
        parent.setId(1L);
        parent.setName("技术");
        parent.setSortOrder(1);

        ApCircle child = new ApCircle();
        child.setId(10L);
        child.setName("Java");
        child.setParentId(1L);
        child.setIcon("java.png");
        child.setMemberCount(100);
        child.setPinsCount(50);
        child.setSortOrder(1);

        when(apCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(parent), Collections.singletonList(child));

        ResponseResult result = pinsPublicService.circles();

        assertEquals(200, result.getCode());
    }

    @Test
    void testCirclesEmpty() {
        when(apCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>(), new ArrayList<>());

        ResponseResult result = pinsPublicService.circles();

        assertEquals(200, result.getCode());
    }

    // ==================== uploadImage() tests ====================

    @Test
    void testUploadImageSuccess() {
        ResponseResult result = pinsPublicService.uploadImage("https://img.example.com/pic.jpg");

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals("https://img.example.com/pic.jpg", data.get("url"));
    }

    @Test
    void testUploadImageNullUrl() {
        ResponseResult result = pinsPublicService.uploadImage(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testUploadImageEmptyUrl() {
        ResponseResult result = pinsPublicService.uploadImage("");

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    // ==================== asyncReviewPins() tests ====================

    @Test
    void testAsyncReviewPinsSuccess() {
        ApPins pins = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        pinsPublicService.asyncReviewPins(pins);

        assertEquals(ApPins.Status.PUBLISHED.getCode(), pins.getStatus().byteValue());
        assertNotNull(pins.getReviewTime());
    }

    @Test
    void testAsyncReviewPinsException() {
        ApPins pins = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.updateById(any(ApPins.class)))
                .thenThrow(new RuntimeException("db error"))
                .thenReturn(1); // second call for FAIL status update

        assertDoesNotThrow(() -> pinsPublicService.asyncReviewPins(pins));
        assertEquals(ApPins.Status.FAIL.getCode(), pins.getStatus().byteValue());
    }

    @Test
    void testAsyncReviewPinsInnerException() {
        ApPins pins = buildPin(1L, ApPins.Status.SUBMIT);
        when(apPinsMapper.updateById(any(ApPins.class)))
                .thenThrow(new RuntimeException("db error"))
                .thenThrow(new RuntimeException("inner db error")); // inner catch block

        assertDoesNotThrow(() -> pinsPublicService.asyncReviewPins(pins));
        assertEquals(ApPins.Status.FAIL.getCode(), pins.getStatus().byteValue());
    }

    // ==================== calcHotScore() via listHot() tests ====================

    @Test
    void testCalcHotScoreWithLevel() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(2);
        pin.setShare(3);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(5);
        pin2.setComment(3);
        pin2.setShare(1);

        ApUserLevel level = new ApUserLevel();
        level.setUserId(1L);
        level.setDailyLevel(4);
        level.setPowerLevel(5);

        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(level));

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCalcHotScoreWithNullFields() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(null);
        pin.setComment(null);
        pin.setShare(null);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(1);
        pin2.setComment(1);
        pin2.setShare(1);

        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== sendPinsCreatedNotification coverage tests ====================

    @Test
    void testPublishWithNotificationSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap()))
                .thenReturn(ResponseResult.okResult());

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithNotificationFailure() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap()))
                .thenReturn(ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR));

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    @Test
    void testPublishWithNotificationException() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(anyMap()))
                .thenThrow(new RuntimeException("network error"));

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== parseStringList tests ====================

    @Test
    void testParseStringListNormal() {
        // parseStringList is private, tested via convertToVO which uses it for imageUrls/topicTags
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setImageUrls("url1,url2,url3");
        pin.setTopicTags("tag1, tag2");
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) data.get("list");
        assertFalse(list.isEmpty());
    }

    @Test
    void testParseStringListEmpty() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setImageUrls("");
        pin.setTopicTags("   ");
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testParseStringListNull() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setImageUrls(null);
        pin.setTopicTags(null);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== convertCommentToVO null fields tests ====================

    @Test
    void testCommentListWithNullFields() {
        ApPinsComment comment = new ApPinsComment();
        comment.setId(1L);
        comment.setPinsId(1L);
        comment.setUserId(null);
        comment.setUserName(null);
        comment.setUserAvatar(null);
        comment.setParentId(null);
        comment.setContent(null);
        comment.setLikeCount(null);
        comment.setReplyCount(null);
        comment.setCreatedTime(new Date());

        when(apPinsCommentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPinsComment>(1, 10, 1) {{
                    setRecords(Collections.singletonList(comment));
                }});
        when(apPinsCommentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== convertToVO with null likedPinsIds tests ====================

    @Test
    void testListLatestWithNullUser() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== getUserOrNull exception path tests ====================

    @Test
    void testGetUserOrNullException() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenThrow(new RuntimeException("thread error"));
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== listHot with multiple sorted pins ====================

    @Test
    void testListHotMultiplePins() {
        ApPins pin1 = buildPin(1L, ApPins.Status.PUBLISHED);
        pin1.setLikes(1);
        pin1.setComment(1);
        pin1.setShare(1);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(10);
        pin2.setComment(5);
        pin2.setShare(3);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin1, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2L, data.get("total"));
    }

    // ==================== listHot second page ====================

    @Test
    void testListHotSecondPage() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(1);
        pin.setShare(1);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pin));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 2, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) data.get("list");
        assertTrue(list.isEmpty());
    }

    // ==================== sidebar with null user fields ====================

    @Test
    void testSidebarLoggedInNullAuthorId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apUserCircleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(apFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", new ArrayList<TopicRecommendVO>());
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    // ==================== calcHotScore: level not found in map ====================

    @Test
    void testCalcHotScoreLevelNotFound() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(2);
        pin.setShare(3);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(3);
        pin2.setComment(2);
        pin2.setShare(1);
        // pin authorId=1, but levelMap is empty (no entry for 1)
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== calcHotScore: level exists but dailyLevel/powerLevel null ====================

    @Test
    void testCalcHotScoreLevelNullDailyAndPower() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(2);
        pin.setShare(3);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(3);
        pin2.setComment(2);
        pin2.setShare(1);

        ApUserLevel level = new ApUserLevel();
        level.setUserId(1L);
        level.setDailyLevel(null);
        level.setPowerLevel(null);

        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(level));

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== calcHotScore: individual null fields ====================

    @Test
    void testCalcHotScoreLikesNull() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(null);
        pin.setComment(2);
        pin.setShare(3);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(1);
        pin2.setComment(1);
        pin2.setShare(1);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCalcHotScoreCommentNull() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(null);
        pin.setShare(3);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(1);
        pin2.setComment(1);
        pin2.setShare(1);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testCalcHotScoreShareNull() {
        // Use 2 pins to trigger the sorted() comparator
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(1);
        pin.setComment(2);
        pin.setShare(null);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(1);
        pin2.setComment(1);
        pin2.setShare(1);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2));
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== listHot: no authorIds (empty) ====================

    @Test
    void testListHotNoAuthorIds() {
        // Pin with null authorId => no authorIds extracted
        ApPins pin = new ApPins();
        pin.setId(1L);
        pin.setAuthorId(null);
        pin.setAuthorName("");
        pin.setUserName("");
        pin.setContent("test");
        pin.setLikes(0);
        pin.setComment(0);
        pin.setShare(0);
        pin.setStatus(ApPins.Status.PUBLISHED.getCode());
        pin.setIsDeleted(false);
        pin.setCreatedTime(new Date());
        pin.setPublishTime(new Date());

        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pin));

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== listHot: duplicate userIds in level map ====================

    @Test
    void testListHotDuplicateLevelEntries() {
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pin));

        ApUserLevel level1 = new ApUserLevel();
        level1.setUserId(1L);
        level1.setDailyLevel(1);
        level1.setPowerLevel(2);
        ApUserLevel level2 = new ApUserLevel();
        level2.setUserId(1L);
        level2.setDailyLevel(3);
        level2.setPowerLevel(4);
        when(apUserLevelMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(level1, level2));

        ResponseResult result = pinsPublicService.list("hot", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== publish: null user fields ====================

    @Test
    void testPublishWithNullUserFields() {
        ApUser userWithNulls = new ApUser();
        userWithNulls.setId(1);
        userWithNulls.setNickname(null);
        userWithNulls.setImage(null);
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(userWithNulls);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
        ApPins saved = (ApPins) result.getData();
        assertEquals("", saved.getAuthorName());
        assertEquals("", saved.getAuthorImage());
        assertEquals("", saved.getUserName());
        assertEquals("", saved.getUserAvatar());
    }

    // ==================== publish: null optional fields ====================

    @Test
    void testPublishWithNullOptionalFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setImageUrls(null);
        dto.setTopicTags(null);
        dto.setLinkUrl(null);
        dto.setLinkTitle(null);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
        ApPins saved = (ApPins) result.getData();
        assertEquals("", saved.getImageUrls());
        assertEquals("", saved.getTopicTags());
        assertEquals("", saved.getLinkUrl());
        assertEquals("", saved.getLinkTitle());
    }

    // ==================== publish: null postCount on topic ====================

    @Test
    void testPublishWithNullTopicPostCount() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApTopic topic = new ApTopic();
        topic.setId(100L);
        topic.setPostCount(null);
        when(topicMapper.selectById(100L)).thenReturn(topic);
        when(topicMapper.updateById(any(ApTopic.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setTopicId(100L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== publish: null pinsCount on circle ====================

    @Test
    void testPublishWithNullCirclePinsCount() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApCircle circle = new ApCircle();
        circle.setId(200L);
        circle.setPinsCount(null);
        when(apCircleMapper.selectById(200L)).thenReturn(circle);
        when(apCircleMapper.updateById(any(ApCircle.class))).thenReturn(1);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");
        dto.setCircleId(200L);

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== sendPinsCreatedNotification: null notificationClient ====================

    @Test
    void testPublishWithNullNotificationClient() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        // Set notificationClient to null
        ReflectionTestUtils.setField(pinsPublicService, "notificationClient", null);

        PinsPublishDTO dto = new PinsPublishDTO();
        dto.setContent("test");

        ResponseResult result = pinsPublicService.publish(dto);

        assertEquals(200, result.getCode());
        // Restore
        ReflectionTestUtils.setField(pinsPublicService, "notificationClient", notificationClient);
    }

    // ==================== like: null likes on pins ====================

    @Test
    void testLikePinsWithNullLikes() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apPinsLikeMapper.insert(any(ApPinsLike.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(null);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(true);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== unlike: pins not found ====================

    @Test
    void testUnlikePinsNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPinsLike existLike = new ApPinsLike();
        existLike.setId(10L);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existLike);
        when(apPinsLikeMapper.deleteById(10L)).thenReturn(1);
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(false);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== unlike: null likes on pins ====================

    @Test
    void testUnlikePinsWithNullLikes() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPinsLike existLike = new ApPinsLike();
        existLike.setId(10L);
        when(apPinsLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existLike);
        when(apPinsLikeMapper.deleteById(10L)).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(null);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsLikeDTO dto = new PinsLikeDTO();
        dto.setPinsId(1L);
        dto.setLiked(false);

        ResponseResult result = pinsPublicService.like(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment: pins not found ====================

    @Test
    void testCreateCommentPinsNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("test");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment: null comment count on pins ====================

    @Test
    void testCreateCommentPinsNullComment() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setComment(null);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("test");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment: reply with parent not found ====================

    @Test
    void testCreateCommentReplyParentNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);
        when(apPinsCommentMapper.selectById(100L)).thenReturn(null);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("reply");
        dto.setParentId(100L);

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment: parent with null replyCount ====================

    @Test
    void testCreateCommentParentNullReplyCount() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ApPinsComment parent = new ApPinsComment();
        parent.setId(100L);
        parent.setReplyCount(null);
        when(apPinsCommentMapper.selectById(100L)).thenReturn(parent);
        when(apPinsCommentMapper.updateById(any(ApPinsComment.class))).thenReturn(1);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("reply");
        dto.setParentId(100L);

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
    }

    // ==================== createComment: null user fields ====================

    @Test
    void testCreateCommentWithNullUserFields() {
        ApUser userWithNulls = new ApUser();
        userWithNulls.setId(1);
        userWithNulls.setNickname(null);
        userWithNulls.setImage(null);
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(userWithNulls);
        when(apPinsCommentMapper.insert(any(ApPinsComment.class))).thenReturn(1);

        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        PinsCommentDTO dto = new PinsCommentDTO();
        dto.setPinsId(1L);
        dto.setContent("test");

        ResponseResult result = pinsPublicService.createComment(dto);

        assertEquals(200, result.getCode());
        ApPinsComment comment = (ApPinsComment) result.getData();
        assertEquals("", comment.getUserName());
        assertEquals("", comment.getUserAvatar());
    }

    // ==================== commentList: empty pageComments ====================

    @Test
    void testCommentListEmpty() {
        when(apPinsCommentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPinsComment>(1, 10, 0));

        ResponseResult result = pinsPublicService.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("total"));
    }

    // ==================== linkPreview: invalid URI (catch branch) ====================

    @Test
    void testLinkPreviewInvalidUri() {
        PinsLinkPreviewDTO dto = new PinsLinkPreviewDTO();
        dto.setUrl("://invalid");

        ResponseResult result = pinsPublicService.linkPreview(dto);

        assertEquals(200, result.getCode());
        PinsLinkPreviewVO vo = (PinsLinkPreviewVO) result.getData();
        assertEquals("", vo.getDomain());
    }

    // ==================== sidebar: featured pins with null likes/comment ====================

    @Test
    void testSidebarWithNullFeaturedScores() {
        // Use 3 pins with mixed null/non-null likes and comment to trigger the sorted() comparator
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setLikes(null);
        pin.setComment(null);
        ApPins pin2 = buildPin(2L, ApPins.Status.PUBLISHED);
        pin2.setLikes(null);
        pin2.setComment(5);
        ApPins pin3 = buildPin(3L, ApPins.Status.PUBLISHED);
        pin3.setLikes(3);
        pin3.setComment(null);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(pin, pin2, pin3));
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", new ArrayList<TopicRecommendVO>());
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    // ==================== sidebar: topics list is null ====================

    @Test
    void testSidebarTopicServiceNullList() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        when(apPinsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(topicService.recommend(0, 5)).thenReturn(new HashMap<String, Object>() {{
            put("list", null);
        }});

        ResponseResult result = pinsPublicService.sidebar();

        assertEquals(200, result.getCode());
    }

    // ==================== convertToVOList: null list ====================

    @Test
    void testListLatestWithNullPinsList() {
        // This tests convertToVOList with empty records (pinsList empty)
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== convertToVO: null string fields ====================

    @Test
    void testConvertToVONullStringFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = new ApPins();
        pin.setId(1L);
        pin.setUserId(null);
        pin.setAuthorId(1L);
        pin.setUserName(null);
        pin.setUserAvatar(null);
        pin.setAuthorName(null);
        pin.setAuthorImage(null);
        pin.setContent(null);
        pin.setLinkUrl(null);
        pin.setLinkTitle(null);
        pin.setLikes(0);
        pin.setComment(0);
        pin.setShare(0);
        pin.setStatus(ApPins.Status.PUBLISHED.getCode());
        pin.setIsDeleted(false);
        pin.setCreatedTime(new Date());
        pin.setPublishTime(new Date());

        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== convertToVO: null likedPinsIds ====================

    @Test
    void testListLatestWithNullLikedPinsIds() {
        // user is null => likedPinsIds is not queried, but finalLikedPinsIds is a non-null empty set
        // This tests the likedPinsIds != null is true, but contains returns false
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== parseStringList: empty element in middle ====================

    @Test
    void testParseStringListEmptyElement() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        pin.setImageUrls("url1,,url2");
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    // ==================== Direct private method tests for full branch coverage ====================

    @Test
    void testConvertCommentToVONullFields() {
        // Use reflection to directly test convertCommentToVO with null fields
        ApPinsComment comment = new ApPinsComment();
        comment.setId(1L);
        comment.setPinsId(1L);
        comment.setUserId(null);
        comment.setUserName(null);
        comment.setUserAvatar(null);
        comment.setParentId(null);
        comment.setContent("test");
        comment.setLikeCount(null);
        comment.setReplyCount(null);
        comment.setCreatedTime(new Date());

        Object result = ReflectionTestUtils.invokeMethod(pinsPublicService, "convertCommentToVO", comment);
        assertNotNull(result);
    }

    @Test
    void testCommentListWithNonNullFields() {
        // Test convertCommentToVO with non-null userName, userAvatar, likeCount, replyCount
        ApPinsComment comment = new ApPinsComment();
        comment.setId(1L);
        comment.setPinsId(1L);
        comment.setUserId(1);
        comment.setUserName("Alice");
        comment.setUserAvatar("https://avatar.jpg");
        comment.setParentId(0L);
        comment.setContent("test");
        comment.setLikeCount(5);
        comment.setReplyCount(2);
        comment.setCreatedTime(new Date());

        when(apPinsCommentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPinsComment>(1, 10, 1) {{
                    setRecords(Collections.singletonList(comment));
                }});
        when(apPinsCommentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        ResponseResult result = pinsPublicService.commentList(1L, 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListLatestWithLikedPin() {
        // Test convertToVO with likedPinsIds containing the pin (covers likedPinsIds.contains == true)
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});
        ApPinsLike like = new ApPinsLike();
        like.setPinsId(1L);
        like.setUserId(mockUser.getId());
        when(apPinsLikeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(like));

        ResponseResult result = pinsPublicService.list("latest", 1, 10);

        assertEquals(200, result.getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConvertToVOListWithNullList() {
        // Use reflection to directly test convertToVOList with null list
        List<Object> result = (List<Object>) ReflectionTestUtils.invokeMethod(
                pinsPublicService, "convertToVOList", (List<ApPins>) null, (ApUser) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToVOWithNullLikedPinsIds() {
        // Use reflection to directly test convertToVO with null likedPinsIds
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        Object result = ReflectionTestUtils.invokeMethod(pinsPublicService, "convertToVO", pin, (Set<Long>) null);
        assertNotNull(result);
    }

    // ==================== topics: null name and postCount ====================

    @Test
    void testTopicsWithNullFields() {
        ApTopic topic = new ApTopic();
        topic.setId(1L);
        topic.setName(null);
        topic.setPostCount(null);

        when(topicMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApTopic>(1, 10, 1) {{
                    setRecords(Collections.singletonList(topic));
                }});

        ResponseResult result = pinsPublicService.topics("test", 1, 10);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertEquals("", list.get(0).get("name"));
        assertEquals(0, list.get(0).get("count"));
    }

    // ==================== circles: null fields on parent and child ====================

    @Test
    void testCirclesWithNullFields() {
        ApCircle parent = new ApCircle();
        parent.setId(1L);
        parent.setName(null);
        parent.setSortOrder(1);

        ApCircle child = new ApCircle();
        child.setId(10L);
        child.setName(null);
        child.setParentId(1L);
        child.setIcon(null);
        child.setMemberCount(null);
        child.setPinsCount(null);
        child.setSortOrder(1);

        when(apCircleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(parent), Collections.singletonList(child));

        ResponseResult result = pinsPublicService.circles();

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.getData();
        assertEquals("", data.get(0).get("name"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> circles = (List<Map<String, Object>>) data.get(0).get("circles");
        assertEquals("", circles.get(0).get("name"));
        assertEquals("", circles.get(0).get("icon"));
        assertEquals(0, circles.get(0).get("memberCount"));
        assertEquals(0, circles.get(0).get("pinsCount"));
    }

    // ==================== Helper ====================

    private ApPins buildPin(Long id, ApPins.Status status) {
        ApPins pin = new ApPins();
        pin.setId(id);
        pin.setAuthorId(id);
        pin.setAuthorName("author" + id);
        pin.setAuthorImage("");
        pin.setUserName("author" + id);
        pin.setUserAvatar("");
        pin.setContent("content " + id);
        pin.setLikes(0);
        pin.setComment(0);
        pin.setShare(0);
        pin.setStatus(status.getCode());
        pin.setIsDeleted(false);
        pin.setCreatedTime(new Date());
        pin.setPublishTime(new Date());
        return pin;
    }
}