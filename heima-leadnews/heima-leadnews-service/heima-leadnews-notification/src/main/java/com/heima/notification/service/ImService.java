package com.heima.notification.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.notification.dtos.ImMessageDto;
import com.heima.model.notification.dtos.ImReadDto;

public interface ImService {

    ResponseResult listSessions(Long userId);

    ResponseResult listMessages(Long userId, Long sessionId, Long cursor, Integer size);

    ResponseResult sendMessage(Long senderId, ImMessageDto dto);

    ResponseResult markRead(Long userId, ImReadDto dto);
}