package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleAutoScanService;
import com.heima.common.aliyun.GreenImageScanPlus;
import com.heima.common.aliyun.GreenTextScanPlus;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticle.Status;
import com.heima.model.article.pojos.ApArticleContent;
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
    private GreenImageScanPlus greenImageScan;

    @Autowired
    private GreenTextScanPlus greenTextScan;

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

        // 文本审核
        boolean isTextScan = handleTextMsg(article.getTitle(), content, article);
        if (!isTextScan) {
            return CompletableFuture.completedFuture(false);
        }

        // 图片审核 - 从内容中提取图片
        if (content != null && !content.isEmpty()) {
            List<String> imageUrls = extractImageUrls(content);
            boolean isImageScan = handleImageMsg(imageUrls, article);
            if (!isImageScan) {
                return CompletableFuture.completedFuture(false);
            }
        }

        // 审核通过，更新状态为已发布
        updateArticle(article, Status.PUBLISHED.getCode(), "");
        log.info("文章审核通过, articleId={}", articleId);
        return CompletableFuture.completedFuture(true);
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
    private boolean handleImageMsg(List<String> imageUrls, ApArticle article) {
        boolean flag = true;
        if (imageUrls == null || imageUrls.isEmpty()) {
            return flag;
        }
        try {
            for (String imageUrl : imageUrls) {
                Map map = greenImageScan.imageScan(imageUrl);
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
        apArticleMapper.updateById(article);
    }
}