<template>
    <div class="publish-box">
        <!-- 话题标签 -->
        <div class="topic-tag-wrapper" v-if="localTopic" @click.stop>
            <span class="topic-tag">
                {{ topicDisplayName }}
                <span class="topic-tag-remove" @click="removeTopic">&#xf00d;</span>
            </span>
        </div>

        <!-- 话题已移除提示 -->
        <div class="topic-removed-hint" v-if="topicRemoved">
            <span>话题已移除</span>
        </div>

        <textarea 
            ref="publishTextarea"
            class="publish-input"
            :class="{ 'has-topic': !!localTopic }"
            v-model="localContent"
            :placeholder="placeholderText"
            maxlength="1000"
            @input="onPublishInput"
            @keydown="onTextareaKeydown"
        ></textarea>
        
        <!-- 图片预览 -->
        <div class="publish-extras" v-if="localImages.length > 0">
            <div class="publish-images">
                <div class="publish-image-item" v-for="(img, idx) in localImages" :key="idx">
                    <img :src="img" class="publish-image-preview" alt="preview">
                    <span class="publish-image-remove" @click="removeImage(idx)">&#xf00d;</span>
                </div>
            </div>
        </div>

        <!-- 链接预览 -->
        <div class="publish-extras" v-if="localLinkPreview">
            <div class="publish-link-preview">
                <span class="publish-link-domain">{{ escapeHtml(localLinkPreview.domain) }}</span>
                <span class="publish-link-title">{{ escapeHtml(localLinkPreview.title || localLinkPreview.url) }}</span>
                <span class="publish-link-remove" @click="removeLink">&#xf00d;</span>
            </div>
        </div>

        <!-- 链接输入框 -->
        <div class="publish-link-input" v-if="showLinkInput">
            <input 
                type="text" 
                class="link-url-input" 
                placeholder="请输入链接地址" 
                v-model="linkUrl"
                @keyup.enter="fetchLinkPreview"
            >
            <button class="link-add-btn" @click="fetchLinkPreview" :disabled="!linkUrl.trim()">添加</button>
            <button class="link-cancel-btn" @click="showLinkInput = false; linkUrl = ''">取消</button>
        </div>

        <div class="publish-footer">
            <div class="publish-actions">
                <!-- 表情按钮 -->
                <button class="action-btn emoji-btn" @click="showEmojiPicker = !showEmojiPicker">
                    <span>😊</span>
                </button>
                <!-- 图片按钮 -->
                <button class="action-btn" @click="triggerImageUpload" :disabled="!!localLinkPreview">
                    <span>📷</span>
                </button>
                <input 
                    type="file" 
                    ref="imageInput" 
                    accept="image/*" 
                    style="display:none" 
                    @change="handleImageUpload"
                >
                <!-- 链接按钮 -->
                <button class="action-btn" @click="toggleLinkInput" :disabled="localImages.length > 0">
                    <span>🔗</span>
                </button>
                <!-- 圈子选择 -->
                <button 
                    class="action-btn circle-btn"
                    @click="$emit('select-circle')"
                >
                    <span class="action-icon">&#xf02e;</span>
                    <span>{{ localCircle ? escapeHtml(localCircle.name) : '请选择圈子' }}</span>
                </button>
                <!-- 话题选择 -->
                <button 
                    class="action-btn topic-btn"
                    @click="onSelectTopic"
                >
                    <span class="action-icon">&#xf02b;</span>
                    <span>{{ localTopic ? topicDisplayName : '话题' }}</span>
                </button>
            </div>
            <div class="publish-count">{{ localContent.length }}/1000</div>
            <button 
                class="publish-btn"
                :disabled="!localContent.trim() || publishing"
                @click="handlePublish"
            >发布</button>
        </div>

        <!-- 表情弹窗 -->
        <div class="emoji-picker" v-if="showEmojiPicker">
            <div class="emoji-grid">
                <span 
                    class="emoji-item" 
                    v-for="emoji in emojiList" 
                    :key="emoji"
                    @click="insertEmoji(emoji)"
                >{{ emoji }}</span>
            </div>
        </div>
    </div>
</template>

<script>
import { previewLink } from '@/apis/pins'
import { uploadFile } from '@/common/oss_upload'
import { toast } from '@/utils/toast'

export default {
    name: 'PinsPublishBox',
    props: {
        value: { type: String, default: '' },
        selectedCircle: { type: Object, default: null },
        selectedTopic: { type: Object, default: null },
        publishing: { type: Boolean, default: false },
        // 是否在创作者中心模式下（用于遮罩等差异化处理）
        isCreatorMode: { type: Boolean, default: false }
    },
    data() {
        return {
            localContent: this.value || '',
            localCircle: this.selectedCircle,
            localTopic: this.selectedTopic,
            localImages: [],
            localLinkPreview: null,
            showLinkInput: false,
            linkUrl: '',
            showEmojiPicker: false,
            topicRemoved: false,
            emojiList: [
                '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂',
                '😉', '😌', '😍', '🥰', '😘', '😗', '😋', '😛', '😜', '🤪',
                '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑',
                '😶', '😏', '😒', '🙄', '😬', '😪', '😮', '🤯', '😴', '🤤',
                '😭', '😤', '😡', '🤬', '😈', '💀', '💩', '🤡', '👻', '👽',
                '🤖', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾',
                '🙈', '🙉', '🙊', '💋', '💌', '💘', '💝', '💖', '💗', '💓',
                '💞', '💕', '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
                '💯', '🔥', '⭐', '👍', '👎', '👏', '🙌', '🤝', '💪', '✍️',
                '🎉', '🎊', '🎈', '✨', '🌟', '💥', '☀️', '🌙', '⚡', '💧'
            ]
        }
    },
    computed: {
        placeholderText() {
            if (this.localTopic) {
                return '说点什么吧...'
            }
            return '快来和逐友一起分享新鲜事！告诉你个小秘密，发布沸点时添加圈子和话题会被更多逐友看到哦~'
        },
        topicDisplayName() {
            if (!this.localTopic) return ''
            const name = this.localTopic.name || ''
            // 如果名字已经包含 #，则直接使用，否则包装
            if (name.startsWith('#') && name.endsWith('#')) {
                return name
            }
            return '#' + name + '#'
        }
    },
    watch: {
        value(val) {
            this.localContent = val || ''
        },
        selectedCircle(val) {
            this.localCircle = val
        },
        selectedTopic(val) {
            this.localTopic = val
            if (val) {
                this.topicRemoved = false
            }
        },
        localContent(val) {
            this.$emit('input', val)
        }
    },
    methods: {
        escapeHtml(str) {
            if (!str) return ''
            return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;')
        },
        onPublishInput() {
            if (this.showEmojiPicker) {
                this.showEmojiPicker = false
            }
        },
        onTextareaKeydown(e) {
            // 当有话题标签且光标在开头时，按 delete/backspace 移除话题
            if (this.localTopic && (e.key === 'Backspace' || e.key === 'Delete')) {
                const textarea = this.$refs.publishTextarea
                if (textarea && textarea.selectionStart === 0 && textarea.selectionEnd === 0) {
                    e.preventDefault()
                    this.removeTopic()
                }
            }
            // 按 Esc 键关闭表情面板
            if (e.key === 'Escape' && this.showEmojiPicker) {
                this.showEmojiPicker = false
            }
        },
        onSelectTopic() {
            this.$emit('select-topic')
        },
        removeTopic() {
            this.localTopic = null
            this.topicRemoved = true
            this.$emit('update:selectedTopic', null)
            // 3秒后自动隐藏提示
            setTimeout(() => {
                this.topicRemoved = false
            }, 3000)
        },
        insertEmoji(emoji) {
            const textarea = this.$refs.publishTextarea
            if (textarea) {
                const start = textarea.selectionStart
                const end = textarea.selectionEnd
                const before = this.localContent.substring(0, start)
                const after = this.localContent.substring(end)
                this.localContent = before + emoji + after
                this.$nextTick(() => {
                    const newPos = start + emoji.length
                    textarea.selectionStart = newPos
                    textarea.selectionEnd = newPos
                    textarea.focus()
                })
            } else {
                this.localContent += emoji
            }
            this.showEmojiPicker = false
        },
        triggerImageUpload() {
            if (this.localLinkPreview) return
            this.$refs.imageInput.click()
        },
        async handleImageUpload(e) {
            const file = e.target.files[0]
            if (!file) return
            try {
                const url = await uploadFile(file)
                this.localImages.push(url)
                this.removeLink()
                this.$emit('update:images', [...this.localImages])
            } catch (e) {
                toast('图片上传失败', 2)
            } finally {
                this.$refs.imageInput.value = ''
            }
        },
        removeImage(idx) {
            this.localImages.splice(idx, 1)
            this.$emit('update:images', [...this.localImages])
        },
        toggleLinkInput() {
            if (this.localImages.length > 0) return
            this.showLinkInput = !this.showLinkInput
            if (!this.showLinkInput) {
                this.linkUrl = ''
            }
        },
        async fetchLinkPreview() {
            const url = this.linkUrl.trim()
            if (!url) return
            try {
                const res = await previewLink({ url })
                if (res && res.code === 200 && res.data) {
                    this.localLinkPreview = res.data
                    this.localImages = []
                    this.$emit('update:images', [])
                    this.$emit('update:linkPreview', this.localLinkPreview)
                    this.showLinkInput = false
                    this.linkUrl = ''
                } else {
                    toast('链接解析失败', 2)
                }
            } catch (e) {
                toast('链接解析失败', 2)
            }
        },
        removeLink() {
            this.localLinkPreview = null
            this.linkUrl = ''
            this.showLinkInput = false
            this.$emit('update:linkPreview', null)
        },
        handlePublish() {
            const data = {
                content: this.localContent.trim()
            }
            if (this.localCircle) {
                data.circleId = this.localCircle.id
                data.circleName = this.localCircle.name
            }
            if (this.localTopic) {
                data.topicId = this.localTopic.id
                data.topicTags = [this.topicDisplayName]
                data.topicName = this.localTopic.name
            }
            if (this.localImages.length > 0) {
                data.imageUrls = this.localImages
            }
            if (this.localLinkPreview) {
                data.linkUrl = this.localLinkPreview.url
                data.linkTitle = this.localLinkPreview.title || this.localLinkPreview.domain
            }
            this.$emit('publish', data)
        },
        // 供父组件调用的重置方法
        reset() {
            this.localContent = ''
            this.localImages = []
            this.localLinkPreview = null
            this.showLinkInput = false
            this.linkUrl = ''
            this.showEmojiPicker = false
            this.localCircle = null
            this.localTopic = null
            this.topicRemoved = false
        }
    }
}
</script>

<style lang="less" scoped>
.publish-box {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    position: relative;
}

.topic-tag-wrapper {
    margin-bottom: 8px;
}

.topic-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    background: #eaf2ff;
    color: #1e80ff;
    font-size: 13px;
    font-weight: 500;
    border-radius: 4px;
    border: 1px solid #d6e4ff;
    user-select: none;
}

.topic-tag-remove {
    font-family: fontawesome;
    font-size: 12px;
    cursor: pointer;
    color: #8a919f;
    &:hover {
        color: #ff4d4f;
    }
}

.topic-removed-hint {
    margin-bottom: 8px;
    font-size: 12px;
    color: #8a919f;
    animation: fadeOut 3s forwards;
}

@keyframes fadeOut {
    0%, 70% { opacity: 1; }
    100% { opacity: 0; }
}

.publish-input {
    width: 100%;
    height: 80px;
    border: none;
    resize: none;
    font-size: 14px;
    line-height: 1.6;
    color: #252933;
    &::placeholder {
        color: #c4c9d1;
    }
    &:focus {
        outline: none;
    }
}

.publish-extras {
    margin-top: 8px;
}

.publish-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}

.publish-image-item {
    position: relative;
    width: 80px;
    height: 80px;
}

.publish-image-preview {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
}

.publish-image-remove {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 18px;
    height: 18px;
    background: #ff4d4f;
    color: #fff;
    font-family: fontawesome;
    font-size: 10px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    line-height: 1;
}

.publish-link-preview {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    background: #f0f5ff;
    border-radius: 4px;
    font-size: 13px;
}

.publish-link-domain {
    color: #1e80ff;
    font-weight: 500;
}

.publish-link-title {
    color: #515767;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.publish-link-remove {
    font-family: fontawesome;
    color: #8a919f;
    cursor: pointer;
    font-size: 12px;
    &:hover {
        color: #ff4d4f;
    }
}

.publish-link-input {
    display: flex;
    gap: 8px;
    margin-top: 8px;
    align-items: center;
}

.link-url-input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 13px;
    outline: none;
    &:focus {
        border-color: #1e80ff;
    }
}

.link-add-btn {
    padding: 6px 16px;
    border: none;
    border-radius: 4px;
    background: #1e80ff;
    color: #fff;
    font-size: 13px;
    cursor: pointer;
    &:hover {
        background: #4096ff;
    }
    &:disabled {
        background: #c4c9d1;
        cursor: not-allowed;
    }
}

.link-cancel-btn {
    padding: 6px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #515767;
    font-size: 13px;
    cursor: pointer;
    &:hover {
        background: #f7f8fa;
    }
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
    gap: 8px;
    flex-wrap: wrap;
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
    &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
        &:hover {
            color: #8a919f;
            background: transparent;
        }
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
    &:hover {
        background: #4096ff;
    }
    &:disabled {
        background: #c4c9d1;
        cursor: not-allowed;
    }
}

/* 表情弹窗 */
.emoji-picker {
    position: absolute;
    bottom: 100%;
    left: 16px;
    background: #fff;
    border: 1px solid #e4e6eb;
    border-radius: 8px;
    padding: 10px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.12);
    z-index: 100;
    margin-bottom: 8px;
    width: 320px;
}

.emoji-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    max-height: 200px;
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
    &:hover {
        background: #f0f5ff;
    }
}
</style>