package com.heima.search.config;

import com.heima.apis.article.IArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 错误处理配置（预留）
 * RabbitMQ相关配置已移除，搜索服务不再使用消息队列
 */
@Configuration
public class ErrorConfiguration {

    @Autowired
    private IArticleClient articleClient;

}