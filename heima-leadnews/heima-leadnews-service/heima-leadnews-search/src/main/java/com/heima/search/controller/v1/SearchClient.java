package com.heima.search.controller.v1;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search/sync")
@Slf4j
public class SearchClient {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private IArticleClient articleClient;

    /** 同步文章到 ES 索引（由 Feign 调用，替代原 RabbitMQ 监听器） */
    @PostMapping("/article")
    public ResponseResult syncArticle(@RequestBody SearchArticleVo searchArticleVo) {
        if (searchArticleVo == null || searchArticleVo.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章信息不能为空");
        }

        try {
            Long articleId = searchArticleVo.getId();

            // 获取文章内容
            ResponseResult contentResult = articleClient.getContent(articleId);
            if (contentResult != null && contentResult.getData() != null) {
                searchArticleVo.setContent(contentResult.getData().toString());
            }

            // 索引到 ES
            String msg = JSON.toJSONString(searchArticleVo);
            IndexRequest indexRequest = new IndexRequest("app_info_article");
            indexRequest.id(articleId.toString());
            indexRequest.source(msg, XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            log.info("文章同步到ES成功, articleId={}", articleId);
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        } catch (IOException e) {
            log.error("同步文章到ES索引失败, articleId={}", searchArticleVo.getId(), e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "同步文章到ES索引失败");
        }
    }
}