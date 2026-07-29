package com.heima.apis.article.fallback;

import com.heima.apis.article.IFollowClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IFollowClientFallback implements IFollowClient {

    @Override
    public ResponseResult follow(Long userId, Long followUserId) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "关注服务不可用");
    }
}