package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.content.mapper.TagMapper;
import com.heima.content.service.TagService;
import com.heima.model.article.pojos.ApTag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, ApTag> implements TagService {

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
}