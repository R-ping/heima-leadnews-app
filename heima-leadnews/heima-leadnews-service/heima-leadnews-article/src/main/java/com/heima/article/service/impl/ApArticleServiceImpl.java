package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    private final static short MAX_PAGE_SIZE = 50;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ApArticleEventMapper apArticleEventMapper;

    /**
     * 加载文章列表
     *
     * @param type 1 加载更多   2 加载最新
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        //1.检验参数
        //分页条数的校验
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        //分页的值不超过50
        size = Math.min(size, MAX_PAGE_SIZE);
        dto.setSize(size);
        //校验参数  -->type
        if (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }
        //频道参数校验
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        //时间校验
        if (dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }
        //2.查询
        List<ApArticle> articleList = apArticleMapper.loadArticleList(dto, type);
        //3.结果返回
        return ResponseResult.okResult(articleList);
    }

    /**
     * 保存app端相关文章
     */
    @Override
    @Transactional
    public ResponseResult saveArticle(ArticleDto dto, long executeTimeInterval) {
        //1.检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);
        try {
            //2.判断是否存在id
            if (dto.getId() == null) {
                //2.1 不存在id  保存  文章  文章配置  文章内容
                save(apArticle);
                ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
                ApArticleContent apArticleContent = new ApArticleContent();
                apArticleContent.setArticleId(apArticle.getId());
                apArticleContent.setContent(dto.getContent());
                apArticleConfigMapper.insert(apArticleConfig);
                apArticleContentMapper.insert(apArticleContent);
            } else {
                //2.2 存在id   修改  文章  文章内容
                updateById(apArticle);
                ApArticleContent apArticleContent = new ApArticleContent();
                apArticleContent.setContent(dto.getContent());
                apArticleContentMapper.update(apArticleContent,
                    new QueryWrapper<ApArticleContent>().eq("article_id", apArticle.getId()));
            }
            // 本地消息表入库（事务内）
            ArticleEvent event = buildArticleEvent();
            event.setArticleId(apArticle.getId());
            apArticleEventMapper.insertArticleEvent(event);
        } catch (Exception e) {
            log.error("文章保存失败", e);
            throw new RuntimeException(e);
        }
        // 异步操作移到事务提交后，避免事务边界问题
        articleFreemarkerService.buildHTMLAndSend(apArticle, "", executeTimeInterval);
        // 结果返回 文章的id
        return ResponseResult.okResult(apArticle.getId());
    }


    /**
     * 根据文章id生成文章事件,后续进行mq异步处理
     */
    @Override
    public boolean generateArticleEvent(ApArticle article, long lastExecuteInterval) {
        //1.检查参数
        if (article == null) {
            log.error("文章保存失败，参数为空");
            return false;
        }
        try {
            // 本地消息表入库（事务内）
            ArticleEvent event = buildArticleEvent();
            event.setArticleId(article.getId());
            apArticleEventMapper.insertArticleEvent(event);
            log.info("文章本地消息表保存成功，文章id：{}", article.getId());
        } catch (Exception e) {
            log.error("文章保存失败", e);
            return false;
        }
        // 异步操作移到事务提交后，避免事务边界问题
        articleFreemarkerService.buildHTMLAndSend(article, "", lastExecuteInterval);
        return true;
    }


    /**
     * 构建文章事件
     */
    private static ArticleEvent buildArticleEvent() {
        ArticleEvent event = new ArticleEvent();
        event.setCreateTime(new Date());
        event.setUpdateTime(new Date());
        return event;
    }

    @Override
    public void updateScore(ArticleVisitStreamMess message) {
        ApArticle apArticle1 = new ApArticle();
        apArticle1.setCollection(message.getCollect());
        apArticle1.setComment(message.getComment());
        apArticle1.setLikes(message.getLike());
        apArticle1.setViews(message.getView());
        int newScore = computeScore(apArticle1) * 3;
        ApArticle apArticle2 = getById(message.getArticleId());
        if (apArticle2 == null) {
            log.warn("updateScore: article not found, id={}", message.getArticleId());
            return;
        }
        //1.更新文章的阅读、点赞、收藏、评论的数量
        int oldScore = computeScore(apArticle2);
        updateArticle(message);
        //2.计算文章的分值并持久化
        int resultScore = newScore + oldScore;
        ApArticle updateScore = new ApArticle();
        updateScore.setId(message.getArticleId());
        updateScore.setScore(resultScore);
        updateById(updateScore);
    }

    @Override
    public List<ApArticle> listByAuthorId(ArticleDto dto) {
        // 构建查询条件
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();

        // 固定条件：作者ID（如果必填）
        wrapper.eq(dto.getAuthorId() != null, ApArticle::getAuthorId, dto.getAuthorId());

        // 可选条件：频道ID
        wrapper.eq(dto.getChannelId() != null, ApArticle::getChannelId, dto.getChannelId());

        // 可选条件：JSON 标签重叠查询（修复点：将 List 转为 JSON 字符串）
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            String tagsJson = JSON.toJSONString(dto.getTags()); // 得到 ["44","45"]
            wrapper.apply("JSON_OVERLAPS(tags, {0})", tagsJson);
        }

        // 可选条件：删除状态
        wrapper.eq(dto.getIsDeleted() != null, ApArticle::getIsDeleted, dto.getIsDeleted());

        return list(wrapper);
    }

    /**
     * 更新文章行为数量
     */
    private void updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection() == null ? 0 : apArticle.getCollection() + mess.getCollect());
        apArticle.setComment(apArticle.getComment() == null ? 0 : apArticle.getComment() + mess.getComment());
        apArticle.setLikes(apArticle.getLikes() == null ? 0 : apArticle.getLikes() + mess.getLike());
        apArticle.setViews(apArticle.getViews() == null ? 0 : apArticle.getViews() + mess.getView());
        updateById(apArticle);

    }

    /**
     * 计算文章的具体分值
     */
    private Integer computeScore(ApArticle apArticle) {
        int score = 0;
        if (apArticle.getLikes() != null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null) {
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null) {
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }
}
