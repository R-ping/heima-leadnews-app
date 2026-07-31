package com.heima.article.controller.v1;

import com.heima.article.service.ApCourseChapterService;
import com.heima.model.article.dtos.ChapterDto;
import com.heima.model.article.dtos.ChapterSortDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course/chapter")
@Slf4j
public class CourseChapterController {

    @Autowired
    private ApCourseChapterService chapterService;

    /** 公开获取章节详情（用于阅读） */
    @GetMapping("/{id}/detail")
    public ResponseResult getChapterDetail(@PathVariable Long id) {
        return chapterService.getChapterDetail(id);
    }

    /** 创建章节 */
    @PostMapping("/create")
    public ResponseResult createChapter(@RequestBody ChapterDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(com.heima.model.common.enums.AppHttpCodeEnum.NEED_LOGIN);
        }
        return chapterService.createChapter(dto, user.getId().longValue());
    }

    /** 更新章节 */
    @PutMapping("/update")
    public ResponseResult updateChapter(@RequestBody ChapterDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(com.heima.model.common.enums.AppHttpCodeEnum.NEED_LOGIN);
        }
        return chapterService.updateChapter(dto, user.getId().longValue());
    }

    /** 删除章节 */
    @DeleteMapping("/{id}")
    public ResponseResult deleteChapter(@PathVariable Long id) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(com.heima.model.common.enums.AppHttpCodeEnum.NEED_LOGIN);
        }
        return chapterService.deleteChapter(id, user.getId().longValue());
    }

    /** 批量更新章节排序 */
    @PutMapping("/sort")
    public ResponseResult updateSort(@RequestBody ChapterSortDto dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(com.heima.model.common.enums.AppHttpCodeEnum.NEED_LOGIN);
        }
        return chapterService.updateSort(dto, user.getId().longValue());
    }
}