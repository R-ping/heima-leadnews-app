package com.heima.content.service.level.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.content.mapper.level.ApLevelConfigMapper;
import com.heima.content.mapper.level.ApUserLevelMapper;
import com.heima.content.mapper.user.ApUserDiamondLogMapper;
import com.heima.model.level.pojos.ApLevelConfig;
import com.heima.model.level.pojos.ApUserLevel;
import com.heima.model.user.pojos.ApUserDiamondLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 钻石奖励服务 — 负责等级升级时发放钻石奖励
 */
@Slf4j
@Service
public class LevelDiamondService {

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Autowired
    private ApLevelConfigMapper levelConfigMapper;

    @Autowired
    private ApUserDiamondLogMapper diamondLogMapper;

    @Autowired
    private LevelQueryService levelQueryService;

    /**
     * 等级升级时发放钻石奖励
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantDiamondOnLevelUp(Long userId, int levelType, int newLevel) {
        try {
            LambdaQueryWrapper<ApLevelConfig> configQuery = new LambdaQueryWrapper<>();
            configQuery.eq(ApLevelConfig::getLevelType, levelType);
            configQuery.eq(ApLevelConfig::getLevelValue, newLevel);
            ApLevelConfig config = levelConfigMapper.selectOne(configQuery);

            if (config == null || config.getDiamondReward() == null || config.getDiamondReward() <= 0) {
                log.info("等级{}无钻石奖励配置, levelType={}, newLevel={}", newLevel, levelType, newLevel);
                return;
            }

            int diamondAmount = config.getDiamondReward();

            ApUserLevel userLevel = levelQueryService.getUserLevel(userId);
            int currentBalance = userLevel.getDiamondBalance() != null ? userLevel.getDiamondBalance() : 0;
            int newBalance = currentBalance + diamondAmount;

            userLevel.setDiamondBalance(newBalance);
            userLevelMapper.updateById(userLevel);

            ApUserDiamondLog diamondLog = new ApUserDiamondLog();
            diamondLog.setUserId(userId);
            diamondLog.setChangeType("level_up");
            diamondLog.setChangeAmount(diamondAmount);
            diamondLog.setBalance(newBalance);
            diamondLog.setSourceId(String.valueOf(newLevel));
            diamondLog.setCreatedAt(java.time.LocalDateTime.now());
            diamondLogMapper.insert(diamondLog);

            log.info("等级升级钻石奖励发放, userId={}, levelType={}, newLevel={}, diamondAmount={}, newBalance={}",
                userId, levelType, newLevel, diamondAmount, newBalance);
        } catch (Exception e) {
            log.error("钻石奖励发放异常, userId={}, levelType={}, newLevel={}", userId, levelType, newLevel, e);
        }
    }
}