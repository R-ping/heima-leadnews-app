<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="publish-modal" @click.stop>
      <div class="modal-header">
        <span class="modal-title">发布沸点</span>
        <button class="modal-close" @click="$emit('close')">&#xf00d;</button>
      </div>
      <div class="publish-box">
        <textarea
          class="publish-input"
          v-model="content"
          placeholder="#新人报道#"
          maxlength="1000"
        ></textarea>
        <div class="publish-footer">
          <div class="publish-actions">
            <button class="action-btn">😊</button>
            <button class="action-btn">📷</button>
            <button class="action-btn">🔗</button>
            <button class="action-btn circle-btn" @click="$emit('select-circle')">
              <span class="action-icon">&#xf02e;</span>
              <span>{{ selectedCircleName }}</span>
            </button>
            <button class="action-btn topic-btn" @click="$emit('select-topic')">
              <span class="action-icon">&#xf02b;</span>
              <span>{{ selectedTopicName }}</span>
            </button>
          </div>
          <div class="publish-count">{{ content.length }}/1000</div>
          <button class="publish-btn" :disabled="!content.trim()" @click="$emit('publish', content)">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PinsPublishModal',
  props: {
    value: { type: String, default: '' },
    selectedCircleName: { type: String, default: '请选择圈子' },
    selectedTopicName: { type: String, default: '话题' }
  },
  computed: {
    content: {
      get() { return this.value },
      set(val) { this.$emit('input', val) }
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

.publish-modal {
  background: #fff;
  border-radius: 8px;
  width: 600px;
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

.publish-box {
  padding: 16px 20px;
}

.publish-input {
  width: 100%;
  height: 80px;
  border: none;
  resize: none;
  font-size: 14px;
  line-height: 1.6;
  color: #252933;
  &::placeholder { color: #c4c9d1; }
  &:focus { outline: none; }
}

.publish-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
}

.publish-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #8a919f;
  cursor: pointer;
  &:hover {
    color: #1e80ff;
    background: #f0f5ff;
    border-radius: 4px;
  }
}

.action-icon {
  font-family: fontawesome;
}

.circle-btn, .topic-btn {
  padding: 6px 12px;
  border-radius: 4px;
}

.publish-count {
  font-size: 13px;
  color: #c4c9d1;
}

.publish-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 4px;
  background: #1e80ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #4096ff; }
  &:disabled { background: #c4c9d1; cursor: not-allowed; }
}
</style>