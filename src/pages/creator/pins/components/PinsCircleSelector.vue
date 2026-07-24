<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="circle-modal" @click.stop>
      <div class="modal-header">
        <span class="modal-title">选择圈子</span>
        <button class="modal-close" @click="$emit('close')">&#xf00d;</button>
      </div>
      <div class="circle-search">
        <input type="text" class="search-input" placeholder="搜索圈子名称" v-model="searchKeyword">
      </div>
      <div class="circle-list">
        <div
          class="circle-card"
          v-for="circle in filteredCircles"
          :key="circle.id"
          :class="{ 'selected': selected && selected.id === circle.id }"
          @click="$emit('select', circle)"
        >
          <div class="circle-icon">{{ circle.icon || '📌' }}</div>
          <div class="circle-info">
            <div class="circle-name">{{ circle.name }}</div>
            <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
          </div>
          <div class="circle-check" v-if="selected && selected.id === circle.id">&#xf00c;</div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="cancel-btn" @click="$emit('close')">不选择圈子</button>
        <button class="confirm-btn" @click="$emit('close')">确认</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PinsCircleSelector',
  props: {
    circles: { type: Array, default: () => [] },
    selected: { type: Object, default: null }
  },
  data() {
    return { searchKeyword: '' }
  },
  computed: {
    filteredCircles() {
      if (!this.searchKeyword) return this.circles
      return this.circles.filter(c => c.name.includes(this.searchKeyword))
    }
  }
}
</script>

<style lang="less" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.circle-modal {
  background: #fff;
  border-radius: 8px;
  width: 600px;
  max-height: 70vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f2f3f5;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #252933;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-family: fontawesome;
  font-size: 16px;
  color: #8a919f;
  cursor: pointer;
  border-radius: 50%;
  &:hover {
    background: #f2f3f5;
    color: #515767;
  }
}

.circle-search {
  padding: 12px 20px;
}

.search-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e4e6eb;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  &:focus { border-color: #1e80ff; }
}

.circle-list {
  padding: 12px 20px;
  max-height: 300px;
  overflow-y: auto;
}

.circle-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover { background: #f7f8fa; }
  &.selected { background: #eaf2ff; }
}

.circle-icon { font-size: 24px; }

.circle-info { flex: 1; }

.circle-name {
  font-size: 14px;
  color: #252933;
  margin-bottom: 2px;
}

.circle-stats {
  font-size: 12px;
  color: #8a919f;
}

.circle-check {
  font-family: fontawesome;
  font-size: 16px;
  color: #1e80ff;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f2f3f5;
}

.cancel-btn {
  padding: 8px 24px;
  border: 1px solid #e4e6eb;
  border-radius: 4px;
  background: #fff;
  color: #515767;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #f7f8fa; }
}

.confirm-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 4px;
  background: #1e80ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #4096ff; }
}
</style>