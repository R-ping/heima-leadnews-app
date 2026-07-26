package com.heima.article.service;

import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("沸点服务测试")
public class PinsServiceImplTest {

    @Autowired
    private PinsService pinsService;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        ApUser user = new ApUser();
        user.setId(TEST_USER_ID.intValue());
        user.setNickname("测试用户");
        AppThreadLocalUtil.setUser(user);
    }

    @AfterEach
    void tearDown() {
        AppThreadLocalUtil.clear();
    }

    // ==================== list ====================

    @Test
    @Order(1)
    @DisplayName("沸点列表 - 正常返回分页数据")
    void testList_Success() {
        ResponseResult result = pinsService.list(TEST_USER_ID, 1, 10, null);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(2)
    @DisplayName("沸点列表 - 按已发布状态筛选")
    void testList_FilterByPublished() {
        ResponseResult result = pinsService.list(TEST_USER_ID, 1, 10, "published");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("沸点列表 - 按审核中状态筛选")
    void testList_FilterByReviewing() {
        ResponseResult result = pinsService.list(TEST_USER_ID, 1, 10, "reviewing");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== statistics ====================

    @Test
    @Order(4)
    @DisplayName("沸点统计 - 正常返回统计数据")
    void testStatistics_Success() {
        ResponseResult result = pinsService.statistics(TEST_USER_ID);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== createPins ====================

    @Test
    @Order(5)
    @DisplayName("创建沸点 - 正常创建")
    void testCreatePins_Success() {
        ApPins pins = new ApPins();
        pins.setContent("测试沸点内容" + System.currentTimeMillis());

        ResponseResult result = pinsService.createPins(pins);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof ApPins);
    }

    @Test
    @Order(6)
    @DisplayName("创建沸点 - 内容为空返回错误")
    void testCreatePins_EmptyContent() {
        ApPins pins = new ApPins();
        pins.setContent("");

        ResponseResult result = pinsService.createPins(pins);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(7)
    @DisplayName("创建沸点 - 内容为null返回错误")
    void testCreatePins_NullContent() {
        ApPins pins = new ApPins();
        pins.setContent(null);

        ResponseResult result = pinsService.createPins(pins);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== deletePins ====================

    @Test
    @Order(8)
    @DisplayName("删除沸点 - 正常软删除")
    void testDeletePins_Success() {
        // 先创建一个沸点
        ApPins pins = new ApPins();
        pins.setContent("待删除沸点" + System.currentTimeMillis());
        ResponseResult createResult = pinsService.createPins(pins);
        assertEquals(200, createResult.getCode());
        Long pinsId = ((ApPins) createResult.getData()).getId();

        ResponseResult result = pinsService.deletePins(pinsId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(9)
    @DisplayName("删除沸点 - id为空返回错误")
    void testDeletePins_NullId() {
        ResponseResult result = pinsService.deletePins(null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(10)
    @DisplayName("删除沸点 - 沸点不存在返回错误")
    void testDeletePins_NotFound() {
        ResponseResult result = pinsService.deletePins(99999L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(11)
    @DisplayName("沸点列表 - 未登录返回错误")
    void testList_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = pinsService.list(TEST_USER_ID, 1, 10, null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }
}