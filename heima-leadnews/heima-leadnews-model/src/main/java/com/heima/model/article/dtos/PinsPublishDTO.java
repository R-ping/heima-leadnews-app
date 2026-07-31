package com.heima.model.article.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发布沸点请求 DTO
 */
@Data
@NoArgsConstructor
public class PinsPublishDTO {

    private String content = "";

    private String imageUrls = "";

    private String topicTags = "";

    private Long topicId;

    private Long circleId;

    private String linkUrl = "";

    private String linkTitle = "";
}