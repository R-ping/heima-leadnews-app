package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCheckInMapper;
import com.heima.article.mapper.SignInConfigMapper;
import com.heima.model.article.pojos.ApCheckIn;
import com.heima.model.article.pojos.SignInConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签到日历数据构建器，从 CheckInServiceImpl 中提取以降低类复杂度
 */
@Component
public class CheckInCalendarBuilder {

    @Autowired
    private ApCheckInMapper checkInMapper;

    @Autowired
    private SignInConfigMapper signInConfigMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, Object> buildCalendarData(Long userId, int year, int month, int retroactiveCards) {
        Map<String, Object> calendar = new HashMap<>();
        calendar.put("year", year);
        calendar.put("month", month);

        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

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
                .map(r -> new java.sql.Date(r.getCheckInDate().getTime()).toLocalDate())
                .collect(Collectors.toSet());

        Map<LocalDate, Integer> rewardMap = new HashMap<>();
        for (ApCheckIn r : records) {
            rewardMap.put(new java.sql.Date(r.getCheckInDate().getTime()).toLocalDate(), r.getRewardPoints());
        }

        LambdaQueryWrapper<SignInConfig> configQuery = new LambdaQueryWrapper<>();
        configQuery.eq(SignInConfig::getIsActive, 1);
        configQuery.le(SignInConfig::getDayOfMonth, daysInMonth);
        List<SignInConfig> configs = signInConfigMapper.selectList(configQuery);
        Map<Integer, SignInConfig> configMap = configs.stream()
                .collect(Collectors.toMap(SignInConfig::getDayOfMonth, c -> c, (a, b) -> a));

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
}