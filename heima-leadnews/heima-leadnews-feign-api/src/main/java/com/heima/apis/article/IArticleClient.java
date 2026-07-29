package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.common.dtos.ResponseResult;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {

    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto, @RequestParam("executeTime") long executeTime);

    @PostMapping("/api/v1/article/event")
    public void eventUpdate(@RequestBody ArticleEvent event);

    @GetMapping("/api/v1/article/content")
    public ResponseResult getContent(@RequestParam("articleId") Long articleId);

    @GetMapping("/api/v1/article/info")
    public ResponseResult getArticleInfo(@RequestParam("articleId") Long articleId);
    @PostMapping("/api/v1/article/generate/event")
    public boolean generateArticleEvent(@RequestBody ApArticle article, @RequestParam("lastExecuteInterval") long executeTimeInterval);
    @PostMapping("/api/v1/article/publish")
    public ResponseResult publishArticle(@RequestParam("articleId") Long articleId);

    @PostMapping("/api/v1/article/list")
    public List<Map<String, Object>> listByAuthorId(@RequestBody ArticleDto dto);

    @GetMapping("/api/v1/article/feign/statistics")
    public ResponseResult getStatisticsFeign(@RequestParam("userId") Long userId);
}
