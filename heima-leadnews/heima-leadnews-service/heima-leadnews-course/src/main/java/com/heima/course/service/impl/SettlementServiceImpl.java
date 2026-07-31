package com.heima.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.course.mapper.ApCourseOrderMapper;
import com.heima.course.mapper.ApCourseSettlementMapper;
import com.heima.course.service.SettlementService;
import com.heima.model.article.pojos.ApCourseOrder;
import com.heima.model.article.pojos.ApCourseSettlement;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private static final BigDecimal AUTHOR_SHARE_RATE = new BigDecimal("0.7");
    private static final BigDecimal PLATFORM_SHARE_RATE = new BigDecimal("0.3");

    @Autowired
    private ApCourseSettlementMapper settlementMapper;

    @Autowired
    private ApCourseOrderMapper orderMapper;

    @Override
    public ResponseResult getMonthlyList(Long authorId) {
        LambdaQueryWrapper<ApCourseSettlement> query = new LambdaQueryWrapper<>();
        query.eq(ApCourseSettlement::getAuthorId, authorId.intValue());
        query.orderByDesc(ApCourseSettlement::getSettlementMonth);
        List<ApCourseSettlement> list = settlementMapper.selectList(query);

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalPlatformShare = BigDecimal.ZERO;
        BigDecimal totalAuthorShare = BigDecimal.ZERO;
        for (ApCourseSettlement s : list) {
            totalSales = totalSales.add(s.getTotalSales() != null ? s.getTotalSales() : BigDecimal.ZERO);
            totalPlatformShare = totalPlatformShare.add(s.getPlatformShare() != null ? s.getPlatformShare() : BigDecimal.ZERO);
            totalAuthorShare = totalAuthorShare.add(s.getAuthorShare() != null ? s.getAuthorShare() : BigDecimal.ZERO);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list != null ? list : Collections.emptyList());
        result.put("totalSales", totalSales);
        result.put("totalPlatformShare", totalPlatformShare);
        result.put("totalAuthorShare", totalAuthorShare);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult getSettlementDetail(Long settlementId) {
        ApCourseSettlement settlement = settlementMapper.selectById(settlementId);
        if (settlement == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.okResult(settlement);
    }

    @Override
    @Transactional
    public void executeMonthlySettlement(String month) {
        log.info("开始执行月度结算: {}", month);

        // 查询上月已支付订单
        LambdaQueryWrapper<ApCourseOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(ApCourseOrder::getStatus, ApCourseOrder.Status.PAID.getCode());
        orderQuery.apply("DATE_FORMAT(pay_time, '%Y-%m') = {0}", month);
        List<ApCourseOrder> orders = orderMapper.selectList(orderQuery);

        if (orders.isEmpty()) {
            log.info("月份 {} 无待结算订单", month);
            return;
        }

        // 按作者+课程分组统计
        Map<String, SettlementGroup> groupMap = new HashMap<>();
        for (ApCourseOrder order : orders) {
            String key = order.getUserId() + "_" + order.getCourseId();
            SettlementGroup group = groupMap.computeIfAbsent(key, k -> {
                SettlementGroup g = new SettlementGroup();
                g.authorId = order.getUserId();
                g.courseId = order.getCourseId();
                return g;
            });
            group.totalSales = group.totalSales.add(order.getPaidAmount());
            group.orderCount++;
        }

        // 生成结算记录
        for (SettlementGroup group : groupMap.values()) {
            ApCourseSettlement settlement = new ApCourseSettlement();
            settlement.setAuthorId(group.authorId);
            settlement.setCourseId(group.courseId);
            settlement.setSettlementMonth(month);
            settlement.setTotalSales(group.totalSales);
            settlement.setPlatformShare(group.totalSales.multiply(PLATFORM_SHARE_RATE).setScale(2, BigDecimal.ROUND_HALF_UP));
            settlement.setAuthorShare(group.totalSales.multiply(AUTHOR_SHARE_RATE).setScale(2, BigDecimal.ROUND_HALF_UP));
            settlement.setOrderCount(group.orderCount);
            settlement.setStatus(0);
            settlement.setCreatedTime(new Date());

            settlementMapper.insert(settlement);
            log.info("结算记录: authorId={}, courseId={}, month={}, totalSales={}, authorShare={}",
                    group.authorId, group.courseId, month, group.totalSales, settlement.getAuthorShare());
        }

        log.info("月度结算完成: {}, 共{}条记录", month, groupMap.size());
    }

    private static class SettlementGroup {
        int authorId;
        Long courseId;
        BigDecimal totalSales = BigDecimal.ZERO;
        int orderCount = 0;
    }
}