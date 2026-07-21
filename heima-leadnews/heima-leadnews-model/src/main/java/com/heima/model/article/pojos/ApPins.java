package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_pins")
public class ApPins implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Integer userId;

    @TableField("user_name")
    private String userName;

    @TableField("user_avatar")
    private String userAvatar;

    @TableField("content")
    private String content;

    @TableField("circle_id")
    private Long circleId;

    @TableField("topic_id")
    private Long topicId;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("share_count")
    private Integer shareCount;

    @TableField("status")
    private Byte status;

    @TableField("reason")
    private String reason;

    @TableField("created_time")
    private Date createdTime;

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