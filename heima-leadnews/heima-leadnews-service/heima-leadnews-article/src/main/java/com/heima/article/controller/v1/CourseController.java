package com.heima.article.controller.v1;

import com.heima.article.service.ApCourseService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/course")
@Slf4j
public class CourseController {

    @Autowired
    private ApCourseService apCourseService;

    @GetMapping("/list")
    public ResponseResult findList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Byte status) {
        return apCourseService.findList(page, size, status);
    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteById(@PathVariable Long id) {
        return apCourseService.deleteById(id);
    }

    @PutMapping("/status")
    public ResponseResult updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Byte status = Byte.parseByte(params.get("status").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        return apCourseService.updateStatus(id, status, reason);
    }

    @GetMapping("/my")
    public ResponseResult getMyCourses(@RequestParam(required = false) String filter) {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = user != null ? user.getId().longValue() : null;
        return apCourseService.getMyCourses(userId, filter);
    }

    @PostMapping("/progress")
    public ResponseResult updateProgress(@RequestBody Map<String, Object> params) {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = user != null ? user.getId().longValue() : null;
        Long courseId = params.get("courseId") != null ? Long.parseLong(params.get("courseId").toString()) : null;
        Long chapterId = params.get("chapterId") != null ? Long.parseLong(params.get("chapterId").toString()) : null;
        Boolean isCompleted = params.get("isCompleted") != null ? Boolean.parseBoolean(params.get("isCompleted").toString()) : null;
        return apCourseService.updateProgress(userId, courseId, chapterId, isCompleted);
    }
}
