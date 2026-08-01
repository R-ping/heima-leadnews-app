package com.heima.content.service.impl;

import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.service.ApArticleRecommendService;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
            return ResponseResult.okResult(buildEmptyResponse(seed, page, size));
        }

        // 2. 计算各项指标的最大值（用于归一化）—— 单次遍历
        long now = System.currentTimeMillis();
        int maxViews = 0, maxLikes = 0, maxComments = 0, maxCollections = 0;
        for (ApArticle a : candidates) {
            maxViews = Math.max(maxViews, a.getViews() != null ? a.getViews() : 0);
            maxLikes = Math.max(maxLikes, a.getLikes() != null ? a.getLikes() : 0);
            maxComments = Math.max(maxComments, a.getComment() != null ? a.getComment() : 0);
            maxCollections = Math.max(maxCollections, a.getCollection() != null ? a.getCollection() : 0);
        }
        if (maxViews == 0) maxViews = 1;
        if (maxLikes == 0) maxLikes = 1;
        if (maxComments == 0) maxComments = 1;
        if (maxCollections == 0) maxCollections = 1;

        // 3. 预计算加权分数（含确定性随机噪声），存入缓存避免 Comparator 非确定性问题
        //    使用种子确保同种子 → 同噪声 → 同排序 → 同分页结果
        Random rng = new Random(seed);
        Map<Long, Double> scoreCache = new HashMap<>();
        for (ApArticle article : candidates) {
            double baseScore = computeBaseScore(article, now, maxViews, maxLikes, maxComments, maxCollections);
            double noise = rng.nextDouble() * 0.05; // 确定性噪声，种子相同则噪声相同
            scoreCache.put(article.getId(), baseScore + noise);
        }

        // 4. 按预计算分数排序（Comparator 确定性强，不再依赖 ThreadLocalRandom）
        candidates.sort((a, b) -> Double.compare(scoreCache.get(b.getId()), scoreCache.get(a.getId())));

        // 5. 带权随机采样：高分段内做小范围抖动，保留排序优势同时增加非确定性
        //    将排序后的列表按分数分成若干组，组内做种子随机洗牌
        List<ApArticle> shuffled = groupedShuffle(candidates, scoreCache, rng);

        // 6. 分页截取
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, shuffled.size());
        boolean hasMore = toIndex < shuffled.size();
        List<ApArticle> pageResult = fromIndex < shuffled.size()
                ? shuffled.subList(fromIndex, toIndex)
                : Collections.emptyList();

        // 7. 构建响应 - null-safe 处理
        List<Map<String, Object>> safeList = pageResult.stream()
                .map(ApArticle::nullSafeToMap).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("list", safeList);
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
     * 分组洗牌：将排序后的列表按分数区间分组，组内随机打乱。
     * 这样既保留了加权排序的宏观优势（高分文章在前面），
     * 又引入了组内随机性（同分段内非确定性），实现"每次刷新不同结果"。
     */
    private List<ApArticle> groupedShuffle(List<ApArticle> sorted, Map<Long, Double> scoreCache, Random rng) {
        int groupSize = Math.max(sorted.size() / 5, 5); // 至少5个一组，最多分5组
        List<ApArticle> result = new ArrayList<>(sorted.size());

        for (int i = 0; i < sorted.size(); i += groupSize) {
            int end = Math.min(i + groupSize, sorted.size());
            List<ApArticle> group = new ArrayList<>(sorted.subList(i, end));
            Collections.shuffle(group, new Random(rng.nextLong()));
            result.addAll(group);
        }
        return result;
    }

    /**
     * 计算基础加权推荐分数（不含随机噪声）。
     * 权重分配：
     *   score          × 0.25  — 编辑/系统设置的热度分
     *   recencyFactor  × 0.20  — 发布时间越近分越高（7天内线性衰减）
     *   views          × 0.15  — 阅读量（归一化）
     *   likes          × 0.15  — 点赞数（归一化）
     *   comments       × 0.10  — 评论数（归一化）
     *   collections    × 0.10  — 收藏数（归一化）
     */
    private double computeBaseScore(ApArticle article, long now,
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

        return normalizedScore * 0.25
                + recencyFactor * 0.20
                + normalizedViews * 0.15
                + normalizedLikes * 0.15
                + normalizedComments * 0.10
                + normalizedCollections * 0.10;
    }

    private Map<String, Object> buildEmptyResponse(long seed, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", Collections.emptyList());
        result.put("seed", seed);
        result.put("page", page);
        result.put("size", size);
        result.put("hasMore", false);
        result.put("total", 0);
        return result;
    }
}