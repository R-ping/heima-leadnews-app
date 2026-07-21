package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import org.springframework.amqp.rabbit.connection.CorrelationData;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     */
    public void buildHTMLAndSend(ApArticle apArticle,String content, long lastTime);//, Integer wmNewsId
    public void sendArticleVo2Mq(SearchArticleVo vo);
    public CorrelationData getCorrelationData(SearchArticleVo vo,boolean retry);
    public void sendMsg2Mq(String routingKey, SearchArticleVo vo, CorrelationData minioEs);
}
