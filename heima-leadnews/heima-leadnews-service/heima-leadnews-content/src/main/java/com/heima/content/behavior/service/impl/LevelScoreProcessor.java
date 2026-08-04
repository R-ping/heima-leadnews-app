package com.heima.content.behavior.service.impl;

import com.heima.content.behavior.service.BehaviorPostProcessor;
import com.heima.content.service.level.LevelService;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 等级积分后置处理器
 * 用户在主动行为后获得逐日等级积分
 * 文章被互动时，文章作者获得逐力值
 */
@Slf4j
@Component
public class LevelScoreProcessor implements BehaviorPostProcessor {

    @Autowired
    private LevelService levelService;

    @Override
    public void postProcess(BehaviorContext context, BehaviorResult result) {
        BehaviorType type = context.getBehaviorType();
        Integer userId = context.getUserId();
        Integer targetUserId = context.getTargetUserId();

        if (userId == null) {
            return;
        }

        // 1. 操作用户获得逐日等级积分
        String actionType = mapToLevelAction(type);
        if (actionType != null) {
            String actionDetail = buildActionDetail(context);
            try {
                levelService.recordActionWithLimit(userId.longValue(), actionType, actionDetail);
                log.debug("用户{}获得逐日积分: actionType={}", userId, actionType);
            } catch (Exception e) {
                log.error("用户{}逐日积分记录失败: actionType={}", userId, actionType, e);
            }
        }

        // 2. 目标用户（文章作者/被关注者）获得逐力值
        if (targetUserId != null) {
            String powerChangeType = mapToPowerChange(type);
            if (powerChangeType != null) {
                try {
                    Long targetId = context.getTargetId();
                    levelService.calculatePower(targetUserId.longValue(), targetId, powerChangeType, 1);
                    log.debug("用户{}获得逐力值: changeType={}, sourceId={}", targetUserId, powerChangeType, targetId);
                } catch (Exception e) {
                    log.error("用户{}逐力值记录失败: changeType={}", targetUserId, powerChangeType, e);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return 1; // 最先执行
    }

    /**
     * 将行为类型映射为等级积分中的 actionType
     */
    private String mapToLevelAction(BehaviorType type) {
        switch (type) {
            case LIKE_ARTICLE: return "like_article";
            case LIKE_PIN: return "like_pin";
            case COLLECT_ARTICLE: return "collect_article";
            case COMMENT_ARTICLE: return "comment_article";
            case COMMENT_PIN: return "comment_pin";
            case FOLLOW_USER: return "follow_user";
            case SHARE: return "share";
            case PUBLISH_ARTICLE: return "publish_article";
            case PUBLISH_PIN: return "publish_pins";
            case BROWSE_ARTICLE: return "browse_article";
            case BROWSE_COURSE: return "browse_course";
            default: return null;
        }
    }

    /**
     * 将行为类型映射为逐力值变更类型
     */
    private String mapToPowerChange(BehaviorType type) {
        switch (type) {
            case LIKE_ARTICLE:
            case LIKE_PIN:
                return "get_like";
            case COMMENT_ARTICLE:
            case COMMENT_PIN:
                return "get_comment";
            case COLLECT_ARTICLE:
                return "get_favorite";
            case BROWSE_ARTICLE:
                return "get_read";
            default: return null;
        }
    }

    /**
     * 构建行为详情描述
     */
    private String buildActionDetail(BehaviorContext context) {
        String targetName = "";
        switch (context.getTargetType()) {
            case 1: targetName = "文章"; break;
            case 2: targetName = "沸点"; break;
            case 3: targetName = "用户"; break;
            case 4: targetName = "课程"; break;
            case 5: targetName = "专栏"; break;
        }
        return targetName + "ID:" + context.getTargetId();
    }
}