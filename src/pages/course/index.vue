<template>
  <div class="course-page">
    <div class="course-header">
      <div class="header-title">课程</div>
      <div class="header-subtitle">发现优质课程，提升技术能力</div>
    </div>

    <div class="category-tabs">
      <div 
        v-for="cat in categories" 
        :key="cat.id"
        class="category-tab"
        :class="{ active: currentCategory === cat.id }"
        @click="selectCategory(cat.id)"
      >
        {{ cat.name }}
      </div>
    </div>

    <div class="filter-bar">
      <div 
        v-for="filter in filters" 
        :key="filter.id"
        class="filter-item"
        :class="{ active: currentFilter === filter.id }"
        @click="selectFilter(filter.id)"
      >
        {{ filter.name }}
      </div>
    </div>

    <div class="course-list">
      <div 
        v-for="course in courseList" 
        :key="course.id"
        class="course-card"
        @click="goToDetail(course.id)"
      >
        <div class="course-cover">
          <img :src="course.coverImage || '/static/images/avatar_head_1.png'" alt="课程封面" />
          <div class="course-category">{{ getCategoryName(course.categoryId) }}</div>
        </div>
        <div class="course-info">
          <div class="course-title">{{ course.title }}</div>
          <div class="course-subtitle">{{ course.subtitle }}</div>
          <div class="course-author">
            <img :src="course.authorAvatar || '/static/images/avatar_head_1.png'" class="author-avatar" />
            <span class="author-name">{{ course.authorName }}</span>
          </div>
          <div class="course-stats">
            <span class="stat-item">
              <span class="stat-icon">&#xf02d;</span>
              <span class="stat-text">{{ course.chapterCount }}节</span>
            </span>
            <span class="stat-item">
              <span class="stat-icon">&#xf0c0;</span>
              <span class="stat-text">{{ course.studyCount }}人学习</span>
            </span>
            <span class="stat-item">
              <span class="stat-icon">&#xf017;</span>
              <span class="stat-text">{{ course.estimatedHours }}h</span>
            </span>
          </div>
          <div class="course-price">
            <span class="current-price">¥{{ course.price }}</span>
            <span class="original-price" v-if="course.originalPrice > course.price">¥{{ course.originalPrice }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="loading-more" v-if="loading">
      <span class="loading-spinner"></span>
      <span class="loading-text">加载中...</span>
    </div>

    <div class="no-more" v-if="!loading && noMore">
      <span>— 没有更多了 —</span>
    </div>
  </div>
</template>

<script>
import { toast } from "@/utils/toast"

export default {
  name: 'CoursePage',
  data() {
    return {
      categories: [
        { id: 0, name: '全部' },
        { id: 1, name: '后端' },
        { id: 2, name: '前端' },
        { id: 3, name: 'Android' },
        { id: 4, name: 'iOS' },
        { id: 5, name: '人工智能' },
        { id: 6, name: '开发工具' },
        { id: 7, name: '代码人生' },
        { id: 8, name: '阅读' }
      ],
      filters: [
        { id: 'all', name: '全部' },
        { id: 'latest', name: '最新' },
        { id: 'hot', name: '热销' },
        { id: 'price', name: '价格' }
      ],
      currentCategory: 0,
      currentFilter: 'all',
      courseList: [],
      page: 1,
      loading: false,
      noMore: false
    }
  },
  mounted() {
    this.loadCourseList()
  },
  methods: {
    getCategoryName(categoryId) {
      const cat = this.categories.find(c => c.id === categoryId)
      return cat ? cat.name : ''
    },
    selectCategory(categoryId) {
      this.currentCategory = categoryId
      this.page = 1
      this.courseList = []
      this.noMore = false
      this.loadCourseList()
    },
    selectFilter(filterId) {
      this.currentFilter = filterId
      this.page = 1
      this.courseList = []
      this.noMore = false
      this.loadCourseList()
    },
    loadCourseList() {
      if (this.loading) return
      this.loading = true

      setTimeout(() => {
        const mockData = [
          {
            id: 1,
            title: 'Vue3 完全指南',
            subtitle: '从零开始掌握 Vue3 组合式 API',
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

        let filteredData = mockData
        if (this.currentCategory !== 0) {
          filteredData = filteredData.filter(c => c.categoryId === this.currentCategory)
        }

        if (this.currentFilter === 'latest') {
          filteredData = filteredData.sort((a, b) => b.id - a.id)
        } else if (this.currentFilter === 'hot') {
          filteredData = filteredData.sort((a, b) => b.studyCount - a.studyCount)
        } else if (this.currentFilter === 'price') {
          filteredData = filteredData.sort((a, b) => a.price - b.price)
        }

        this.courseList = this.courseList.concat(filteredData.slice((this.page - 1) * 10, this.page * 10))
        this.loading = false
        if (this.courseList.length >= filteredData.length) {
          this.noMore = true
        }
        this.page++
      }, 500)
    },
    goToDetail(courseId) {
      this.$router.push(`/course/${courseId}`)
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.course-page {
  min-height: 100vh;
  background-color: #f4f5f7;
  padding-bottom: 120px;
}

.course-header {
  background: linear-gradient(135deg, #1E80FF 0%, #4A90FF 100%);
  padding: 40px 24px 32px;
  color: #fff;
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-subtitle {
  font-size: 14px;
  opacity: 0.85;
}

.category-tabs {
  display: flex;
  overflow-x: auto;
  padding: 16px 20px;
  background-color: #fff;
  gap: 8px;
  white-space: nowrap;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.category-tab {
  padding: 8px 16px;
  font-size: 14px;
  color: #515767;
  background-color: #f4f5f7;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.category-tab.active {
  background-color: #1E80FF;
  color: #fff;
}

.filter-bar {
  display: flex;
  padding: 12px 20px;
  background-color: #fff;
  border-top: 1px solid #f0f1f5;
  gap: 24px;
}

.filter-item {
  font-size: 14px;
  color: #515767;
  cursor: pointer;
  position: relative;
  padding-bottom: 8px;
}

.filter-item.active {
  color: #1E80FF;
  font-weight: 500;
}

.filter-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #1E80FF;
  border-radius: 1px;
}

.course-list {
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.course-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
}

.course-cover {
  position: relative;
  width: 100%;
  padding-top: 60%;
  background-color: #f4f5f7;
}

.course-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-category {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  background-color: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
}

.course-info {
  padding: 12px;
}

.course-title {
  font-size: 15px;
  font-weight: 600;
  color: #252933;
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-subtitle {
  font-size: 12px;
  color: #8a919f;
  line-height: 1.4;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-author {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-size: 12px;
  color: #8a919f;
}

.course-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-family: fontawesome;
  font-size: 12px;
  color: #c0c4cc;
}

.stat-text {
  font-size: 12px;
  color: #8a919f;
}

.course-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.current-price {
  font-size: 16px;
  font-weight: 700;
  color: #F53F3F;
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.loading-more, .no-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 8px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e0e0e0;
  border-top-color: #3194ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 13px;
  color: #999;
}

.no-more span {
  font-size: 13px;
  color: #ccc;
}

@media screen and (min-width: 768px) {
  .course-page {
    padding-bottom: 24px;
  }

  .course-header {
    padding: 48px 24px 40px;
  }

  .header-title {
    font-size: 36px;
  }

  .course-list {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    padding: 20px 24px;
  }

  .course-cover {
    padding-top: 56%;
  }
}

@media screen and (min-width: 1024px) {
  .course-list {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>