<template>
  <div class="course-read-page">
    <div class="read-header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">&#xf060;</span>
        <span class="course-title">{{ course.title }}</span>
      </div>
      <div class="header-right">
        <span class="header-btn" @click="goToDetail">目录</span>
      </div>
    </div>

    <div class="read-body">
      <div class="chapter-sidebar">
        <div class="sidebar-header">目录</div>
        <div class="chapter-list">
          <div 
            v-for="chapter in chapters" 
            :key="chapter.id"
            class="chapter-item"
            :class="{ 
              active: currentChapterId === chapter.id,
              locked: !chapter.isFree && !isPurchased 
            }"
            @click="switchChapter(chapter)"
          >
            <span class="chapter-title">{{ chapter.title }}</span>
            <span class="free-tag" v-if="chapter.isFree">免费</span>
            <span class="lock-icon" v-else-if="!isPurchased">&#xf023;</span>
          </div>
        </div>
      </div>

      <div class="content-area">
        <div v-if="!hasAccess" class="locked-content">
          <div class="lock-icon-lg">&#xf023;</div>
          <div class="locked-title">该章节需要购买后才能阅读</div>
          <div class="locked-desc">购买课程后即可阅读全部内容，支持7天无理由退款</div>
          <button class="buy-now-btn" @click="goToBuy">立即购买</button>
        </div>

        <div v-else class="markdown-content" v-html="renderedContent"></div>
      </div>
    </div>

    <div class="read-footer">
      <div class="footer-left">
        <button 
          class="nav-btn" 
          :disabled="!prevChapter"
          @click="goToPrev"
        >
          <span class="nav-icon">&#xf060;</span>
          <span>上一节</span>
        </button>
      </div>
      <div class="footer-center">
        <span class="progress-text">{{ currentIndex + 1 }}/{{ chapters.length }}</span>
      </div>
      <div class="footer-right">
        <button 
          class="nav-btn" 
          :disabled="!nextChapter"
          @click="goToNext"
        >
          <span>下一节</span>
          <span class="nav-icon">&#xf061;</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import { toast } from "@/utils/toast"
import { courseMockData } from './mockData'

marked.setOptions({
  gfm: true,
  breaks: true
})

export default {
  name: 'CourseReadPage',
  data() {
    return {
      currentChapterId: null,
      currentChapter: {},
      course: {},
      chapters: [],
      isPurchased: false,
      renderedContent: ''
    }
  },
  computed: {
    currentIndex() {
      return this.chapters.findIndex(c => c.id === this.currentChapterId)
    },
    prevChapter() {
      const idx = this.currentIndex
      return idx > 0 ? this.chapters[idx - 1] : null
    },
    nextChapter() {
      const idx = this.currentIndex
      return idx < this.chapters.length - 1 ? this.chapters[idx + 1] : null
    },
    hasAccess() {
      if (!this.currentChapter) return false
      return this.currentChapter.isFree || this.isPurchased
    }
  },
  mounted() {
    this.loadChapterDetail()
    this.checkPurchaseStatus()
  },
  methods: {
    loadChapterDetail() {
      const chapterId = parseInt(this.$route.params.id)
      const mockChapters = courseMockData.chapters

      this.currentChapter = mockChapters.find(c => c.id === chapterId) || mockChapters[0]
      this.currentChapterId = this.currentChapter.id

      const courseId = this.currentChapter.courseId
      this.course = courseMockData.courses.find(c => c.id === courseId) || courseMockData.courses[0]
      this.chapters = mockChapters.filter(c => c.courseId === courseId)

      if (this.hasAccess) {
        this.renderedContent = sanitizeHtml(marked(this.currentChapter.content || ''))
      }
    },
    checkPurchaseStatus() {
      const purchasedCourses = JSON.parse(localStorage.getItem('purchasedCourses') || '[]')
      this.isPurchased = purchasedCourses.includes(this.course.id)
    },
    switchChapter(chapter) {
      if (!chapter.isFree && !this.isPurchased) {
        toast('该章节需要购买后才能阅读', 2)
        return
      }
      this.currentChapterId = chapter.id
      this.currentChapter = chapter
      this.renderedContent = sanitizeHtml(marked(chapter.content || ''))
    },
    goBack() {
      this.$router.back()
    },
    goToDetail() {
      this.$router.push(`/course/${this.course.id}`)
    },
    goToBuy() {
      this.$router.push(`/course/${this.course.id}`)
    },
    goToPrev() {
      if (this.prevChapter) {
        this.switchChapter(this.prevChapter)
      }
    },
    goToNext() {
      if (this.nextChapter) {
        this.switchChapter(this.nextChapter)
      }
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';
@import 'github-markdown-css';

.course-read-page {
  min-height: 100vh;
  background-color: #f4f5f7;
  display: flex;
  flex-direction: column;
}

.read-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background-color: #fff;
  border-bottom: 1px solid #f0f1f5;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: fontawesome;
  font-size: 20px;
  color: #515767;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background-color: #f5f7fa;
}

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: #252933;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-btn {
  padding: 6px 14px;
  background-color: #f5f7fa;
  color: #515767;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
}

.read-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.chapter-sidebar {
  width: 280px;
  background-color: #fff;
  border-right: 1px solid #f0f1f5;
  overflow-y: auto;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #252933;
  border-bottom: 1px solid #f0f1f5;
}

.chapter-list {
  padding: 8px 0;
}

.chapter-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.chapter-item:hover {
  background-color: #f5f7fa;
}

.chapter-item.active {
  background-color: #E8F3FF;
}

.chapter-item.active .chapter-title {
  color: #1E80FF;
  font-weight: 500;
}

.chapter-item.locked {
  opacity: 0.5;
}

.chapter-title {
  font-size: 14px;
  color: #515767;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.free-tag {
  padding: 2px 8px;
  background-color: #EAF2FF;
  color: #1E80FF;
  font-size: 12px;
  border-radius: 4px;
  flex-shrink: 0;
}

.lock-icon {
  font-family: fontawesome;
  font-size: 14px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.markdown-content {
  max-width: 800px;
  margin: 0 auto;
  font-size: 15px;
  line-height: 1.8;
}

.markdown-content :deep(h1) {
  font-size: 28px;
  font-weight: 700;
  color: #252933;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f1f5;
}

.markdown-content :deep(h2) {
  font-size: 22px;
  font-weight: 600;
  color: #252933;
  margin: 24px 0 12px;
}

.markdown-content :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  color: #252933;
  margin: 20px 0 10px;
}

.markdown-content :deep(p) {
  margin-bottom: 12px;
  color: #515767;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-bottom: 12px;
  padding-left: 24px;
}

.markdown-content :deep(li) {
  margin-bottom: 6px;
  color: #515767;
}

.markdown-content :deep(code) {
  padding: 2px 6px;
  background-color: #f5f7fa;
  color: #F53F3F;
  font-size: 14px;
  border-radius: 4px;
}

.markdown-content :deep(pre) {
  background-color: #1e1e1e;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  color: #d4d4d4;
  font-size: 14px;
  padding: 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #1E80FF;
  padding-left: 16px;
  margin: 16px 0;
  color: #8a919f;
  background-color: #f5f7fa;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

.markdown-content :deep(a) {
  color: #1E80FF;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.locked-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: 16px;
}

.lock-icon-lg {
  font-family: fontawesome;
  font-size: 64px;
  color: #c0c4cc;
}

.locked-title {
  font-size: 20px;
  font-weight: 600;
  color: #252933;
}

.locked-desc {
  font-size: 14px;
  color: #8a919f;
  text-align: center;
}

.buy-now-btn {
  padding: 12px 32px;
  background-color: #F53F3F;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 16px;
}

.read-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background-color: #fff;
  border-top: 1px solid #f0f1f5;
}

.footer-left,
.footer-right {
  flex: 1;
}

.footer-center {
  text-align: center;
}

.progress-text {
  font-size: 14px;
  color: #8a919f;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: #f5f7fa;
  color: #515767;
  font-size: 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background-color: #e8e8e8;
  color: #252933;
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.nav-icon {
  font-family: fontawesome;
  font-size: 14px;
}

.footer-left .nav-btn {
  float: left;
}

.footer-right .nav-btn {
  float: right;
}

@media screen and (max-width: 768px) {
  .chapter-sidebar {
    display: none;
  }

  .content-area {
    padding: 16px;
  }

  .markdown-content {
    font-size: 14px;
  }

  .markdown-content :deep(h1) {
    font-size: 24px;
  }

  .markdown-content :deep(h2) {
    font-size: 20px;
  }

  .read-footer {
    padding: 10px 16px;
  }

  .nav-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>