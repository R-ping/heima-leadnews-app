package com.heima.model.article.dtos;

import lombok.Data;

@Data
public class ArticleRecommendDto {
    private String channel;   // 频道ID，__all__表示全站
    private Integer size;     // 每页大小，默认10
    private Long seed;        // 随机种子，null时服务端生成
    private Integer page;     // 页码，从0开始
    private String tagName;   // 标签名过滤
}