package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("ap_pins")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApPins implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("author_id")
    private Long authorId;

    @TableField("author_name")
    private String authorName;

    @TableField("author_image")
    private String authorImage;

    private String content;

    @TableField("image_urls")
    private String imageUrls;

    @TableField("topic_tags")
    private String topicTags;

    @TableField("like_count")
    private Integer likes = 0;

    @TableField("comment_count")
    private Integer comment = 0;

    @TableField("share_count")
    private Integer share = 0;

    private Byte status;

    private String reason;

    @TableField("is_deleted")
    private Boolean isDeleted = false;

    @TableField("created_time")
    private Date createdTime;

    @TableField("publish_time")
    private Date publishTime;

    public enum Status {
        DRAFT((byte) 0),
        SUBMIT((byte) 1),
        FAIL((byte) 2),
        PUBLISHED((byte) 9);

        byte code;
        Status(byte code) { this.code = code; }
        public byte getCode() { return code; }
    }
}
