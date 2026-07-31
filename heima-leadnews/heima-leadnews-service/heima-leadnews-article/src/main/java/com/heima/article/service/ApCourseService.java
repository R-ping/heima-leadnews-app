package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.CourseDto;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCourseService extends IService<ApCourse> {

    ResponseResult findList(Integer page, Integer size, Byte status);

    ResponseResult deleteById(Long id);

    ResponseResult updateStatus(Long id, Byte status, String reason);

    ResponseResult getMyCourses(Long userId, String filter);

    ResponseResult updateProgress(Long userId, Long courseId, Long chapterId, Boolean isCompleted);

    /** 公开课程详情（含章节列表） */
    ResponseResult getPublicDetail(Long courseId);

    // ===== 课程创作管理 =====

    /** 检查用户是否有课程创作权限（逐力值 >= Lv5） */
    ResponseResult checkAuthorPermission(Long userId);

    /** 创建课程草稿 */
    ResponseResult createCourse(CourseDto dto, Long userId);

    /** 更新课程信息 */
    ResponseResult updateCourse(CourseDto dto, Long userId);

    /** 作者课程管理列表 */
    ResponseResult manageList(Integer page, Integer size, Byte status, String keyword, Long userId);

    /** 课程编辑详情（含所有章节） */
    ResponseResult manageDetail(Long courseId, Long userId);

    /** 软删除课程 */
    ResponseResult softDelete(Long courseId, Long userId);
}