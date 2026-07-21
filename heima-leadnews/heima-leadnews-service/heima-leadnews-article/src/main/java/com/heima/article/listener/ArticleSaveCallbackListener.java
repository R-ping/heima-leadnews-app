package com.heima.article.listener;

import com.heima.article.feign.ArticleClient;
import com.heima.model.article.pojos.ArticleEvent;
import com.rabbitmq.client.Channel;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArticleSaveCallbackListener {

    @Autowired
    private ArticleClient articleClient;
    // 监听存储、搜索服务的回调信息
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "article.save.callback.queue",durable = "true"),
        exchange = @Exchange(value = "es.process.exchange"),
        key = "es.process.result"
    ))
    public void onMessage(Message message, Map<String, String> map, Channel channel) {
        String articleId = map.get("articleId");
        String status = map.get("status");
        String type = map.get("type");
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(Long.valueOf(articleId));
        if (status.equals("success")) {
            if (Objects.equals(type, "minio")){
                event.setEsStatus((byte) 2);
            }else if (Objects.equals(type, "es")){
                event.setMinioStatus((byte) 2);
            }
        } else if (status.equals("fail")) {
            if (Objects.equals(type, "minio")){
                event.setEsStatus((byte) 1);
            }else{
                event.setMinioStatus((byte) 1);
            }
        }
        articleClient.eventUpdate(event);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("ack failed, message: {}", message, e);
        }
    }

}
