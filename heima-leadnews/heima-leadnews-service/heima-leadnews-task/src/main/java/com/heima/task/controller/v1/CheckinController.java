package com.heima.task.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.task.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    /** 获取签到首页数据 */
    @GetMapping("/dashboard")
    public ResponseResult dashboard(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L; // 临时调试
        return checkinService.getDashboard(userId);
    }

    /** 执行签到 */
    @PostMapping("/do")
    public ResponseResult doCheckin(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return checkinService.doCheckin(userId);
    }

    /** 使用补签卡 */
    @PostMapping("/patch")
    public ResponseResult patchCheckin(@RequestHeader(value = "userId", required = false) Long userId,
                                        @RequestBody Map<String, String> body) {
        if (userId == null) userId = 1L;
        String targetDate = body.get("targetDate");
        if (targetDate == null) {
            return ResponseResult.errorResult(400, "缺少补签日期");
        }
        return checkinService.patchCheckin(userId, targetDate);
    }

    /** 获取签到进度 */
    @GetMapping("/milestone")
    public ResponseResult milestone(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return checkinService.getMilestone(userId);
    }
}