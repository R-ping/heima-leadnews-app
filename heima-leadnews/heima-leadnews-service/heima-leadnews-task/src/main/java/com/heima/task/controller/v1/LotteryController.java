package com.heima.task.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.task.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/lottery")
public class LotteryController {

    @Autowired
    private LotteryService lotteryService;

    /** 获取抽奖页面数据 */
    @GetMapping("/dashboard")
    public ResponseResult dashboard(@RequestHeader(value = "userId", required = false) Long userId) {
        if (userId == null) userId = 1L;
        return lotteryService.getDashboard(userId);
    }

    /** 执行抽奖 */
    @PostMapping("/draw")
    public ResponseResult draw(@RequestHeader(value = "userId", required = false) Long userId,
                                @RequestBody Map<String, Object> body) {
        if (userId == null) userId = 1L;
        String type = (String) body.get("type");
        Boolean useFree = (Boolean) body.get("useFree");
        return lotteryService.draw(userId, type, useFree);
    }

    /** 领取实物奖品 */
    @PostMapping("/claim-physical")
    public ResponseResult claimPhysical(@RequestHeader(value = "userId", required = false) Long userId,
                                         @RequestBody Map<String, Object> body) {
        if (userId == null) userId = 1L;
        return lotteryService.claimPhysical(userId, body);
    }

    /** 获取我的收获 */
    @GetMapping("/my-prizes")
    public ResponseResult myPrizes(@RequestHeader(value = "userId", required = false) Long userId,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size,
                                    @RequestParam(defaultValue = "all") String type) {
        if (userId == null) userId = 1L;
        return lotteryService.getMyPrizes(userId, page, size, type);
    }

    /** 获取中奖播报 */
    @GetMapping("/broadcast/recent")
    public ResponseResult broadcast() {
        return lotteryService.getBroadcast();
    }
}