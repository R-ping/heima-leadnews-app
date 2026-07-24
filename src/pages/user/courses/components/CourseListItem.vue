<template>
  <div class="list-item" @click="$emit('click')">
    <div class="list-cover">
      <img :src="course.coverImage || defaultCover" alt="封面" />
    </div>
    <div class="list-info">
      <div class="list-title">{{ course.title }}</div>
      <div class="list-meta">
        <span class="list-author">{{ course.authorName }}</span>
        <span class="list-date" v-if="course.lastLearnAt">{{ formatDate(course.lastLearnAt) }}</span>
      </div>
      <div class="list-progress" v-if="course.progress !== undefined">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
        </div>
        <span class="progress-text">{{ course.progress }}%</span>
      </div>
    </div>
    <div class="list-action" @click.stop>
      <button
        class="action-btn"
        :class="course.isTrial ? 'trial' : 'continue'"
        @click="$emit('click')"
      >
        {{ course.isTrial ? '试学' : '继续学习' }}
      </button>
    </div>
  </div>
</template>

<script>
import { formatDate } from '@/utils/format'

export default {
  name: 'CourseListItem',
  props: {
    course: { type: Object, required: true }
  },
  data() {
    return {
      defaultCover: '/static/images/avatar_head_1.png'
    }
  },
  methods: {
    formatDate
  }
}
</script>

<style lang="less" scoped>
.list-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f8fafc;
  }
}

.list-cover {
  width: 120px;
  height: 68px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f2f5;
  margin-right: 16px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.list-info {
  flex: 1;
  min-width: 0;
}

.list-title {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.list-author {
  font-size: 13px;
  color: #8c8c8c;
}

.list-date {
  font-size: 12px;
  color: #bfbfbf;
}

.list-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 260px;
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

.list-action {
  flex-shrink: 0;
  margin-left: 16px;
}

.action-btn {
  padding: 6px 20px;
  border: 1px solid #1e80ff;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  background: #fff;
  color: #1e80ff;
  transition: all 0.2s;

  &:hover {
    background: #1e80ff;
    color: #fff;
  }

  &.trial {
    border-color: #52c41a;
    color: #52c41a;

    &:hover {
      background: #52c41a;
      color: #fff;
    }
  }

  &.continue {
    border-color: #1e80ff;
    color: #1e80ff;

    &:hover {
      background: #1e80ff;
      color: #fff;
    }
  }
}

@media screen and (max-width: 767px) {
  .list-item {
    padding: 12px 16px;
  }

  .list-cover {
    width: 80px;
    height: 50px;
  }

  .list-action {
    margin-left: 8px;
  }

  .action-btn {
    padding: 4px 12px;
    font-size: 12px;
  }
}
</style>