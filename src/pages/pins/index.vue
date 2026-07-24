<template>
    <div class="pins-page">
        <div class="art-top"><HomeBar/></div>
        
        <div class="pins-content">
            <!-- 左侧边栏 -->
            <div class="pins-sidebar">
                <div class="sidebar-section">
                    <div 
                        class="sidebar-item" 
                        :class="{ 'active': activeTab === 'latest' }"
                        @click="activeTab = 'latest'"
                    >
                        <span class="sidebar-icon">&#xf01e;</span>
                        <span class="sidebar-text">最新</span>
                    </div>
                    <div 
                        class="sidebar-item" 
                        :class="{ 'active': activeTab === 'hot' }"
                        @click="activeTab = 'hot'"
                    >
                        <span class="sidebar-icon">&#xf06d;</span>
                        <span class="sidebar-text">最热</span>
                    </div>
                    <div 
                        class="sidebar-item" 
                        :class="{ 'active': activeTab === 'follow' }"
                        @click="activeTab = 'follow'"
                    >
                        <span class="sidebar-icon">&#xf0c0;</span>
                        <span class="sidebar-text">关注</span>
                    </div>
                </div>

                <div class="sidebar-section">
                    <div class="section-title">我的圈子</div>
                    <div 
                        class="sidebar-item circle-item"
                        v-for="circle in myCircles.slice(0, 5)"
                        :key="circle.id"
                        :class="{ 'active': activeCircle === circle.id }"
                        @click="selectCircle(circle)"
                    >
                        <span class="sidebar-text">{{ circle.name }}</span>
                    </div>
                    <div 
                        class="sidebar-item more-item"
                        v-if="myCircles.length > 5"
                        @click="showMyCirclesModal = true"
                    >
                        <span class="sidebar-text">更多</span>
                    </div>
                </div>

                <div class="sidebar-section">
                    <div class="section-title">推荐圈子</div>
                    <div 
                        class="sidebar-item circle-item"
                        v-for="circle in recommendedCircles.slice(0, 5)"
                        :key="circle.id"
                        :class="{ 'active': activeCircle === circle.id }"
                        @click="selectCircle(circle)"
                    >
                        <span class="sidebar-text">{{ circle.name }}</span>
                    </div>
                    <div 
                        class="sidebar-item more-item"
                        @click="goToCircles"
                    >
                        <span class="sidebar-text">更多</span>
                    </div>
                </div>
            </div>

            <!-- 右侧主内容区 -->
            <div class="pins-main">
                <!-- 发布框 -->
                <div class="publish-box">
                    <textarea 
                        class="publish-input"
                        v-model="publishContent"
                        placeholder="#新人报道#"
                        maxlength="1000"
                        @input="onPublishInput"
                    ></textarea>
                    <div class="publish-footer">
                        <div class="publish-actions">
                            <button class="action-btn">😊</button>
                            <button class="action-btn">📷</button>
                            <button class="action-btn">🔗</button>
                            <button 
                                class="action-btn circle-btn"
                                @click="showCircleSelector = true"
                            >
                                <span class="action-icon">&#xf02e;</span>
                                <span>{{ selectedCircle ? selectedCircle.name : '请选择圈子' }}</span>
                            </button>
                            <button 
                                class="action-btn topic-btn"
                                @click="showTopicSelector = true"
                            >
                                <span class="action-icon">&#xf02b;</span>
                                <span>{{ selectedTopic ? selectedTopic.name : '话题' }}</span>
                            </button>
                        </div>
                        <div class="publish-count">{{ publishContent.length }}/1000</div>
                        <button 
                            class="publish-btn"
                            :disabled="!publishContent.trim()"
                            @click="publishPins"
                        >发布</button>
                    </div>
                </div>

                <!-- 帖子列表 -->
                <div class="pins-list">
                    <div class="pins-item" v-for="pins in pinsList" :key="pins.id">
                        <img :src="pins.userAvatar || defaultAvatar" class="pins-avatar" alt="avatar">
                        <div class="pins-content-area">
                            <div class="pins-header">
                                <span class="pins-user">{{ pins.userName }}</span>
                                <span class="pins-time">{{ formatTime(pins.createdTime) }}</span>
                            </div>
                            <div class="pins-text">{{ pins.content }}</div>
                            <div class="pins-tags">
                                <span 
                                    class="pins-circle" 
                                    v-if="pins.circleName"
                                    @click="selectCircleByName(pins.circleName)"
                                >{{ pins.circleName }}</span>
                                <span 
                                    class="pins-topic" 
                                    v-if="pins.topicName"
                                >{{ pins.topicName }}</span>
                            </div>
                            <div class="pins-actions">
                                <button 
                                    class="pins-action-btn"
                                    @click="sharePins(pins)"
                                >
                                    <span class="action-icon">&#xf1e0;</span>
                                    <span>{{ pins.shareCount }}</span>
                                </button>
                                <button 
                                    class="pins-action-btn"
                                    @click="toggleComments(pins)"
                                >
                                    <span class="action-icon">&#xf075;</span>
                                    <span>{{ pins.commentCount }}</span>
                                </button>
                                <button 
                                    class="pins-action-btn"
                                    :class="{ 'active': pins.isLiked }"
                                    @click="toggleLike(pins)"
                                >
                                    <span class="action-icon">&#xf087;</span>
                                    <span>{{ pins.likeCount }}</span>
                                </button>
                            </div>

                            <!-- 评论区 -->
                            <div class="comments-section" v-if="pins.showComments">
                                <div class="comment-list">
                                    <div class="comment-item" v-for="comment in pins.comments" :key="comment.id">
                                        <img :src="comment.userAvatar || defaultAvatar" class="comment-avatar" alt="avatar">
                                        <div class="comment-content">
                                            <div class="comment-header">
                                                <span class="comment-user">{{ comment.userName }}</span>
                                                <span class="comment-time">{{ formatTime(comment.createdTime) }}</span>
                                            </div>
                                            <div class="comment-text">{{ comment.content }}</div>
                                            <div class="comment-actions">
                                                <button 
                                                    class="comment-action-btn"
                                                    :class="{ 'active': comment.isLiked }"
                                                    @click="toggleCommentLike(comment)"
                                                >
                                                    <span class="action-icon">&#xf087;</span>
                                                    <span>{{ comment.likeCount }}</span>
                                                </button>
                                                <button 
                                                    class="comment-action-btn"
                                                    @click="replyComment(pins, comment)"
                                                >
                                                    <span class="action-icon">&#xf112;</span>
                                                    <span>回复</span>
                                                </button>
                                            </div>

                                            <!-- 二级回复 -->
                                            <div class="reply-list" v-if="comment.replies && comment.replies.length">
                                                <div class="reply-item" v-for="reply in comment.replies" :key="reply.id">
                                                    <span class="reply-user">{{ reply.userName }}</span>
                                                    <span class="reply-text">回复 {{ reply.targetName }}：{{ reply.content }}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- 评论输入框 -->
                                <div class="comment-input-area">
                                    <input 
                                        type="text" 
                                        class="comment-input"
                                        :placeholder="replyingComment ? '回复 ' + replyingComment.userName : '平等表达，友善交流'"
                                        v-model="commentInput"
                                        @keyup.enter="submitComment(pins)"
                                    >
                                    <button 
                                        class="comment-submit-btn"
                                        :disabled="!commentInput.trim()"
                                        @click="submitComment(pins)"
                                    >发送</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 圈子选择弹窗 -->
        <div class="modal-overlay" v-if="showCircleSelector" @click="showCircleSelector = false">
            <div class="circle-modal" @click.stop>
                <div class="modal-header">
                    <span class="modal-title">选择圈子</span>
                    <button class="modal-close" @click="showCircleSelector = false">&#xf00d;</button>
                </div>
                <div class="circle-search">
                    <input type="text" class="search-input" placeholder="搜索圈子名称" v-model="circleSearchKeyword">
                </div>
                <div class="circle-categories">
                    <div 
                        class="category-item"
                        :class="{ 'active': selectedCategory === null }"
                        @click="selectedCategory = null"
                    >推荐圈子</div>
                    <div 
                        class="category-item"
                        v-for="category in categories"
                        :key="category.id"
                        :class="{ 'active': selectedCategory === category.id }"
                        @click="selectedCategory = category.id"
                    >{{ category.name }}</div>
                </div>
                <div class="circle-list">
                    <div 
                        class="circle-card"
                        v-for="circle in filteredCircles"
                        :key="circle.id"
                        :class="{ 'selected': selectedCircle && selectedCircle.id === circle.id }"
                        @click="selectCircleFromModal(circle)"
                    >
                        <div class="circle-icon">{{ circle.icon || '📌' }}</div>
                        <div class="circle-info">
                            <div class="circle-name">{{ circle.name }}</div>
                            <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
                        </div>
                        <div class="circle-check" v-if="selectedCircle && selectedCircle.id === circle.id">&#xf00c;</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="cancel-btn" @click="showCircleSelector = false">不选择圈子</button>
                    <button class="confirm-btn" @click="confirmCircleSelection">确认</button>
                </div>
            </div>
        </div>

        <!-- 话题选择弹窗 -->
        <div class="modal-overlay" v-if="showTopicSelector" @click="showTopicSelector = false">
            <div class="topic-modal" @click.stop>
                <div class="modal-header">
                    <span class="modal-title">选择话题</span>
                    <button class="modal-close" @click="showTopicSelector = false">&#xf00d;</button>
                </div>
                <div class="topic-search">
                    <input type="text" class="search-input" placeholder="搜索话题名称" v-model="topicSearchKeyword">
                </div>
                <div class="topic-list">
                    <div 
                        class="topic-item"
                        v-for="topic in filteredTopics"
                        :key="topic.id"
                        :class="{ 'selected': selectedTopic && selectedTopic.id === topic.id }"
                        @click="selectTopic(topic)"
                    >
                        <span class="topic-name">{{ topic.name }}</span>
                        <span class="topic-count">{{ topic.count }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 我的圈子更多弹窗 -->
        <div class="modal-overlay" v-if="showMyCirclesModal" @click="showMyCirclesModal = false">
            <div class="mycircles-modal" @click.stop>
                <div class="modal-header">
                    <span class="modal-title">我的圈子</span>
                    <button class="modal-close" @click="showMyCirclesModal = false">&#xf00d;</button>
                </div>
                <div class="mycircles-list">
                    <div 
                        class="mycircles-item"
                        v-for="circle in myCircles"
                        :key="circle.id"
                        :class="{ 'active': activeCircle === circle.id }"
                        @click="selectCircleFromMyCircles(circle)"
                    >
                        <span class="mycircles-name">{{ circle.name }}</span>
                        <span class="mycircles-arrow" v-if="activeCircle === circle.id">&#xf0da;</span>
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
    name: 'Pins',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'latest',
            activeCircle: null,
            selectedCircle: null,
            selectedTopic: null,
            selectedCategory: null,
            publishContent: '',
            commentInput: '',
            replyingComment: null,
            circleSearchKeyword: '',
            topicSearchKeyword: '',
            showCircleSelector: false,
            showTopicSelector: false,
            showMyCirclesModal: false,
            
            // 一级圈子分类
            categories: [
                { id: 1, name: '技术' },
                { id: 2, name: '职场' },
                { id: 3, name: '吃喝玩乐' },
                { id: 4, name: '资讯' },
                { id: 5, name: '理财' },
                { id: 6, name: '互动交流' },
                { id: 7, name: '书影音' },
                { id: 8, name: '生活' },
                { id: 9, name: '搞笑' },
                { id: 10, name: '情感' },
                { id: 11, name: '游戏' },
                { id: 12, name: '数码' }
            ],

            // 二级圈子
            circles: [
                { id: 13, parentId: 1, name: '大模型生态圈', description: '大模型技术交流', memberCount: 35000, pinsCount: 80000, icon: '🤖' },
                { id: 14, parentId: 1, name: '微服务生态圈', description: '微服务架构', memberCount: 25000, pinsCount: 60000, icon: '🏗️' },
                { id: 15, parentId: 1, name: '前端开发圈', description: '前端技术', memberCount: 40000, pinsCount: 120000, icon: '🎨' },
                { id: 16, parentId: 1, name: '服务端与架构', description: '服务端开发', memberCount: 30000, pinsCount: 70000, icon: '⚙️' },
                { id: 17, parentId: 1, name: '技术交流圈', description: '综合技术', memberCount: 50000, pinsCount: 150000, icon: '💬' },
                { id: 18, parentId: 2, name: '上班摸鱼', description: '职场摸鱼', memberCount: 40000, pinsCount: 100000, icon: '🐟' },
                { id: 19, parentId: 2, name: '内推招聘广场', description: '内推招聘', memberCount: 35000, pinsCount: 80000, icon: '📋' },
                { id: 20, parentId: 3, name: '美食探店', description: '美食分享', memberCount: 50000, pinsCount: 150000, icon: '🍜' },
                { id: 21, parentId: 3, name: '旅行日记', description: '旅行分享', memberCount: 45000, pinsCount: 120000, icon: '✈️' },
                { id: 22, parentId: 3, name: '什么值得买', description: '好物推荐', memberCount: 60000, pinsCount: 200000, icon: '🛒' },
                { id: 23, parentId: 4, name: '今日新鲜事', description: '新鲜资讯', memberCount: 50000, pinsCount: 120000, icon: '📰' },
                { id: 24, parentId: 5, name: '股票基金', description: '投资理财', memberCount: 30000, pinsCount: 60000, icon: '📈' }
            ],

            // 我的圈子
            myCircles: [
                { id: 13, name: 'AGI交流圈', memberCount: 35000, pinsCount: 80000 },
                { id: 14, name: '数据标注交流圈', memberCount: 25000, pinsCount: 60000 },
                { id: 15, name: '今天学到了', memberCount: 40000, pinsCount: 120000 },
                { id: 16, name: '今日新鲜事', memberCount: 50000, pinsCount: 120000 },
                { id: 17, name: '什么值得买', memberCount: 60000, pinsCount: 200000 },
                { id: 18, name: '前端开发圈', memberCount: 40000, pinsCount: 120000 },
                { id: 19, name: '大模型生态圈', memberCount: 35000, pinsCount: 80000 },
                { id: 20, name: '服务端与架构', memberCount: 30000, pinsCount: 70000 }
            ],

            // 推荐圈子
            recommendedCircles: [
                { id: 21, name: 'VibeLaunch', memberCount: 28000, pinsCount: 40000 },
                { id: 13, name: '大模型生态圈', memberCount: 35000, pinsCount: 80000 },
                { id: 16, name: '服务端与架构', memberCount: 30000, pinsCount: 70000 },
                { id: 15, name: '前端开发圈', memberCount: 40000, pinsCount: 120000 },
                { id: 22, name: '什么值得买', memberCount: 60000, pinsCount: 200000 },
                { id: 18, name: '上班摸鱼', memberCount: 40000, pinsCount: 100000 }
            ],

            // 话题列表
            topics: [
                { id: 1, name: '#新人报道#', count: 5000 },
                { id: 2, name: '#程序员脱单到底有多难#', count: 15000 },
                { id: 3, name: '#每日快讯#', count: 20000 },
                { id: 4, name: '#每日精选文章#', count: 18000 },
                { id: 5, name: '#日新计划#', count: 12000 },
                { id: 6, name: '#每天一个知识点#', count: 8000 },
                { id: 7, name: '#VueLaunch沸点秀#', count: 6000 },
                { id: 8, name: '#代码人生#', count: 10000 },
                { id: 9, name: '#优秀开源项目#', count: 7000 },
                { id: 10, name: '#技术交流#', count: 13000 },
                { id: 11, name: '#上班摸鱼#', count: 25000 },
                { id: 12, name: '#美食分享#', count: 30000 },
                { id: 13, name: '#旅行日记#', count: 18000 },
                { id: 14, name: '#投资理财#', count: 10000 },
                { id: 15, name: '#读书推荐#', count: 12000 }
            ],

            // 沸点帖子列表
            pinsList: [
                {
                    id: 1,
                    userAvatar: '',
                    userName: '大强同学',
                    content: '平时操作电脑总要点来点去，调音量开软件特别繁琐。想靠语音操控桌面，大多工具要么联网卡顿要么功能特别局限。这款知言桌面助手刚好能解决这些麻烦，主打语音优先操控电脑，大幅简化桌面各类操作。',
                    circleName: 'Vibe 编程交流圈',
                    topicName: '#VibeLaunch 沸点秀#',
                    likeCount: 9,
                    commentCount: 13,
                    shareCount: 1,
                    createdTime: Date.now() - 42 * 60 * 60 * 1000,
                    isLiked: false,
                    showComments: false,
                    comments: [
                        {
                            id: 1,
                            userAvatar: '',
                            userName: '程序员小站',
                            content: '这个看起来不错，试试！',
                            likeCount: 2,
                            createdTime: Date.now() - 40 * 60 * 60 * 1000,
                            isLiked: false,
                            replies: [
                                { id: 1, userName: '大强同学', targetName: '程序员小站', content: '谢谢支持！' }
                            ]
                        },
                        {
                            id: 2,
                            userAvatar: '',
                            userName: '技术爱好者',
                            content: '下载链接在哪里？',
                            likeCount: 0,
                            createdTime: Date.now() - 35 * 60 * 60 * 1000,
                            isLiked: false,
                            replies: []
                        }
                    ]
                },
                {
                    id: 2,
                    userAvatar: '',
                    userName: '灵魂画手_Panda',
                    content: '昨天和女朋友吵架了，她最近在备教师编压力大，昨天是面试结果出来的日子。我早上起来就一直在忙工作，忘了问她结果，她就生气了，说我不关心她。我该怎么办？',
                    circleName: '情感',
                    topicName: '#每日快闪#',
                    likeCount: 18,
                    commentCount: 237,
                    shareCount: 5,
                    createdTime: Date.now() - 2 * 24 * 60 * 60 * 1000,
                    isLiked: true,
                    showComments: false,
                    comments: [
                        {
                            id: 1,
                            userAvatar: '',
                            userName: '过来人',
                            content: '女孩子需要的是被重视的感觉，好好哄哄她',
                            likeCount: 15,
                            createdTime: Date.now() - 2 * 24 * 60 * 60 * 1000 + 1000,
                            isLiked: true,
                            replies: []
                        }
                    ]
                },
                {
                    id: 3,
                    userAvatar: '',
                    userName: '前端工程师小王',
                    content: 'Vue3的Composition API真的太香了！最近用它重构了一个老项目，代码量减少了一半，逻辑也清晰多了。推荐大家都试试！',
                    circleName: '前端开发圈',
                    topicName: '#技术交流#',
                    likeCount: 45,
                    commentCount: 8,
                    shareCount: 12,
                    createdTime: Date.now() - 3 * 24 * 60 * 60 * 1000,
                    isLiked: false,
                    showComments: false,
                    comments: []
                }
            ]
        }
    },
    computed: {
        defaultAvatar() {
            return defaultAvatar
        },
        filteredCircles() {
            let result = this.circles
            if (this.selectedCategory) {
                result = result.filter(c => c.parentId === this.selectedCategory)
            }
            if (this.circleSearchKeyword) {
                result = result.filter(c => c.name.includes(this.circleSearchKeyword))
            }
            return result
        },
        filteredTopics() {
            if (!this.topicSearchKeyword) return this.topics
            return this.topics.filter(t => t.name.includes(this.topicSearchKeyword))
        }
    },
    methods: {
        onPublishInput() {
        },
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
        selectCircle(circle) {
            this.activeCircle = circle.id
        },
        selectCircleByName(name) {
            const circle = this.circles.find(c => c.name === name)
            if (circle) {
                this.activeCircle = circle.id
            }
        },
        selectCircleFromModal(circle) {
            this.selectedCircle = circle
        },
        selectCircleFromMyCircles(circle) {
            this.activeCircle = circle.id
            this.showMyCirclesModal = false
        },
        confirmCircleSelection() {
            this.showCircleSelector = false
        },
        selectTopic(topic) {
            this.selectedTopic = topic
            this.showTopicSelector = false
        },
        goToCircles() {
            this.$router.push('/pins/circles')
        },
        publishPins() {
            if (!this.publishContent.trim()) return
            toast('发布成功！', 2)
            this.publishContent = ''
            this.selectedCircle = null
            this.selectedTopic = null
        },
        sharePins(pins) {
            toast('分享功能开发中', 2)
        },
        toggleComments(pins) {
            pins.showComments = !pins.showComments
        },
        toggleLike(pins) {
            pins.isLiked = !pins.isLiked
            pins.likeCount += pins.isLiked ? 1 : -1
        },
        toggleCommentLike(comment) {
            comment.isLiked = !comment.isLiked
            comment.likeCount += comment.isLiked ? 1 : -1
        },
        replyComment(pins, comment) {
            this.replyingComment = comment
        },
        submitComment(pins) {
            if (!this.commentInput.trim()) return
            
            const newComment = {
                id: Date.now(),
                userAvatar: '',
                userName: '当前用户',
                content: this.commentInput.trim(),
                likeCount: 0,
                createdTime: Date.now(),
                isLiked: false,
                replies: []
            }
            
            if (this.replyingComment) {
                this.replyingComment.replies.push({
                    id: Date.now(),
                    userName: '当前用户',
                    targetName: this.replyingComment.userName,
                    content: this.commentInput.trim()
                })
                this.replyingComment.replyCount = (this.replyingComment.replyCount || 0) + 1
                this.replyingComment = null
            } else {
                pins.comments.push(newComment)
                pins.commentCount++
            }
            
            this.commentInput = ''
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../styles/common';

.pins-page {
    min-height: 100vh;
    background: #f7f8fa;
}

.pins-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
    display: flex;
    gap: 24px;
}

/* 左侧边栏 */
.pins-sidebar {
    width: 200px;
    flex-shrink: 0;
}

.sidebar-section {
    background: #fff;
    border-radius: 8px;
    padding: 12px 0;
    margin-bottom: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.section-title {
    padding: 8px 16px;
    font-size: 12px;
    color: #8a919f;
    font-weight: 500;
}

.sidebar-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover {
        background: #f7f8fa;
    }
    &.active {
        background: #eaf2ff;
        .sidebar-text {
            color: #1e80ff;
        }
    }
}

.sidebar-icon {
    font-family: fontawesome;
    font-size: 16px;
    color: #8a919f;
}

.sidebar-text {
    font-size: 14px;
    color: #515767;
}

.circle-item {
    padding-left: 32px;
}

.more-item {
    padding-left: 32px;
    .sidebar-text {
        color: #1e80ff;
    }
}

/* 右侧主内容区 */
.pins-main {
    flex: 1;
    min-width: 0;
}

/* 发布框 */
.publish-box {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
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
    &:hover {
        background: #4096ff;
    }
    &:disabled {
        background: #c4c9d1;
        cursor: not-allowed;
    }
}

/* 帖子列表 */
.pins-list {
    background: #fff;
    border-radius: 8px;
    padding: 8px 0;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.pins-item {
    display: flex;
    gap: 12px;
    padding: 16px;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
        border: none;
    }
}

.pins-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.pins-content-area {
    flex: 1;
    min-width: 0;
}

.pins-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.pins-user {
    font-size: 14px;
    font-weight: 500;
    color: #252933;
}

.pins-time {
    font-size: 12px;
    color: #8a919f;
}

.pins-text {
    font-size: 14px;
    color: #252933;
    line-height: 1.6;
    margin-bottom: 8px;
    word-break: break-word;
}

.pins-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.pins-circle {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    background: #eaf2ff;
    color: #1e80ff;
    font-size: 12px;
    border-radius: 4px;
    cursor: pointer;
    &:hover {
        background: #d6e4ff;
    }
}

.pins-topic {
    display: inline-flex;
    align-items: center;
    padding: 2px 8px;
    background: #fff7e6;
    color: #fa8c16;
    font-size: 12px;
    border-radius: 4px;
}

.pins-actions {
    display: flex;
    align-items: center;
    gap: 24px;
}

.pins-action-btn {
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

/* 评论区 */
.comments-section {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f2f3f5;
}

.comment-list {
    margin-bottom: 16px;
}

.comment-item {
    display: flex;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
        border: none;
    }
}

.comment-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
}

.comment-content {
    flex: 1;
}

.comment-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
}

.comment-user {
    font-size: 13px;
    font-weight: 500;
    color: #252933;
}

.comment-time {
    font-size: 12px;
    color: #8a919f;
}

.comment-text {
    font-size: 14px;
    color: #252933;
    line-height: 1.5;
    margin-bottom: 8px;
}

.comment-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.comment-action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    border: none;
    background: transparent;
    font-size: 12px;
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

.reply-list {
    margin-top: 8px;
    padding-left: 24px;
    border-left: 2px solid #e4e6eb;
}

.reply-item {
    padding: 6px 0;
    font-size: 13px;
    line-height: 1.5;
}

.reply-user {
    color: #1e80ff;
    font-weight: 500;
    margin-right: 4px;
}

.reply-text {
    color: #515767;
}

/* 评论输入框 */
.comment-input-area {
    display: flex;
    gap: 12px;
}

.comment-input {
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

.comment-submit-btn {
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
    &:disabled {
        background: #c4c9d1;
        cursor: not-allowed;
    }
}

/* 弹窗 */
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

.circle-modal, .topic-modal, .mycircles-modal {
    background: #fff;
    border-radius: 8px;
    width: 600px;
    max-height: 70vh;
    overflow: hidden;
}

.mycircles-modal {
    width: 400px;
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

/* 圈子选择弹窗 */
.circle-search, .topic-search {
    padding: 12px 20px;
}

.search-input {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #e4e6eb;
    border-radius: 4px;
    font-size: 14px;
    outline: none;
    &:focus {
        border-color: #1e80ff;
    }
}

.circle-categories {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0 20px;
    border-bottom: 1px solid #f2f3f5;
}

.category-item {
    padding: 6px 12px;
    border-radius: 4px;
    font-size: 13px;
    color: #515767;
    cursor: pointer;
    background: #f7f8fa;
    &:hover {
        background: #eaf2ff;
        color: #1e80ff;
    }
    &.active {
        background: #1e80ff;
        color: #fff;
    }
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
    &:hover {
        background: #f7f8fa;
    }
    &.selected {
        background: #eaf2ff;
    }
}

.circle-icon {
    font-size: 24px;
}

.circle-info {
    flex: 1;
}

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
    &:hover {
        background: #f7f8fa;
    }
}

.confirm-btn {
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
}

/* 话题选择弹窗 */
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
    &:hover {
        background: #f7f8fa;
    }
    &.selected {
        background: #eaf2ff;
        .topic-name {
            color: #1e80ff;
        }
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

/* 我的圈子弹窗 */
.mycircles-list {
    padding: 8px 0;
}

.mycircles-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 20px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover {
        background: #f7f8fa;
    }
    &.active {
        background: #eaf2ff;
        .mycircles-name {
            color: #1e80ff;
        }
    }
}

.mycircles-name {
    font-size: 14px;
    color: #252933;
}

.mycircles-arrow {
    font-family: fontawesome;
    font-size: 14px;
    color: #1e80ff;
}

/* 响应式 */
@media screen and (max-width: 768px) {
    .pins-content {
        flex-direction: column;
        padding: 12px;
    }
    .pins-sidebar {
        width: 100%;
    }
    .sidebar-section {
        margin-bottom: 12px;
    }
    .circle-modal, .topic-modal {
        width: 90%;
    }
}
</style>