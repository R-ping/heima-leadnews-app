package com.heima.reward.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.reward.service.CheckinService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sign")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    /** 获取签到状态与日历数据 */
    @GetMapping("/status")
    public ResponseResult status(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return checkinService.getStatus(userId);
    }

    /** 执行每日签到 */
    @PostMapping("/checkin")
    public ResponseResult doCheckin(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return checkinService.doCheckin(userId);
    }

    /** 执行补签操作 */
    @PostMapping("/extra")
    public ResponseResult doExtra(@RequestHeader(value = "userId", required = false) Long userId,
                                   @RequestBody Map<String, String> body) {
        if (userId == null) userId = 1L;
        String targetDate = body.get("date");
        if (targetDate == null) {
            return ResponseResult.errorResult(400, "缺少补签日期");
        }
        return checkinService.doExtra(userId, targetDate);
    }

    /** 获取今日签到状态（侧边栏用，保留旧路径兼容） */
    @GetMapping("/today")
    public ResponseResult todayStatus(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return checkinService.getTodayStatus(userId);
    }
}