package com.heima.model.comment.dtos;

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
    /** 目标类型: 1-文章, 2-沸点 */
    private Integer articleType;
    /** 目标作者ID */
    private Integer targetUserId;
}