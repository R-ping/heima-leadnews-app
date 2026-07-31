package com.heima.user.service.impl;

import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.UserStatisticsService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        ResponseResult feignResult = articleClient.getStatisticsFeign(currentUser.getId().longValue());

        // 添加创作天数
        if (feignResult != null && feignResult.getData() instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) feignResult.getData();
            if (currentUser.getCreatedTime() != null) {
                long diff = System.currentTimeMillis() - currentUser.getCreatedTime().getTime();
                long createDays = TimeUnit.MILLISECONDS.toDays(diff);
                data.put("createDays", Math.max(createDays, 1));
            } else {
                data.put("createDays", 1);
            }
        }

        return feignResult;
    }
}
