package com.heima.article.service;

import com.heima.model.wemedia.pojos.WmTag;

import java.util.List;

public interface TagService {

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    List<WmTag> findList(String keyword);
}