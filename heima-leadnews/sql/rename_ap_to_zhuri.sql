-- 数据库迁移脚本：将 ap_ 和 wm_ 表重命名为 zhuri_ 前缀
-- 产品名：逐日 (zhuri)
-- 
-- ⚠️ 注意：此脚本为未来迁移使用，当前数据库表仍使用 ap_/wm_ 前缀
-- 执行前请先：
--   1. 执行 init_all_tables.sql 创建所有缺失的表
--   2. 备份数据库
--   3. 修改所有 Java 实体类 @TableName 为 zhuri_ 前缀
--   4. 修改所有 Mapper XML 中的表引用
--   5. 重新编译部署

-- ============================================
-- ap_ 前缀表重命名
-- ============================================
ALTER TABLE ap_article RENAME TO zhuri_article;
ALTER TABLE ap_article_config RENAME TO zhuri_article_config;
ALTER TABLE ap_article_content RENAME TO zhuri_article_content;
ALTER TABLE ap_article_draft RENAME TO zhuri_article_draft;
ALTER TABLE ap_article_event RENAME TO zhuri_article_event;
ALTER TABLE ap_user RENAME TO zhuri_user;
ALTER TABLE ap_user_social_binding RENAME TO zhuri_user_social_binding;
ALTER TABLE ap_author RENAME TO zhuri_author;
ALTER TABLE ap_comment RENAME TO zhuri_comment;
ALTER TABLE ap_comment_reply RENAME TO zhuri_comment_reply;
ALTER TABLE ap_comment_like RENAME TO zhuri_comment_like;
ALTER TABLE ap_collection RENAME TO zhuri_collection;
ALTER TABLE ap_behavior RENAME TO zhuri_behavior;
ALTER TABLE ap_search RENAME TO zhuri_search;
ALTER TABLE ap_associate_words RENAME TO zhuri_associate_words;
ALTER TABLE ap_user_follow RENAME TO zhuri_user_follow;
ALTER TABLE ap_course RENAME TO zhuri_course;
ALTER TABLE ap_course_chapter RENAME TO zhuri_course_chapter;
ALTER TABLE ap_course_lesson RENAME TO zhuri_course_lesson;
ALTER TABLE ap_course_enrollment RENAME TO zhuri_course_enrollment;
ALTER TABLE ap_pins RENAME TO zhuri_pins;
ALTER TABLE ap_pins_comment RENAME TO zhuri_pins_comment;
ALTER TABLE ap_pins_like RENAME TO zhuri_pins_like;
ALTER TABLE ap_level_config RENAME TO zhuri_level_config;
ALTER TABLE ap_user_level RENAME TO zhuri_user_level;
ALTER TABLE ap_level_privilege RENAME TO zhuri_level_privilege;
ALTER TABLE ap_notification RENAME TO zhuri_notification;
ALTER TABLE ap_user_action_log RENAME TO zhuri_user_action_log;
ALTER TABLE ap_user_permission RENAME TO zhuri_user_permission;
ALTER TABLE ap_user_power_log RENAME TO zhuri_user_power_log;
ALTER TABLE ap_circle RENAME TO zhuri_circle;
ALTER TABLE ap_user_circle RENAME TO zhuri_user_circle;
ALTER TABLE ap_user_course RENAME TO zhuri_user_course;
ALTER TABLE ap_course_category RENAME TO zhuri_course_category;
ALTER TABLE ap_course_order RENAME TO zhuri_course_order;
ALTER TABLE ap_course_order_item RENAME TO zhuri_course_order_item;
ALTER TABLE ap_course_reading_progress RENAME TO zhuri_course_reading_progress;
ALTER TABLE ap_permission_definition RENAME TO zhuri_permission_definition;
ALTER TABLE ap_topic RENAME TO zhuri_topic;
ALTER TABLE ap_pins_comment_like RENAME TO zhuri_pins_comment_like;

-- ============================================
-- wm_ 前缀表重命名
-- ============================================
ALTER TABLE wm_channel RENAME TO zhuri_channel;
ALTER TABLE wm_tag RENAME TO zhuri_tag;
ALTER TABLE wm_topic RENAME TO zhuri_topic;

-- ============================================
-- 注意：
-- 1. wm_user 表已废弃，统一使用 zhuri_user
-- 2. wm_news 表已废弃，发布流程改为 zhuri_article_draft → zhuri_article 直连
-- 3. wm_material 表已废弃，素材业务未使用
-- 4. 执行前请确保已在测试环境验证
-- 5. 建议先备份数据库
-- ============================================