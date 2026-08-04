package com.heima.content.behavior.service.impl;

import com.heima.common.constants.ArticleConstants;
import com.heima.content.behavior.service.BehaviorPostProcessor;
import com.heima.content.mapper.article.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorResult;
import com.heima.model.behavior.BehaviorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 文章热度分后置处理器
 * 用户对文章/沸点进行互动后，更新对应的热度分数
 *
 * 行为 → 热度分影响：
 * - 点赞文章 → likes +1 → 重新计算热度分
 * - 收藏文章 → collection +1 → 重新计算热度分
 * - 评论文章 → comment +1 → 重新计算热度分
 * - 浏览文章 → views +1 → 重新计算热度分
 */
@Slf4j
@Component
public class ArticleScoreProcessor implements BehaviorPostProcessor {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public void postProcess(BehaviorContext context, BehaviorResult result) {
        BehaviorType type = context.getBehaviorType();

        // 仅处理文章相关行为（targetType=1）
        if (context.getTargetType() == null || context.getTargetType() != 1) {
            return;
        }

        Long targetId = context.getTargetId();
        if (targetId == null) {
            return;
        }

        // 判断是否为需要更新热度分的行为
        String field = mapToScoreField(type);
        if (field == null) {
            return;
        }

        try {
            // 增加对应字段计数
            incrementField(targetId, field);
            // 重新计算热度分
            recalculateScore(targetId);
            log.debug("文章{}热度分已更新: action={}", targetId, type.getCode());
        } catch (Exception e) {
            log.error("文章{}热度分更新失败: action={}", targetId, type.getCode(), e);
        }
    }

    @Override
    public int getOrder() {
        return 2; // 在等级积分(1)之后，通知(4)之前执行
    }

    /**
     * 将行为类型映射为需要更新的文章字段名
     */
    private String mapToScoreField(BehaviorType type) {
        switch (type) {
            case LIKE_ARTICLE:
                return "likes";
            case COLLECT_ARTICLE:
                return "collection";
            case COMMENT_ARTICLE:
                return "comment";
            case BROWSE_ARTICLE:
                return "views";
            default:
                return null;
        }
    }

    /**
     * 增加文章对应字段的计数
     * 使用直接SQL更新，避免并发问题
     */
    private void incrementField(Long articleId, String field) {
        ApArticle article = apArticleMapper.selectById(articleId);
        if (article == null) {
            log.warn("文章不存在，无法更新热度分: id={}", articleId);
            return;
        }

        switch (field) {
            case "likes":
                article.setLikes(article.getLikes() == null ? 1 : article.getLikes() + 1);
                break;
            case "collection":
                article.setCollection(article.getCollection() == null ? 1 : article.getCollection() + 1);
                break;
            case "comment":
                article.setComment(article.getComment() == null ? 1 : article.getComment() + 1);
                break;
            case "views":
                article.setViews(article.getViews() == null ? 1 : article.getViews() + 1);
                break;
            default:
                return;
        }

        apArticleMapper.updateById(article);
    }

    /**
     * 根据各字段权重重新计算文章热度分
     */
    private void recalculateScore(Long articleId) {
        ApArticle article = apArticleMapper.selectById(articleId);
        if (article == null) {
            return;
        }

        int score = 0;
        if (article.getLikes() != null) {
            score += article.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (article.getViews() != null) {
            score += article.getViews();
        }
        if (article.getComment() != null) {
            score += article.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (article.getCollection() != null) {
            score += article.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        ApArticle update = new ApArticle();
        update.setId(articleId);
        update.setScore(score);
        apArticleMapper.updateById(update);

        log.debug("文章{}热度分重新计算: score={}", articleId, score);
    }
}