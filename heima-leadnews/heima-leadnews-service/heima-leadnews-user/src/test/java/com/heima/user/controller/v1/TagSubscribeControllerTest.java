package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.TagSubscribeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagSubscribeController 单元测试")
class TagSubscribeControllerTest {

    @Mock
    private TagSubscribeService tagSubscribeService;

    @InjectMocks
    private TagSubscribeController tagSubscribeController;

    // ==================== discover ====================

    @Nested
    @DisplayName("discover 方法测试")
    class DiscoverTests {

        @Test
        @DisplayName("正常发现标签 - 使用默认参数")
        void shouldDiscoverWithDefaults() {
            ResponseResult expected = ResponseResult.okResult("tag-list");
            when(tagSubscribeService.discover("hottest", null, 1, 20)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", null, 1, 20);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("hottest", null, 1, 20);
        }

        @Test
        @DisplayName("发现标签 - 按热度排序")
        void shouldDiscoverByHottest() {
            ResponseResult expected = ResponseResult.okResult("hot-tags");
            when(tagSubscribeService.discover("hottest", null, 1, 20)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", null, 1, 20);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("hottest", null, 1, 20);
        }

        @Test
        @DisplayName("发现标签 - 按最新排序")
        void shouldDiscoverByLatest() {
            ResponseResult expected = ResponseResult.okResult("latest-tags");
            when(tagSubscribeService.discover("latest", null, 1, 20)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("latest", null, 1, 20);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("latest", null, 1, 20);
        }

        @Test
        @DisplayName("发现标签 - 带关键词搜索")
        void shouldDiscoverWithKeyword() {
            ResponseResult expected = ResponseResult.okResult("search-results");
            when(tagSubscribeService.discover("hottest", "java", 1, 20)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", "java", 1, 20);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("hottest", "java", 1, 20);
        }

        @Test
        @DisplayName("发现标签 - 关键词为空字符串")
        void shouldDiscoverWithEmptyKeyword() {
            ResponseResult expected = ResponseResult.okResult("all-tags");
            when(tagSubscribeService.discover("hottest", "", 1, 20)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", "", 1, 20);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("hottest", "", 1, 20);
        }

        @Test
        @DisplayName("发现标签 - 自定义分页")
        void shouldDiscoverWithCustomPagination() {
            ResponseResult expected = ResponseResult.okResult("paged-tags");
            when(tagSubscribeService.discover("hottest", null, 3, 50)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", null, 3, 50);

            assertSame(expected, result);
            verify(tagSubscribeService).discover("hottest", null, 3, 50);
        }

        @Test
        @DisplayName("发现标签 - 服务返回错误")
        void shouldReturnErrorWhenDiscoverFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "发现标签失败");
            when(tagSubscribeService.discover(anyString(), isNull(), anyInt(), anyInt())).thenReturn(expected);

            ResponseResult result = tagSubscribeController.discover("hottest", null, 1, 20);

            assertEquals(500, result.getCode());
            assertEquals("发现标签失败", result.getMessage());
        }
    }

    // ==================== getFollowed ====================

    @Nested
    @DisplayName("getFollowed 方法测试")
    class GetFollowedTests {

        @Test
        @DisplayName("正常获取已关注标签")
        void shouldReturnFollowedTagsSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("followed-tags");
            when(tagSubscribeService.getFollowed()).thenReturn(expected);

            ResponseResult result = tagSubscribeController.getFollowed();

            assertSame(expected, result);
            verify(tagSubscribeService).getFollowed();
        }

        @Test
        @DisplayName("获取已关注标签 - 列表为空")
        void shouldReturnEmptyFollowedList() {
            ResponseResult expected = ResponseResult.okResult(null);
            when(tagSubscribeService.getFollowed()).thenReturn(expected);

            ResponseResult result = tagSubscribeController.getFollowed();

            assertSame(expected, result);
            verify(tagSubscribeService).getFollowed();
        }

        @Test
        @DisplayName("获取已关注标签 - 服务返回错误")
        void shouldReturnErrorWhenGetFollowedFails() {
            ResponseResult expected = ResponseResult.errorResult(500, "获取关注标签失败");
            when(tagSubscribeService.getFollowed()).thenReturn(expected);

            ResponseResult result = tagSubscribeController.getFollowed();

            assertEquals(500, result.getCode());
            assertEquals("获取关注标签失败", result.getMessage());
        }
    }

    // ==================== follow ====================

    @Nested
    @DisplayName("follow 方法测试")
    class FollowTests {

        @Test
        @DisplayName("正常关注标签")
        void shouldFollowTagSuccessfully() {
            ResponseResult expected = ResponseResult.okResult();
            when(tagSubscribeService.follow(100)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.follow(100);

            assertSame(expected, result);
            verify(tagSubscribeService).follow(100);
        }

        @Test
        @DisplayName("关注标签 - tagId不存在")
        void shouldHandleNonExistentTagId() {
            ResponseResult expected = ResponseResult.errorResult(503, "标签不存在");
            when(tagSubscribeService.follow(9999)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.follow(9999);

            assertEquals(503, result.getCode());
            verify(tagSubscribeService).follow(9999);
        }

        @Test
        @DisplayName("关注标签 - tagId为null")
        void shouldHandleNullTagId() {
            ResponseResult expected = ResponseResult.errorResult(400, "tagId不能为空");
            when(tagSubscribeService.follow(null)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.follow(null);

            assertEquals(400, result.getCode());
            verify(tagSubscribeService).follow(null);
        }

        @Test
        @DisplayName("关注标签 - 重复关注")
        void shouldHandleDuplicateFollow() {
            ResponseResult expected = ResponseResult.errorResult(503, "已关注该标签");
            when(tagSubscribeService.follow(100)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.follow(100);

            assertEquals(503, result.getCode());
            verify(tagSubscribeService).follow(100);
        }

        @Test
        @DisplayName("关注标签 - tagId为负数")
        void shouldHandleNegativeTagId() {
            ResponseResult expected = ResponseResult.errorResult(400, "tagId无效");
            when(tagSubscribeService.follow(-1)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.follow(-1);

            assertEquals(400, result.getCode());
            verify(tagSubscribeService).follow(-1);
        }
    }

    // ==================== unfollow ====================

    @Nested
    @DisplayName("unfollow 方法测试")
    class UnfollowTests {

        @Test
        @DisplayName("正常取消关注标签")
        void shouldUnfollowTagSuccessfully() {
            ResponseResult expected = ResponseResult.okResult();
            when(tagSubscribeService.unfollow(100)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.unfollow(100);

            assertSame(expected, result);
            verify(tagSubscribeService).unfollow(100);
        }

        @Test
        @DisplayName("取消关注标签 - tagId不存在")
        void shouldHandleNonExistentTagId() {
            ResponseResult expected = ResponseResult.errorResult(503, "标签不存在或未关注");
            when(tagSubscribeService.unfollow(9999)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.unfollow(9999);

            assertEquals(503, result.getCode());
            verify(tagSubscribeService).unfollow(9999);
        }

        @Test
        @DisplayName("取消关注标签 - tagId为null")
        void shouldHandleNullTagId() {
            ResponseResult expected = ResponseResult.errorResult(400, "tagId不能为空");
            when(tagSubscribeService.unfollow(null)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.unfollow(null);

            assertEquals(400, result.getCode());
            verify(tagSubscribeService).unfollow(null);
        }

        @Test
        @DisplayName("取消关注标签 - 未关注该标签")
        void shouldHandleNotFollowed() {
            ResponseResult expected = ResponseResult.errorResult(503, "未关注该标签");
            when(tagSubscribeService.unfollow(200)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.unfollow(200);

            assertEquals(503, result.getCode());
            verify(tagSubscribeService).unfollow(200);
        }

        @Test
        @DisplayName("取消关注标签 - tagId为负数")
        void shouldHandleNegativeTagId() {
            ResponseResult expected = ResponseResult.errorResult(400, "tagId无效");
            when(tagSubscribeService.unfollow(-1)).thenReturn(expected);

            ResponseResult result = tagSubscribeController.unfollow(-1);

            assertEquals(400, result.getCode());
            verify(tagSubscribeService).unfollow(-1);
        }
    }
}