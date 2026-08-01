package com.heima.content.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.search.ISearchClient;
import com.heima.content.mapper.ApArticleContentMapper;
import com.heima.content.schedule.service.TaskService;
import com.heima.content.service.ArticleFreemarkerService;
import com.heima.content.utils.MarkdownUtils;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.file.config.MinIOConfig;
import com.heima.file.utils.MinioUtil;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.model.search.vos.TocItem;
import com.heima.utils.common.ProtostuffUtil;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ISearchClient searchClient;
    @Autowired
    private TaskService taskService;

    /**
     * 生成静态文件上传到minIO中
     */
    @Async
    @Override
    public void buildHTMLAndSend(ApArticle apArticle, String content, long lastExecuteInterval) {
        // 添加延迟发布任务（使用Redisson延迟队列替代RabbitMQ）
        addDelayTask(apArticle.getId(), lastExecuteInterval);
        //已知文章的id
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        String markdown = resolveContent(apArticle.getId(), content);
        vo.setContent(markdown);
        buildHtmlContent(vo, markdown);
        buildFileNameAndPath(apArticle, vo);
        // 同步文章到ES索引（使用Feign调用替代RabbitMQ）
        try {
            searchClient.syncArticle(vo);
            log.info("文章同步到ES成功, articleId={}", apArticle.getId());
        } catch (Exception e) {
            log.error("文章同步到ES失败, articleId={}", apArticle.getId(), e);
        }
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
     * 添加延迟发布任务（使用Redisson延迟队列替代RabbitMQ）
     */
    private void addDelayTask(Long articleId, long lastExecuteInterval) {
        ApArticle apArticle = new ApArticle();
        apArticle.setId(articleId);
        Date now = new Date();
        long firstExecInterval;
        if (lastExecuteInterval <= 5 * 60 * 1000) {
            firstExecInterval = 0;
        } else {
            long delay = (long) (Math.random() * 5 + 5);
            delay = delay * 60 * 1000;
            firstExecInterval = lastExecuteInterval - delay;
        }
        Date executeTime = new Date(now.getTime() + lastExecuteInterval);

        Task task = new Task();
        task.setFirstExecInterval(Math.max(0, firstExecInterval));
        task.setObjExecInterval(lastExecuteInterval);
        task.setExecuteTime(executeTime);
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        task.setParameters(ProtostuffUtil.serialize(apArticle));
        taskService.addTask(task);
        log.info("延迟任务添加成功, articleId={}, delay={}ms", articleId, lastExecuteInterval);
    }

    private void buildFileNameAndPath(ApArticle apArticle, SearchArticleVo vo) {
        // "yyyy/MM/dd/articleId"
        String fileName = minioUtil.builderFilePath("", String.valueOf(apArticle.getId()));
        vo.setFileName(fileName);
        // path http://xx:9000/bucketName/2020/08/05/articleId.html
        String path = prop.getReadPath() + "/" + prop.getBucket() + "/" + fileName + ".html";
        vo.setStaticUrl(path);
    }

}
