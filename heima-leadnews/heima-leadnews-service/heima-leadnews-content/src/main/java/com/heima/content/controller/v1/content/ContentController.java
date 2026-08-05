package com.heima.content.controller.v1.content;

import com.heima.content.service.article.ApArticleRecommendService;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 内容聚合控制器 — 提供统一的 /api/v1/content/* 路径
 * 用于兼容前端通过 /content/ 前缀调用的各种接口
 */
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    @Autowired
    private ApArticleRecommendService apArticleRecommendService;

    /**
     * 推荐文章（兼容前端 /content/api/v1/content/recommend 调用）
     */
    @PostMapping("/recommend")
    public ResponseResult recommend(@RequestBody ArticleRecommendDto dto) {
        return apArticleRecommendService.recommend(dto);
    }
}