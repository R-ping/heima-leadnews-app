<template>
  <div class="article-item" @click="$emit('click')">
    <div class="article-main">
      <span class="type-badge">{{ typeLabel }}</span>
      <div class="article-info">
        <div class="article-title">{{ item.articleTitle }}</div>
        <div v-if="item.summary" class="article-summary">{{ item.summary }}</div>
        <div class="article-meta">
          <span class="article-author">{{ item.authorName }}</span>
          <span class="meta-divider">·</span>
          <span class="article-stat">{{ formatCount(item.readCount) }} 阅读</span>
          <span class="meta-divider">·</span>
          <span class="article-stat">{{ formatCount(item.likeCount) }} 点赞</span>
          <span class="meta-divider">·</span>
          <span class="article-stat">{{ formatCount(item.commentCount) }} 评论</span>
        </div>
      </div>
    </div>
    <div class="article-time">{{ formatTime(item.browseTime) }}</div>
  </div>
</template>

<script>
import { formatCount, formatTime } from '@/utils/format'

const TYPE_MAP = { 1: '文章', 2: '沸点', 3: '课程', 4: '专栏' }

export default {
  name: 'HistoryItem',
  props: {
    item: { type: Object, required: true }
  },
  computed: {
    typeLabel() {
      return TYPE_MAP[this.item.targetType] || '文章'
    }
  },
  methods: {
    formatCount,
    formatTime
  }
}
</script>

<style lang="less" scoped>
.article-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #F2F3F5;
  cursor: pointer;
  transition: background-color 0.2s;
  &:last-child {
    border-bottom: none;
  }
  &:hover {
    background-color: #F7F8FA;
  }
}

.article-main {
  display: flex;
  align-items: flex-start;
  flex: 1;
  min-width: 0;
}

.type-badge {
  display: inline-block;
  padding: 2px 10px;
  background: #F0F2F5;
  color: #666;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
  margin-right: 12px;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 15px;
  font-weight: 500;
  color: #252933;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  &:hover {
    color: #1E80FF;
  }
}

.article-summary {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
  color: #8A919F;
  margin-top: 6px;
}

.article-author {
  color: #8A919F;
}

.meta-divider {
  margin: 0 8px;
  color: #C4C9D1;
}

.article-stat {
  display: inline-flex;
  align-items: center;
}

.article-time {
  font-size: 13px;
  color: #8A919F;
  flex-shrink: 0;
  margin-left: 16px;
}

@media screen and (max-width: 768px) {
  .article-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .article-time {
    margin-left: 0;
  }
}
</style>