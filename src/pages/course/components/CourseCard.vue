<template>
  <div class="course-card" @click="$emit('click')">
    <div class="course-cover">
      <img :src="course.coverImage || defaultCover" alt="课程封面" />
      <div class="course-category" v-if="categoryName">{{ categoryName }}</div>
    </div>
    <div class="course-info">
      <div class="course-title">{{ course.title }}</div>
      <div class="course-subtitle" v-if="course.subtitle">{{ course.subtitle }}</div>
      <div class="course-author">
        <img :src="course.authorAvatar || defaultCover" class="author-avatar" />
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
</template>

<script>
export default {
  name: 'CourseCard',
  props: {
    course: { type: Object, required: true },
    categoryName: { type: String, default: '' }
  },
  data() {
    return {
      defaultCover: '/static/images/avatar_head_1.png'
    }
  }
}
</script>

<style lang="less" scoped>
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-subtitle {
  font-size: 12px;
  color: #8a919f;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-author {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
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
  color: #8a919f;
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
  font-weight: 600;
  color: #f56c6c;
}
.original-price {
  font-size: 12px;
  color: #c4c9d1;
  text-decoration: line-through;
}
</style>