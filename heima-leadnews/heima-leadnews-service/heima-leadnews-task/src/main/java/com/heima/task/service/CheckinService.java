package com.heima.task.service;

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
}