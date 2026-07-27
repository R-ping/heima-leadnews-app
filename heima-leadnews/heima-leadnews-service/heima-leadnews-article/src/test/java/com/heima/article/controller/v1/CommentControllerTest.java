package com.heima.article.controller.v1;

import com.heima.article.service.ApCommentService;
import com.heima.model.article.dtos.CommentDto;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController 单元测试")
class CommentControllerTest {

    @Mock
    private ApCommentService apCommentService;

    @InjectMocks
    private CommentController commentController;

    @Nested
    @DisplayName("getCommentList() - 获取评论列表")
    class GetCommentListTests {

        @Test
        @DisplayName("正常获取评论列表，返回成功结果")
        void shouldReturnCommentList() {
            CommentDto dto = new CommentDto();
            dto.setArticleId(100L);
            dto.setPage(1);
            dto.setSize(10);
            ResponseResult expected = ResponseResult.okResult("comment_list");
            when(apCommentService.getCommentList(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.getCommentList(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apCommentService).getCommentList(dto);
        }

        @Test
        @DisplayName("DTO为空时，委托给service处理")
        void shouldDelegateToServiceWhenDtoIsEmpty() {
            CommentDto dto = new CommentDto();
            ResponseResult expected = ResponseResult.errorResult(501, "无效参数");
            when(apCommentService.getCommentList(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.getCommentList(dto);

            assertNotNull(result);
            assertEquals(501, result.getCode());
        }

        @Test
        @DisplayName("分页获取评论列表")
        void shouldGetPagedCommentList() {
            CommentDto dto = new CommentDto();
            dto.setArticleId(100L);
            dto.setPage(2);
            dto.setSize(5);
            ResponseResult expected = ResponseResult.okResult("paged_comments");
            when(apCommentService.getCommentList(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.getCommentList(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCommentService).getCommentList(dto);
        }
    }

    @Nested
    @DisplayName("addComment() - 添加评论")
    class AddCommentTests {

        @Test
        @DisplayName("正常添加评论，返回成功结果")
        void shouldAddCommentSuccessfully() {
            CommentDto dto = new CommentDto();
            dto.setArticleId(100L);
            dto.setContent("这是一条测试评论");
            ResponseResult expected = ResponseResult.okResult("comment_added");
            when(apCommentService.addComment(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.addComment(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apCommentService).addComment(dto);
        }

        @Test
        @DisplayName("添加子评论（回复评论）")
        void shouldAddReplyComment() {
            CommentDto dto = new CommentDto();
            dto.setArticleId(100L);
            dto.setParentId(10L);
            dto.setContent("回复评论内容");
            dto.setReplyToUserId(2001L);
            dto.setReplyToUserName("原评论用户");
            ResponseResult expected = ResponseResult.okResult("reply_added");
            when(apCommentService.addComment(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.addComment(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCommentService).addComment(dto);
        }

        @Test
        @DisplayName("添加评论失败时，透传错误")
        void shouldPropagateErrorWhenAddFails() {
            CommentDto dto = new CommentDto();
            dto.setArticleId(100L);
            ResponseResult errorResult = ResponseResult.errorResult(501, "请输入评论内容");
            when(apCommentService.addComment(any(CommentDto.class))).thenReturn(errorResult);

            ResponseResult result = commentController.addComment(dto);

            assertNotNull(result);
            assertEquals(501, result.getCode());
            assertEquals("请输入评论内容", result.getMessage());
        }
    }

    @Nested
    @DisplayName("likeComment() - 点赞/取消点赞评论")
    class LikeCommentTests {

        @Test
        @DisplayName("正常点赞评论，返回成功结果")
        void shouldLikeCommentSuccessfully() {
            CommentDto dto = new CommentDto();
            dto.setCommentId(100L);
            ResponseResult expected = ResponseResult.okResult("liked");
            when(apCommentService.likeComment(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.likeComment(dto);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apCommentService).likeComment(dto);
        }

        @Test
        @DisplayName("取消点赞评论，返回成功结果")
        void shouldUnlikeCommentSuccessfully() {
            CommentDto dto = new CommentDto();
            dto.setCommentId(100L);
            ResponseResult expected = ResponseResult.okResult("unliked");
            when(apCommentService.likeComment(any(CommentDto.class))).thenReturn(expected);

            ResponseResult result = commentController.likeComment(dto);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCommentService).likeComment(dto);
        }

        @Test
        @DisplayName("点赞不存在的评论，透传错误")
        void shouldPropagateErrorWhenCommentNotFound() {
            CommentDto dto = new CommentDto();
            dto.setCommentId(999L);
            ResponseResult errorResult = ResponseResult.errorResult(1002, "评论不存在");
            when(apCommentService.likeComment(any(CommentDto.class))).thenReturn(errorResult);

            ResponseResult result = commentController.likeComment(dto);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
            assertEquals("评论不存在", result.getMessage());
        }
    }
}