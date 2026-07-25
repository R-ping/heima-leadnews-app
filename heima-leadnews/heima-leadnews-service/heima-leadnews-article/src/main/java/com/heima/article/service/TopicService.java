package com.heima.article.service;

import com.heima.model.article.pojos.ApTopic;
import java.util.List;

public interface TopicService {

    /**
     * 查询话题列表
     * @param keyword 关键字
     * @return
     */
    List<ApTopic> findList(String keyword);

    /**
     * 获取推荐话题（按讨论数降序，Top 10）
     * @return
     */
    List<ApTopic> getRecommendTopics();
}