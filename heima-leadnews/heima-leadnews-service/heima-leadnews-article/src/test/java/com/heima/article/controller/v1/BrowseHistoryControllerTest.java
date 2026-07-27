package com.heima.article.controller.v1;

import com.heima.article.service.BrowseHistoryService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrowseHistoryController 单元测试")
class BrowseHistoryControllerTest {

    @Mock
    private BrowseHistoryService browseHistoryService;

    @InjectMocks
    private BrowseHistoryController browseHistoryController;

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
    @DisplayName("getHistoryList() - 获取浏览历史列表")
    class GetHistoryListTests {

        @Test
        @DisplayName("正常获取浏览历史，返回成功结果")
        void shouldReturnHistoryList() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("history_list");
            when(browseHistoryService.getHistoryList(eq(1001L), eq(1), eq(10), isNull()))
                    .thenReturn(expected);

            ResponseResult result = browseHistoryController.getHistoryList(1, 10, null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(browseHistoryService).getHistoryList(1001L, 1, 10, null);
        }

        @Test
        @DisplayName("带关键词搜索浏览历史")
        void shouldSearchHistoryWithKeyword() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("searched_history");
            when(browseHistoryService.getHistoryList(eq(1001L), eq(1), eq(10), eq("java")))
                    .thenReturn(expected);

            ResponseResult result = browseHistoryController.getHistoryList(1, 10, "java");

            assertNotNull(result);
            assertEquals(expected, result);
            verify(browseHistoryService).getHistoryList(1001L, 1, 10, "java");
        }

        @Test
        @DisplayName("用户未登录时，返回需要登录错误")
        void shouldReturnNeedLoginWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

            ResponseResult result = browseHistoryController.getHistoryList(1, 10, null);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
            verify(browseHistoryService, never()).getHistoryList(anyLong(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("使用默认分页参数获取历史")
        void shouldUseDefaultPagination() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            ResponseResult expected = ResponseResult.okResult("default_page");
            when(browseHistoryService.getHistoryList(eq(1001L), eq(1), eq(10), isNull()))
                    .thenReturn(expected);

            ResponseResult result = browseHistoryController.getHistoryList(1, 10, null);

            assertNotNull(result);
            verify(browseHistoryService).getHistoryList(1001L, 1, 10, null);
        }
    }

    @Nested
    @DisplayName("clearHistory() - 清空浏览历史")
    class ClearHistoryTests {

        @Test
        @DisplayName("正常清空浏览历史，返回成功结果")
        void shouldClearHistorySuccessfully() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);

            ResponseResult result = browseHistoryController.clearHistory();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            verify(browseHistoryService).clearHistory(1001L);
        }

        @Test
        @DisplayName("用户未登录时，返回需要登录错误")
        void shouldReturnNeedLoginWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);

            ResponseResult result = browseHistoryController.clearHistory();

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
            verify(browseHistoryService, never()).clearHistory(anyLong());
        }
    }

    @Nested
    @DisplayName("reportBrowse() - 上报浏览记录")
    class ReportBrowseTests {

        @Test
        @DisplayName("正常上报浏览记录，返回成功结果")
        void shouldReportBrowseSuccessfully() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> params = new HashMap<>();
            params.put("targetType", 1);
            params.put("targetId", 100L);
            ResponseResult expected = ResponseResult.okResult("reported");
            when(browseHistoryService.reportBrowse(eq(1001L), eq(1), eq(100L)))
                    .thenReturn(expected);

            ResponseResult result = browseHistoryController.reportBrowse(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(browseHistoryService).reportBrowse(1001L, 1, 100L);
        }

        @Test
        @DisplayName("用户未登录时，返回需要登录错误")
        void shouldReturnNeedLoginWhenUserNotLoggedIn() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(null);
            Map<String, Object> params = new HashMap<>();
            params.put("targetType", 1);
            params.put("targetId", 100L);

            ResponseResult result = browseHistoryController.reportBrowse(params);

            assertNotNull(result);
            assertEquals(AppHttpCodeEnum.NEED_LOGIN.getCode(), result.getCode());
            verify(browseHistoryService, never()).reportBrowse(anyLong(), anyInt(), any());
        }

        @Test
        @DisplayName("参数为null时，正常委托给service")
        void shouldDelegateToServiceWhenParamsMissing() {
            threadLocalMock.when(AppThreadLocalUtil::getUser).thenReturn(mockUser);
            Map<String, Object> params = new HashMap<>();
            ResponseResult expected = ResponseResult.okResult("reported");
            when(browseHistoryService.reportBrowse(eq(1001L), isNull(), isNull()))
                    .thenReturn(expected);

            ResponseResult result = browseHistoryController.reportBrowse(params);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(browseHistoryService).reportBrowse(1001L, null, null);
        }
    }
}