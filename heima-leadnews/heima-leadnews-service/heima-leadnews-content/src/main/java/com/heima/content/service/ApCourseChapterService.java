package com.heima.content.service;

import com.heima.model.article.dtos.ChapterDto;
import com.heima.model.article.dtos.ChapterSortDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApCourseChapterService {

    /** 创建章节 */
    ResponseResult createChapter(ChapterDto dto, Long userId);

    /** 更新章节 */
    ResponseResult updateChapter(ChapterDto dto, Long userId);

    /** 删除章节 */
    ResponseResult deleteChapter(Long chapterId, Long userId);

    /** 批量更新章节排序 */
    ResponseResult updateSort(ChapterSortDto dto, Long userId);

    /** 公开获取章节详情（用于阅读） */
    ResponseResult getChapterDetail(Long chapterId);
}