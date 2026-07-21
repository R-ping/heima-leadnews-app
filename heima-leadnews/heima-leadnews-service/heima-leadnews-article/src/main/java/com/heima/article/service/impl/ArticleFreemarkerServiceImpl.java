package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleEventMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.article.utils.MarkdownUtils;
import com.heima.file.config.MinIOConfig;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ArticleEvent;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.model.search.vos.TocItem;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {


    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private MinIOConfig prop;
    @Autowired
    private ApArticleEventMapper apArticleEventMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    /**
     * 生成静态文件上传到minIO中
     */
    @Async
    @Override
    public void buildHTMLAndSend(ApArticle apArticle, String content, long lastTime) {
        //
        sendObjExecutionMsg2Mq(apArticle.getId(), lastTime);
        //已知文章的id
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        String markdown = resolveContent(apArticle.getId(), content);
        vo.setContent(markdown);
        buildHtmlContent(vo, markdown);
        buildFileNameAndPath(apArticle, vo);
        sendArticleVo2Mq(vo);
    }

    /**
     * 解析文章内容：优先使用传入的内容，为空则从数据库读取
     */
    private String resolveContent(Long articleId, String content) {
        if (StringUtils.isNotBlank(content)) {
            return MarkdownUtils.normalizeContent(content);
        }
        ApArticleContent articleContent = apArticleContentMapper.selectOne(
            Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, articleId));
        return articleContent != null ? MarkdownUtils.normalizeContent(articleContent.getContent()) : "";
    }

    /**
     * 将 Markdown 渲染为 HTML 并提取目录
     */
    private void buildHtmlContent(SearchArticleVo vo, String markdown) {
        String rawHtml = MarkdownUtils.toHtml(markdown);
        List<TocItem> tocList = MarkdownUtils.extractToc(rawHtml);
        String htmlContent = MarkdownUtils.injectHeadingAnchors(rawHtml);
        vo.setHtmlContent(htmlContent);
        vo.setTocList(tocList);
    }

    /**
     * 最后延迟，改变文章可见状态
     * @param articleId
     * @param lastTime
     */
    public void sendObjExecutionMsg2Mq(Long articleId, long lastTime) {
        SearchArticleVo vo = new SearchArticleVo();
        vo.setId(articleId);
        vo.setPublishTime(new Date(lastTime));
        // mq发送延迟任务
        sendDelayMsg2Mq("article.last.do", vo, getCorrelationData(vo, false));
        log.info("last延迟消息发送成功");
    }

    public void sendArticleVo2Mq(SearchArticleVo vo) {
        CorrelationData minio_es = getCorrelationData(vo,false);
        sendMsg2Mq("article.minio.es", vo,minio_es);
    }

    public CorrelationData getCorrelationData(SearchArticleVo vo,boolean retry) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        correlationData.getFuture().thenAcceptAsync(confirm -> {
            log.info("生产者投递消息confirmCallback 回执");
            ArticleEvent event = new ArticleEvent();
            event.setRetryTime(new Date(System.currentTimeMillis() + 15 * 1000));
            event.setParameter(JSON.toJSONString(vo));
            if (retry) {
                event.setRetryCount((byte) 1);
            }
            if (confirm.isAck()) {
                log.info("ApArticle服务消息发送成功");
                event.setSendStatus((byte) 2);
            } else {
                event.setSendStatus((byte) 1);
                log.info("消息发送失败,原因：{}", confirm.getReason());
            }
            apArticleEventMapper.updateArticleEvent(event);
        });
        return correlationData;
    }

    public void sendMsg2Mq(String routingKey, SearchArticleVo vo, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend("article.exchange", routingKey, vo, correlationData);
    }

    public void sendDelayMsg2Mq(String routingKey, SearchArticleVo vo, CorrelationData correlationData) {
        rabbitTemplate.convertAndSend("delay.exchange"  , routingKey, vo,
            message -> {
                message.getMessageProperties().setDelayLong(vo.getPublishTime().getTime());
                return message;
            },correlationData);
    }

    private void buildFileNameAndPath(ApArticle apArticle, SearchArticleVo vo) {
        // "yyyy/MM/dd/articleId"
        String fileName = minioUtil.builderFilePath("", String.valueOf(apArticle.getId()));
        vo.setFileName(fileName);
        // path http://xx:9000/bucketName/2020/08/05/articleId.html
        String path = prop.getReadPath() + "/" + prop.getBucket() + "/" + fileName + ".html";
        vo.setStaticUrl(path);
    }

    // 已弃用
    @Async
    public void buildHTMLAndSend2(ApArticle apArticle, String content) {
        //已知文章的id
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        vo.setContent(content);
        // "yyyy/MM/dd/articleId"
        String fileName = minioUtil.builderFilePath("", String.valueOf(apArticle.getId()));
        vo.setFileName(fileName);
        // path http://xx:9000/bucketName/2020/08/05/articleId.html
        String path = prop.getReadPath() + "/" + prop.getBucket() + "/" + fileName + ".html";
        vo.setStaticUrl(path);
        CorrelationData minioCd = new CorrelationData(UUID.randomUUID().toString());
        minioCd.getFuture().thenAcceptAsync(confirm -> {
            log.info("MinIO消息confirmCallback 回执");
            if (confirm.isAck()) {
                //update event status to success
                log.info("MinIO消息发送成功");
            } else {
                ArticleEvent event = new ArticleEvent();
                event.setRetryTime(new Date(System.currentTimeMillis() + 15 * 1000));
                event.setParameter(JSON.toJSONString(vo));
                event.setMinioStatus((byte) 1);
                apArticleEventMapper.updateArticleEvent(event);
                log.info("MinIO消息发送失败,原因：{}", confirm.getReason());
            }
        });
        // 创建cd
        CorrelationData ecd = new CorrelationData(UUID.randomUUID().toString());
        // 添加confirmCallback
        ecd.getFuture().thenAcceptAsync(confirm -> {
            log.info("收到confirmCallback 回执");
            if (confirm.isAck()) {
                log.info("es消息发送成功");
            } else {
                ArticleEvent event = new ArticleEvent();
                event.setRetryTime(new Date(System.currentTimeMillis() + 15 * 1000));
                event.setParameter(JSON.toJSONString(vo));
                event.setMinioStatus((byte) 1);
                apArticleEventMapper.updateArticleEvent(event);
                log.info("es消息发送失败,原因：{}", confirm.getReason());
            }
        });
        rabbitTemplate.convertAndSend("article.exchange", "article.minio", JSON.toJSONString(vo), minioCd);

        rabbitTemplate.convertAndSend("article.exchange", "article.es", JSON.toJSONString(vo), ecd);
    }

}
