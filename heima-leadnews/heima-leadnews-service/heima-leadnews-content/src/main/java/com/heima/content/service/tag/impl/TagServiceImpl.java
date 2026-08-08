package com.heima.content.service.tag.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.content.mapper.article.ApArticleMapper;
import com.heima.content.mapper.tag.TagMapper;
import com.heima.content.service.tag.TagService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.tag.pojos.ApTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, ApTag> implements TagService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    @Override
    public List<ApTag> findList(String keyword) {
        LambdaQueryWrapper<ApTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApTag::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ApTag::getName, keyword.trim());
        }
        wrapper.orderByAsc(ApTag::getSort);
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> findTagsByCategory(Integer categoryId) {
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getChannelId, categoryId)
               .eq(ApArticle::getStatus, (byte) 9);
        List<ApArticle> articles = apArticleMapper.selectList(wrapper);

        Map<String, Integer> tagCountMap = new HashMap<>();
        for (ApArticle article : articles) {
            List<String> tags = article.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    if (tag != null && !tag.trim().isEmpty()) {
                        tagCountMap.merge(tag.trim(), 1, Integer::sum);
                    }
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tagCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("tagName", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        result.sort((a, b) -> (Integer) b.get("count") - (Integer) a.get("count"));
        return result;
    }
}