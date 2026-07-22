package com.heima.article.service;

import com.heima.model.common.dtos.ResponseResult;

import java.util.Map;

public interface BrowseHistoryService {

    /**
     * 分页查询浏览记录，按日期分组
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 按日期分组的数据
     */
    ResponseResult getHistoryList(Long userId, Integer page, Integer size, String keyword);

    /**
     * 清空用户所有浏览记录（软删除）
     * @param userId 用户ID
     */
    void clearHistory(Long userId);
}