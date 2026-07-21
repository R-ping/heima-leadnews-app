package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("ap_course")
public class ApCourse implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("subtitle")
    private String subtitle;

    @TableField("description")
    private String description;

    @TableField("cover_image")
    private String coverImage;

    @TableField("author_id")
    private Integer authorId;

    @TableField("author_name")
    private String authorName;

    @TableField("author_avatar")
    private String authorAvatar;

    @TableField("price")
    private BigDecimal price;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("status")
    private Byte status;

    @TableField("reason")
    private String reason;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("chapter_count")
    private Integer chapterCount;

    @TableField("study_count")
    private Integer studyCount;

    @TableField("estimated_hours")
    private BigDecimal estimatedHours;

    @TableField("published_at")
    private Date publishedAt;

    @TableField("created_time")
    private Date createdTime;

    @TableField("updated_time")
    private Date updatedTime;

    public enum Status {
        NORMAL((byte) 0),
        SUBMIT((byte) 1),
        FAIL((byte) 2),
        PUBLISHED((byte) 9);

        byte code;

        Status(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return this.code;
        }
    }
}