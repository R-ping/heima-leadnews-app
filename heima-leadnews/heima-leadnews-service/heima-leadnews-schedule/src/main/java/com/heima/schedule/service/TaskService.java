package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

public interface TaskService {


    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    void addTask(Task task);

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    void cancelTask(long taskId);


    /**
     * 添加延迟任务
     * @param task
     * @return
     */

    /**
     * 消费任务
     */
    void consumerTask(Long taskId);

    void updateTask(Task task);

    void failTask(Long taskId);
}
