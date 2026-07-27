package com.heima.schedule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.service.TaskService;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    CacheService cacheService;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    /**
     * 添加延迟任务
     */
    @Override
    public void addTask(Task task) {
        //1.添加任务到数据库中
        boolean success = addTaskToDb(task);
        if (success) {
            // 发送延迟消息
            sendTaskDelayMsg(task, null);
        }
    }

    private void sendTaskDelayMsg(Task task, Long nowTime) {
        long executeTime = task.getExecuteTime().getTime();
        nowTime = nowTime == null ? System.currentTimeMillis() : nowTime;
        // 得到1个小时后的时间
        long oneHourInterval = 60 * 60 * 1000;
        long delay = 0;
        if (executeTime <= nowTime) {
            delay = 0;
        } else if (executeTime <= nowTime + oneHourInterval) {
            delay = task.getFirstExecInterval();
        }
        final long finalDelay = delay;
        // 使用RabbitMQ延迟插件发送延迟消息
        String taskJson = JSON.toJSONString(task);
        rabbitTemplate.convertAndSend("delay.exchange", "task.delay", taskJson,
            message -> {
                message.getMessageProperties().setDelayLong(finalDelay);
                return message;
            });
        log.info("RabbitMQ延迟消息已发送，taskId={}, delay={}ms", task.getTaskId(), delay);
    }

    // 半个小时执行一次
    @Scheduled(cron = "0 0/30 * * * ?")
    public void refreshTaskToRedis() {
        // 当前时间
        long nowTime = System.currentTimeMillis();
        // 未来一个小时
        Date nextHour = new Date(nowTime + 60 * 60 * 1000);
        taskinfoLogsMapper.selectGoal(nextHour).forEach(taskinfoLogs -> {
            Task task = BeanUtil.copyProperties(taskinfoLogs, Task.class);
            sendTaskDelayMsg(task, nowTime);
        });
    }

    /**
     * 添加任务到数据库中
     */
    private boolean addTaskToDb(Task task) {
        boolean flag = false;
        try {
            //保存任务日志数据
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(task, taskinfoLogs);
            taskinfoLogs.setStatus(ScheduleConstants.EXECUTED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            //设置taskID
            task.setTaskId(taskinfoLogs.getTaskId());
            flag = true;
        } catch (Exception e) {

        }
        return flag;
    }

    /**
     * 更新任务日志
     */
    private Task updateDb(long taskId, int status) {

        Task task = null;
        try {
            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            task = BeanUtil.copyProperties(taskinfoLogs, Task.class);
            log.info("taskInfoLogs update success taskId={}, status={}", taskId, status);
        } catch (Exception e) {
            log.error("task cancel exception taskId={}", taskId);
        }

        return task;
    }

    /**
     * 消费任务
     */
    public void consumerTask(Long taskId) {
        try {
            updateDb(taskId, ScheduleConstants.EXECUTED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void failTask(Long taskId) {
        try {
            updateDb(taskId, ScheduleConstants.FAIL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
