package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.*;
import com.heima.article.service.ArticleStatisticsService;
import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleStatisticsServiceImpl implements ArticleStatisticsService {

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
    public ResponseResult getUserStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. followCount
        long followCount = apFollowMapper.selectCount(
                new LambdaQueryWrapper<ApFollow>().eq(ApFollow::getUserId, userId));
        result.put("followCount", followCount);

        // 2. likeCount
        long likeCount = calculateLikeCount(userId);
        result.put("likeCount", likeCount);

        // 3. collectCount
        long collectCount = apCollectionMapper.selectCount(
                new LambdaQueryWrapper<ApCollection>().eq(ApCollection::getUserId, userId));
        result.put("collectCount", collectCount);

        // 4. levelInfo
        Map<String, Object> levelInfo = levelService.getUserLevelInfo(userId);
        result.put("levelInfo", levelInfo);

        // 5. diamondCount
        result.put("diamondCount", 0);

        return ResponseResult.okResult(result);
    }

    private long calculateLikeCount(Long userId) {
        long totalLikes = 0;

        // 文章点赞数
        List<ApArticle> articles = apArticleMapper.selectList(
                new LambdaQueryWrapper<ApArticle>()
                        .eq(ApArticle::getAuthorId, userId)
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

        // 沸点点赞数
        List<ApPins> pins = apPinsMapper.selectList(
                new LambdaQueryWrapper<ApPins>()
                        .eq(ApPins::getAuthorId, userId)
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
