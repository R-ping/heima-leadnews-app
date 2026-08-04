package com.heima.content.behavior.service.impl;

import com.heima.content.behavior.service.BehaviorPostProcessor;
import com.heima.content.mapper.level.ApUserLevelMapper;
import com.heima.model.level.pojos.ApUserLevel;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 统计数据后置处理器
 *
 * 职责：
 * 1. 更新用户最后活跃时间（用于"最近活跃"展示）
 * 2. 维护用户行为统计摘要数据
 *
 * 备注：文章热度分更新由 ArticleScoreProcessor 处理，
 * 等级积分由 LevelScoreProcessor 处理，
 * 站内信通知由 NotificationProcessor 处理，
 * 本处理器专注于用户维度的统计信息维护。
 */
@Slf4j
@Component
public class StatisticsProcessor implements BehaviorPostProcessor {

    @Autowired
    private ApUserLevelMapper userLevelMapper;

    @Override
    public void postProcess(BehaviorContext context, BehaviorResult result) {
        Integer userId = context.getUserId();
        if (userId == null) {
            return;
        }

        // 1. 更新用户最后活跃时间
        updateLastActiveTime(userId.longValue());

        // 2. 统计不同类型的活跃度
        trackBehaviorActivity(context);
    }

    @Override
    public int getOrder() {
        return 5; // 在所有主要后置处理器之后执行（等级1 → 文章热度2 → 通知4 → 统计5）
    }

    /**
     * 更新用户最后活跃时间
     * 使用 updated_time 字段记录最后活跃时间
     */
    private void updateLastActiveTime(Long userId) {
        try {
            ApUserLevel userLevel = userLevelMapper.selectById(userId);
            if (userLevel != null) {
                userLevel.setUpdatedTime(new Date());
                userLevelMapper.updateById(userLevel);
            }
        } catch (Exception e) {
            log.error("更新用户最后活跃时间失败: userId={}", userId, e);
        }
    }

    /**
     * 跟踪行为活跃度
     * 按行为类型分组统计，识别用户行为模式
     */
    private void trackBehaviorActivity(BehaviorContext context) {
        BehaviorType type = context.getBehaviorType();
        Integer userId = context.getUserId();

        // 记录行为类型到日志（用于后续分析）
        log.info("用户行为统计: userId={}, type={}, targetType={}, targetId={}",
            userId, type.getCode(), context.getTargetType(), context.getTargetId());
    }
}