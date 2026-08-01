package com.heima.content.service;

import com.heima.model.article.dtos.TopicSquareDto;
import com.heima.model.article.vos.TopicDetailVO;
import com.heima.model.article.vos.TopicRecommendVO;

import java.util.List;
import java.util.Map;

public interface TopicService {

    /**
     * 推荐话题（侧边栏"换一换"，支持分页轮换）
     */
    Map<String, Object> recommend(int page, int size);

    /**
     * 话题广场列表
     */
    Map<String, Object> square(TopicSquareDto dto);

    /**
     * 话题详情
     */
    TopicDetailVO detail(Long id);

    /**
     * 话题内容 Feed 流
     */
    Map<String, Object> feed(Long id, String tab, long cursor, int size);

    /**
     * 增加话题阅读量（防刷限流）
     */
    void incrView(Long topicId, Long userId);

    /**
     * 搜索话题
     */
    List<TopicRecommendVO> search(String keyword, int limit);
}