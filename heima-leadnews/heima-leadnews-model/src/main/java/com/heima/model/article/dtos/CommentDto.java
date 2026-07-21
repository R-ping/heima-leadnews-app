package com.heima.model.article.dtos;

import lombok.Data;

@Data
public class CommentDto {
    private Long articleId;
    private Long parentId;
    private Long commentId;
    private Long replyToUserId;
    private String replyToUserName;
    private String content;
    private Integer page;
    private Integer size;
}