package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IArticleClientFallback implements IArticleClient {
//    @Override
//    public ResponseResult saveArticle(ArticleDto dto, Integer wmNewsId, long executeTime) {
//        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文章保存异常");
//    }
//
//    @Override
//    public ResponseResult saveArticle(Integer wmNewsId) {
//        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文章保存异常");
//    }

    @Override
    public ResponseResult saveArticle(ArticleDto dto, long executeTime) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文章保存异常");
    }

    @Override
    public void eventUpdate(ArticleEvent event) {
        System.out.println("远程更新article操作事件失败");
    }

    @Override
    public ResponseResult getContent(Long articleId) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取文章内容异常");
    }

    @Override
    public ResponseResult getArticleInfo(Long articleId) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取文章信息异常");
    }

    @Override
    public ResponseResult publishArticle(Long articleId) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文章发布异常");
    }

    @Override
    public List<ApArticle> listByAuthorId(ArticleDto dto) {
        return Collections.emptyList();
    }

    @Override
    public ResponseResult getStatisticsFeign(Long userId) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "获取用户统计数据异常");
    }

}
