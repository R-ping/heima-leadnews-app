package com.heima.article.schedule.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.feign.ArticleClient;
import com.heima.article.schedule.service.TaskService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;
import com.heima.utils.common.ProtostuffUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Redisson延迟任务消费者，替代RabbitMQ延迟队列消费者
 * 监听TASK_DELAY_QUEUE队列，消费延迟消息后执行文章发布流程
 */
@Component
@Slf4j
public class TaskDelayConsumer {

    @Autowired
    private ArticleClient articleClient;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue("TASK_DELAY_QUEUE");
            log.info("Redisson延迟任务消费者启动，等待消费...");
            while (true) {
                try {
                    String taskJson = blockingQueue.take();
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
                } catch (Exception e) {
                    log.error("消费延迟任务失败", e);
                }
            }
        }, "redisson-delay-consumer").start();
    }
}