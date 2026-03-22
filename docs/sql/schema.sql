-- 论坛专栏发布与订阅管理系统 SQL 模型
CREATE DATABASE IF NOT EXISTS `knowledge_platform` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `knowledge_platform`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `bio` VARCHAR(255) COMMENT '个人简介',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0普通用户(读者), 1创作者, 2管理员',
    `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '虚拟余额(用于模退购买)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用, 1正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 专栏表
CREATE TABLE IF NOT EXISTS `column_info` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `creator_id` BIGINT NOT NULL COMMENT '创作者ID',
    `title` VARCHAR(100) NOT NULL COMMENT '专栏标题',
    `description` TEXT COMMENT '专栏简介',
    `cover` VARCHAR(255) COMMENT '专栏封面URL',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '专栏价格(0为免费)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0下架/草稿, 1上架/已发布',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏信息表';

-- 3. 文章表
CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `column_id` BIGINT NOT NULL COMMENT '所属专栏ID',
    `chapter_id` BIGINT COMMENT '所属章节ID',
    `title` VARCHAR(100) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT NOT NULL COMMENT '文章正文(MD或HTML)',
    `is_free` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免费试看: 0否, 1是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0草稿, 1发布',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_column` (`column_id`),
    INDEX `idx_chapter` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏文章表';

-- 3.5 章节表
CREATE TABLE IF NOT EXISTS `chapter` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `column_id` BIGINT NOT NULL COMMENT '所属专栏ID',
    `title` VARCHAR(100) NOT NULL COMMENT '章节标题',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_column` (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏章节表';

-- 4. 订阅/已购表
CREATE TABLE IF NOT EXISTS `subscription` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '购买者ID',
    `column_id` BIGINT NOT NULL COMMENT '购买的专栏ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅/购买时间',
    UNIQUE INDEX `uk_user_column` (`user_id`, `column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户专栏订阅表';

-- 5. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '购买者ID',
    `column_id` BIGINT NOT NULL COMMENT '对应的专栏ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '实付金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0待支付, 1已支付, 2已取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME COMMENT '支付时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 6. 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_article` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 初始化管理员账号: admin / admin (假设使用BCrypt加密后，这里密码暂时写明文/固定哈希，后续在Java里生成)
-- 为了方便测试，初始化一条记录，密码为123456的BCrypt哈希: $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `email`, `role`, `balance`, `status`) 
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@example.com', 2, 9999.00, 1);
