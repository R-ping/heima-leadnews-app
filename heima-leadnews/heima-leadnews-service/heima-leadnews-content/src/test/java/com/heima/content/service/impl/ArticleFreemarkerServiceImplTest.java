package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.apis.search.ISearchClient;
import com.heima.content.mapper.ApArticleContentMapper;
import com.heima.content.schedule.service.TaskService;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.file.config.MinIOConfig;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.search.vos.SearchArticleVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleFreemarkerServiceImplTest {

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private MinIOConfig prop;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @Mock
    private ISearchClient searchClient;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ArticleFreemarkerServiceImpl articleFreemarkerService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testBuildHTMLAndSendWithContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");
        article.setAuthorId(1L);
        article.setAuthorName("author");

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenReturn(null);

        articleFreemarkerService.buildHTMLAndSend(article, "# markdown content", 0L);

        // 验证调用了ES同步
        verify(searchClient, times(1)).syncArticle(any(SearchArticleVo.class));
        // 验证添加了延迟任务
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void testBuildHTMLAndSendWithoutContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");
        article.setAuthorId(1L);

        ApArticleContent articleContent = new ApArticleContent();
        articleContent.setArticleId(1L);
        articleContent.setContent("# content from db");
        when(apArticleContentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(articleContent);

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenReturn(null);

        articleFreemarkerService.buildHTMLAndSend(article, "", 0L);

        verify(searchClient, times(1)).syncArticle(any(SearchArticleVo.class));
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void testBuildHTMLAndSendWithNullContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(apArticleContentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenReturn(null);

        articleFreemarkerService.buildHTMLAndSend(article, null, 1000L);

        verify(searchClient, times(1)).syncArticle(any(SearchArticleVo.class));
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void testBuildHTMLAndSendEsSyncFailure() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        // ES同步失败时抛出异常，不应影响整体流程
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenThrow(new RuntimeException("ES sync failed"));

        assertDoesNotThrow(() -> articleFreemarkerService.buildHTMLAndSend(article, "# content", 0L));

        verify(searchClient, times(1)).syncArticle(any(SearchArticleVo.class));
        verify(taskService, times(1)).addTask(any(Task.class));
    }

    @Test
    void testBuildHTMLAndSendDelayTaskParameters() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenReturn(null);

        articleFreemarkerService.buildHTMLAndSend(article, "# content", 600000L); // 10分钟

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).addTask(taskCaptor.capture());
        Task task = taskCaptor.getValue();

        assertNotNull(task);
        assertEquals(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), task.getTaskType());
        assertEquals(TaskTypeEnum.NEWS_SCAN_TIME.getPriority(), task.getPriority());
        assertEquals(600000L, task.getObjExecInterval());
        assertNotNull(task.getParameters());
        assertNotNull(task.getExecuteTime());
    }

    @Test
    void testBuildHTMLAndSendImmediateTask() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");
        when(searchClient.syncArticle(any(SearchArticleVo.class))).thenReturn(null);

        // 立即执行（间隔为0）
        articleFreemarkerService.buildHTMLAndSend(article, "# content", 0L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService, times(1)).addTask(taskCaptor.capture());
        Task task = taskCaptor.getValue();

        assertEquals(0, task.getFirstExecInterval());
        assertEquals(0, task.getObjExecInterval());
    }
}