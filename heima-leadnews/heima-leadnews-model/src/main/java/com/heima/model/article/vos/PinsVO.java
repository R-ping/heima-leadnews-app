package com.heima.model.article.vos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 沸点列表项 VO
 */
@Data
@NoArgsConstructor
public class PinsVO {

    private Long id;
    private Long userId;
    private String userName = "";
    private String userAvatar = "";
    private Long authorId;
    private String authorName = "";
    private String authorImage = "";
    private String content = "";
    private List<String> imageUrls = new ArrayList<>();
    private List<String> topicTags = new ArrayList<>();
    private String linkUrl = "";
    private String linkTitle = "";
    private Integer likeCount = 0;
    private Integer commentCount = 0;
    private Integer shareCount = 0;
    private Boolean liked = false;
    private Date createdTime;
    private Date publishTime;
    private Date reviewTime;
}