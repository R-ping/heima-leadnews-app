package com.heima.article.service;

import com.heima.model.wemedia.pojos.WmTopic;

import java.util.List;

public interface TopicService {

    /**
     * 查询话题列表
     * @param keyword 关键字
     * @return
     */
    List<WmTopic> findList(String keyword);
}