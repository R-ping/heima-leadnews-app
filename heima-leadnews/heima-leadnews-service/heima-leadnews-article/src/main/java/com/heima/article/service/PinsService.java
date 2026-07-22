package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;

public interface PinsService extends IService<ApPins> {

    ResponseResult list(Long authorId, Integer page, Integer size, String status);

    ResponseResult statistics(Long authorId);

    ResponseResult createPins(ApPins pins);

    ResponseResult deletePins(Long id);
}
