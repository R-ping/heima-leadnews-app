<template>
    <div class="notification-page">
        <div class="art-top"><HomeBar/></div>
        <div class="notification-content">
            <div class="tabs-bar">
                <div 
                    class="tab-item" 
                    :class="{ 'active': activeTab === 'comment' }"
                    @click="activeTab = 'comment'"
                >
                    评论
                </div>
                <div 
                    class="tab-item" 
                    :class="{ 'active': activeTab === 'like' }"
                    @click="activeTab = 'like'"
                >
                    赞和收藏
                </div>
                <div 
                    class="tab-item" 
                    :class="{ 'active': activeTab === 'follow' }"
                    @click="activeTab = 'follow'"
                >
                    新增粉丝
                </div>
                <div 
                    class="tab-item" 
                    :class="{ 'active': activeTab === 'message' }"
                    @click="activeTab = 'message'"
                >
                    私信
                </div>
                <div 
                    class="tab-item" 
                    :class="{ 'active': activeTab === 'system' }"
                    @click="activeTab = 'system'"
                >
                    系统通知
                </div>
            </div>

            <div class="content-area">
                <div v-if="activeTab === 'comment'" class="tab-content">
                    <div class="notification-list">
                        <div class="notification-item" v-for="item in commentList" :key="item.id">
                            <img :src="item.userAvatar || defaultAvatar" class="notify-avatar" alt="avatar">
                            <div class="notify-content">
                                <div class="notify-header">
                                    <span class="notify-user">{{ item.userName }}</span>
                                    <span class="notify-action">{{ item.action }}</span>
                                    <span class="notify-time">{{ formatTime(item.time) }}</span>
                                </div>
                                <div class="notify-text">{{ item.content }}</div>
                                <div class="notify-article">{{ item.articleTitle }}</div>
                                <div class="notify-actions">
                                    <button class="action-btn" :class="{ 'active': item.isLiked }" @click="toggleLike(item)">
                                        <span class="action-icon">&#xf087;</span>
                                        <span class="action-text">{{ item.likeCount }}</span>
                                    </button>
                                    <button class="action-btn" @click="handleReply(item)">
                                        <span class="action-icon">&#xf112;</span>
                                        <span class="action-text">回复</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeTab === 'like'" class="tab-content">
                    <div class="notification-list">
                        <div class="notification-item" v-for="item in likeList" :key="item.id">
                            <img :src="item.userAvatar || defaultAvatar" class="notify-avatar" alt="avatar">
                            <div class="notify-content">
                                <div class="notify-header">
                                    <span class="notify-user">{{ item.userName }}</span>
                                    <span class="notify-action">{{ item.action }}</span>
                                    <span class="notify-time">{{ formatTime(item.time) }}</span>
                                </div>
                                <div class="notify-text" v-if="item.content">{{ item.content }}</div>
                                <div class="notify-article">{{ item.articleTitle }}</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeTab === 'follow'" class="tab-content">
                    <div class="notification-list">
                        <div class="notification-item" v-for="item in followList" :key="item.id">
                            <img :src="item.userAvatar || defaultAvatar" class="notify-avatar" alt="avatar">
                            <div class="notify-content">
                                <div class="notify-header">
                                    <span class="notify-user">{{ item.userName }}</span>
                                    <span class="notify-action">{{ item.action }}</span>
                                    <span class="notify-time">{{ formatTime(item.time) }}</span>
                                </div>
                            </div>
                            <button 
                                class="follow-btn" 
                                :class="{ 'active': item.isFollowed }"
                                @click="toggleFollow(item)"
                            >
                                {{ item.isFollowed ? '已关注' : '关注' }}
                            </button>
                        </div>
                    </div>
                </div>

                <div v-if="activeTab === 'message'" class="tab-content">
                    <div class="message-layout">
                        <div class="message-sidebar">
                            <div class="search-box">
                                <input type="text" class="search-input" placeholder="搜索联系人" v-model="searchKeyword">
                            </div>
                            <div class="contact-list">
                                <div 
                                    class="contact-item" 
                                    :class="{ 'active': selectedContact && selectedContact.id === contact.id }"
                                    v-for="contact in filteredContacts"
                                    :key="contact.id"
                                    @click="selectContact(contact)"
                                >
                                    <img :src="contact.avatar || defaultAvatar" class="contact-avatar" alt="avatar">
                                    <div class="contact-info">
                                        <div class="contact-name">{{ contact.name }}</div>
                                        <div class="contact-last-message">{{ contact.lastMessage }}</div>
                                    </div>
                                    <div class="contact-time">{{ formatTime(contact.lastTime) }}</div>
                                </div>
                            </div>
                        </div>
                        <div class="message-chat">
                            <div v-if="!selectedContact" class="empty-chat">
                                <span class="empty-icon">💬</span>
                                <span class="empty-text">选择一个联系人开始聊天</span>
                            </div>
                            <div v-else class="chat-content">
                                <div class="chat-header">
                                    <span class="chat-title">{{ selectedContact.name }}</span>
                                </div>
                                <div class="chat-messages" ref="chatMessages">
                                    <div 
                                        class="message-item" 
                                        :class="{ 'is-self': msg.isSelf }"
                                        v-for="msg in selectedContact.messages"
                                        :key="msg.id"
                                    >
                                        <img :src="msg.isSelf ? (userAvatar || defaultAvatar) : (selectedContact.avatar || defaultAvatar)" class="msg-avatar" alt="avatar">
                                        <div class="msg-content">
                                            <div class="msg-text">{{ msg.content }}</div>
                                            <div class="msg-time">{{ formatTime(msg.time) }}</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="chat-warning" v-if="!selectedContact.isMutualFollow">
                                    由于对方并未关注你，在收到对方回复之前，你最多只能发送1条文字消息
                                </div>
                                <div class="chat-input-area">
                                    <button class="input-icon-btn">😊</button>
                                    <button class="input-icon-btn">📷</button>
                                    <input 
                                        type="text" 
                                        class="chat-input" 
                                        placeholder="输入消息..." 
                                        v-model="messageInput"
                                        @keyup.enter="sendMessage"
                                    >
                                    <button class="send-btn" @click="sendMessage">发送</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="activeTab === 'system'" class="tab-content">
                    <div class="notification-list">
                        <div class="notification-item" v-for="item in systemList" :key="item.id">
                            <div class="notify-content system-content">
                                <div class="notify-header">
                                    <span class="notify-time">{{ formatTime(item.time) }}</span>
                                </div>
                                <div class="notify-text">{{ item.content }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import defaultAvatar from '@/static/images/creator/avatar.jpg'
import { toast } from '@/utils/toast'

export default {
    name: 'Notification',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'comment',
            searchKeyword: '',
            selectedContact: null,
            messageInput: '',
            userAvatar: '',
            commentList: [
                {
                    id: 1,
                    userAvatar: '',
                    userName: '用户85499312907',
                    action: '回复了你在文章的评论',
                    content: '22222',
                    articleTitle: '《智谱GLM-5这次开源，让高级程序员也危险了...》',
                    time: Date.now() - 86400000,
                    isLiked: false,
                    likeCount: 0
                },
                {
                    id: 2,
                    userAvatar: '',
                    userName: '用户85499312907',
                    action: '评论了你在文章',
                    content: '35岁程序员的春天来了',
                    articleTitle: '《智谱GLM-5这次开源，让高级程序员也危险了...》',
                    time: Date.now() - 86400000 * 2,
                    isLiked: true,
                    likeCount: 5
                }
            ],
            likeList: [
                {
                    id: 1,
                    userAvatar: '',
                    userName: '用户85499312907',
                    action: '赞了你在文章的评论',
                    content: '1111',
                    articleTitle: '《智谱GLM-5这次开源，让高级程序员也危险了...》',
                    time: Date.now() - 86400000
                },
                {
                    id: 2,
                    userAvatar: '',
                    userName: '技术爱好者',
                    action: '赞了你的文章',
                    articleTitle: '《Vue3组合式API入门指南》',
                    time: Date.now() - 86400000 * 2
                },
                {
                    id: 3,
                    userAvatar: '',
                    userName: '前端工程师',
                    action: '收藏了你的文章',
                    articleTitle: '《TypeScript高级类型技巧》',
                    time: Date.now() - 86400000 * 3
                }
            ],
            followList: [
                {
                    id: 1,
                    userAvatar: '',
                    userName: '用户85499312907',
                    action: '关注了你',
                    time: Date.now() - 86400000,
                    isFollowed: false
                },
                {
                    id: 2,
                    userAvatar: '',
                    userName: '新来的小伙伴',
                    action: '关注了你',
                    time: Date.now() - 86400000 * 2,
                    isFollowed: true
                }
            ],
            contacts: [
                {
                    id: 1,
                    name: '程序员小站',
                    avatar: '',
                    lastMessage: '',
                    lastTime: Date.now(),
                    isMutualFollow: false,
                    sentCount: 0,
                    messages: []
                },
                {
                    id: 2,
                    name: '技术交流群',
                    avatar: '',
                    lastMessage: '今天有什么技术分享？',
                    lastTime: Date.now() - 3600000,
                    isMutualFollow: true,
                    messages: [
                        { id: 1, content: '今天有什么技术分享？', time: Date.now() - 3600000, isSelf: false },
                        { id: 2, content: '我准备分享一下Vue3的新特性', time: Date.now() - 3500000, isSelf: true }
                    ]
                }
            ],
            systemList: [
                {
                    id: 1,
                    content: '欢迎加入黑马头条！',
                    time: Date.now() - 86400000 * 7
                },
                {
                    id: 2,
                    content: '您的文章《Vue3组合式API入门指南》已审核通过',
                    time: Date.now() - 86400000 * 2
                },
                {
                    id: 3,
                    content: '系统将于今晚22:00进行维护升级，预计持续2小时',
                    time: Date.now() - 86400000
                }
            ]
        }
    },
    computed: {
        defaultAvatar() {
            return defaultAvatar
        },
        filteredContacts() {
            if (!this.searchKeyword) return this.contacts
            return this.contacts.filter(c => c.name.includes(this.searchKeyword))
        }
    },
    methods: {
        formatTime(timestamp) {
            const now = Date.now()
            const diff = now - timestamp
            const hours = Math.floor(diff / 3600000)
            const days = Math.floor(diff / 86400000)
            const months = Math.floor(diff / 2592000000)
            
            if (hours < 1) return '刚刚'
            if (hours < 24) return hours + '小时前'
            if (days < 30) return days + '天前'
            if (months < 12) return months + '个月前'
            return Math.floor(months / 12) + '年前'
        },
        toggleLike(item) {
            item.isLiked = !item.isLiked
            item.likeCount += item.isLiked ? 1 : -1
        },
        handleReply(item) {
            toast('回复功能开发中', 2)
        },
        toggleFollow(item) {
            item.isFollowed = !item.isFollowed
            toast(item.isFollowed ? '已关注' : '已取消关注', 2)
        },
        selectContact(contact) {
            this.selectedContact = contact
        },
        sendMessage() {
            if (!this.messageInput.trim()) return
            
            const contact = this.selectedContact
            if (!contact) return
            
            if (!contact.isMutualFollow && contact.sentCount >= 1) {
                toast('对方未关注你，你最多只能发送1条消息', 2)
                return
            }
            
            const newMsg = {
                id: Date.now(),
                content: this.messageInput.trim(),
                time: Date.now(),
                isSelf: true
            }
            contact.messages.push(newMsg)
            contact.sentCount++
            contact.lastMessage = this.messageInput.trim()
            contact.lastTime = Date.now()
            
            this.messageInput = ''
            
            setTimeout(() => {
                const chatMessages = this.$refs.chatMessages
                if (chatMessages) {
                    chatMessages.scrollTop = chatMessages.scrollHeight
                }
            }, 100)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.notification-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.notification-content {
    max-width: 900px;
    margin: 0 auto;
    padding: 24px;
}

.tabs-bar {
    display: flex;
    background: #fff;
    border-radius: 8px;
    padding: 0 16px;
    margin-bottom: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.tab-item {
    padding: 16px 24px;
    font-size: 14px;
    color: #515767;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;
    &:hover {
        color: #1e80ff;
    }
    &.active {
        color: #1e80ff;
        &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 24px;
            height: 3px;
            background: #1e80ff;
            border-radius: 2px;
        }
    }
}

.content-area {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    min-height: 400px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.notification-list {
    padding: 8px 0;
}

.notification-item {
    display: flex;
    gap: 12px;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
        border: none;
    }
}

.notify-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.notify-content {
    flex: 1;
}

.notify-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.notify-user {
    font-size: 14px;
    font-weight: 500;
    color: #252933;
}

.notify-action {
    font-size: 14px;
    color: #515767;
}

.notify-time {
    font-size: 12px;
    color: #8a919f;
    margin-left: auto;
}

.notify-text {
    font-size: 14px;
    color: #252933;
    line-height: 1.5;
    margin-bottom: 4px;
}

.notify-article {
    font-size: 13px;
    color: #1e80ff;
    cursor: pointer;
    &:hover {
        text-decoration: underline;
    }
}

.notify-actions {
    display: flex;
    gap: 16px;
    margin-top: 12px;
}

.action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    border: none;
    background: transparent;
    font-size: 13px;
    color: #8a919f;
    cursor: pointer;
    transition: color 0.2s;
    &:hover {
        color: #1e80ff;
    }
    &.active {
        color: #ff4d4f;
    }
}

.action-icon {
    font-family: fontawesome;
    font-size: 14px;
}

.action-text {
    font-size: 13px;
}

.follow-btn {
    padding: 6px 16px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    background: #fff;
    color: #515767;
    font-size: 13px;
    cursor: pointer;
    flex-shrink: 0;
    &.active {
        background: #8a919f;
        color: #fff;
        border-color: #8a919f;
    }
    &:hover:not(.active) {
        border-color: #1e80ff;
        color: #1e80ff;
    }
}

.message-layout {
    display: flex;
    height: 500px;
    border: 1px solid #f2f3f5;
    border-radius: 8px;
    overflow: hidden;
}

.message-sidebar {
    width: 280px;
    border-right: 1px solid #f2f3f5;
    display: flex;
    flex-direction: column;
}

.search-box {
    padding: 12px;
    border-bottom: 1px solid #f2f3f5;
}

.search-input {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #e4e6eb;
    border-radius: 20px;
    font-size: 13px;
    outline: none;
    &:focus {
        border-color: #1e80ff;
    }
}

.contact-list {
    flex: 1;
    overflow-y: auto;
}

.contact-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover {
        background: #f7f8fa;
    }
    &.active {
        background: #eaf2ff;
    }
}

.contact-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
}

.contact-info {
    flex: 1;
    min-width: 0;
}

.contact-name {
    font-size: 14px;
    color: #252933;
    margin-bottom: 2px;
}

.contact-last-message {
    font-size: 12px;
    color: #8a919f;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.contact-time {
    font-size: 12px;
    color: #c4c9d1;
}

.message-chat {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.empty-chat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

.empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.empty-text {
    font-size: 14px;
    color: #8a919f;
}

.chat-content {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.chat-header {
    padding: 16px;
    border-bottom: 1px solid #f2f3f5;
    background: #f7f8fa;
}

.chat-title {
    font-size: 15px;
    font-weight: 600;
    color: #252933;
}

.chat-messages {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
}

.message-item {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    &.is-self {
        flex-direction: row-reverse;
        .msg-content {
            align-items: flex-end;
            .msg-text {
                background: #1e80ff;
                color: #fff;
                border-radius: 12px 12px 0 12px;
            }
        }
    }
}

.msg-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.msg-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 60%;
}

.msg-text {
    padding: 10px 14px;
    background: #f7f8fa;
    border-radius: 12px 12px 12px 0;
    font-size: 14px;
    line-height: 1.5;
    word-break: break-all;
}

.msg-time {
    font-size: 12px;
    color: #8a919f;
}

.chat-warning {
    padding: 8px 16px;
    background: #fff7e6;
    color: #fa8c16;
    font-size: 12px;
    text-align: center;
}

.chat-input-area {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-top: 1px solid #f2f3f5;
    background: #fff;
}

.input-icon-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    font-size: 20px;
    cursor: pointer;
    border-radius: 50%;
    &:hover {
        background: #f7f8fa;
    }
}

.chat-input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid #e4e6eb;
    border-radius: 20px;
    font-size: 14px;
    outline: none;
    &:focus {
        border-color: #1e80ff;
    }
}

.send-btn {
    padding: 10px 24px;
    border: none;
    border-radius: 20px;
    background: #1e80ff;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
    &:hover {
        background: #4096ff;
    }
}

.system-content {
    padding-left: 0;
}

@media screen and (max-width: 768px) {
    .message-layout {
        flex-direction: column;
        height: auto;
    }
    .message-sidebar {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid #f2f3f5;
        max-height: 200px;
    }
}
</style>