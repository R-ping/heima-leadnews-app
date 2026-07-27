package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleRecommendService;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ApArticleRecommendServiceImpl implements ApArticleRecommendService {

    private static final int MAX_CANDIDATES = 500;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public ResponseResult recommend(ArticleRecommendDto dto) {
        int size = (dto.getSize() == null || dto.getSize() <= 0) ? DEFAULT_SIZE : Math.min(dto.getSize(), MAX_SIZE);
        int page = (dto.getPage() == null || dto.getPage() < 0) ? 0 : dto.getPage();
        String channel = (dto.getChannel() == null || dto.getChannel().isEmpty()) ? "__all__" : dto.getChannel();

        // 生成新种子或使用已有种子
        long seed = (dto.getSeed() != null) ? dto.getSeed() : System.nanoTime();
        if (dto.getSeed() == null) {
            log.info("Recommend: new seed={}, channel={}, page={}", seed, channel, page);
        }

        // 1. 查询候选池
        Integer channelId = null;
        if (!"__all__".equals(channel)) {
            try { channelId = Integer.parseInt(channel); } catch (NumberFormatException ignored) {}
        }
        List<ApArticle> candidates = apArticleMapper.selectRecommendCandidates(channelId, MAX_CANDIDATES);
        if (candidates == null || candidates.isEmpty()) {
            log.info("Recommend: no candidates for channel={}", channel);
            return ResponseResult.okResult(buildEmptyResponse(seed, page));
        }

        // 2. 计算各项指标的最大值（用于归一化）
        long now = System.currentTimeMillis();
        int maxViews = candidates.stream().mapToInt(a -> a.getViews() != null ? a.getViews() : 0).max().orElse(1);
        int maxLikes = candidates.stream().mapToInt(a -> a.getLikes() != null ? a.getLikes() : 0).max().orElse(1);
        int maxComments = candidates.stream().mapToInt(a -> a.getComment() != null ? a.getComment() : 0).max().orElse(1);
        int maxCollections = candidates.stream().mapToInt(a -> a.getCollection() != null ? a.getCollection() : 0).max().orElse(1);

        // 3. 计算加权分数并预排序
        // 使用 ThreadLocalRandom 为每个文章添加微小的随机扰动（0~0.03），
        // 确保即使分数相同，排序也有细微差别，增强非确定性
        final int fMaxViews = maxViews;
        final int fMaxLikes = maxLikes;
        final int fMaxComments = maxComments;
        final int fMaxCollections = maxCollections;
        candidates.sort((a, b) -> {
            double scoreA = computeWeightedScore(a, now, fMaxViews, fMaxLikes, fMaxComments, fMaxCollections);
            double scoreB = computeWeightedScore(b, now, fMaxViews, fMaxLikes, fMaxComments, fMaxCollections);
            return Double.compare(scoreB, scoreA);
        });

        // 4. 基于种子随机洗牌 —— 这是实现"每次刷新不同结果"的核心
        //    同一个 seed 产生相同的洗牌顺序，保证分页一致性
        //    不同 seed 产生完全不同的排列
        List<ApArticle> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, new Random(seed));

        // 5. 分页截取
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, shuffled.size());
        boolean hasMore = toIndex < shuffled.size();
        List<ApArticle> pageResult = fromIndex < shuffled.size()
                ? shuffled.subList(fromIndex, toIndex)
                : Collections.emptyList();

        // 6. 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult);
        result.put("seed", seed);
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", hasMore);
        result.put("total", shuffled.size());

        log.info("Recommend: returned {} articles, hasMore={}, seed={}, channel={}, page={}",
                pageResult.size(), hasMore, seed, channel, page);
        return ResponseResult.okResult(result);
    }

    /**
     * 计算加权推荐分数。
     * 综合考虑：热度分(score)、时效性、阅读量、点赞数、评论数、收藏数。
     *
     * 权重分配：
     *   score          × 0.25  — 编辑/系统设置的热度分
     *   recencyFactor  × 0.20  — 发布时间越近分越高（7天内线性衰减）
     *   views          × 0.15  — 阅读量（归一化）
     *   likes          × 0.15  — 点赞数（归一化）
     *   comments       × 0.10  — 评论数（归一化）
     *   collections    × 0.10  — 收藏数（归一化）
     *   randomNoise    × 0.05  — 微小随机扰动，打破平局
     *
     * maxValues 用于归一化各指标到 0~1 区间
     */
    private double computeWeightedScore(ApArticle article, long now,
                                        int maxViews, int maxLikes, int maxComments, int maxCollections) {
        // 热度分（归一化到0-1，假设最高分10000）
        int score = article.getScore() != null ? article.getScore() : 0;
        double normalizedScore = Math.min(score / 10000.0, 1.0);

        // 时效性因子：7天内线性衰减，0=最旧/超过7天，1=刚刚发布
        double recencyFactor = 0;
        if (article.getPublishTime() != null) {
            long daysSincePublished = (now - article.getPublishTime().getTime()) / (1000L * 60 * 60 * 24);
            recencyFactor = Math.max(0, 1.0 - daysSincePublished / 7.0);
        }

        // 用户互动指标（归一化到0-1）
        int views = article.getViews() != null ? article.getViews() : 0;
        int likes = article.getLikes() != null ? article.getLikes() : 0;
        int comments = article.getComment() != null ? article.getComment() : 0;
        int collections = article.getCollection() != null ? article.getCollection() : 0;

        double normalizedViews = maxViews > 0 ? (double) views / maxViews : 0;
        double normalizedLikes = maxLikes > 0 ? (double) likes / maxLikes : 0;
        double normalizedComments = maxComments > 0 ? (double) comments / maxComments : 0;
        double normalizedCollections = maxCollections > 0 ? (double) collections / maxCollections : 0;

        // 微小随机扰动（0 ~ 0.05），打破完全相同分数的平局
        double randomNoise = ThreadLocalRandom.current().nextDouble() * 0.05;

        return normalizedScore * 0.25
                + recencyFactor * 0.20
                + normalizedViews * 0.15
                + normalizedLikes * 0.15
                + normalizedComments * 0.10
                + normalizedCollections * 0.10
                + randomNoise;
    }

    private Map<String, Object> buildEmptyResponse(long seed, int page) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", Collections.emptyList());
        result.put("seed", seed);
        result.put("page", page);
        result.put("size", 0);
        result.put("hasMore", false);
        result.put("total", 0);
        return result;
    }
}