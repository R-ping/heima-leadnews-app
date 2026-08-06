package com.heima.content.service.level;

import com.heima.model.level.pojos.ApLevelConfig;
import com.heima.model.level.pojos.ApUserLevel;

import java.util.List;
import java.util.Map;

public interface LevelService {

    ApUserLevel getUserLevel(Long userId);

    void recordAction(Long userId, String actionType, String actionDetail);

    void calculatePower(Long userId, Long articleId, String changeType, Integer powerChange);

    Map<String, Object> calculatePowerWithLimit(Long userId, Long articleId, String changeType, Integer powerChange);

    boolean hasPermission(Long userId, String permissionCode);

    List<String> getUserPermissions(Long userId);

    Map<String, Object> getUserLevelInfo(Long userId);

    Map<String, Object> getUserLevelData(Long userId);

    Map<String, Object> checkIn(Long userId);

    Map<String, Object> recordActionWithLimit(Long userId, String actionType, String actionDetail);

    Map<String, Object> getTodayTaskProgress(Long userId);

    List<ApLevelConfig> getLevelConfigs(Integer levelType);

    Map<String, Object> getLevelPrivileges(Long userId);

    Map<String, Object> getUserInfoPack(Long userId);
}