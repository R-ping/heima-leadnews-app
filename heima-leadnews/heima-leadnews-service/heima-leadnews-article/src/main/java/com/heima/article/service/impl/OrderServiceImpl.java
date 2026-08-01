package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApCourseDiscountMapper;
import com.heima.article.mapper.ApCourseMapper;
import com.heima.article.mapper.ApCourseOrderMapper;
import com.heima.article.mapper.ApUserCourseMapper;
import com.heima.article.service.DiscountService;
import com.heima.article.service.OrderService;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.article.pojos.ApCourseDiscount;
import com.heima.model.article.pojos.ApCourseOrder;
import com.heima.model.article.pojos.ApUserCourse;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ApCourseOrderMapper orderMapper;

    @Autowired
    private ApCourseMapper courseMapper;

    @Autowired
    private ApUserCourseMapper userCourseMapper;

    @Autowired
    private ApCourseDiscountMapper discountMapper;

    @Autowired
    private DiscountService discountService;

    @Override
    @Transactional
    public ResponseResult createOrder(Long courseId, String discountCode, Long userId) {
        if (courseId == null || userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询课程信息获取真实价格
        ApCourse course = courseMapper.selectById(courseId);
        if (course == null || course.getIsDeleted() == 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "课程不存在");
        }

        BigDecimal originalAmount = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

        BigDecimal discountAmount = BigDecimal.ZERO;
        ApCourseDiscount discount = null;

        // 校验折扣码
        if (discountCode != null && !discountCode.isEmpty()) {
            discount = discountService.validateDiscount(discountCode, courseId);
            if (discount == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "折扣码无效或已过期");
            }

            // 计算折扣金额
            if (discount.getDiscountType() == ApCourseDiscount.DiscountType.FIXED.getCode()) {
                discountAmount = discount.getDiscountValue();
            } else if (discount.getDiscountType() == ApCourseDiscount.DiscountType.PERCENTAGE.getCode()) {
                discountAmount = originalAmount.multiply(
                    BigDecimal.ONE.subtract(discount.getDiscountValue().divide(new BigDecimal("100")))
                );
                discountAmount = originalAmount.subtract(discountAmount);
            }
        }

        // 计算实付金额
        BigDecimal paidAmount = originalAmount.subtract(discountAmount);
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            paidAmount = BigDecimal.ZERO;
        }

        // 创建订单
        ApCourseOrder order = new ApCourseOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId.intValue());
        order.setCourseId(courseId);
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPaidAmount(paidAmount);
        order.setDiscountCode(discountCode != null ? discountCode : "");
        order.setStatus(ApCourseOrder.Status.PENDING.getCode());
        order.setCreatedTime(new Date());
        order.setUpdatedTime(new Date());

        orderMapper.insert(order);

        return ResponseResult.okResult(order);
    }

    @Override
    public ResponseResult getOrderStatus(String orderNo) {
        ApCourseOrder order = getByOrderNo(orderNo);
        if (order == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "订单不存在");
        }
        return ResponseResult.okResult(order);
    }

    @Override
    public ResponseResult getMyOrders(Long userId, Integer page, Integer size) {
        IPage<ApCourseOrder> iPage = new Page<>(page, size);
        LambdaQueryWrapper<ApCourseOrder> query = new LambdaQueryWrapper<>();
        query.eq(ApCourseOrder::getUserId, userId.intValue());
        query.orderByDesc(ApCourseOrder::getCreatedTime);

        IPage<ApCourseOrder> resultPage = orderMapper.selectPage(iPage, query);
        Map<String, Object> data = new HashMap<>();
        data.put("list", resultPage.getRecords());
        data.put("total", resultPage.getTotal());
        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional
    public void handlePaySuccess(String orderNo, String tradeNo) {
        ApCourseOrder order = getByOrderNo(orderNo);
        if (order == null) {
            log.error("订单不存在: {}", orderNo);
            return;
        }

        if (order.getStatus() != ApCourseOrder.Status.PENDING.getCode()) {
            log.warn("订单状态异常: {}, status={}", orderNo, order.getStatus());
            return;
        }

        order.setStatus(ApCourseOrder.Status.PAID.getCode());
        order.setTradeNo(tradeNo);
        order.setPayTime(new Date());
        order.setUpdatedTime(new Date());
        orderMapper.updateById(order);

        // 更新折扣码使用次数
        if (order.getDiscountCode() != null && !order.getDiscountCode().isEmpty()) {
            LambdaQueryWrapper<ApCourseDiscount> discountQuery = new LambdaQueryWrapper<>();
            discountQuery.eq(ApCourseDiscount::getCode, order.getDiscountCode());
            ApCourseDiscount discount = discountService.getDiscountByCode(order.getDiscountCode());
            if (discount != null) {
                discount.setUsedCount(discount.getUsedCount() + 1);
                discountMapper.updateById(discount);
            }
        }

        // 更新课程学习人数和销售数量
        ApCourse course = courseMapper.selectById(order.getCourseId());
        if (course != null) {
            course.setStudyCount((course.getStudyCount() != null ? course.getStudyCount() : 0) + 1);
            course.setSalesCount((course.getSalesCount() != null ? course.getSalesCount() : 0) + 1);
            if (course.getTotalRevenue() != null) {
                course.setTotalRevenue(course.getTotalRevenue().add(order.getPaidAmount()));
            } else {
                course.setTotalRevenue(order.getPaidAmount());
            }
            courseMapper.updateById(course);
        }

        // 添加用户课程权限
        LambdaQueryWrapper<ApUserCourse> ucQuery = new LambdaQueryWrapper<>();
        ucQuery.eq(ApUserCourse::getUserId, order.getUserId());
        ucQuery.eq(ApUserCourse::getCourseId, order.getCourseId());
        ApUserCourse userCourse = userCourseMapper.selectOne(ucQuery);
        if (userCourse == null) {
            userCourse = new ApUserCourse();
            userCourse.setUserId(order.getUserId());
            userCourse.setCourseId(order.getCourseId());
            userCourse.setAccessType(1); // 购买获得
            userCourse.setIsActive((byte) 1);
            userCourse.setIsTrial(0);
            userCourse.setProgress(BigDecimal.ZERO);
            userCourse.setLastLearnAt(new Date());
            userCourse.setCreatedTime(new Date());
            userCourseMapper.insert(userCourse);
        } else {
            userCourse.setIsActive((byte) 1);
            userCourse.setAccessType(1);
            userCourse.setIsTrial(0);
            userCourse.setLastLearnAt(new Date());
            userCourseMapper.updateById(userCourse);
        }

        log.info("订单支付成功: orderNo={}, tradeNo={}, userId={}, courseId={}",
                orderNo, tradeNo, order.getUserId(), order.getCourseId());
    }

    @Override
    public ApCourseOrder getByOrderNo(String orderNo) {
        LambdaQueryWrapper<ApCourseOrder> query = new LambdaQueryWrapper<>();
        query.eq(ApCourseOrder::getOrderNo, orderNo);
        return orderMapper.selectOne(query);
    }

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}