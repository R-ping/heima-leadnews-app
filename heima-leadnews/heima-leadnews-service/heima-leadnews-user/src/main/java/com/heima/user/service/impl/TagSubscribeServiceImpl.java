package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.SysTag;
import com.heima.model.user.pojos.UserTagRelation;
import com.heima.model.user.vo.TagDiscoverVO;
import com.heima.user.mapper.SysTagMapper;
import com.heima.user.mapper.UserTagRelationMapper;
import com.heima.user.service.TagSubscribeService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TagSubscribeServiceImpl implements TagSubscribeService {

    @Autowired
    private SysTagMapper sysTagMapper;

    @Autowired
    private UserTagRelationMapper userTagRelationMapper;

    @Override
    public ResponseResult discover(String sort, String keyword, Integer page, Integer size) {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        Long userId = currentUser != null ? currentUser.getId().longValue() : null;

        QueryWrapper<SysTag> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("tag_name", keyword.trim());
        }
        if ("latest".equals(sort)) {
            wrapper.orderByDesc("id");
        } else {
            wrapper.orderByDesc("sort_order");
        }

        Page<SysTag> pageParam = new Page<>(page, size);
        IPage<SysTag> result = sysTagMapper.selectPage(pageParam, wrapper);

        List<TagDiscoverVO> voList = new ArrayList<>();
        for (SysTag tag : result.getRecords()) {
            TagDiscoverVO vo = new TagDiscoverVO();
            vo.setId(tag.getId());
            vo.setTagName(tag.getTagName());
            vo.setArticleCount(tag.getSortOrder() != null ? tag.getSortOrder() : 0);

            // 关注数
            LambdaQueryWrapper<UserTagRelation> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(UserTagRelation::getTagId, tag.getId());
            countWrapper.eq(UserTagRelation::getRelType, 2);
            Long followCount = userTagRelationMapper.selectCount(countWrapper);
            vo.setFollowCount(followCount != null ? followCount.intValue() : 0);

            // 当前用户是否已关注
            if (userId != null) {
                LambdaQueryWrapper<UserTagRelation> followWrapper = new LambdaQueryWrapper<>();
                followWrapper.eq(UserTagRelation::getUserId, userId);
                followWrapper.eq(UserTagRelation::getTagId, tag.getId());
                followWrapper.eq(UserTagRelation::getRelType, 2);
                Long count = userTagRelationMapper.selectCount(followWrapper);
                vo.setIsFollowing(count > 0);
            } else {
                vo.setIsFollowing(false);
            }

            voList.add(vo);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("page", page);
        resultMap.put("size", size);
        resultMap.put("list", voList);
        return ResponseResult.okResult(resultMap);
    }

    @Override
    public ResponseResult getFollowed() {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = currentUser.getId().longValue();

        LambdaQueryWrapper<UserTagRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(UserTagRelation::getUserId, userId);
        relationWrapper.eq(UserTagRelation::getRelType, 2);
        List<UserTagRelation> relations = userTagRelationMapper.selectList(relationWrapper);

        List<Map<String, Object>> tagList = new ArrayList<>();
        for (UserTagRelation relation : relations) {
            SysTag tag = sysTagMapper.selectById(relation.getTagId());
            if (tag != null) {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("id", tag.getId());
                tagMap.put("tagName", tag.getTagName());
                tagMap.put("categoryCode", tag.getCategoryCode());
                tagMap.put("categoryName", tag.getCategoryName());
                tagList.add(tagMap);
            }
        }

        return ResponseResult.okResult(tagList);
    }

    @Override
    public ResponseResult follow(Integer tagId) {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = currentUser.getId().longValue();

        if (tagId == null) {
            return ResponseResult.errorResult(503, "标签ID不能为空");
        }

        SysTag tag = sysTagMapper.selectById(tagId);
        if (tag == null) {
            return ResponseResult.errorResult(503, "标签不存在");
        }

        // 检查是否已关注（rel_type=2）
        LambdaQueryWrapper<UserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTagRelation::getUserId, userId);
        wrapper.eq(UserTagRelation::getTagId, tagId);
        wrapper.eq(UserTagRelation::getRelType, 2);
        Long count = userTagRelationMapper.selectCount(wrapper);
        if (count > 0) {
            return ResponseResult.okResult();
        }

        UserTagRelation relation = new UserTagRelation();
        relation.setUserId(userId);
        relation.setTagId(tagId);
        relation.setRelType(2);
        userTagRelationMapper.insert(relation);

        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult unfollow(Integer tagId) {
        ApUser currentUser = AppThreadLocalUtil.getUser();
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = currentUser.getId().longValue();

        if (tagId == null) {
            return ResponseResult.errorResult(503, "标签ID不能为空");
        }

        LambdaQueryWrapper<UserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTagRelation::getUserId, userId);
        wrapper.eq(UserTagRelation::getTagId, tagId);
        wrapper.eq(UserTagRelation::getRelType, 2);
        userTagRelationMapper.delete(wrapper);

        return ResponseResult.okResult();
    }
}