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

    /**
     * 获取签到仪表盘聚合数据
     * @param userId 用户ID
     * @return 包含用户信息、今日状态、统计、补签卡、日历、任务等聚合数据
     */
    Map<String, Object> getDashboard(Long userId);

    /**
     * 补签
     * @param userId 用户ID
     * @param missedDate 补签日期 (yyyy-MM-dd)
     * @return 补签结果
     */
    Map<String, Object> doRetroactive(Long userId, String missedDate);

    /**
     * 获取今日签到状态（用于首页右侧栏签到入口）
     * @param userId 用户ID，null 表示未登录
     * @return 包含 isSignedIn, consecutiveDays, totalOre 的 map
     */
    Map<String, Object> getTodayStatus(Long userId);
}