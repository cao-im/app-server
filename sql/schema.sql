-- 应用服务数据库初始化脚本
-- 数据库: cao_im_app_server
-- 说明: 业务用户表，与IM用户分离，通过 im_user_id 关联

CREATE DATABASE IF NOT EXISTS cao_im_app_server DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cao_im_app_server;

-- 用户表（业务用户）
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(100) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    im_user_id BIGINT DEFAULT NULL COMMENT '关联的IM用户ID',
    im_token VARCHAR(255) DEFAULT '' COMMENT 'IM Token',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_im_user_id (im_user_id),
    INDEX idx_phone (phone),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务用户表';
