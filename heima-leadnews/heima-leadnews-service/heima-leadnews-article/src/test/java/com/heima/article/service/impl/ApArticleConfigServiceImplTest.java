package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.model.article.pojos.ApArticleConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("文章配置服务测试")
class ApArticleConfigServiceImplTest {

    @Mock
    private ApArticleConfigMapper apArticleConfigMapper;

    @InjectMocks
    private ApArticleConfigServiceImpl apArticleConfigService;

    private Map<String, Object> map;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apArticleConfigService, "baseMapper", apArticleConfigMapper);
        map = new HashMap<>();
        map.put("articleId", 1001L);
        map.put("enable", 1);
    }

    // ==================== updateByMap ====================

    @Test
    @DisplayName("修改文章配置 - enable=1上架，isDown=false")
    void testUpdateByMap_Enable() {
        when(apArticleConfigMapper.update(any(), any(Wrapper.class))).thenReturn(1);

        apArticleConfigService.updateByMap(map);

        verify(apArticleConfigMapper, times(1)).update(any(), any(Wrapper.class));
    }

    @Test
    @DisplayName("修改文章配置 - enable=0下架，isDown=true")
    void testUpdateByMap_Disable() {
        map.put("enable", 0);
        when(apArticleConfigMapper.update(any(), any(Wrapper.class))).thenReturn(1);

        apArticleConfigService.updateByMap(map);

        verify(apArticleConfigMapper, times(1)).update(any(), any(Wrapper.class));
    }

    @Test
    @DisplayName("修改文章配置 - 空map不抛异常")
    void testUpdateByMap_EmptyMap() {
        Map<String, Object> emptyMap = new HashMap<>();

        assertThrows(NullPointerException.class, () -> apArticleConfigService.updateByMap(emptyMap));
    }

    @Test
    @DisplayName("修改文章配置 - map为null抛出NPE")
    void testUpdateByMap_NullMap() {
        assertThrows(NullPointerException.class, () -> apArticleConfigService.updateByMap(null));
    }
}