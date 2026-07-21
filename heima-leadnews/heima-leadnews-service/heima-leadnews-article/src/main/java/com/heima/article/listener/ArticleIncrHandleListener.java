package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleService;
import com.heima.model.mess.ArticleVisitStreamMess;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArticleIncrHandleListener {

    @Autowired
    private ApArticleService apArticleService;

    @KafkaListener(topics = "hot.article.incr.handle.topic")
    public void onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            log.info("监听到的消息是:{}", message);
            ArticleVisitStreamMess articleVisitStreamMess = JSON.parseObject(message, ArticleVisitStreamMess.class);
            apArticleService.updateScore(articleVisitStreamMess);

        }
    }

}
