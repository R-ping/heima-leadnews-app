package com.heima.content.service.impl;

import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.schedule.service.TaskService;
import com.heima.content.service.ArticleTaskService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticle.Status;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.utils.common.ProtostuffUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class ArticleTaskServiceImpl implements ArticleTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    @Async
    public void addArticleToTask(Long articleId, Date publishTime) {
        log.info("添加文章到延迟发布队列, articleId={}, publishTime={}", articleId, publishTime);

        Date now = new Date();
        long executeTimeInterval = publishTime.getTime() - now.getTime();

        long firstTimeInterval;
        if (executeTimeInterval <= 0) {
            executeTimeInterval = 0;
            firstTimeInterval = 0;
        } else if (executeTimeInterval <= 5 * 60 * 1000) {
            firstTimeInterval = 0;
        } else if (executeTimeInterval <= 15 * 60 * 1000) {
            firstTimeInterval = executeTimeInterval - 2 * 60 * 1000;
        } else {
            long delay = (long) (Math.random() * 5 + 5);
            delay = delay * 60 * 1000;
            firstTimeInterval = executeTimeInterval - delay;
        }

        Task task = new Task();
        task.setFirstExecInterval(Math.max(0, firstTimeInterval));
        task.setObjExecInterval(executeTimeInterval);
        task.setExecuteTime(publishTime);
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        // 序列化 ApArticle（仅包含ID，用于调度任务反序列化）
        ApArticle apArticle = new ApArticle();
        apArticle.setId(articleId);
        task.setParameters(ProtostuffUtil.serialize(apArticle));

        taskService.addTask(task);
        log.info("文章延迟发布任务已添加, articleId={},", articleId);
    }

    @Override
    public void publishArticle(Long articleId) {
        ApArticle article = apArticleMapper.selectById(articleId);
        if (article == null) {
            log.error("发布文章失败，文章不存在, articleId={}", articleId);
            return;
        }
        article.setStatus(Status.PUBLISHED.getCode());
        apArticleMapper.updateById(article);
        log.info("文章已发布, articleId={}", articleId);
    }
}