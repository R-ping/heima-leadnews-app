package com.heima.content.service;

import com.heima.model.common.dtos.ResponseResult;

public interface ChannelService {

    /**
     * 查询所有频道
     * @return
     */
    ResponseResult findAll();
}