package com.heima.article.service.impl;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章任务服务测试")
class ArticleTaskServiceImplTest {

    @Mock
    private IScheduleClient scheduleClient;

    @Mock
    private ApArticleMapper apArticleMapper;

    @InjectMocks
    private ArticleTaskServiceImpl articleTaskService;

    // ==================== addArticleToTask ====================

    @Test
    @DisplayName("添加文章到延迟任务 - 发布时间已过，立即执行")
    void testAddArticleToTask_PastTime() {
        Date pastTime = new Date(System.currentTimeMillis() - 3600000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1001L, pastTime);

        verify(scheduleClient, times(1)).addTask(any(Task.class));
    }

    @Test
    @DisplayName("添加文章到延迟任务 - 发布时间在未来，设置延迟任务")
    void testAddArticleToTask_FutureTime() {
        Date futureTime = new Date(System.currentTimeMillis() + 3600000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1001L, futureTime);

        verify(scheduleClient, times(1)).addTask(any(Task.class));
    }

    @Test
    @DisplayName("添加文章到延迟任务 - 发布时间在5分钟内")
    void testAddArticleToTask_Within5Minutes() {
        Date nearTime = new Date(System.currentTimeMillis() + 2 * 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1001L, nearTime);

        verify(scheduleClient, times(1)).addTask(any(Task.class));
    }

    @Test
    @DisplayName("添加文章到延迟任务 - 发布时间在15分钟内")
    void testAddArticleToTask_Within15Minutes() {
        Date nearTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1001L, nearTime);

        verify(scheduleClient, times(1)).addTask(any(Task.class));
    }

    @Test
    @DisplayName("添加文章到延迟任务 - 发布时间超过15分钟")
    void testAddArticleToTask_Over15Minutes() {
        Date farTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1001L, farTime);

        verify(scheduleClient, times(1)).addTask(any(Task.class));
    }

    // ==================== publishArticle ====================

    @Test
    @DisplayName("发布文章 - 文章不存在记录错误日志")
    void testPublishArticle_NotFound() {
        when(apArticleMapper.selectById(999L)).thenReturn(null);

        articleTaskService.publishArticle(999L);

        verify(apArticleMapper, times(1)).selectById(999L);
        verify(apArticleMapper, never()).updateById(any(ApArticle.class));
    }

    @Test
    @DisplayName("发布文章 - 发布成功更新状态为已发布")
    void testPublishArticle_Success() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setStatus((byte) 1);
        when(apArticleMapper.selectById(1001L)).thenReturn(article);
        when(apArticleMapper.updateById(any(ApArticle.class))).thenReturn(1);

        articleTaskService.publishArticle(1001L);

        assertEquals((byte) 9, article.getStatus());
        verify(apArticleMapper, times(1)).updateById(any(ApArticle.class));
    }
}