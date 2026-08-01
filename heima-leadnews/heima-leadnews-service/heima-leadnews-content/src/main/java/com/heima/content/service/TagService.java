package com.heima.content.service;

import com.heima.model.article.pojos.ApTag;
import java.util.List;

public interface TagService {

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    List<ApTag> findList(String keyword);
}