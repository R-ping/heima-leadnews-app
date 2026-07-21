<template>
  <div class="comment-input-wrapper">
    <!-- 回复目标提示 -->
    <div v-if="replyTarget" class="reply-target-bar">
      <span class="reply-label">回复</span>
      <span class="reply-target-name">{{ replyTarget.nickname || replyTarget.name || '用户' }}</span>
      <i class="fa fa-close close-reply" @click="$emit('cancel-reply')"></i>
    </div>

    <div class="comment-input-box" :class="replyTarget ? 'has-reply' : ''">
      <textarea
        ref="textarea"
        v-model="content"
        :placeholder="placeholder"
        :maxlength="1000"
        class="comment-textarea"
        rows="3"
        @input="onInput"
      ></textarea>

      <div class="comment-input-toolbar">
        <div class="toolbar-left">
          <!-- 表情 -->
          <span class="tool-item emoji-btn" @click="showEmoji = !showEmoji" title="表情">
            <i class="fa fa-smile-o"></i>
          </span>
          <!-- 图片上传 -->
          <span class="tool-item image-btn" @click="triggerImageUpload" title="图片">
            <i class="fa fa-image"></i>
          </span>
          <input
            ref="imageInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleImageUpload"
          />
        </div>
        <div class="toolbar-right">
          <span class="word-count" :class="{ 'limit': content.length >= 1000 }">
            {{ content.length }}/1000
          </span>
          <button
            class="submit-btn"
            :disabled="!content.trim() || submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '发表中...' : '发表评论' }}
          </button>
        </div>
      </div>

      <!-- 表情面板 -->
      <div v-if="showEmoji" class="emoji-panel">
        <span
          v-for="emoji in emojiList"
          :key="emoji"
          class="emoji-item"
          @click="insertEmoji(emoji)"
        >{{ emoji }}</span>
      </div>

      <!-- 图片预览 -->
      <div v-if="uploadedImages.length" class="image-preview-row">
        <div
          v-for="(img, index) in uploadedImages"
          :key="index"
          class="image-preview-item"
        >
          <img :src="img" alt="上传图片" />
          <i class="fa fa-times-circle remove-image" @click="removeImage(index)"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { uploadFile } from '@/common/oss_upload'

export default {
  name: 'CommentInput',
  props: {
    replyTarget: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      content: '',
      showEmoji: false,
      submitting: false,
      uploadedImages: [],
      emojiList: [
        '😀', '😃', '😄', '😁', '😅', '🤣', '😂', '🙂', '😊', '😇',
        '😍', '😩', '😘', '😗', '😚', '😋', '😛', '😜', '😪', '😝',
        '🤑', '🤗', '🤭', '🤫', '🤔', '😐', '🤨', '😐', '😑', '😶',
        '😏', '😒', '🙄', '🤬', '🤮', '🤯', '😲', '🤐', '😤', '😪',
        '👍', '👎', '👏', '🙌', '🤝', '💪', '👋', '🤙', '❤️', '🔥',
        '⭐', '🎉', '🙏', '💯', '✨', '💡', '📌', '💬', '🗨️', '📝'
      ]
    }
  },
  computed: {
    placeholder() {
      if (this.replyTarget) {
        const name = this.replyTarget.nickname || this.replyTarget.name || '用户'
        return '回复 ' + name
      }
      return '平等表达，友善交流'
    }
  },
  watch: {
    replyTarget() {
      this.reset()
    }
  },
  methods: {
    onInput() {
      this.$nextTick(() => {
        const el = this.$refs.textarea
        if (el) {
          el.style.height = 'auto'
          el.style.height = Math.min(el.scrollHeight, 120) + 'px'
        }
      })
    },
    insertEmoji(emoji) {
      this.content += emoji
      this.showEmoji = false
      this.$nextTick(() => {
        this.$refs.textarea.focus()
      })
    },
    triggerImageUpload() {
      this.$refs.imageInput.click()
    },
    async handleImageUpload(e) {
      const file = e.target.files[0]
      if (!file) return
      if (!file.type.startsWith('image/')) {
        if (this.$message) this.$message.warning('请选择图片文件')
        return
      }
      try {
        const url = await uploadFile(file)
        if (url) {
          this.uploadedImages.push(url)
          this.content += ' ![图片](' + url + ') '
        }
      } catch (err) {
        console.error('图片上传失败:', err)
        if (this.$message) this.$message.error('图片上传失败')
      }
      this.$refs.imageInput.value = ''
    },
    removeImage(index) {
      this.uploadedImages.splice(index, 1)
    },
    async handleSubmit() {
      const text = this.content.trim()
      if (!text) return
      this.submitting = true
      this.$emit('submit', this.content)
    },
    reset() {
      this.content = ''
      this.uploadedImages = []
      this.showEmoji = false
      this.submitting = false
    },
    setSubmitting(val) {
      this.submitting = val
    }
  }
}
</script>

<style lang="less" scoped>
.comment-input-wrapper {
  margin-bottom: 24px;
}

.reply-target-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f2f3f5;
  border-radius: 6px 6px 0 0;
  font-size: 13px;
  color: #515767;
  .reply-label {
    color: #8a93a6;
  }
  .reply-target-name {
    font-weight: 500;
    color: #1e80ff;
  }
  .close-reply {
    margin-left: auto;
    cursor: pointer;
    color: #8a93a6;
    font-size: 14px;
    &:hover {
      color: #515767;
    }
  }
}

.comment-input-box {
  border: 1px solid #e4e6eb;
  border-radius: 6px;
  overflow: hidden;
  &.has-reply {
    border-radius: 0 0 6px 6px;
    border-top: none;
  }
}

.comment-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #252933;
  background: #fff;
  box-sizing: border-box;
  min-height: 72px;
  &::placeholder {
    color: #c4c9d1;
  }
}

.comment-input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background-color: #fafbfc;
  border-top: 1px solid #f2f3f5;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tool-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: #515767;
  font-size: 18px;
  transition: background-color 0.2s;
  &:hover {
    background-color: #e4e6eb;
  }
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.word-count {
  font-size: 12px;
  color: #8a93a6;
  &.limit {
    color: #ff4d4f;
  }
}

.submit-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  background-color: #1e80ff;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover {
    background-color: #1171ee;
  }
  &:disabled {
    background-color: #c4c9d1;
    cursor: not-allowed;
  }
}

.emoji-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 10px 16px;
  background-color: #fafbfc;
  border-top: 1px solid #f2f3f5;
  max-height: 160px;
  overflow-y: auto;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  &:hover {
    background-color: #e4e6eb;
  }
}

.image-preview-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 16px;
  background-color: #fafbfc;
  border-top: 1px solid #f2f3f5;
}

.image-preview-item {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .remove-image {
    position: absolute;
    top: -4px;
    right: -4px;
    color: #999;
    font-size: 14px;
    cursor: pointer;
    background: #fff;
    border-radius: 50%;
    &:hover {
      color: #ff4d4f;
    }
  }
}
</style>