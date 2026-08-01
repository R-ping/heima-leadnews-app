-- ==============================================
-- 沸点功能数据库表结构
-- ==============================================

-- 1. 圈子表
CREATE TABLE IF NOT EXISTS `ap_circle` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `parent_id` bigint DEFAULT NULL COMMENT '父圈子ID，null表示一级圈子',
    `name` varchar(50) NOT NULL COMMENT '圈子名称',
    `description` varchar(255) DEFAULT '' COMMENT '圈子描述',
    `icon` varchar(255) DEFAULT '' COMMENT '圈子图标',
    `member_count` int DEFAULT 0 COMMENT '成员数',
    `pins_count` int DEFAULT 0 COMMENT '沸点数',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子表';

-- 2. 用户圈子关系表
CREATE TABLE IF NOT EXISTS `ap_user_circle` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `circle_id` bigint NOT NULL COMMENT '圈子ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_circle` (`user_id`, `circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户圈子关系表';

-- 3. 话题表
CREATE TABLE IF NOT EXISTS `ap_topic` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '话题名称',
    `count` int DEFAULT 0 COMMENT '关联沸点数',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- 4. 沸点帖子表
CREATE TABLE IF NOT EXISTS `ap_pins` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `user_name` varchar(50) DEFAULT '' COMMENT '用户昵称',
    `user_avatar` varchar(255) DEFAULT '' COMMENT '用户头像',
    `content` text NOT NULL COMMENT '帖子内容',
    `circle_id` bigint DEFAULT NULL COMMENT '圈子ID（可选）',
    `topic_id` bigint DEFAULT NULL COMMENT '话题ID（可选）',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `comment_count` int DEFAULT 0 COMMENT '评论数',
    `share_count` int DEFAULT 0 COMMENT '分享数',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_circle_id` (`circle_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点帖子表';

-- 5. 沸点评论表
CREATE TABLE IF NOT EXISTS `ap_pins_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `pins_id` bigint NOT NULL COMMENT '沸点ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `user_name` varchar(50) DEFAULT '' COMMENT '用户昵称',
    `user_avatar` varchar(255) DEFAULT '' COMMENT '用户头像',
    `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，null表示一级评论',
    `content` text NOT NULL COMMENT '评论内容',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `reply_count` int DEFAULT 0 COMMENT '回复数',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_pins_id` (`pins_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点评论表';

-- 6. 沸点评论点赞表
CREATE TABLE IF NOT EXISTS `ap_pins_comment_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点评论点赞记录表';

-- 7. 沸点帖子点赞表
CREATE TABLE IF NOT EXISTS `ap_pins_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `pins_id` bigint NOT NULL COMMENT '沸点ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pins_user` (`pins_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点帖子点赞记录表';

-- ==============================================
-- 初始数据 - 一级圈子（12个）
-- ==============================================

INSERT INTO `ap_circle` (`id`, `parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(1, NULL, '技术', '技术爱好者交流社区', 150000, 500000, 1),
(2, NULL, '职场', '职场经验分享与交流', 120000, 300000, 2),
(3, NULL, '吃喝玩乐', '美食、旅行、生活分享', 200000, 800000, 3),
(4, NULL, '资讯', '最新资讯与热点讨论', 180000, 450000, 4),
(5, NULL, '理财', '投资理财经验分享', 80000, 150000, 5),
(6, NULL, '互动交流', '日常互动与交友', 90000, 200000, 6),
(7, NULL, '书影音', '书籍、电影、音乐推荐', 100000, 250000, 7),
(8, NULL, '生活', '生活感悟与日常分享', 130000, 350000, 8),
(9, NULL, '搞笑', '搞笑段子与趣闻', 160000, 600000, 9),
(10, NULL, '情感', '情感倾诉与交流', 110000, 280000, 10),
(11, NULL, '游戏', '游戏攻略与讨论', 140000, 400000, 11),
(12, NULL, '数码', '数码产品评测与推荐', 95000, 180000, 12);

-- ==============================================
-- 初始数据 - 二级圈子
-- ==============================================

-- 技术
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(1, '大模型生态圈', '大模型技术交流与应用', 35000, 80000, 1),
(1, '微服务生态圈', '微服务架构与实践', 25000, 60000, 2),
(1, '前端开发圈', '前端技术与框架交流', 40000, 120000, 3),
(1, '服务端与架构', '服务端开发与系统架构', 30000, 70000, 4),
(1, '技术交流圈', '综合技术交流', 50000, 150000, 5);

-- 职场
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(2, '上班摸鱼', '职场摸鱼日常', 40000, 100000, 1),
(2, '内推招聘广场', '内推与招聘信息', 35000, 80000, 2),
(2, '程序员成长', '程序员职业发展', 45000, 120000, 3);

-- 吃喝玩乐
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(3, '美食探店', '美食分享与探店', 50000, 150000, 1),
(3, '旅行日记', '旅行经历分享', 45000, 120000, 2),
(3, '什么值得买', '好物推荐与评测', 60000, 200000, 3),
(3, '吃货日常', '日常美食分享', 45000, 130000, 4);

-- 资讯
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(4, '今日新鲜事', '每日新鲜资讯', 50000, 120000, 1),
(4, '科技前沿', '前沿科技资讯', 45000, 100000, 2),
(4, '互联网热点', '互联网行业热点', 45000, 110000, 3);

-- 理财
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(5, '股票基金', '股票与基金投资', 30000, 60000, 1),
(5, '投资理财', '综合投资理财', 35000, 50000, 2),
(5, '省钱攻略', '省钱技巧分享', 35000, 40000, 3);

-- 互动交流
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(6, '新人报道', '新人自我介绍', 20000, 30000, 1),
(6, '每日打卡', '每日打卡记录', 25000, 50000, 2),
(6, '问答交流', '问题解答与交流', 45000, 120000, 3);

-- 书影音
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(7, '读书分享', '读书心得分享', 30000, 70000, 1),
(7, '电影推荐', '电影推荐与影评', 35000, 80000, 2),
(7, '音乐分享', '音乐推荐与感悟', 35000, 100000, 3);

-- 生活
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(8, '日常生活', '日常生活分享', 40000, 100000, 1),
(8, '健身打卡', '健身与运动', 30000, 60000, 2),
(8, '宠物日常', '宠物萌宠分享', 30000, 80000, 3),
(8, '家居装修', '家居装修与布置', 30000, 60000, 4);

-- 搞笑
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(9, '搞笑段子', '搞笑段子分享', 50000, 150000, 1),
(9, '神回复', '神回复合集', 40000, 120000, 2),
(9, '趣图分享', '有趣图片分享', 40000, 130000, 3),
(9, '职场搞笑', '职场搞笑日常', 30000, 80000, 4);

-- 情感
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(10, '恋爱话题', '恋爱经验分享', 35000, 80000, 1),
(10, '单身日记', '单身生活分享', 30000, 60000, 2),
(10, '家庭关系', '家庭关系探讨', 35000, 70000, 3),
(10, '心灵鸡汤', '心灵感悟与鸡汤', 30000, 70000, 4);

-- 游戏
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(11, '手游交流', '手机游戏交流', 40000, 100000, 1),
(11, '端游攻略', '端游攻略分享', 35000, 80000, 2),
(11, '游戏杂谈', '游戏话题讨论', 35000, 80000, 3),
(11, '电竞赛事', '电竞比赛讨论', 30000, 60000, 4);

-- 数码
INSERT INTO `ap_circle` (`parent_id`, `name`, `description`, `member_count`, `pins_count`, `sort_order`) VALUES
(12, '手机评测', '手机评测与推荐', 30000, 50000, 1),
(12, '电脑配置', '电脑配置讨论', 30000, 40000, 2),
(12, '智能家居', '智能家居分享', 25000, 50000, 3),
(12, '配件推荐', '数码配件推荐', 20000, 40000, 4);

-- ==============================================
-- 初始数据 - 话题表
-- ==============================================

INSERT INTO `ap_topic` (`name`, `count`, `sort_order`) VALUES
('#新人报道#', 5000, 1),
('#程序员脱单到底有多难#', 15000, 2),
('#每日快讯#', 20000, 3),
('#每日精选文章#', 18000, 4),
('#日新计划#', 12000, 5),
('#每天一个知识点#', 8000, 6),
('#VueLaunch沸点秀#', 6000, 7),
('#代码人生#', 10000, 8),
('#优秀开源项目#', 7000, 9),
('#技术交流#', 13000, 10),
('#上班摸鱼#', 25000, 11),
('#美食分享#', 30000, 12),
('#旅行日记#', 18000, 13),
('#投资理财#', 10000, 14),
('#读书推荐#', 12000, 15);

-- ==============================================
-- 初始数据 - 测试用户圈子关系（用户ID为1的用户关注的圈子）
-- ==============================================

INSERT INTO `ap_user_circle` (`user_id`, `circle_id`) VALUES
(1, 13),
(1, 14),
(1, 15),
(1, 16),
(1, 17),
(1, 18),
(1, 19),
(1, 20);