-- ============================================
-- 课程系统扩展 DDL（Phase 1: 基础设施）
-- 执行前请确保 leadnews_article 数据库已存在
-- ============================================

USE leadnews_article;

-- ============================================
-- 1. 扩展 ap_course 表
-- ============================================
ALTER TABLE ap_course ADD COLUMN is_deleted TINYINT(1) DEFAULT 0 COMMENT '软删除标记';
ALTER TABLE ap_course ADD COLUMN version INT DEFAULT 1 COMMENT '版本号';
ALTER TABLE ap_course ADD COLUMN sales_count INT DEFAULT 0 COMMENT '销售数量';
ALTER TABLE ap_course ADD COLUMN total_revenue DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计收入';

-- ============================================
-- 2. 扩展 ap_course_chapter 表
-- ============================================
ALTER TABLE ap_course_chapter ADD COLUMN status TINYINT DEFAULT 1 COMMENT '0草稿 1已发布';
ALTER TABLE ap_course_chapter ADD COLUMN estimated_minutes INT DEFAULT 5 COMMENT '预估阅读时长(分钟)';
ALTER TABLE ap_course_chapter ADD COLUMN comment_count INT DEFAULT 0 COMMENT '评论数';

-- ============================================
-- 3. 课程订单表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id INT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    original_amount DECIMAL(10,2) NOT NULL COMMENT '原价',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '折扣金额',
    paid_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    discount_code VARCHAR(32) DEFAULT '' COMMENT '使用的折扣码',
    status TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2已取消 3已退款',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    trade_no VARCHAR(64) DEFAULT '' COMMENT '支付宝交易号',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单表';

-- ============================================
-- 4. 折扣码表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_discount (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    code VARCHAR(32) NOT NULL COMMENT '折扣码',
    discount_type TINYINT NOT NULL COMMENT '1固定金额 2百分比',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '折扣值',
    max_uses INT DEFAULT 100 COMMENT '最大使用次数',
    used_count INT DEFAULT 0 COMMENT '已使用次数',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 1 COMMENT '1有效 0失效',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程折扣码表';

-- ============================================
-- 5. 编辑审核记录表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    reviewer_id INT NOT NULL COMMENT '审核人ID',
    action TINYINT NOT NULL COMMENT '1通过 2拒绝 3反馈',
    comment VARCHAR(1000) DEFAULT '' COMMENT '审核意见',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程审核记录表';

-- ============================================
-- 6. 邀请编辑表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_invitation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    inviter_id INT NOT NULL COMMENT '邀请人ID',
    token VARCHAR(64) NOT NULL COMMENT '邀请token',
    status TINYINT DEFAULT 0 COMMENT '0待接受 1已接受 2已过期',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程邀请编辑表';

-- ============================================
-- 7. 收入结算表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_settlement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id INT NOT NULL COMMENT '作者ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    settlement_month VARCHAR(7) NOT NULL COMMENT '结算月份(YYYY-MM)',
    total_sales DECIMAL(10,2) DEFAULT 0.00 COMMENT '总销售额',
    platform_share DECIMAL(10,2) DEFAULT 0.00 COMMENT '平台分成',
    author_share DECIMAL(10,2) DEFAULT 0.00 COMMENT '作者分成',
    order_count INT DEFAULT 0 COMMENT '订单数',
    status TINYINT DEFAULT 0 COMMENT '0待结算 1已结算',
    settled_at DATETIME DEFAULT NULL COMMENT '结算时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_author_course_month (author_id, course_id, settlement_month),
    KEY idx_author_id (author_id),
    KEY idx_settlement_month (settlement_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程收入结算表';

-- ============================================
-- 8. 章节评论表
-- ============================================
CREATE TABLE IF NOT EXISTS ap_course_chapter_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    user_id INT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
    user_avatar VARCHAR(255) DEFAULT '' COMMENT '用户头像',
    content VARCHAR(2000) NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID(0为一级评论)',
    reply_to_uid INT DEFAULT 0 COMMENT '回复目标用户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_chapter_id (chapter_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节评论表';