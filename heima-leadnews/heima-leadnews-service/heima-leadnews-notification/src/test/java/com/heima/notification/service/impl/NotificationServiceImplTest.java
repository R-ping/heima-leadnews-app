package com.heima.notification.service.impl;

import com.heima.apis.article.ICommentClient;
import com.heima.apis.article.IFollowClient;
import com.heima.model.article.dtos.CommentDto;
import com.heima.model.article.pojos.ApComment;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.notification.mapper.NotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl 单元测试")
class NotificationServiceImplTest {

    @Mock
    private ICommentClient commentClient;

    @Mock
    private IFollowClient followClient;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() throws Exception {
        // commentClient 和 followClient 使用 @Autowired(required = false)，
        // @InjectMocks 无法注入 required=false 的字段，需通过反射手动设置
        java.lang.reflect.Field commentField = NotificationServiceImpl.class.getDeclaredField("commentClient");
        commentField.setAccessible(true);
        commentField.set(notificationService, commentClient);

        java.lang.reflect.Field followField = NotificationServiceImpl.class.getDeclaredField("followClient");
        followField.setAccessible(true);
        followField.set(notificationService, followClient);
    }

    // ==================== reply ====================

    @Nested
    @DisplayName("reply 方法测试")
    class ReplyTests {

        @Test
        @DisplayName("userId 为 null - 返回 PARAM_INVALID")
        void testReply_NullUserId() {
            ResponseResult result = notificationService.reply(null, 1L, "content");
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("commentId 为 null - 返回 PARAM_INVALID")
        void testReply_NullCommentId() {
            ResponseResult result = notificationService.reply(1L, null, "content");
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("content 为 null - 返回 PARAM_INVALID")
        void testReply_NullContent() {
            ResponseResult result = notificationService.reply(1L, 1L, null);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("content 为空字符串 - 返回 PARAM_INVALID")
        void testReply_EmptyContent() {
            ResponseResult result = notificationService.reply(1L, 1L, "");
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("content 仅含空白字符 - 返回 PARAM_INVALID")
        void testReply_BlankContent() {
            ResponseResult result = notificationService.reply(1L, 1L, "   ");
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("评论不存在 - getCommentById 返回 null")
        void testReply_CommentNotFound_NullResult() {
            when(commentClient.getCommentById(1L)).thenReturn(null);

            ResponseResult result = notificationService.reply(1L, 1L, "回复内容");

            assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
            verify(commentClient).getCommentById(1L);
            verify(commentClient, never()).addComment(any());
        }

        @Test
        @DisplayName("评论不存在 - getCommentById 返回错误码")
        void testReply_CommentNotFound_ErrorCode() {
            ResponseResult errorResult = ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            when(commentClient.getCommentById(1L)).thenReturn(errorResult);

            ResponseResult result = notificationService.reply(1L, 1L, "回复内容");

            assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
            verify(commentClient).getCommentById(1L);
            verify(commentClient, never()).addComment(any());
        }

        @Test
        @DisplayName("评论数据异常 - comment data 为 null")
        void testReply_CommentDataNull() {
            // getCommentById 返回成功但 data 为 null，convertValue 返回 null
            ResponseResult successResult = ResponseResult.okResult(null);
            when(commentClient.getCommentById(1L)).thenReturn(successResult);

            ResponseResult result = notificationService.reply(1L, 1L, "回复内容");

            assertEquals(AppHttpCodeEnum.DATA_NOT_EXIST.getCode(), result.getCode());
            verify(commentClient).getCommentById(1L);
            verify(commentClient, never()).addComment(any());
        }

        @Test
        @DisplayName("正常回复 - 调用 addComment 成功")
        void testReply_Success() {
            ApComment comment = new ApComment();
            comment.setId(1L);
            comment.setArticleId(100L);
            comment.setUserId(10);
            comment.setUserName("测试用户");

            ResponseResult commentResult = ResponseResult.okResult(comment);
            when(commentClient.getCommentById(1L)).thenReturn(commentResult);

            ResponseResult addResult = ResponseResult.okResult(200, "回复成功");
            when(commentClient.addComment(any(CommentDto.class))).thenReturn(addResult);

            ResponseResult result = notificationService.reply(1L, 1L, "回复内容");

            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
            verify(commentClient).getCommentById(1L);
            verify(commentClient).addComment(any(CommentDto.class));
        }

        @Test
        @DisplayName("异常处理 - 回复时抛出异常返回 SERVER_ERROR")
        void testReply_Exception() {
            when(commentClient.getCommentById(1L)).thenThrow(new RuntimeException("网络异常"));

            ResponseResult result = notificationService.reply(1L, 1L, "回复内容");

            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
            verify(commentClient).getCommentById(1L);
        }
    }

    // ==================== toggleLike ====================

    @Nested
    @DisplayName("toggleLike 方法测试")
    class ToggleLikeTests {

        @Test
        @DisplayName("userId 为 null - 返回 PARAM_INVALID")
        void testToggleLike_NullUserId() {
            ResponseResult result = notificationService.toggleLike(null, 1L);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("commentId 为 null - 返回 PARAM_INVALID")
        void testToggleLike_NullCommentId() {
            ResponseResult result = notificationService.toggleLike(1L, null);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(commentClient);
        }

        @Test
        @DisplayName("正常点赞 - 调用 likeComment 成功")
        void testToggleLike_Success() {
            ResponseResult likeResult = ResponseResult.okResult(200, "点赞成功");
            when(commentClient.likeComment(any(CommentDto.class))).thenReturn(likeResult);

            ResponseResult result = notificationService.toggleLike(1L, 100L);

            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
            verify(commentClient).likeComment(any(CommentDto.class));
        }

        @Test
        @DisplayName("异常处理 - 点赞时抛出异常返回 SERVER_ERROR")
        void testToggleLike_Exception() {
            when(commentClient.likeComment(any(CommentDto.class))).thenThrow(new RuntimeException("网络异常"));

            ResponseResult result = notificationService.toggleLike(1L, 100L);

            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
            verify(commentClient).likeComment(any(CommentDto.class));
        }
    }

    // ==================== followBack ====================

    @Nested
    @DisplayName("followBack 方法测试")
    class FollowBackTests {

        @Test
        @DisplayName("userId 为 null - 返回 PARAM_INVALID")
        void testFollowBack_NullUserId() {
            ResponseResult result = notificationService.followBack(null, 1L);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(followClient);
        }

        @Test
        @DisplayName("followerId 为 null - 返回 PARAM_INVALID")
        void testFollowBack_NullFollowerId() {
            ResponseResult result = notificationService.followBack(1L, null);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(followClient);
        }

        @Test
        @DisplayName("自己关注自己 - 返回 PARAM_INVALID")
        void testFollowBack_SelfFollow() {
            ResponseResult result = notificationService.followBack(1L, 1L);
            assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
            verifyNoInteractions(followClient);
        }

        @Test
        @DisplayName("正常回关 - 调用 follow 成功")
        void testFollowBack_Success() {
            ResponseResult followResult = ResponseResult.okResult(200, "关注成功");
            when(followClient.follow(eq(1L), eq(2L))).thenReturn(followResult);

            ResponseResult result = notificationService.followBack(1L, 2L);

            assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), result.getCode());
            verify(followClient).follow(1L, 2L);
        }

        @Test
        @DisplayName("异常处理 - 关注时抛出异常返回 SERVER_ERROR")
        void testFollowBack_Exception() {
            when(followClient.follow(eq(1L), eq(2L))).thenThrow(new RuntimeException("网络异常"));

            ResponseResult result = notificationService.followBack(1L, 2L);

            assertEquals(AppHttpCodeEnum.SERVER_ERROR.getCode(), result.getCode());
            verify(followClient).follow(1L, 2L);
        }
    }
}