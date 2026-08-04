package com.heima.reward.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.reward.service.WelfareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/welfare")
public class WelfareController {

    @Autowired
    private WelfareService welfareService;

    /** 获取福利商品列表 */
    @GetMapping("/goods")
    public ResponseResult goodsList(@RequestParam(defaultValue = "1") Integer type,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size) {
        return welfareService.getGoodsList(type, page, size);
    }

    /** 获取商品详情 */
    @GetMapping("/goods/{goodsId}")
    public ResponseResult goodsDetail(@PathVariable String goodsId) {
        return welfareService.getGoodsDetail(goodsId);
    }

    /** 执行兑换 */
    @PostMapping("/exchange")
    public ResponseResult exchange(@RequestHeader(value = "userId", required = false) Long userId,
                                    @RequestBody Map<String, Object> body) {
        if (userId == null) userId = 1L;
        return welfareService.exchange(userId, body);
    }

    /** 获取我的兑换记录 */
    @GetMapping("/my-exchanges")
    public ResponseResult myExchanges(@RequestHeader(value = "userId", required = false) Long userId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size,
                                       @RequestParam(defaultValue = "all") String status) {
        if (userId == null) userId = 1L;
        return welfareService.getMyExchanges(userId, page, size, status);
    }
}
