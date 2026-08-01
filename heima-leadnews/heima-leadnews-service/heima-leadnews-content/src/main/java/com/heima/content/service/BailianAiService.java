package com.heima.content.service;

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

    /**
     * 对文章进行AI违规内容检测
     * @param article 文章对象
     * @param content 文章内容
     * @return 检测结果Map，包含: success(Boolean), is_violation(Boolean), violation_type(String), violation_reason(String)
     */
    Map<String, Object> checkViolation(ApArticle article, String content);
}