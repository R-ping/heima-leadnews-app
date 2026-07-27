package com.heima.schedule.feign;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScheduleClient implements IScheduleClient {

    /**
     * 前端提交任务--》 1、保存到数据库 2、判断时间发送延迟消息
     */

    @PostMapping("/api/v1/taskDelay/add")
    public ResponseResult addTaskDelayMsg(@RequestBody Task task) {
//        taskService.addDelayTask(task);
        return null;
    }


    @Autowired
    private TaskService taskService;

    /**
     * 添加延迟任务
     */
    @PostMapping("/api/v1/task/add")
    public void addTask(@RequestBody Task task) {
        taskService.addTask(task);

    }
}
