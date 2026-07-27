package com.heima.article.controller.v1;

import com.heima.article.service.TopicService;
import com.heima.model.article.pojos.ApTopic;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicController 单元测试")
class TopicControllerTest {

    @Mock
    private TopicService topicService;

    @InjectMocks
    private TopicController topicController;

    @Nested
    @DisplayName("findList() - 查询话题列表")
    class FindListTests {

        @Test
        @DisplayName("正常查询话题列表，返回成功结果")
        void shouldReturnTopicList() {
            List<ApTopic> mockData = Collections.singletonList(new ApTopic());
            when(topicService.findList("java")).thenReturn(mockData);

            ResponseResult result = topicController.findList("java");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(topicService).findList("java");
        }

        @Test
        @DisplayName("keyword为null时，正常委托给service")
        void shouldDelegateToServiceWhenKeywordIsNull() {
            List<ApTopic> mockData = Collections.emptyList();
            when(topicService.findList(isNull())).thenReturn(mockData);

            ResponseResult result = topicController.findList(null);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(topicService).findList(null);
        }

        @Test
        @DisplayName("keyword为空字符串时，正常委托给service")
        void shouldDelegateToServiceWhenKeywordIsEmpty() {
            List<ApTopic> mockData = Collections.emptyList();
            when(topicService.findList("")).thenReturn(mockData);

            ResponseResult result = topicController.findList("");

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(topicService).findList("");
        }

        @Test
        @DisplayName("Service抛出异常时，异常向上传播")
        void shouldPropagateException() {
            when(topicService.findList("invalid")).thenThrow(new RuntimeException("服务异常"));

            assertThrows(RuntimeException.class, () -> topicController.findList("invalid"));
        }
    }

    @Nested
    @DisplayName("getRecommendTopics() - 获取推荐话题")
    class GetRecommendTopicsTests {

        @Test
        @DisplayName("正常获取推荐话题，返回成功结果")
        void shouldReturnRecommendTopics() {
            List<ApTopic> mockData = Collections.singletonList(new ApTopic());
            when(topicService.getRecommendTopics()).thenReturn(mockData);

            ResponseResult result = topicController.getRecommendTopics();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
            verify(topicService).getRecommendTopics();
        }

        @Test
        @DisplayName("推荐话题为空列表时，正常返回")
        void shouldReturnEmptyRecommendTopics() {
            List<ApTopic> mockData = Collections.emptyList();
            when(topicService.getRecommendTopics()).thenReturn(mockData);

            ResponseResult result = topicController.getRecommendTopics();

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(mockData, result.getData());
        }
    }
}