package com.heima.article.service;

import java.util.Map;

public interface CheckInService {

    /**
     * 执行签到
     * @param userId 用户ID
     * @return 签到结果，包含 success, rewardPoints, consecutiveDays, totalDays
     */
    Map<String, Object> doCheckIn(Long userId);

    /**
     * 获取月度签到记录
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     * @return 包含签到日期列表的 map
     */
    Map<String, Object> getCheckInRecords(Long userId, Integer year, Integer month);

    /**
     * 获取签到统计
     * @param userId 用户ID
     * @return 统计信息，包含 consecutiveDays, totalDays, totalPoints
     */
    Map<String, Object> getCheckInStats(Long userId);

    Map<String, Object> getCheckInTasks(Long userId);
}