USE leadnews_article;
CREATE TABLE IF NOT EXISTS `ap_check_in` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `check_in_date` date NOT NULL COMMENT '签到日期',
    `reward_points` int DEFAULT 0 COMMENT '奖励积分',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';