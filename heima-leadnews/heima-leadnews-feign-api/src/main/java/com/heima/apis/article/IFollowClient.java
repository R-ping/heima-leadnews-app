package com.heima.apis.article;

import com.heima.apis.article.fallback.IFollowClientFallback;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "leadnews-article", contextId = "leadnews-article-followClient", fallback = IFollowClientFallback.class)
public interface IFollowClient {

    @PostMapping("/api/v1/follow/do")
    public ResponseResult follow(@RequestParam("userId") Long userId, @RequestParam("followUserId") Long followUserId);
}