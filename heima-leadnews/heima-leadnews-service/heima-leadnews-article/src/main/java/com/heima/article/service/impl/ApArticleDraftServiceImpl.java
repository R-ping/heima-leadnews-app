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
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApArticleDraft;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 初始化统计字段默认值
        article.setViews(0);
        article.setLikes(0);
        article.setCollection(0);
        article.setComment(0);
        article.setScore(0);

        // 查询作者信息
        ApUser apUser = AppThreadLocalUtil.getUser();
        if (apUser != null) {
            article.setAuthorName(apUser.getNickname() != null ? apUser.getNickname() : "");
            article.setAuthorImage(apUser.getImage() != null ? apUser.getImage() : "");
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
        // 不能直接抛异常触发回滚，因为有用户申诉业务（其实就是一个专门的“反馈 & 建议”的沸点的圈子），如果直接回滚掉整个文章信息，用户申诉成功也拿不回文章了
        // 审核失败还缺少站内信的“系统通知”业务逻辑，如“你的文章 你知道的，我们上午是不写代码的 因违反社区规范已被删除， 详细规则请见  社区规范
        // 文章内容: 存在色情低俗内容，建议删除带有低俗导向的图片/文字”
        // 审核成功发布，需要通知等级服务，是否满足增加经验要求，如每天发2篇文章+10经验等，然后经验是否满足等级升级，升级后解锁相关权限或赠送“钻石”物品。
        // 异步提交审核，审核结果由 ArticleAutoScanService 内部处理（经验值、通知、状态更新等）
        // 不再同步等待审核结果，直接返回成功
        articleAutoScanService.autoScanArticle(article.getId());
        log.info("文章已提交审核（异步）, articleId: {}", article.getId());

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