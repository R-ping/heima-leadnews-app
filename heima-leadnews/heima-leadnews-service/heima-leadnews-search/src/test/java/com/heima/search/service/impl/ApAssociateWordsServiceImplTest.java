package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.pojos.ApAssociateWords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ApAssociateWordsServiceImpl 单元测试
 * 测试联想词搜索和搜索次数统计
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("联想词服务测试")
class ApAssociateWordsServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ApAssociateWordsServiceImpl apAssociateWordsService;

    private UserSearchDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserSearchDto();
        validDto.setSearchWords("测试");
    }

    // ==================== search 方法 ====================

    @Test
    @DisplayName("search — 正常返回联想词列表")
    void testSearch_Success() {
        List<ApAssociateWords> mockList = new ArrayList<>();
        ApAssociateWords word1 = createAssociateWords("测试关键词1", 100);
        ApAssociateWords word2 = createAssociateWords("测试关键词2", 80);
        mockList.add(word1);
        mockList.add(word2);

        when(mongoTemplate.find(any(Query.class), eq(ApAssociateWords.class))).thenReturn(mockList);

        ResponseResult result = apAssociateWordsService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        @SuppressWarnings("unchecked")
        List<ApAssociateWords> data = (List<ApAssociateWords>) result.getData();
        assertEquals(2, data.size());
        assertEquals("测试关键词1", data.get(0).getAssociateWords());
        assertEquals("测试关键词2", data.get(1).getAssociateWords());
    }

    @Test
    @DisplayName("search — searchWords为blank返回参数错误")
    void testSearch_BlankSearchWords() {
        UserSearchDto dto = new UserSearchDto();
        dto.setSearchWords("   ");

        ResponseResult result = apAssociateWordsService.search(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
        verify(mongoTemplate, never()).find(any(Query.class), eq(ApAssociateWords.class));
    }

    @Test
    @DisplayName("search — searchWords为null返回参数错误")
    void testSearch_NullSearchWords() {
        UserSearchDto dto = new UserSearchDto();
        dto.setSearchWords(null);

        ResponseResult result = apAssociateWordsService.search(dto);

        assertNotNull(result);
        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), result.getCode());
    }

    @Test
    @DisplayName("search — 无匹配结果返回空列表")
    void testSearch_EmptyResults() {
        when(mongoTemplate.find(any(Query.class), eq(ApAssociateWords.class))).thenReturn(new ArrayList<>());

        ResponseResult result = apAssociateWordsService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<ApAssociateWords> data = (List<ApAssociateWords>) result.getData();
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("search — 模糊匹配返回相关结果")
    void testSearch_FuzzyMatch() {
        validDto.setSearchWords("Java");

        List<ApAssociateWords> mockList = new ArrayList<>();
        mockList.add(createAssociateWords("Java入门", 200));
        mockList.add(createAssociateWords("JavaScript教程", 150));
        mockList.add(createAssociateWords("Java高级", 120));

        when(mongoTemplate.find(any(Query.class), eq(ApAssociateWords.class))).thenReturn(mockList);

        ResponseResult result = apAssociateWordsService.search(validDto);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        List<ApAssociateWords> data = (List<ApAssociateWords>) result.getData();
        assertEquals(3, data.size());
        // 验证按searchCount降序排列
        assertEquals(200, data.get(0).getSearchCount());
        assertEquals(150, data.get(1).getSearchCount());
        assertEquals(120, data.get(2).getSearchCount());
    }

    @Test
    @DisplayName("search — dto为null时抛出NPE（由调用方保证）")
    void testSearch_NullDto() {
        // 当dto为null时，调用dto.getSearchWords()会抛出NPE
        assertThrows(NullPointerException.class, () -> apAssociateWordsService.search(null));
    }

    // ==================== incrementSearchCount 方法 ====================

    @Test
    @DisplayName("incrementSearchCount — 正常增加搜索次数")
    void testIncrementSearchCount_Success() {
        apAssociateWordsService.incrementSearchCount("测试关键词");

        verify(mongoTemplate).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(ApAssociateWords.class));
    }

    @Test
    @DisplayName("incrementSearchCount — 空白关键词不执行操作")
    void testIncrementSearchCount_BlankKeyword() {
        apAssociateWordsService.incrementSearchCount("   ");

        verify(mongoTemplate, never()).findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any());
    }

    @Test
    @DisplayName("incrementSearchCount — null关键词不执行操作")
    void testIncrementSearchCount_NullKeyword() {
        apAssociateWordsService.incrementSearchCount(null);

        verify(mongoTemplate, never()).findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any());
    }

    @Test
    @DisplayName("incrementSearchCount — 空字符串关键词不执行操作")
    void testIncrementSearchCount_EmptyKeyword() {
        apAssociateWordsService.incrementSearchCount("");

        verify(mongoTemplate, never()).findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any());
    }

    @Test
    @DisplayName("incrementSearchCount — 带前后空格的关键词trim后执行")
    void testIncrementSearchCount_WithWhitespace() {
        apAssociateWordsService.incrementSearchCount("  关键词  ");

        verify(mongoTemplate).findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(ApAssociateWords.class));
    }

    // ==================== helper ====================

    private ApAssociateWords createAssociateWords(String words, int searchCount) {
        ApAssociateWords aw = new ApAssociateWords();
        aw.setId("id_" + words);
        aw.setAssociateWords(words);
        aw.setSearchCount(searchCount);
        aw.setCreatedTime(new Date());
        return aw;
    }
}