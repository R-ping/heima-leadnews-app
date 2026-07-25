package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_profile")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("career_start_date")
    private Date careerStartDate;

    @TableField("career_direction")
    private String careerDirection;

    @TableField("position")
    private String position;

    @TableField("company")
    private String company;

    @TableField("website")
    private String website;

    @TableField("bio")
    private String bio;

    @TableField("update_time")
    private Date updateTime;

    @TableField("privacy_message")
    private Integer privacyMessage;
}