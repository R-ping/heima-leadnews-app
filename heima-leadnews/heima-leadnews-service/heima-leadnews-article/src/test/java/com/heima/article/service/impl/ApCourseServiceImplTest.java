package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApCourseChapterMapper;
import com.heima.article.mapper.ApCourseMapper;
import com.heima.article.mapper.ApCourseReadingProgressMapper;
import com.heima.article.mapper.ApUserCourseMapper;
import com.heima.model.article.pojos.*;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("课程服务测试")
class ApCourseServiceImplTest {

    @Mock
    private ApCourseMapper apCourseMapper;

    @Mock
    private ApUserCourseMapper userCourseMapper;

    @Mock
    private ApCourseReadingProgressMapper readingProgressMapper;

    @Mock
    private ApCourseChapterMapper chapterMapper;

    @InjectMocks
    private ApCourseServiceImpl apCourseService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apCourseService, "baseMapper", apCourseMapper);
    }

    // ==================== findList ====================

    @Test
    @DisplayName("查询课程列表 - 正常返回分页数据")
    void testFindList_Success() {
        ApCourse course = new ApCourse();
        course.setId(1L);
        course.setTitle("测试课程");
        when(apCourseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApCourse> page = inv.getArgument(0);
            page.setRecords(Collections.singletonList(course));
            page.setTotal(1);
            return page;
        });

        ResponseResult result = apCourseService.findList(1, 10, (byte) 1);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("查询课程列表 - status为null查询全部")
    void testFindList_NullStatus() {
        when(apCourseMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            Page<ApCourse> page = inv.getArgument(0);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return page;
        });

        ResponseResult result = apCourseService.findList(1, 10, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== deleteById ====================

    @Test
    @DisplayName("删除课程 - id为null返回参数错误")
    void testDeleteById_NullId() {
        ResponseResult result = apCourseService.deleteById(null);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除课程 - 删除成功")
    void testDeleteById_Success() {
        when(apCourseMapper.deleteById(1L)).thenReturn(1);

        ResponseResult result = apCourseService.deleteById(1L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("删除课程 - 数据不存在")
    void testDeleteById_NotFound() {
        when(apCourseMapper.deleteById(999L)).thenReturn(0);

        ResponseResult result = apCourseService.deleteById(999L);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    // ==================== updateStatus ====================

    @Test
    @DisplayName("更新课程状态 - id为null返回参数错误")
    void testUpdateStatus_NullId() {
        ResponseResult result = apCourseService.updateStatus(null, (byte) 1, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新课程状态 - status为null返回参数错误")
    void testUpdateStatus_NullStatus() {
        ResponseResult result = apCourseService.updateStatus(1L, null, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新课程状态 - 课程不存在")
    void testUpdateStatus_NotFound() {
        when(apCourseMapper.selectById(999L)).thenReturn(null);

        ResponseResult result = apCourseService.updateStatus(999L, (byte) 1, "审核通过");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新课程状态 - 更新成功")
    void testUpdateStatus_Success() {
        ApCourse course = new ApCourse();
        course.setId(1L);
        course.setTitle("测试课程");
        when(apCourseMapper.selectById(1L)).thenReturn(course);
        when(apCourseMapper.updateById(any(ApCourse.class))).thenReturn(1);

        ResponseResult result = apCourseService.updateStatus(1L, (byte) 1, "审核通过");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== getMyCourses ====================

    @Test
    @DisplayName("获取我的课程 - userId为null返回需要登录")
    void testGetMyCourses_NullUserId() {
        ResponseResult result = apCourseService.getMyCourses(null, "all");

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取我的课程 - 正常返回课程列表")
    void testGetMyCourses_Success() {
        ApUserCourse uc = new ApUserCourse();
        uc.setCourseId(1L);
        uc.setUserId(1);
        uc.setAccessType(1);
        uc.setIsActive((byte) 1);
        when(userCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(uc));

        ApCourse course = new ApCourse();
        course.setId(1L);
        course.setTitle("测试课程");
        when(apCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(course));

        ResponseResult result = apCourseService.getMyCourses(1L, "all");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取我的课程 - 无记录返回空列表")
    void testGetMyCourses_Empty() {
        when(userCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        ResponseResult result = apCourseService.getMyCourses(1L, "all");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("获取我的课程 - filter为purchased过滤已购买")
    void testGetMyCourses_FilterPurchased() {
        ApUserCourse uc = new ApUserCourse();
        uc.setCourseId(1L);
        uc.setUserId(1);
        uc.setAccessType(1);
        uc.setIsActive((byte) 1);
        when(userCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(uc));
        when(apCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        ResponseResult result = apCourseService.getMyCourses(1L, "purchased");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== updateProgress ====================

    @Test
    @DisplayName("更新学习进度 - userId为null返回参数错误")
    void testUpdateProgress_NullUserId() {
        ResponseResult result = apCourseService.updateProgress(null, 1L, 1L, true);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新学习进度 - courseId为null返回参数错误")
    void testUpdateProgress_NullCourseId() {
        ResponseResult result = apCourseService.updateProgress(1L, null, 1L, true);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新学习进度 - 用户未拥有该课程")
    void testUpdateProgress_NoAccess() {
        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ResponseResult result = apCourseService.updateProgress(1L, 1L, 1L, true);

        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("更新学习进度 - 更新成功")
    void testUpdateProgress_Success() {
        ApUserCourse uc = new ApUserCourse();
        uc.setUserId(1);
        uc.setCourseId(1L);
        uc.setIsActive((byte) 1);
        when(userCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(uc);
        when(readingProgressMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(readingProgressMapper.insert(any(ApCourseReadingProgress.class))).thenReturn(1);
        when(userCourseMapper.updateById(any(ApUserCourse.class))).thenReturn(1);

        ResponseResult result = apCourseService.updateProgress(1L, 1L, 1L, true);

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
}