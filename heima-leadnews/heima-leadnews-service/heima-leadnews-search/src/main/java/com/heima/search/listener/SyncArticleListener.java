package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

//    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
//    public void onMessage(String message){
//        if(StringUtils.isNotBlank(message)){
//
//            log.info("SyncArticleListener,message={}",message);
//
//            SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);
//            IndexRequest indexRequest = new IndexRequest("app_info_article");
//            indexRequest.id(searchArticleVo.getId().toString());
//            indexRequest.source(message, XContentType.JSON);
//            try {
//                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//            } catch (IOException e) {
//                e.printStackTrace();
//                log.error("sync es error={}",e);
//            }
//        }
//
//    }

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "es.queue",durable = "true"),
        exchange = @Exchange(value = "article.exchange"),
        key = "article.*.es"
    ))
    public void onMessage(Message message,SearchArticleVo vo, Channel channel){
        if(vo!=null){
            String articleId=null;
            Map<String,String> resultMap=new HashMap<>();
            resultMap.put("type","minio");
            try {
                articleId = vo.getId().toString();
                resultMap.put("articleId",articleId);
                ResponseResult responseResult = articleClient.getContent(Long.valueOf(articleId));
                String content = responseResult.getData().toString();
                vo.setContent(content);
                String msg = JSON.toJSONString(vo);
                IndexRequest indexRequest = new IndexRequest("app_info_article");
                indexRequest.id(articleId);
                indexRequest.source(msg, XContentType.JSON);
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                // 手动ack
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                // 索引成功
                resultMap.put("status", "success");
                rabbitTemplate.convertAndSend("process.exchange","process.result",resultMap);
            } catch (IOException e) {
                log.error("sync es error=", e);
                resultMap.put("status", "fail");
                resultMap.put("articleId",articleId);
                rabbitTemplate.convertAndSend("process.exchange","process.result",resultMap);
            } catch (Exception e) {
                log.error("Unexpected error when syncing article to ES", e);
            }
        }
    }

  /*  public void onMessage2(String message){
        if(StringUtils.isNotBlank(message)){

            try {
                SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);
                String articleId = searchArticleVo.getId().toString();
                String content = articleClient.getContent(Long.valueOf(articleId));
                searchArticleVo.setContent(content);
                message = JSON.toJSONString(searchArticleVo);
                IndexRequest indexRequest = new IndexRequest("app_info_article");
                indexRequest.id(articleId);
                indexRequest.source(message, XContentType.JSON);
                // 先查询ES中是否已存在该文档
//                GetRequest getRequest = new GetRequest("app_info_article", articleId);
//                boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
//                if (!exists) {
//                    // 文档不存在时才执行index操作
//                    restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//                    log.info("ES文档不存在，已执行index操作，id: {}", articleId);
//                } else {
//                    log.info("ES文档已存在，跳过index操作，id: {}", articleId);
//                }
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                // 后续Feign调用及重试逻辑（同上）
//                boolean feignSuccess = false;
//                int retryCount = 0;
//                while (!feignSuccess) {  // 限制重试次数
//                    try {
//                        ArticleEvent event = new ArticleEvent();
//                        event.setArticleId(searchArticleVo.getId());
//                        event.setEsStatus((byte) 2);
//                        articleClient.eventUpdate(event);
//                        feignSuccess = true;
//                    } catch (Exception e) {
//                        log.error("Feign调用失败，第{}次重试", retryCount, e);
//                        if (retryCount >= 1) {
//                            throw new RuntimeException("Feign调用多次失败", e);
//                        }
//                        retryCount++;
//                        Thread.sleep(200);
//                    }
//                }
                rabbitTemplate.convertAndSend("es.success",articleId);
            } catch (IOException e) {
                log.error("sync es error= {}", e);
                throw new RuntimeException("Failed to sync article to ES", e);
            } catch (Exception e) {
                log.error("Unexpected error when syncing article to ES", e);
                throw new RuntimeException("Unexpected error when syncing article to ES", e);
            }
        }
    }*/
}
