package com.heima.file.config;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry",name = "enabled",havingValue = "true")
public class ErrorConfiguration {

    @Autowired
    private IArticleClient articleClient;

    @Bean
    public DirectExchange errorExchange(){
        return new DirectExchange("error.direct");
    }

    @Bean
    public Queue errorQueue(){
        return new Queue("error.queue");
    }

    @Bean
    public Binding errorBinding(){
        return BindingBuilder.bind(errorQueue()).to(errorExchange()).with("error");
    }

//    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){

        return new RepublishMessageRecoverer(rabbitTemplate,"error.direct","error"){
            @Override
            public void recover(Message message, Throwable cause) {
                String messageBody = new String(message.getBody());
                SearchArticleVo searchArticleVo = JSON.parseObject(messageBody, SearchArticleVo.class);
                Long articleId = searchArticleVo.getId();
                ArticleEvent event = new ArticleEvent();
                event.setArticleId(articleId);
                event.setSendStatus((byte) 1);
                event.setRetryCount((byte) 2);
                articleClient.eventUpdate(event);

                // 调用父类方法，将消息发送到死信队列
                super.recover(message, cause);
            }
        };
    }

}
