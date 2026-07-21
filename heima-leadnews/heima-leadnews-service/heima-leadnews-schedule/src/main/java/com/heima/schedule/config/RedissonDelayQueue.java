package com.heima.schedule.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Redisson延迟队列已废弃，延迟任务功能已迁移至RabbitMQ延迟插件
 * 参见: com.heima.schedule.listener.TaskDelayConsumer
 * 参见: com.heima.schedule.service.impl.TaskServiceImpl.sendTaskDelayMsg()
 */
@Component
@Slf4j
@Deprecated
public class RedissonDelayQueue {

    // 延迟任务功能已迁移至RabbitMQ延迟插件，此组件保留兼容不做清理

}
