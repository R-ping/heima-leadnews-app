<template>
  <div class="course-detail-page">
    <div class="detail-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="course-cover-wrapper">
          <img :src="course.coverImage || '/static/images/avatar_head_1.png'" class="course-cover" />
          <div class="course-badge" v-if="course.price === 0">免费</div>
          <div class="course-badge discount" v-else-if="course.originalPrice > course.price">
            {{ Math.round((1 - course.price / course.originalPrice) * 100) }}%OFF
          </div>
        </div>
        <div class="course-meta">
          <div class="course-title">{{ course.title }}</div>
          <div class="course-subtitle">{{ course.subtitle }}</div>
          <div class="course-author">
            <img :src="course.authorAvatar || '/static/images/avatar_head_1.png'" class="author-avatar" />
            <div class="author-info">
              <div class="author-name">{{ course.authorName }}</div>
              <div class="author-label">作者</div>
            </div>
          </div>
          <div class="course-stats-row">
            <span class="stat-item">
              <span class="stat-value">{{ course.chapterCount }}</span>
              <span class="stat-label">小节</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-value">{{ course.studyCount }}</span>
              <span class="stat-label">人已购</span>
            </span>
            <span class="stat-divider"></span>
            <span class="stat-item">
              <span class="stat-value">{{ course.estimatedHours }}h</span>
              <span class="stat-label">预计阅读</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="detail-body">
      <div class="detail-left">
        <div class="section">
          <div class="section-title">课程简介</div>
          <div class="section-content">
            {{ course.description }}
          </div>
        </div>

        <div class="section">
          <div class="section-title">课程目录</div>
          <div class="chapter-list">
            <div 
              v-for="chapter in chapters" 
              :key="chapter.id"
              class="chapter-item"
              :class="{ locked: !chapter.isFree && !isPurchased }"
              @click="handleChapterClick(chapter)"
            >
              <div class="chapter-left">
                <span class="chapter-number">{{ chapter.sortOrder }}</span>
                <span class="chapter-title">{{ chapter.title }}</span>
              </div>
              <div class="chapter-right">
                <span class="free-tag" v-if="chapter.isFree">免费</span>
                <span class="lock-icon" v-else-if="!isPurchased">&#xf023;</span>
                <span class="word-count">{{ chapter.wordCount }}字</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-right">
        <div class="purchase-card">
          <div class="price-section">
            <div class="price-row">
              <span class="current-price">¥{{ course.price }}</span>
              <span class="original-price" v-if="course.originalPrice > course.price">¥{{ course.originalPrice }}</span>
            </div>
            <div class="save-text" v-if="course.originalPrice > course.price">
              立省 ¥{{ (course.originalPrice - course.price).toFixed(0) }}
            </div>
          </div>

          <div class="action-buttons">
            <button 
              class="buy-btn"
              v-if="!isPurchased"
              @click="handleBuy"
            >
              立即购买
            </button>
            <button 
              class="read-btn"
              v-else
              @click="handleRead"
            >
              继续阅读
            </button>
          </div>

          <div class="purchase-info">
            <div class="info-item">
              <span class="info-icon">&#xf075;</span>
              <span class="info-text">支持7天无理由退款</span>
            </div>
            <div class="info-item">
              <span class="info-icon">&#xf02e;</span>
              <span class="info-text">永久有效，随时回看</span>
            </div>
            <div class="info-item">
              <span class="info-icon">&#xf121;</span>
              <span class="info-text">支持多端阅读</span>
            </div>
          </div>

          <div class="author-card">
            <img :src="course.authorAvatar || '/static/images/avatar_head_1.png'" class="author-avatar-lg" />
            <div class="author-info-lg">
              <div class="author-name-lg">{{ course.authorName }}</div>
              <div class="author-course-count">共{{ getAuthorCourseCount() }}门课程</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="purchase-modal" v-if="showPurchaseModal" @click="closePurchaseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-title">确认购买</div>
          <span class="modal-close" @click="closePurchaseModal">&#10005;</span>
        </div>
        <div class="modal-body">
          <div class="order-info">
            <div class="order-item">
              <span class="order-label">课程名称</span>
              <span class="order-value">{{ course.title }}</span>
            </div>
            <div class="order-item">
              <span class="order-label">课程作者</span>
              <span class="order-value">{{ course.authorName }}</span>
            </div>
            <div class="order-item">
              <span class="order-label">课程价格</span>
              <span class="order-value price">¥{{ course.price }}</span>
            </div>
            <div class="order-total">
              <span class="total-label">应付金额</span>
              <span class="total-value">¥{{ course.price }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closePurchaseModal">取消</button>
          <button class="confirm-btn" @click="confirmPurchase">确认支付</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { toast } from "@/utils/toast"

export default {
  name: 'CourseDetailPage',
  data() {
    return {
      course: {},
      chapters: [],
      isPurchased: false,
      showPurchaseModal: false,
      orderNo: ''
    }
  },
  mounted() {
    this.loadCourseDetail()
    this.checkPurchaseStatus()
  },
  methods: {
    loadCourseDetail() {
      const courseId = parseInt(this.$route.params.id)

      const mockCourses = [
        {
          id: 1,
          title: 'Vue3 完全指南',
          subtitle: '从零开始掌握 Vue3 组合式 API',
          description: '本课程将带你从零开始学习 Vue3 的核心概念，包括组合式 API、响应式系统、组件通信等。通过大量实战案例，帮助你快速掌握 Vue3 开发技能。',
          coverImage: '/static/images/avatar_head_1.png',
          authorId: 1,
          authorName: '张三',
          authorAvatar: '',
          price: 49.00,
          originalPrice: 99.00,
          categoryId: 2,
          chapterCount: 8,
          studyCount: 1256,
          estimatedHours: 8.5
        },
        {
          id: 2,
          title: 'Spring Boot 实战',
          subtitle: '构建企业级后端服务',
          description: '深入学习 Spring Boot 的核心特性，包括自动配置、数据访问、安全认证、微服务架构等。通过完整的项目实战，让你具备独立开发后端系统的能力。',
          coverImage: '/static/images/avatar_head_2.png',
          authorId: 2,
          authorName: '李四',
          authorAvatar: '',
          price: 69.00,
          originalPrice: 129.00,
          categoryId: 1,
          chapterCount: 12,
          studyCount: 2341,
          estimatedHours: 15.0
        },
        {
          id: 3,
          title: 'Python 数据分析',
          subtitle: '从入门到精通',
          description: '掌握 Python 数据分析的核心技能，包括 NumPy、Pandas、Matplotlib 等库的使用，以及数据清洗、可视化、机器学习入门等内容。',
          coverImage: '/static/images/avatar_head_3.png',
          authorId: 3,
          authorName: '王五',
          authorAvatar: '',
          price: 39.00,
          originalPrice: 79.00,
          categoryId: 5,
          chapterCount: 10,
          studyCount: 892,
          estimatedHours: 10.0
        },
        {
          id: 4,
          title: 'React Native 跨平台开发',
          subtitle: '一套代码构建多端应用',
          description: '学习 React Native 的核心概念和开发技巧，掌握组件开发、导航、状态管理、原生模块调用等技能，实现真正的跨平台开发。',
          coverImage: '/static/images/avatar_head_4.png',
          authorId: 4,
          authorName: '赵六',
          authorAvatar: '',
          price: 59.00,
          originalPrice: 109.00,
          categoryId: 3,
          chapterCount: 9,
          studyCount: 567,
          estimatedHours: 12.0
        },
        {
          id: 5,
          title: 'TypeScript 完全手册',
          subtitle: '类型安全的 JavaScript',
          description: '全面学习 TypeScript 的类型系统、高级特性和最佳实践，让你的代码更加健壮、可维护。适合有一定 JavaScript 基础的开发者。',
          coverImage: '/static/images/avatar_head_5.png',
          authorId: 5,
          authorName: '孙七',
          authorAvatar: '',
          price: 29.00,
          originalPrice: 59.00,
          categoryId: 2,
          chapterCount: 6,
          studyCount: 1890,
          estimatedHours: 6.0
        },
        {
          id: 6,
          title: 'Docker 容器化实战',
          subtitle: '容器技术从入门到实践',
          description: '学习 Docker 容器技术的核心概念，包括镜像构建、容器管理、Dockerfile 编写、Docker Compose 编排等内容。',
          coverImage: '/static/images/avatar_head_6.png',
          authorId: 6,
          authorName: '周八',
          authorAvatar: '',
          price: 35.00,
          originalPrice: 69.00,
          categoryId: 6,
          chapterCount: 7,
          studyCount: 789,
          estimatedHours: 7.5
        },
        {
          id: 7,
          title: '算法与数据结构',
          subtitle: '程序员必备核心技能',
          description: '系统学习常用的数据结构和算法，包括数组、链表、树、图、排序、查找等。通过大量练习题，提升你的编程能力和面试成功率。',
          coverImage: '/static/images/avatar_head_7.png',
          authorId: 7,
          authorName: '吴九',
          authorAvatar: '',
          price: 59.00,
          originalPrice: 99.00,
          categoryId: 1,
          chapterCount: 15,
          studyCount: 3210,
          estimatedHours: 20.0
        },
        {
          id: 8,
          title: '代码整洁之道',
          subtitle: '写出高质量代码的艺术',
          description: '学习代码整洁的原则和实践，包括命名规范、函数设计、类设计、错误处理等。让你的代码更加清晰、易读、易维护。',
          coverImage: '/static/images/avatar_head_8.png',
          authorId: 8,
          authorName: '郑十',
          authorAvatar: '',
          price: 25.00,
          originalPrice: 49.00,
          categoryId: 7,
          chapterCount: 5,
          studyCount: 456,
          estimatedHours: 4.0
        }
      ]

      this.course = mockCourses.find(c => c.id === courseId) || mockCourses[0]

      this.chapters = [
        { id: 1, courseId: this.course.id, title: '第一章：Vue3 入门与环境搭建', sortOrder: 1, wordCount: 1500, isFree: 1 },
        { id: 2, courseId: this.course.id, title: '第二章：组合式 API 核心概念', sortOrder: 2, wordCount: 1200, isFree: 1 },
        { id: 3, courseId: this.course.id, title: '第三章：响应式系统深入理解', sortOrder: 3, wordCount: 1400, isFree: 0 },
        { id: 4, courseId: this.course.id, title: '第四章：组件通信', sortOrder: 4, wordCount: 1100, isFree: 0 },
        { id: 5, courseId: this.course.id, title: '第五章：路由与状态管理', sortOrder: 5, wordCount: 1000, isFree: 0 },
        { id: 6, courseId: this.course.id, title: '第六章：组合式函数', sortOrder: 6, wordCount: 900, isFree: 0 },
        { id: 7, courseId: this.course.id, title: '第七章：实战项目开发', sortOrder: 7, wordCount: 800, isFree: 0 },
        { id: 8, courseId: this.course.id, title: '第八章：性能优化与最佳实践', sortOrder: 8, wordCount: 700, isFree: 0 }
      ]
    },
    checkPurchaseStatus() {
      const purchasedCourses = JSON.parse(localStorage.getItem('purchasedCourses') || '[]')
      this.isPurchased = purchasedCourses.includes(this.course.id)
    },
    getAuthorCourseCount() {
      return 3
    },
    handleChapterClick(chapter) {
      if (!chapter.isFree && !this.isPurchased) {
        toast('该章节需要购买后才能阅读', 2)
        return
      }
      this.$router.push(`/course/read/${chapter.id}`)
    },
    handleBuy() {
      if (!this.$store.getters.isLoggedIn) {
        this.$store.dispatch('showLogin')
        return
      }
      this.showPurchaseModal = true
    },
    closePurchaseModal() {
      this.showPurchaseModal = false
    },
    confirmPurchase() {
      this.showPurchaseModal = false
      
      toast('支付中...', 1)
      
      setTimeout(() => {
        this.orderNo = 'ORD' + Date.now()
        
        let purchasedCourses = JSON.parse(localStorage.getItem('purchasedCourses') || '[]')
        if (!purchasedCourses.includes(this.course.id)) {
          purchasedCourses.push(this.course.id)
          localStorage.setItem('purchasedCourses', JSON.stringify(purchasedCourses))
        }
        
        this.isPurchased = true
        toast('购买成功！', 2)
        
        setTimeout(() => {
          const firstChapter = this.chapters[0]
          if (firstChapter) {
            this.$router.push(`/course/read/${firstChapter.id}`)
          }
        }, 1500)
      }, 1000)
    },
    handleRead() {
      const firstChapter = this.chapters[0]
      if (firstChapter) {
        this.$router.push(`/course/read/${firstChapter.id}`)
      }
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.course-detail-page {
  min-height: 100vh;
  background-color: #f4f5f7;
}

.detail-header {
  position: relative;
  padding-bottom: 32px;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, #1E80FF 0%, #4A90FF 100%);
}

.header-content {
  position: relative;
  display: flex;
  gap: 20px;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.course-cover-wrapper {
  position: relative;
  flex-shrink: 0;
}

.course-cover {
  width: 280px;
  height: 168px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.course-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  padding: 4px 12px;
  background-color: #F53F3F;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
}

.course-badge.discount {
  background-color: #FF7D00;
}

.course-meta {
  flex: 1;
  color: #fff;
}

.course-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.course-subtitle {
  font-size: 16px;
  opacity: 0.85;
  margin-bottom: 16px;
}

.course-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.5);
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
}

.author-label {
  font-size: 12px;
  opacity: 0.7;
}

.course-stats-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
}

.stat-label {
  font-size: 12px;
  opacity: 0.7;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background-color: rgba(255,255,255,0.3);
}

.detail-body {
  display: flex;
  gap: 24px;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.detail-left {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
}

.detail-right {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #252933;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f1f5;
}

.section-content {
  font-size: 15px;
  color: #515767;
  line-height: 1.8;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chapter-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chapter-item:hover {
  background-color: #f5f7fa;
}

.chapter-item.locked {
  opacity: 0.6;
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.chapter-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #8a919f;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  flex-shrink: 0;
}

.chapter-title {
  font-size: 14px;
  color: #252933;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chapter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.free-tag {
  padding: 2px 8px;
  background-color: #EAF2FF;
  color: #1E80FF;
  font-size: 12px;
  border-radius: 4px;
}

.lock-icon {
  font-family: fontawesome;
  font-size: 14px;
  color: #c0c4cc;
}

.word-count {
  font-size: 13px;
  color: #8a919f;
}

.purchase-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}

.price-section {
  margin-bottom: 16px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: #F53F3F;
}

.original-price {
  font-size: 16px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.save-text {
  font-size: 13px;
  color: #F53F3F;
  margin-top: 4px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.buy-btn {
  padding: 12px;
  background-color: #F53F3F;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.buy-btn:hover {
  background-color: #E53935;
}

.read-btn {
  padding: 12px;
  background-color: #1E80FF;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.read-btn:hover {
  background-color: #1a7de8;
}

.purchase-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f1f5;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-icon {
  font-family: fontawesome;
  font-size: 14px;
  color: #c0c4cc;
}

.info-text {
  font-size: 13px;
  color: #8a919f;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar-lg {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.author-info-lg {
  display: flex;
  flex-direction: column;
}

.author-name-lg {
  font-size: 14px;
  font-weight: 500;
  color: #252933;
}

.author-course-count {
  font-size: 12px;
  color: #8a919f;
}

.purchase-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 500;
}

.modal-content {
  width: 90%;
  max-width: 420px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f1f5;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #252933;
}

.modal-close {
  font-size: 20px;
  color: #c0c4cc;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-label {
  font-size: 14px;
  color: #8a919f;
}

.order-value {
  font-size: 14px;
  color: #252933;
}

.order-value.price {
  color: #F53F3F;
  font-weight: 600;
}

.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  margin-top: 12px;
  border-top: 1px solid #f0f1f5;
}

.total-label {
  font-size: 16px;
  color: #252933;
  font-weight: 500;
}

.total-value {
  font-size: 24px;
  color: #F53F3F;
  font-weight: 700;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f1f5;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background-color: #f4f5f7;
  color: #515767;
  font-size: 16px;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.confirm-btn {
  flex: 1;
  padding: 12px;
  background-color: #F53F3F;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

@media screen and (max-width: 768px) {
  .header-content {
    flex-direction: column;
  }

  .course-cover {
    width: 100%;
    height: auto;
    max-height: 200px;
  }

  .detail-body {
    flex-direction: column;
  }

  .detail-right {
    width: 100%;
    position: static;
  }
}
</style>