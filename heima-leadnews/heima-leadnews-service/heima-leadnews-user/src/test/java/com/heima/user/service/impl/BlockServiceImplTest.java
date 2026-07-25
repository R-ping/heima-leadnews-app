package com.heima.user.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.BlockDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.UserBlockRelation;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.UserBlockRelationMapper;
import com.heima.user.service.BlockService;
import com.heima.utils.thread.AppThreadLocalUtil;
import com.aliyun.oss.OSS;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("屏蔽管理服务测试")
public class BlockServiceImplTest {

    @MockBean
    private OSS ossClient;

    @Autowired
    private BlockService blockService;

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private UserBlockRelationMapper userBlockRelationMapper;

    private ApUser testUser;

    private static int testIdCounter = 900000;

    @BeforeEach
    void setUp() {
        testUser = new ApUser();
        testUser.setId(testIdCounter++);
        testUser.setPhone("13800000000");
        testUser.setNickname("屏蔽测试用户");
        testUser.setStatus(true);
        testUser.setFlag((short) 0);
        testUser.setCreatedTime(new Date());
        apUserMapper.insert(testUser);

        AppThreadLocalUtil.setUser(testUser);
    }

    @AfterEach
    void tearDown() {
        AppThreadLocalUtil.clear();
        if (testUser != null && testUser.getId() != null) {
            // 清理测试用户的屏蔽记录
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBlockRelation> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(UserBlockRelation::getUserId, testUser.getId().longValue());
            userBlockRelationMapper.delete(wrapper);
            apUserMapper.deleteById(testUser.getId());
        }
    }

    // ==================== addBlock ====================

    @Test
    @Order(1)
    @DisplayName("添加屏蔽 - 正常添加作者屏蔽")
    void testAddBlock_Author_Success() {
        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(100L);

        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(2)
    @DisplayName("添加屏蔽 - 正常添加标签屏蔽")
    void testAddBlock_Tag_Success() {
        BlockDTO dto = new BlockDTO();
        dto.setType(2);
        dto.setTargetId(200L);

        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("添加屏蔽 - 重复屏蔽返回错误")
    void testAddBlock_Duplicate() {
        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(100L);

        // 第一次添加
        blockService.addBlock(dto);
        // 第二次添加应返回错误
        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(4)
    @DisplayName("添加屏蔽 - 无效类型返回错误")
    void testAddBlock_InvalidType() {
        BlockDTO dto = new BlockDTO();
        dto.setType(3);
        dto.setTargetId(100L);

        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(5)
    @DisplayName("添加屏蔽 - targetId为空返回错误")
    void testAddBlock_NullTargetId() {
        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(null);

        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("添加屏蔽 - 用户未登录返回错误")
    void testAddBlock_NotLogin() {
        AppThreadLocalUtil.clear();

        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(100L);

        ResponseResult result = blockService.addBlock(dto);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== getBlocks ====================

    @Test
    @Order(7)
    @DisplayName("获取屏蔽列表 - 正常返回作者屏蔽列表")
    void testGetBlocks_Author_Success() {
        // 先添加一条屏蔽记录
        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(100L);
        blockService.addBlock(dto);

        ResponseResult result = blockService.getBlocks(1, 1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Map);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("total"));
        assertNotNull(data.get("list"));
    }

    @Test
    @Order(8)
    @DisplayName("获取屏蔽列表 - 正常返回标签屏蔽列表")
    void testGetBlocks_Tag_Success() {
        // 先添加一条标签屏蔽记录
        BlockDTO dto = new BlockDTO();
        dto.setType(2);
        dto.setTargetId(200L);
        blockService.addBlock(dto);

        ResponseResult result = blockService.getBlocks(2, 1, 10);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(9)
    @DisplayName("获取屏蔽列表 - 无效类型返回错误")
    void testGetBlocks_InvalidType() {
        ResponseResult result = blockService.getBlocks(3, 1, 10);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(10)
    @DisplayName("获取屏蔽列表 - 用户未登录返回错误")
    void testGetBlocks_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = blockService.getBlocks(1, 1, 10);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== removeBlock ====================

    @Test
    @Order(11)
    @DisplayName("删除屏蔽 - 正常删除屏蔽记录")
    void testRemoveBlock_Success() {
        // 先添加一条屏蔽记录
        BlockDTO dto = new BlockDTO();
        dto.setType(1);
        dto.setTargetId(300L);
        blockService.addBlock(dto);

        // 获取屏蔽列表，找到ID
        ResponseResult listResult = blockService.getBlocks(1, 1, 10);
        Map<String, Object> data = (Map<String, Object>) listResult.getData();
        java.util.List<?> list = (java.util.List<?>) data.get("list");
        assertFalse(list.isEmpty());
        // 使用反射或直接读取 BlockVO 的 id 字段
        Object firstItem = list.get(0);
        Long blockId;
        if (firstItem instanceof Map) {
            blockId = ((Number) ((Map<?, ?>) firstItem).get("id")).longValue();
        } else {
            // 通过反射获取 id 字段
            try {
                blockId = (Long) firstItem.getClass().getMethod("getId").invoke(firstItem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ResponseResult result = blockService.removeBlock(blockId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(12)
    @DisplayName("删除屏蔽 - 不存在的记录返回错误")
    void testRemoveBlock_NotFound() {
        ResponseResult result = blockService.removeBlock(99999L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("删除屏蔽 - 用户未登录返回错误")
    void testRemoveBlock_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = blockService.removeBlock(1L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }
}