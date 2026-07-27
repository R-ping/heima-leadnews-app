package com.heima.article.controller.v1;

import com.heima.article.service.ApCourseService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseController 单元测试")
class CourseControllerTest {

    @Mock
    private ApCourseService apCourseService;

    @InjectMocks
    private CourseController courseController;

    private MockedStatic<AppThreadLocalUtil> threadLocalMock;
    private ApUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new ApUser();
        mockUser.setId(1001);
        threadLocalMock = mockStatic(AppThreadLocalUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (threadLocalMock != null) {
            threadLocalMock.close();
        }
    }

    @Nested
    @DisplayName("findList() - 查询课程列表")
    class FindListTests {

        @Test
        @DisplayName("正常查询课程列表，返回成功结果")
        void shouldReturnCourseList() {
            ResponseResult expected = ResponseResult.okResult("course_list");
            when(apCourseService.findList(eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = courseController.findList(1, 10, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apCourseService).findList(1, 10, null);
        }

        @Test
        @DisplayName("按状态筛选课程列表")
        void shouldFilterByStatus() {
            ResponseResult expected = ResponseResult.okResult("filtered_courses");
            when(apCourseService.findList(eq(1), eq(10), eq((byte) 9))).thenReturn(expected);

            ResponseResult result = courseController.findList(1, 10, (byte) 9);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).findList(1, 10, (byte) 9);
        }

        @Test
        @DisplayName("使用默认分页参数")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_courses");
            when(apCourseService.findList(eq(1), eq(10), isNull())).thenReturn(expected);

            ResponseResult result = courseController.findList(1, 10, null);

            assertNotNull(result);
            verify(apCourseService).findList(1, 10, null);
        }
    }

    @Nested
    @DisplayName("deleteById() - 删除课程")
    class DeleteByIdTests {

        @Test
        @DisplayName("正常删除课程，返回成功结果")
        void shouldDeleteCourseSuccessfully() {
            ResponseResult expected = ResponseResult.okResult("deleted");
            when(apCourseService.deleteById(eq(100L))).thenReturn(expected);

            ResponseResult result = courseController.deleteById(100L);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).deleteById(100L);
        }

        @Test
        @DisplayName("删除不存在的课程，透传错误")
        void shouldPropagateErrorWhenCourseNotFound() {
            ResponseResult errorResult = ResponseResult.errorResult(1002, "数据不存在");
            when(apCourseService.deleteById(eq(999L))).thenReturn(errorResult);

            ResponseResult result = courseController.deleteById(999L);

            assertNotNull(result);
            assertEquals(1002, result.getCode());
            verify(apCourseService).deleteById(999L);
        }
    }

    @Nested
    @DisplayName("updateStatus() - 更新课程状态")
    class UpdateStatusTests {

        @Test
        @DisplayName("正常更新课程状态，返回成功结果")
        void shouldUpdateStatusSuccessfully() {
            Map<String, Object> params = new HashMap<>();
            params.put("id", "100");
            params.put("status", "9");
            params.put("reason", "审核通过");
            ResponseResult expected = ResponseResult.okResult("updated");
            when(apCourseService.updateStatus(eq(100L), eq((byte) 9), eq("审核通过")))
                    .thenReturn(expected);

            ResponseResult result = courseController.updateStatus(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).updateStatus(100L, (byte) 9, "审核通过");
        }

        @Test
        @DisplayName("更新状态不提供reason，传递null")
        void shouldPassNullReasonWhenNotProvided() {
            Map<String, Object> params = new HashMap<>();
            params.put("id", "100");
            params.put("status", "2");
            ResponseResult expected = ResponseResult.okResult("updated");
            when(apCourseService.updateStatus(eq(100L), eq((byte) 2), isNull()))
                    .thenReturn(expected);

            ResponseResult result = courseController.updateStatus(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).updateStatus(100L, (byte) 2, null);
        }
    }

    @Nested
    @DisplayName("getMyCourses() - 获取我的课程")
    class GetMyCoursesTests {

        @Test
        @DisplayName("正常获取我的课程，返回成功结果")
        void shouldReturnMyCourses() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("my_courses");
            when(apCourseService.getMyCourses(eq(1001L), isNull())).thenReturn(expected);

            ResponseResult result = courseController.getMyCourses(null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(apCourseService).getMyCourses(1001L, null);
        }

        @Test
        @DisplayName("带筛选条件获取我的课程")
        void shouldFilterMyCourses() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("filtered_my_courses");
            when(apCourseService.getMyCourses(eq(1001L), eq("in_progress"))).thenReturn(expected);

            ResponseResult result = courseController.getMyCourses("in_progress");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).getMyCourses(1001L, "in_progress");
        }

        @Test
        @DisplayName("用户未登录时，传递null给service")
        void shouldPassNullWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
            ResponseResult expected = ResponseResult.okResult("guest_courses");
            when(apCourseService.getMyCourses(isNull(), isNull())).thenReturn(expected);

            ResponseResult result = courseController.getMyCourses(null);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).getMyCourses(null, null);
        }
    }

    @Nested
    @DisplayName("updateProgress() - 更新学习进度")
    class UpdateProgressTests {

        @Test
        @DisplayName("正常更新学习进度，返回成功结果")
        void shouldUpdateProgressSuccessfully() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> params = new HashMap<>();
            params.put("courseId", "100");
            params.put("chapterId", "10");
            params.put("isCompleted", "true");
            ResponseResult expected = ResponseResult.okResult("progress_updated");
            when(apCourseService.updateProgress(eq(1001L), eq(100L), eq(10L), eq(true)))
                    .thenReturn(expected);

            ResponseResult result = courseController.updateProgress(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).updateProgress(1001L, 100L, 10L, true);
        }

        @Test
        @DisplayName("更新进度部分参数为null")
        void shouldPassNullWhenParamsMissing() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> params = new HashMap<>();
            params.put("courseId", "100");
            ResponseResult expected = ResponseResult.okResult("partial_progress");
            when(apCourseService.updateProgress(eq(1001L), eq(100L), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = courseController.updateProgress(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(apCourseService).updateProgress(1001L, 100L, null, null);
        }
    }
}