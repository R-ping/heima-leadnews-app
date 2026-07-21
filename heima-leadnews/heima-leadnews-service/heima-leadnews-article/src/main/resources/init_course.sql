CREATE TABLE IF NOT EXISTS `ap_course_category` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

INSERT INTO `ap_course_category` (`id`, `name`, `sort_order`) VALUES
(1, '后端', 1),
(2, '前端', 2),
(3, 'Android', 3),
(4, 'iOS', 4),
(5, '人工智能', 5),
(6, '开发工具', 6),
(7, '代码人生', 7),
(8, '阅读', 8);

CREATE TABLE IF NOT EXISTS `ap_course` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(200) NOT NULL COMMENT '课程标题',
    `subtitle` varchar(500) DEFAULT '' COMMENT '副标题/摘要',
    `description` text COMMENT '课程详细介绍',
    `cover_image` varchar(255) DEFAULT '' COMMENT '封面图URL',
    `author_id` int NOT NULL COMMENT '作者用户ID',
    `author_name` varchar(50) NOT NULL COMMENT '作者昵称',
    `author_avatar` varchar(255) DEFAULT '' COMMENT '作者头像',
    `price` decimal(10,2) DEFAULT 0.00 COMMENT '售价',
    `original_price` decimal(10,2) DEFAULT 0.00 COMMENT '原价',
    `status` tinyint DEFAULT 2 COMMENT '状态（0-草稿, 1-待审, 2-已上架, 3-已下架）',
    `category_id` int NOT NULL COMMENT '分类ID',
    `chapter_count` int DEFAULT 0 COMMENT '小节数量',
    `study_count` int DEFAULT 0 COMMENT '学习人数',
    `estimated_hours` decimal(5,1) DEFAULT 0.0 COMMENT '预估学习时长（小时）',
    `published_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

INSERT INTO `ap_course` (`id`, `title`, `subtitle`, `description`, `cover_image`, `author_id`, `author_name`, `author_avatar`, `price`, `original_price`, `status`, `category_id`, `chapter_count`, `study_count`, `estimated_hours`, `published_at`) VALUES
(1, 'Vue3 完全指南', '从零开始掌握 Vue3 组合式 API', '本课程将带你从零开始学习 Vue3 的核心概念，包括组合式 API、响应式系统、组件通信等。通过大量实战案例，帮助你快速掌握 Vue3 开发技能。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 1, '张三', '', 49.00, 99.00, 2, 2, 8, 1256, 8.5, '2024-01-15 10:00:00'),
(2, 'Spring Boot 实战', '构建企业级后端服务', '深入学习 Spring Boot 的核心特性，包括自动配置、数据访问、安全认证、微服务架构等。通过完整的项目实战，让你具备独立开发后端系统的能力。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 2, '李四', '', 69.00, 129.00, 2, 1, 12, 2341, 15.0, '2024-02-20 14:00:00'),
(3, 'Python 数据分析', '从入门到精通', '掌握 Python 数据分析的核心技能，包括 NumPy、Pandas、Matplotlib 等库的使用，以及数据清洗、可视化、机器学习入门等内容。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 3, '王五', '', 39.00, 79.00, 2, 5, 10, 892, 10.0, '2024-03-10 09:00:00'),
(4, 'React Native 跨平台开发', '一套代码构建多端应用', '学习 React Native 的核心概念和开发技巧，掌握组件开发、导航、状态管理、原生模块调用等技能，实现真正的跨平台开发。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 4, '赵六', '', 59.00, 109.00, 2, 3, 9, 567, 12.0, '2024-01-28 16:00:00'),
(5, 'TypeScript 完全手册', '类型安全的 JavaScript', '全面学习 TypeScript 的类型系统、高级特性和最佳实践，让你的代码更加健壮、可维护。适合有一定 JavaScript 基础的开发者。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 5, '孙七', '', 29.00, 59.00, 2, 2, 6, 1890, 6.0, '2024-04-05 11:00:00'),
(6, 'Docker 容器化实战', '容器技术从入门到实践', '学习 Docker 容器技术的核心概念，包括镜像构建、容器管理、Dockerfile 编写、Docker Compose 编排等内容。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 6, '周八', '', 35.00, 69.00, 2, 6, 7, 789, 7.5, '2024-02-10 10:00:00'),
(7, '算法与数据结构', '程序员必备核心技能', '系统学习常用的数据结构和算法，包括数组、链表、树、图、排序、查找等。通过大量练习题，提升你的编程能力和面试成功率。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 7, '吴九', '', 59.00, 99.00, 2, 1, 15, 3210, 20.0, '2024-01-05 09:00:00'),
(8, '代码整洁之道', '写出高质量代码的艺术', '学习代码整洁的原则和实践，包括命名规范、函数设计、类设计、错误处理等。让你的代码更加清晰、易读、易维护。', 'https://p3-passport.byteacctimg.com/img/user-avatar/3177199135720085~300x300.image', 8, '郑十', '', 25.00, 49.00, 2, 7, 5, 456, 4.0, '2024-03-25 14:00:00');

CREATE TABLE IF NOT EXISTS `ap_course_chapter` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `course_id` bigint NOT NULL COMMENT '所属课程',
    `title` varchar(200) NOT NULL COMMENT '小节标题',
    `sort_order` int DEFAULT 0 COMMENT '排序序号',
    `content` longtext COMMENT '小节正文（Markdown格式）',
    `word_count` int DEFAULT 0 COMMENT '字数统计',
    `is_free` tinyint DEFAULT 0 COMMENT '是否免费（0-付费, 1-免费）',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程小节表';

INSERT INTO `ap_course_chapter` (`id`, `course_id`, `title`, `sort_order`, `content`, `word_count`, `is_free`) VALUES
(1, 1, '第一章：Vue3 入门与环境搭建', 1, '# Vue3 入门与环境搭建\n\n欢迎来到 Vue3 的世界！本章节将带你了解 Vue3 的基本概念，并完成开发环境的搭建。\n\n## 1.1 Vue3 简介\n\nVue.js 是一款用于构建用户界面的渐进式 JavaScript 框架。Vue3 是 Vue.js 的最新版本，引入了许多新特性和改进。\n\n### 主要特性\n\n- **组合式 API**：提供更灵活的代码组织方式\n- **响应式系统重构**：使用 Proxy 替代 Object.defineProperty\n- **更好的 TypeScript 支持**：原生支持 TypeScript\n- **性能提升**：更快的渲染速度和更小的打包体积\n\n## 1.2 环境搭建\n\n### 安装 Node.js\n\n首先需要安装 Node.js，建议使用 LTS 版本。\n\n```bash\n# 检查 Node.js 版本\nnode -v\n\n# 检查 npm 版本\nnpm -v\n```\n\n### 创建 Vue3 项目\n\n使用 Vue CLI 创建项目：\n\n```bash\nnpm create vue@6.5.0 .\n```\n\n按照提示选择项目配置，完成后进入项目目录并安装依赖：\n\n```bash\ncd your-project\nnpm install\nnpm run dev\n```\n\n## 1.3 第一个 Vue3 应用\n\n创建一个简单的 Hello World 应用：\n\n```vue\n<template>\n  <div>{{ message }}</div>\n</template>\n\n<script setup>\nimport { ref } from 'vue'\n\nconst message = ref('Hello Vue3!')\n</script>\n```\n\n恭喜你！你已经成功创建了第一个 Vue3 应用。', 1500, 1),
(2, 1, '第二章：组合式 API 核心概念', 2, '# 组合式 API 核心概念\n\n组合式 API 是 Vue3 最重要的特性之一，它提供了一种更灵活的代码组织方式。\n\n## 2.1 setup 函数\n\nsetup 函数是组合式 API 的入口：\n\n```vue\n<script setup>\n// 在这里编写组合式 API 代码\n</script>\n```\n\n## 2.2 ref 和 reactive\n\n### ref\n\nref 用于创建响应式的基本类型数据：\n\n```javascript\nimport { ref }\n\nconst count = ref(0)\n\nconsole.log(count.value) // 0\ncount.value++\nconsole.log(count.value) // 1\n```\n\n### reactive\n\nreactive 用于创建响应式的对象：\n\n```javascript\nimport { reactive }\n\nconst state = reactive({\n  count: 0,\n  name: 'Vue3'\n})\n\nstate.count++\nconsole.log(state.count) // 1\n```\n\n## 2.3 computed\n\ncomputed 用于创建计算属性：\n\n```javascript\nimport { ref, computed }\n\nconst firstName = ref('张')\nconst lastName = ref('三')\n\nconst fullName = computed(() => {\n  return firstName.value + lastName.value\n})\n```', 1200, 1),
(3, 1, '第三章：响应式系统深入理解', 3, '# 响应式系统深入理解\n\nVue3 的响应式系统是其核心特性之一，理解它的工作原理对于编写高效的代码至关重要。\n\n## 3.1 Proxy 原理\n\nVue3 使用 ES6 的 Proxy 对象来实现响应式：\n\n```javascript\nconst target = { count: 0 }\nconst proxy = new Proxy(target, {\n  get(target, key) {\n    // 收集依赖\n    track(target, key)\n    return target[key]\n  },\n  set(target, key, value) {\n    target[key] = value\n    // 触发更新\n    trigger(target, key)\n    return true\n  }\n})\n```\n\n## 3.2 依赖收集与触发\n\n### track 函数\n\ntrack 函数负责收集依赖，将当前的 effect 添加到依赖集合中。\n\n### trigger 函数\n\ntrigger 函数负责触发更新，遍历依赖集合并执行所有 effect。\n\n## 3.3 effect 和 watch\n\n### effect\n\neffect 用于创建副作用函数：\n\n```javascript\nimport { ref, effect }\n\nconst count = ref(0)\n\neffect(() => {\n  console.log(`count changed: ${count.value}`)\n})\n\ncount.value++ // 输出: count changed: 1\n```\n\n### watch\n\nwatch 用于监听响应式数据的变化：\n\n```javascript\nimport { ref, watch }\n\nconst count = ref(0)\n\nwatch(count, (newValue, oldValue) => {\n  console.log(`count: ${oldValue} -> ${newValue}`)\n})\n```', 1400, 0),
(4, 1, '第四章：组件通信', 4, '# 组件通信\n\n在 Vue3 中，组件之间的通信方式有多种，每种方式适用于不同的场景。\n\n## 4.1 Props\n\nProps 是父组件向子组件传递数据的主要方式：\n\n```vue\n<!-- Parent.vue -->\n<Child :message=\"message\" />\n\n<!-- Child.vue -->\n<script setup>\ndefineProps({\n  message: String\n})\n</script>\n```\n\n## 4.2 Emits\n\nEmits 是子组件向父组件发送事件的方式：\n\n```vue\n<!-- Child.vue -->\n<button @click=\"$emit('update', 'new value')\">\n  更新\n</button>\n\n<script setup>\ndefineEmits(['update'])\n</script>\n\n<!-- Parent.vue -->\n<Child @update=\"handleUpdate\" />\n```\n\n## 4.3 provide 和 inject\n\nprovide 和 inject 用于跨层级组件通信：\n\n```vue\n<!-- Parent.vue -->\n<script setup>\nimport { provide }\n\nprovide('theme', 'dark')\n</script>\n\n<!-- GrandChild.vue -->\n<script setup>\nimport { inject }\n\nconst theme = inject('theme')\n</script>\n```', 1100, 0),
(5, 1, '第五章：路由与状态管理', 5, '# 路由与状态管理\n\n在大型应用中，路由和状态管理是必不可少的。\n\n## 5.1 Vue Router\n\nVue Router 是 Vue 官方的路由管理器：\n\n```javascript\nimport { createRouter, createWebHistory }\nimport Home from './views/Home.vue'\nimport About from './views/About.vue'\n\nconst router = createRouter({\n  history: createWebHistory(),\n  routes: [\n    { path: '/', component: Home },\n    { path: '/about', component: About }\n  ]\n})\n```\n\n## 5.2 Pinia\n\nPinia 是 Vue 官方推荐的状态管理库：\n\n```javascript\nimport { defineStore } from 'pinia'\n\nexport const useCounterStore = defineStore('counter', {\n  state: () => ({ count: 0 }),\n  actions: {\n    increment() {\n      this.count++\n    }\n  },\n  getters: {\n    double: (state) => state.count * 2\n  }\n})\n```', 1000, 0),
(6, 1, '第六章：组合式函数', 6, '# 组合式函数\n\n组合式函数是 Vue3 中组织逻辑的一种方式，它允许你将可复用的逻辑提取到函数中。\n\n## 6.1 什么是组合式函数\n\n组合式函数是一个使用 Vue 组合式 API 的函数，它返回一组响应式状态和方法。\n\n## 6.2 创建组合式函数\n\n```javascript\nimport { ref, onMounted, onUnmounted }\n\nexport function useMousePosition() {\n  const x = ref(0)\n  const y = ref(0)\n\n  function update(event) {\n    x.value = event.pageX\n    y.value = event.pageY\n  }\n\n  onMounted(() => {\n    window.addEventListener('mousemove', update)\n  })\n\n  onUnmounted(() => {\n    window.removeEventListener('mousemove', update)\n  })\n\n  return { x, y }\n}\n```\n\n## 6.3 使用组合式函数\n\n```vue\n<script setup>\nimport { useMousePosition }\n\nconst { x, y } = useMousePosition()\n</script>\n\n<template>\n  Mouse position: {{ x }}, {{ y }}\n</template>\n```', 900, 0),
(7, 1, '第七章：实战项目开发', 7, '# 实战项目开发\n\n通过一个完整的实战项目，将所学知识应用到实际开发中。\n\n## 7.1 项目规划\n\n在开始开发之前，需要进行项目规划：\n\n1. 需求分析\n2. 技术选型\n3. 架构设计\n4. 任务分解\n\n## 7.2 项目结构\n\n```\nsrc/\n├── components/\n│   ├── Header.vue\n│   ├── Sidebar.vue\n│   └── Footer.vue\n├── views/\n│   ├── Home.vue\n│   ├── About.vue\n│   └── Contact.vue\n├── stores/\n│   └── index.js\n├── router/\n│   └── index.js\n├── App.vue\n└── main.js\n```\n\n## 7.3 开发流程\n\n按照以下流程进行开发：\n\n1. 创建项目基础结构\n2. 实现组件\n3. 配置路由\n4. 实现状态管理\n5. 测试与优化', 800, 0),
(8, 1, '第八章：性能优化与最佳实践', 8, '# 性能优化与最佳实践\n\n学习 Vue3 的性能优化技巧和最佳实践，让你的应用更加高效。\n\n## 8.1 性能优化技巧\n\n### 使用 v-memo\n\nv-memo 可以缓存模板片段，避免不必要的重新渲染：\n\n```vue\n<div v-memo=\"[item.id]\">\n  {{ item.name }}\n</div>\n```\n\n### 使用 shallowRef 和 shallowReactive\n\n对于不需要深度响应式的数据，使用 shallowRef 和 shallowReactive：\n\n```javascript\nimport { shallowRef }\n\nconst data = shallowRef({ count: 0 })\n```\n\n## 8.2 最佳实践\n\n1. 使用组合式 API 组织代码\n2. 合理使用计算属性\n3. 避免在模板中使用复杂表达式\n4. 使用 v-for 时添加 key\n5. 按需引入组件', 700, 0);

CREATE TABLE IF NOT EXISTS `ap_course_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_no` varchar(50) NOT NULL UNIQUE COMMENT '对外订单号',
    `user_id` int NOT NULL COMMENT '下单用户',
    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
    `status` tinyint DEFAULT 0 COMMENT '状态（0-待支付, 1-已支付, 2-已取消, 3-已退款）',
    `pay_method` varchar(20) DEFAULT '' COMMENT '支付方式',
    `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单表';

CREATE TABLE IF NOT EXISTS `ap_course_order_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL COMMENT '所属订单',
    `course_id` bigint NOT NULL COMMENT '购买的课程',
    `price` decimal(10,2) NOT NULL COMMENT '购买时的课程单价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程订单明细表';

CREATE TABLE IF NOT EXISTS `ap_user_course` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `course_id` bigint NOT NULL COMMENT '课程ID',
    `order_id` bigint DEFAULT NULL COMMENT '关联订单',
    `purchased_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
    `is_active` tinyint DEFAULT 1 COMMENT '权限是否有效',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程购买记录表';

CREATE TABLE IF NOT EXISTS `ap_course_reading_progress` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` int NOT NULL COMMENT '用户ID',
    `chapter_id` bigint NOT NULL COMMENT '小节ID',
    `progress` float DEFAULT 0.0 COMMENT '阅读进度百分比',
    `last_read_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_chapter` (`user_id`, `chapter_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读进度表';