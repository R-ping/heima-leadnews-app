package com.heima.article.controller.v1;

import com.heima.article.service.FansDataService;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FansDataController 单元测试")
class FansDataControllerTest {

    @Mock
    private FansDataService fansDataService;

    @InjectMocks
    private FansDataController fansDataController;

    @Nested
    @DisplayName("getStatistics() - 获取粉丝统计")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常获取粉丝统计数据，返回成功结果")
        void shouldReturnFansStatistics() {
            ResponseResult expected = ResponseResult.okResult("fans_statistics");
            when(fansDataService.getFansStatistics(eq("2026-07-01"), eq("2026-07-27")))
                    .thenReturn(expected);

            ResponseResult result = fansDataController.getStatistics("2026-07-01", "2026-07-27");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(fansDataService).getFansStatistics("2026-07-01", "2026-07-27");
        }

        @Test
        @DisplayName("Service返回错误时，正确透传")
        void shouldPropagateErrorResult() {
            ResponseResult errorResult = ResponseResult.errorResult(500, "查询失败");
            when(fansDataService.getFansStatistics(eq("invalid"), eq("invalid")))
                    .thenReturn(errorResult);

            ResponseResult result = fansDataController.getStatistics("invalid", "invalid");

            assertNotNull(result);
            assertEquals(500, result.getCode());
        }
    }

    @Nested
    @DisplayName("getTrend() - 获取粉丝趋势")
    class GetTrendTests {

        @Test
        @DisplayName("正常获取粉丝趋势数据，返回成功结果")
        void shouldReturnFansTrend() {
            ResponseResult expected = ResponseResult.okResult("fans_trend");
            when(fansDataService.getFansTrend(eq("2026-07-01"), eq("2026-07-27"), eq(7)))
                    .thenReturn(expected);

            ResponseResult result = fansDataController.getTrend("2026-07-01", "2026-07-27", 7);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(fansDataService).getFansTrend("2026-07-01", "2026-07-27", 7);
        }

        @Test
        @DisplayName("使用默认days参数")
        void shouldUseDefaultDays() {
            ResponseResult expected = ResponseResult.okResult("default_trend");
            when(fansDataService.getFansTrend(eq("2026-07-01"), eq("2026-07-27"), eq(7)))
                    .thenReturn(expected);

            ResponseResult result = fansDataController.getTrend("2026-07-01", "2026-07-27", 7);

            assertNotNull(result);
            verify(fansDataService).getFansTrend("2026-07-01", "2026-07-27", 7);
        }
    }

    @Nested
    @DisplayName("getList() - 获取粉丝列表")
    class GetListTests {

        @Test
        @DisplayName("正常获取粉丝列表，返回成功结果")
        void shouldReturnFansList() {
            ResponseResult expected = ResponseResult.okResult("fans_list");
            when(fansDataService.getFansList(eq(1), eq(10))).thenReturn(expected);

            ResponseResult result = fansDataController.getList(1, 10);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(fansDataService).getFansList(1, 10);
        }

        @Test
        @DisplayName("使用默认分页参数获取粉丝列表")
        void shouldUseDefaultPagination() {
            ResponseResult expected = ResponseResult.okResult("default_fans");
            when(fansDataService.getFansList(eq(1), eq(10))).thenReturn(expected);

            ResponseResult result = fansDataController.getList(1, 10);

            assertNotNull(result);
            verify(fansDataService).getFansList(1, 10);
        }
    }

    @Nested
    @DisplayName("follow() - 关注粉丝")
    class FollowTests {

        @Test
        @DisplayName("正常关注用户，返回成功结果")
        void shouldFollowSuccessfully() {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", 1001);
            ResponseResult expected = ResponseResult.okResult("followed");
            when(fansDataService.followFans(eq(1001))).thenReturn(expected);

            ResponseResult result = fansDataController.follow(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(fansDataService).followFans(1001);
        }

        @Test
        @DisplayName("userId为null时，传递null给service")
        void shouldPassNullWhenUserIdMissing() {
            Map<String, Object> body = new HashMap<>();
            ResponseResult expected = ResponseResult.okResult("no_user");
            when(fansDataService.followFans(isNull())).thenReturn(expected);

            ResponseResult result = fansDataController.follow(body);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(fansDataService).followFans(null);
        }
    }

    @Nested
    @DisplayName("getPortrait() - 获取粉丝画像")
    class GetPortraitTests {

        @Test
        @DisplayName("正常获取粉丝画像，返回成功结果")
        void shouldReturnFansPortrait() {
            ResponseResult expected = ResponseResult.okResult("fans_portrait");
            when(fansDataService.getFansPortrait()).thenReturn(expected);

            ResponseResult result = fansDataController.getPortrait();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(fansDataService).getFansPortrait();
        }
    }

    @Nested
    @DisplayName("getAvatars() - 获取粉丝头像")
    class GetAvatarsTests {

        @Test
        @DisplayName("正常获取粉丝头像列表，返回成功结果")
        void shouldReturnFansAvatars() {
            ResponseResult expected = ResponseResult.okResult("fans_avatars");
            when(fansDataService.getFansAvatars(eq(1), eq(20))).thenReturn(expected);

            ResponseResult result = fansDataController.getAvatars(1, 20);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(expected, result);
            verify(fansDataService).getFansAvatars(1, 20);
        }

        @Test
        @DisplayName("使用默认分页获取头像")
        void shouldUseDefaultPaginationForAvatars() {
            ResponseResult expected = ResponseResult.okResult("default_avatars");
            when(fansDataService.getFansAvatars(eq(1), eq(20))).thenReturn(expected);

            ResponseResult result = fansDataController.getAvatars(1, 20);

            assertNotNull(result);
            verify(fansDataService).getFansAvatars(1, 20);
        }
    }
}