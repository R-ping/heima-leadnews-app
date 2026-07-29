CREATE TABLE IF NOT EXISTS `ap_user_diamond_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `change_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '变更类型: level_up-等级升级',
    `change_amount` INT NOT NULL DEFAULT 0 COMMENT '变更数量(正数为增加)',
    `balance` INT NOT NULL DEFAULT 0 COMMENT '变更后余额',
    `source_id` VARCHAR(100) DEFAULT '' COMMENT '来源ID(如等级ID)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钻石交易日志表';