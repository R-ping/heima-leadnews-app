-- ==============================================
-- 逐日 (zhuri) 平台 - 全部数据库表结构初始化脚本
-- 生成日期：2026-07-21
-- 说明：包含所有 ap_ / wm_ / ad_ / taskinfo 系列表
-- 所有表使用 CREATE TABLE IF NOT EXISTS
-- ==============================================

-- ==============================================
-- 一、用户相关表 (user)
-- ==============================================

-- 1. 用户信息表
CREATE TABLE IF NOT EXISTS `ap_user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nickname` varchar(50) DEFAULT '' COMMENT '昵称',
    `password` varchar(128) DEFAULT NULL COMMENT '密码（BCrypt加密）',
    `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
    `image` varchar(255) DEFAULT NULL COMMENT '头像',
    `sex` tinyint DEFAULT NULL COMMENT '0 男 1 女 2 未知',
    `is_certification` tinyint DEFAULT NULL COMMENT '是否认证 0未 1是',
    `is_identity_authentication` tinyint DEFAULT NULL COMMENT '是否身份认证',
    `status` tinyint DEFAULT NULL COMMENT '1正常 0锁定',
    `flag` smallint DEFAULT NULL COMMENT '0普通用户 1自媒体人 2大V',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 2. 用户社交账号绑定表
CREATE TABLE IF NOT EXISTS `ap_user_social_binding` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID（关联ap_user.id）',
    `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
    `platform` varchar(20) DEFAULT NULL COMMENT '平台: github / weibo / wechat',
    `git_uid` varchar(100) DEFAULT NULL COMMENT 'GitHub平台唯一标识',
    `weibo_uid` varchar(100) DEFAULT NULL COMMENT '微博平台唯一标识',
    `open_id` varchar(100) DEFAULT NULL COMMENT '微信开放平台ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_platform_git_uid` (`platform`, `git_uid`),
    UNIQUE KEY `uk_platform_weibo_uid` (`platform`, `weibo_uid`),
    UNIQUE KEY `uk_platform_open_id` (`platform`, `open_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户社交账号绑定表';

-- 3. 用户关注表
CREATE TABLE IF NOT EXISTS `ap_user_follow` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `follow_user_id` int NOT NULL COMMENT '被关注用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- ==============================================
-- 二、文章相关表 (article)
-- ==============================================

-- 4. 文章信息表
CREATE TABLE IF NOT EXISTS `ap_article` (
    `id` bigint NOT NULL COMMENT '主键',
    `title` varchar(200) DEFAULT NULL COMMENT '标题',
    `author_id` bigint DEFAULT NULL COMMENT '作者id',
    `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `channel_id` int DEFAULT NULL COMMENT '频道id',
    `channel_name` varchar(50) DEFAULT NULL COMMENT '频道名称',
    `layout` tinyint DEFAULT NULL COMMENT '文章布局 0无图 1单图 2多图',
    `flag` tinyint DEFAULT NULL COMMENT '文章标记 0普通 1热点 2置顶 3精品 4大V',
    `images` varchar(1000) DEFAULT NULL COMMENT '封面图片 多张逗号分隔',
    `labels` varchar(500) DEFAULT NULL COMMENT '标签',
    `likes` int DEFAULT 0 COMMENT '点赞数量',
    `collection` int DEFAULT 0 COMMENT '收藏数量',
    `comment` int DEFAULT 0 COMMENT '评论数量',
    `views` int DEFAULT 0 COMMENT '阅读数量',
    `score` int DEFAULT 0 COMMENT '评分',
    `province_id` int DEFAULT NULL COMMENT '省',
    `city_id` int DEFAULT NULL COMMENT '市',
    `county_id` int DEFAULT NULL COMMENT '区县',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
    `sync_status` tinyint DEFAULT 0 COMMENT '同步状态',
    `origin` tinyint DEFAULT NULL COMMENT '来源',
    `static_url` varchar(500) DEFAULT NULL COMMENT '静态页面地址',
    `status` tinyint DEFAULT 0 COMMENT '审核状态 0草稿 1提交审核 2审核失败 9已发布',
    `reason` varchar(500) DEFAULT NULL COMMENT '审核拒绝理由',
    `author_image` varchar(255) DEFAULT NULL COMMENT '作者头像',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_publish_time` (`publish_time`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章信息表';

-- 5. 文章配置表
CREATE TABLE IF NOT EXISTS `ap_article_config` (
    `id` bigint NOT NULL COMMENT '主键',
    `article_id` bigint NOT NULL COMMENT '文章id',
    `is_comment` tinyint DEFAULT 1 COMMENT '是否可评论 1是 0否',
    `is_forward` tinyint DEFAULT 1 COMMENT '是否转发 1是 0否',
    `is_down` tinyint DEFAULT 0 COMMENT '是否下架 1是 0否',
    `is_delete` tinyint DEFAULT 0 COMMENT '是否删除 1是 0否',
    `is_recommend` tinyint DEFAULT 1 COMMENT '是否推荐 1是 0否',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    KEY `idx_config_filter` (`is_delete`, `is_down`, `is_recommend`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章配置表';

-- 6. 文章内容表
CREATE TABLE IF NOT EXISTS `ap_article_content` (
    `id` bigint NOT NULL COMMENT '主键',
    `article_id` bigint NOT NULL COMMENT '文章id',
    `content` longtext COMMENT '文章内容',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章内容表';

-- 7. 文章草稿表
CREATE TABLE IF NOT EXISTS `ap_article_draft` (
    `id` bigint NOT NULL COMMENT '主键',
    `article_id` bigint DEFAULT NULL COMMENT '关联文章ID',
    `title` varchar(200) DEFAULT NULL COMMENT '标题',
    `author_id` bigint DEFAULT NULL COMMENT '作者id',
    `channel_id` int DEFAULT NULL COMMENT '频道id',
    `channel_name` varchar(50) DEFAULT NULL COMMENT '频道名称',
    `layout` smallint DEFAULT NULL COMMENT '布局',
    `images` varchar(1000) DEFAULT NULL COMMENT '封面图片',
    `labels` varchar(500) DEFAULT NULL COMMENT '标签',
    `topic` varchar(200) DEFAULT NULL COMMENT '话题',
    `content` longtext COMMENT '文章内容',
    `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
    `publish_time` datetime DEFAULT NULL COMMENT '定时发布时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` tinyint DEFAULT 0 COMMENT '状态',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章草稿表';

-- 8. 文章事件表（异步同步状态跟踪）
CREATE TABLE IF NOT EXISTS `ap_article_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` bigint NOT NULL COMMENT '文章ID',
    `retry_count` tinyint DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` tinyint DEFAULT 2 COMMENT '最大重试次数',
    `retry_time` datetime DEFAULT NULL COMMENT '重试时间',
    `send_status` tinyint DEFAULT 0 COMMENT '生产者发送状态 0初始化 1未成功 2已成功',
    `minio_status` tinyint DEFAULT 0 COMMENT 'minio状态 0初始化 1未成功 2已成功',
    `es_status` tinyint DEFAULT 0 COMMENT 'es状态 0初始化 1未成功 2已成功',
    `parameter` varchar(1000) DEFAULT NULL COMMENT '参数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_send_status` (`send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章事件表';

-- ==============================================
-- 三、作者相关表 (author)
-- ==============================================

-- 9. 作者表
CREATE TABLE IF NOT EXISTS `ap_author` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `type` int DEFAULT NULL COMMENT '类型 1签约合作商 2平台自媒体人',
    `user_id` int DEFAULT NULL COMMENT '社交账号用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `wm_user_id` int DEFAULT NULL COMMENT '自媒体账号ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_wm_user_id` (`wm_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作者信息表';

-- ==============================================
-- 四、评论相关表 (comment)
-- ==============================================

-- 10. 文章评论表
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

-- 11. 评论回复表
CREATE TABLE IF NOT EXISTS `ap_comment_reply` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` int NOT NULL COMMENT '回复用户ID',
    `user_name` varchar(50) DEFAULT '' COMMENT '回复用户昵称',
    `user_avatar` varchar(255) DEFAULT '' COMMENT '回复用户头像',
    `reply_to_user_id` int DEFAULT NULL COMMENT '被回复用户ID',
    `reply_to_user_name` varchar(50) DEFAULT '' COMMENT '被回复用户昵称',
    `content` text NOT NULL COMMENT '回复内容',
    `like_count` int DEFAULT 0 COMMENT '点赞数',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论回复表';

-- 12. 评论点赞记录表
CREATE TABLE IF NOT EXISTS `ap_comment_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';

-- ==============================================
-- 五、收藏/行为表
-- ==============================================

-- 13. 收藏表
CREATE TABLE IF NOT EXISTS `ap_collection` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `article_id` bigint NOT NULL COMMENT '文章ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 14. 用户行为表
CREATE TABLE IF NOT EXISTS `ap_behavior` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `article_id` bigint DEFAULT NULL COMMENT '文章ID',
    `behavior_type` varchar(50) NOT NULL COMMENT '行为类型: read, like, unlike, comment, share, follow, unfollow, collection',
    `duration` int DEFAULT 0 COMMENT '阅读时长（秒）',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_behavior_type` (`behavior_type`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- 注：ap_user_search / ap_associate_words 为MongoDB集合，不在此处创建MySQL表

-- ==============================================
-- 六、课程相关表 (course)
-- ==============================================

-- 15. 课程分类表
CREATE TABLE IF NOT EXISTS `ap_course_category` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 16. 课程表
CREATE TABLE IF NOT EXISTS `ap_course` (
    `id` bigint NOT NULL COMMENT '主键',
    `title` varchar(200) NOT NULL COMMENT '课程标题',
    `subtitle` varchar(500) DEFAULT '' COMMENT '副标题/摘要',
    `description` text COMMENT '课程详细介绍',
    `cover_image` varchar(255) DEFAULT '' COMMENT '封面图URL',
    `author_id` int NOT NULL COMMENT '作者用户ID',
    `author_name` varchar(50) NOT NULL COMMENT '作者昵称',
    `author_avatar` varchar(255) DEFAULT '' COMMENT '作者头像',
    `price` decimal(10,2) DEFAULT 0.00 COMMENT '售价',
    `original_price` decimal(10,2) DEFAULT 0.00 COMMENT '原价',
    `status` tinyint DEFAULT 0 COMMENT '状态 0草稿 1待审 2已上架 3已下架',
    `reason` varchar(500) DEFAULT NULL COMMENT '审核拒绝理由',
    `category_id` int NOT NULL COMMENT '分类ID',
    `chapter_count` int DEFAULT 0 COMMENT '小节数量',
    `study_count` int DEFAULT 0 COMMENT '学习人数',
    `estimated_hours` decimal(5,1) DEFAULT 0.0 COMMENT '预估学习时长（小时）',
    `published_at` datetime DEFAULT NULL COMMENT '上架时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 17. 课程章节表
CREATE TABLE IF NOT EXISTS `ap_course_chapter` (
    `id` bigint NOT NULL COMMENT '主键',
    `course_id` bigint NOT NULL COMMENT '所属课程',
    `title` varchar(200) NOT NULL COMMENT '小节标题',
    `sort_order` int DEFAULT 0 COMMENT '排序序号',
    `content` longtext COMMENT '小节正文（Markdown格式）',
    `word_count` int DEFAULT 0 COMMENT '字数统计',
    `is_free` tinyint DEFAULT 0 COMMENT '是否免费 0付费 1免费',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程小节表';

-- 18. 课程课时表（视频/音频课时）
CREATE TABLE IF NOT EXISTS `ap_course_lesson` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chapter_id` bigint NOT NULL COMMENT '所属章节',
    `course_id` bigint NOT NULL COMMENT '所属课程',
    `title` varchar(200) NOT NULL COMMENT '课时标题',
    `sort_order` int DEFAULT 0 COMMENT '排序序号',
    `content` longtext COMMENT '课时内容',
    `video_url` varchar(500) DEFAULT NULL COMMENT '视频地址',
    `duration` int DEFAULT 0 COMMENT '视频时长（秒）',
    `is_free` tinyint DEFAULT 0 COMMENT '是否免费 0付费 1免费',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_id` (`chapter_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课时表';

-- 19. 课程报名表
CREATE TABLE IF NOT EXISTS `ap_course_enrollment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `course_id` bigint NOT NULL COMMENT '课程ID',
    `enrolled_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course_enroll` (`user_id`, `course_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程报名表';

-- 20. 课程订单表
CREATE TABLE IF NOT EXISTS `ap_course_order` (
    `id` bigint NOT NULL COMMENT '主键',
    `order_no` varchar(50) NOT NULL COMMENT '对外订单号',
    `user_id` int NOT NULL COMMENT '下单用户',
    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
    `status` tinyint DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消 3已退款',
    `pay_method` varchar(20) DEFAULT '' COMMENT '支付方式',
    `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单表';

-- 21. 课程订单明细表
CREATE TABLE IF NOT EXISTS `ap_course_order_item` (
    `id` bigint NOT NULL COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '所属订单',
    `course_id` bigint NOT NULL COMMENT '购买的课程',
    `price` decimal(10,2) NOT NULL COMMENT '购买时的课程单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单明细表';

-- 22. 用户课程购买记录表
CREATE TABLE IF NOT EXISTS `ap_user_course` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `course_id` bigint NOT NULL COMMENT '课程ID',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单',
    `purchased_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
    `is_active` tinyint DEFAULT 1 COMMENT '权限是否有效',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程购买记录表';

-- 23. 阅读进度表
CREATE TABLE IF NOT EXISTS `ap_course_reading_progress` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` int NOT NULL COMMENT '用户ID',
    `chapter_id` bigint NOT NULL COMMENT '小节ID',
    `progress` float DEFAULT 0.0 COMMENT '阅读进度百分比',
    `last_read_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_chapter` (`user_id`, `chapter_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读进度表';

-- ==============================================
-- 七、沸点/圈子相关表 (pins)
-- ==============================================

-- 24. 圈子表
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

-- 25. 用户圈子关系表
CREATE TABLE IF NOT EXISTS `ap_user_circle` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `circle_id` bigint NOT NULL COMMENT '圈子ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_circle` (`user_id`, `circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户圈子关系表';

-- 25.1 人气圈子配置表
CREATE TABLE IF NOT EXISTS `ap_circle_hot_config` (
    `id` int NOT NULL AUTO_INCREMENT,
    `circle_id` bigint NOT NULL COMMENT '圈子ID',
    `display_order` int NOT NULL COMMENT '展示顺序 1-5',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_order` (`display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人气圈子配置表';

-- 25.2 圈子精选沸点表
CREATE TABLE IF NOT EXISTS `club_featured_pin` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `circle_id` bigint NOT NULL COMMENT '圈子ID',
    `pin_id` bigint NOT NULL COMMENT '沸点帖子ID',
    `sort_order` int DEFAULT 0 COMMENT '排序权重',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_club_pin` (`circle_id`, `pin_id`),
    KEY `idx_sort_order` (`circle_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子精选沸点表';

-- 26. 话题表
CREATE TABLE IF NOT EXISTS `ap_topic` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL COMMENT '话题名称',
    `description` varchar(200) DEFAULT '' COMMENT '导语',
    `cover_image` varchar(255) DEFAULT '' COMMENT '话题封面图',
    `type` tinyint DEFAULT 1 COMMENT '1-纯沸点, 2-文章+沸点',
    `post_count` int DEFAULT 0 COMMENT '关联帖子总数',
    `view_count` bigint DEFAULT 0 COMMENT '总阅读数',
    `participant_count` bigint DEFAULT 0 COMMENT '参与人数',
    `recommend_sort` int DEFAULT 0 COMMENT '推荐排序权重',
    `is_recommend` tinyint(1) DEFAULT 0 COMMENT '是否推荐至侧边栏',
    `badge` varchar(20) DEFAULT '' COMMENT '角标文字',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_recommend_sort` (`recommend_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- 27. 沸点帖子表
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
    `status` tinyint DEFAULT 0 COMMENT '状态 0草稿 1待审 2审核失败 9已发布',
    `reason` varchar(500) DEFAULT NULL COMMENT '审核拒绝理由',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_circle_id` (`circle_id`),
    KEY `idx_topic_id` (`topic_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点帖子表';

-- 28. 沸点评论表
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

-- 29. 沸点评论点赞表
CREATE TABLE IF NOT EXISTS `ap_pins_comment_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `comment_id` bigint NOT NULL COMMENT '评论ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点评论点赞记录表';

-- 30. 沸点帖子点赞表
CREATE TABLE IF NOT EXISTS `ap_pins_like` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `pins_id` bigint NOT NULL COMMENT '沸点ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pins_user` (`pins_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='沸点帖子点赞记录表';

-- ==============================================
-- 八、等级/权限相关表 (level)
-- ==============================================

-- 31. 用户等级表
CREATE TABLE IF NOT EXISTS `ap_user_level` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `daily_score` int DEFAULT 0 COMMENT '逐日分',
    `daily_level` tinyint DEFAULT 1 COMMENT '逐日等级',
    `power_value` int DEFAULT 0 COMMENT '逐力值',
    `power_level` tinyint DEFAULT 1 COMMENT '逐力等级',
    `daily_score_today` int DEFAULT 0 COMMENT '今日逐日分获取量',
    `power_value_today` int DEFAULT 0 COMMENT '今日逐力值获取量',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户等级表';

-- 32. 等级配置表
CREATE TABLE IF NOT EXISTS `ap_level_config` (
    `id` bigint NOT NULL COMMENT '主键',
    `level_type` tinyint NOT NULL COMMENT '等级类型 1-逐日等级 2-逐力等级',
    `level_value` tinyint NOT NULL COMMENT '等级值',
    `min_score` int NOT NULL COMMENT '最低分数',
    `max_score` int NOT NULL COMMENT '最高分数',
    `title` varchar(50) DEFAULT NULL COMMENT '等级头衔',
    `icon_url` varchar(255) DEFAULT NULL COMMENT '等级图标',
    `description` varchar(200) DEFAULT NULL COMMENT '等级描述',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_type_value` (`level_type`, `level_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级配置表';

-- 33. 等级特权表
CREATE TABLE IF NOT EXISTS `ap_level_privilege` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `level_type` tinyint NOT NULL COMMENT '等级类型 1-逐日等级 2-逐力等级',
    `level_value` tinyint NOT NULL COMMENT '等级值',
    `privilege_name` varchar(100) NOT NULL COMMENT '特权名称',
    `privilege_code` varchar(50) NOT NULL COMMENT '特权编码',
    `description` varchar(500) DEFAULT NULL COMMENT '特权描述',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_privilege` (`level_type`, `level_value`, `privilege_code`),
    KEY `idx_level_type` (`level_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级特权表';

-- 34. 权限定义表
CREATE TABLE IF NOT EXISTS `ap_permission_definition` (
    `id` bigint NOT NULL COMMENT '主键',
    `permission_code` varchar(50) NOT NULL COMMENT '权限编码',
    `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
    `description` varchar(500) DEFAULT NULL COMMENT '权限描述',
    `related_level_type` tinyint NOT NULL COMMENT '关联等级类型 1-逐日等级 2-逐力等级',
    `required_level` tinyint NOT NULL COMMENT '所需等级',
    `is_active` tinyint DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限定义表';

-- 35. 用户权限表
CREATE TABLE IF NOT EXISTS `ap_user_permission` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `permission_code` varchar(50) NOT NULL COMMENT '权限编码',
    `granted_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '授予时间',
    `expired_at` datetime DEFAULT NULL COMMENT '过期时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限表';

-- 36. 用户行为日志表
CREATE TABLE IF NOT EXISTS `ap_user_action_log` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `action_type` varchar(50) NOT NULL COMMENT '行为类型 daily_login, article_read, comment, like, share, follow',
    `score_change` int NOT NULL COMMENT '逐日分变化量',
    `action_detail` varchar(500) DEFAULT NULL COMMENT '行为详情',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 37. 逐力值变化日志表
CREATE TABLE IF NOT EXISTS `ap_user_power_log` (
    `id` bigint NOT NULL COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `power_change` int NOT NULL COMMENT '逐力值变化量',
    `change_type` varchar(50) NOT NULL COMMENT '变化类型 publish_article, get_like, get_comment, get_favorite, get_read',
    `source_id` bigint DEFAULT NULL COMMENT '来源ID（如文章ID）',
    `calculated_at` date DEFAULT NULL COMMENT '计算日期',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_change_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐力值变化日志表';

-- ==============================================
-- 九、通知相关表
-- ==============================================

-- 38. 通知表
CREATE TABLE IF NOT EXISTS `ap_notification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL COMMENT '接收用户ID',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` text COMMENT '通知内容',
    `type` varchar(50) NOT NULL COMMENT '通知类型 system, comment, like, follow, pins, course',
    `is_read` tinyint DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
    `related_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ==============================================
-- 十、自媒体相关表 (wemedia)
-- 注：wm_user 已废弃，统一使用 ap_user
--     wm_news 已废弃，发布流程改为 ap_article_draft → ap_article
--     wm_material 已废弃，素材业务未使用
-- 保留建表语句以兼容历史数据
-- ==============================================

-- 39. 频道信息表
CREATE TABLE IF NOT EXISTS `wm_channel` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) DEFAULT NULL COMMENT '频道名称',
    `description` varchar(255) DEFAULT NULL COMMENT '频道描述',
    `is_default` tinyint DEFAULT 0 COMMENT '是否默认频道 1默认 0非默认',
    `status` tinyint DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    `ord` int DEFAULT 0 COMMENT '默认排序',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道信息表';

-- 40. 标签表
CREATE TABLE IF NOT EXISTS `wm_tag` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) DEFAULT NULL COMMENT '标签名称',
    `category` varchar(50) DEFAULT NULL COMMENT '标签分类（语言方向/技术栈/数据库/其它）',
    `sort` int DEFAULT 0 COMMENT '排序',
    `status` int DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 41. 自媒体话题表
CREATE TABLE IF NOT EXISTS `wm_topic` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(100) DEFAULT NULL COMMENT '话题名称',
    `description` varchar(255) DEFAULT NULL COMMENT '话题描述',
    `sort` int DEFAULT 0 COMMENT '排序',
    `status` int DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自媒体话题表';

-- 42. 自媒体用户信息表（已废弃，保留兼容）
CREATE TABLE IF NOT EXISTS `wm_user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ap_user_id` int DEFAULT NULL COMMENT '关联app用户ID',
    `ap_author_id` int DEFAULT NULL COMMENT '关联作者ID',
    `name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
    `password` varchar(128) DEFAULT NULL COMMENT '登录密码',
    `salt` varchar(32) DEFAULT NULL COMMENT '盐',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `image` varchar(255) DEFAULT NULL COMMENT '头像',
    `location` varchar(100) DEFAULT NULL COMMENT '归属地',
    `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
    `status` int DEFAULT 9 COMMENT '状态 0暂时不可用 1永久不可用 9正常可用',
    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
    `type` int DEFAULT 0 COMMENT '账号类型 0个人 1企业 2子账号',
    `score` int DEFAULT 0 COMMENT '运营评分',
    `login_time` datetime DEFAULT NULL COMMENT '最后一次登录时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ap_user_id` (`ap_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自媒体用户信息表（已废弃）';

-- 43. 自媒体图文内容信息表（已废弃，保留兼容）
CREATE TABLE IF NOT EXISTS `wm_news` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int DEFAULT NULL COMMENT '自媒体用户ID',
    `author_name` varchar(50) DEFAULT NULL COMMENT '作者名称',
    `author_image` varchar(255) DEFAULT NULL COMMENT '作者头像',
    `title` varchar(200) DEFAULT NULL COMMENT '标题',
    `content` longtext COMMENT '图文内容',
    `type` tinyint DEFAULT NULL COMMENT '1无图 2单图',
    `channel_id` int DEFAULT NULL COMMENT '图文频道ID',
    `labels` varchar(500) DEFAULT NULL COMMENT '标签',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `submited_time` datetime DEFAULT NULL COMMENT '提交时间',
    `status` tinyint DEFAULT 0 COMMENT '状态 0草稿 1审核中 2审核失败 8审核通过 9已发布',
    `publish_time` datetime DEFAULT NULL COMMENT '定时发布时间',
    `reason` varchar(500) DEFAULT NULL COMMENT '拒绝理由',
    `article_id` bigint DEFAULT NULL COMMENT '发布库文章ID',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
    `enable` tinyint DEFAULT 1 COMMENT '是否启用',
    `cont_pics` varchar(2000) DEFAULT NULL COMMENT '内容图片',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自媒体图文内容信息表（已废弃）';

-- 44. 自媒体图文素材信息表（已废弃，保留兼容）
CREATE TABLE IF NOT EXISTS `wm_material` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int DEFAULT NULL COMMENT '自媒体用户ID',
    `url` varchar(500) DEFAULT NULL COMMENT '图片地址',
    `type` smallint DEFAULT 0 COMMENT '素材类型 0图片 1视频',
    `is_collection` smallint DEFAULT 0 COMMENT '是否收藏',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自媒体图文素材信息表（已废弃）';

-- 45. 自媒体图文引用素材信息表（已废弃，保留兼容）
CREATE TABLE IF NOT EXISTS `wm_news_material` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `material_id` int DEFAULT NULL COMMENT '素材ID',
    `news_id` int DEFAULT NULL COMMENT '图文ID',
    `type` smallint DEFAULT 0 COMMENT '引用类型 0内容引用 1主图引用',
    `ord` smallint DEFAULT 0 COMMENT '引用排序',
    PRIMARY KEY (`id`),
    KEY `idx_news_id` (`news_id`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自媒体图文引用素材信息表（已废弃）';

-- ==============================================
-- 十一、管理员相关表 (admin)
-- ==============================================

-- 46. 管理员用户信息表
CREATE TABLE IF NOT EXISTS `ad_user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) DEFAULT NULL COMMENT '登录用户名',
    `password` varchar(128) DEFAULT NULL COMMENT '登录密码',
    `salt` varchar(32) DEFAULT NULL COMMENT '盐',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `image` varchar(255) DEFAULT NULL COMMENT '头像',
    `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
    `status` int DEFAULT 9 COMMENT '状态 0暂时不可用 1永久不可用 9正常可用',
    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
    `login_time` datetime DEFAULT NULL COMMENT '最后一次登录时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户信息表';

-- ==============================================
-- 十二、调度任务相关表 (schedule)
-- ==============================================

-- 47. 任务信息表
CREATE TABLE IF NOT EXISTS `taskinfo` (
    `task_id` bigint NOT NULL COMMENT '任务id',
    `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
    `parameters` blob COMMENT '参数',
    `priority` int DEFAULT 0 COMMENT '优先级',
    `task_type` int DEFAULT NULL COMMENT '任务类型',
    PRIMARY KEY (`task_id`),
    KEY `idx_execute_time` (`execute_time`),
    KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

-- 48. 任务日志表
CREATE TABLE IF NOT EXISTS `taskinfo_logs` (
    `task_id` bigint NOT NULL COMMENT '任务id',
    `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
    `parameters` blob COMMENT '参数',
    `priority` int DEFAULT 0 COMMENT '优先级',
    `task_type` int DEFAULT NULL COMMENT '任务类型',
    `version` int DEFAULT 0 COMMENT '版本号（乐观锁）',
    `status` int DEFAULT 0 COMMENT '状态 0=INIT 1=EXECUTED 2=SUCCESS 3=CANCELLED',
    `first_exec_interval` bigint DEFAULT 0 COMMENT '预执行时间间隔',
    `last_exec_interval` bigint DEFAULT 0 COMMENT '执行时间间隔',
    PRIMARY KEY (`task_id`),
    KEY `idx_execute_time` (`execute_time`),
    KEY `idx_status` (`status`),
    KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

-- 28. 话题关联表
CREATE TABLE IF NOT EXISTS `topic_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `topic_id` bigint NOT NULL,
    `target_type` tinyint NOT NULL COMMENT '1-文章(article), 2-沸点(pin)',
    `target_id` bigint NOT NULL,
    `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_topic_target` (`topic_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题关联表';

-- 29. 用户话题发布记录表
CREATE TABLE IF NOT EXISTS `user_topic_post` (
    `user_id` bigint NOT NULL,
    `topic_id` bigint NOT NULL,
    `first_post_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `last_post_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `post_count` int DEFAULT 1,
    PRIMARY KEY (`user_id`, `topic_id`),
    KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户话题发布记录';

-- 30. 话题-圈子关联表
CREATE TABLE IF NOT EXISTS `topic_circle_relation` (
    `topic_id` bigint NOT NULL,
    `circle_id` bigint NOT NULL,
    PRIMARY KEY (`topic_id`, `circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题-圈子关联表';

-- ==============================================
-- 完成标记
-- ==============================================
-- 所有 ap_ 前缀业务表：30 张
-- 所有 wm_ 前缀表：7 张（其中3张已废弃保留兼容）
-- 所有 ad_ 前缀表：1 张
-- 所有 taskinfo 系列表：2 张
-- 所有 topic_ / user_topic 系列表：3 张
-- 总计：51 张表
--
-- 注：ap_user_search 和 ap_associate_words 为 MongoDB 集合，
--     不在 MySQL 中创建，相关文档类见 search 模块。
-- ==============================================