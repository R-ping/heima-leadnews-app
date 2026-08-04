package com.heima.content.service.article;

import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleRecommendService {
    ResponseResult recommend(ArticleRecommendDto dto);
}