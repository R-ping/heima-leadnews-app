package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCommentLikeMapper;
import com.heima.article.mapper.ApCommentMapper;
import com.heima.model.article.dtos.CommentDto;
import com.heima.model.article.pojos.ApComment;
import com.heima.model.article.pojos.ApCommentLike;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("评论服务测试")
class ApCommentServiceImplTest {

    @Mock
    private ApCommentMapper apCommentMapper;

    @Mock
    private ApCommentLikeMapper apCommentLikeMapper;

    @InjectMocks
    private ApCommentServiceImpl apCommentService;

    private MockedStatic<AppThreadLocalUtil> appThreadLocalUtilMockedStatic;
    private ApUser testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apCommentService, "baseMapper", apCommentMapper);
        testUser = new ApUser();
        testUser.setId(1);
        testUser.setNickname("测试用户");
        appThreadLocalUtilMockedStatic = mockStatic(AppThreadLocalUtil.class);
        appThreadLocalUtilMockedStatic.when(AppThreadLocalUtil::getUser).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        appThreadLocalUtilMockedStatic.close();
    }

    // ==================== getCommentList ====================

    @Test
    @DisplayName("获取评论列表 - dto为null返回参数错误")
    void testGetCommentList_NullDto() {
        ResponseResult result = apCommentService.getCommentList(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取评论列表 - articleId为null返回参数错误")
    void testGetCommentList_NullArticleId() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(null);

        ResponseResult result = apCommentService.getCommentList(dto);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取评论列表 - 无评论返回空列表")
    void testGetCommentList_Empty() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        when(apCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        ResponseResult result = apCommentService.getCommentList(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        List<?> data = (List<?>) result.getData();
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("获取评论列表 - 正常返回一级评论带二级回复")
    void testGetCommentList_Success() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        dto.setPage(1);
        dto.setSize(10);

        ApComment comment = new ApComment();
        comment.setId(1L);
        comment.setArticleId(1001L);
        comment.setUserId(2);
        comment.setUserName("用户");
        comment.setContent("评论内容");
        List<ApComment> topComments = Collections.singletonList(comment);
        when(apCommentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(topComments);

        when(apCommentLikeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        ResponseResult result = apCommentService.getCommentList(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== addComment ====================

    @Test
    @DisplayName("添加评论 - dto为null返回参数错误")
    void testAddComment_NullDto() {
        ResponseResult result = apCommentService.addComment(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("添加评论 - 内容为空返回参数错误")
    void testAddComment_EmptyContent() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        dto.setContent("");

        ResponseResult result = apCommentService.addComment(dto);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("添加评论 - 内容超过1000字返回参数错误")
    void testAddComment_ContentTooLong() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            sb.append('a');
        }
        dto.setContent(sb.toString());

        ResponseResult result = apCommentService.addComment(dto);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("添加评论 - 添加成功")
    void testAddComment_Success() {
        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        dto.setContent("这是一条评论");
        when(apCommentMapper.insert(any(ApComment.class))).thenReturn(1);

        ResponseResult result = apCommentService.addComment(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("添加评论 - 二级回复更新父评论回复数")
    void testAddComment_Reply() {
        ApComment parent = new ApComment();
        parent.setId(1L);
        parent.setReplyCount(0);
        when(apCommentMapper.selectById(1L)).thenReturn(parent);

        CommentDto dto = new CommentDto();
        dto.setArticleId(1001L);
        dto.setParentId(1L);
        dto.setContent("这是一条回复");
        when(apCommentMapper.insert(any(ApComment.class))).thenReturn(1);
        when(apCommentMapper.updateById(any(ApComment.class))).thenReturn(1);

        ResponseResult result = apCommentService.addComment(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, parent.getReplyCount());
    }

    // ==================== likeComment ====================

    @Test
    @DisplayName("点赞评论 - dto为null返回参数错误")
    void testLikeComment_NullDto() {
        ResponseResult result = apCommentService.likeComment(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("点赞评论 - commentId为null返回参数错误")
    void testLikeComment_NullCommentId() {
        CommentDto dto = new CommentDto();
        dto.setCommentId(null);

        ResponseResult result = apCommentService.likeComment(dto);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("点赞评论 - 评论不存在返回数据不存在")
    void testLikeComment_NotFound() {
        CommentDto dto = new CommentDto();
        dto.setCommentId(999L);
        when(apCommentMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = apCommentService.likeComment(dto);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("点赞评论 - 点赞成功")
    void testLikeComment_LikeSuccess() {
        CommentDto dto = new CommentDto();
        dto.setCommentId(1L);
        ApComment comment = new ApComment();
        comment.setId(1L);
        comment.setLikeCount(0);
        when(apCommentMapper.selectById(1L)).thenReturn(comment);
        when(apCommentLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apCommentLikeMapper.insert(any(ApCommentLike.class))).thenReturn(1);
        when(apCommentMapper.updateById(any(ApComment.class))).thenReturn(1);

        ResponseResult result = apCommentService.likeComment(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(1, comment.getLikeCount());
    }

    @Test
    @DisplayName("点赞评论 - 取消点赞成功")
    void testLikeComment_UnlikeSuccess() {
        CommentDto dto = new CommentDto();
        dto.setCommentId(1L);
        ApComment comment = new ApComment();
        comment.setId(1L);
        comment.setLikeCount(1);
        ApCommentLike existingLike = new ApCommentLike();
        existingLike.setId(1L);
        when(apCommentMapper.selectById(1L)).thenReturn(comment);
        when(apCommentLikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);
        when(apCommentLikeMapper.deleteById(1L)).thenReturn(1);
        when(apCommentMapper.updateById(any(ApComment.class))).thenReturn(1);

        ResponseResult result = apCommentService.likeComment(dto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(0, comment.getLikeCount());
    }
}