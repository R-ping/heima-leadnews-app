package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.vos.SearchArticleVo;
import com.rabbitmq.client.Channel;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SyncArticleListener 单元测试
 * 测试文章同步到ES的RabbitMQ监听器
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文章同步ES监听器测试")
class SyncArticleListenerTest {

    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Mock
    private IArticleClient articleClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SyncArticleListener syncArticleListener;

    @Mock
    private Message message;

    @Mock
    private Channel channel;

    @Mock
    private MessageProperties messageProperties;

    private SearchArticleVo vo;
    private static final Long TEST_ARTICLE_ID = 1001L;
    private static final String TEST_CONTENT = "这是一篇测试文章的内容";
    private static final long DELIVERY_TAG = 1L;

    @BeforeEach
    void setUp() {
        vo = new SearchArticleVo();
        vo.setId(TEST_ARTICLE_ID);
        vo.setTitle("测试文章标题");
        vo.setAuthorName("测试作者");

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(DELIVERY_TAG);
    }

    // ==================== onMessage 方法测试 ====================

    @Nested
    @DisplayName("onMessage 方法测试")
    class OnMessageTests {

        @Test
        @DisplayName("正常流程 — 索引文档成功并手动ack和发送成功通知")
        void shouldIndexDocumentAndAckSuccessfully() throws IOException {
            ResponseResult contentResult = ResponseResult.okResult(TEST_CONTENT);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);
            when(restHighLevelClient.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT)))
                    .thenReturn(null);

            syncArticleListener.onMessage(message, vo, channel);

            // 验证获取了文章内容
            verify(articleClient).getContent(TEST_ARTICLE_ID);
            // 验证ES索引操作
            verify(restHighLevelClient).index(argThat((IndexRequest req) ->
                    "app_info_article".equals(req.index()) &&
                            req.id().equals(TEST_ARTICLE_ID.toString())
            ), eq(RequestOptions.DEFAULT));
            // 验证手动ack
            verify(channel).basicAck(DELIVERY_TAG, false);
            // 验证发送成功通知
            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(rabbitTemplate).convertAndSend(eq("process.exchange"), eq("process.result"), mapCaptor.capture());
            Map<String, String> sentMap = mapCaptor.getValue();
            assertEquals("success", sentMap.get("status"));
            assertEquals(TEST_ARTICLE_ID.toString(), sentMap.get("articleId"));
        }

        @Test
        @DisplayName("vo为null — 跳过处理不执行任何操作")
        void shouldSkipProcessingWhenVoIsNull() throws IOException {
            syncArticleListener.onMessage(message, null, channel);

            verify(articleClient, never()).getContent(anyLong());
            verify(restHighLevelClient, never()).index(any(), any());
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        @DisplayName("articleClient.getContent抛出IOException — 发送失败通知")
        void shouldSendFailResultWhenGetContentThrowsIOException() throws IOException {
            when(articleClient.getContent(TEST_ARTICLE_ID))
                    .thenThrow(new IOException("Feign调用失败"));

            syncArticleListener.onMessage(message, vo, channel);

            // 验证发送失败通知
            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(rabbitTemplate).convertAndSend(eq("process.exchange"), eq("process.result"), mapCaptor.capture());
            Map<String, String> sentMap = mapCaptor.getValue();
            assertEquals("fail", sentMap.get("status"));
            assertEquals(TEST_ARTICLE_ID.toString(), sentMap.get("articleId"));
            // 验证ES索引未执行
            verify(restHighLevelClient, never()).index(any(), any());
            // 验证未进行ack
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
        }

        @Test
        @DisplayName("restHighLevelClient.index抛出IOException — 发送失败通知")
        void shouldSendFailResultWhenIndexThrowsIOException() throws IOException {
            ResponseResult contentResult = ResponseResult.okResult(TEST_CONTENT);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);
            when(restHighLevelClient.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT)))
                    .thenThrow(new IOException("ES写入失败"));

            syncArticleListener.onMessage(message, vo, channel);

            // 验证发送失败通知
            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(rabbitTemplate).convertAndSend(eq("process.exchange"), eq("process.result"), mapCaptor.capture());
            Map<String, String> sentMap = mapCaptor.getValue();
            assertEquals("fail", sentMap.get("status"));
            assertEquals(TEST_ARTICLE_ID.toString(), sentMap.get("articleId"));
            // 验证未进行ack
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
        }

        @Test
        @DisplayName("articleClient.getContent返回null — 抛出NPE被Exception捕获")
        void shouldCatchExceptionWhenGetContentReturnsNull() throws IOException {
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(null);

            // null.getData() 会抛出 NPE，被 catch(Exception) 捕获
            syncArticleListener.onMessage(message, vo, channel);

            // 验证ES未执行
            verify(restHighLevelClient, never()).index(any(), any());
            // 验证未发送rabbit消息
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }

        @Test
        @DisplayName("ES索引抛出Exception(非IOException) — 被Exception捕获")
        void shouldCatchExceptionWhenIndexThrowsGenericException() throws IOException {
            ResponseResult contentResult = ResponseResult.okResult(TEST_CONTENT);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);
            when(restHighLevelClient.index(any(IndexRequest.class), eq(RequestOptions.DEFAULT)))
                    .thenThrow(new RuntimeException("ES意外错误"));

            syncArticleListener.onMessage(message, vo, channel);

            // 验证未发送rabbit消息（RuntimeException 被 catch(Exception) 捕获，只记录日志）
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
            // 验证未进行ack
            verify(channel, never()).basicAck(anyLong(), anyBoolean());
        }

        @Test
        @DisplayName("正常流程 — 验证ES索引内容包含article字段")
        void shouldIndexWithCorrectContent() throws IOException {
            String rawContent = "完整的文章Markdown内容";
            ResponseResult contentResult = ResponseResult.okResult(rawContent);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);

            syncArticleListener.onMessage(message, vo, channel);

            // 验证vo的content被设置
            assertEquals(rawContent, vo.getContent());
            // 验证ES索引请求
            ArgumentCaptor<IndexRequest> requestCaptor = ArgumentCaptor.forClass(IndexRequest.class);
            verify(restHighLevelClient).index(requestCaptor.capture(), eq(RequestOptions.DEFAULT));
            IndexRequest request = requestCaptor.getValue();
            assertEquals("app_info_article", request.index());
            assertEquals(TEST_ARTICLE_ID.toString(), request.id());
        }

        @Test
        @DisplayName("正常流程 — 验证ack的deliveryTag正确")
        void shouldAckWithCorrectDeliveryTag() throws IOException {
            long expectedTag = 42L;
            when(messageProperties.getDeliveryTag()).thenReturn(expectedTag);
            ResponseResult contentResult = ResponseResult.okResult(TEST_CONTENT);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);

            syncArticleListener.onMessage(message, vo, channel);

            verify(channel).basicAck(expectedTag, false);
        }

        @Test
        @DisplayName("正常流程 — 验证成功通知中包含articleId和type")
        void shouldIncludeArticleIdAndTypeInSuccessNotification() throws IOException {
            ResponseResult contentResult = ResponseResult.okResult(TEST_CONTENT);
            when(articleClient.getContent(TEST_ARTICLE_ID)).thenReturn(contentResult);

            syncArticleListener.onMessage(message, vo, channel);

            ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(rabbitTemplate).convertAndSend(eq("process.exchange"), eq("process.result"), mapCaptor.capture());
            Map<String, String> sentMap = mapCaptor.getValue();
            assertEquals(TEST_ARTICLE_ID.toString(), sentMap.get("articleId"));
            assertEquals("minio", sentMap.get("type"));
            assertEquals("success", sentMap.get("status"));
        }

        @Test
        @DisplayName("articleId为null时 — 异常被捕获不崩溃")
        void shouldNotCrashWhenArticleIdIsNull() throws IOException {
            vo.setId(null);
            // articleId = null.toString() → NPE, 被catch(Exception)捕获
            syncArticleListener.onMessage(message, vo, channel);

            // 验证方法未崩溃
            verify(articleClient, never()).getContent(anyLong());
            verify(restHighLevelClient, never()).index(any(), any());
            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
        }
    }
}