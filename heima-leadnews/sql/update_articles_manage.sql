USE leadnews_article;

-- ==============================================
-- 创作者中心 - 文章管理、专栏、沸点相关表结构更新
-- ==============================================

-- 1. 文章表添加 is_deleted 字段（软删除）
ALTER TABLE `ap_article` 
    ADD COLUMN `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除 0未删除 1已删除';

-- 2. 文章草稿表添加 is_deleted 字段（软删除）
ALTER TABLE `ap_article_draft` 
    ADD COLUMN `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除 0未删除 1已删除';

-- 3. 专栏表（新建）
CREATE TABLE IF NOT EXISTS `ap_column` (
    `id` bigint NOT NULL COMMENT '主键',
    `author_id` bigint DEFAULT NULL COMMENT '作者ID',
    `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `author_image` varchar(255) DEFAULT NULL COMMENT '作者头像',
    `title` varchar(100) DEFAULT NULL COMMENT '专栏名称',
    `description` varchar(500) DEFAULT NULL COMMENT '专栏简介',
    `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片',
    `article_count` int DEFAULT 0 COMMENT '文章数',
    `subscribe_count` int DEFAULT 0 COMMENT '订阅人数',
    `status` tinyint DEFAULT 0 COMMENT '审核状态 0草稿 1提交审核 2审核失败 9已发布',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏表';

-- 4. 沸点表（新建）
CREATE TABLE IF NOT EXISTS `ap_pins` (
    `id` bigint NOT NULL COMMENT '主键',
    `author_id` bigint DEFAULT NULL COMMENT '作者ID',
    `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `author_image` varchar(255) DEFAULT NULL COMMENT '作者头像',
    `content` text COMMENT '沸点内容',
    `image_urls` varchar(1000) DEFAULT NULL COMMENT '图片URL，逗号分隔',
    `topic_tags` varchar(500) DEFAULT NULL COMMENT '话题标签，逗号分隔',
    `likes` int DEFAULT 0 COMMENT '点赞数',
    `comment` int DEFAULT 0 COMMENT '评论数',
    `share` int DEFAULT 0 COMMENT '分享数',
    `status` tinyint DEFAULT 0 COMMENT '审核状态 0草稿 1提交审核 2审核失败 9已发布',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点表';
