package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApBehaviorLikes;
import com.heima.model.article.pojos.ApCollection;
import com.heima.model.article.pojos.ApFollow;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApArticleMapper;
import com.heima.user.mapper.ApBehaviorLikesMapper;
import com.heima.user.mapper.ApCollectionMapper;
import com.heima.user.mapper.ApFollowMapper;
import com.heima.user.mapper.ApPinsMapper;
import com.heima.user.service.UserStatisticsService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {

    @Autowired
    private ApFollowMapper apFollowMapper;

    @Autowired
    private ApBehaviorLikesMapper apBehaviorLikesMapper;

    @Autowired
    private ApCollectionMapper apCollectionMapper;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApPinsMapper apPinsMapper;

    @Autowired
    private LevelService levelService;

    @Override
    public ResponseResult<Map<String, Object>> getUserStatistics() {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.okResult(new HashMap<>());
        }
        Integer userId = currentUser.getId();

        Map<String, Object> result = new HashMap<>();

        // 1. followCount: from ap_user_follow where user_id = current user
        long followCount = apFollowMapper.selectCount(
                new LambdaQueryWrapper<ApFollow>().eq(ApFollow::getUserId, userId));
        result.put("followCount", followCount);

        // 2. likeCount: sum of likes on user's articles + likes on user's pins
        long likeCount = calculateLikeCount(userId);
        result.put("likeCount", likeCount);

        // 3. collectCount: from ap_collection where user_id = current user
        long collectCount = apCollectionMapper.selectCount(
                new LambdaQueryWrapper<ApCollection>().eq(ApCollection::getUserId, userId));
        result.put("collectCount", collectCount);

        // 4. levelInfo: from LevelService
        Map<String, Object> levelInfo = levelService.getUserLevelInfo(userId.longValue());
        result.put("levelInfo", levelInfo);

        // 5. diamondCount: placeholder 0
        result.put("diamondCount", 0);

        return ResponseResult.okResult(result);
    }

    private long calculateLikeCount(Integer userId) {
        long totalLikes = 0;

        // Article likes: get user's articles, then count likes
        List<ApArticle> articles = apArticleMapper.selectList(
                new LambdaQueryWrapper<ApArticle>()
                        .eq(ApArticle::getAuthorId, userId.longValue())
                        .eq(ApArticle::getIsDeleted, false));
        if (!articles.isEmpty()) {
            List<Long> articleIds = articles.stream()
                    .map(ApArticle::getId)
                    .collect(Collectors.toList());
            totalLikes += apBehaviorLikesMapper.selectCount(
                    new LambdaQueryWrapper<ApBehaviorLikes>()
                            .in(ApBehaviorLikes::getEntryId, articleIds)
                            .eq(ApBehaviorLikes::getType, 0)
                            .eq(ApBehaviorLikes::getOperation, 0));
        }

        // Pins likes: get user's pins, then count likes
        List<ApPins> pins = apPinsMapper.selectList(
                new LambdaQueryWrapper<ApPins>()
                        .eq(ApPins::getAuthorId, userId.longValue())
                        .eq(ApPins::getIsDeleted, false));
        if (!pins.isEmpty()) {
            List<Long> pinsIds = pins.stream()
                    .map(ApPins::getId)
                    .collect(Collectors.toList());
            totalLikes += apBehaviorLikesMapper.selectCount(
                    new LambdaQueryWrapper<ApBehaviorLikes>()
                            .in(ApBehaviorLikes::getEntryId, pinsIds)
                            .eq(ApBehaviorLikes::getType, 1)
                            .eq(ApBehaviorLikes::getOperation, 0));
        }

        return totalLikes;
    }
}