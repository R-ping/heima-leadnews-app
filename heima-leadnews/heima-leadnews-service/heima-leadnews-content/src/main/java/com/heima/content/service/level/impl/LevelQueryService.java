package com.heima.content.service.level.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.content.mapper.level.ApLevelConfigMapper;
import com.heima.content.mapper.level.ApUserLevelMapper;
import com.heima.content.service.level.LevelPermissionService;
import com.heima.model.level.pojos.ApLevelConfig;
import com.heima.model.level.pojos.ApUserLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户等级查询服务 — 负责等级信息查询、等级配置查询、等级计算
 */
@Service
public class LevelQueryService {

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Autowired
    private ApLevelConfigMapper levelConfigMapper;

    @Autowired
    private LevelPermissionService permissionService;

    /**
     * 获取用户等级信息，不存在则创建默认记录
     */
    public ApUserLevel getUserLevel(Long userId) {
        LambdaQueryWrapper<ApUserLevel> query = new LambdaQueryWrapper<>();
        query.eq(ApUserLevel::getUserId, userId);
        ApUserLevel userLevel = userLevelMapper.selectOne(query);

        if (userLevel == null) {
            userLevel = new ApUserLevel();
            userLevel.setUserId(userId);
            userLevel.setDailyScore(0);
            userLevel.setDailyLevel(1);
            userLevel.setPowerValue(0);
            userLevel.setPowerLevel(1);
            userLevel.setDailyScoreToday(0);
            userLevel.setPowerValueToday(0);
            userLevelMapper.insert(userLevel);
        }

        return userLevel;
    }

    /**
     * 获取用户等级完整信息（含等级标题、描述、权限列表）
     */
    public Map<String, Object> getUserLevelInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        ApUserLevel userLevel = getUserLevel(userId);

        LambdaQueryWrapper<ApLevelConfig> dailyConfigQuery = new LambdaQueryWrapper<>();
        dailyConfigQuery.eq(ApLevelConfig::getLevelType, 1);
        dailyConfigQuery.eq(ApLevelConfig::getLevelValue, userLevel.getDailyLevel());
        ApLevelConfig dailyConfig = levelConfigMapper.selectOne(dailyConfigQuery);

        LambdaQueryWrapper<ApLevelConfig> powerConfigQuery = new LambdaQueryWrapper<>();
        powerConfigQuery.eq(ApLevelConfig::getLevelType, 2);
        powerConfigQuery.eq(ApLevelConfig::getLevelValue, userLevel.getPowerLevel());
        ApLevelConfig powerConfig = levelConfigMapper.selectOne(powerConfigQuery);

        result.put("dailyScore", userLevel.getDailyScore());
        result.put("dailyLevel", userLevel.getDailyLevel());
        result.put("dailyTitle", dailyConfig != null ? dailyConfig.getTitle() : "");
        result.put("dailyDescription", dailyConfig != null ? dailyConfig.getDescription() : "");

        result.put("powerValue", userLevel.getPowerValue());
        result.put("powerLevel", userLevel.getPowerLevel());
        result.put("powerTitle", powerConfig != null ? powerConfig.getTitle() : "");
        result.put("powerDescription", powerConfig != null ? powerConfig.getDescription() : "");

        result.put("permissions", permissionService.getUserPermissions(userId));

        return result;
    }

    /**
     * 获取等级配置列表
     */
    public List<ApLevelConfig> getLevelConfigs(Integer levelType) {
        LambdaQueryWrapper<ApLevelConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.orderByAsc(ApLevelConfig::getLevelValue);
        return levelConfigMapper.selectList(query);
    }

    /**
     * 根据积分计算等级
     */
    public int calculateLevel(int levelType, int score) {
        LambdaQueryWrapper<ApLevelConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.le(ApLevelConfig::getMinScore, score);
        query.orderByDesc(ApLevelConfig::getMinScore);
        query.last("LIMIT 1");
        ApLevelConfig config = levelConfigMapper.selectOne(query);

        if (config != null) {
            return config.getLevelValue();
        }

        // 无匹配范围时返回最高等级
        query.clear();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.orderByDesc(ApLevelConfig::getLevelValue);
        query.last("LIMIT 1");
        ApLevelConfig highest = levelConfigMapper.selectOne(query);
        return highest != null ? highest.getLevelValue() : 1;
    }
}