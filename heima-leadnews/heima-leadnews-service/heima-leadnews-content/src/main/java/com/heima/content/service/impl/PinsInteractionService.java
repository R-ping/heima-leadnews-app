package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.content.mapper.ApPinsCommentMapper;
import com.heima.content.mapper.ApPinsLikeMapper;
import com.heima.content.mapper.ApPinsMapper;
import com.heima.model.article.dtos.PinsCommentDTO;
import com.heima.model.article.dtos.PinsLikeDTO;
import com.heima.model.article.dtos.PinsShareDTO;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.article.pojos.ApPinsComment;
import com.heima.model.article.pojos.ApPinsLike;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Slf4j
public class PinsInteractionService {

    @Autowired
    private ApPinsMapper apPinsMapper;

    @Autowired
    private ApPinsLikeMapper apPinsLikeMapper;

    @Autowired
    private ApPinsCommentMapper apPinsCommentMapper;

    @Transactional(rollbackFor = Exception.class)
    public ResponseResult like(PinsLikeDTO dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }

        LambdaQueryWrapper<ApPinsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPinsLike::getPinsId, dto.getPinsId());
        wrapper.eq(ApPinsLike::getUserId, user.getId());
        ApPinsLike existLike = apPinsLikeMapper.selectOne(wrapper);

        if (Boolean.TRUE.equals(dto.getLiked())) {
            // 点赞
            if (existLike != null) {
                return ResponseResult.okResult();
            }
            ApPinsLike like = new ApPinsLike();
            like.setPinsId(dto.getPinsId());
            like.setUserId(user.getId());
            like.setCreatedTime(new Date());
            apPinsLikeMapper.insert(like);
            // 更新沸点点赞数
            ApPins pins = apPinsMapper.selectById(dto.getPinsId());
            if (pins != null) {
                pins.setLikes((pins.getLikes() != null ? pins.getLikes() : 0) + 1);
                apPinsMapper.updateById(pins);
            }
        } else {
            // 取消点赞
            if (existLike == null) {
                return ResponseResult.okResult();
            }
            apPinsLikeMapper.deleteById(existLike.getId());
            ApPins pins = apPinsMapper.selectById(dto.getPinsId());
            if (pins != null) {
                int newLikes = Math.max(0, (pins.getLikes() != null ? pins.getLikes() : 0) - 1);
                pins.setLikes(newLikes);
                apPinsMapper.updateById(pins);
            }
        }
        return ResponseResult.okResult();
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseResult createComment(PinsCommentDTO dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能为空");
        }

        ApPinsComment comment = new ApPinsComment();
        comment.setPinsId(dto.getPinsId());
        comment.setUserId(user.getId());
        comment.setUserName(user.getNickname() != null ? user.getNickname() : "");
        comment.setUserAvatar(user.getImage() != null ? user.getImage() : "");
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreatedTime(new Date());
        apPinsCommentMapper.insert(comment);

        // 更新沸点评论数
        ApPins pins = apPinsMapper.selectById(dto.getPinsId());
        if (pins != null) {
            pins.setComment((pins.getComment() != null ? pins.getComment() : 0) + 1);
            apPinsMapper.updateById(pins);
        }

        // 如果是回复，更新父评论的回复数
        if (dto.getParentId() != null) {
            ApPinsComment parentComment = apPinsCommentMapper.selectById(dto.getParentId());
            if (parentComment != null) {
                parentComment.setReplyCount((parentComment.getReplyCount() != null ? parentComment.getReplyCount() : 0) + 1);
                apPinsCommentMapper.updateById(parentComment);
            }
        }

        return ResponseResult.okResult(comment);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseResult share(PinsShareDTO dto) {
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        ApPins pins = apPinsMapper.selectById(dto.getPinsId());
        if (pins == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        // 原子递增分享数
        apPinsMapper.incrementShare(dto.getPinsId());
        return ResponseResult.okResult();
    }
}