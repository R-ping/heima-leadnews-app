package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章事件服务测试")
class ApArticleEventServiceImplTest {

    @Mock
    private ApArticleEventMapper apArticleEventMapper;

    @Mock
    private ArticleFreemarkerService articleFreemarkerService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ApArticleEventServiceImpl apArticleEventService;

    // ==================== updateEvent ====================

    @Test
    @DisplayName("更新文章事件 - 调用mapper更新")
    void testUpdateEvent_Success() {
        ArticleEvent event = new ArticleEvent();
        event.setId(1L);
        event.setArticleId(1001L);

        doNothing().when(apArticleEventMapper).updateArticleEvent(any(ArticleEvent.class));

        apArticleEventService.updateEvent(event);

        verify(apArticleEventMapper, times(1)).updateArticleEvent(event);
    }

    // ==================== processEvent ====================

    @Test
    @DisplayName("处理文章事件 - 无事件不处理")
    void testProcessEvent_NoEvents() {
        when(apArticleEventMapper.loadArticleEvent()).thenReturn(new ArrayList<>());

        apArticleEventService.processEvent();

        verify(apArticleEventMapper, times(1)).loadArticleEvent();
        verify(apArticleEventMapper, times(1)).deleteArticleEvent(any());
    }

    @Test
    @DisplayName("处理文章事件 - 完全成功事件删除")
    void testProcessEvent_AllCompleted() {
        ArrayList<ArticleEvent> events = new ArrayList<>();
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(1001L);
        event.setSendStatus((byte) 2);
        event.setMinioStatus((byte) 2);
        event.setEsStatus((byte) 2);
        events.add(event);
        when(apArticleEventMapper.loadArticleEvent()).thenReturn(events);

        apArticleEventService.processEvent();

        verify(apArticleEventMapper, times(1)).deleteArticleEvent(any());
    }

    @Test
    @DisplayName("处理文章事件 - 需要重试ES")
    void testProcessEvent_RetryEs() {
        ArrayList<ArticleEvent> events = new ArrayList<>();
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(1001L);
        event.setSendStatus((byte) 2);
        event.setMinioStatus((byte) 2);
        event.setEsStatus((byte) 1);
        event.setRetryTime(new Date(System.currentTimeMillis() - 10000));
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);
        event.setParameter(JSON.toJSONString(vo));
        events.add(event);
        when(apArticleEventMapper.loadArticleEvent()).thenReturn(events);
        when(articleFreemarkerService.getCorrelationData(any(), eq(true))).thenReturn(new CorrelationData("test"));
        doNothing().when(articleFreemarkerService).sendMsg2Mq(anyString(), any(SearchArticleVo.class), any(CorrelationData.class));

        apArticleEventService.processEvent();

        verify(articleFreemarkerService, times(1)).sendMsg2Mq(eq("article.resend.es"), any(SearchArticleVo.class), any(CorrelationData.class));
    }

    @Test
    @DisplayName("处理文章事件 - 需要重试Minio")
    void testProcessEvent_RetryMinio() {
        ArrayList<ArticleEvent> events = new ArrayList<>();
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(1001L);
        event.setSendStatus((byte) 2);
        event.setMinioStatus((byte) 1);
        event.setEsStatus((byte) 2);
        event.setRetryTime(new Date(System.currentTimeMillis() - 10000));
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);
        event.setParameter(JSON.toJSONString(vo));
        events.add(event);
        when(apArticleEventMapper.loadArticleEvent()).thenReturn(events);
        when(articleFreemarkerService.getCorrelationData(any(), eq(true))).thenReturn(new CorrelationData("test"));
        doNothing().when(articleFreemarkerService).sendMsg2Mq(anyString(), any(SearchArticleVo.class), any(CorrelationData.class));

        apArticleEventService.processEvent();

        verify(articleFreemarkerService, times(1)).sendMsg2Mq(eq("article.minio.resend"), any(SearchArticleVo.class), any(CorrelationData.class));
    }

    @Test
    @DisplayName("处理文章事件 - 发送失败达到重试次数发送死信")
    void testProcessEvent_SendFailureMaxRetry() {
        ArrayList<ArticleEvent> events = new ArrayList<>();
        ArticleEvent event = new ArticleEvent();
        event.setArticleId(1001L);
        event.setSendStatus((byte) 1);
        event.setRetryCount((byte) 2);
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(1001L);
        event.setParameter(JSON.toJSONString(vo));
        events.add(event);
        when(apArticleEventMapper.loadArticleEvent()).thenReturn(events);
        doNothing().when(rabbitTemplate).convertAndSend(eq("error.direct"), eq("error"), any(String.class));

        apArticleEventService.processEvent();

        verify(rabbitTemplate, times(1)).convertAndSend(eq("error.direct"), eq("error"), any(String.class));
    }
}