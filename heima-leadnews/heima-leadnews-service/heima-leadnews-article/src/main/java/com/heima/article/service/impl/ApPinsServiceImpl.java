package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.article.service.ApPinsService;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApPinsServiceImpl extends ServiceImpl<ApPinsMapper, ApPins> implements ApPinsService {

    @Override
    public ResponseResult findList(Integer page, Integer size, Byte status) {
        IPage<ApPins> iPage = new Page<>(page, size);
        LambdaQueryWrapper<ApPins> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(ApPins::getStatus, status);
        }
        
        queryWrapper.orderByDesc(ApPins::getCreatedTime);
        
        IPage<ApPins> resultPage = page(iPage, queryWrapper);
        
        return new PageResponseResult(page, size, (int) resultPage.getTotal(), resultPage.getRecords());
    }

    @Override
    public ResponseResult deleteById(Long id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        
        boolean deleted = removeById(id);
        
        if (deleted) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        
        return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
    }

    @Override
    public ResponseResult updateStatus(Long id, Byte status, String reason) {
        if (id == null || status == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        
        ApPins apPins = getById(id);
        
        if (apPins == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        
        apPins.setStatus(status);
        apPins.setReason(reason);
        
        boolean updated = updateById(apPins);
        
        if (updated) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }
}
