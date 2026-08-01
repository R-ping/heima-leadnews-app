# CHANGELOG

## 2026-08-01 — 后端服务架构重构

### 架构变更

#### ScheduleApplication → 合并入 Article 服务
- **BREAKING**: 删除 `heima-leadnews-schedule` 独立微服务模块
- 将 TaskService、TaskinfoMapper、TaskDelayConsumer 等全部迁移至 article 模块的 `com.heima.article.schedule` 包
- 使用 Redisson 延迟队列（`RBlockingQueue` + `RDelayedQueue`）替代 RabbitMQ 延迟插件，消除外部 RabbitMQ 依赖
- 移除 `IScheduleClient` Feign 接口，远程调用改为本地 Service 方法调用
- 新增 `schedule.sql` DDL 文件，用于在 `leadnews_article` 库中创建 `taskinfo` 和 `taskinfo_logs` 表

#### BehaviorApplication → 合并入 Article 服务并重构
- **BREAKING**: 删除 `heima-leadnews-behavior` 独立微服务模块
- 将 LikesBehavior、ReadBehavior、UnlikesBehavior 的 Controller 和 Service 全部迁移至 article 模块的 `com.heima.article.behavior` 包
- **移除 Redis 缓存用户行为逻辑**（`LIKE_BEHAVIOR`、`READ_BEHAVIOR`、`UN_LIKE_BEHAVIOR`），改为直接数据库持久化
  - 点赞/取消点赞：原子更新 `ap_article.likes` 字段，并记录到 `ap_user_action_log` 表
  - 阅读行为：原子更新 `ap_article.views` 字段，并记录到 `ap_browse_history` 表
  - 不喜欢行为：记录到 `ap_user_action_log` 表
- 移除 AOP 切面（`ReadLikeUnLikeAspect`）和 `@UserBehavior` 注解，不再发送 Kafka 消息

#### 移除 Kafka 技术栈
- 删除 `KafkaStreamConfig.java`（Kafka Streams 配置）
- 删除 `HotArticleStreamHandler.java`（Kafka Streams 聚合处理器）
- 删除 `ArticleIncrHandleListener.java`（Kafka 消费者）
- 删除 `ArticleIsDownListener.java`（Kafka 消费者）
- 从 `pom.xml` 移除 `spring-kafka`、`kafka-streams`、`kafka-clients` 依赖
- 从 `application.yml` 移除 Kafka 配置
- 行为热度更新改为直接调用 `ApArticleService.updateScoreByBehavior()`，无需消息队列中转

#### 沸点服务决策
- 沸点（Pins）保持现状，不拆分为独立微服务。与文章共享基础设施，符合"去微服务化"趋势

### 数据库变更
- 新增 `taskinfo` 和 `taskinfo_logs` 表（`leadnews_article` 库），参考 `schedule.sql`

### 基础架构变更
- 从 `heima-leadnews-service/pom.xml` 中移除 `heima-leadnews-schedule` 和 `heima-leadnews-behavior` 模块
- 网关路由中移除 `/schedule/` 和 `/behavior/` 路由
- 减少 2 个微服务实例、消除 Kafka 和 RabbitMQ 外部依赖

### 变更文件列表

#### 删除（模块）
- `heima-leadnews-service/heima-leadnews-schedule/`（完整模块）
- `heima-leadnews-service/heima-leadnews-behavior/`（完整模块）

#### 删除（Feign接口）
- `heima-leadnews-feign-api/.../schedule/IScheduleClient.java`

#### 删除（Kafka相关）
- `heima-leadnews-article/.../config/KafkaStreamConfig.java`
- `heima-leadnews-article/.../stream/HotArticleStreamHandler.java`
- `heima-leadnews-article/.../listener/ArticleIncrHandleListener.java`
- `heima-leadnews-article/.../listener/ArticleIsDownListener.java`

#### 新增（article模块）
- `heima-leadnews-article/.../schedule/service/TaskService.java`
- `heima-leadnews-article/.../schedule/service/impl/TaskServiceImpl.java`
- `heima-leadnews-article/.../schedule/listener/TaskDelayConsumer.java`
- `heima-leadnews-article/.../schedule/mapper/TaskinfoLogsMapper.java`
- `heima-leadnews-article/.../schedule/mapper/TaskinfoMapper.java`
- `heima-leadnews-article/.../behavior/controller/v1/ApLikesBehaviorController.java`
- `heima-leadnews-article/.../behavior/controller/v1/ApReadBehaviorController.java`
- `heima-leadnews-article/.../behavior/controller/v1/ApUnlikesBehaviorController.java`
- `heima-leadnews-article/.../behavior/service/ApLikesBehaviorService.java`
- `heima-leadnews-article/.../behavior/service/ApReadBehaviorService.java`
- `heima-leadnews-article/.../behavior/service/ApUnlikesBehaviorService.java`
- `heima-leadnews-article/.../behavior/service/impl/ApLikesBehaviorServiceImpl.java`
- `heima-leadnews-article/.../behavior/service/impl/ApReadBehaviorServiceImpl.java`
- `heima-leadnews-article/.../behavior/service/impl/ApUnlikesBehaviorServiceImpl.java`
- `heima-leadnews-article/.../config/RedissonConfig.java`
- `heima-leadnews-article/src/main/resources/schedule.sql`
- `heima-leadnews-article/src/main/resources/mapper/TaskinfoMapper.xml`

#### 修改
- `heima-leadnews-article/.../ArticleApplication.java`（MapperScan 增加 schedule 包）
- `heima-leadnews-article/.../service/ApArticleService.java`（新增 updateScoreByBehavior 方法）
- `heima-leadnews-article/.../service/impl/ApArticleServiceImpl.java`（实现 updateScoreByBehavior）
- `heima-leadnews-article/.../service/impl/ArticleTaskServiceImpl.java`（Feign 改为本地调用）
- `heima-leadnews-article/pom.xml`（移除 Kafka 依赖，保留 Redisson）
- `heima-leadnews-article/src/main/resources/application.yml`（移除 Kafka 配置）
- `heima-leadnews-service/pom.xml`（移除 schedule 和 behavior 模块引用）
- `heima-leadnews-gateway/.../application-gateway.yml`（移除 schedule 和 behavior 路由）
- `heima-leadnews-common/.../constants/BehaviorConstants.java`（添加废弃注释）

### 新增功能

#### 课程微服务 (heima-leadnews-course)
- 新增 `heima-leadnews-course` 微服务模块（端口 51803），独立处理课程交易、营销、结算、审核逻辑
- 网关路由：`/course/**` → 课程微服务

#### 课程创作与章节管理
- 课程 CRUD 接口（创建、更新、列表、详情、软删除），支持状态管理（草稿/待审核/已上架/已下架）
- 章节 CRUD 接口，支持 Markdown 内容编辑、试读章节设置、排序管理
- 前端创作者中心课程管理页面（列表/新建/编辑），含状态筛选和搜索功能
- 前端章节编辑器，支持 Markdown 编辑与实时预览

#### 课程购买与支付
- 订单创建接口，支持从数据库获取真实课程价格
- 支付宝沙箱环境支付集成，含支付页面生成、异步通知回调处理
- 支付后处理：更新折扣码使用次数、课程学习人数、用户课程权限
- 前端课程详情页：购买流程、折扣码实时验证、折后价格展示
- 前端课程阅读页：章节切换加载完整内容、XSS 安全处理
- 前端"我的课程"页面：展示已购买课程列表

#### 课程营销活动
- 折扣码创建/列表/停用/验证接口，支持固定金额和百分比两种折扣类型
- 前端折扣码管理页面，支持创建、查看、停用操作

#### 收入结算
- 月度结算计算逻辑（作者分成 70%，平台分成 30%）
- 结算明细查询接口
- 前端收入结算看板页面，展示结算列表与明细

#### 课程权限控制
- 基于逐力值等级（Lv5+）控制课程创作权限
- 前端导航栏根据权限显示"创作者中心"入口
- 路由守卫控制创作者中心访问权限

### 修复问题

- **沸点接口 404 错误**：修复网关 `StripPrefix= 1` 空格导致前缀未正确剥离的问题，以及前端接口路径重复 `/article/` 前缀问题
- **444 错误后登录状态丢失**：`article_request.js` 实现请求队列机制，修复并发刷新 token 时登录态丢失
- **沸点页/课程页顶栏不统一**：将沸点页和课程页路由纳入 Layout 组件，统一复用桌面端顶栏
- **课程价格硬编码**：`OrderServiceImpl` 改为从数据库获取真实课程价格
- **支付后处理不完整**：补充折扣码使用次数更新、课程销售数据更新、用户课程权限添加
- **API 返回格式不统一**：课程列表、订单列表等接口统一使用 `data.list` 和 `data.total` 格式
- **课程服务编译错误**：修复 `javax.servlet` → `jakarta.servlet` 适配 Spring Boot 3.x，修复 `ApUser` 导入路径错误

### 数据库变更

#### 新增表
- `ap_course_order`：课程订单表
- `ap_course_discount`：课程折扣码表
- `ap_course_review`：编辑审核记录表
- `ap_course_invitation`：邀请编辑表
- `ap_course_settlement`：收入结算表
- `ap_course_chapter_comment`：章节评论表

#### 扩展现有表
- `ap_course`：新增 `is_deleted`、`version`、`sales_count`、`total_revenue` 字段
- `ap_course_chapter`：新增 `status`、`estimated_minutes`、`comment_count` 字段

### 基础架构变更

- 新增 `heima-leadnews-course` 模块到 `heima-leadnews-service/pom.xml`
- Vite 配置添加 `/course` 代理路由
- 创作者中心菜单和路由更新（折扣码管理、收入结算入口）
- 课程详情页/阅读页移除静态 Mock 数据，全部改为 API 调用
- 删除 `src/pages/course/mockData.js`

### 变更文件列表

#### 后端（新增）
- `heima-leadnews-service/heima-leadnews-course/`（完整微服务模块）
- `heima-leadnews-model/.../dtos/CourseDto.java`
- `heima-leadnews-model/.../dtos/ChapterDto.java`
- `heima-leadnews-model/.../dtos/ChapterSortDto.java`
- `heima-leadnews-model/.../dtos/CourseDiscountDto.java`
- `heima-leadnews-model/.../pojos/ApCourseDiscount.java`
- `heima-leadnews-model/.../pojos/ApCourseOrder.java`
- `heima-leadnews-model/.../pojos/ApCourseReview.java`
- `heima-leadnews-model/.../pojos/ApCourseInvitation.java`
- `heima-leadnews-model/.../pojos/ApCourseSettlement.java`
- `heima-leadnews-model/.../pojos/ApCourseChapterComment.java`
- `heima-leadnews-article/.../controller/v1/CourseChapterController.java`
- `heima-leadnews-article/.../service/ApCourseChapterService.java`
- `heima-leadnews-article/.../service/impl/ApCourseChapterServiceImpl.java`
- `heima-leadnews-gateway/.../dto/`（网关新增 DTO）
- `sql/init_course_extended.sql`

#### 前端（新增）
- `src/apis/course.js`
- `src/apis/circle.js`
- `src/apis/pins.js`
- `src/pages/creator/course/list.vue`
- `src/pages/creator/course/edit.vue`
- `src/pages/creator/course/discount.vue`
- `src/pages/creator/course/settlement.vue`
- `src/pages/user/courses/index.vue`
- `src/pages/user/courses/components/CourseListItem.vue`
- `src/pages/user/courses/components/CourseGridCard.vue`

#### 前端（修改）
- `src/pages/course/detail.vue`（API 调用、折扣码验证、购买流程）
- `src/pages/course/read.vue`（API 调用、章节切换、进度更新）
- `src/pages/course/index.vue`（动态数据）
- `src/pages/pins/index.vue`（顶栏适配）
- `src/pages/pins/circles.vue`（顶栏适配）
- `src/pages/creator/constants/menus.js`（菜单更新）
- `src/routers/creator.js`（路由更新）
- `src/common/request.js`（请求拦截器修复）
- `src/common/wemedia_request.js`（请求拦截器修复）
- `src/common/conf.js`（配置更新）
- `src/components/bars/home_bar.vue`（顶栏权限控制）
- `vite.config.js`（代理配置）
- `src/apis/home/api.js`
- `src/apis/topic.js`
- `src/apis/user.js`