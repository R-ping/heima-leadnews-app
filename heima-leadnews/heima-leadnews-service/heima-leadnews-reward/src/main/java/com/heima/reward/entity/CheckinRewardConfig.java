package com.heima.reward.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("checkin_reward_config")
public class CheckinRewardConfig {
    @TableId
    private Integer periodDay;
    private Integer baseOre;
    private Boolean isSpecial;
    private Integer specialOre;
}
