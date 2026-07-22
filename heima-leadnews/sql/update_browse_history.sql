USE leadnews_article;
CREATE TABLE IF NOT EXISTS `ap_browse_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `article_id` bigint DEFAULT NULL COMMENT '文章ID',
    `article_title` varchar(200) DEFAULT NULL COMMENT '文章标题',
    `author_id` bigint DEFAULT NULL COMMENT '作者ID',
    `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `browse_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_browse_time` (`browse_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录表';