package com.heima.task.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("lottery_prize_pool")
public class LotteryPrizePool {
    @TableId
    private String id;
    private String name;
    private Integer type;
    private String iconUrl;
    private BigDecimal probability;
    private Integer minOre;
    private Integer maxOre;
    private String virtualItemCode;
    private Integer unlockRequiredDraws;
    private Boolean isPhysical;
    private Integer sortOrder;
    private Integer status;
}