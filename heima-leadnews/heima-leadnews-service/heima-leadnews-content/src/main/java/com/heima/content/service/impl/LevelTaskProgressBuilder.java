package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.content.mapper.ApUserActionLogMapper;
import com.heima.model.article.pojos.ApUserActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.heima.content.constants.LevelScoreConstants.ACTION_SCORE_MAP;
import static com.heima.content.constants.LevelScoreConstants.DAILY_ACTION_LIMIT;

/**
 * 等级任务进度构建器，从 LevelServiceImpl 中提取以降低类复杂度
 */
@Component
public class LevelTaskProgressBuilder {

    @Autowired
    private ApUserActionLogMapper actionLogMapper;

    private static final Map<String, String> TASK_NAME_MAP = new HashMap<>();
    private static final Map<String, String> TASK_ICON_MAP = new HashMap<>();
    private static final String[] ACTION_TYPES = {
        "daily_checkin", "upload_avatar", "daily_login", "publish_article",
        "publish_pins", "comment_article", "comment_pin", "like_article",
        "like_pin", "collect_article", "follow_user", "browse_article",
        "browse_course", "be_followed", "pin_liked", "article_liked"
    };

    static {
        TASK_NAME_MAP.put("daily_checkin", "每日签到");
        TASK_NAME_MAP.put("upload_avatar", "上传头像");
        TASK_NAME_MAP.put("daily_login", "移动端登录");
        TASK_NAME_MAP.put("publish_article", "发布文章");
        TASK_NAME_MAP.put("publish_pins", "发布沸点");
        TASK_NAME_MAP.put("comment_article", "评论文章");
        TASK_NAME_MAP.put("comment_pin", "评论沸点");
        TASK_NAME_MAP.put("like_article", "点赞文章");
        TASK_NAME_MAP.put("like_pin", "点赞沸点");
        TASK_NAME_MAP.put("collect_article", "收藏文章");
        TASK_NAME_MAP.put("follow_user", "关注用户");
        TASK_NAME_MAP.put("browse_article", "浏览文章");
        TASK_NAME_MAP.put("browse_course", "浏览课程");
        TASK_NAME_MAP.put("be_followed", "被关注");
        TASK_NAME_MAP.put("pin_liked", "沸点获赞");
        TASK_NAME_MAP.put("article_liked", "文章获赞");

        TASK_ICON_MAP.put("daily_checkin", "check-circle");
        TASK_ICON_MAP.put("upload_avatar", "camera");
        TASK_ICON_MAP.put("daily_login", "smartphone");
        TASK_ICON_MAP.put("publish_article", "edit");
        TASK_ICON_MAP.put("publish_pins", "message-circle");
        TASK_ICON_MAP.put("comment_article", "message-square");
        TASK_ICON_MAP.put("comment_pin", "message-circle");
        TASK_ICON_MAP.put("like_article", "heart");
        TASK_ICON_MAP.put("like_pin", "thumbs-up");
        TASK_ICON_MAP.put("collect_article", "star");
        TASK_ICON_MAP.put("follow_user", "user-plus");
        TASK_ICON_MAP.put("browse_article", "book-open");
        TASK_ICON_MAP.put("browse_course", "play-circle");
        TASK_ICON_MAP.put("be_followed", "users");
        TASK_ICON_MAP.put("pin_liked", "award");
        TASK_ICON_MAP.put("article_liked", "trending-up");
    }

    public Map<String, Object> buildTaskProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tasks = new ArrayList<>();

        String today = new java.sql.Date(System.currentTimeMillis()).toString();

        for (String actionType : ACTION_TYPES) {
            Map<String, Object> task = new HashMap<>();
            Integer max = DAILY_ACTION_LIMIT.get(actionType);
            Integer score = ACTION_SCORE_MAP.get(actionType);

            LambdaQueryWrapper<ApUserActionLog> query = new LambdaQueryWrapper<>();
            query.eq(ApUserActionLog::getUserId, userId);
            query.eq(ApUserActionLog::getActionType, actionType);
            query.apply("DATE(created_time) = '" + today + "'");
            long current = actionLogMapper.selectCount(query);

            task.put("actionType", actionType);
            task.put("name", TASK_NAME_MAP.get(actionType));
            task.put("icon", TASK_ICON_MAP.get(actionType));
            task.put("current", current);
            task.put("max", max);
            task.put("score", score);

            if (max == null) {
                task.put("completed", false);
            } else {
                task.put("completed", current >= max);
            }

            tasks.add(task);
        }

        result.put("tasks", tasks);
        return result;
    }
}