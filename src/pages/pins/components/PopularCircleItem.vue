<template>
  <div class="popular-item">
    <div class="popular-icon">{{ circle.icon || '🔥' }}</div>
    <div class="popular-info">
      <div class="popular-name">{{ circle.name }}</div>
      <div class="popular-desc">{{ circle.description }}</div>
    </div>
    <button
      class="popular-join-btn"
      :class="{ joined: joined }"
      @click.stop="$emit('toggle-join', circle)"
    >
      {{ joined ? '已加' : '加' }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'PopularCircleItem',
  props: {
    circle: { type: Object, required: true },
    joined: { type: Boolean, default: false }
  }
}
</script>

<style lang="less" scoped>
.popular-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover {
    background: #f7f8fa;
  }
}

.popular-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.popular-info {
  flex: 1;
  min-width: 0;
}

.popular-name {
  font-size: 13px;
  font-weight: 500;
  color: #252933;
  margin-bottom: 2px;
}

.popular-desc {
  font-size: 12px;
  color: #8a919f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.popular-join-btn {
  padding: 4px 8px;
  border: 1px solid #1e80ff;
  border-radius: 4px;
  background: #fff;
  color: #1e80ff;
  font-size: 12px;
  cursor: pointer;
  flex-shrink: 0;
  &:hover:not(.joined) {
    background: #eaf2ff;
  }
  &.joined {
    border-color: #8a919f;
    color: #8a919f;
  }
}
</style>