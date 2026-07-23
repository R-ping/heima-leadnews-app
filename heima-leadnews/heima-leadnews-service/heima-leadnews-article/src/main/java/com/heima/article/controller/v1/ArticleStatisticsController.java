package com.heima.article.controller.v1;

import com.heima.article.service.ArticleStatisticsService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article/statistics")
public class ArticleStatisticsController {

    @Autowired
    private ArticleStatisticsService articleStatisticsService;

    @GetMapping
    public ResponseResult getStatistics(@RequestParam("userId") Long userId) {
        return articleStatisticsService.getUserStatistics(userId);
    }
}
