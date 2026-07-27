package com.heima.article.service.impl;

import com.heima.article.mapper.ApAuthorMapper;
import com.heima.model.article.dtos.ApAuthor;
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
@DisplayName("作者服务测试")
class ApAuthorServiceImplTest {

    @Mock
    private ApAuthorMapper apAuthorMapper;

    @InjectMocks
    private ApAuthorServiceImpl apAuthorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apAuthorService, "baseMapper", apAuthorMapper);
    }

    // ==================== 继承自ServiceImpl的基类方法 ====================

    @Test
    @DisplayName("保存作者 - 调用基类save方法")
    void testSave() {
        ApAuthor author = new ApAuthor();
        author.setName("测试作者");
        when(apAuthorMapper.insert(any(ApAuthor.class))).thenReturn(1);

        boolean result = apAuthorService.save(author);

        assertTrue(result);
        verify(apAuthorMapper, times(1)).insert(any(ApAuthor.class));
    }

    @Test
    @DisplayName("根据ID查询作者 - 调用基类getById方法")
    void testGetById() {
        ApAuthor author = new ApAuthor();
        author.setId(1);
        author.setName("测试作者");
        when(apAuthorMapper.selectById(1)).thenReturn(author);

        ApAuthor result = apAuthorService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试作者", result.getName());
        verify(apAuthorMapper, times(1)).selectById(1);
    }

    @Test
    @DisplayName("根据ID查询作者 - 不存在返回null")
    void testGetById_NotFound() {
        when(apAuthorMapper.selectById(999)).thenReturn(null);

        ApAuthor result = apAuthorService.getById(999);

        assertNull(result);
    }
}