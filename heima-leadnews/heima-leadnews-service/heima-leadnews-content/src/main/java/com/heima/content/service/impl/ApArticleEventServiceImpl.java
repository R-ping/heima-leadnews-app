package com.heima.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.search.ISearchClient;
import com.heima.content.mapper.ApArticleEventMapper;
import com.heima.content.service.ApArticleEventService;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApArticleEventServiceImpl implements ApArticleEventService {

    @Autowired
    private ApArticleEventMapper apArticleEventMapper;

    @Autowired
    private ISearchClient searchClient;

    @Override
    public void updateEvent(ArticleEvent event) {
        apArticleEventMapper.updateArticleEvent(event);
    }

    @Scheduled(fixedRate = 20000)
    public void processEvent() {
        ArrayList<Long> success_list = new ArrayList<>();
        for (ArticleEvent event : apArticleEventMapper.loadArticleEvent()) {
            if (event.getSendStatus() == 2) {
                if (event.getMinioStatus() == 2 && event.getEsStatus() == 2) {
                    success_list.add(event.getArticleId());
                    continue;
                }
                long retry_time = event.getRetryTime().getTime();
                long expect_time = System.currentTimeMillis() - 5000;
                boolean isBackward = retry_time <= expect_time;
                // ES同步失败重试
                if (event.getEsStatus() == 1 && isBackward) {
                    try {
                        SearchArticleVo searchArticleVo = JSON.parseObject(event.getParameter(), SearchArticleVo.class);
                        if (searchArticleVo != null) {
                            searchClient.syncArticle(searchArticleVo);
                            event.setEsStatus((byte) 2);
                            apArticleEventMapper.updateArticleEvent(event);
                            log.info("ES同步重试成功, articleId={}", event.getArticleId());
                        }
                    } catch (Exception e) {
                        log.error("ES同步重试失败, articleId={}", event.getArticleId(), e);
                    }
                }
                // MinIO失败日志记录（MinIO上传已在本地完成，不再需要重试）
                if (event.getMinioStatus() == 1 && isBackward) {
                    log.warn("MinIO上传失败, articleId={}", event.getArticleId());
                    // 标记为失败，不再重试
                    event.setMinioStatus((byte) 2);
                    apArticleEventMapper.updateArticleEvent(event);
                }
            } else if (event.getSendStatus() == 1) {
                // 生产者投递失败（RabbitMQ已移除，此状态不再出现），直接标记为失败
                log.warn("文章事件发送状态异常, articleId={}, 直接标记完成", event.getArticleId());
                event.setSendStatus((byte) 2);
                event.setEsStatus((byte) 2);
                event.setMinioStatus((byte) 2);
                apArticleEventMapper.updateArticleEvent(event);
            }
        }
        apArticleEventMapper.deleteArticleEvent(success_list);
    }

}
