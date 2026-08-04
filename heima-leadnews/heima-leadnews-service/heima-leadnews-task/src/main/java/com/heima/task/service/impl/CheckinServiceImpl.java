package com.heima.task.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.task.entity.*;
import com.heima.task.mapper.*;
import com.heima.task.service.CheckinService;
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

    @Override
    public ResponseResult getDashboard(Long userId) {
        // 获取用户签到状态
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        if (state == null) {
            state = new UserCheckinState();
            state.setUserId(userId);
            state.setContinuousDays(0);
            state.setPeriodDay(0);
            state.setTotalCheckinDays(0);
            state.setPatchCardCount(0);
        }

        // 获取用户资产
        UserAssets assets = userAssetsMapper.selectById(userId);
        int oreBalance = (assets != null) ? assets.getOreBalance() : 0;

        // 获取今日是否已签到
        String todayStr = DateUtil.today();
        boolean todaySigned = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, userId)
                        .eq(CheckinRecord::getCheckinDate, java.sql.Date.valueOf(todayStr))
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
                .collect(Collectors.toMap(r -> DateUtil.formatDate(r.getCheckinDate()), CheckinRecord::getEarnedOre));

        // 生成最近2个月的日历
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.MONTH, -1);
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endCal = Calendar.getInstance();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

        for (Calendar d = (Calendar) startCal.clone(); d.before(endCal) || d.equals(endCal); d.add(Calendar.DAY_OF_MONTH, 1)) {
            String dateStr = DateUtil.formatDate(d.getTime());
            String todayCheck = DateUtil.today();
            Map<String, Object> day = new HashMap<>();
            day.put("date", dateStr);
            day.put("dayOfMonth", d.get(Calendar.DAY_OF_MONTH));

            if (dateStr.equals(todayCheck)) {
                day.put("status", todaySigned ? "signed" : "today");
            } else if (signedDates.contains(dateStr)) {
                // 检查是否为补签
                Optional<CheckinRecord> rec = records.stream()
                        .filter(r -> DateUtil.formatDate(r.getCheckinDate()).equals(dateStr))
                        .findFirst();
                day.put("status", rec.isPresent() && rec.get().getIsPatch() ? "repaired" : "signed");
            } else if (d.getTime().before(new Date())) {
                day.put("status", "miss");
            } else {
                day.put("status", "future");
            }

            if (dateOreMap.containsKey(dateStr)) {
                day.put("oreReward", dateOreMap.get(dateStr));
            }
            // 判断是否为特殊奖励节点
            day.put("isSpecial", false);
            calendarDays.add(day);
        }

        // 计算下一个特殊奖励节点
        Map<String, Object> nextSpecial = null;
        if (state.getPeriodDay() != null && state.getPeriodDay() > 0) {
            int[] specialDays = {3, 7, 14, 21, 30};
            for (int sd : specialDays) {
                if (sd > state.getPeriodDay()) {
                    CheckinRewardConfig config = checkinRewardConfigMapper.selectById(sd);
                    if (config != null) {
                        nextSpecial = new HashMap<>();
                        nextSpecial.put("day", sd);
                        nextSpecial.put("ore", config.getSpecialOre());
                        nextSpecial.put("daysLeft", sd - state.getPeriodDay());
                    }
                    break;
                }
            }
        }

        // 计算今日应得矿石
        int pendingReward = 0;
        if (!todaySigned && state.getPeriodDay() != null && state.getPeriodDay() > 0) {
            int nextDay = (state.getPeriodDay() % 30) + 1;
            CheckinRewardConfig config = checkinRewardConfigMapper.selectById(nextDay);
            if (config != null) {
                pendingReward = config.getIsSpecial() ? config.getSpecialOre() : config.getBaseOre();
            }
        } else if (!todaySigned && (state.getPeriodDay() == null || state.getPeriodDay() == 0)) {
            pendingReward = 10; // 第一天基础值
        }

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("nickname", "用户" + userId);
        userInfo.put("level", "JY.1");
        userInfo.put("avatar", "");

        Map<String, Object> stats = new HashMap<>();
        stats.put("continuousDays", state.getContinuousDays() != null ? state.getContinuousDays() : 0);
        stats.put("totalDays", state.getTotalCheckinDays() != null ? state.getTotalCheckinDays() : 0);
        stats.put("oreBalance", oreBalance);
        stats.put("todaySigned", todaySigned);
        stats.put("pendingReward", pendingReward);

        data.put("userInfo", userInfo);
        data.put("checkinStats", stats);
        data.put("calendar", calendarDays);
        data.put("patchCardCount", state.getPatchCardCount() != null ? state.getPatchCardCount() : 0);
        data.put("currentPeriodDay", state.getPeriodDay() != null ? state.getPeriodDay() : 0);
        data.put("nextSpecialReward", nextSpecial);

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
                // 检查是否有补签覆盖了空档
                newContinuousDays = 1; // 断签重置
            }
        }
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

        // 7. 更新矿石余额（使用updateById替代updateOreBalance）
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

        // 计算下一个特殊奖励
        int[] specialDays = {3, 7, 14, 21, 30};
        Map<String, Object> nextSpecial = null;
        for (int sd : specialDays) {
            if (sd > newPeriodDay) {
                CheckinRewardConfig rc = checkinRewardConfigMapper.selectById(sd);
                if (rc != null) {
                    nextSpecial = new HashMap<>();
                    nextSpecial.put("day", sd);
                    nextSpecial.put("ore", rc.getSpecialOre());
                    nextSpecial.put("daysLeft", sd - newPeriodDay);
                }
                break;
            }
        }
        data.put("nextSpecial", nextSpecial);
        data.put("totalOreBalance", (assets != null ? assets.getOreBalance() : 0) + earnedOre);

        // 里程碑进度
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("current", newPeriodDay);
        milestone.put("total", 30);
        milestone.put("specialDays", Arrays.asList(3, 7, 14, 21, 30));
        data.put("milestoneProgress", milestone);

        return ResponseResult.okResult(data);
    }

    /**
     * 授予免费抽奖次数（暂未实现完整逻辑）
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

        // 1. 校验目标日期必须在最近2个月内
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        if (target.before(cal.getTime())) {
            return ResponseResult.errorResult(400, "只能补签最近2个月内的日期");
        }
        if (target.after(new Date())) {
            return ResponseResult.errorResult(400, "不能补签未来日期");
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

        // 4. 计算补签该日应得的矿石数
        int newContinuousDays = (state.getContinuousDays() != null ? state.getContinuousDays() : 0) + 1;
        int newPeriodDay = (newContinuousDays - 1) % 30 + 1;

        CheckinRewardConfig config = checkinRewardConfigMapper.selectById(newPeriodDay);
        int earnedOre = config != null ? (config.getIsSpecial() ? config.getSpecialOre() : config.getBaseOre()) : 10;

        // 5. 扣除补签卡
        state.setPatchCardCount(state.getPatchCardCount() - 1);
        userCheckinStateMapper.updateById(state);

        // 6. 插入补签记录
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(target);
        record.setEarnedOre(earnedOre);
        record.setPeriodDay(newPeriodDay);
        record.setIsPatch(true);
        record.setCreatedAt(new Date());
        checkinRecordMapper.insert(record);

        // 7. 更新连续天数
        state.setContinuousDays(newContinuousDays);
        state.setPeriodDay(newPeriodDay);
        state.setLastCheckinDate(target);
        userCheckinStateMapper.updateById(state);

        // 8. 记录补签卡消耗日志
        PatchCardLog patchLog = new PatchCardLog();
        patchLog.setUserId(userId);
        patchLog.setChangeAmount(-1);
        patchLog.setSource("补签");
        patchLog.setCreatedAt(new Date());
        patchCardLogMapper.insert(patchLog);

        // 9. 更新矿石（使用updateById替代updateOreBalance）
        UserAssets assets = userAssetsMapper.selectById(userId);
        if (assets != null) {
            assets.setOreBalance(assets.getOreBalance() + earnedOre);
            assets.setUpdatedAt(new Date());
            userAssetsMapper.updateById(assets);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("earnedOre", earnedOre);
        data.put("isPatch", true);
        data.put("newContinuousDays", newContinuousDays);
        data.put("periodDay", newPeriodDay);

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getMilestone(Long userId) {
        UserCheckinState state = userCheckinStateMapper.selectById(userId);
        int periodDay = (state != null && state.getPeriodDay() != null) ? state.getPeriodDay() : 0;

        List<Map<String, Object>> milestones = new ArrayList<>();
        int[] specialDays = {3, 7, 14, 21, 30};
        for (int sd : specialDays) {
            CheckinRewardConfig config = checkinRewardConfigMapper.selectById(sd);
            Map<String, Object> m = new HashMap<>();
            m.put("day", sd);
            m.put("ore", config != null ? config.getSpecialOre() : 0);
            m.put("achieved", periodDay >= sd);
            if (!m.get("achieved").equals(true)) {
                m.put("daysLeft", sd - periodDay);
            }
            milestones.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("periodDay", periodDay);
        data.put("totalDays", 30);
        data.put("specialMilestones", milestones);
        data.put("canClaimSpecial", Arrays.asList(3, 7, 14, 21, 30).contains(periodDay));

        return ResponseResult.okResult(data);
    }
}