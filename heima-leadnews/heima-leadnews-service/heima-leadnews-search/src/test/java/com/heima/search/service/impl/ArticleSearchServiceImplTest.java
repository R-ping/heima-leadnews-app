package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ApAssociateWordsService;
import com.heima.search.service.ApUserSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ArticleSearchServiceImpl 单元测试
 * 测试 ES 文章分页检索的各类场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文章搜索服务测试")
class ArticleSearchServiceImplTest {

    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Mock
    private ApUserSearchService apUserSearchService;

    @Mock
    private ApAssociateWordsService apAssociateWordsService;

    @InjectMocks
    private ArticleSearchServiceImpl articleSearchService;

    private UserSearchDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserSearchDto();
        validDto.setSearchWords("测试关键词");
        validDto.setPageNum(1);
        validDto.setPageSize(10);
        validDto.setMinBehotTime(new Date());
    }

    // ==================== search 方法 ====================

    @Test
    @DisplayName("search — 正常检索返回高亮结果")
    void testSearch_WithHighlights() throws IOException {
        // 准备 ES mock 响应
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit searchHit = mock(SearchHit.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{searchHit});
        when(searchHit.getSourceAsString()).thenReturn("{\"title\":\"原始标题\",\"content\":\"测试内容\"}");

        // 模拟高亮字段
        Map<String, HighlightField> highlightFields = new HashMap<>();
        HighlightField highlightField = mock(HighlightField.class);
        Text[] texts = new Text[]{new Text("<font style='color: red; font-size: inherit;'>高亮标题</font>")};
        when(highlightField.getFragments()).thenReturn(texts);
        highlightFields.put("title", highlightField);
        when(searchHit.getHighlightFields()).thenReturn(highlightFields);

        ResponseResult result = articleSearchService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertInstanceOf(List.class, result.getData());

        @SuppressWarnings("unchecked")
        List<Map> list = (List<Map>) result.getData();
        assertEquals(1, list.size());
        assertEquals("<font style='color: red; font-size: inherit;'>高亮标题</font>", list.get(0).get("h_title"));

        verify(apAssociateWordsService).incrementSearchCount("测试关键词");
        verify(restHighLevelClient).search(any(SearchRequest.class), eq(RequestOptions.DEFAULT));
    }

    @Test
    @DisplayName("search — 无高亮时返回原始标题")
    void testSearch_NoHighlights() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit searchHit = mock(SearchHit.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{searchHit});
        when(searchHit.getSourceAsString()).thenReturn("{\"title\":\"原始标题\",\"content\":\"测试内容\"}");
        when(searchHit.getHighlightFields()).thenReturn(Collections.emptyMap());

        ResponseResult result = articleSearchService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<Map> list = (List<Map>) result.getData();
        assertEquals(1, list.size());
        assertEquals("原始标题", list.get(0).get("h_title"));
    }

    @Test
    @DisplayName("search — 高亮字段为null时使用原始标题")
    void testSearch_NullHighlightFields() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit searchHit = mock(SearchHit.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{searchHit});
        when(searchHit.getSourceAsString()).thenReturn("{\"title\":\"原始标题\"}");
        when(searchHit.getHighlightFields()).thenReturn(null);

        ResponseResult result = articleSearchService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<Map> list = (List<Map>) result.getData();
        assertEquals("原始标题", list.get(0).get("h_title"));
    }

    @Test
    @DisplayName("search — ES返回空结果")
    void testSearch_EmptyResults() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[0]);

        ResponseResult result = articleSearchService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<Map> list = (List<Map>) result.getData();
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("search — dto为null返回参数错误")
    void testSearch_NullDto() throws IOException {
        ResponseResult result = articleSearchService.search(null);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verify(restHighLevelClient, never()).search(any(), any());
    }

    @Test
    @DisplayName("search — searchWords为空返回参数错误")
    void testSearch_BlankSearchWords() throws IOException {
        UserSearchDto dto = new UserSearchDto();
        dto.setSearchWords("   ");
        dto.setMinBehotTime(new Date());

        ResponseResult result = articleSearchService.search(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verify(restHighLevelClient, never()).search(any(), any());
    }

    @Test
    @DisplayName("search — searchWords为null返回参数错误")
    void testSearch_NullSearchWords() throws IOException {
        UserSearchDto dto = new UserSearchDto();
        dto.setSearchWords(null);
        dto.setMinBehotTime(new Date());

        ResponseResult result = articleSearchService.search(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    @DisplayName("search — ES抛出IOException时异常向上传播")
    void testSearch_IOException() throws IOException {
        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenThrow(new IOException("ES连接失败"));

        assertThrows(IOException.class, () -> articleSearchService.search(validDto));
        verify(apAssociateWordsService).incrementSearchCount("测试关键词");
    }

    @Test
    @DisplayName("search — 验证incrementSearchCount被调用")
    void testSearch_IncrementSearchCountCalled() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[0]);

        articleSearchService.search(validDto);

        verify(apAssociateWordsService).incrementSearchCount("测试关键词");
    }

    @Test
    @DisplayName("search — 多条结果正常返回")
    void testSearch_MultipleResults() throws IOException {
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        SearchHit hit3 = mock(SearchHit.class);

        when(restHighLevelClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenReturn(searchResponse);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{hit1, hit2, hit3});

        when(hit1.getSourceAsString()).thenReturn("{\"title\":\"标题1\"}");
        when(hit1.getHighlightFields()).thenReturn(Collections.emptyMap());
        when(hit2.getSourceAsString()).thenReturn("{\"title\":\"标题2\"}");
        when(hit2.getHighlightFields()).thenReturn(Collections.emptyMap());
        when(hit3.getSourceAsString()).thenReturn("{\"title\":\"标题3\"}");
        when(hit3.getHighlightFields()).thenReturn(Collections.emptyMap());

        ResponseResult result = articleSearchService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<Map> list = (List<Map>) result.getData();
        assertEquals(3, list.size());
    }
}