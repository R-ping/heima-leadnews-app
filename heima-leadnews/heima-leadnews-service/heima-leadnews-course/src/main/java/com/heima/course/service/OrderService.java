package com.heima.course.service;

import com.heima.model.article.pojos.ApCourseOrder;
import com.heima.model.common.dtos.ResponseResult;

import java.math.BigDecimal;

public interface OrderService {

    /** 创建订单 */
    ResponseResult createOrder(Long courseId, String discountCode, Long userId);

    /** 查询订单状态 */
    ResponseResult getOrderStatus(String orderNo);

    /** 我的订单列表 */
    ResponseResult getMyOrders(Long userId, Integer page, Integer size);

    /** 支付成功回调处理 */
    void handlePaySuccess(String orderNo, String tradeNo);

    /** 根据订单号查询 */
    ApCourseOrder getByOrderNo(String orderNo);
}