package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.*;
import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class LevelServiceImpl implements LevelService {

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Autowired
    private ApLevelConfigMapper levelConfigMapper;

    @Autowired
    private ApUserActionLogMapper actionLogMapper;

    @Autowired
    private ApUserPowerLogMapper powerLogMapper;

    @Autowired
    private ApPermissionDefinitionMapper permissionDefinitionMapper;

    @Autowired
    private ApUserPermissionMapper userPermissionMapper;

    private static final int DAILY_SCORE_LIMIT = 100;

    private static final Map<String, Integer> ACTION_SCORE_MAP = new HashMap<>();
    static {
        ACTION_SCORE_MAP.put("daily_login", 10);
        ACTION_SCORE_MAP.put("article_read", 2);
        ACTION_SCORE_MAP.put("comment", 5);
        ACTION_SCORE_MAP.put("like", 1);
        ACTION_SCORE_MAP.put("share", 3);
        ACTION_SCORE_MAP.put("follow", 2);
        ACTION_SCORE_MAP.put("publish_article", 8);
        ACTION_SCORE_MAP.put("publish_pins", 8);
        ACTION_SCORE_MAP.put("daily_checkin", 2);
    }

    private static final Map<String, Integer> DAILY_ACTION_LIMIT = new HashMap<>();
    static {
        DAILY_ACTION_LIMIT.put("publish_article", 2);
        DAILY_ACTION_LIMIT.put("publish_pins", 2);
        DAILY_ACTION_LIMIT.put("comment", 5);
        DAILY_ACTION_LIMIT.put("like", 10);
        DAILY_ACTION_LIMIT.put("follow", 2);
        DAILY_ACTION_LIMIT.put("daily_checkin", 1);
    }

    private static final Map<String, Integer> POWER_ACTION_LIMIT = new HashMap<>();
    static {
        POWER_ACTION_LIMIT.put("publish_article", 2);
    }

    @Override
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

    @Override
    @Transactional
    public void recordAction(Long userId, String actionType, String actionDetail) {
        Integer score = ACTION_SCORE_MAP.getOrDefault(actionType, 0);
        if (score == 0) {
            return;
        }

        ApUserLevel userLevel = getUserLevel(userId);

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Integer dailyLimit = DAILY_ACTION_LIMIT.get(actionType);
        if (dailyLimit != null) {
            LambdaQueryWrapper<ApUserActionLog> limitQuery = new LambdaQueryWrapper<>();
            limitQuery.eq(ApUserActionLog::getUserId, userId);
            limitQuery.eq(ApUserActionLog::getActionType, actionType);
            limitQuery.apply("DATE(created_time) = '" + today + "'");
            long todayCount = actionLogMapper.selectCount(limitQuery);
            if (todayCount >= dailyLimit) {
                return;
            }
        }

        LambdaQueryWrapper<ApUserActionLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(ApUserActionLog::getUserId, userId);
        logQuery.apply("DATE(created_time) = '" + today + "'");
        Integer todayScore = actionLogMapper.selectList(logQuery).stream()
                .mapToInt(ApUserActionLog::getScoreChange).sum();

        int actualScore = Math.min(score, DAILY_SCORE_LIMIT - todayScore);
        if (actualScore <= 0) {
            return;
        }

        ApUserActionLog actionLog = new ApUserActionLog();
        actionLog.setUserId(userId);
        actionLog.setActionType(actionType);
        actionLog.setScoreChange(actualScore);
        actionLog.setActionDetail(actionDetail);
        actionLogMapper.insert(actionLog);

        userLevel.setDailyScore(userLevel.getDailyScore() + actualScore);
        userLevel.setDailyScoreToday(userLevel.getDailyScoreToday() + actualScore);

        int newDailyLevel = calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}执行行为{}，获得逐日分{}，当前逐日等级{}", userId, actionType, actualScore, userLevel.getDailyLevel());
    }

    @Override
    @Transactional
    public void calculatePower(Long userId, Long articleId, String changeType, Integer powerChange) {
        Map<String, Object> result = calculatePowerWithLimit(userId, articleId, changeType, powerChange);
    }

    @Override
    @Transactional
    public Map<String, Object> calculatePowerWithLimit(Long userId, Long articleId, String changeType, Integer powerChange) {
        Map<String, Object> result = new HashMap<>();

        int actualPower = calculateActualPower(userId, changeType, powerChange);
        if (actualPower <= 0) {
            result.put("success", false);
            result.put("message", "未获得逐力值");
            result.put("power", 0);
            result.put("powerValue", getUserLevel(userId).getPowerValue());
            result.put("powerLevel", getUserLevel(userId).getPowerLevel());
            return result;
        }

        ApUserLevel userLevel = getUserLevel(userId);

        ApUserPowerLog powerLog = new ApUserPowerLog();
        powerLog.setUserId(userId);
        powerLog.setPowerChange(actualPower);
        powerLog.setChangeType(changeType);
        powerLog.setSourceId(articleId);
        powerLog.setCalculatedAt(new java.sql.Date(System.currentTimeMillis()));
        powerLogMapper.insert(powerLog);

        userLevel.setPowerValue(userLevel.getPowerValue() + actualPower);
        userLevel.setPowerValueToday(userLevel.getPowerValueToday() + actualPower);

        int newPowerLevel = calculateLevel(2, userLevel.getPowerValue());
        int oldLevel = userLevel.getPowerLevel();
        boolean levelChanged = false;
        if (newPowerLevel != userLevel.getPowerLevel()) {
            userLevel.setPowerLevel(newPowerLevel);
            updateUserPermissions(userId, 2, oldLevel, newPowerLevel);
            levelChanged = true;
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}获得逐力值{}，当前逐力等级{}", userId, actualPower, userLevel.getPowerLevel());

        result.put("success", true);
        result.put("message", "逐力值计算成功");
        result.put("power", actualPower);
        result.put("powerValue", userLevel.getPowerValue());
        result.put("powerLevel", userLevel.getPowerLevel());
        result.put("levelChanged", levelChanged);
        result.put("oldLevel", oldLevel);
        result.put("newLevel", newPowerLevel);

        return result;
    }

    private int calculateActualPower(Long userId, String changeType, Integer powerChange) {
        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Integer dailyLimit = POWER_ACTION_LIMIT.get(changeType);
        if (dailyLimit != null) {
            LambdaQueryWrapper<ApUserPowerLog> limitQuery = new LambdaQueryWrapper<>();
            limitQuery.eq(ApUserPowerLog::getUserId, userId);
            limitQuery.eq(ApUserPowerLog::getChangeType, changeType);
            limitQuery.apply("DATE(calculated_at) = '" + today + "'");
            long todayCount = powerLogMapper.selectCount(limitQuery);
            if (todayCount >= dailyLimit) {
                return 0;
            }
        }

        int basePower = 0;
        switch (changeType) {
            case "publish_article":
                basePower = 10;
                break;
            case "get_like":
                basePower = 1;
                break;
            case "get_comment":
                basePower = 1;
                break;
            case "get_favorite":
                basePower = 1;
                break;
            case "get_read":
                basePower = powerChange / 100;
                break;
            default:
                basePower = powerChange;
        }

        return basePower;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        LambdaQueryWrapper<ApUserPermission> query = new LambdaQueryWrapper<>();
        query.eq(ApUserPermission::getUserId, userId);
        query.eq(ApUserPermission::getPermissionCode, permissionCode);
        query.isNull(ApUserPermission::getExpiredAt);
        return userPermissionMapper.selectCount(query) > 0;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        LambdaQueryWrapper<ApUserPermission> query = new LambdaQueryWrapper<>();
        query.eq(ApUserPermission::getUserId, userId);
        query.isNull(ApUserPermission::getExpiredAt);
        List<ApUserPermission> permissions = userPermissionMapper.selectList(query);
        List<String> permissionCodes = new ArrayList<>();
        for (ApUserPermission p : permissions) {
            permissionCodes.add(p.getPermissionCode());
        }
        return permissionCodes;
    }

    @Override
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

        result.put("permissions", getUserPermissions(userId));

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> checkIn(Long userId) {
        Map<String, Object> result = new HashMap<>();

        String today = new java.sql.Date(System.currentTimeMillis()).toString();
        LambdaQueryWrapper<ApUserActionLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(ApUserActionLog::getUserId, userId);
        logQuery.eq(ApUserActionLog::getActionType, "daily_checkin");
        logQuery.apply("DATE(created_time) = '" + today + "'");
        long todayCheckinCount = actionLogMapper.selectCount(logQuery);

        if (todayCheckinCount > 0) {
            result.put("success", false);
            result.put("hasCheckedIn", true);
            result.put("score", 0);
            return result;
        }

        ApUserLevel userLevel = getUserLevel(userId);

        Integer dailyLimit = DAILY_ACTION_LIMIT.get("daily_checkin");
        if (dailyLimit != null && todayCheckinCount >= dailyLimit) {
            result.put("success", false);
            result.put("hasCheckedIn", true);
            result.put("score", 0);
            return result;
        }

        LambdaQueryWrapper<ApUserActionLog> scoreQuery = new LambdaQueryWrapper<>();
        scoreQuery.eq(ApUserActionLog::getUserId, userId);
        scoreQuery.apply("DATE(created_time) = '" + today + "'");
        Integer todayScore = actionLogMapper.selectList(scoreQuery).stream()
                .mapToInt(ApUserActionLog::getScoreChange).sum();

        Integer score = ACTION_SCORE_MAP.getOrDefault("daily_checkin", 0);
        int actualScore = Math.min(score, DAILY_SCORE_LIMIT - todayScore);
        if (actualScore <= 0) {
            result.put("success", false);
            result.put("hasCheckedIn", false);
            result.put("score", 0);
            return result;
        }

        ApUserActionLog actionLog = new ApUserActionLog();
        actionLog.setUserId(userId);
        actionLog.setActionType("daily_checkin");
        actionLog.setScoreChange(actualScore);
        actionLog.setActionDetail("每日签到");
        actionLogMapper.insert(actionLog);

        userLevel.setDailyScore(userLevel.getDailyScore() + actualScore);
        userLevel.setDailyScoreToday(userLevel.getDailyScoreToday() + actualScore);

        int newDailyLevel = calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}签到成功，获得逐日分{}，当前逐日等级{}", userId, actualScore, userLevel.getDailyLevel());

        result.put("success", true);
        result.put("hasCheckedIn", true);
        result.put("score", actualScore);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> recordActionWithLimit(Long userId, String actionType, String actionDetail) {
        Map<String, Object> result = new HashMap<>();

        Integer score = ACTION_SCORE_MAP.getOrDefault(actionType, 0);
        if (score == 0) {
            result.put("success", false);
            result.put("message", "无效的行为类型");
            result.put("score", 0);
            return result;
        }

        ApUserLevel userLevel = getUserLevel(userId);

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Integer dailyLimit = DAILY_ACTION_LIMIT.get(actionType);
        if (dailyLimit != null) {
            LambdaQueryWrapper<ApUserActionLog> limitQuery = new LambdaQueryWrapper<>();
            limitQuery.eq(ApUserActionLog::getUserId, userId);
            limitQuery.eq(ApUserActionLog::getActionType, actionType);
            limitQuery.apply("DATE(created_time) = '" + today + "'");
            long todayCount = actionLogMapper.selectCount(limitQuery);
            if (todayCount >= dailyLimit) {
                result.put("success", false);
                result.put("message", "今日该行为已达上限");
                result.put("score", 0);
                return result;
            }
        }

        LambdaQueryWrapper<ApUserActionLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(ApUserActionLog::getUserId, userId);
        logQuery.apply("DATE(created_time) = '" + today + "'");
        Integer todayScore = actionLogMapper.selectList(logQuery).stream()
                .mapToInt(ApUserActionLog::getScoreChange).sum();

        int actualScore = Math.min(score, DAILY_SCORE_LIMIT - todayScore);
        if (actualScore <= 0) {
            result.put("success", false);
            result.put("message", "今日积分已达上限");
            result.put("score", 0);
            return result;
        }

        ApUserActionLog actionLog = new ApUserActionLog();
        actionLog.setUserId(userId);
        actionLog.setActionType(actionType);
        actionLog.setScoreChange(actualScore);
        actionLog.setActionDetail(actionDetail);
        actionLogMapper.insert(actionLog);

        userLevel.setDailyScore(userLevel.getDailyScore() + actualScore);
        userLevel.setDailyScoreToday(userLevel.getDailyScoreToday() + actualScore);

        int newDailyLevel = calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}执行行为{}，获得逐日分{}，当前逐日等级{}", userId, actionType, actualScore, userLevel.getDailyLevel());

        result.put("success", true);
        result.put("message", "行为记录成功");
        result.put("score", actualScore);
        return result;
    }

    @Override
    public Map<String, Object> getTodayTaskProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tasks = new ArrayList<>();

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Map<String, String> taskNameMap = new HashMap<>();
        taskNameMap.put("daily_checkin", "每日签到");
        taskNameMap.put("publish_article", "发布文章");
        taskNameMap.put("publish_pins", "发布沸点");
        taskNameMap.put("comment", "发表评论");
        taskNameMap.put("like", "点赞");
        taskNameMap.put("follow", "关注用户");

        Map<String, String> taskIconMap = new HashMap<>();
        taskIconMap.put("daily_checkin", "check-circle");
        taskIconMap.put("publish_article", "edit");
        taskIconMap.put("publish_pins", "message-circle");
        taskIconMap.put("comment", "message-square");
        taskIconMap.put("like", "heart");
        taskIconMap.put("follow", "user-plus");

        String[] actionTypes = {"daily_checkin", "publish_article", "publish_pins", "comment", "like", "follow"};

        for (String actionType : actionTypes) {
            Map<String, Object> task = new HashMap<>();
            Integer max = DAILY_ACTION_LIMIT.get(actionType);
            Integer score = ACTION_SCORE_MAP.get(actionType);

            LambdaQueryWrapper<ApUserActionLog> query = new LambdaQueryWrapper<>();
            query.eq(ApUserActionLog::getUserId, userId);
            query.eq(ApUserActionLog::getActionType, actionType);
            query.apply("DATE(created_time) = '" + today + "'");
            long current = actionLogMapper.selectCount(query);

            task.put("actionType", actionType);
            task.put("name", taskNameMap.get(actionType));
            task.put("icon", taskIconMap.get(actionType));
            task.put("current", current);
            task.put("max", max);
            task.put("score", score);
            task.put("completed", current >= max);

            tasks.add(task);
        }

        result.put("tasks", tasks);
        return result;
    }

    private int calculateLevel(int levelType, int score) {
        LambdaQueryWrapper<ApLevelConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.le(ApLevelConfig::getMinScore, score);
        query.ge(ApLevelConfig::getMaxScore, score);
        ApLevelConfig config = levelConfigMapper.selectOne(query);

        if (config != null) {
            return config.getLevelValue();
        }

        query.clear();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.orderByDesc(ApLevelConfig::getLevelValue);
        List<ApLevelConfig> configs = levelConfigMapper.selectList(query);
        if (!configs.isEmpty()) {
            return configs.get(0).getLevelValue();
        }

        return 1;
    }

    @Transactional
    public void updateUserPermissions(Long userId, int levelType, int oldLevel, int newLevel) {
        LambdaQueryWrapper<ApPermissionDefinition> query = new LambdaQueryWrapper<>();
        query.eq(ApPermissionDefinition::getRelatedLevelType, levelType);
        query.eq(ApPermissionDefinition::getIsActive, 1);
        List<ApPermissionDefinition> permissions = permissionDefinitionMapper.selectList(query);

        for (ApPermissionDefinition permission : permissions) {
            int requiredLevel = permission.getRequiredLevel();
            String permissionCode = permission.getPermissionCode();

            if (newLevel >= requiredLevel && oldLevel < requiredLevel) {
                grantPermission(userId, permissionCode);
            } else if (newLevel < requiredLevel && oldLevel >= requiredLevel) {
                revokePermission(userId, permissionCode);
            }
        }
    }

    private void grantPermission(Long userId, String permissionCode) {
        LambdaQueryWrapper<ApUserPermission> query = new LambdaQueryWrapper<>();
        query.eq(ApUserPermission::getUserId, userId);
        query.eq(ApUserPermission::getPermissionCode, permissionCode);
        ApUserPermission existing = userPermissionMapper.selectOne(query);

        if (existing == null) {
            ApUserPermission userPermission = new ApUserPermission();
            userPermission.setUserId(userId);
            userPermission.setPermissionCode(permissionCode);
            userPermission.setGrantedAt(new Date());
            userPermissionMapper.insert(userPermission);
            log.info("用户{}获得权限{}", userId, permissionCode);
        } else if (existing.getExpiredAt() != null) {
            existing.setExpiredAt(null);
            userPermissionMapper.updateById(existing);
        }
    }

    @Override
    public List<ApLevelConfig> getLevelConfigs(Integer levelType) {
        LambdaQueryWrapper<ApLevelConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApLevelConfig::getLevelType, levelType);
        query.orderByAsc(ApLevelConfig::getLevelValue);
        return levelConfigMapper.selectList(query);
    }

    private void revokePermission(Long userId, String permissionCode) {
        LambdaQueryWrapper<ApUserPermission> query = new LambdaQueryWrapper<>();
        query.eq(ApUserPermission::getUserId, userId);
        query.eq(ApUserPermission::getPermissionCode, permissionCode);
        ApUserPermission existing = userPermissionMapper.selectOne(query);

        if (existing != null && existing.getExpiredAt() == null) {
            existing.setExpiredAt(new Date());
            userPermissionMapper.updateById(existing);
            log.info("用户{}失去权限{}", userId, permissionCode);
        }
    }
}
