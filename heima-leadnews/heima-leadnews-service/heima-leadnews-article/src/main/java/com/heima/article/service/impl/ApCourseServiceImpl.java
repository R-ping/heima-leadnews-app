package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApCourseChapterMapper;
import com.heima.article.mapper.ApCourseMapper;
import com.heima.article.mapper.ApCourseReadingProgressMapper;
import com.heima.article.mapper.ApUserCourseMapper;
import com.heima.article.service.ApCourseService;
import com.heima.model.article.pojos.ApCourse;
import com.heima.model.article.pojos.ApCourseChapter;
import com.heima.model.article.pojos.ApCourseReadingProgress;
import com.heima.model.article.pojos.ApUserCourse;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApCourseServiceImpl extends ServiceImpl<ApCourseMapper, ApCourse> implements ApCourseService {

    @Autowired
    private ApUserCourseMapper userCourseMapper;

    @Autowired
    private ApCourseReadingProgressMapper readingProgressMapper;

    @Autowired
    private ApCourseChapterMapper chapterMapper;

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
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // Query user_course records by filter type
        LambdaQueryWrapper<ApUserCourse> ucQuery = new LambdaQueryWrapper<>();
        ucQuery.eq(ApUserCourse::getUserId, userId.intValue());
        ucQuery.eq(ApUserCourse::getIsActive, (byte) 1);

        if ("purchased".equals(filter)) {
            ucQuery.eq(ApUserCourse::getAccessType, 1);
        } else if ("vip".equals(filter)) {
            ucQuery.eq(ApUserCourse::getAccessType, 2);
        }
        // "all" or null: no access_type filter

        ucQuery.orderByDesc(ApUserCourse::getLastLearnAt);
        List<ApUserCourse> userCourses = userCourseMapper.selectList(ucQuery);

        // If no records found, return empty list
        if (userCourses.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", Collections.emptyList());
            result.put("total", 0);
            return ResponseResult.okResult(result);
        }

        // Collect course IDs
        List<Long> courseIds = userCourses.stream()
                .map(ApUserCourse::getCourseId)
                .collect(Collectors.toList());

        // Query course info
        LambdaQueryWrapper<ApCourse> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.in(ApCourse::getId, courseIds);
        List<ApCourse> courses = list(courseQuery);
        Map<Long, ApCourse> courseMap = courses.stream()
                .collect(Collectors.toMap(ApCourse::getId, c -> c));

        // Build result list
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> list = new ArrayList<>();
        for (ApUserCourse uc : userCourses) {
            ApCourse course = courseMap.get(uc.getCourseId());
            if (course == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", course.getId());
            item.put("title", course.getTitle());
            item.put("subtitle", course.getSubtitle());
            item.put("coverImage", course.getCoverImage());
            item.put("authorName", course.getAuthorName());
            item.put("authorId", course.getAuthorId());
            item.put("price", course.getPrice());
            item.put("originalPrice", course.getOriginalPrice());
            item.put("chapterCount", course.getChapterCount());
            item.put("studyCount", course.getStudyCount());
            item.put("categoryId", course.getCategoryId());
            item.put("progress", uc.getProgress() != null ? uc.getProgress() : BigDecimal.ZERO);
            item.put("accessType", uc.getAccessType());
            item.put("isTrial", uc.getIsTrial() != null ? uc.getIsTrial() : 0);
            item.put("borrowExpireAt", uc.getBorrowExpireAt() != null ? sdf.format(uc.getBorrowExpireAt()) : null);
            item.put("lastLearnAt", uc.getLastLearnAt() != null ? sdf.format(uc.getLastLearnAt()) : null);
            item.put("lastLearnChapterId", uc.getLastLearnChapterId());
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult updateProgress(Long userId, Long courseId, Long chapterId, Boolean isCompleted) {
        if (userId == null || courseId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // Check user has access to this course
        LambdaQueryWrapper<ApUserCourse> ucQuery = new LambdaQueryWrapper<>();
        ucQuery.eq(ApUserCourse::getUserId, userId.intValue());
        ucQuery.eq(ApUserCourse::getCourseId, courseId);
        ucQuery.eq(ApUserCourse::getIsActive, (byte) 1);
        ApUserCourse userCourse = userCourseMapper.selectOne(ucQuery);

        if (userCourse == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH, "您未拥有该课程");
        }

        // Update or insert reading progress
        if (chapterId != null) {
            LambdaQueryWrapper<ApCourseReadingProgress> rpQuery = new LambdaQueryWrapper<>();
            rpQuery.eq(ApCourseReadingProgress::getUserId, userId.intValue());
            rpQuery.eq(ApCourseReadingProgress::getChapterId, chapterId);
            ApCourseReadingProgress progress = readingProgressMapper.selectOne(rpQuery);

            if (progress == null) {
                progress = new ApCourseReadingProgress();
                progress.setUserId(userId.intValue());
                progress.setChapterId(chapterId);
                progress.setProgress(100f);
                progress.setIsCompleted(isCompleted != null && isCompleted ? 1 : 0);
                progress.setCompletedAt(isCompleted != null && isCompleted ? new Date() : null);
                progress.setLastReadAt(new Date());
                readingProgressMapper.insert(progress);
            } else {
                progress.setProgress(100f);
                if (isCompleted != null && isCompleted) {
                    progress.setIsCompleted(1);
                    progress.setCompletedAt(new Date());
                }
                progress.setLastReadAt(new Date());
                readingProgressMapper.updateById(progress);
            }

            // Recalculate overall progress
            ApCourse course = getById(courseId);
            if (course != null && course.getChapterCount() != null && course.getChapterCount() > 0) {
                LambdaQueryWrapper<ApCourseReadingProgress> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(ApCourseReadingProgress::getUserId, userId.intValue());
                countQuery.in(ApCourseReadingProgress::getChapterId, 
                    // get all chapters for this course
                    chapterMapper.selectList(
                        new LambdaQueryWrapper<ApCourseChapter>()
                            .eq(ApCourseChapter::getCourseId, courseId)
                            .select(ApCourseChapter::getId)
                    ).stream().map(ApCourseChapter::getId).collect(Collectors.toList())
                );
                countQuery.eq(ApCourseReadingProgress::getIsCompleted, 1);
                long completedCount = readingProgressMapper.selectCount(countQuery);

                BigDecimal newProgress = BigDecimal.valueOf(completedCount * 100.0 / course.getChapterCount())
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                userCourse.setProgress(newProgress);
            }
        }

        userCourse.setLastLearnChapterId(chapterId);
        userCourse.setLastLearnAt(new Date());
        userCourseMapper.updateById(userCourse);

        Map<String, Object> result = new HashMap<>();
        result.put("progress", userCourse.getProgress());
        return ResponseResult.okResult(result);
    }
}