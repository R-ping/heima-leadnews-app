package com.heima.model.pins.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论请求 DTO
 */
@Data
@NoArgsConstructor
public class PinsCommentDTO {

    private Long pinsId;

    private String content = "";

    private Long parentId;
}