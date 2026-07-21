package com.heima.article.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleContentService;
import com.heima.article.service.ApArticleEventService;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleTaskService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;
    @Autowired
    private ApArticleContentService apArticleContentService;

    @Autowired
    private ApArticleEventService apArticleEventService;

    @Autowired
    private ArticleTaskService articleTaskService;

    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto, @RequestParam("executeTime") long executeTime) {
        return apArticleService.saveArticle(dto, executeTime);
    }

    @PostMapping("/api/v1/article/event")
    public void eventUpdate(@RequestBody ArticleEvent event) {
        apArticleEventService.updateEvent(event);
    }

    @GetMapping("/api/v1/article/content")
    public ResponseResult getContent(@RequestParam("articleId") Long articleId) {
        ApArticleContent articleContent = apArticleContentService.getOne(
            new QueryWrapper<ApArticleContent>().eq("article_id", articleId));
        if (articleContent == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.okResult(articleContent.getContent());
    }

    @GetMapping("/api/v1/article/info")
    public ResponseResult getArticleInfo(@RequestParam("articleId") Long articleId) {
        ApArticle apArticle = apArticleService.getById(articleId);
        if (apArticle == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.okResult(apArticle);
    }

    @PostMapping("/api/v1/article/publish")
    public ResponseResult publishArticle(@RequestParam("articleId") Long articleId) {
        articleTaskService.publishArticle(articleId);
        return ResponseResult.okResult();
    }
}
