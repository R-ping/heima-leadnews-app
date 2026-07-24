USE leadnews_article;

-- ============================================================
-- 1. 签到记录表 (原有)
-- ============================================================
CREATE TABLE IF NOT EXISTS `ap_check_in` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `check_in_date` date NOT NULL COMMENT '签到日期',
    `reward_points` int DEFAULT 0 COMMENT '奖励积分',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- ============================================================
-- 2. 签到奖励配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sign_in_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `day_of_month` TINYINT NOT NULL COMMENT '当月第几天',
    `base_reward` INT DEFAULT 0 COMMENT '基础矿石奖励',
    `bonus_multiplier` DECIMAL(3,2) DEFAULT 1.00 COMMENT '连续签到加成系数',
    `extra_label` VARCHAR(50) COMMENT '特殊标签文案',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否生效',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到奖励配置表';

-- ============================================================
-- 3. 用户签到汇总表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_sign_in_summary` (
    `user_id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID',
    `current_consecutive_days` INT DEFAULT 0 COMMENT '当前连续签到天数',
    `max_consecutive_days` INT DEFAULT 0 COMMENT '历史最高连续天数',
    `total_signed_days` INT DEFAULT 0 COMMENT '累计签到总天数',
    `retroactive_card_count` INT DEFAULT 0 COMMENT '补签卡剩余数量',
    `last_sign_date` DATE COMMENT '最后签到日期',
    `total_ore` BIGINT DEFAULT 0 COMMENT '当前矿石总数',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到汇总表';

-- ============================================================
-- 4. 用户新手任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_onboarding_tasks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_type` VARCHAR(30) NOT NULL COMMENT '任务类型',
    `status` TINYINT DEFAULT 0 COMMENT '0-未开始, 1-进行中, 2-已完成待领奖, 3-已领奖',
    `condition_value` INT DEFAULT 0 COMMENT '条件阈值',
    `reward_ore` INT DEFAULT 0 COMMENT '奖励矿石数',
    `complete_time` DATETIME COMMENT '完成时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_task` (`user_id`, `task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户新手任务表';

-- ============================================================
-- 5. 修改 ap_check_in 表，添加新字段（幂等处理）
-- ============================================================
-- 添加 is_retroactive 字段
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_not_exists`(
    IN tbl_name VARCHAR(128),
    IN col_name VARCHAR(128),
    IN col_definition VARCHAR(512)
)
BEGIN
    DECLARE col_count INT DEFAULT 0;
    SELECT COUNT(*) INTO col_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tbl_name
      AND COLUMN_NAME = col_name;
    IF col_count = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN `', col_name, '` ', col_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_column_if_not_exists('ap_check_in', 'is_retroactive', 'TINYINT DEFAULT 0 COMMENT \'是否补签 0-正常 1-补签\'');
CALL add_column_if_not_exists('ap_check_in', 'consecutive_days', 'INT DEFAULT 0 COMMENT \'签到时的连续天数快照\'');

DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;

-- ============================================================
-- 6. 种子数据：sign_in_config
-- ============================================================
-- 第1-24天：基础奖励5，加成系数1.00
INSERT INTO `sign_in_config` (`day_of_month`, `base_reward`, `bonus_multiplier`, `extra_label`, `is_active`)
SELECT days.day_num, 5, 1.00, NULL, 1
FROM (
    SELECT 1 AS day_num UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
) AS days
WHERE NOT EXISTS (
    SELECT 1 FROM `sign_in_config` WHERE `day_of_month` = days.day_num
);

-- 第25天：每月彩蛋
INSERT INTO `sign_in_config` (`day_of_month`, `base_reward`, `bonus_multiplier`, `extra_label`, `is_active`)
SELECT 25, 512, 1.00, '每月彩蛋', 1
WHERE NOT EXISTS (SELECT 1 FROM `sign_in_config` WHERE `day_of_month` = 25);

-- 第26-30天：基础奖励5
INSERT INTO `sign_in_config` (`day_of_month`, `base_reward`, `bonus_multiplier`, `extra_label`, `is_active`)
SELECT days.day_num, 5, 1.00, NULL, 1
FROM (
    SELECT 26 AS day_num UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
) AS days
WHERE NOT EXISTS (
    SELECT 1 FROM `sign_in_config` WHERE `day_of_month` = days.day_num
);

-- 第31天：月末彩蛋
INSERT INTO `sign_in_config` (`day_of_month`, `base_reward`, `bonus_multiplier`, `extra_label`, `is_active`)
SELECT 31, 550, 1.00, '月末彩蛋', 1
WHERE NOT EXISTS (SELECT 1 FROM `sign_in_config` WHERE `day_of_month` = 31);

-- ============================================================
-- 7. 种子数据：user_onboarding_tasks（user_id=1）
-- ============================================================
INSERT INTO `user_onboarding_tasks` (`user_id`, `task_type`, `status`, `condition_value`, `reward_ore`, `complete_time`)
SELECT 1, 'PUBLISH_ARTICLE', 3, 400, 10000, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `user_onboarding_tasks` WHERE `user_id` = 1 AND `task_type` = 'PUBLISH_ARTICLE'
);

INSERT INTO `user_onboarding_tasks` (`user_id`, `task_type`, `status`, `condition_value`, `reward_ore`, `complete_time`)
SELECT 1, 'PUBLISH_BOOLEAN', 3, 1, 5000, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `user_onboarding_tasks` WHERE `user_id` = 1 AND `task_type` = 'PUBLISH_BOOLEAN'
);