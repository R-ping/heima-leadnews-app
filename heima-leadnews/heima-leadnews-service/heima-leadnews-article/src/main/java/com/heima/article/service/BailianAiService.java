package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

import java.util.Map;

public interface BailianAiService {

    /**
     * 对文章进行AI多维分析
     * @param article 文章对象
     * @param content 文章内容
     * @return 分析结果Map，包含各维度评分
     */
    Map<String, Object> analyzeArticle(ApArticle article, String content);
}