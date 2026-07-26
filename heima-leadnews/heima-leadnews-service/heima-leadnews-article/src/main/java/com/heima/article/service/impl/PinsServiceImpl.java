package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.article.service.PinsService;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PinsServiceImpl extends ServiceImpl<ApPinsMapper, ApPins> implements PinsService {

    @Override
    public ResponseResult list(Long authorId, Integer page, Integer size, String status) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Long userId = authorId != null ? authorId : user.getId().longValue();
        Page<ApPins> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        if (status != null && !status.isEmpty()) {
            Byte statusCode = getStatusCode(status);
            if (statusCode != null) {
                wrapper.eq(ApPins::getStatus, statusCode);
            }
        }
        wrapper.orderByDesc(ApPins::getCreatedTime);
        IPage<ApPins> result = page(pageParam, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
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
        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        data.put("total", count(wrapper));
        wrapper.eq(ApPins::getStatus, ApPins.Status.PUBLISHED.getCode());
        data.put("published", count(wrapper));
        wrapper.clear();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        wrapper.eq(ApPins::getStatus, ApPins.Status.SUBMIT.getCode());
        data.put("reviewing", count(wrapper));
        wrapper.clear();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        wrapper.eq(ApPins::getStatus, ApPins.Status.FAIL.getCode());
        data.put("rejected", count(wrapper));
        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional
    public ResponseResult createPins(ApPins pins) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (pins.getContent() == null || pins.getContent().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "内容不能为空");
        }
        pins.setUserId(user.getId().longValue());
        pins.setAuthorId(user.getId().longValue());
        pins.setAuthorName(user.getNickname());
        pins.setAuthorImage(user.getImage());
        pins.setIsDeleted(false);
        pins.setStatus(ApPins.Status.SUBMIT.getCode());
        pins.setLikes(0);
        pins.setComment(0);
        pins.setShare(0);
        pins.setCreatedTime(new Date());
        pins.setPublishTime(new Date());
        save(pins);
        return ResponseResult.okResult(pins);
    }

    @Override
    @Transactional
    public ResponseResult deletePins(Long id) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "id 不能为空");
        }
        ApPins pins = getById(id);
        if (pins == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (!pins.getAuthorId().equals(user.getId().longValue())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        pins.setIsDeleted(true);
        updateById(pins);
        return ResponseResult.okResult();
    }

    private Byte getStatusCode(String status) {
        switch (status) {
            case "published":
                return ApPins.Status.PUBLISHED.getCode();
            case "reviewing":
                return ApPins.Status.SUBMIT.getCode();
            case "rejected":
                return ApPins.Status.FAIL.getCode();
            default:
                return null;
        }
    }
}
