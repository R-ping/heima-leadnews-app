package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApCommentLikeMapper;
import com.heima.article.mapper.ApCommentMapper;
import com.heima.article.service.ApCommentService;
import com.heima.model.article.dtos.CommentDto;
import com.heima.model.article.pojos.ApComment;
import com.heima.model.article.pojos.ApCommentLike;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.thread.AppThreadLocalUtil;
import com.heima.model.user.pojos.ApUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApCommentServiceImpl extends ServiceImpl<ApCommentMapper, ApComment> implements ApCommentService {

    @Autowired
    private ApCommentLikeMapper apCommentLikeMapper;

    @Override
    public ResponseResult getCommentList(CommentDto dto) {
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        int page = dto.getPage() != null ? dto.getPage() : 1;
        int size = dto.getSize() != null ? dto.getSize() : 3;

        // 查询一级评论：parentId IS NULL
        LambdaQueryWrapper<ApComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApComment::getArticleId, dto.getArticleId())
               .isNull(ApComment::getParentId)
               .orderByDesc(ApComment::getCreatedTime);

        // 分页
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + "," + size);

        List<ApComment> topComments = list(wrapper);
        if (topComments.isEmpty()) {
            return ResponseResult.okResult(Collections.emptyList());
        }

        // 查询每个一级评论的子评论（最多2条）
        List<Long> parentIds = topComments.stream().map(ApComment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<ApComment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.in(ApComment::getParentId, parentIds)
                    .orderByAsc(ApComment::getCreatedTime)
                    .last("LIMIT " + (parentIds.size() * 2)); // 粗略限制

        List<ApComment> allChildren = list(childWrapper);

        // 按 parentId 分组，每组最多2条
        Map<Long, List<ApComment>> childrenMap = new HashMap<>();
        for (ApComment child : allChildren) {
            childrenMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(child);
        }
        // 限制每组最多2条
        childrenMap.forEach((parentId, children) -> {
            if (children.size() > 2) {
                childrenMap.put(parentId, children.subList(0, 2));
            }
        });

        // 获取当前用户ID（用于判断点赞状态）
        Integer currentUserId = getCurrentUserId();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ApComment top : topComments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", top.getId());
            item.put("articleId", top.getArticleId());
            item.put("userId", top.getUserId());
            item.put("userName", top.getUserName());
            item.put("userAvatar", top.getUserAvatar());
            item.put("content", top.getContent());
            item.put("likeCount", top.getLikeCount() != null ? top.getLikeCount() : 0);
            item.put("replyCount", top.getReplyCount() != null ? top.getReplyCount() : 0);
            item.put("createdTime", top.getCreatedTime());
            item.put("liked", isLiked(top.getId(), currentUserId));

            List<ApComment> children = childrenMap.getOrDefault(top.getId(), Collections.emptyList());
            List<Map<String, Object>> childList = children.stream().map(child -> {
                Map<String, Object> cm = new HashMap<>();
                cm.put("id", child.getId());
                cm.put("userId", child.getUserId());
                cm.put("userName", child.getUserName());
                cm.put("userAvatar", child.getUserAvatar());
                cm.put("content", child.getContent());
                cm.put("likeCount", child.getLikeCount() != null ? child.getLikeCount() : 0);
                cm.put("parentId", child.getParentId());
                cm.put("createdTime", child.getCreatedTime());
                cm.put("liked", isLiked(child.getId(), currentUserId));
                return cm;
            }).collect(Collectors.toList());
            item.put("children", childList);
            result.add(item);
        }

        return ResponseResult.okResult(result);
    }

    @Override
    @Transactional
    public ResponseResult addComment(CommentDto dto) {
        if (dto == null || dto.getArticleId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "请输入评论内容");
        }
        if (dto.getContent().length() > 1000) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能超过1000字");
        }

        ApUser user = getCurrentUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        ApComment comment = new ApComment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(user.getId());
        comment.setUserName(user.getNickname() != null ? user.getNickname() : "用户");
        comment.setUserAvatar(user.getImage() != null ? user.getImage() : "");
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent().trim());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreatedTime(new Date());

        save(comment);

        // 如果是二级评论，更新父评论的回复数
        if (dto.getParentId() != null) {
            ApComment parent = getById(dto.getParentId());
            if (parent != null) {
                parent.setReplyCount((parent.getReplyCount() != null ? parent.getReplyCount() : 0) + 1);
                updateById(parent);
            }
        }

        return ResponseResult.okResult(comment);
    }

    @Override
    @Transactional
    public ResponseResult likeComment(CommentDto dto) {
        if (dto == null || dto.getCommentId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = getCurrentUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        ApComment comment = getById(dto.getCommentId());
        if (comment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "评论不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<ApCommentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(ApCommentLike::getCommentId, dto.getCommentId())
                   .eq(ApCommentLike::getUserId, user.getId());
        ApCommentLike existingLike = apCommentLikeMapper.selectOne(likeWrapper);

        if (existingLike != null) {
            // 已点赞，取消点赞
            apCommentLikeMapper.deleteById(existingLike.getId());
            comment.setLikeCount(Math.max(0, (comment.getLikeCount() != null ? comment.getLikeCount() : 1) - 1));
            updateById(comment);
            return ResponseResult.okResult(Map.of("liked", false, "likeCount", comment.getLikeCount()));
        } else {
            // 点赞
            ApCommentLike like = new ApCommentLike();
            like.setCommentId(dto.getCommentId());
            like.setUserId(user.getId());
            like.setCreatedTime(new Date());
            apCommentLikeMapper.insert(like);
            comment.setLikeCount((comment.getLikeCount() != null ? comment.getLikeCount() : 0) + 1);
            updateById(comment);
            return ResponseResult.okResult(Map.of("liked", true, "likeCount", comment.getLikeCount()));
        }
    }

    @Override
    public ResponseResult getCommentById(Long id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApComment comment = getById(id);
        if (comment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "评论不存在");
        }
        return ResponseResult.okResult(comment);
    }

    private boolean isLiked(Long commentId, Integer userId) {
        if (userId == null) return false;
        LambdaQueryWrapper<ApCommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApCommentLike::getCommentId, commentId)
               .eq(ApCommentLike::getUserId, userId);
        return apCommentLikeMapper.selectCount(wrapper) > 0;
    }

    private Integer getCurrentUserId() {
        try {
            ApUser user = AppThreadLocalUtil.getUser();
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private ApUser getCurrentUser() {
        try {
            return AppThreadLocalUtil.getUser();
        } catch (Exception e) {
            return null;
        }
    }
}