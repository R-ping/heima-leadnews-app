package com.heima.article.service.impl;

import com.heima.article.mapper.TagMapper;
import com.heima.model.article.pojos.ApTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("标签服务测试")
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tagService, "baseMapper", tagMapper);
    }

    // ==================== findList ====================

    @Test
    @DisplayName("查询标签列表 - 关键字为null，返回所有启用标签")
    void testFindList_NullKeyword() {
        ApTag tag1 = new ApTag();
        tag1.setId(1);
        tag1.setName("Java");
        ApTag tag2 = new ApTag();
        tag2.setId(2);
        tag2.setName("Spring");
        when(tagMapper.selectList(any())).thenReturn(Arrays.asList(tag1, tag2));

        List<ApTag> result = tagService.findList(null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("查询标签列表 - 按关键字模糊匹配")
    void testFindList_WithKeyword() {
        ApTag tag = new ApTag();
        tag.setId(1);
        tag.setName("Spring Boot");
        when(tagMapper.selectList(any())).thenReturn(Collections.singletonList(tag));

        List<ApTag> result = tagService.findList("Spring");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Spring Boot", result.get(0).getName());
    }

    @Test
    @DisplayName("查询标签列表 - 关键字为空字符串")
    void testFindList_EmptyKeyword() {
        when(tagMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ApTag> result = tagService.findList("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询标签列表 - 关键字为空白字符串")
    void testFindList_BlankKeyword() {
        when(tagMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ApTag> result = tagService.findList("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询标签列表 - 无匹配结果返回空列表")
    void testFindList_NoMatch() {
        when(tagMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ApTag> result = tagService.findList("不存在的标签");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}