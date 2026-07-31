package com.heima.article.service.impl;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.schedule.dtos.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleTaskServiceImplTest {

    @Mock
    private IScheduleClient scheduleClient;

    @Mock
    private ApArticleMapper apArticleMapper;

    @InjectMocks
    private ArticleTaskServiceImpl articleTaskService;

    // ==================== addArticleToTask() tests ====================

    @Test
    void testAddArticleToTaskWithImmediatePublish() {
        Date publishTime = new Date(System.currentTimeMillis() - 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1L, publishTime);

        verify(scheduleClient).addTask(any(Task.class));
    }

    @Test
    void testAddArticleToTaskWithFuturePublish() {
        Date publishTime = new Date(System.currentTimeMillis() + 60000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1L, publishTime);

        verify(scheduleClient).addTask(any(Task.class));
    }

    @Test
    void testAddArticleToTaskWithShortDelay() {
        Date publishTime = new Date(System.currentTimeMillis() + 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1L, publishTime);

        verify(scheduleClient).addTask(any(Task.class));
    }

    @Test
    void testAddArticleToTaskWithMediumDelay() {
        Date publishTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1L, publishTime);

        verify(scheduleClient).addTask(any(Task.class));
    }

    @Test
    void testAddArticleToTaskWithLongDelay() {
        Date publishTime = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
        doNothing().when(scheduleClient).addTask(any(Task.class));

        articleTaskService.addArticleToTask(1L, publishTime);

        verify(scheduleClient).addTask(any(Task.class));
    }

    // ==================== publishArticle() tests ====================

    @Test
    void testPublishArticleSuccess() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setStatus(ApArticle.Status.SUBMIT.getCode());
        when(apArticleMapper.selectById(1L)).thenReturn(article);
        doAnswer(invocation -> {
            ApArticle arg = invocation.getArgument(0);
            arg.setStatus(ApArticle.Status.PUBLISHED.getCode());
            return 1;
        }).when(apArticleMapper).updateById(any(ApArticle.class));

        articleTaskService.publishArticle(1L);

        verify(apArticleMapper).updateById(any(ApArticle.class));
    }

    @Test
    void testPublishArticleNotFound() {
        when(apArticleMapper.selectById(1L)).thenReturn(null);

        articleTaskService.publishArticle(1L);

        verify(apArticleMapper, never()).updateById(any(ApArticle.class));
    }
}