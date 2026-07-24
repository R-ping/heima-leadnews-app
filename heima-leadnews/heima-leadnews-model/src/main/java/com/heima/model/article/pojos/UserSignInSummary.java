package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_sign_in_summary")
public class UserSignInSummary implements Serializable {
    @TableId("user_id")
    private Long userId;
    @TableField("current_consecutive_days")
    private Integer currentConsecutiveDays;
    @TableField("max_consecutive_days")
    private Integer maxConsecutiveDays;
    @TableField("total_signed_days")
    private Integer totalSignedDays;
    @TableField("retroactive_card_count")
    private Integer retroactiveCardCount;
    @TableField("last_sign_date")
    private Date lastSignDate;
    @TableField("total_ore")
    private Long totalOre;
    @TableField("update_time")
    private Date updateTime;
}