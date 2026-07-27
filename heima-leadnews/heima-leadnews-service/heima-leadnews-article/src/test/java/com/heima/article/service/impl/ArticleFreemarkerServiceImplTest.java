package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.file.config.MinIOConfig;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.search.vos.SearchArticleVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章Freemarker服务测试")
class ArticleFreemarkerServiceImplTest {

    @Mock
    private MinioUtil minioUtil;

    @Mock
    private MinIOConfig minIOConfig;

    @Mock
    private ApArticleEventMapper apArticleEventMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @InjectMocks
    private ArticleFreemarkerServiceImpl articleFreemarkerService;

    // ==================== buildHTMLAndSend ====================

    @Test
    @DisplayName("构建HTML并发送 - 正常调用")
    void testBuildHTMLAndSend_Success() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        article.setAuthorName("作者");
        article.setPublishTime(new Date());

        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2026/07/27/1001");
        when(minIOConfig.getReadPath()).thenReturn("http://localhost:9000");
        when(minIOConfig.getBucket()).thenReturn("leadnews");
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(), any(CorrelationData.class));

        articleFreemarkerService.buildHTMLAndSend(article, "# 测试内容", 5000L);

        // 验证调用了消息发送
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(SearchArticleVo.class), any(CorrelationData.class));
    }

    @Test
    @DisplayName("构建HTML并发送 - 内容为空从数据库读取")
    void testBuildHTMLAndSend_EmptyContent() {
        ApArticle article = new ApArticle();
        article.setId(1001L);
        article.setTitle("测试文章");
        article.setPublishTime(new Date());

        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1001L);
        content.setContent("# 数据库内容");
        when(apArticleContentMapper.selectOne(any())).thenReturn(content);
        when(minioUtil.builderFilePath(anyString(), anyString())).thenReturn("2026/07/27/1001");
        when(minIOConfig.getReadPath()).thenReturn("http://localhost:9000");
        when(minIOConfig.getBucket()).thenReturn("leadnews");
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(CorrelationData.class));
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(), any(CorrelationData.class));

        articleFreemarkerService.buildHTMLAndSend(article, "", 5000L);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(SearchArticleVo.class), any(CorrelationData.class));
    }

    // ==================== sendArticleVo2Mq ====================

    @Test
    @DisplayName("发送文章VO到MQ - 正常调用")
    void testSendArticleVo2Mq_Success() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);
        vo.setTitle("测试文章");

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(SearchArticleVo.class), any(CorrelationData.class));

        articleFreemarkerService.sendArticleVo2Mq(vo);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("article.exchange"), eq("article.minio.es"), eq(vo), any(CorrelationData.class));
    }

    // ==================== getCorrelationData ====================

    @Test
    @DisplayName("获取CorrelationData - 非重试模式")
    void testGetCorrelationData_NotRetry() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);

        CorrelationData result = articleFreemarkerService.getCorrelationData(vo, false);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getFuture());
    }

    @Test
    @DisplayName("获取CorrelationData - 重试模式")
    void testGetCorrelationData_Retry() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);

        CorrelationData result = articleFreemarkerService.getCorrelationData(vo, true);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getFuture());
    }

    // ==================== sendMsg2Mq ====================

    @Test
    @DisplayName("发送消息到MQ - 正常调用")
    void testSendMsg2Mq_Success() {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);
        CorrelationData correlationData = new CorrelationData("test-id");

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(SearchArticleVo.class), eq(correlationData));

        articleFreemarkerService.sendMsg2Mq("article.minio.es", vo, correlationData);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("article.exchange"), eq("article.minio.es"), eq(vo), eq(correlationData));
    }
}