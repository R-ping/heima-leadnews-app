CREATE TABLE IF NOT EXISTS `ap_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `article_id` bigint NOT NULL COMMENT '文章ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `user_name` varchar(50) DEFAULT '' COMMENT '用户昵称',
    `user_avatar` varchar(255) DEFAULT '' COMMENT '用户头像',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，null表示一级评论',
    `content` text NOT NULL COMMENT '评论内容',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `reply_count` int DEFAULT 0 COMMENT '回复数',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

CREATE TABLE IF NOT EXISTS `ap_comment_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';