package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleAiAnalysisMapper;
import com.heima.article.service.BailianAiService;
import com.heima.common.bailian.DashScopeClient;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleAiAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BailianAiServiceImpl implements BailianAiService {

    @Autowired
    private DashScopeClient dashScopeClient;

    @Autowired
    private ApArticleAiAnalysisMapper aiAnalysisMapper;

    // ==================== 提示词模板 ====================

    private static final String SYSTEM_PROMPT = "你是一个专业的技术文章审核专家，负责对技术社区的文章进行多维度质量评估。请严格按照要求的JSON格式输出分析结果，不要输出任何额外的解释或markdown格式。";

    // 标题与内容相关性分析提示词
    private static final String TITLE_RELEVANCE_PROMPT = 
            "请分析以下文章的标题与内容的相关性，判断是否存在标题党行为。\n\n" +
            "评分标准：\n" +
            "- 90-100分：标题精准概括内容，无任何夸大或误导\n" +
            "- 70-89分：标题基本反映内容，存在轻微修饰但可接受\n" +
            "- 60-69分：标题与内容存在偏差，有一定夸大成分\n" +
            "- 40-59分：标题明显夸大或偏离内容，存在标题党嫌疑\n" +
            "- 0-39分：标题与内容严重不符，完全属于标题党行为\n\n" +
            "请以JSON格式输出：\n" +
            "{\"score\": <0-100的整数>, \"reason\": \"<判断理由，100字以内>\"}\n\n" +
            "标题：%s\n\n内容：%s";

    // 内容质量评分提示词
    private static final String QUALITY_PROMPT = 
            "请对以下技术文章进行内容质量评估，从原创性、逻辑性、表达清晰度三个维度进行评分。\n\n" +
            "评分标准：\n" +
            "原创性（0-100分）：\n" +
            "- 90-100：观点独特，内容原创性强，有深度见解\n" +
            "- 70-89：有一定原创性，能提出独立观点\n" +
            "- 50-69：部分原创，借鉴较多但仍有个人思考\n" +
            "- 0-49：明显拼凑或抄袭，缺乏原创性\n\n" +
            "逻辑性（0-100分）：\n" +
            "- 90-100：结构严谨，论证充分，逻辑清晰\n" +
            "- 70-89：结构合理，逻辑基本通顺\n" +
            "- 50-69：结构松散，逻辑存在跳跃\n" +
            "- 0-49：逻辑混乱，前后矛盾\n\n" +
            "表达清晰度（0-100分）：\n" +
            "- 90-100：语言精炼准确，表达清晰易懂\n" +
            "- 70-89：表达基本清晰，偶有冗余\n" +
            "- 50-69：表达不够清晰，存在语病或冗余\n" +
            "- 0-49：表达混乱，难以理解\n\n" +
            "综合评分 = 原创性*0.4 + 逻辑性*0.3 + 表达清晰度*0.3\n\n" +
            "请以JSON格式输出：\n" +
            "{\"quality_score\": <0-100的整数>, \"originality_score\": <0-100的整数>, \"logic_score\": <0-100的整数>, \"clarity_score\": <0-100的整数>, \"comment\": \"<综合评语，100字以内>\"}\n\n" +
            "标题：%s\n\n内容：%s";

    // 技术相关性判断提示词
    private static final String TECH_RELEVANCE_PROMPT = 
            "请判断以下文章是否属于技术内容。技术内容的定义非常广泛，包括但不限于以下类型：\n\n" +
            "1. 技术硬核类：技术栈分析、源码解读、架构设计、解决方案、性能优化、算法研究、安全攻防等\n" +
            "2. 技术实践类：项目实战、开发经验、工具使用、DevOps、测试方法、代码质量等\n" +
            "3. 技术趋势类：技术选型分析、行业技术动态、技术发展预测、AI/ML技术进展等\n" +
            "4. 技术时政类：技术政策解读、开源社区事件、技术公司动态、技术伦理讨论等\n" +
            "5. 程序员职业类：技术人成长、程序员思维方式、技术管理、团队协作、职业规划等\n" +
            "6. 技术叙事类：程序员代码人生、技术人故事、开发趣事、技术文化等\n\n" +
            "非技术内容示例（不限于此）：纯游戏攻略、纯音乐推荐、纯娱乐八卦、纯生活分享、纯情感故事等\n\n" +
            "请以JSON格式输出：\n" +
            "{\"is_tech\": true/false, \"confidence\": <0.0-1.0的浮点数, 置信度>, \"reason\": \"<判断理由，50字以内>\"}\n\n" +
            "标题：%s\n\n内容：%s";

    @Override
    public Map<String, Object> analyzeArticle(ApArticle article, String content) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("titleRelevanceScore", 0);
        result.put("qualityScore", 0);
        result.put("isTechContent", true);

        if (content == null || content.isEmpty()) {
            log.warn("Article content is empty for articleId={}", article.getId());
            return result;
        }

        // 截断内容，避免token超限
        String truncatedContent = content.length() > 4000 ? content.substring(0, 4000) : content;
        String title = article.getTitle() != null ? article.getTitle() : "";

        ApArticleAiAnalysis analysis = new ApArticleAiAnalysis();
        analysis.setArticleId(article.getId());
        analysis.setCreatedTime(new Date());

        StringBuilder rawResponses = new StringBuilder();

        try {
            // 1. 标题相关性分析
            log.info("Starting title relevance analysis for articleId={}", article.getId());
            String titlePrompt = String.format(TITLE_RELEVANCE_PROMPT, title, truncatedContent);
            String titleResponse = dashScopeClient.callGeneration(SYSTEM_PROMPT, titlePrompt);
            
            if (titleResponse != null) {
                rawResponses.append("===TITLE_RELEVANCE===\n").append(titleResponse).append("\n\n");
                JSONObject titleJson = parseJsonResponse(titleResponse);
                if (titleJson != null) {
                    analysis.setTitleRelevanceScore(titleJson.getInteger("score"));
                    analysis.setTitleRelevanceReason(titleJson.getString("reason"));
                    result.put("titleRelevanceScore", titleJson.getInteger("score"));
                }
            }

            // 2. 内容质量评分
            log.info("Starting quality analysis for articleId={}", article.getId());
            String qualityPrompt = String.format(QUALITY_PROMPT, title, truncatedContent);
            String qualityResponse = dashScopeClient.callGeneration(SYSTEM_PROMPT, qualityPrompt);
            
            if (qualityResponse != null) {
                rawResponses.append("===QUALITY===\n").append(qualityResponse).append("\n\n");
                JSONObject qualityJson = parseJsonResponse(qualityResponse);
                if (qualityJson != null) {
                    analysis.setQualityScore(qualityJson.getInteger("quality_score"));
                    analysis.setOriginalityScore(qualityJson.getInteger("originality_score"));
                    analysis.setLogicScore(qualityJson.getInteger("logic_score"));
                    analysis.setClarityScore(qualityJson.getInteger("clarity_score"));
                    analysis.setQualityComment(qualityJson.getString("comment"));
                    result.put("qualityScore", qualityJson.getInteger("quality_score"));
                }
            }

            // 3. 技术相关性判断
            log.info("Starting tech relevance analysis for articleId={}", article.getId());
            String techPrompt = String.format(TECH_RELEVANCE_PROMPT, title, truncatedContent);
            String techResponse = dashScopeClient.callGeneration(SYSTEM_PROMPT, techPrompt);
            
            if (techResponse != null) {
                rawResponses.append("===TECH_RELEVANCE===\n").append(techResponse).append("\n\n");
                JSONObject techJson = parseJsonResponse(techResponse);
                if (techJson != null) {
                    analysis.setIsTechContent(techJson.getBoolean("is_tech"));
                    if (techJson.get("confidence") != null) {
                        analysis.setTechConfidence(BigDecimal.valueOf(techJson.getDouble("confidence")));
                    }
                    result.put("isTechContent", techJson.getBoolean("is_tech"));
                }
            }

            // 保存原始响应
            analysis.setRawResponse(rawResponses.toString());

            // 持久化分析结果
            saveAnalysis(analysis);
            
            result.put("success", true);
            log.info("AI analysis completed for articleId={}, qualityScore={}, isTech={}", 
                    article.getId(), analysis.getQualityScore(), analysis.getIsTechContent());

        } catch (Exception e) {
            log.error("AI analysis failed for articleId={}: {}", article.getId(), e.getMessage(), e);
            // 分析失败不阻塞流程
        }

        return result;
    }

    /**
     * 从AI响应中提取JSON
     */
    private JSONObject parseJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        try {
            // 尝试直接解析
            String cleaned = response.trim();
            // 移除可能的markdown代码块标记
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            // 尝试提取JSON对象
            Pattern pattern = Pattern.compile("\\{[^{}]*\\}");
            Matcher matcher = pattern.matcher(cleaned);
            if (matcher.find()) {
                return JSON.parseObject(matcher.group());
            }
            return JSON.parseObject(cleaned);
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON: {}", response.substring(0, Math.min(200, response.length())));
            return null;
        }
    }

    /**
     * 保存分析结果
     */
    private void saveAnalysis(ApArticleAiAnalysis analysis) {
        try {
            // 先删除旧记录
            QueryWrapper<ApArticleAiAnalysis> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("article_id", analysis.getArticleId());
            aiAnalysisMapper.delete(deleteWrapper);
            // 插入新记录
            aiAnalysisMapper.insert(analysis);
        } catch (Exception e) {
            log.error("Failed to save AI analysis for articleId={}: {}", analysis.getArticleId(), e.getMessage());
        }
    }
}