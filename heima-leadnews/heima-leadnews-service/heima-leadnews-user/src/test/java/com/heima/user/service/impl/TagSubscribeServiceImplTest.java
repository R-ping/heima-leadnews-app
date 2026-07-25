package com.heima.user.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.SysTag;
import com.heima.model.user.pojos.UserTagRelation;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.SysTagMapper;
import com.heima.user.mapper.UserTagRelationMapper;
import com.heima.user.service.TagSubscribeService;
import com.heima.utils.thread.AppThreadLocalUtil;
import com.aliyun.oss.OSS;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("标签订阅服务测试")
public class TagSubscribeServiceImplTest {

    @MockBean
    private OSS ossClient;

    @Autowired
    private TagSubscribeService tagSubscribeService;

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private SysTagMapper sysTagMapper;

    @Autowired
    private UserTagRelationMapper userTagRelationMapper;

    private ApUser testUser;

    private static int testIdCounter = 900000;

    @BeforeEach
    void setUp() {
        testUser = new ApUser();
        testUser.setId(testIdCounter++);
        testUser.setPhone("13900000000");
        testUser.setNickname("标签测试用户");
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
            Long userId = testUser.getId().longValue();
            // 清理测试用户的标签关注记录
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserTagRelation> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(UserTagRelation::getUserId, userId);
            userTagRelationMapper.delete(wrapper);
            apUserMapper.deleteById(testUser.getId());
        }
    }

    // ==================== discover ====================

    @Test
    @Order(1)
    @DisplayName("发现标签 - 按热度排序返回标签列表")
    void testDiscover_Hottest() {
        ResponseResult result = tagSubscribeService.discover("hottest", null, 1, 20);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Map);
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("total"));
        assertNotNull(data.get("list"));
    }

    @Test
    @Order(2)
    @DisplayName("发现标签 - 按最新排序返回标签列表")
    void testDiscover_Latest() {
        ResponseResult result = tagSubscribeService.discover("latest", null, 1, 20);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("发现标签 - 关键词搜索")
    void testDiscover_Keyword() {
        ResponseResult result = tagSubscribeService.discover("hottest", "Java", 1, 20);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(4)
    @DisplayName("发现标签 - 未登录用户可访问(isFollowing均为false)")
    void testDiscover_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = tagSubscribeService.discover("hottest", null, 1, 20);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        List<?> list = (List<?>) data.get("list");
        for (Object item : list) {
            try {
                Boolean isFollowing = (Boolean) item.getClass().getMethod("getIsFollowing").invoke(item);
                assertFalse(isFollowing);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ==================== follow ====================

    @Test
    @Order(5)
    @DisplayName("关注标签 - 正常关注")
    void testFollow_Success() {
        // 找一个存在的标签
        List<SysTag> tags = sysTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysTag>()
        );
        if (tags.isEmpty()) {
            return; // 没有标签，跳过测试
        }
        Integer tagId = tags.get(0).getId();

        ResponseResult result = tagSubscribeService.follow(tagId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("关注标签 - 重复关注返回成功（幂等）")
    void testFollow_Duplicate() {
        List<SysTag> tags = sysTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysTag>()
        );
        if (tags.isEmpty()) {
            return;
        }
        Integer tagId = tags.get(0).getId();

        tagSubscribeService.follow(tagId);
        ResponseResult result = tagSubscribeService.follow(tagId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(7)
    @DisplayName("关注标签 - 标签不存在返回错误")
    void testFollow_NotFound() {
        ResponseResult result = tagSubscribeService.follow(99999);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
        assertEquals(503, result.getCode());
    }

    @Test
    @Order(8)
    @DisplayName("关注标签 - 用户未登录返回错误")
    void testFollow_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = tagSubscribeService.follow(1);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== getFollowed ====================

    @Test
    @Order(9)
    @DisplayName("获取已关注标签 - 正常返回列表")
    void testGetFollowed_Success() {
        // 先关注一个标签
        List<SysTag> tags = sysTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysTag>()
        );
        if (!tags.isEmpty()) {
            tagSubscribeService.follow(tags.get(0).getId());
        }

        ResponseResult result = tagSubscribeService.getFollowed();
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof List);
    }

    @Test
    @Order(10)
    @DisplayName("获取已关注标签 - 用户未登录返回错误")
    void testGetFollowed_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = tagSubscribeService.getFollowed();
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== unfollow ====================

    @Test
    @Order(11)
    @DisplayName("取消关注标签 - 正常取消关注")
    void testUnfollow_Success() {
        List<SysTag> tags = sysTagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysTag>()
        );
        if (tags.isEmpty()) {
            return;
        }
        Integer tagId = tags.get(0).getId();

        // 先关注
        tagSubscribeService.follow(tagId);
        // 再取消关注
        ResponseResult result = tagSubscribeService.unfollow(tagId);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(12)
    @DisplayName("取消关注标签 - 未关注的标签取消也返回成功（幂等）")
    void testUnfollow_Idempotent() {
        ResponseResult result = tagSubscribeService.unfollow(99999);
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("取消关注标签 - 用户未登录返回错误")
    void testUnfollow_NotLogin() {
        AppThreadLocalUtil.clear();

        ResponseResult result = tagSubscribeService.unfollow(1);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }
}