<template>
    <div class="circle-detail-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>
        
        <div class="detail-content">
            <div class="detail-main">
                <!-- 圈子信息 -->
                <div class="circle-info-card">
                    <div class="circle-icon">{{ circleInfo.icon || '📌' }}</div>
                    <div class="circle-info-body">
                        <h1 class="circle-name">{{ circleInfo.name }}</h1>
                        <p class="circle-desc" v-if="circleInfo.description">{{ circleInfo.description }}</p>
                        <div class="circle-stats">{{ circleInfo.memberCount || 0 }} 掘友 · {{ circleInfo.pinsCount || 0 }} 沸点</div>
                    </div>
                    <button
                        class="join-btn"
                        :class="{ joined: circleInfo.isJoined }"
                        @click="toggleJoin"
                    >
                        {{ circleInfo.isJoined ? '已加入' : '+ 加入' }}
                    </button>
                </div>

                <!-- 发沸点区域 -->
                <div class="publish-area" @click="showPublishBox = true">
                    <div class="publish-placeholder">快和掘友一起分享新鲜事！发布沸点时添加圈子和话题会被更多掘友看到哦~</div>
                    <button class="publish-btn">发布</button>
                </div>

                <!-- Tab 切换 -->
                <div class="feed-tabs">
                    <div 
                        class="feed-tab"
                        :class="{ active: activeTab === 'hot' }"
                        @click="switchTab('hot')"
                    >最热</div>
                    <div 
                        class="feed-tab"
                        :class="{ active: activeTab === 'new' }"
                        @click="switchTab('new')"
                    >最新</div>
                    <div 
                        class="feed-tab"
                        :class="{ active: activeTab === 'featured' }"
                        @click="switchTab('featured')"
                    >精选</div>
                </div>

                <!-- 沸点列表 -->
                <div class="feed-list">
                    <div class="feed-card" v-for="pin in feedList" :key="pin.id">
                        <div class="feed-header">
                            <img class="feed-avatar" :src="pin.userAvatar || defaultAvatar" />
                            <span class="feed-username">{{ pin.userName }}</span>
                            <span class="feed-time">{{ formatTime(pin.createdTime) }}</span>
                        </div>
                        <div class="feed-content">{{ pin.content }}</div>
                        <div class="feed-actions">
                            <span class="feed-action">👍 {{ pin.likeCount || 0 }}</span>
                            <span class="feed-action">💬 {{ pin.commentCount || 0 }}</span>
                        </div>
                    </div>
                    <div class="empty-state" v-if="feedList.length === 0 && !loading">
                        <p>暂无内容</p>
                    </div>
                    <div class="loading-state" v-if="loading">
                        <p>加载中...</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 发沸点弹窗 -->
        <div class="modal-overlay" v-if="showPublishBox" @click="showPublishBox = false">
            <div class="publish-modal" @click.stop>
                <div class="modal-header">
                    <span class="modal-title">发布沸点</span>
                    <button class="modal-close" @click="showPublishBox = false">✕</button>
                </div>
                <div class="publish-body">
                    <textarea 
                        class="publish-textarea" 
                        placeholder="快和掘友一起分享新鲜事！"
                        v-model="publishContent"
                    ></textarea>
                    <div class="publish-tag">
                        <span class="tag-label">圈子：</span>
                        <span class="tag-value">{{ circleInfo.name }}</span>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="cancel-btn" @click="showPublishBox = false">取消</button>
                    <button class="confirm-btn" @click="doPublish">发布</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import defaultAvatar from '@/static/images/creator/avatar.jpg'
import { toast } from '@/utils/toast'
import { getCircleDetail, joinCircle, leaveCircle, getCircleFeed } from '@/apis/circle'

export default {
    name: 'CircleDetail',
    components: { HomeBar },
    data() {
        return {
            circleInfo: {
                id: null,
                name: '',
                description: '',
                icon: '',
                memberCount: 0,
                pinsCount: 0,
                isJoined: false
            },
            activeTab: 'hot',
            feedList: [],
            feedPage: 1,
            feedSize: 20,
            hasMore: true,
            loading: false,
            showPublishBox: false,
            publishContent: ''
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        },
        defaultAvatar() {
            return defaultAvatar
        },
        circleId() {
            return this.$route.params.id
        }
    },
    mounted() {
        this.fetchCircleDetail()
        this.fetchFeed()
    },
    methods: {
        async fetchCircleDetail() {
            try {
                const res = await getCircleDetail(this.circleId)
                if (res && res.code === 200 && res.data) {
                    this.circleInfo = res.data
                }
            } catch (e) {
                toast('获取圈子信息失败', 2)
            }
        },
        async fetchFeed() {
            if (this.loading || !this.hasMore) return
            this.loading = true
            try {
                const res = await getCircleFeed(this.circleId, {
                    tab: this.activeTab,
                    page: this.feedPage,
                    size: this.feedSize
                })
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    const list = data.list || data.records || []
                    this.feedList = this.feedPage === 1 ? list : [...this.feedList, ...list]
                    this.hasMore = (data.has_more !== undefined) ? data.has_more : (list.length >= this.feedSize)
                }
            } catch (e) {} finally {
                this.loading = false
            }
        },
        switchTab(tab) {
            if (this.activeTab === tab) return
            this.activeTab = tab
            this.feedPage = 1
            this.feedList = []
            this.hasMore = true
            this.fetchFeed()
        },
        async toggleJoin() {
            try {
                if (this.circleInfo.isJoined) {
                    const res = await leaveCircle(this.circleId)
                    if (res && res.code === 200) {
                        this.circleInfo.isJoined = false
                        this.circleInfo.memberCount = Math.max(0, (this.circleInfo.memberCount || 1) - 1)
                        toast('已退出圈子', 2)
                    }
                } else {
                    const res = await joinCircle(this.circleId)
                    if (res && res.code === 200) {
                        this.circleInfo.isJoined = true
                        this.circleInfo.memberCount = (this.circleInfo.memberCount || 0) + 1
                        toast('加入成功', 2)
                    }
                }
            } catch (e) {
                toast('操作失败，请重试', 2)
            }
        },
        doPublish() {
            if (!this.publishContent.trim()) {
                toast('请输入内容', 2)
                return
            }
            toast('发布成功！', 2)
            this.publishContent = ''
            this.showPublishBox = false
        },
        formatTime(timestamp) {
            if (!timestamp) return ''
            const now = Date.now()
            const diff = now - new Date(timestamp).getTime()
            const hours = Math.floor(diff / 3600000)
            const days = Math.floor(diff / 86400000)
            const months = Math.floor(diff / 2592000000)
            if (hours < 1) return '刚刚'
            if (hours < 24) return hours + '小时前'
            if (days < 30) return days + '天前'
            if (months < 12) return months + '个月前'
            return Math.floor(months / 12) + '年前'
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.circle-detail-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.detail-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 24px;
}

.detail-main {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.circle-info-card {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding-bottom: 20px;
    border-bottom: 1px solid #e4e6eb;
    margin-bottom: 20px;
}

.circle-icon {
    font-size: 48px;
    width: 72px;
    height: 72px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f8fa;
    border-radius: 12px;
    border: 1px solid #e4e6eb;
    flex-shrink: 0;
}

.circle-info-body {
    flex: 1;
    min-width: 0;
}

.circle-name {
    font-size: 22px;
    font-weight: 600;
    color: #252933;
    margin: 0 0 8px;
}

.circle-desc {
    font-size: 14px;
    color: #515767;
    margin: 0 0 8px;
    line-height: 1.6;
}

.circle-stats {
    font-size: 13px;
    color: #8a919f;
}

.join-btn {
    padding: 8px 24px;
    border: 1px solid #1e80ff;
    border-radius: 6px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    flex-shrink: 0;
    transition: all 0.2s;
    &:hover {
        background: #1171ee;
    }
    &.joined {
        background: #fff;
        color: #8a919f;
        border-color: #8a919f;
    }
}

.publish-area {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #f7f8fa;
    border-radius: 8px;
    border: 1px solid #e4e6eb;
    cursor: pointer;
    margin-bottom: 20px;
    transition: border-color 0.2s;
    &:hover {
        border-color: #1e80ff;
    }
}

.publish-placeholder {
    flex: 1;
    font-size: 14px;
    color: #8a919f;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.publish-btn {
    padding: 6px 20px;
    border: none;
    border-radius: 4px;
    background: #1e80ff;
    color: #fff;
    font-size: 13px;
    cursor: pointer;
}

.feed-tabs {
    display: flex;
    gap: 24px;
    border-bottom: 1px solid #e4e6eb;
    margin-bottom: 16px;
}

.feed-tab {
    padding: 8px 0;
    font-size: 15px;
    color: #515767;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: all 0.2s;
    &:hover {
        color: #1e80ff;
    }
    &.active {
        color: #1e80ff;
        border-bottom-color: #1e80ff;
        font-weight: 500;
    }
}

.feed-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.feed-card {
    padding: 16px;
    background: #fff;
    border: 1px solid #e4e6eb;
    border-radius: 8px;
}

.feed-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
}

.feed-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
}

.feed-username {
    font-size: 14px;
    font-weight: 500;
    color: #252933;
}

.feed-time {
    font-size: 12px;
    color: #8a919f;
    margin-left: auto;
}

.feed-content {
    font-size: 14px;
    color: #252933;
    line-height: 1.6;
    margin-bottom: 10px;
}

.feed-actions {
    display: flex;
    gap: 20px;
}

.feed-action {
    font-size: 13px;
    color: #8a919f;
    cursor: pointer;
}

.empty-state, .loading-state {
    text-align: center;
    padding: 40px;
    color: #8a919f;
}

/* 弹窗样式 */
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
    width: 500px;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
}

.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #e4e6eb;
}

.modal-title {
    font-size: 16px;
    font-weight: 600;
    color: #252933;
}

.modal-close {
    border: none;
    background: none;
    font-size: 18px;
    color: #8a919f;
    cursor: pointer;
}

.publish-body {
    padding: 16px 20px;
}

.publish-textarea {
    width: 100%;
    min-height: 120px;
    border: 1px solid #e4e6eb;
    border-radius: 6px;
    padding: 12px;
    font-size: 14px;
    resize: vertical;
    outline: none;
    &:focus {
        border-color: #1e80ff;
    }
}

.publish-tag {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.tag-label {
    font-size: 13px;
    color: #8a919f;
}

.tag-value {
    font-size: 13px;
    color: #1e80ff;
    background: #eaf2ff;
    padding: 2px 8px;
    border-radius: 4px;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #e4e6eb;
}

.cancel-btn {
    padding: 8px 20px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #515767;
    font-size: 14px;
    cursor: pointer;
}

.confirm-btn {
    padding: 8px 20px;
    border: none;
    border-radius: 4px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
}
</style>