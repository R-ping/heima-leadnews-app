package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.article.service.ApArticleEventService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import java.util.ArrayList;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ApArticleEventServiceImpl implements ApArticleEventService {

    @Autowired
    private ApArticleEventMapper apArticleEventMapper;

    @Override
    public void updateEvent(ArticleEvent event) {
        apArticleEventMapper.updateArticleEvent(event);
    }

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 20000)
    public void processEvent() {
        ArrayList<Long> success_list = new ArrayList<>();
        for (ArticleEvent event : apArticleEventMapper.loadArticleEvent()) {
            if (event.getSendStatus() == 2) {
                if (event.getMinioStatus() == 2 && event.getEsStatus() == 2) {
                    success_list.add(event.getArticleId());
                    continue;
                }
                long retry_time = event.getRetryTime().getTime();
                long expect_time = System.currentTimeMillis() - 5000;
                boolean isBackward = retry_time <= expect_time;
                if (event.getEsStatus() == 1 && isBackward) {
                    SearchArticleVo searchArticleVo = JSON.parseObject(event.getParameter(), SearchArticleVo.class);
                    CorrelationData esCorrelationData = articleFreemarkerService.getCorrelationData(searchArticleVo, true);
                    articleFreemarkerService.sendMsg2Mq("article.resend.es",searchArticleVo,esCorrelationData);
                }
                if (event.getMinioStatus() == 1 && isBackward) {
                    SearchArticleVo searchArticleVo = JSON.parseObject(event.getParameter(), SearchArticleVo.class);
                    CorrelationData minioCorrelationData = articleFreemarkerService.getCorrelationData(searchArticleVo, true);
                    articleFreemarkerService.sendMsg2Mq("article.minio.resend",searchArticleVo,minioCorrelationData);
                }
            } else if (event.getSendStatus() == 1 && event.getRetryCount() == 2) {
                rabbitTemplate.convertAndSend("error.direct", "error", event.getParameter());
            } else { //event.getSendStatus() == 1 && event.getRetryCount() == 1，生产者投递失败
                SearchArticleVo searchArticleVo = JSON.parseObject(event.getParameter(), SearchArticleVo.class);
                articleFreemarkerService.sendArticleVo2Mq(searchArticleVo);
            }
        }
        apArticleEventMapper.deleteArticleEvent(success_list);
    }

    /**
     * 定时任务处理文章事件，每20s 处理一次，本地事务状态与minio、es状态共用一个reCt 逻辑：每次拿select *，在业务层分类 一、status=1 reCt=2--->死信交换机
     * reCt<2--->重新投递，利用反射 二、status=2 reTm=null 不管
     *
     * reTm=xx reTm<=now()+10s esStatus=1 or minioStatus=1 重试
     *
     * esStatus=2 and minioStatus=2 成功，删除
     */
    // 弃用
//    @Scheduled(fixedDelay = 20000)
    public void processEvent2() {

//        ArrayList<ArticleEvent> es_retry_list = new ArrayList<>();
//        ArrayList<ArticleEvent> minio_retry_list = new ArrayList<>();
        ArrayList<Long> success_list = new ArrayList<>();

        for (ArticleEvent event : apArticleEventMapper.loadArticleEvent()) {
            if (event.getSendStatus() == 2) {
                if (event.getMinioStatus() == 2 && event.getEsStatus() == 2) {
                    success_list.add(event.getArticleId());
                    // 跳过当前循环
                    continue;
                }
                long retry_time = event.getRetryTime().getTime();
                long expect_time = System.currentTimeMillis() - 5000;
                boolean isBackward = retry_time <= expect_time;
                if (event.getEsStatus() == 1 && isBackward) {
//                    es_retry_list.add(event);
                }
                if (event.getMinioStatus() == 1 && isBackward) {
//                    minio_retry_list.add(event);
                }
            } else if (event.getSendStatus() == 1 && event.getRetryCount() == 2) {
                rabbitTemplate.convertAndSend("error.direct", "error", event.getParameter());
            } else { //event.getSendStatus() == 1 && event.getRetryCount() == 1，生产者投递失败
                // TODO 利用反射，拿到ArticleFreemarkerServiceImpl的代理，调用objectSendMsg2Mq方法，重新投递;searchArticleVo = JSON.parseObject(event.getParameter(), SearchArticleVo.class);
            }
        }

        apArticleEventMapper.deleteArticleEvent(success_list);
    }


}
