package com.heima.content.service.level.impl;

import com.heima.content.service.level.LevelPermissionService;
import com.heima.content.service.level.LevelService;
import com.heima.model.level.pojos.ApLevelConfig;
import com.heima.model.level.pojos.ApUserLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 等级服务 Facade — 委派各子服务处理具体职责
 *
 * 职责拆分：
 * - LevelQueryService: 用户等级查询、等级配置查询、等级计算
 * - LevelActionService: 行为记录、逐日分计算、签到
 * - LevelPowerService: 逐力值计算
 * - LevelPermissionService: 权限管理
 * - LevelDiamondService: 钻石奖励
 * - LevelTaskProgressBuilder: 任务进度构建（已独立）
 */
@Slf4j
@Service
public class LevelServiceImpl implements LevelService {

    @Autowired
    private LevelQueryService levelQueryService;

    @Autowired
    private LevelActionService levelActionService;

    @Autowired
    private LevelPowerService levelPowerService;

    @Autowired
    private LevelPermissionService permissionService;

    // ==================== 等级查询 ====================

    @Override
    public ApUserLevel getUserLevel(Long userId) {
        return levelQueryService.getUserLevel(userId);
    }

    @Override
    public Map<String, Object> getUserLevelInfo(Long userId) {
        return levelQueryService.getUserLevelInfo(userId);
    }

    @Override
    public List<ApLevelConfig> getLevelConfigs(Integer levelType) {
        return levelQueryService.getLevelConfigs(levelType);
    }

    // ==================== 行为记录 ====================

    @Override
    public void recordAction(Long userId, String actionType, String actionDetail) {
        levelActionService.recordAction(userId, actionType, actionDetail);
    }

    @Override
    public Map<String, Object> recordActionWithLimit(Long userId, String actionType, String actionDetail) {
        return levelActionService.recordActionWithLimit(userId, actionType, actionDetail);
    }

    @Override
    public Map<String, Object> checkIn(Long userId) {
        return levelActionService.checkIn(userId);
    }

    @Override
    public Map<String, Object> getTodayTaskProgress(Long userId) {
        return levelActionService.getTodayTaskProgress(userId);
    }

    // ==================== 逐力值 ====================

    @Override
    public void calculatePower(Long userId, Long articleId, String changeType, Integer powerChange) {
        levelPowerService.calculatePower(userId, articleId, changeType, powerChange);
    }

    @Override
    public Map<String, Object> calculatePowerWithLimit(Long userId, Long articleId, String changeType,
        Integer powerChange) {
        return levelPowerService.calculatePowerWithLimit(userId, articleId, changeType, powerChange);
    }

    // ==================== 权限 ====================

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        return permissionService.hasPermission(userId, permissionCode);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return permissionService.getUserPermissions(userId);
    }
}