package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleAutoScanService;
import com.heima.article.service.ArticleSimilarityService;
import com.heima.article.service.BailianAiService;
import com.heima.article.service.LevelService;
import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.common.aliyun.GreenTextScanPlus;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticle.Status;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApArticleDraft.ContPic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@Transactional
public class ArticleAutoScanServiceImpl implements ArticleAutoScanService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private GreenImageScanPlus greenImageScan;

    @Autowired
    private GreenTextScanPlus greenTextScan;

    @Autowired
    private BailianAiService bailianAiService;

    @Autowired
    private ArticleSimilarityService articleSimilarityService;

    @Autowired
    private LevelService levelService;

    @Override
    @Async
    public CompletableFuture<Boolean> autoScanArticle(Long articleId) {
        ApArticle article = apArticleMapper.selectById(articleId);
        if (article == null) {
            log.error("ArticleAutoScanServiceImpl-文章不存在, articleId={}", articleId);
            return CompletableFuture.completedFuture(false);
        }

        if (!article.getStatus().equals(Status.SUBMIT.getCode())) {
            log.info("文章状态非审核中，跳过审核, articleId={}, status={}", articleId, article.getStatus());
            return CompletableFuture.completedFuture(true);
        }

        // 从文章内容表获取内容
        QueryWrapper<ApArticleContent> contentQuery = new QueryWrapper<>();
        contentQuery.eq("article_id", articleId);
        ApArticleContent articleContent = apArticleContentMapper.selectOne(contentQuery);
        String content = articleContent != null ? articleContent.getContent() : "";

        // 1. 文本审核
        boolean isTextScan = handleTextMsg(article.getTitle(), content, article);
        if (!isTextScan) {
            return CompletableFuture.completedFuture(false);
        }

        // 2. 图片审核 - 从内容中提取图片
        if (content != null && !content.isEmpty()) {
            List<ContPic> contPics = article.getContPics();
            if (contPics == null) {
                contPics = new ArrayList<>();
            }
            String coverImage = article.getCoverImage();
            if (coverImage != null && !coverImage.isEmpty()) {
                ContPic contPic = new ContPic();
                contPic.setPicUrl(coverImage);
                contPics.add(contPic);
            }
            boolean isImageScan = handleImageMsg(contPics, article);
            if (!isImageScan) {
                return CompletableFuture.completedFuture(false);
            }
        }

        // 3. AI内容分析（百炼平台）
        log.info("开始AI内容分析, articleId={}", articleId);
        Map<String, Object> aiResult = null;
        try {
            aiResult = bailianAiService.analyzeArticle(article, content);
        } catch (Exception e) {
            log.error("AI内容分析异常, articleId={}, 将降级为仅通过内容安全审核", articleId, e);
        }

        // 4. RAG相似度检验
        boolean isHighSimilarity = false;
        try {
            if (content != null && !content.isEmpty()) {
                Map<String, Object> similarityResult = articleSimilarityService.checkSimilarity(article, content);
                if (similarityResult != null && Boolean.TRUE.equals(similarityResult.get("isSimilar"))) {
                    isHighSimilarity = true;
                    log.info("检测到高相似度文章, articleId={}, similarity={}", 
                            articleId, similarityResult.get("maxSimilarity"));
                }
            }
        } catch (Exception e) {
            log.error("RAG相似度检验异常, articleId={}", articleId, e);
        }

        // 5. 更新文章配置（is_recommend字段）
        updateArticleConfig(articleId, !isHighSimilarity);

        // 6. 计算逐力值加成（根据AI质量评分）
        if (aiResult != null && Boolean.TRUE.equals(aiResult.get("success"))) {
            Integer qualityScore = (Integer) aiResult.get("qualityScore");
            if (qualityScore != null) {
                calculatePowerBonus(article, qualityScore);
            }
        }

        log.info("文章审核完成, articleId={}, isHighSimilarity={}", articleId, isHighSimilarity);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * 更新文章配置的推荐状态
     */
    private void updateArticleConfig(Long articleId, boolean isRecommend) {
        try {
            QueryWrapper<ApArticleConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("article_id", articleId);
            ApArticleConfig config = apArticleConfigMapper.selectOne(queryWrapper);
            if (config == null) {
                config = new ApArticleConfig(articleId);
                config.setIsRecommend(isRecommend);
                apArticleConfigMapper.insert(config);
            } else {
                config.setIsRecommend(isRecommend);
                apArticleConfigMapper.updateById(config);
            }
            log.info("更新文章推荐状态, articleId={}, isRecommend={}", articleId, isRecommend);
        } catch (Exception e) {
            log.error("更新文章配置失败, articleId={}", articleId, e);
        }
    }

    /**
     * 根据AI质量评分计算逐力值加成
     * 80分以上+3，60-79分+1，60分以下+0
     */
    private void calculatePowerBonus(ApArticle article, Integer qualityScore) {
        try {
            int powerBonus = 0;
            if (qualityScore >= 80) {
                powerBonus = 3;
            } else if (qualityScore >= 60) {
                powerBonus = 1;
            }

            if (powerBonus > 0 && article.getAuthorId() != null) {
                Map<String, Object> powerResult = levelService.calculatePowerWithLimit(
                        article.getAuthorId(), article.getId(), "ai_quality_score", powerBonus);
                
                if (powerResult != null && Boolean.TRUE.equals(powerResult.get("success"))) {
                    log.info("AI质量评分逐力值加成, articleId={}, authorId={}, qualityScore={}, powerBonus={}", 
                            article.getId(), article.getAuthorId(), qualityScore, powerBonus);
                    
                    // 检查是否达到4级，自动推荐到首页
                    Integer newLevel = (Integer) powerResult.get("newLevel");
                    if (newLevel != null && newLevel >= 4) {
                        updateArticleConfig(article.getId(), true);
                        log.info("作者逐力值达到4级，文章自动推荐到首页, articleId={}, authorId={}, level={}", 
                                article.getId(), article.getAuthorId(), newLevel);
                    }
                }
            }
        } catch (Exception e) {
            log.error("逐力值计算异常, articleId={}", article.getId(), e);
        }
    }

    /**
     * 从 Markdown 内容中提取图片 URL
     */
    private List<String> extractImageUrls(String content) {
        List<String> images = new ArrayList<>();
        Pattern imgPattern = Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
        Matcher imgMatcher = imgPattern.matcher(content);
        while (imgMatcher.find()) {
            images.add(imgMatcher.group(1));
        }
        // 也提取 HTML img 标签
        Pattern htmlImgPattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']");
        Matcher htmlImgMatcher = htmlImgPattern.matcher(content);
        while (htmlImgMatcher.find()) {
            images.add(htmlImgMatcher.group(1));
        }
        return images;
    }

    /**
     * 审核图片
     */
    private boolean handleImageMsg(List<ContPic> imageUrls, ApArticle article) {
        boolean flag = true;
        if (imageUrls == null || imageUrls.isEmpty()) {
            return flag;
        }
        try {
            for (ContPic contPic : imageUrls) {
                Map map = greenImageScan.imageScan(contPic.getPicUrl());
                if (map != null) {
                    if (map.get("level").equals("high")) {
                        flag = false;
                        updateArticle(article, Status.FAIL.getCode(), "当前文章中的图片存在违规内容");
                        return flag;
                    }
                    if (map.get("level").equals("medium")) {
                        flag = false;
                        updateArticle(article, Status.FAIL.getCode(), "当前文章中的图片存在不确定内容");
                        return flag;
                    }
                }
            }
        } catch (Exception e) {
            flag = false;
            log.error("图片审核异常", e);
        }
        return flag;
    }

    /**
     * 审核纯文本内容
     */
    private boolean handleTextMsg(String title, String content, ApArticle article) {
        boolean flag = true;
        String textToScan = (title != null ? title : "") + "-" + (content != null ? content : "");
        if (textToScan.length() <= 1) {
            return flag;
        }
        try {
            Map map = greenTextScan.greeTextScan(textToScan);
            if (map != null) {
                if (map.get("level").equals("high")) {
                    flag = false;
                    updateArticle(article, Status.FAIL.getCode(), "当前文章中的内容存在违规内容");
                }
                if (map.get("level").equals("medium")) {
                    flag = false;
                    updateArticle(article, Status.FAIL.getCode(), "当前文章中的内容存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            log.error("文本审核异常", e);
        }
        return flag;
    }

    /**
     * 更新文章状态
     */
    private void updateArticle(ApArticle article, byte status, String reason) {
        article.setStatus(status);
        article.setReason(reason);
        if (status == Status.FAIL.getCode() && !article.getIsDeleted()) {
            article.setIsDeleted(true);
        }
        apArticleMapper.updateById(article);
    }
}