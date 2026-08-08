package com.heima.content.service.article;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     */
//    public void buildHTMLAndSend(ApArticle apArticle,String content, long lastTime);
    public void buildHTMLAndSend(ApArticle apArticle, String content, Task task,long lastTime);
}