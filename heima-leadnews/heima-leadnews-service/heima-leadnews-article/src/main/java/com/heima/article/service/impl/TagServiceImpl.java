package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.TagMapper;
import com.heima.article.service.TagService;
import com.heima.model.wemedia.pojos.WmTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, WmTag> implements TagService {

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    @Override
    public List<WmTag> findList(String keyword) {
        LambdaQueryWrapper<WmTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WmTag::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(WmTag::getName, keyword.trim());
        }
        wrapper.orderByAsc(WmTag::getSort);
        return list(wrapper);
    }
}