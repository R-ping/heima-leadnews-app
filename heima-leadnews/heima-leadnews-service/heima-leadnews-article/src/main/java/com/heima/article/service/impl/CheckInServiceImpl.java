package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApCheckInMapper;
import com.heima.article.service.CheckInService;
import com.heima.model.article.pojos.ApCheckIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    @Autowired
    private ApCheckInMapper checkInMapper;

    private static final int BASE_REWARD = 5;
    private static final int MAX_BONUS = 10;

    @Override
    public Map<String, Object> doCheckIn(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 检查今天是否已签到
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        LambdaQueryWrapper<ApCheckIn> query = new LambdaQueryWrapper<>();
        query.eq(ApCheckIn::getUserId, userId);
        query.eq(ApCheckIn::getCheckInDate, today);
        Long todayCount = checkInMapper.selectCount(query);

        if (todayCount > 0) {
            result.put("success", false);
            result.put("message", "今天已签到");
            result.put("rewardPoints", 0);
            // 返回当前统计
            Map<String, Object> stats = getCheckInStats(userId);
            result.putAll(stats);
            return result;
        }

        // 计算连续签到天数（从昨天往前数）
        int consecutiveDays = calculateConsecutiveDays(userId, today);

        // 计算奖励积分：5 基础分 + (连续天数-1) 额外奖励，最多额外 10 分
        int bonus = Math.min(consecutiveDays, MAX_BONUS);
        int rewardPoints = BASE_REWARD + bonus;

        // 插入签到记录
        ApCheckIn checkIn = new ApCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(today);
        checkIn.setRewardPoints(rewardPoints);
        checkIn.setCreatedTime(new Date());
        checkInMapper.insert(checkIn);

        log.info("用户{}签到成功，获得积分{}，连续签到{}天", userId, rewardPoints, consecutiveDays + 1);

        // 签到后的统计（连续天数 +1 因为今天也算）
        result.put("success", true);
        result.put("rewardPoints", rewardPoints);
        result.put("consecutiveDays", consecutiveDays + 1);

        // 计算总天数和总积分
        LambdaQueryWrapper<ApCheckIn> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(ApCheckIn::getUserId, userId);
        totalQuery.select(ApCheckIn::getRewardPoints);
        List<ApCheckIn> allRecords = checkInMapper.selectList(totalQuery);
        result.put("totalDays", allRecords.size());
        int totalPoints = allRecords.stream().mapToInt(ApCheckIn::getRewardPoints).sum();
        result.put("totalPoints", totalPoints);

        return result;
    }

    @Override
    public Map<String, Object> getCheckInRecords(Long userId, Integer year, Integer month) {
        Map<String, Object> result = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        java.sql.Date startDate = new java.sql.Date(cal.getTimeInMillis());

        cal.set(year, month, 1, 0, 0, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        java.sql.Date endDate = new java.sql.Date(cal.getTimeInMillis());

        LambdaQueryWrapper<ApCheckIn> query = new LambdaQueryWrapper<>();
        query.eq(ApCheckIn::getUserId, userId);
        query.ge(ApCheckIn::getCheckInDate, startDate);
        query.le(ApCheckIn::getCheckInDate, endDate);
        query.orderByAsc(ApCheckIn::getCheckInDate);
        List<ApCheckIn> records = checkInMapper.selectList(query);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> recordsList = new ArrayList<>();
        for (ApCheckIn record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", sdf.format(record.getCheckInDate()));
            item.put("rewardPoints", record.getRewardPoints());
            recordsList.add(item);
        }

        result.put("records", recordsList);
        result.put("checkInDays", records.size());
        return result;
    }

    @Override
    public Map<String, Object> getCheckInStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        int consecutiveDays = calculateConsecutiveDays(userId, today);

        LambdaQueryWrapper<ApCheckIn> totalQuery = new LambdaQueryWrapper<>();
        totalQuery.eq(ApCheckIn::getUserId, userId);
        totalQuery.select(ApCheckIn::getRewardPoints);
        List<ApCheckIn> allRecords = checkInMapper.selectList(totalQuery);
        int totalDays = allRecords.size();
        int totalPoints = allRecords.stream().mapToInt(ApCheckIn::getRewardPoints).sum();

        result.put("consecutiveDays", consecutiveDays);
        result.put("totalDays", totalDays);
        result.put("totalPoints", totalPoints);

        return result;
    }

    /**
     * 计算连续签到天数（从昨天开始往前数，直到遇到中断）
     */
    private int calculateConsecutiveDays(Long userId, java.sql.Date today) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -1); // 从昨天开始

        int consecutiveDays = 0;
        while (true) {
            java.sql.Date checkDate = new java.sql.Date(cal.getTimeInMillis());
            LambdaQueryWrapper<ApCheckIn> query = new LambdaQueryWrapper<>();
            query.eq(ApCheckIn::getUserId, userId);
            query.eq(ApCheckIn::getCheckInDate, checkDate);
            long count = checkInMapper.selectCount(query);
            if (count > 0) {
                consecutiveDays++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                break;
            }
        }
        return consecutiveDays;
    }
}