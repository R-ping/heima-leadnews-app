package com.heima.content.controller.v1;

import com.heima.common.annotation.RateLimit;
import com.heima.content.service.ApArticleRecommendService;
import com.heima.content.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Autowired
    private ApArticleService apArticleService;

    @Autowired
    private ApArticleRecommendService apArticleRecommendService;

    /**
     * 加载首页
     * @param dto
     * @return
     */
    @PostMapping("/load/")
    public ResponseResult load(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载更多
     * @param dto
     * @return
     */
    @PostMapping("/load/more")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    /**
     * 加载最新
     * @param dto
     * @return
     */
    @PostMapping("/load/new")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto dto){
        return apArticleService.load(dto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }

    /**
     * 推荐文章（非确定性排序，基于种子随机洗牌）
     */
    @PostMapping("/recommend")
    @RateLimit(dimension = RateLimit.Dimension.GLOBAL, count = 500, interval = 1, timeUnit = RateLimit.TimeUnit.MINUTES)
    @RateLimit(dimension = RateLimit.Dimension.IP, count = 30, interval = 1, timeUnit = RateLimit.TimeUnit.MINUTES)
    public ResponseResult recommend(@RequestBody ArticleRecommendDto dto) {
        return apArticleRecommendService.recommend(dto);
    }
}
