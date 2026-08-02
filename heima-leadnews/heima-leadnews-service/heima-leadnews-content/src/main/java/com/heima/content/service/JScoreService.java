package com.heima.content.service;

import com.heima.model.common.dtos.ResponseResult;

public interface JScoreService {
    ResponseResult getOverview(Long userId);
    ResponseResult getDetail(Long userId, String category, String cursor, Integer size);
}