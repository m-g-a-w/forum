USE `knowledge_platform`;

-- 1. 更新之前 3 个专栏的封面为高清 Unsplash 图片
UPDATE `column_info` SET `cover` = 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80' WHERE `id` = 1;
UPDATE `column_info` SET `cover` = 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80' WHERE `id` = 2;
UPDATE `column_info` SET `cover` = 'https://images.unsplash.com/photo-1561070791-2526d30994b5?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80' WHERE `id` = 3;

-- 2. 增加一位新的大V创作者
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `email`, `role`, `balance`, `status`) VALUES 
(4, 'creator_pro', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'pro@example.com', 1, 100.00, 1);

-- 3. 插入全新带图专栏
INSERT IGNORE INTO `column_info` (`id`, `creator_id`, `title`, `description`, `cover`, `price`, `status`) VALUES 
(4, 4, '深入理解计算机系统 CSAPP', '程序员必学的底层知识，从 C 语言到汇编、内存管理、操作系统内核原理。', 'https://images.unsplash.com/photo-1518770660439-4636190af475?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80', 29.99, 1),
(5, 4, '人人都是产品经理', '以一线互联网公司的真实案例，剖析需求调研、交互设计与敏捷迭代全过程。', 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80', 12.50, 1),
(6, 4, 'Python 爬虫实战：数据变现指南', '从 Requests + BS4 到 Scrapy 异步分布式抓取框架，应对各种反爬机制。', 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80', 19.99, 1),
(7, 2, '跟大师学人像摄影', '通过对光影的捕捉，讲述人像摄影的核心技巧与后期 Lightroom 调色心法。', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80', 49.90, 1),
(8, 2, '自由职业接单避坑指南', '全方位为您解析作为独立开发者/设计师，如何报价、沟通需求与签署合同。', 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?ixlib=rb-1.2.1&auto=format&fit=crop&w=600&q=80', 0.00, 1);

-- 4. 为新专栏插入文章 (包含图片)
INSERT IGNORE INTO `article` (`id`, `column_id`, `title`, `content`, `is_free`, `sort_order`, `status`) VALUES 
(7, 4, '01 信息的表示与处理', '<p>我们先来看一张CPU架构的经典图片：</p><img src="https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60" alt="CPU"><p>在计算机内，一切信息都是以 0 和 1 的形式存储的。这节课先带大家重温进制转换。</p>', 1, 1, 1),
(8, 4, '02 深入解析汇编语言', '<p>此为付费核心篇章。</p><p>我们在C语言中写下的 if-else 语句，被 GCC 转译后会变成哪些 jump 指令？</p>', 0, 2, 1),

(9, 5, '做交互设计的黄金法则', '<p>设计是一门艺术，但产品设计是一门科学。</p><img src="https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60"><p>了解用户心理预期，是做好按钮摆放位置的前提。</p>', 1, 1, 1),

(10, 6, 'BeautifulSoup的魔法清洗', '<p>如何从杂乱无章的 HTML 中剥离出纯净的文本？</p><p>本课免费开放。</p>', 1, 1, 1),
(11, 6, 'Selenium 突破动态加载', '<p>付费章节：当数据是通过 JS 渲染出来的，静态请求无能为力时，我们就需要祭出无头浏览器。</p><img src="https://images.unsplash.com/photo-1555949963-ff9fe0c870eb?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60"><p>这里是核心配置代码......</p>', 0, 2, 1),

(12, 7, '光影的魔术手', '<p>先欣赏这张大师级黑白人像：</p><img src="https://images.unsplash.com/photo-1517404215738-15263e9f9178?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60"><p>要想拍得高级，一定要学会制造“影子”。</p>', 1, 1, 1),
(13, 8, '如何给外包项目报价？', '<p>直接说重点（免费放送）：先摸清楚客户的底线，再乘以你时薪的两倍。</p><img src="https://images.unsplash.com/photo-1553729459-efe14ef6055d?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60">', 1, 1, 1);
