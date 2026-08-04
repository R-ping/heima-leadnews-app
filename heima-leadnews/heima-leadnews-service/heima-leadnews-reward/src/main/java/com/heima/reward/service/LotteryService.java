package com.heima.reward.service;

import com.heima.model.common.dtos.ResponseResult;

import java.util.Map;

public interface LotteryService {
    /** 获取抽奖页面数据 */
    ResponseResult getDashboard(Long userId);
    /** 执行抽奖 */
    ResponseResult draw(Long userId, String type, Boolean useFree);
    /** 领取实物奖品（填写地址） */
    ResponseResult claimPhysical(Long userId, Map<String, Object> body);
    /** 获取我的收获列表 */
    ResponseResult getMyPrizes(Long userId, Integer page, Integer size, String type);
    /** 获取中奖播报 */
    ResponseResult getBroadcast();
}
