package com.heima.apis.search.fallback;

import com.heima.apis.search.ISearchClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ISearchClientFallback implements ISearchClient {

    @Override
    public ResponseResult syncArticle(SearchArticleVo searchArticleVo) {
        log.error("远程同步文章到ES索引异常, articleId={}", searchArticleVo != null ? searchArticleVo.getId() : null);
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "同步文章到ES索引异常");
    }
}