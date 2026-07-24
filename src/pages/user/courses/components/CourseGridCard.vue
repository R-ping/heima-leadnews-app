<template>
  <div class="course-card" @click="$emit('click')">
    <div class="card-cover">
      <img :src="course.coverImage || defaultCover" alt="封面" />
    </div>
    <div class="card-info">
      <div class="card-title">{{ course.title }}</div>
      <div class="card-author">{{ course.authorName }}</div>
      <div class="card-progress" v-if="course.progress !== undefined">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
        </div>
        <span class="progress-text">{{ course.progress }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CourseGridCard',
  props: {
    course: { type: Object, required: true }
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
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  }
}

.card-cover {
  width: 100%;
  height: 160px;
  background: #f5f7fa;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-info {
  padding: 12px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-author {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.card-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #e8e8e8;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #1e80ff;
  border-radius: 2px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 12px;
  color: #666;
  flex-shrink: 0;
}
</style>