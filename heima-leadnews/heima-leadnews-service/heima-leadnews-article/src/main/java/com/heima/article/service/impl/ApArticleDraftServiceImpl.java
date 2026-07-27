package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleDraftMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleDraftService;
import com.heima.article.service.ArticleAutoScanService;
import com.heima.article.service.ArticleTaskService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApArticleDraft;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class ApArticleDraftServiceImpl extends ServiceImpl<ApArticleDraftMapper, ApArticleDraft> implements ApArticleDraftService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private ArticleAutoScanService articleAutoScanService;

    @Autowired
    private ArticleTaskService articleTaskService;

    @Override
    @Transactional
    public ResponseResult createDraft(ApArticleDraft draft) {
        ApUser user = AppThreadLocalUtil.getUser();
        draft.setCreatedTime(new Date());
        draft.setUpdatedTime(new Date());
        draft.setAuthorId(user.getId().longValue());
        save(draft);
        log.info("草稿创建成功, draftId: {}", draft.getId());
        return ResponseResult.okResult(draft);
    }

    @Override
    @Transactional
    public ResponseResult updateDraft(ApArticleDraft draft) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (draft.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "草稿ID不能为空");
        }
        ApArticleDraft existing = getById(draft.getId());
        if (existing == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "草稿不存在");
        }
        draft.setUpdatedTime(new Date());
        if (draft.getAuthorId() == null) {
            draft.setAuthorId(user.getId().longValue());
        }
        updateById(draft);
        return ResponseResult.okResult(draft);
    }

    @Override
    @Transactional
    public ResponseResult publishFromDraft(Long draftId) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (draftId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "草稿ID不能为空");
        }
        ApArticleDraft draft = getById(draftId);
        if (draft == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "草稿不存在");
        }

        // 从草稿直接创建 ap_article 记录
        ApArticle article = new ApArticle();
        article.setTitle(draft.getTitle());
        article.setAuthorId(draft.getAuthorId() != null ? draft.getAuthorId() : user.getId().longValue());
        article.setChannelId(draft.getChannelId());
        article.setLayout(draft.getLayout() != null ? draft.getLayout().byteValue() : (byte) 0);
        article.setCoverImage(draft.getCoverImage());
        article.setTags(draft.getTags());
        article.setCreatedTime(new Date());
        // 获取发布时间，如果为null（不延迟时）则与创建时间相等
        article.setPublishTime(draft.getPublishTime() != null ? draft.getPublishTime() : new Date());
        article.setStatus(ApArticle.Status.SUBMIT.getCode()); // 审核中

        // 查询作者信息
        ApUser apUser = AppThreadLocalUtil.getUser();
        if (apUser != null) {
            article.setAuthorName(apUser.getNickname());
            article.setAuthorImage(apUser.getImage());
        }

        apArticleMapper.insert(article);

        // 保存文章配置
        ApArticleConfig apArticleConfig = new ApArticleConfig(article.getId());
        apArticleConfigMapper.insert(apArticleConfig);

        // 保存文章内容
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(article.getId());
        apArticleContent.setContent(draft.getContent());
        apArticleContentMapper.insert(apArticleContent);

        log.info("从草稿发布文章成功, draftId: {}, articleId: {}", draftId, article.getId());

        // 删除草稿
        removeById(draftId);

        // 异步提交审核
        articleAutoScanService.autoScanArticle(article.getId());

        // 如果是延迟发布，添加到调度任务
//        if (draft.getPublishTime() != null && draft.getPublishTime().after(new Date())) {
//            articleTaskService.addArticleToTask(article.getId(), draft.getPublishTime());
//        }

        // 不管是不是延迟发布，都添加到调度任务，只不过不延迟时interval：0，多经过了Task任务类流转
        articleTaskService.addArticleToTask(article.getId(), article.getPublishTime());

        return ResponseResult.okResult(article);
    }

    @Override
    public ResponseResult getDraftById(Long id) {
        ApArticleDraft draft = getById(id);
        if (draft == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.okResult(draft);
    }

    @Override
    public ResponseResult listDrafts(Long authorId, Integer page, Integer size) {
        Page<ApArticleDraft> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApArticleDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(authorId != null, ApArticleDraft::getAuthorId, authorId);
        queryWrapper.orderByDesc(ApArticleDraft::getUpdatedTime);
        IPage<ApArticleDraft> result = page(pageParam, queryWrapper);
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult deleteDraft(Long id) {
        removeById(id);
        return ResponseResult.okResult();
    }
}