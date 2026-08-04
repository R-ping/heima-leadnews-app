package com.heima.reward.service;

import com.heima.model.common.dtos.ResponseResult;

public interface CheckinService {
    /** 获取签到首页数据 */
    ResponseResult getDashboard(Long userId);
    /** 执行签到 */
    ResponseResult doCheckin(Long userId);
    /** 使用补签卡 */
    ResponseResult patchCheckin(Long userId, String targetDate);
    /** 获取签到进度 */
    ResponseResult getMilestone(Long userId);
    /** 获取今日签到状态（用于侧边栏） */
    ResponseResult getTodayStatus(Long userId);
}
