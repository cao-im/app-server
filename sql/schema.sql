-- 创建数据库
CREATE DATABASE IF NOT EXISTS cao_im_app_server DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cao_im_app_server;

-- 用户表
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) DEFAULT '' COMMENT '昵称',
    im_token VARCHAR(255) DEFAULT '' COMMENT 'IM Token',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化管理员账号（后台管理系统专用）
-- 用户名: admin, 密码: admin123 (BCrypt加密)
INSERT INTO app_user (username, password, nickname, status) VALUES
('admin', '$2a$10$TfjvtpjrdOabAukwCNlioO01jNsvLg67jFVSrmgAihwRMS0BuQXjK', '管理员', 1);
