package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.apis.notification.INotificationClient;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.model.article.pojos.ApPins;
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
class PinsServiceImplTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private INotificationClient notificationClient;

    @InjectMocks
    private PinsServiceImpl pinsService;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        threadLocalMock = Mockito.mockStatic(AppThreadLocalUtil.class);
        mockUser = new ApUser();
        mockUser.setId(1);
        mockUser.setNickname("testUser");
        mockUser.setImage("https://avatar.jpg");
        ReflectionTestUtils.setField(pinsService, "baseMapper", apPinsMapper);
    }

    @AfterEach
    void tearDown() {
        threadLocalMock.close();
    }

    // ==================== list() tests ====================

    @Test
    void testListOwnPins() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsService.list(null, 1, 10, null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListOthersPins() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(2L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 1) {{
                    setRecords(Collections.singletonList(pin));
                }});

        ResponseResult result = pinsService.list(2L, 1, 10, null);

        assertEquals(200, result.getCode());
    }

    @Test
    void testListNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.list(null, 1, 10, null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testListWithPublishedStatus() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, "published");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithReviewingStatus() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, "reviewing");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithRejectedStatus() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, "rejected");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithUnknownStatus() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, "unknown");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListWithEmptyStatus() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, "");

        assertEquals(200, result.getCode());
    }

    @Test
    void testListEmpty() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<ApPins>(1, 10, 0));

        ResponseResult result = pinsService.list(null, 1, 10, null);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("total"));
    }

    // ==================== statistics() tests ====================

    @Test
    void testStatisticsOwn() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 5L, 2L, 1L);

        ResponseResult result = pinsService.statistics(null);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(10L, data.get("total"));
        assertEquals(5L, data.get("published"));
        assertEquals(2L, data.get("reviewing"));
        assertEquals(1L, data.get("rejected"));
    }

    @Test
    void testStatisticsOthers() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L, 2L, 1L, 0L);

        ResponseResult result = pinsService.statistics(2L);

        assertEquals(200, result.getCode());
    }

    @Test
    void testStatisticsNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.statistics(null);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    // ==================== createPins() tests ====================

    @Test
    void testCreatePinsSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApPins pins = new ApPins();
        pins.setContent("test content");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testCreatePinsNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ApPins pins = new ApPins();
        pins.setContent("test");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testCreatePinsNullContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ApPins pins = new ApPins();
        pins.setContent(null);

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreatePinsEmptyContent() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ApPins pins = new ApPins();
        pins.setContent("");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testCreatePinsWithAllFields() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);

        ApPins pins = new ApPins();
        pins.setContent("full content");
        pins.setImageUrls("url1,url2");
        pins.setTopicTags("tag1");
        pins.setLinkUrl("https://example.com");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        ApPins saved = (ApPins) result.getData();
        assertEquals("full content", saved.getContent());
    }

    @Test
    void testCreatePinsNotificationClientNull() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        ReflectionTestUtils.setField(pinsService, "notificationClient", null);

        ApPins pins = new ApPins();
        pins.setContent("test content");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        // restore for other tests
        ReflectionTestUtils.setField(pinsService, "notificationClient", notificationClient);
    }

    @Test
    void testCreatePinsNotificationSuccess() throws Exception {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(any(Map.class)))
                .thenReturn(ResponseResult.okResult("ok"));

        ApPins pins = new ApPins();
        pins.setContent("test content");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(notificationClient).createNotification(any(Map.class));
    }

    @Test
    void testCreatePinsNotificationException() throws Exception {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(any(Map.class)))
                .thenThrow(new RuntimeException("notification service error"));

        ApPins pins = new ApPins();
        pins.setContent("test content");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testCreatePinsNotificationErrorCode() throws Exception {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.insert(any(ApPins.class))).thenReturn(1);
        when(notificationClient.createNotification(any(Map.class)))
                .thenReturn(ResponseResult.errorResult(500, "service error"));

        ApPins pins = new ApPins();
        pins.setContent("test content");

        ResponseResult result = pinsService.createPins(pins);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    // ==================== deletePins() tests ====================

    @Test
    void testDeletePinsSuccess() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(1L, ApPins.Status.PUBLISHED);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = pinsService.deletePins(1L);

        assertEquals(200, result.getCode());
        assertTrue(pin.getIsDeleted());
    }

    @Test
    void testDeletePinsNeedLogin() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.deletePins(1L);

        assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
    }

    @Test
    void testDeletePinsNullId() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

        ResponseResult result = pinsService.deletePins(null);

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    void testDeletePinsNotFound() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        when(apPinsMapper.selectById(1L)).thenReturn(null);

        ResponseResult result = pinsService.deletePins(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    @Test
    void testDeletePinsNotOwner() {
        threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
        ApPins pin = buildPin(2L, ApPins.Status.PUBLISHED);
        pin.setAuthorId(2L);
        when(apPinsMapper.selectById(1L)).thenReturn(pin);

        ResponseResult result = pinsService.deletePins(1L);

        assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
    }

    // ==================== Helper ====================

    private ApPins buildPin(Long id, ApPins.Status status) {
        ApPins pin = new ApPins();
        pin.setId(id);
        pin.setAuthorId(1L);
        pin.setAuthorName("author" + id);
        pin.setAuthorImage("");
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