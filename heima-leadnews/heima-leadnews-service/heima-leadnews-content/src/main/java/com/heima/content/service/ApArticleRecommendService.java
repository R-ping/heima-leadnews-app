package com.heima.content.service;

import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleRecommendService {
    ResponseResult recommend(ArticleRecommendDto dto);
}