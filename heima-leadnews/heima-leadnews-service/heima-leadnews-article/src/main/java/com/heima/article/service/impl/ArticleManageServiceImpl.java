package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleManageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleManageServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ArticleManageService {

    @Override
    public ResponseResult list(Long authorId, Integer page, Integer size, String status, String title) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = authorId != null ? authorId : user.getId().longValue();
        Page<ApArticle> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        if (status != null && !status.isEmpty()) {
            Byte statusCode = getStatusCode(status);
            if (statusCode != null) {
                wrapper.eq(ApArticle::getStatus, statusCode);
            }
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like(ApArticle::getTitle, title);
        }
        wrapper.orderByDesc(ApArticle::getCreatedTime);
        IPage<ApArticle> result = page(pageParam, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords().stream().map(ApArticle::nullSafeToMap).collect(Collectors.toList()));
        data.put("total", result.getTotal());
        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult statistics(Long authorId) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = authorId != null ? authorId : user.getId().longValue();
        Map<String, Object> data = new HashMap<>();
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        data.put("total", count(wrapper));
        wrapper.eq(ApArticle::getStatus, ApArticle.Status.PUBLISHED.getCode());
        data.put("published", count(wrapper));
        wrapper.clear();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        wrapper.eq(ApArticle::getStatus, ApArticle.Status.SUBMIT.getCode());
        data.put("reviewing", count(wrapper));
        wrapper.clear();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        wrapper.eq(ApArticle::getStatus, ApArticle.Status.FAIL.getCode());
        data.put("rejected", count(wrapper));
        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional
    public ResponseResult deleteArticle(Long id) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "id 不能为空");
        }
        ApArticle article = getById(id);
        if (article == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (!article.getAuthorId().equals(user.getId().longValue())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        article.setIsDeleted(true);
        updateById(article);
        return ResponseResult.okResult();
    }

    private Byte getStatusCode(String status) {
        switch (status) {
            case "published":
                return ApArticle.Status.PUBLISHED.getCode();
            case "reviewing":
                return ApArticle.Status.SUBMIT.getCode();
            case "rejected":
                return ApArticle.Status.FAIL.getCode();
            default:
                return null;
        }
    }
}
