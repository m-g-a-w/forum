USE `knowledge_platform`;

-- 密码统一为 '123456' 的 BCrypt Hash: $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2

-- 插入创作者与普通用户
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `email`, `role`, `balance`, `status`) VALUES 
(2, 'creator1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'creator1@example.com', 1, 100.00, 1),
(3, 'reader1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'reader1@example.com', 0, 100.00, 1);

-- 插入专栏
INSERT IGNORE INTO `column_info` (`id`, `creator_id`, `title`, `description`, `cover`, `price`, `status`) VALUES 
(1, 2, 'Vue 3 企业级进阶实战', '涵盖 Vue3 Composition API, Vite 构建与性能优化，手把手带你完成大型前端微服务架构设计。', 'https://via.placeholder.com/600x400?text=Vue+3+Mastery', 9.99, 1),
(2, 2, 'Spring Boot 微服务架构与全链路追踪', '系统深入讲解 Spring Boot 2.x/3.x 与 Spring Cloud Netflix/Alibaba 微服务体系，提供工业级解决方案。', 'https://via.placeholder.com/600x400?text=Spring+Boot+Microservices', 19.99, 1),
(3, 2, '零基础 UI 设计入门', '针对程序员和初学者的极简 UI 设计课程，培养美感与常用设计工具的落地实践。', 'https://via.placeholder.com/600x400?text=UI+UX+Design', 0.00, 1);

-- 插入文章
INSERT IGNORE INTO `article` (`id`, `column_id`, `title`, `content`, `is_free`, `sort_order`, `status`) VALUES 
(1, 1, '第一章：Vue 3 核心变更与响应式原理解析', '<p>欢迎来到 Vue 3 进阶实战。本章节我们将深入探讨 Proxy 代理模式如何替代原有的 Object.defineProperty。这是免费阅读内容。</p>', 1, 1, 1),
(2, 1, '第二章：深入探究 Composition API 最佳实践', '<p>本文是进阶内容，只有订阅本专栏的同学们才能看到。</p><p>在这里我们分享了如何从 Options API 优雅地迁移，并剥离聚合复用逻辑的核心心法。</p>', 0, 2, 1),
(3, 2, '第一章：微服务到底解决什么问题？', '<p>微服务并不是万灵药。作为架构师，你需要判断系统的业务边界和组织架构。我们先来聊一聊演进式架构。</p>', 1, 1, 1),
(4, 2, '第二章：Spring Cloud Gateway 动态路由实战', '<p>配置动态路由需要结合 Nacos 做到热加载配置。这部分代码较多，请仔细听。</p><pre><code>@Bean\npublic RouteLocator customRouteLocator(RouteLocatorBuilder builder) { ... }</code></pre>', 0, 2, 1),
(5, 3, '第一章：打好基础——色彩心理学篇', '<p>不要什么系统都用红蓝绿。要善于利用 HSL 模式去调配有高级感的颜色。</p>', 1, 1, 1),
(6, 3, '第二章：如何设计有层次感的卡片阴影', '<p>阴影不在深，而在乎多层晕染。本课免费开放给大家作为日常实战指南。</p>', 1, 2, 1);

-- 插入评论测试
INSERT IGNORE INTO `comment` (`id`, `article_id`, `user_id`, `content`) VALUES
(1, 1, 3, '太棒了，这正是我要找的文章！通俗易懂。');

-- 插入默认订阅记录（用户默认订阅专栏1）
INSERT IGNORE INTO `subscription` (`id`, `user_id`, `column_id`, `status`, `create_time`) VALUES
(1, 3, 1, 1, NOW());
