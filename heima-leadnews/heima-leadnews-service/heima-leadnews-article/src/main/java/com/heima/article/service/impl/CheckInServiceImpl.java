package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCheckInMapper;
import com.heima.article.mapper.ApUserLevelMapper;
import com.heima.article.mapper.SignInConfigMapper;
import com.heima.article.mapper.UserOnboardingTaskMapper;
import com.heima.article.mapper.UserSignInSummaryMapper;
import com.heima.article.service.CheckInService;
import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.ApCheckIn;
import com.heima.model.article.pojos.ApUserLevel;
import com.heima.model.article.pojos.SignInConfig;
import com.heima.model.article.pojos.UserOnboardingTask;
import com.heima.model.article.pojos.UserSignInSummary;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private ApCheckInMapper checkInMapper;

    @Autowired
    private SignInConfigMapper signInConfigMapper;

    @Autowired
    private UserSignInSummaryMapper summaryMapper;

    @Autowired
    private UserOnboardingTaskMapper onboardingTaskMapper;

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Autowired
    private LevelService levelService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_BONUS = 10;

    @Override
    public Map<String, Object> getDashboard(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // user info
        ApUser user = AppThreadLocalUtil.getUser();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getNickname());

        ApUserLevel userLevel = levelService.getUserLevel(userId);
        userMap.put("level", "JY." + userLevel.getDailyLevel());
        result.put("user", userMap);

        // today status
        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);
        LambdaQueryWrapper<ApCheckIn> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(ApCheckIn::getUserId, userId);
        todayQuery.eq(ApCheckIn::getCheckInDate, todaySql);
        ApCheckIn todayRecord = checkInMapper.selectOne(todayQuery);

        Map<String, Object> todayStatus = new HashMap<>();
        todayStatus.put("signed", todayRecord != null);
        todayStatus.put("reward", todayRecord != null ? todayRecord.getRewardPoints() : 0);
        result.put("todayStatus", todayStatus);

        // stats from summary
        UserSignInSummary summary = getOrCreateSummary(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("consecutive", summary.getCurrentConsecutiveDays() != null ? summary.getCurrentConsecutiveDays() : 0);
        stats.put("total", summary.getTotalSignedDays() != null ? summary.getTotalSignedDays() : 0);
        stats.put("ore", summary.getTotalOre() != null ? summary.getTotalOre() : 0L);
        result.put("stats", stats);

        // cards
        Map<String, Object> cards = new HashMap<>();
        int retroactiveCards = summary.getRetroactiveCardCount() != null ? summary.getRetroactiveCardCount() : 0;
        cards.put("retroactive", retroactiveCards);
        result.put("cards", cards);

        // calendar
        Map<String, Object> calendar = buildCalendarData(userId, today.getYear(), today.getMonthValue(), retroactiveCards);
        result.put("calendar", calendar);

        // tasks
        List<Map<String, Object>> tasks = buildTasksData(userId);
        result.put("tasks", tasks);

        return result;
    }

    @Override
    public Map<String, Object> doCheckIn(Long userId) {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);

        // check if already signed today
        LambdaQueryWrapper<ApCheckIn> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(ApCheckIn::getUserId, userId);
        todayQuery.eq(ApCheckIn::getCheckInDate, todaySql);
        Long todayCount = checkInMapper.selectCount(todayQuery);

        if (todayCount > 0) {
            result.put("success", false);
            result.put("message", "今天已签到");
            return result;
        }

        // get consecutive days from summary
        UserSignInSummary summary = getOrCreateSummary(userId);
        int consecutiveDays = summary.getCurrentConsecutiveDays() != null ? summary.getCurrentConsecutiveDays() : 0;

        // get today's reward from config
        int dayOfMonth = today.getDayOfMonth();
        LambdaQueryWrapper<SignInConfig> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(SignInConfig::getDayOfMonth, dayOfMonth);
        configQuery.eq(SignInConfig::getIsActive, 1);
        SignInConfig config = signInConfigMapper.selectOne(configQuery);
        int baseReward = config != null ? config.getBaseReward() : 5;

        // calculate consecutive bonus
        int bonus = Math.min(consecutiveDays, MAX_BONUS);
        int totalReward = baseReward + bonus;

        // insert check-in record
        ApCheckIn checkIn = new ApCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(todaySql);
        checkIn.setRewardPoints(totalReward);
        checkIn.setIsRetroactive(0);
        checkIn.setConsecutiveDays(consecutiveDays + 1);
        checkIn.setCreatedTime(new Date());
        checkInMapper.insert(checkIn);

        // update summary
        summary.setTotalSignedDays(summary.getTotalSignedDays() != null ? summary.getTotalSignedDays() + 1 : 1);
        summary.setCurrentConsecutiveDays(consecutiveDays + 1);
        int maxConsecutive = summary.getMaxConsecutiveDays() != null ? summary.getMaxConsecutiveDays() : 0;
        if (summary.getCurrentConsecutiveDays() > maxConsecutive) {
            summary.setMaxConsecutiveDays(summary.getCurrentConsecutiveDays());
        }
        summary.setLastSignDate(todaySql);
        summary.setTotalOre(summary.getTotalOre() != null ? summary.getTotalOre() + totalReward : totalReward);
        summary.setUpdateTime(new Date());
        summaryMapper.updateById(summary);

        log.info("用户{}签到成功，获得积分{}，连续签到{}天", userId, totalReward, consecutiveDays + 1);

        result.put("success", true);
        result.put("reward", totalReward);
        result.put("consecutiveDays", consecutiveDays + 1);
        result.put("totalDays", summary.getTotalSignedDays());
        result.put("totalOre", summary.getTotalOre());

        return result;
    }

    @Override
    public Map<String, Object> doRetroactive(Long userId, String missedDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDate missed = LocalDate.parse(missedDate, DATE_FORMATTER);
        LocalDate today = LocalDate.now();

        // validate missedDate is in the past
        if (!missed.isBefore(today)) {
            result.put("success", false);
            result.put("message", "只能补签过去的日期");
            return result;
        }

        // check not already signed
        java.sql.Date missedSql = java.sql.Date.valueOf(missed);
        LambdaQueryWrapper<ApCheckIn> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(ApCheckIn::getUserId, userId);
        checkQuery.eq(ApCheckIn::getCheckInDate, missedSql);
        Long count = checkInMapper.selectCount(checkQuery);
        if (count > 0) {
            result.put("success", false);
            result.put("message", "该日期已签到");
            return result;
        }

        // check retroactive cards
        UserSignInSummary summary = getOrCreateSummary(userId);
        if (summary.getRetroactiveCardCount() == null || summary.getRetroactiveCardCount() <= 0) {
            result.put("success", false);
            result.put("message", "没有补签卡");
            return result;
        }

        // get reward from config
        int dayOfMonth = missed.getDayOfMonth();
        LambdaQueryWrapper<SignInConfig> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(SignInConfig::getDayOfMonth, dayOfMonth);
        configQuery.eq(SignInConfig::getIsActive, 1);
        SignInConfig config = signInConfigMapper.selectOne(configQuery);
        int reward = config != null ? config.getBaseReward() : 5;

        // insert check-in record
        ApCheckIn checkIn = new ApCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(missedSql);
        checkIn.setRewardPoints(reward);
        checkIn.setIsRetroactive(1);
        checkIn.setConsecutiveDays(0);
        checkIn.setCreatedTime(new Date());
        checkInMapper.insert(checkIn);

        // update summary
        summary.setRetroactiveCardCount(summary.getRetroactiveCardCount() - 1);
        summary.setTotalSignedDays(summary.getTotalSignedDays() != null ? summary.getTotalSignedDays() + 1 : 1);
        summary.setTotalOre(summary.getTotalOre() != null ? summary.getTotalOre() + reward : reward);
        summary.setUpdateTime(new Date());
        summaryMapper.updateById(summary);

        log.info("用户{}补签{}成功，获得积分{}", userId, missedDate, reward);

        result.put("success", true);
        result.put("reward", reward);
        result.put("date", missedDate);
        result.put("retroactiveCardsLeft", summary.getRetroactiveCardCount());

        return result;
    }

    @Override
    public Map<String, Object> getCheckInRecords(Long userId, Integer year, Integer month) {
        UserSignInSummary summary = getOrCreateSummary(userId);
        int retroactiveCards = summary.getRetroactiveCardCount() != null ? summary.getRetroactiveCardCount() : 0;
        return buildCalendarData(userId, year, month, retroactiveCards);
    }

    @Override
    public Map<String, Object> getCheckInStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<UserSignInSummary> query = new LambdaQueryWrapper<>();
        query.eq(UserSignInSummary::getUserId, userId);
        UserSignInSummary summary = summaryMapper.selectOne(query);

        if (summary == null) {
            result.put("consecutiveDays", 0);
            result.put("totalDays", 0);
            result.put("totalPoints", 0L);
            result.put("patchCardCount", 0);
            result.put("todaySigned", false);
            return result;
        }

        // check if today signed
        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);
        LambdaQueryWrapper<ApCheckIn> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(ApCheckIn::getUserId, userId);
        todayQuery.eq(ApCheckIn::getCheckInDate, todaySql);
        Long todayCount = checkInMapper.selectCount(todayQuery);

        result.put("consecutiveDays", summary.getCurrentConsecutiveDays() != null ? summary.getCurrentConsecutiveDays() : 0);
        result.put("totalDays", summary.getTotalSignedDays() != null ? summary.getTotalSignedDays() : 0);
        result.put("totalPoints", summary.getTotalOre() != null ? summary.getTotalOre() : 0L);
        result.put("patchCardCount", summary.getRetroactiveCardCount() != null ? summary.getRetroactiveCardCount() : 0);
        result.put("todaySigned", todayCount > 0);

        return result;
    }

    @Override
    public Map<String, Object> getCheckInTasks(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tasks = buildTasksData(userId);
        result.put("list", tasks);
        return result;
    }

    @Override
    public Map<String, Object> getTodayStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("isSignedIn", false);
            result.put("consecutiveDays", 0);
            result.put("totalOre", 0L);
            return result;
        }

        UserSignInSummary summary = getOrCreateSummary(userId);
        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);

        // 判断今日是否已签到
        LambdaQueryWrapper<ApCheckIn> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(ApCheckIn::getUserId, userId);
        todayQuery.eq(ApCheckIn::getCheckInDate, todaySql);
        Long todayCount = checkInMapper.selectCount(todayQuery);

        result.put("isSignedIn", todayCount > 0);
        result.put("consecutiveDays", summary.getCurrentConsecutiveDays() != null ? summary.getCurrentConsecutiveDays() : 0);
        result.put("totalOre", summary.getTotalOre() != null ? summary.getTotalOre() : 0L);
        return result;
    }

    /**
     * 计算连续签到天数（从昨天开始往前数，直到遇到中断）
     */
    private int calculateConsecutiveDays(Long userId, java.sql.Date today) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -1); // 从昨天开始

        int consecutiveDays = 0;
        while (true) {
            java.sql.Date checkDate = new java.sql.Date(cal.getTimeInMillis());
            LambdaQueryWrapper<ApCheckIn> query = new LambdaQueryWrapper<>();
            query.eq(ApCheckIn::getUserId, userId);
            query.eq(ApCheckIn::getCheckInDate, checkDate);
            long count = checkInMapper.selectCount(query);
            if (count > 0) {
                consecutiveDays++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                break;
            }
        }
        return consecutiveDays;
    }

    private UserSignInSummary getOrCreateSummary(Long userId) {
        LambdaQueryWrapper<UserSignInSummary> query = new LambdaQueryWrapper<>();
        query.eq(UserSignInSummary::getUserId, userId);
        UserSignInSummary summary = summaryMapper.selectOne(query);
        if (summary == null) {
            summary = new UserSignInSummary();
            summary.setUserId(userId);
            summary.setCurrentConsecutiveDays(0);
            summary.setMaxConsecutiveDays(0);
            summary.setTotalSignedDays(0);
            summary.setRetroactiveCardCount(0);
            summary.setTotalOre(0L);
            summaryMapper.insert(summary);
        }
        return summary;
    }

    private Map<String, Object> buildCalendarData(Long userId, int year, int month, int retroactiveCards) {
        Map<String, Object> calendar = new HashMap<>();
        calendar.put("year", year);
        calendar.put("month", month);

        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // get all signed records for this month
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        java.sql.Date startSql = java.sql.Date.valueOf(startOfMonth);
        java.sql.Date endSql = java.sql.Date.valueOf(endOfMonth);

        LambdaQueryWrapper<ApCheckIn> recordsQuery = new LambdaQueryWrapper<>();
        recordsQuery.eq(ApCheckIn::getUserId, userId);
        recordsQuery.ge(ApCheckIn::getCheckInDate, startSql);
        recordsQuery.le(ApCheckIn::getCheckInDate, endSql);
        List<ApCheckIn> records = checkInMapper.selectList(recordsQuery);

        Set<LocalDate> signedDates = records.stream()
                .map(r -> {
                    java.sql.Date sqlDate = new java.sql.Date(r.getCheckInDate().getTime());
                    return sqlDate.toLocalDate();
                })
                .collect(Collectors.toSet());

        Map<LocalDate, Integer> rewardMap = new HashMap<>();
        for (ApCheckIn r : records) {
            java.sql.Date sqlDate = new java.sql.Date(r.getCheckInDate().getTime());
            rewardMap.put(sqlDate.toLocalDate(), r.getRewardPoints());
        }

        // get all sign_in_config for month
        LambdaQueryWrapper<SignInConfig> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(SignInConfig::getIsActive, 1);
        configQuery.le(SignInConfig::getDayOfMonth, daysInMonth);
        List<SignInConfig> configs = signInConfigMapper.selectList(configQuery);
        Map<Integer, SignInConfig> configMap = configs.stream()
                .collect(Collectors.toMap(SignInConfig::getDayOfMonth, c -> c, (a, b) -> a));

        // build days array
        List<Map<String, Object>> days = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(DATE_FORMATTER));

            String status;
            int reward = 0;
            String label = null;
            boolean canRetroactive = false;

            SignInConfig dayConfig = configMap.get(day);

            if (date.isAfter(today) && year == today.getYear() && month == today.getMonthValue()) {
                status = "FUTURE";
            } else if (signedDates.contains(date)) {
                status = "SIGNED";
                reward = rewardMap.getOrDefault(date, 0);
                label = dayConfig != null ? dayConfig.getExtraLabel() : null;
            } else if (date.isBefore(today) || (year < today.getYear()) || (year == today.getYear() && month < today.getMonthValue())) {
                status = "MISSED";
                canRetroactive = retroactiveCards > 0;
            } else {
                status = "NORMAL";
            }

            dayData.put("status", status);
            dayData.put("reward", reward);
            dayData.put("label", label);
            dayData.put("canRetroactive", canRetroactive);
            days.add(dayData);
        }

        calendar.put("days", days);
        return calendar;
    }

    private List<Map<String, Object>> buildTasksData(Long userId) {
        LambdaQueryWrapper<UserOnboardingTask> query = new LambdaQueryWrapper<>();
        query.eq(UserOnboardingTask::getUserId, userId);
        List<UserOnboardingTask> tasks = onboardingTaskMapper.selectList(query);

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (UserOnboardingTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", task.getId());
            item.put("name", getTaskTypeName(task.getTaskType()));
            item.put("description", task.getTaskType());
            item.put("reward", task.getRewardOre());
            item.put("icon", getTaskIcon(task.getTaskType()));
            item.put("completed", task.getStatus() != null && task.getStatus() >= 2);
            item.put("status", task.getStatus());
            taskList.add(item);
        }
        return taskList;
    }

    private String getTaskTypeName(String taskType) {
        switch (taskType) {
            case "PUBLISH_ARTICLE":
                return "首次成功发布文章（>400字）";
            case "PUBLISH_BOOLEAN":
                return "首次成功发布沸点";
            default:
                return taskType;
        }
    }

    private String getTaskIcon(String taskType) {
        switch (taskType) {
            case "PUBLISH_ARTICLE":
                return "📝";
            case "PUBLISH_BOOLEAN":
                return "💧";
            default:
                return "";
        }
    }
}