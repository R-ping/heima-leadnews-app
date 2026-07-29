CREATE TABLE IF NOT EXISTS `ap_article_audit_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '文章标题',
    `content` LONGTEXT COMMENT '文章内容',
    `reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '审核失败原因',
    `audit_type` VARCHAR(50) NOT NULL DEFAULT 'text' COMMENT '审核类型: text-文本审核, image-图片审核',
    `status` TINYINT NOT NULL DEFAULT 2 COMMENT '审核状态: 2-失败',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章审核记录表';