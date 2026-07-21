<template>
  <div class="comment-list">
    <div class="comment-list-header">
      <h3 class="comment-title">评论</h3>
      <span class="comment-count" v-if="comments.length">{{ comments.length }} 条评论</span>
    </div>

    <div v-if="comments.length === 0" class="comment-empty">
      <div class="empty-icon">💬</div>
      <p>暂无评论，快来抢沙发</p>
    </div>

    <div v-else class="comment-items">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <!-- 一级评论 -->
        <div class="comment-main">
          <img
            v-if="comment.userAvatar"
            :src="comment.userAvatar"
            class="comment-avatar"
            alt=""
          />
          <div v-else class="comment-avatar default-avatar">
            {{ (comment.userName || '?')[0] }}
          </div>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-time">{{ timeAgo(comment.createdTime) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
              <span class="action-item" @click="$emit('like', comment)">
                <i :class="comment.liked ? 'fa fa-heart liked' : 'fa fa-heart-o'"></i>
                <span v-if="comment.likeCount > 0">{{ comment.likeCount }}</span>
              </span>
              <span class="action-item reply-btn" @click="$emit('reply', comment)">
                <i class="fa fa-comment-o"></i>
                <span>回复</span>
              </span>
            </div>
          </div>
        </div>

        <!-- 二级评论 -->
        <div v-if="comment.children && comment.children.length" class="comment-children">
          <div
            v-for="child in comment.children"
            :key="child.id"
            class="child-comment-item"
          >
            <img
              v-if="child.userAvatar"
              :src="child.userAvatar"
              class="comment-avatar small"
              alt=""
            />
            <div v-else class="comment-avatar small default-avatar">
              {{ (child.userName || '?')[0] }}
            </div>
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-author">{{ child.userName }}</span>
                <span class="comment-time">{{ timeAgo(child.createdTime) }}</span>
              </div>
              <div class="comment-content">{{ child.content }}</div>
              <div class="comment-actions">
                <span class="action-item" @click="$emit('like', child)">
                  <i :class="child.liked ? 'fa fa-heart liked' : 'fa fa-heart-o'"></i>
                  <span v-if="child.likeCount > 0">{{ child.likeCount }}</span>
                </span>
                <span class="action-item reply-btn" @click="$emit('reply', comment)">
                  <i class="fa fa-comment-o"></i>
                  <span>回复</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { timeAgo } from '@/utils/timeAgo'

export default {
  name: 'CommentList',
  props: {
    comments: {
      type: Array,
      default: () => []
    },
    articleId: {
      type: [Number, String],
      default: null
    }
  },
  methods: {
    timeAgo(time) {
      return timeAgo(time)
    }
  }
}
</script>

<style lang="less" scoped>
.comment-list {
  padding: 0;
}

.comment-list-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e6eb;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #252933;
  margin: 0;
}

.comment-count {
  font-size: 13px;
  color: #8a93a6;
}

.comment-empty {
  text-align: center;
  padding: 60px 20px;
  color: #8a93a6;
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  p {
    font-size: 14px;
    margin: 0;
  }
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #f2f3f5;
  &:last-child {
    border-bottom: none;
  }
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  &.small {
    width: 32px;
    height: 32px;
  }
  &.default-avatar {
    background-color: #1e80ff;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 600;
  }
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-author {
  font-size: 14px;
  font-weight: 500;
  color: #515767;
}

.comment-time {
  font-size: 12px;
  color: #8a93a6;
}

.comment-content {
  font-size: 14px;
  color: #252933;
  line-height: 1.6;
  word-break: break-word;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8a93a6;
  cursor: pointer;
  transition: color 0.2s;
  user-select: none;
  &:hover {
    color: #1e80ff;
  }
  .liked {
    color: #ff4d4f;
  }
}

.reply-btn {
  &:hover {
    color: #1e80ff;
  }
}

.comment-children {
  margin-top: 16px;
  margin-left: 52px;
  padding: 12px 16px;
  background-color: #f7f8fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.child-comment-item {
  display: flex;
  gap: 10px;
  &:last-child {
    margin-bottom: 0;
  }
}
</style>