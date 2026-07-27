package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.model.article.pojos.ApArticleContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章内容服务测试")
class ApArticleContentServiceImplTest {

    @Mock
    private ApArticleContentMapper apArticleContentMapper;

    @InjectMocks
    private ApArticleContentServiceImpl apArticleContentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apArticleContentService, "baseMapper", apArticleContentMapper);
    }

    // ==================== 继承自ServiceImpl的基类方法 ====================

    @Test
    @DisplayName("保存文章内容 - 调用基类save方法")
    void testSave() {
        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1001L);
        content.setContent("测试内容");
        when(apArticleContentMapper.insert(any(ApArticleContent.class))).thenReturn(1);

        boolean result = apArticleContentService.save(content);

        assertTrue(result);
        verify(apArticleContentMapper, times(1)).insert(any(ApArticleContent.class));
    }

    @Test
    @DisplayName("根据ID查询文章内容 - 调用基类getById方法")
    void testGetById() {
        ApArticleContent content = new ApArticleContent();
        content.setArticleId(1001L);
        when(apArticleContentMapper.selectById(1001L)).thenReturn(content);

        ApArticleContent result = apArticleContentService.getById(1001L);

        assertNotNull(result);
        assertEquals(1001L, result.getArticleId());
        verify(apArticleContentMapper, times(1)).selectById(1001L);
    }

    @Test
    @DisplayName("根据ID查询文章内容 - 不存在返回null")
    void testGetById_NotFound() {
        when(apArticleContentMapper.selectById(999L)).thenReturn(null);

        ApArticleContent result = apArticleContentService.getById(999L);

        assertNull(result);
    }
}