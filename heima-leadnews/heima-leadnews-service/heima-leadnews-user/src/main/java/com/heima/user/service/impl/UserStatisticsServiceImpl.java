package com.heima.user.service.impl;

import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.UserStatisticsService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {

    @Autowired
    private IArticleClient articleClient;

    @Override
    public ResponseResult getUserStatistics() {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.okResult(new java.util.HashMap<>());
        }

        // 通过 Feign 远程调用 article 服务获取统计数据
        return articleClient.getStatistics(currentUser.getId().longValue());
    }
}
