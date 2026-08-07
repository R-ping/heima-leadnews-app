-- ============================================
-- leadnews_task 数据库完整建表脚本
-- 包含：签到模块 + 抽奖模块 + 兑换模块 + 用户资产
-- ============================================
create database if not exists leadnews_reward default charset utf8mb4;
use leadnews_reward;
-- 1. 用户资产表（矿石余额）
CREATE TABLE IF NOT EXISTS `user_assets` (
  `user_id` bigint(20) NOT NULL PRIMARY KEY,
  `ore_balance` int(11) NOT NULL DEFAULT '0' COMMENT '当前矿石余额',
  `frozen_ore` int(11) NOT NULL DEFAULT '0' COMMENT '冻结矿石（TCC模式使用）',
  `lucky_value` int(11) NOT NULL DEFAULT '0' COMMENT '当前幸运值',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';

-- ============================================
-- 签到模块表
-- ============================================

-- 签到奖励配置表
CREATE TABLE IF NOT EXISTS `checkin_reward_config` (
  `period_day` tinyint(4) NOT NULL PRIMARY KEY COMMENT '周期第几天（1~30）',
  `base_ore` int(11) NOT NULL COMMENT '基础矿石数',
  `is_special` tinyint(1) DEFAULT '0' COMMENT '是否为特殊奖励日',
  `special_ore` int(11) DEFAULT '0' COMMENT '特殊奖励矿石数（覆盖 base_ore）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到奖励配置表';

-- 签到记录表
CREATE TABLE IF NOT EXISTS `checkin_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `checkin_date` date NOT NULL COMMENT '签到日期（yyyy-MM-dd）',
  `earned_ore` int(11) NOT NULL COMMENT '本次签到获得的矿石数',
  `period_day` tinyint(4) NOT NULL COMMENT '当前连续周期内的第几天（1~30）',
  `is_patch` tinyint(1) DEFAULT '0' COMMENT '是否为补签（0-正常签到 1-补签）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_date` (`user_id`, `checkin_date`),
  KEY `idx_user_date_desc` (`user_id`, `checkin_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 新签到记录表（替代 checkin_records）
CREATE TABLE IF NOT EXISTS `sign_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `sign_date` date NOT NULL COMMENT '签到日期（yyyy-MM-dd）',
  `award_ore` int(11) NOT NULL COMMENT '本次签到获得的矿石数（补签重算后更新）',
  `is_extra` tinyint(1) DEFAULT '0' COMMENT '是否为补签（0-正常签到 1-补签）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_date` (`user_id`, `sign_date`),
  KEY `idx_user_date_desc` (`user_id`, `sign_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表（新）';

-- 用户签到状态表
CREATE TABLE IF NOT EXISTS `user_checkin_state` (
  `user_id` bigint(20) NOT NULL PRIMARY KEY,
  `continuous_days` int(11) NOT NULL DEFAULT '0' COMMENT '当前连续签到天数',
  `period_day` tinyint(4) NOT NULL DEFAULT '0' COMMENT '当前周期内的第几天（1~30，0表示未开始）',
  `last_checkin_date` date DEFAULT NULL COMMENT '最后一次签到日期（含补签）',
  `total_checkin_days` int(11) NOT NULL DEFAULT '0' COMMENT '历史累计签到总天数',
  `patch_card_count` int(11) NOT NULL DEFAULT '0' COMMENT '补签卡库存',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到状态表';

-- 补签卡库存变更日志
CREATE TABLE IF NOT EXISTS `patch_card_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `change_amount` int(11) NOT NULL COMMENT '正数-获得，负数-消耗',
  `source` varchar(32) NOT NULL COMMENT '来源：兑换、系统赠送、活动等',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补签卡库存变更日志';

-- ============================================
-- 抽奖模块表
-- ============================================

-- 奖品池配置表
CREATE TABLE IF NOT EXISTS `lottery_prize_pool` (
  `id` varchar(32) NOT NULL PRIMARY KEY,
  `name` varchar(64) NOT NULL,
  `type` tinyint(4) NOT NULL COMMENT '1-矿石 2-虚拟道具 3-实物',
  `icon_url` varchar(255) DEFAULT NULL,
  `probability` decimal(5,4) NOT NULL COMMENT '基础概率（如 0.0500 = 5%）',
  `min_ore` int(11) DEFAULT '0' COMMENT '矿石范围最小值（type=1时使用）',
  `max_ore` int(11) DEFAULT '0' COMMENT '矿石范围最大值',
  `virtual_item_code` varchar(32) DEFAULT NULL COMMENT '虚拟道具代码（type=2时使用）',
  `unlock_required_draws` tinyint(4) DEFAULT '0' COMMENT '需当日抽几次才解锁（0=无需解锁）',
  `is_physical` tinyint(1) DEFAULT '0',
  `sort_order` int(11) DEFAULT '0',
  `status` tinyint(4) DEFAULT '1' COMMENT '1-启用 0-停用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品池配置表';

-- 用户抽奖记录表
CREATE TABLE IF NOT EXISTS `lottery_draw_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `draw_batch_id` varchar(64) NOT NULL COMMENT '批次ID（单次/十连共用）',
  `user_id` bigint(20) NOT NULL,
  `prize_id` varchar(32) NOT NULL,
  `prize_name` varchar(64) NOT NULL,
  `prize_type` tinyint(4) NOT NULL,
  `ore_amount` int(11) DEFAULT '0',
  `virtual_item_code` varchar(32) DEFAULT NULL,
  `physical_order_id` bigint(20) DEFAULT NULL COMMENT '关联实物订单表',
  `lucky_value_before` int(11) NOT NULL,
  `lucky_value_after` int(11) NOT NULL,
  `today_draw_count_at_time` int(11) NOT NULL COMMENT '抽奖时的当日累计次数',
  `cost_ore` int(11) DEFAULT '0',
  `is_free` tinyint(1) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at` DESC),
  KEY `idx_batch` (`draw_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户抽奖记录表';

-- 实物奖品订单表
CREATE TABLE IF NOT EXISTS `lottery_physical_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `prize_id` varchar(32) NOT NULL,
  `prize_name` varchar(64) NOT NULL,
  `receiver_name` varchar(64) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1' COMMENT '1-待填地址 2-待发货 3-已发货 4-已签收 5-已过期',
  `express_no` varchar(64) DEFAULT NULL,
  `expire_at` datetime DEFAULT NULL COMMENT '填写地址截止时间（30天后）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实物奖品订单表';

-- 用户每日抽奖状态表
CREATE TABLE IF NOT EXISTS `lottery_daily_state` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `stat_date` date NOT NULL,
  `draw_count` int(11) DEFAULT '0' COMMENT '当日抽奖总次数（不含免费）',
  `free_used` tinyint(1) DEFAULT '0' COMMENT '今日免费次数是否已用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_date` (`user_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日抽奖状态表';

-- ============================================
-- 兑换模块表
-- ============================================

-- 福利商品表
CREATE TABLE IF NOT EXISTS `welfare_goods` (
  `id` varchar(32) NOT NULL PRIMARY KEY,
  `name` varchar(128) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `type` tinyint(4) NOT NULL COMMENT '1-实物 2-虚拟道具',
  `category` tinyint(4) NOT NULL COMMENT '1-惊喜好物 2-社区道具',
  `ore_price` int(11) NOT NULL COMMENT '兑换所需矿石数',
  `original_price` int(11) DEFAULT '0' COMMENT '划线原价',
  `discount_tag` varchar(32) DEFAULT NULL COMMENT '折扣标签（如"五折"）',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '当前库存（-1表示无限）',
  `total_stock` int(11) NOT NULL COMMENT '总库存（用于统计）',
  `exchanged_count` int(11) DEFAULT '0' COMMENT '已兑换人数（冗余计数）',
  `is_virtual` tinyint(1) NOT NULL COMMENT '1-虚拟商品 0-实物',
  `time_limit_start` time DEFAULT NULL COMMENT '限时兑换开始时间',
  `time_limit_end` time DEFAULT NULL COMMENT '限时兑换结束时间',
  `time_limit_desc` varchar(64) DEFAULT NULL COMMENT '限时描述（如"周六~周日开放兑换"）',
  `virtual_code_template` varchar(255) DEFAULT NULL COMMENT '虚拟商品兑换码生成模板',
  `status` tinyint(4) DEFAULT '1' COMMENT '1-上架 0-下架',
  `sort_order` int(11) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='福利商品表';

-- 兑换订单表
CREATE TABLE IF NOT EXISTS `welfare_exchange_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` varchar(64) NOT NULL COMMENT '业务订单号',
  `user_id` bigint(20) NOT NULL,
  `goods_id` varchar(32) NOT NULL,
  `goods_name` varchar(128) NOT NULL,
  `is_virtual` tinyint(1) NOT NULL,
  `ore_cost` int(11) NOT NULL,
  `receiver_name` varchar(64) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `virtual_code` varchar(255) DEFAULT NULL COMMENT '虚拟商品兑换码',
  `status` tinyint(4) DEFAULT '1' COMMENT '1-待处理 2-已完成 3-已过期',
  `express_no` varchar(64) DEFAULT NULL COMMENT '物流单号（实物）',
  `address_expire_at` datetime DEFAULT NULL COMMENT '填写地址截止时间（实物，30天后）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_exchange_id` (`exchange_id`),
  KEY `idx_user_created` (`user_id`, `created_at` DESC),
  KEY `idx_status_expire` (`status`, `address_expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换订单表';

-- 库存扣减日志
CREATE TABLE IF NOT EXISTS `welfare_stock_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` varchar(32) NOT NULL,
  `change_amount` int(11) NOT NULL COMMENT '负数-扣减',
  `exchange_id` varchar(64) NOT NULL COMMENT '关联订单号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_goods_created` (`goods_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存扣减日志';

-- 中奖播报消息表
CREATE TABLE IF NOT EXISTS `lottery_broadcast_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `user_nickname` varchar(64) DEFAULT NULL,
  `prize_name` varchar(64) NOT NULL,
  `prize_type` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_created` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中奖播报消息表';

-- ============================================
-- 初始化签到奖励配置数据
-- ============================================
INSERT IGNORE INTO `checkin_reward_config` (`period_day`, `base_ore`, `is_special`, `special_ore`) VALUES
(1, 10, 0, 0),
(2, 20, 0, 0),
(3, 30, 1, 512),
(4, 40, 0, 0),
(5, 50, 0, 0),
(6, 60, 0, 0),
(7, 70, 1, 1024),
(8, 80, 0, 0),
(9, 90, 0, 0),
(10, 100, 0, 0),
(11, 110, 0, 0),
(12, 120, 0, 0),
(13, 130, 0, 0),
(14, 140, 1, 2048),
(15, 150, 0, 0),
(16, 160, 0, 0),
(17, 170, 0, 0),
(18, 180, 0, 0),
(19, 190, 0, 0),
(20, 200, 0, 0),
(21, 210, 1, 4096),
(22, 220, 0, 0),
(23, 230, 0, 0),
(24, 240, 0, 0),
(25, 250, 0, 0),
(26, 260, 0, 0),
(27, 270, 0, 0),
(28, 280, 0, 0),
(29, 290, 0, 0),
(30, 300, 1, 5120);