package com.heima.schedule.listener;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.ProtostuffUtil;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ延迟任务消费者，替代RedissonDelayQueue的消费者线程
 * 监听delay.exchange的task.delay路由键，消费延迟消息后执行文章发布流程
 */
@Component
@Slf4j
public class TaskDelayConsumer {

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private TaskService taskService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "task.delay.queue", durable = "true"),
        exchange = @Exchange(value = "delay.exchange", type = "x-delayed-message",
            arguments = @org.springframework.amqp.rabbit.annotation.Argument(
                name = "x-delayed-type", value = "direct")),
        key = "task.delay"
    ))
    public void onMessage(Message message, Channel channel) {
        try {
            String taskJson = new String(message.getBody());
            Task task = JSON.parseObject(taskJson, Task.class);
            ApArticle article = ProtostuffUtil.deserialize(task.getParameters(), ApArticle.class);
            log.info("消费延迟任务，taskId={}, articleId={}", task.getTaskId(), article.getId());
            // 文章服务，本地消息表+mq方案，article-->minio、es
            long lastExecInterval = task.getObjExecInterval() - task.getFirstExecInterval();
            boolean isArticleEvenBuilt = articleClient.generateArticleEvent(article, lastExecInterval);
            if (isArticleEvenBuilt) {
                taskService.consumerTask(task.getTaskId());
            } else {
                taskService.failTask(task.getTaskId());
            }
            log.info("延迟任务消费成功，taskId={}", task.getTaskId());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("消费延迟任务失败", e);
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception ex) {
                log.error("ack失败", ex);
            }
        }
    }
}