package com.heima.article.service;

import com.heima.model.article.pojos.ApColumn;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires running services")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("专栏服务测试")
public class ColumnServiceImplTest {

    @Autowired
    private ColumnService columnService;

    private static final Long TEST_USER_ID = 1L;

    private Long createdColumnId;

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
    @DisplayName("专栏列表 - 正常返回分页数据")
    void testList_Success() {
        ResponseResult result = columnService.list(TEST_USER_ID, 1, 10, null, null);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(2)
    @DisplayName("专栏列表 - 按已发布状态筛选")
    void testList_FilterByPublished() {
        ResponseResult result = columnService.list(TEST_USER_ID, 1, 10, "published", null);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("专栏列表 - 按标题搜索")
    void testList_SearchByTitle() {
        ResponseResult result = columnService.list(TEST_USER_ID, 1, 10, null, "测试");
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== statistics ====================

    @Test
    @Order(4)
    @DisplayName("专栏统计 - 正常返回统计数据")
    void testStatistics_Success() {
        ResponseResult result = columnService.statistics(TEST_USER_ID);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== createColumn ====================

    @Test
    @Order(5)
    @DisplayName("创建专栏 - 正常创建")
    void testCreateColumn_Success() {
        ApColumn column = new ApColumn();
        column.setTitle("测试专栏" + System.currentTimeMillis());
        column.setDescription("测试专栏描述");

        ResponseResult result = columnService.createColumn(column);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof ApColumn);
        createdColumnId = ((ApColumn) result.getData()).getId();
    }

    @Test
    @Order(6)
    @DisplayName("创建专栏 - 名称为空返回错误")
    void testCreateColumn_EmptyTitle() {
        ApColumn column = new ApColumn();
        column.setTitle("");
        column.setDescription("描述");

        ResponseResult result = columnService.createColumn(column);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(7)
    @DisplayName("创建专栏 - 简介为空返回错误")
    void testCreateColumn_EmptyDescription() {
        ApColumn column = new ApColumn();
        column.setTitle("测试标题");
        column.setDescription("");

        ResponseResult result = columnService.createColumn(column);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updateColumn ====================

    @Test
    @Order(8)
    @DisplayName("更新专栏 - 正常更新")
    void testUpdateColumn_Success() {
        // 先创建一个专栏
        ApColumn column = new ApColumn();
        column.setTitle("待更新专栏" + System.currentTimeMillis());
        column.setDescription("原始描述");
        ResponseResult createResult = columnService.createColumn(column);
        assertEquals(200, createResult.getCode());
        Long colId = ((ApColumn) createResult.getData()).getId();

        // 更新
        ApColumn update = new ApColumn();
        update.setId(colId);
        update.setTitle("已更新专栏");
        update.setDescription("更新后的描述");

        ResponseResult result = columnService.updateColumn(update);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(9)
    @DisplayName("更新专栏 - id为空返回错误")
    void testUpdateColumn_NullId() {
        ApColumn column = new ApColumn();
        column.setTitle("测试");

        ResponseResult result = columnService.updateColumn(column);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(10)
    @DisplayName("更新专栏 - 专栏不存在返回错误")
    void testUpdateColumn_NotFound() {
        ApColumn column = new ApColumn();
        column.setId(99999L);
        column.setTitle("测试");

        ResponseResult result = columnService.updateColumn(column);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== deleteColumn ====================

    @Test
    @Order(11)
    @DisplayName("删除专栏 - 正常软删除")
    void testDeleteColumn_Success() {
        // 先创建一个专栏
        ApColumn column = new ApColumn();
        column.setTitle("待删除专栏" + System.currentTimeMillis());
        column.setDescription("待删除");
        ResponseResult createResult = columnService.createColumn(column);
        assertEquals(200, createResult.getCode());
        Long colId = ((ApColumn) createResult.getData()).getId();

        ResponseResult result = columnService.deleteColumn(colId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(12)
    @DisplayName("删除专栏 - id为空返回错误")
    void testDeleteColumn_NullId() {
        ResponseResult result = columnService.deleteColumn(null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("删除专栏 - 专栏不存在返回错误")
    void testDeleteColumn_NotFound() {
        ResponseResult result = columnService.deleteColumn(99999L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }
}