package com.heima.content.service.tag;

import com.heima.model.tag.pojos.ApTag;
import java.util.List;

public interface TagService {

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    List<ApTag> findList(String keyword);
}