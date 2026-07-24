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
@TableName("sign_in_config")
public class SignInConfig implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("day_of_month")
    private Integer dayOfMonth;
    @TableField("base_reward")
    private Integer baseReward;
    @TableField("bonus_multiplier")
    private BigDecimal bonusMultiplier;
    @TableField("extra_label")
    private String extraLabel;
    @TableField("is_active")
    private Integer isActive;
    @TableField("created_time")
    private Date createdTime;
}