package com.heima.reward.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.apis.article.ILevelClient;
import com.heima.apis.user.IUserClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.reward.entity.*;
import com.heima.reward.mapper.*;
import com.heima.reward.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;
    @Autowired
    private UserCheckinStateMapper userCheckinStateMapper;
    @Autowired
    private CheckinRewardConfigMapper checkinRewardConfigMapper;
    @Autowired
    private UserAssetsMapper userAssetsMapper;
    @Autowired
    private PatchCardLogMapper patchCardLogMapper;
    @Autowired
    private IUserClient userClient;
    @Autowired
    private ILevelClient levelClient;

    // 特殊奖励日
    private static final int[] SPECIAL_DAYS = {3, 7, 14, 21, 30};

    @Override
    public ResponseResult getDashboard(Long userId) {
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        if (state == null) {
            state = new UserCheckinState();
            state.setUserId(userId);
            state.setContinuousDays(0);
            state.setPeriodDay(0);
            state.setTotalCheckinDays(0);
            state.setPatchCardCount(0);
        }

        UserAssets assets = userAssetsMapper.selectById(userId);
        int oreBalance = (assets != null) ? assets.getOreBalance() : 0;

        String todayStr = DateUtil.today();
        Date todayDate = java.sql.Date.valueOf(todayStr);
        boolean todaySigned = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, todayDate)
        ) > 0;

        // 获取签到日历数据（最近2个月）
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        cal.add(Calendar.MONTH, -2);
        Date startDate = cal.getTime();

        List<CheckinRecord> records = checkinRecordMapper.selectList(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .ge(CheckinRecord::getCheckinDate, startDate)
                        .le(CheckinRecord::getCheckinDate, endDate)
                        .orderByAsc(CheckinRecord::getCheckinDate)
        );

        // 构建日历
        List<Map<String, Object>> calendarDays = new ArrayList<>();
        Set<String> signedDates = records.stream()
                .map(r -> DateUtil.formatDate(r.getCheckinDate()))
                .collect(Collectors.toSet());
        Map<String, Integer> dateOreMap = records.stream()
                .collect(Collectors.toMap(
                        r -> DateUtil.formatDate(r.getCheckinDate()),
                        CheckinRecord::getEarnedOre,
                        (a, b) -> a));

        // 生成最近2个月的日历
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.MONTH, -1);
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

        for (Calendar d = (Calendar) startCal.clone(); !d.after(endCal); d.add(Calendar.DAY_OF_MONTH, 1)) {
            String dateStr = DateUtil.formatDate(d.getTime());
            Map<String, Object> day = new HashMap<>();
            day.put("date", dateStr);
            day.put("dayOfMonth", d.get(Calendar.DAY_OF_MONTH));

            if (dateStr.equals(todayStr)) {
                day.put("status", todaySigned ? "signed" : "today");
            } else if (signedDates.contains(dateStr)) {
                Optional<CheckinRecord> rec = records.stream()
                        .filter(r -> DateUtil.formatDate(r.getCheckinDate()).equals(dateStr))
                        .findFirst();
                day.put("status", rec.isPresent() && rec.get().getIsPatch() ? "repaired" : "signed");
            } else if (d.getTime().before(todayDate)) {
                day.put("status", "miss");
            } else {
                day.put("status", "future");
            }

            if (dateOreMap.containsKey(dateStr)) {
                day.put("oreReward", dateOreMap.get(dateStr));
            }
            day.put("isSpecial", false);
            calendarDays.add(day);
        }

        // 计算下一个特殊奖励节点
        Map<String, Object> nextSpecial = calculateNextSpecial(state.getPeriodDay());

        // 计算今日应得矿石
        int pendingReward = 0;
        if (!todaySigned) {
            int nextDay = (state.getPeriodDay() != null && state.getPeriodDay() > 0)
                    ? (state.getPeriodDay() % 30) + 1
                    : 1;
            CheckinRewardConfig config = checkinRewardConfigMapper.selectById(nextDay);
            if (config != null) {
                pendingReward = config.getIsSpecial() ? config.getSpecialOre() : config.getBaseOre();
            } else {
                pendingReward = 10;
            }
        }

        // 用户信息
        Map<String, Object> userInfo = buildUserInfo(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("continuousDays", state.getContinuousDays() != null ? state.getContinuousDays() : 0);
        stats.put("totalDays", state.getTotalCheckinDays() != null ? state.getTotalCheckinDays() : 0);
        stats.put("oreBalance", oreBalance);
        stats.put("todaySigned", todaySigned);
        stats.put("pendingReward", pendingReward);

        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", userInfo);
        data.put("checkinStats", stats);
        data.put("calendar", calendarDays);
        data.put("patchCardCount", state.getPatchCardCount() != null ? state.getPatchCardCount() : 0);
        data.put("currentPeriodDay", state.getPeriodDay() != null ? state.getPeriodDay() : 0);
        data.put("nextSpecialReward", nextSpecial);

        // 构建里程碑进度
        data.put("milestoneProgress", buildMilestoneProgress(state.getPeriodDay()));

        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult doCheckin(Long userId) {
        // 1. 校验今日是否已签到
        String todayStr = DateUtil.today();
        Date todayDate = java.sql.Date.valueOf(todayStr);
        long count = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, todayDate)
        );
        if (count > 0) {
            return ResponseResult.errorResult(400, "今日已签到，请勿重复签到");
        }

        // 2. 获取或创建签到状态
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        if (state == null) {
            state = new UserCheckinState();
            state.setUserId(userId);
            state.setContinuousDays(0);
            state.setPeriodDay(0);
            state.setTotalCheckinDays(0);
            state.setPatchCardCount(0);
        }

        // 3. 计算连续天数
        int newContinuousDays;
        int newPeriodDay;
        Date lastDate = state.getLastCheckinDate();

        if (lastDate == null) {
            newContinuousDays = 1;
        } else {
            long diff = DateUtil.betweenDay(lastDate, todayDate, false);
            if (diff == 1) {
                newContinuousDays = (state.getContinuousDays() != null ? state.getContinuousDays() : 0) + 1;
            } else if (diff == 0) {
                return ResponseResult.errorResult(400, "今日已签到");
            } else {
                newContinuousDays = 1; // 断签重置
            }
        }
        // 周期天数：30天一个周期
        newPeriodDay = (newContinuousDays - 1) % 30 + 1;

        // 4. 计算应得矿石
        CheckinRewardConfig config = checkinRewardConfigMapper.selectById(newPeriodDay);
        int earnedOre;
        boolean isSpecial = false;
        if (config != null && config.getIsSpecial()) {
            earnedOre = config.getSpecialOre();
            isSpecial = true;
        } else if (config != null) {
            earnedOre = config.getBaseOre();
        } else {
            earnedOre = 10;
        }

        // 5. 插入签到记录
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(todayDate);
        record.setEarnedOre(earnedOre);
        record.setPeriodDay(newPeriodDay);
        record.setIsPatch(false);
        record.setCreatedAt(new Date());
        checkinRecordMapper.insert(record);

        // 6. 更新用户签到状态
        state.setContinuousDays(newContinuousDays);
        state.setPeriodDay(newPeriodDay);
        state.setLastCheckinDate(todayDate);
        state.setTotalCheckinDays((state.getTotalCheckinDays() != null ? state.getTotalCheckinDays() : 0) + 1);
        if (userCheckinStateMapper.selectById(userId) == null) {
            userCheckinStateMapper.insert(state);
        } else {
            userCheckinStateMapper.updateById(state);
        }

        // 7. 更新矿石余额
        UserAssets assets = userAssetsMapper.selectById(userId);
        if (assets == null) {
            assets = new UserAssets();
            assets.setUserId(userId);
            assets.setOreBalance(earnedOre);
            assets.setFrozenOre(0);
            assets.setLuckyValue(0);
            assets.setCreatedAt(new Date());
            assets.setUpdatedAt(new Date());
            userAssetsMapper.insert(assets);
        } else {
            assets.setOreBalance(assets.getOreBalance() + earnedOre);
            assets.setUpdatedAt(new Date());
            userAssetsMapper.updateById(assets);
        }

        // 8. 签到成功后，赠送免费抽奖机会
        grantFreeDraw(userId, todayDate);

        // 9. 构建返回
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("earnedOre", earnedOre);
        data.put("newContinuousDays", newContinuousDays);
        data.put("isSpecialReward", isSpecial);
        data.put("periodDay", newPeriodDay);

        // 下一个特殊奖励
        Map<String, Object> nextSpecial = calculateNextSpecial(newPeriodDay);
        data.put("nextSpecial", nextSpecial);
        data.put("totalOreBalance", assets.getOreBalance());

        // 里程碑进度
        data.put("milestoneProgress", buildMilestoneProgress(newPeriodDay));

        return ResponseResult.okResult(data);
    }

    /**
     * 授予免费抽奖次数
     */
    private void grantFreeDraw(Long userId, Date date) {
        try {
            log.info("签到成功，后续可在此处为 userId={} 授予免费抽奖次数", userId);
        } catch (Exception e) {
            log.warn("授予免费抽奖次数失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult patchCheckin(Long userId, String targetDate) {
        Date target = java.sql.Date.valueOf(targetDate);
        String todayStr = DateUtil.today();
        Date todayDate = java.sql.Date.valueOf(todayStr);

        // 1. 校验目标日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        if (target.before(cal.getTime())) {
            return ResponseResult.errorResult(400, "只能补签最近2个月内的日期");
        }
        if (target.after(todayDate)) {
            return ResponseResult.errorResult(400, "不能补签未来日期");
        }
        if (target.equals(todayDate)) {
            return ResponseResult.errorResult(400, "今日签到请使用签到功能");
        }

        // 2. 校验该日是否已签到
        long count = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, target)
        );
        if (count > 0) {
            return ResponseResult.errorResult(400, "该日已签到，无需补签");
        }

        // 3. 获取签到状态
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        if (state == null || state.getPatchCardCount() == null || state.getPatchCardCount() <= 0) {
            return ResponseResult.errorResult(400, "补签卡不足");
        }

        // 4. 计算补签后连续天数
        Date lastDate = state.getLastCheckinDate();
        int newContinuousDays;
        if (lastDate == null) {
            newContinuousDays = 1;
        } else {
            long diff = DateUtil.betweenDay(lastDate, target, false);
            if (diff == 1) {
                newContinuousDays = (state.getContinuousDays() != null ? state.getContinuousDays() : 0) + 1;
            } else {
                newContinuousDays = 1;
            }
        }
        int newPeriodDay = (newContinuousDays - 1) % 30 + 1;

        // 5. 计算应得矿石
        CheckinRewardConfig config = checkinRewardConfigMapper.selectById(newPeriodDay);
        int earnedOre;
        boolean isSpecial = false;
        if (config != null && config.getIsSpecial()) {
            earnedOre = config.getSpecialOre();
            isSpecial = true;
        } else if (config != null) {
            earnedOre = config.getBaseOre();
        } else {
            earnedOre = 10;
        }

        // 6. 扣除补签卡
        state.setPatchCardCount(state.getPatchCardCount() - 1);
        userCheckinStateMapper.updateById(state);

        // 7. 插入补签记录
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(target);
        record.setEarnedOre(earnedOre);
        record.setPeriodDay(newPeriodDay);
        record.setIsPatch(true);
        record.setCreatedAt(new Date());
        checkinRecordMapper.insert(record);

        // 8. 更新签到状态
        state.setContinuousDays(newContinuousDays);
        state.setPeriodDay(newPeriodDay);
        if (target.after(state.getLastCheckinDate())) {
            state.setLastCheckinDate(target);
        }
        userCheckinStateMapper.updateById(state);

        // 9. 记录补签卡消耗日志
        PatchCardLog patchLog = new PatchCardLog();
        patchLog.setUserId(userId);
        patchLog.setChangeAmount(-1);
        patchLog.setSource("补签");
        patchLog.setCreatedAt(new Date());
        patchCardLogMapper.insert(patchLog);

        // 10. 更新矿石
        UserAssets assets = userAssetsMapper.selectById(userId);
        if (assets != null) {
            assets.setOreBalance(assets.getOreBalance() + earnedOre);
            assets.setUpdatedAt(new Date());
            userAssetsMapper.updateById(assets);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("earnedOre", earnedOre);
        data.put("isSpecialReward", isSpecial);
        data.put("isPatch", true);
        data.put("newContinuousDays", newContinuousDays);
        data.put("periodDay", newPeriodDay);
        data.put("nextSpecial", calculateNextSpecial(newPeriodDay));

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getTodayStatus(Long userId) {
        String todayStr = DateUtil.today();
        Date todayDate = java.sql.Date.valueOf(todayStr);
        boolean todaySigned = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, todayDate)
        ) > 0;

        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        int continuousDays = (state != null && state.getContinuousDays() != null) ? state.getContinuousDays() : 0;

        UserAssets assets = userAssetsMapper.selectById(userId);
        int totalOre = (assets != null) ? assets.getOreBalance() : 0;

        int patchCardCount = (state != null && state.getPatchCardCount() != null) ? state.getPatchCardCount() : 0;

        Map<String, Object> data = new HashMap<>();
        data.put("isSignedIn", todaySigned);
        data.put("consecutiveDays", continuousDays);
        data.put("totalOre", totalOre);
        data.put("patchCardCount", patchCardCount);

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getMilestone(Long userId) {
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        int periodDay = (state != null && state.getPeriodDay() != null) ? state.getPeriodDay() : 0;

        List<Map<String, Object>> milestones = new ArrayList<>();
        for (int sd : SPECIAL_DAYS) {
            CheckinRewardConfig config = checkinRewardConfigMapper.selectById(sd);
            Map<String, Object> m = new HashMap<>();
            m.put("day", sd);
            m.put("ore", config != null ? config.getSpecialOre() : 0);
            m.put("achieved", periodDay >= sd);
            if (periodDay < sd) {
                m.put("daysLeft", sd - periodDay);
            }
            milestones.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("periodDay", periodDay);
        data.put("totalDays", 30);
        data.put("specialMilestones", milestones);
        data.put("canClaimSpecial", Arrays.asList(SPECIAL_DAYS).contains(periodDay));
        data.put("milestoneProgress", buildMilestoneProgress(periodDay));

        return ResponseResult.okResult(data);
    }

    /**
     * 构建用户信息
     */
    private Map<String, Object> buildUserInfo(Long userId) {
        Map<String, Object> userInfo = new HashMap<>();
        try {
            ResponseResult userResult = userClient.getBasicInfo(userId);
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                userInfo.put("userId", userData.getOrDefault("userId", userId));
                userInfo.put("nickname", userData.getOrDefault("nickname", "用户" + userId));
                userInfo.put("avatar", userData.getOrDefault("avatar", ""));
            } else {
                userInfo.put("userId", userId);
                userInfo.put("nickname", "用户" + userId);
                userInfo.put("avatar", "");
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败 userId={}: {}", userId, e.getMessage());
            userInfo.put("userId", userId);
            userInfo.put("nickname", "用户" + userId);
            userInfo.put("avatar", "");
        }

        try {
            Map<String, Object> levelInfo = levelClient.getUserLevelInfo(userId);
            if (levelInfo != null && !levelInfo.isEmpty()) {
                Integer dailyLevel = levelInfo.get("dailyLevel") instanceof Integer
                        ? (Integer) levelInfo.get("dailyLevel")
                        : 1;
                userInfo.put("level", "ZR." + dailyLevel);
            } else {
                userInfo.put("level", "ZR.1");
            }
        } catch (Exception e) {
            log.warn("获取用户等级信息失败 userId={}: {}", userId, e.getMessage());
            userInfo.put("level", "ZR.1");
        }

        return userInfo;
    }

    /**
     * 计算下一个特殊奖励
     */
    private Map<String, Object> calculateNextSpecial(Integer periodDay) {
        if (periodDay == null || periodDay <= 0) {
            periodDay = 0;
        }
        for (int sd : SPECIAL_DAYS) {
            if (sd > periodDay) {
                CheckinRewardConfig config = checkinRewardConfigMapper.selectById(sd);
                if (config != null) {
                    Map<String, Object> nextSpecial = new HashMap<>();
                    nextSpecial.put("day", sd);
                    nextSpecial.put("ore", config.getSpecialOre());
                    nextSpecial.put("daysLeft", sd - periodDay);
                    return nextSpecial;
                }
            }
        }
        return null;
    }

    /**
     * 构建里程碑进度
     */
    private Map<String, Object> buildMilestoneProgress(Integer periodDay) {
        if (periodDay == null || periodDay < 0) {
            periodDay = 0;
        }
        Map<String, Object> progress = new HashMap<>();
        progress.put("current", periodDay);
        progress.put("total", 30);

        List<Map<String, Object>> specialDays = new ArrayList<>();
        for (int sd : SPECIAL_DAYS) {
            CheckinRewardConfig config = checkinRewardConfigMapper.selectById(sd);
            Map<String, Object> m = new HashMap<>();
            m.put("day", sd);
            m.put("ore", config != null ? config.getSpecialOre() : 0);
            m.put("achieved", periodDay >= sd);
            m.put("isCurrent", periodDay == sd);
            specialDays.add(m);
        }
        progress.put("specialDays", specialDays);

        // 当前进度百分比
        int percent = (int) ((periodDay * 100.0) / 30);
        progress.put("percent", percent);

        return progress;
    }
}