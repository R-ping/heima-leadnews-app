package com.heima.content.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     */
    public void buildHTMLAndSend(ApArticle apArticle,String content, long lastTime);
}
