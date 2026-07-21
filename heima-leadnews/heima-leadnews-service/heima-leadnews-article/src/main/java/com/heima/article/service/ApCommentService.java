package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.article.dtos.CommentDto;

public interface ApCommentService {
    ResponseResult getCommentList(CommentDto dto);
    ResponseResult addComment(CommentDto dto);
    ResponseResult likeComment(CommentDto dto);
}