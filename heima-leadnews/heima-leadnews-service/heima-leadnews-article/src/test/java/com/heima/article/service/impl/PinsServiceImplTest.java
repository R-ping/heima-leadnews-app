package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.apis.notification.INotificationClient;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("沸点服务测试")
class PinsServiceImplTest {

    @Mock
    private ApPinsMapper apPinsMapper;

    @Mock
    private INotificationClient notificationClient;

    @InjectMocks
    private PinsServiceImpl pinsService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMocked;
    private ApUser testUser;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ApPins.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pinsService, "baseMapper", apPinsMapper);
        testUser = new ApUser();
        testUser.setId(1);
        testUser.setNickname("测试用户");
        testUser.setImage("https://example.com/avatar.jpg");
        appThreadLocalUtilMocked = mockStatic(AppThreadLocalUtil.class);
        appThreadLocalUtilMocked.when(AppThreadLocalUtil::getUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        if (appThreadLocalUtilMocked != null) {
            appThreadLocalUtilMocked.close();
        }
    }

    // ==================== list ====================

    @Test
    @DisplayName("查询沸点列表 - 用户未登录返回需要登录")
    void testList_NotLoggedIn() {
        appThreadLocalUtilMocked.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.list(1L, 1, 10, null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询沸点列表 - 查询成功")
    void testList_Success() {
        ApPins pins = new ApPins();
        pins.setId(1L);
        pins.setAuthorId(1L);
        pins.setContent("测试沸点");

        Page<ApPins> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(pins));
        pageResult.setTotal(1);

        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        ResponseResult result = pinsService.list(1L, 1, 10, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1L, data.get("total"));
    }

    @Test
    @DisplayName("查询沸点列表 - 按状态筛选")
    void testList_WithStatus() {
        Page<ApPins> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(apPinsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        ResponseResult result = pinsService.list(1L, 1, 10, "published");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== statistics ====================

    @Test
    @DisplayName("沸点统计 - 用户未登录返回需要登录")
    void testStatistics_NotLoggedIn() {
        appThreadLocalUtilMocked.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.statistics(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("沸点统计 - 统计成功")
    void testStatistics_Success() {
        when(apPinsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 5L, 3L, 2L);

        ResponseResult result = pinsService.statistics(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(10L, data.get("total"));
        assertEquals(5L, data.get("published"));
        assertEquals(3L, data.get("reviewing"));
        assertEquals(2L, data.get("rejected"));
    }

    // ==================== createPins ====================

    @Test
    @DisplayName("创建沸点 - 用户未登录返回需要登录")
    void testCreatePins_NotLoggedIn() {
        appThreadLocalUtilMocked.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ApPins pins = new ApPins();
        pins.setContent("测试内容");

        ResponseResult result = pinsService.createPins(pins);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建沸点 - 内容为空返回参数错误")
    void testCreatePins_EmptyContent() {
        ApPins pins = new ApPins();
        pins.setContent("");

        ResponseResult result = pinsService.createPins(pins);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建沸点 - 内容为null返回参数错误")
    void testCreatePins_NullContent() {
        ApPins pins = new ApPins();

        ResponseResult result = pinsService.createPins(pins);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("创建沸点 - 创建成功")
    void testCreatePins_Success() {
        ApPins pins = new ApPins();
        pins.setContent("测试沸点内容");

        when(apPinsMapper.insert(any(ApPins.class))).thenAnswer(inv -> {
            ApPins p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        });
        when(notificationClient.createNotification(any(Map.class)))
                .thenReturn(ResponseResult.okResult());

        ResponseResult result = pinsService.createPins(pins);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        ApPins created = (ApPins) result.getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(1L, created.getAuthorId());
    }

    @Test
    @DisplayName("创建沸点 - 通知发送失败不影响沸点创建")
    void testCreatePins_NotificationFails() {
        ApPins pins = new ApPins();
        pins.setContent("测试沸点内容");

        when(apPinsMapper.insert(any(ApPins.class))).thenAnswer(inv -> {
            ApPins p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        });
        when(notificationClient.createNotification(any(Map.class)))
                .thenThrow(new RuntimeException("通知服务异常"));

        ResponseResult result = pinsService.createPins(pins);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== deletePins ====================

    @Test
    @DisplayName("删除沸点 - 用户未登录返回需要登录")
    void testDeletePins_NotLoggedIn() {
        appThreadLocalUtilMocked.when(AppThreadLocalUtil::getUser).thenReturn(null);

        ResponseResult result = pinsService.deletePins(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - id为null返回参数错误")
    void testDeletePins_NullId() {
        ResponseResult result = pinsService.deletePins(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - 沸点不存在返回数据不存在")
    void testDeletePins_NotFound() {
        when(apPinsMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = pinsService.deletePins(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - 非作者无权限删除")
    void testDeletePins_NotOwner() {
        ApPins pins = new ApPins();
        pins.setId(1L);
        pins.setAuthorId(999L);

        when(apPinsMapper.selectById(1L)).thenReturn(pins);

        ResponseResult result = pinsService.deletePins(1L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除沸点 - 删除成功（软删除）")
    void testDeletePins_Success() {
        ApPins pins = new ApPins();
        pins.setId(1L);
        pins.setAuthorId(1L);

        when(apPinsMapper.selectById(1L)).thenReturn(pins);
        when(apPinsMapper.updateById(any(ApPins.class))).thenReturn(1);

        ResponseResult result = pinsService.deletePins(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(pins.getIsDeleted());
        verify(apPinsMapper, times(1)).updateById(any(ApPins.class));
    }
}