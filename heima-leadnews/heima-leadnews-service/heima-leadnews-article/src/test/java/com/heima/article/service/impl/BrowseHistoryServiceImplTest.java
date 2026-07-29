package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApBrowseHistoryMapper;
import com.heima.model.article.pojos.ApBrowseHistory;
import com.heima.model.common.dtos.ResponseResult;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("浏览历史服务测试")
class BrowseHistoryServiceImplTest {

    @Mock
    private ApBrowseHistoryMapper apBrowseHistoryMapper;

    @InjectMocks
    private BrowseHistoryServiceImpl browseHistoryService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ApBrowseHistory.class);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(browseHistoryService, "baseMapper", apBrowseHistoryMapper);
    }

    // ==================== getHistoryList ====================

    @Test
    @DisplayName("获取浏览历史列表 - 正常查询返回分页数据")
    void testGetHistoryList_Success() {
        ApBrowseHistory record = new ApBrowseHistory();
        record.setId(1L);
        record.setUserId(1L);
        record.setTargetType(1);
        record.setArticleId(1001L);
        record.setArticleTitle("测试文章");
        record.setAuthorName("测试作者");
        record.setBrowseTime(new Date());

        Page<ApBrowseHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(record));
        pageResult.setTotal(1);

        when(apBrowseHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        ResponseResult result = browseHistoryService.getHistoryList(1L, 1, 10, null);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertEquals(1L, data.get("total"));
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertEquals(1, list.size());
        assertEquals("测试文章", list.get(0).get("articleTitle"));
    }

    @Test
    @DisplayName("获取浏览历史列表 - 带关键词搜索")
    void testGetHistoryList_WithKeyword() {
        Page<ApBrowseHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(apBrowseHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        ResponseResult result = browseHistoryService.getHistoryList(1L, 1, 10, "Java");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0L, data.get("total"));
    }

    @Test
    @DisplayName("获取浏览历史列表 - 空关键词不触发模糊搜索")
    void testGetHistoryList_EmptyKeyword() {
        Page<ApBrowseHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(apBrowseHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        ResponseResult result = browseHistoryService.getHistoryList(1L, 1, 10, "   ");

        assertNotNull(result);
        assertEquals(200, result.getCode());
    }

    // ==================== clearHistory ====================

    @Test
    @DisplayName("清空浏览历史 - 软删除指定用户记录")
    void testClearHistory_Success() {
        when(apBrowseHistoryMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        browseHistoryService.clearHistory(1L);

        verify(apBrowseHistoryMapper, times(1)).update(any(), any(LambdaUpdateWrapper.class));
    }

    // ==================== reportBrowse ====================

    @Test
    @DisplayName("上报浏览记录 - 参数为null返回参数错误")
    void testReportBrowse_NullParams() {
        ResponseResult result = browseHistoryService.reportBrowse(null, 1, 1001L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());

        result = browseHistoryService.reportBrowse(1L, null, 1001L);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());

        result = browseHistoryService.reportBrowse(1L, 1, null);
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    @DisplayName("上报浏览记录 - 记录已存在，更新浏览时间")
    void testReportBrowse_ExistingRecord() {
        ApBrowseHistory existing = new ApBrowseHistory();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setTargetType(1);
        existing.setArticleId(1001L);

        when(apBrowseHistoryMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean()))
                .thenReturn(existing);
        when(apBrowseHistoryMapper.updateById(any(ApBrowseHistory.class))).thenReturn(1);

        ResponseResult result = browseHistoryService.reportBrowse(1L, 1, 1001L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(apBrowseHistoryMapper, times(1)).updateById(any(ApBrowseHistory.class));
    }

    @Test
    @DisplayName("上报浏览记录 - 记录不存在，插入新记录")
    void testReportBrowse_NewRecord() {
        when(apBrowseHistoryMapper.selectOne(any(LambdaQueryWrapper.class), anyBoolean()))
                .thenReturn(null);
        when(apBrowseHistoryMapper.insert(any(ApBrowseHistory.class))).thenReturn(1);

        ResponseResult result = browseHistoryService.reportBrowse(1L, 1, 1001L);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        verify(apBrowseHistoryMapper, times(1)).insert(any(ApBrowseHistory.class));
    }
}