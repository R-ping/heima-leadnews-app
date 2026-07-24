USE leadnews_article;

-- 为 ap_user_course 表补充字段
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'access_type', 'ALTER TABLE ap_user_course ADD COLUMN access_type TINYINT DEFAULT 1 COMMENT ''1-购买 2-VIP借阅''');
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'borrow_expire_at', 'ALTER TABLE ap_user_course ADD COLUMN borrow_expire_at DATETIME COMMENT ''VIP借阅到期时间''');
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'progress', 'ALTER TABLE ap_user_course ADD COLUMN progress DECIMAL(5,2) DEFAULT 0.00 COMMENT ''学习进度百分比''');
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'last_learn_chapter_id', 'ALTER TABLE ap_user_course ADD COLUMN last_learn_chapter_id BIGINT COMMENT ''最后学习章节ID''');
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'last_learn_at', 'ALTER TABLE ap_user_course ADD COLUMN last_learn_at DATETIME COMMENT ''最后学习时间''');
CALL add_column_if_not_exists('leadnews_article', 'ap_user_course', 'is_trial', 'ALTER TABLE ap_user_course ADD COLUMN is_trial TINYINT(1) DEFAULT 0 COMMENT ''是否试学状态''');

-- 为 ap_course_reading_progress 表补充字段
CALL add_column_if_not_exists('leadnews_article', 'ap_course_reading_progress', 'is_completed', 'ALTER TABLE ap_course_reading_progress ADD COLUMN is_completed TINYINT(1) DEFAULT 0 COMMENT ''是否已完成''');
CALL add_column_if_not_exists('leadnews_article', 'ap_course_reading_progress', 'completed_at', 'ALTER TABLE ap_course_reading_progress ADD COLUMN completed_at DATETIME COMMENT ''完成时间''');

-- 为 id 列添加 AUTO_INCREMENT（如果尚未设置）
ALTER TABLE ap_user_course MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE ap_course_reading_progress MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- 插入课程种子数据（仅当表为空时）
INSERT IGNORE INTO ap_course (id, title, subtitle, description, cover_image, author_id, author_name, price, original_price, status, category_id, chapter_count, study_count, published_at, created_time) VALUES
(1, '如何使用掘金社区', '快速上手掘金社区的各项功能', '本课程将带你全面了解掘金社区的使用方法...', '', 1, '阴明', 0.00, 0.00, 9, 1, 10, 1520, '2025-01-01', NOW()),
(2, '如何写一本掘金小册', '从零开始创作你的第一本掘金小册', '本课程将教你如何从选题到发布...', '', 2, '阴明', 29.90, 49.90, 9, 2, 15, 890, '2025-03-15', NOW()),
(3, 'Java入门教程', '零基础学习Java编程', '适合初学者的Java编程入门课程...', '', 3, 'Dannyldea', 19.90, 39.90, 9, 3, 20, 2100, '2025-06-01', NOW()),
(4, '实战：手把手做一个MiniCodeAgent', '动手实践AI编程助手', '从零开始构建一个Mini Code Agent...', '', 4, '葱哥', 39.90, 59.90, 9, 4, 12, 650, '2025-08-01', NOW()),
(5, 'MySQL是怎样运行的：从根儿上理解MySQL', '深入理解MySQL底层原理', '从源码层面理解MySQL的工作原理...', '', 5, '小孩子4919', 49.90, 79.90, 9, 5, 25, 3200, '2025-10-01', NOW());

-- 插入用户课程种子数据（仅当表为空时，user_id=1）
INSERT IGNORE INTO ap_user_course (user_id, course_id, access_type, progress, last_learn_at, is_active, created_time) VALUES
(1, 1, 1, 28.00, '2026-07-22 16:24:00', 1, NOW()),
(1, 2, 1, 99.00, '2026-07-20 10:30:00', 1, NOW()),
(1, 3, 1, 5.00, '2026-07-18 09:00:00', 1, NOW()),
(1, 4, 2, 0.00, '2026-07-15 14:00:00', 1, NOW()),
(1, 5, 1, 45.00, '2026-07-21 20:00:00', 1, NOW());

-- 插入章节种子数据（仅当表为空时，课程1）
INSERT IGNORE INTO ap_course_chapter (course_id, title, sort_order, content, is_free, created_time) VALUES
(1, '欢迎来到掘金社区', 1, '# 欢迎来到掘金社区\n\n掘金是一个帮助开发者成长的社区...', 1, NOW()),
(1, '如何发布文章', 2, '# 如何发布文章\n\n在掘金发布文章非常简单...', 1, NOW()),
(1, '如何使用沸点', 3, '# 如何使用沸点\n\n沸点是掘金的轻量级内容...', 0, NOW()),
(1, '如何参与课程', 4, '# 如何参与课程\n\n课程是掘金的知识付费产品...', 0, NOW()),
(1, '社区规范', 5, '# 社区规范\n\n请遵守掘金社区规范...', 0, NOW());