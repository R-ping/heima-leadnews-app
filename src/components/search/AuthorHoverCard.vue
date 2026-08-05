<template>
  <transition name="hover-card-fade">
    <div
      v-if="visible"
      class="author-hover-card"
      :style="cardStyle"
      @click.stop="onCardClick"
    >
      <div class="card-arrow" :class="arrowDirection"></div>

      <div class="card-body" @click.stop>
        <div class="author-section">
          <img
            class="author-avatar"
            :src="author.avatar || defaultAvatar"
            alt="author avatar"
          />
          <div class="author-info">
            <div class="author-name-row">
              <span class="author-name">{{ author.name || '匿名用户' }}</span>
              <span v-if="author.level" class="author-level">{{ author.level }}</span>
            </div>
            <div class="author-bio" v-if="author.bio">{{ author.bio }}</div>
          </div>
        </div>

        <div class="author-stats">
          <div class="stat-item">
            <span class="stat-value">{{ formatCount(author.followCount) }}</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ formatCount(author.followerCount) }}</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>

        <div class="author-actions">
          <button
            class="btn-follow"
            :class="{ 'is-followed': author.isFollowed }"
            @click="onFollow"
          >
            {{ author.isFollowed ? '已关注' : '+ 关注' }}
          </button>
          <button
            class="btn-message"
            @click="onMessage"
          >
            私信
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'AuthorHoverCard',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    author: {
      type: Object,
      default: function () { return {} }
    },
    position: {
      type: Object,
      default: function () { return { top: 0, left: 0 } }
    }
  },
  data() {
    return {
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA2NCA2NCI+PHJlY3Qgd2lkdGg9IjY0IiBoZWlnaHQ9IjY0IiBmaWxsPSIjZTBlMGUwIiByeD0iMzIiLz48dGV4dCB4PSIzMiIgeT0iNDIiIGZvbnQtc2l6ZT0iMzIiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGZpbGw9IiM5OTkiPuWvueaPjC90ZXh0Pjwvc3ZnPg=='
    }
  },
  computed: {
    cardStyle() {
      return {
        top: this.position.top + 'px',
        left: this.position.left + 'px'
      }
    },
    arrowDirection() {
      return this.position.arrow || 'top'
    }
  },
  watch: {
    visible(val) {
      if (val) {
        document.addEventListener('click', this.handleOutsideClick)
      } else {
        document.removeEventListener('click', this.handleOutsideClick)
      }
    }
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleOutsideClick)
  },
  methods: {
    formatCount(count) {
      if (count == null) return '0'
      count = Number(count)
      if (count > 9999) {
        return (count / 10000).toFixed(1) + 'w'
      }
      if (count > 999) {
        return (count / 1000).toFixed(1) + 'k'
      }
      return String(count)
    },
    onFollow() {
      this.$emit('follow', this.author.id)
    },
    onMessage() {
      this.$emit('message', this.author.id)
    },
    onCardClick() {},
    handleOutsideClick(e) {
      if (this.$el && !this.$el.contains(e.target)) {
        this.$emit('close')
      }
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../styles/article';

.author-hover-card {
  position: fixed;
  z-index: 9999;
  width: 220px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
  overflow: visible;
  transform-origin: top center;
}

.card-arrow {
  position: absolute;
  width: 12px;
  height: 12px;
  background-color: #ffffff;
  transform: rotate(45deg);
  z-index: -1;
}

.card-arrow.top {
  top: -6px;
  left: 50%;
  margin-left: -6px;
  box-shadow: -2px -2px 4px rgba(0, 0, 0, 0.04);
}

.card-arrow.bottom {
  bottom: -6px;
  left: 50%;
  margin-left: -6px;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.04);
}

.card-arrow.left {
  left: -6px;
  top: 50%;
  margin-top: -6px;
  box-shadow: -2px 2px 4px rgba(0, 0, 0, 0.04);
}

.card-arrow.right {
  right: -6px;
  top: 50%;
  margin-top: -6px;
  box-shadow: 2px -2px 4px rgba(0, 0, 0, 0.04);
}

.card-body {
  padding: 20px 16px 16px;
  position: relative;
}

.author-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 16px;
}

.author-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #f0f0f0;
  margin-bottom: 10px;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.author-info {
  width: 100%;
}

.author-name-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #252933;
  line-height: 1.4;
}

.author-level {
  font-size: 11px;
  color: #1E80FF;
  background-color: #E8F3FF;
  padding: 1px 6px;
  border-radius: 3px;
  line-height: 1.4;
}

.author-bio {
  font-size: 13px;
  color: #8A93A6;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-top: 4px;
}

.author-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  border-top: 1px solid #F2F3F5;
  border-bottom: 1px solid #F2F3F5;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  flex: 1;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #252933;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #8A93A6;
  line-height: 1;
}

.stat-divider {
  width: 1px;
  height: 24px;
  background-color: #E5E6EB;
}

.author-actions {
  display: flex;
  gap: 8px;
}

.btn-follow {
  flex: 1;
  height: 32px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  background-color: #1E80FF;
  color: #ffffff;
}

.btn-follow:hover {
  background-color: #1A6FD9;
}

.btn-follow.is-followed {
  background-color: #F2F3F5;
  color: #8A93A6;
}

.btn-follow.is-followed:hover {
  background-color: #E5E6EB;
  color: #515767;
}

.btn-message {
  flex: 1;
  height: 32px;
  border: 1px solid #E5E6EB;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  background-color: #ffffff;
  color: #515767;
}

.btn-message:hover {
  border-color: #1E80FF;
  color: #1E80FF;
}

.hover-card-fade-enter-active,
.hover-card-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.hover-card-fade-enter,
.hover-card-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

@media screen and (max-width: 767px) {
  .author-hover-card {
    width: 180px;
  }

  .author-avatar {
    width: 48px;
    height: 48px;
  }

  .author-name {
    font-size: 14px;
  }

  .author-bio {
    font-size: 12px;
  }

  .stat-value {
    font-size: 14px;
  }

  .btn-follow,
  .btn-message {
    height: 28px;
    font-size: 12px;
  }
}
</style>