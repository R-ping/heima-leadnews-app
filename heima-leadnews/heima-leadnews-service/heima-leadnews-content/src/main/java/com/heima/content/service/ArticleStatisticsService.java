package com.heima.content.service;

import com.heima.model.common.dtos.ResponseResult;

public interface ArticleStatisticsService {

    ResponseResult getUserStatistics(Long userId);
}
