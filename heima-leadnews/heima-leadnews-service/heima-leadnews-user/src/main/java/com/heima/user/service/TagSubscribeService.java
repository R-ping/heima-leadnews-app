package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;

public interface TagSubscribeService {
    ResponseResult discover(String sort, String keyword, Integer page, Integer size);
    ResponseResult getFollowed();
    ResponseResult follow(Integer tagId);
    ResponseResult unfollow(Integer tagId);
}