package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCourseService extends IService<ApCourse> {

    ResponseResult findList(Integer page, Integer size, Byte status);

    ResponseResult deleteById(Long id);

    ResponseResult updateStatus(Long id, Byte status, String reason);

    ResponseResult getMyCourses(Long userId, String filter);
}
