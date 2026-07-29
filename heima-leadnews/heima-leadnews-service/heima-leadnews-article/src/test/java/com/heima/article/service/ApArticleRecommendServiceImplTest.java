package com.heima.article.service;

import com.heima.article.ArticleApplication;
import com.heima.model.article.dtos.ArticleRecommendDto;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires running services")
@SpringBootTest(classes = ArticleApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ApArticleRecommendService单元测试")
class ApArticleRecommendServiceImplTest {

    @Autowired
    private ApArticleRecommendService recommendService;

    @Test
    @Order(1)
    @DisplayName("推荐 - 正常推荐（无seed，自动生成）")
    void testRecommend_NoSeed() {
        ArticleRecommendDto dto = new ArticleRecommendDto();
        dto.setChannel("__all__");
        dto.setSize(5);

        ResponseResult result = recommendService.recommend(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        assertNotNull(data.get("seed"));
        assertNotNull(data.get("list"));
        assertTrue(data.get("seed") instanceof Long);
        assertTrue(((Long) data.get("seed")) != 0);
    }

    @Test
    @Order(2)
    @DisplayName("推荐 - 带seed分页一致性")
    void testRecommend_SameSeedSamePage() {
        ArticleRecommendDto dto1 = new ArticleRecommendDto();
        dto1.setChannel("__all__");
        dto1.setSize(10);
        // 先请求一次获取seed
        ResponseResult firstResult = recommendService.recommend(dto1);
        @SuppressWarnings("unchecked")
        Map<String, Object> firstData = (Map<String, Object>) firstResult.getData();
        Long seed = (Long) firstData.get("seed");

        // 使用相同seed再次请求
        ArticleRecommendDto dto2 = new ArticleRecommendDto();
        dto2.setChannel("__all__");
        dto2.setSize(10);
        dto2.setSeed(seed);
        dto2.setPage(0);

        ResponseResult secondResult = recommendService.recommend(dto2);
        @SuppressWarnings("unchecked")
        Map<String, Object> secondData = (Map<String, Object>) secondResult.getData();

        // 同一种子同一页应返回相同结果
        @SuppressWarnings("unchecked")
        List<Object> list1 = (List<Object>) firstData.get("list");
        @SuppressWarnings("unchecked")
        List<Object> list2 = (List<Object>) secondData.get("list");

        assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i));
        }
    }

    @Test
    @Order(3)
    @DisplayName("推荐 - 不同seed结果不同")
    void testRecommend_DifferentSeeds() {
        ArticleRecommendDto dto1 = new ArticleRecommendDto();
        dto1.setChannel("__all__");
        dto1.setSize(5);
        ResponseResult result1 = recommendService.recommend(dto1);

        ArticleRecommendDto dto2 = new ArticleRecommendDto();
        dto2.setChannel("__all__");
        dto2.setSize(5);
        // 不带seed，服务端会生成新的seed
        ResponseResult result2 = recommendService.recommend(dto2);

        @SuppressWarnings("unchecked")
        Map<String, Object> data1 = (Map<String, Object>) result1.getData();
        @SuppressWarnings("unchecked")
        Map<String, Object> data2 = (Map<String, Object>) result2.getData();

        Long seed1 = (Long) data1.get("seed");
        Long seed2 = (Long) data2.get("seed");
        // 两次请求应生成不同的种子
        assertNotEquals(seed1, seed2);
    }

    @Test
    @Order(4)
    @DisplayName("推荐 - 分页连续性（同seed翻页）")
    void testRecommend_Pagination() {
        Long seed = System.nanoTime();

        // 第一页
        ArticleRecommendDto dto1 = new ArticleRecommendDto();
        dto1.setChannel("__all__");
        dto1.setSize(5);
        dto1.setSeed(seed);
        dto1.setPage(0);
        ResponseResult result1 = recommendService.recommend(dto1);

        // 第二页
        ArticleRecommendDto dto2 = new ArticleRecommendDto();
        dto2.setChannel("__all__");
        dto2.setSize(5);
        dto2.setSeed(seed);
        dto2.setPage(1);
        ResponseResult result2 = recommendService.recommend(dto2);

        @SuppressWarnings("unchecked")
        Map<String, Object> data1 = (Map<String, Object>) result1.getData();
        @SuppressWarnings("unchecked")
        Map<String, Object> data2 = (Map<String, Object>) result2.getData();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list1 = (List<Map<String, Object>>) data1.get("list");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list2 = (List<Map<String, Object>>) data2.get("list");

        // 两页不应有重复文章
        if (!list1.isEmpty() && !list2.isEmpty()) {
            for (Map<String, Object> item1 : list1) {
                for (Map<String, Object> item2 : list2) {
                    assertNotEquals(item1.get("id"), item2.get("id"),
                            "分页之间不应有重复文章");
                }
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("推荐 - 空候选池处理")
    void testRecommend_EmptyChannel() {
        ArticleRecommendDto dto = new ArticleRecommendDto();
        dto.setChannel("99999"); // 不存在的频道
        dto.setSize(10);

        ResponseResult result = recommendService.recommend(dto);
        assertNotNull(result);
        assertEquals(200, result.getCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data);
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) data.get("list");
        assertTrue(list.isEmpty());
        assertEquals(false, data.get("hasMore"));
        assertEquals(0, data.get("total"));
    }
}