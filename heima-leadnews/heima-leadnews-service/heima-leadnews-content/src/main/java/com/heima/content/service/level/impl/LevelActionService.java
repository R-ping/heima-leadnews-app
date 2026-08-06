package com.heima.content.service.level.impl;

import static com.heima.content.constants.LevelScoreConstants.ACTION_SCORE_MAP;
import static com.heima.content.constants.LevelScoreConstants.DAILY_ACTION_LIMIT;
import static com.heima.content.constants.LevelScoreConstants.DAILY_SCORE_LIMIT;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.content.mapper.level.ApBehaviorConfigMapper;
import com.heima.content.mapper.level.ApUserDailyProgressMapper;
import com.heima.content.mapper.level.ApUserLevelMapper;
import com.heima.content.mapper.pins.ApUserActionLogMapper;
import com.heima.content.service.level.LevelPermissionService;
import com.heima.model.level.pojos.ApBehaviorConfig;
import com.heima.model.level.pojos.ApUserDailyProgress;
import com.heima.model.level.pojos.ApUserLevel;
import com.heima.model.user.pojos.ApUserActionLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 行为记录与积分服务 — 负责用户行为记录、逐日分计算、签到
 */
@Slf4j
@Service
public class LevelActionService {

    @Autowired
    private ApUserActionLogMapper actionLogMapper;

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Autowired
    private LevelQueryService levelQueryService;

    @Autowired
    private LevelPermissionService permissionService;

    @Autowired
    private LevelDiamondService diamondService;

    @Autowired
    private LevelTaskProgressBuilder taskProgressBuilder;

    @Autowired
    private ApBehaviorConfigMapper behaviorConfigMapper;

    @Autowired
    private ApUserDailyProgressMapper userDailyProgressMapper;

    /**
     * 记录行为（默认行为，无限制校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordAction(Long userId, String actionType, String actionDetail) {
        Integer score = ACTION_SCORE_MAP.getOrDefault(actionType, 0);
        if (score == 0) {
            return;
        }

        ApUserLevel userLevel = levelQueryService.getUserLevel(userId);

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Integer dailyLimit = DAILY_ACTION_LIMIT.get(actionType);
        if (dailyLimit != null) {
            if (getTodayActionCount(userId, actionType, today) >= dailyLimit) {
                return;
            }
        }

        int todayScore = getTodayScore(userId, today);

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

        upsertDailyProgress(userId, actionType);

        userLevel.setDailyScore(userLevel.getDailyScore() + actualScore);
        userLevel.setDailyScoreToday(userLevel.getDailyScoreToday() + actualScore);

        int newDailyLevel = levelQueryService.calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            permissionService.updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
            diamondService.grantDiamondOnLevelUp(userId, 1, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}执行行为{}，获得逐日分{}，当前逐日等级{}", userId, actionType, actualScore,
            userLevel.getDailyLevel());
    }

    /**
     * 记录行为（含限制校验，返回结果）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> recordActionWithLimit(Long userId, String actionType, String actionDetail) {
        Map<String, Object> result = new HashMap<>();

        Integer score = ACTION_SCORE_MAP.getOrDefault(actionType, 0);
        if (score == 0) {
            result.put("success", false);
            result.put("message", "无效的行为类型");
            result.put("score", 0);
            return result;
        }

        ApUserLevel userLevel = levelQueryService.getUserLevel(userId);

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        Integer dailyLimit = DAILY_ACTION_LIMIT.get(actionType);
        if (dailyLimit != null) {
            if (getTodayActionCount(userId, actionType, today) >= dailyLimit) {
                result.put("success", false);
                result.put("message", "今日该行为已达上限");
                result.put("score", 0);
                return result;
            }
        }

        int todayScore = getTodayScore(userId, today);

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

        upsertDailyProgress(userId, actionType);

        userLevel.setDailyScore(userLevel.getDailyScore() + actualScore);
        userLevel.setDailyScoreToday(userLevel.getDailyScoreToday() + actualScore);

        int newDailyLevel = levelQueryService.calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            permissionService.updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
            diamondService.grantDiamondOnLevelUp(userId, 1, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}执行行为{}，获得逐日分{}，当前逐日等级{}", userId, actionType, actualScore,
            userLevel.getDailyLevel());

        result.put("success", true);
        result.put("message", "行为记录成功");
        result.put("score", actualScore);
        return result;
    }

    /**
     * 每日签到
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkIn(Long userId) {
        Map<String, Object> result = new HashMap<>();

        String today = new java.sql.Date(System.currentTimeMillis()).toString();
        LambdaQueryWrapper<ApUserActionLog> logQuery = new LambdaQueryWrapper<>();
        logQuery.eq(ApUserActionLog::getUserId, userId);
        logQuery.eq(ApUserActionLog::getActionType, "daily_checkin");
        logQuery.apply("DATE(created_time) = {0}", today);
        long todayCheckinCount = actionLogMapper.selectCount(logQuery);

        if (todayCheckinCount > 0) {
            result.put("success", false);
            result.put("hasCheckedIn", true);
            result.put("score", 0);
            return result;
        }

        ApUserLevel userLevel = levelQueryService.getUserLevel(userId);

        Integer dailyLimit = DAILY_ACTION_LIMIT.get("daily_checkin");
        if (dailyLimit != null && todayCheckinCount >= dailyLimit) {
            result.put("success", false);
            result.put("hasCheckedIn", true);
            result.put("score", 0);
            return result;
        }

        int todayScore = getTodayScore(userId, today);

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

        int newDailyLevel = levelQueryService.calculateLevel(1, userLevel.getDailyScore());
        if (newDailyLevel != userLevel.getDailyLevel()) {
            int oldLevel = userLevel.getDailyLevel();
            userLevel.setDailyLevel(newDailyLevel);
            permissionService.updateUserPermissions(userId, 1, oldLevel, newDailyLevel);
        }

        userLevelMapper.updateById(userLevel);

        log.info("用户{}签到成功，获得逐日分{}，当前逐日等级{}", userId, actualScore, userLevel.getDailyLevel());

        result.put("success", true);
        result.put("hasCheckedIn", true);
        result.put("score", actualScore);
        return result;
    }

    /**
     * 获取今日任务进度
     */
    public Map<String, Object> getTodayTaskProgress(Long userId) {
        return taskProgressBuilder.buildTaskProgress(userId);
    }

    /**
     * 获取用户今日积分总和
     */
    private int getTodayScore(Long userId, String today) {
        LambdaQueryWrapper<ApUserActionLog> query = new LambdaQueryWrapper<>();
        query.eq(ApUserActionLog::getUserId, userId);
        query.apply("DATE(created_time) = {0}", today);
        return actionLogMapper.selectList(query).stream()
            .mapToInt(ApUserActionLog::getScoreChange).sum();
    }

    /**
     * 获取用户今日指定行为次数
     */
    private long getTodayActionCount(Long userId, String actionType, String today) {
        LambdaQueryWrapper<ApUserActionLog> query = new LambdaQueryWrapper<>();
        query.eq(ApUserActionLog::getUserId, userId);
        query.eq(ApUserActionLog::getActionType, actionType);
        query.apply("DATE(created_time) = {0}", today);
        return actionLogMapper.selectCount(query);
    }

    /**
     * 记录被动行为每日进度（不发积分、不写行为日志）
     * 用于"社区影响力"被动行为（be_followed/pin_liked/article_liked）的进度统计
     */
    public void recordPassiveAction(Long userId, String actionType) {
        upsertDailyProgress(userId, actionType);
    }

    /**
     * 写入/更新用户每日行为进度表 ap_user_daily_progress
     * 仅统计行为配置表中存在的行为，失败不影响主流程
     */
    private void upsertDailyProgress(Long userId, String actionType) {
        try {
            String actionCode = normalizeActionCode(actionType);
            if (actionCode == null) {
                return;
            }
            // 行为不在配置表中（如 daily_checkin/share）则跳过
            LambdaQueryWrapper<ApBehaviorConfig> configQuery = new LambdaQueryWrapper<>();
            configQuery.eq(ApBehaviorConfig::getActionCode, actionCode);
            configQuery.eq(ApBehaviorConfig::getIsActive, 1);
            if (behaviorConfigMapper.selectCount(configQuery) == 0) {
                return;
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            LambdaQueryWrapper<ApUserDailyProgress> progressQuery = new LambdaQueryWrapper<>();
            progressQuery.eq(ApUserDailyProgress::getUserId, userId);
            progressQuery.eq(ApUserDailyProgress::getStatDate, today);
            progressQuery.eq(ApUserDailyProgress::getActionCode, actionCode);
            ApUserDailyProgress progress = userDailyProgressMapper.selectOne(progressQuery);

            if (progress != null) {
                progress.setCount((progress.getCount() == null ? 0 : progress.getCount()) + 1);
                progress.setUpdatedTime(new Date());
                userDailyProgressMapper.updateById(progress);
            } else {
                ApUserDailyProgress newProgress = new ApUserDailyProgress();
                newProgress.setUserId(userId);
                newProgress.setStatDate(today);
                newProgress.setActionCode(actionCode);
                newProgress.setCount(1);
                newProgress.setUpdatedTime(new Date());
                userDailyProgressMapper.insert(newProgress);
            }
        } catch (Exception e) {
            log.warn("写入用户每日行为进度失败: userId={}, actionType={}", userId, actionType, e);
        }
    }

    /**
     * 将行为编码归一化为 ap_behavior_config.action_code
     */
    private String normalizeActionCode(String actionType) {
        if (actionType == null) {
            return null;
        }
        switch (actionType) {
            case "publish_pins": return "publish_pin";
            case "browse_course": return "browse_article";
            default: return actionType;
        }
    }
}