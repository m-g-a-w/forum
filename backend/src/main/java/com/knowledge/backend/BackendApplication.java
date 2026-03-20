package com.knowledge.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@SpringBootApplication
@MapperScan("com.knowledge.backend.mapper")
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                // 创建章节表
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `chapter` (" +
                        "    `id` BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "    `column_id` BIGINT NOT NULL COMMENT '所属专栏ID'," +
                        "    `title` VARCHAR(100) NOT NULL COMMENT '章节标题'," +
                        "    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序'," +
                        "    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                        "    INDEX `idx_column` (`column_id`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏章节表'");
                
                // 为文章表增加章节ID字段（如果不存在）
                // 注意：MySQL 8.0 以下不支持 ADD COLUMN IF NOT EXISTS，这里用 try-catch 或者查询 information_schema
                try {
                    jdbcTemplate.execute("ALTER TABLE `article` ADD COLUMN `chapter_id` BIGINT COMMENT '所属章节ID' AFTER `column_id` ");
                } catch (Exception e) {
                    // 字段已存在则忽略
                }
                
                try {
                    jdbcTemplate.execute("ALTER TABLE `article` ADD INDEX `idx_chapter` (`chapter_id`) ");
                } catch (Exception e) {
                   // 索引已存在则忽略
                }
                
                // 创建留言墙表
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `message_wall` (" +
                        "    `id` BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "    `user_id` BIGINT COMMENT '用户ID'," +
                        "    `nickname` VARCHAR(50) COMMENT '昵称'," +
                        "    `avatar` VARCHAR(255) COMMENT '头像URL'," +
                        "    `content` TEXT NOT NULL COMMENT '留言内容'," +
                        "    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间'" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言墙表'");

                // 注入 Mock 订阅数据 (为测试账号 test1, ID=7 准备)
                try {
                    // 先检查是否有专栏，如果没有则不注入
                    List<Long> columnIds = jdbcTemplate.queryForList("SELECT id FROM column_info LIMIT 3", Long.class);
                    if (!columnIds.isEmpty()) {
                        for (Long colId : columnIds) {
                            jdbcTemplate.execute("INSERT IGNORE INTO `subscription` (`user_id`, `column_id`, `create_time`) " +
                                    "VALUES (7, " + colId + ", NOW())");
                        }
                    }
                } catch (Exception e) {
                    // 忽略注入失败
                }
                
                System.out.println(">> [SUCCESS] Database Schema & Mock Data Updated.");
            } catch (Exception e) {
                System.err.println(">> [ERROR] Database Schema Update Failed: " + e.getMessage());
            }
        };
    }
}
