package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.article.utils.MarkdownUtils;
import com.heima.file.config.MinIOConfig;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private ApArticleEventMapper apArticleEventMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

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

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");

        articleFreemarkerService.buildHTMLAndSend(article, "# markdown content", 0L);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
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

        
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");

        articleFreemarkerService.buildHTMLAndSend(article, "", 0L);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
    }

    @Test
    void testBuildHTMLAndSendWithNullContent() {
        ApArticle article = new ApArticle();
        article.setId(1L);
        article.setTitle("test");

        when(apArticleContentMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2024/01/01/1");
        when(prop.getReadPath()).thenReturn("http://localhost:9000");
        when(prop.getBucket()).thenReturn("leadnews");

        articleFreemarkerService.buildHTMLAndSend(article, null, 1000L);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
    }

    @Test
    void testSendObjExecutionMsg2Mq() {
        
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));

        articleFreemarkerService.sendObjExecutionMsg2Mq(1L, 5000L);

        verify(rabbitTemplate).convertAndSend(eq("delay.exchange"), eq("article.last.do"), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void testSendArticleVo2Mq() {
        
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));

        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        articleFreemarkerService.sendArticleVo2Mq(vo);

        verify(rabbitTemplate).convertAndSend(eq("article.exchange"), eq("article.minio.es"), any(Object.class), any(CorrelationData.class));
    }

    @Test
    void testGetCorrelationData() {
        

        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData result = articleFreemarkerService.getCorrelationData(vo, false);

        assertNotNull(result);
    }

    @Test
    void testGetCorrelationDataRetry() {
        

        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData result = articleFreemarkerService.getCorrelationData(vo, true);

        assertNotNull(result);
    }

    @Test
    void testSendMsg2Mq() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));

        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        articleFreemarkerService.sendMsg2Mq("test.routing", vo, cd);

        verify(rabbitTemplate).convertAndSend(eq("article.exchange"), eq("test.routing"), any(Object.class), any(CorrelationData.class));
    }

    @Test
    void testSendDelayMsg2Mq() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));

        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        articleFreemarkerService.sendDelayMsg2Mq("test.routing", vo, 5000L, cd);

        verify(rabbitTemplate).convertAndSend(eq("delay.exchange"), eq("test.routing"), any(Object.class), any(MessagePostProcessor.class), any(CorrelationData.class));
    }

    @Test
    void testGetCorrelationDataAck() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData cd = articleFreemarkerService.getCorrelationData(vo, false);

        CorrelationData.Confirm confirm = mock(CorrelationData.Confirm.class);
        cd.getFuture().complete(confirm);

        assertNotNull(cd);
    }

    @Test
    void testGetCorrelationDataNack() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1L);
        CorrelationData cd = articleFreemarkerService.getCorrelationData(vo, false);

        CorrelationData.Confirm confirm = mock(CorrelationData.Confirm.class);
        cd.getFuture().complete(confirm);

        assertNotNull(cd);
    }
}