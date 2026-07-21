package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_article_draft")
public class ApArticleDraft implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    private String title;

    @TableField("author_id")
    private Long authorId;

    @TableField("channel_id")
    private Integer channelId;

    @TableField("channel_name")
    private String channelName;

    private Short layout;

    private String images;

    private String labels;

    private String topic;

    private String content;

    private String summary;

    @TableField("publish_time")
    private Date publishTime;

    @TableField("created_time")
    private Date createdTime;

    @TableField("updated_time")
    private Date updatedTime;

    private Byte status;
}