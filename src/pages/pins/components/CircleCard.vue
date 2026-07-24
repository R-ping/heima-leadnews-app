<template>
  <div class="circle-card">
    <div class="circle-icon">{{ circle.icon || '📌' }}</div>
    <div class="circle-info">
      <div class="circle-name">{{ circle.name }}</div>
      <div class="circle-desc" v-if="circle.description && showDesc">{{ circle.description }}</div>
      <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
    </div>
    <div v-if="mode === 'my'" class="circle-status">已加入</div>
    <button
      v-else
      class="join-btn"
      :class="{ joined: joined }"
      @click.stop="$emit('toggle-join', circle)"
    >
      {{ joined ? '已加入' : '+ 加入' }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'CircleCard',
  props: {
    circle: { type: Object, required: true },
    joined: { type: Boolean, default: false },
    mode: { type: String, default: 'square' },
    showDesc: { type: Boolean, default: false }
  }
}
</script>

<style lang="less" scoped>
.circle-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f7f8fa;
  transition: background-color 0.2s;
  &:hover {
    background: #eaf2ff;
  }
}

.circle-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e6eb;
}

.circle-info {
  flex: 1;
  min-width: 0;
}

.circle-name {
  font-size: 14px;
  font-weight: 500;
  color: #252933;
  margin-bottom: 2px;
}

.circle-desc {
  font-size: 12px;
  color: #8a919f;
  margin-bottom: 2px;
}

.circle-stats {
  font-size: 12px;
  color: #8a919f;
}

.circle-status {
  font-size: 12px;
  color: #8a919f;
  padding: 4px 12px;
  background: #f2f3f5;
  border-radius: 4px;
}

.join-btn {
  padding: 6px 16px;
  border: 1px solid #1e80ff;
  border-radius: 4px;
  background: #fff;
  color: #1e80ff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover:not(.joined) {
    background: #eaf2ff;
  }
  &.joined {
    border-color: #8a919f;
    color: #8a919f;
  }
}
</style>