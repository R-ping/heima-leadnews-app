package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.TopicMapper;
import com.heima.article.service.TopicService;
import com.heima.model.article.pojos.ApTopic;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TopicServiceImpl extends ServiceImpl<TopicMapper, ApTopic> implements TopicService {

    /**
     * 查询话题列表
     * @param keyword 关键字
     * @return
     */
    @Override
    public List<ApTopic> findList(String keyword) {
        LambdaQueryWrapper<ApTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApTopic::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ApTopic::getName, keyword.trim());
        }
        wrapper.orderByAsc(ApTopic::getSortOrder);
        return list(wrapper);
    }
}