package com.heima.article.controller.v1;

import com.heima.article.service.CheckInService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @GetMapping("/do")
    public ResponseResult doCheckIn() {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = Long.valueOf(user.getId());
        Map<String, Object> result = checkInService.doCheckIn(userId);
        return ResponseResult.okResult(result);
    }

    @GetMapping("/records")
    public ResponseResult getCheckInRecords(@RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer month) {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = Long.valueOf(user.getId());

        if (year == null || month == null) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
        }

        Map<String, Object> result = checkInService.getCheckInRecords(userId, year, month);
        return ResponseResult.okResult(result);
    }

    @GetMapping("/stats")
    public ResponseResult getCheckInStats() {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = Long.valueOf(user.getId());
        Map<String, Object> result = checkInService.getCheckInStats(userId);
        return ResponseResult.okResult(result);
    }

    @GetMapping("/tasks")
    public ResponseResult getCheckInTasks() {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = Long.valueOf(user.getId());
        Map<String, Object> result = checkInService.getCheckInTasks(userId);
        return ResponseResult.okResult(result);
    }
}