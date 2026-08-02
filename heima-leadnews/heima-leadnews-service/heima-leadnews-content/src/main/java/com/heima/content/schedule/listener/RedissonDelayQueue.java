package com.heima.schedule.config;


import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.ProtostuffUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedissonDelayQueue {

    @Autowired
    private RedissonClient redissonClient;
    // 延迟队列
    private RDelayedQueue<String> delayedQueue;
    //阻塞队列
    private RBlockingQueue<String> blockingQueue;

    private final ExecutorService executorService;

    public RedissonDelayQueue() {
        executorService = new ThreadPoolExecutor(
            10,
            20,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(20)
        );
    }

    @PostConstruct
    public void init() {
        blockingQueue = redissonClient.getBlockingQueue("myQueue");
        delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        startConsumer();
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down RedissonDelayQueue...");
        executorService.shutdownNow(); // 尝试中断所有任务

        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Some tasks did not terminate in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown(); // 关闭 Redisson 客户端
        }
    }

    @Autowired
    @Lazy
    private TaskService taskService;

    @Lazy
    @Autowired
    private IWemediaClient wemediaClient;

    private void startConsumer() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String take = blockingQueue.take();
                    Task task = JSON.parseObject(take, Task.class);
                    WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
                    boolean success = wemediaClient.fromWmNewsToArticle(wmNews.getId(),task.getExecuteTime());
                    //改成发布状态
                    if (success) {
                        taskService.consumerTask(task.getTaskId());
                    }else {
                        taskService.failTask(task.getTaskId());
                    }
                    log.info("consume task success:{}", task.getTaskId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("error:{}", e);
                }
            }
        });
    }

    public void addTask(String task, long delay) {
        delayedQueue.offer(task, delay, TimeUnit.MILLISECONDS);
    }

}
