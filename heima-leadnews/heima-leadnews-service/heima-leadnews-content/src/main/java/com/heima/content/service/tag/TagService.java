package com.heima.content.service.tag;

import com.heima.model.tag.pojos.ApTag;
import java.util.List;
import java.util.Map;

public interface TagService {

    /**
     * 查询标签列表
     * @param keyword 关键字
     * @return
     */
    List<ApTag> findList(String keyword);

    /**
     * 查询指定分类下文章使用的标签及其数量
     * @param categoryId 分类ID（频道ID）
     * @return 标签名和文章数的列表
     */
    List<Map<String, Object>> findTagsByCategory(Integer categoryId);
}