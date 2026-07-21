CREATE TABLE IF NOT EXISTS `ap_user_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `daily_score` INT DEFAULT 0 COMMENT '逐日分',
    `daily_level` TINYINT DEFAULT 1 COMMENT '逐日等级',
    `power_value` INT DEFAULT 0 COMMENT '逐力值',
    `power_level` TINYINT DEFAULT 1 COMMENT '逐力等级',
    `daily_score_today` INT DEFAULT 0 COMMENT '今日逐日分获取量',
    `power_value_today` INT DEFAULT 0 COMMENT '今日逐力值获取量',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级表';

CREATE TABLE IF NOT EXISTS `ap_level_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `level_type` TINYINT NOT NULL COMMENT '等级类型：1-逐日等级 2-逐力等级',
    `level_value` TINYINT NOT NULL COMMENT '等级值',
    `min_score` INT NOT NULL COMMENT '最低分数',
    `max_score` INT NOT NULL COMMENT '最高分数',
    `title` VARCHAR(50) COMMENT '等级头衔',
    `icon_url` VARCHAR(255) COMMENT '等级图标',
    `description` VARCHAR(200) COMMENT '等级描述',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_type_value` (`level_type`, `level_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级配置表';

CREATE TABLE IF NOT EXISTS `ap_user_action_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `action_type` VARCHAR(50) NOT NULL COMMENT '行为类型：daily_login, article_read, comment, like, share, follow',
    `score_change` INT NOT NULL COMMENT '逐日分变化量',
    `action_detail` VARCHAR(500) COMMENT '行为详情',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

CREATE TABLE IF NOT EXISTS `ap_user_power_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `power_change` INT NOT NULL COMMENT '逐力值变化量',
    `change_type` VARCHAR(50) NOT NULL COMMENT '变化类型：publish_article, get_like, get_comment, get_favorite, get_read',
    `source_id` BIGINT COMMENT '来源ID（如文章ID）',
    `calculated_at` DATE COMMENT '计算日期',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_change_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐力值变化日志表';

CREATE TABLE IF NOT EXISTS `ap_permission_definition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `permission_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `description` VARCHAR(500) COMMENT '权限描述',
    `related_level_type` TINYINT NOT NULL COMMENT '关联等级类型：1-逐日等级 2-逐力等级',
    `required_level` TINYINT NOT NULL COMMENT '所需等级',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用：1-启用 0-禁用',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限定义表';

CREATE TABLE IF NOT EXISTS `ap_user_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `permission_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `granted_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授予时间',
    `expired_at` DATETIME COMMENT '过期时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限表';

INSERT INTO `ap_level_config` (`level_type`, `level_value`, `min_score`, `max_score`, `title`, `description`) VALUES
(1, 1, 0, 99, '新手掘友', '刚加入社区的新朋友'),
(1, 2, 100, 499, '初级掘友', '开始活跃在社区'),
(1, 3, 500, 1499, '中级掘友', '社区的中坚力量'),
(1, 4, 1500, 2999, '高级掘友', '社区的活跃分子'),
(1, 5, 3000, 4999, '资深掘友', '社区的资深成员'),
(1, 6, 5000, 7999, '专家掘友', '社区的专家级人物'),
(1, 7, 8000, 11999, '大师掘友', '社区的大师'),
(1, 8, 12000, 19999, '宗师掘友', '社区的宗师'),
(1, 9, 20000, 29999, '传奇掘友', '社区的传奇'),
(1, 10, 30000, 999999, '神话掘友', '社区的神话');

INSERT INTO `ap_level_config` (`level_type`, `level_value`, `min_score`, `max_score`, `title`, `description`) VALUES
(2, 1, 0, 99, '新手创作者', '刚起步的创作者'),
(2, 2, 100, 499, '初级创作者', '开始发布内容'),
(2, 3, 500, 1499, '中级创作者', '有一定影响力'),
(2, 4, 1500, 2999, '高级创作者', '创作经验丰富'),
(2, 5, 3000, 4999, '资深创作者', '资深内容创作者'),
(2, 6, 5000, 7999, '专家创作者', '创作领域专家'),
(2, 7, 8000, 11999, '大师创作者', '创作大师'),
(2, 8, 12000, 19999, '宗师创作者', '创作宗师'),
(2, 9, 20000, 29999, '传奇创作者', '传奇创作者'),
(2, 10, 30000, 999999, '神话创作者', '神话级创作者');

INSERT INTO `ap_permission_definition` (`permission_code`, `permission_name`, `description`, `related_level_type`, `required_level`) VALUES
('can_send_private_message', '私信权限', '可以主动发起私信', 1, 1),
('can_set_comment_permission', '评论区权限设置', '可以设置评论区权限', 1, 3),
('can_create_poll', '发起投票', '可以在沸点发起投票', 1, 2),
('can_become_contributor', '成为共建者', '获得共建者身份标识', 1, 4),
('can_be_recommended', '文章自动推荐', '文章可被自动推荐至首页', 1, 5);

INSERT INTO `ap_permission_definition` (`permission_code`, `permission_name`, `description`, `related_level_type`, `required_level`) VALUES
('can_add_video', '添加视频', '可以在文章中添加视频', 2, 2),
('can_add_2_tags', '2个标签', '文章可添加2个标签', 2, 2),
('can_schedule_publish', '定时发布', '文章可以定时发布', 2, 3),
('can_add_3_tags', '3个标签', '文章可添加3个标签', 2, 3),
('can_add_4_tags', '4个标签', '文章可添加4个标签', 2, 5),
('can_create_course', '创作小册', '开放创作小册功能', 2, 7);
