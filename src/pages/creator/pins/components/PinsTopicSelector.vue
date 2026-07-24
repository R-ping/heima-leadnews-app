<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="topic-modal" @click.stop>
      <div class="modal-header">
        <span class="modal-title">选择话题</span>
        <button class="modal-close" @click="$emit('close')">&#xf00d;</button>
      </div>
      <div class="topic-search">
        <input type="text" class="search-input" placeholder="搜索话题名称" v-model="searchKeyword">
      </div>
      <div class="topic-list">
        <div
          class="topic-item"
          v-for="topic in filteredTopics"
          :key="topic.id"
          :class="{ 'selected': selected && selected.id === topic.id }"
          @click="$emit('select', topic)"
        >
          <span class="topic-name">{{ topic.name }}</span>
          <span class="topic-count">{{ topic.count }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PinsTopicSelector',
  props: {
    topics: { type: Array, default: () => [] },
    selected: { type: Object, default: null }
  },
  data() {
    return { searchKeyword: '' }
  },
  computed: {
    filteredTopics() {
      if (!this.searchKeyword) return this.topics
      return this.topics.filter(t => t.name.includes(this.searchKeyword))
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

.topic-modal {
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

.topic-search {
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

.topic-list {
  padding: 12px 20px;
  max-height: 400px;
  overflow-y: auto;
}

.topic-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover { background: #f7f8fa; }
  &.selected {
    background: #eaf2ff;
    .topic-name { color: #1e80ff; }
  }
}

.topic-name {
  font-size: 14px;
  color: #252933;
}

.topic-count {
  font-size: 12px;
  color: #8a919f;
}
</style>