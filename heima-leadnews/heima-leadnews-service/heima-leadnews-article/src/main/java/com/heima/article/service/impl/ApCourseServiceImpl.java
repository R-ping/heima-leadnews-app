package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApCourseMapper;
import com.heima.article.service.ApCourseService;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ApCourseServiceImpl extends ServiceImpl<ApCourseMapper, ApCourse> implements ApCourseService {

    @Override
    public ResponseResult findList(Integer page, Integer size, Byte status) {
        IPage<ApCourse> iPage = new Page<>(page, size);
        LambdaQueryWrapper<ApCourse> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(ApCourse::getStatus, status);
        }
        
        queryWrapper.orderByDesc(ApCourse::getCreatedTime);
        
        IPage<ApCourse> resultPage = page(iPage, queryWrapper);
        
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
        
        ApCourse apCourse = getById(id);
        
        if (apCourse == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        
        apCourse.setStatus(status);
        apCourse.setReason(reason);
        
        boolean updated = updateById(apCourse);
        
        if (updated) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }

    @Override
    public ResponseResult getMyCourses(Long userId, String filter) {
        LambdaQueryWrapper<ApCourse> queryWrapper = new LambdaQueryWrapper<>();

        if ("purchased".equals(filter)) {
            queryWrapper.eq(ApCourse::getStatus, 1);
        } else if ("vip".equals(filter)) {
            queryWrapper.eq(ApCourse::getStatus, 2);
        }

        queryWrapper.orderByDesc(ApCourse::getCreatedTime);
        List<ApCourse> list = list(queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return ResponseResult.okResult(result);
    }
}
