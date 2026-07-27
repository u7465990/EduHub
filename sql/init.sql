-- ============================================================
-- EduHub 智能作业平台 - 数据库初始化脚本
-- 阶段1: 核心表结构 (MySQL 8.0+)
-- ============================================================

CREATE DATABASE IF NOT EXISTS eduhub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eduhub;

-- -----------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(255) NOT NULL COMMENT '加密密码',
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色: user/admin/vip',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `coins`         INT          NOT NULL DEFAULT 0 COMMENT '学币',
    `last_login_at` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 题目标签/分类表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`   BIGINT      DEFAULT NULL COMMENT '父分类ID',
    `level`       INT         NOT NULL DEFAULT 1 COMMENT '层级',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类';

-- -----------------------------------------------------------
-- 题目表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `category_id`   BIGINT       DEFAULT NULL COMMENT '分类ID',
    `user_id`       BIGINT       DEFAULT NULL COMMENT '上传者ID',
    `title`         VARCHAR(255) DEFAULT NULL COMMENT '题目标题',
    `content`       TEXT         NOT NULL COMMENT '题目内容(文本)',
    `content_img`   VARCHAR(255) DEFAULT NULL COMMENT '题目图片URL',
    `answer`        TEXT         DEFAULT NULL COMMENT '答案/解析',
    `difficulty`    INT          NOT NULL DEFAULT 2 COMMENT '难度: 1简单 2中等 3困难',
    `subject`       VARCHAR(50)  DEFAULT NULL COMMENT '学科: math/english/physics/...',
    `grade`         VARCHAR(20)  DEFAULT NULL COMMENT '年级',
    `type`          VARCHAR(20)  NOT NULL DEFAULT 'choice' COMMENT '题型: choice/multi_choice/fill/essay/judge',
    `options`       JSON         DEFAULT NULL COMMENT '选择题选项 (JSON)',
    `view_count`    INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1审核通过 0待审核 -1拒绝',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_subject` (`subject`),
    KEY `idx_created_at` (`created_at`),
    FULLTEXT KEY `ft_content` (`content`, `title`)  /* 全文检索 */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- -----------------------------------------------------------
-- 用户答题记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_answer` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT   NOT NULL COMMENT '用户ID',
    `question_id`   BIGINT   NOT NULL COMMENT '题目ID',
    `user_answer`   TEXT     DEFAULT NULL COMMENT '用户提交的答案',
    `is_correct`    TINYINT  DEFAULT NULL COMMENT '是否正确: 1正确 0错误',
    `ai_score`      INT      DEFAULT NULL COMMENT 'AI评分(0-100)',
    `ai_feedback`   TEXT     DEFAULT NULL COMMENT 'AI批改反馈',
    `answered_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_question` (`user_id`, `question_id`),
    KEY `idx_answered_at` (`answered_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户答题记录';

-- -----------------------------------------------------------
-- 收藏夹表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `question_id` BIGINT   NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹';

-- -----------------------------------------------------------
-- 操作日志表 (用于后续审计)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `operate_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       DEFAULT NULL,
    `module`      VARCHAR(50)  NOT NULL COMMENT '模块',
    `operation`   VARCHAR(100) NOT NULL COMMENT '操作描述',
    `request_url` VARCHAR(255) DEFAULT NULL,
    `method`      VARCHAR(10)  DEFAULT NULL,
    `params`      TEXT         DEFAULT NULL,
    `ip`          VARCHAR(50)  DEFAULT NULL,
    `duration`    INT          DEFAULT NULL COMMENT '耗时(ms)',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_module` (`user_id`, `module`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ============================================================
-- 初始数据
-- ============================================================
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('数学', NULL, 1, 1),
('语文', NULL, 1, 2),
('英语', NULL, 1, 3),
('物理', NULL, 1, 4),
('化学', NULL, 1, 5),
('生物', NULL, 1, 6);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin'),
('demo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '演示用户', 'user');
-- 密码都是 "123456"，BCrypt加密
