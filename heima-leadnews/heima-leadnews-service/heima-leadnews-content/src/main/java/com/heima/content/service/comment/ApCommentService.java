package com.heima.content.service.comment;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCommentService {
    ResponseResult getCommentList(CommentDto dto);
    ResponseResult addComment(CommentDto dto);
    ResponseResult likeComment(CommentDto dto);
    ResponseResult getCommentById(Long id);
}