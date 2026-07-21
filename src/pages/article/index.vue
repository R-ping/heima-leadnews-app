<template>
    <div class="art-page" :class="{ 'immersive': isImmersive }">
        <div class="art-top"><HomeBar/></div>
        
        <div class="action-sidebar" v-if="!isImmersive">
            <div class="action-item" :class="{ 'hidden-item': !showMiniAvatar }" @click="handleFollow">
                <div class="avatar-wrap">
                    <img :src="authorAvatar || 'https://p3.pstatp.com/thumb/1480/7186611868'" class="author-mini-avatar" alt="avatar">
                    <span class="mini-follow-badge" v-if="!relation.isfollow">关注</span>
                    <span class="mini-follow-badge active" v-else>已关注</span>
                </div>
            </div>
            <div class="action-item" :class="{ 'active': relation.islike }" @click="like">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M2 20h2v-9H2v9zm20-9c0-1.1-.9-2-2-2h-3.17c-.53-1.4-1.53-2.56-2.83-3.09V4c0-1.66-1.34-3-3-3S8 2.34 8 4v1.91C5.94 6.56 4.5 8.69 4.5 11v6.17l-1.83 1.83L4.17 20h12.5c1.66 0 3.08-1.03 3.65-2.5H22v-6.5z"/></svg>
                </div>
                <div class="action-count">{{ likeCount }}</div>
            </div>
            <div class="action-item" @click="scrollToComment">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
                </div>
                <div class="action-count">{{ commentCount }}</div>
            </div>
            <div class="action-item" :class="{ 'active': relation.iscollection }" @click="handleCollect">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
                </div>
                <div class="action-count">{{ collectCount }}</div>
            </div>
            <div class="action-item" @click="handleShare">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/></svg>
                </div>
                <div class="action-count">分享</div>
            </div>
            <div class="action-item" @click="handleReport">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
                </div>
                <div class="action-count">举报</div>
            </div>
            <div class="action-item" :class="{ 'active': isImmersive }" @click="toggleImmersive">
                <div class="action-icon">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/></svg>
                </div>
                <div class="action-count">沉浸</div>
            </div>
        </div>

        <div class="main-wrapper">
            <div class="content-area">
                <div class="scroller" ref="scroller" @scroll="scroller" show-scrollbar="true">
                    <span class="title">{{ title }}</span>
                    <div class="author-header">
                        <div class="author-avatar">
                            <img :src="authorAvatar || 'https://p3.pstatp.com/thumb/1480/7186611868'" alt="avatar">
                        </div>
                        <div class="author-info">
                            <div class="author-name">{{ source }}</div>
                            <div class="publish-meta">
                                <span class="publish-time">{{ formatDate(date) }}</span>
                                <span class="meta-divider">·</span>
                                <span class="read-count">{{ formatNumber(readCount) }}阅读</span>
                                <span class="meta-divider">·</span>
                                <span class="read-time">{{ readTime }}分钟阅读</span>
                            </div>
                        </div>
                        <button class="follow-btn" :class="{ 'active': relation.isfollow }" @click="handleFollow">
                            {{ relation.isfollow ? '已关注' : '+ 关注' }}
                        </button>
                    </div>
                    <div class="content">
                        <div class="status-msg" v-if="contentLoading">
                            <span class="loading-spinner"></span>
                            <span>加载中...</span>
                        </div>
                        <div v-if="contentHtml" v-html="contentHtml" @click="handleImageClick"></div>
                        <div class="no-content-tip" v-if="!contentLoading && !contentHtml">
                            暂无文章内容
                        </div>
                    </div>
                    <div class="tools">
                        <Button text="点赞" @onClick="like" :icon="icon.like" :active="relation.islike" active-text="取消赞"/>
                        <Button text="不喜欢" @onClick="unlike" :icon="icon.unlike" :active="relation.isunlike" />
                    </div>
                    <div class="comment-section" ref="commentSection">
                        <CommentInput
                            ref="commentInput"
                            :replyTarget="replyTarget"
                            @submit="handleCommentSubmit"
                            @cancel-reply="cancelReply"
                        />
                        <CommentList
                            :comments="comments"
                            :articleId="id"
                            @reply="handleReply"
                            @like="handleLike"
                        />
                    </div>
                </div>
            </div>

            <div class="sidebar" v-if="!isImmersive">
                <div class="author-info-card">
                    <div class="author-avatar-wrap">
                        <img :src="authorAvatar || 'https://p3.pstatp.com/thumb/1480/7186611868'" class="avatar" alt="avatar">
                        <div class="name">{{ source }}</div>
                        <div class="badge">AI + 全栈开发工程师</div>
                        <div class="job-title">{{ authorJobTitle || '全栈开发工程师' }}</div>
                        <div class="company">{{ authorCompany || '某科技公司' }}</div>
                    </div>
                    <div class="stats">
                        <div class="stat-item">
                            <div class="stat-value">{{ articleCount }}</div>
                            <div class="stat-label">文章</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ formatNumber(readCount) }}</div>
                            <div class="stat-label">阅读</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ formatNumber(fansCount) }}</div>
                            <div class="stat-label">粉丝</div>
                        </div>
                    </div>
                    <div class="action-btns">
                        <button class="follow-btn" :class="{ 'active': relation.isfollow }" @click="handleFollow">
                            {{ relation.isfollow ? '已关注' : '+ 关注' }}
                        </button>
                        <button class="message-btn">私信</button>
                    </div>
                </div>
                <div class="toc-card">
                    <h3 class="toc-title">目录</h3>
                    <div class="toc-list">
                        <div class="toc-item" v-for="(item, index) in tocItems" :key="index" :class="item.level">
                            {{ item.text }}
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="image-lightbox" :class="{ 'open': showLightbox }" @click="closeLightbox">
            <button class="close-btn" @click="closeLightbox">&times;</button>
            <img :src="lightboxImage" class="lightbox-content">
        </div>
    </div>
</template>

<script>
    import HomeBar from '@/compoents/bars/home_bar'
    import Button from '@/compoents/buttons/button'
    import CommentList from '@/components/comments/CommentList'
    import CommentInput from '@/components/comments/CommentInput'
    import Api from '@/apis/article/api'
    import { toast } from "@/utils/toast"
    import Utils from '@/utils/env'
    import { marked } from 'marked'
    import { sanitizeHtml } from '@/utils/sanitize'

    export default {
        name: "ArticleDetail",
        components: { HomeBar, Button, CommentList, CommentInput },
        data() {
            return {
                scrollerHeight: '500px',
                title: '',
                source: '',
                date: '',
                staticUrl: '',
                icon: {
                    like: '\uf164',
                    unlike: '\uf1f6',
                    wechat: '\uf086',
                    friend: '\uf268'
                },
                contentHtml: '',
                contentLoading: false,
                relation: {
                    islike: false,
                    isunlike: false,
                    iscollection: false,
                    isfollow: false,
                    isforward: false
                },
                time: {
                    timer: null,
                    timerStep: 100,
                    readDuration: 0,
                    percentage: 0,
                    loadDuration: 0,
                    loadOff: true
                },
                comments: [],
                replyTarget: null,
                authorAvatar: '',
                authorJobTitle: '',
                authorCompany: '',
                articleCount: 0,
                readCount: 0,
                fansCount: 0,
                likeCount: 0,
                commentCount: 0,
                collectCount: 0,
                readTime: 5,
                showMiniAvatar: false,
                showLightbox: false,
                lightboxImage: '',
                isImmersive: false,
                tocItems: []
            }
        },
        computed: {
            id: function () {
                return this.$route.params.id
            }
        },
        created() {
            var _this = this
            this.time.timer = setInterval(function () {
                _this.time.readDuration += _this.time.timerStep
                if (_this.time.loadOff) {
                    _this.time.loadDuration += _this.time.timerStep
                }
            }, this.time.timerStep)
            // 加载文章元数据与内容
            this.loadInfo()
            this.loadContent()
            this.loadComments()
        },
        destroyed() {
            this.read()
            window.removeEventListener('scroll', this.onScroll)
            document.removeEventListener('keydown', this.handleKeydown)
        },
        mounted() {
            this.scrollerHeight = (Utils.getPageHeight() - 180) + 'px'
            window.addEventListener('scroll', this.onScroll)
            document.addEventListener('keydown', this.handleKeydown)
        },
        methods: {
            // 加载文章元数据
            loadInfo: function () {
                if (!this.id) return
                Api.getInfo(this.id).then((d) => {
                    if (d && d.code === 200 && d.data) {
                        this.title = d.data.title || ''
                        this.source = d.data.authorName || d.data.source || ''
                        this.date = d.data.publishTime || d.data.date || ''
                        this.staticUrl = d.data.staticUrl || ''
                        this.authorAvatar = d.data.authorAvatar || ''
                        this.authorJobTitle = d.data.authorJobTitle || ''
                        this.authorCompany = d.data.authorCompany || ''
                        this.articleCount = d.data.articleCount || 0
                        this.readCount = d.data.readCount || 0
                        this.fansCount = d.data.fansCount || 0
                        this.likeCount = d.data.likeCount || 0
                        this.commentCount = d.data.commentCount || 0
                        this.collectCount = d.data.collectCount || 0
                        this.readTime = d.data.readTime || Math.ceil((d.data.content?.length || 1500) / 300)
                    }
                }).catch(function () {})
            },
            // 加载文章内容（优先从MinIO staticUrl获取）
            loadContent: function () {
                if (!this.id) return
                this.contentLoading = true
                var _this = this
                // 先获取文章信息拿到staticUrl
                Api.getInfo(this.id).then((d) => {
                    var staticUrl = ''
                    if (d && d.code === 200 && d.data) {
                        this.title = d.data.title || this.title
                        this.source = d.data.authorName || this.source
                        this.date = d.data.publishTime || this.date
                        staticUrl = d.data.staticUrl || ''
                        this.staticUrl = staticUrl
                    }
                    if (staticUrl) {
                        // 从MinIO获取静态HTML内容
                        this._fetchFromMinio(staticUrl)
                    } else {
                        // 降级：从API获取内容
                        this._fetchFromApi()
                    }
                }).catch(() => {
                    this._fetchFromApi()
                })
            },
            // 从MinIO获取HTML内容
            _fetchFromMinio: function (url) {
                var _this = this
                // 将MinIO直连URL转为代理路径，避免跨域
                var proxyUrl = url.replace(/^https?:\/\/[^\/]+\/leadnews\//, '/minio-static/')
                var xhr = new XMLHttpRequest()
                xhr.open('GET', proxyUrl, true)
                xhr.onload = function () {
                    _this.contentLoading = false
                    _this.time.loadOff = false
                    if (xhr.status === 200) {
                        var html = xhr.responseText
                        // 提取 article-body 中的内容
                        var match = html.match(/<div class="article-body">([\s\S]*?)<\/div>\s*<div class="action-bar">/)
                        if (match && match[1]) {
                            _this.contentHtml = sanitizeHtml(match[1])
                        } else {
                            // 如果提取失败，尝试提取body内的主要内容
                            var bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/)
                            _this.contentHtml = bodyMatch ? sanitizeHtml(bodyMatch[1]) : sanitizeHtml(html)
                        }
                    } else {
                        _this._fetchFromApi()
                    }
                }
                xhr.onerror = function () {
                    _this._fetchFromApi()
                }
                xhr.send()
            },
            // 从API获取内容（降级方案）
            _fetchFromApi: function () {
                var _this = this
                Api.getContent(this.id).then((d) => {
                    _this.contentLoading = false
                    _this.time.loadOff = false
                    var raw = ''
                    if (d && d.code === 200 && d.data) {
                        raw = d.data
                    } else if (typeof d === 'string') {
                        raw = d
                    }
                    _this.contentHtml = raw ? sanitizeHtml(marked(raw)) : ''
                }).catch(() => {
                    _this.contentLoading = false
                    _this.contentHtml = ''
                })
            },
            // 点赞
            like: function () {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.like({ articleId: this.id, operation: this.relation.islike ? 1 : 0 }).then(d => {
                    if (d && d.code === 200) {
                        this.relation.islike = !this.relation.islike
                    } else {
                        toast(d && d.errorMessage || '操作失败', 3)
                    }
                }).catch(() => {
                    toast('网络错误，请重试', 3)
                })
            },
            // 不喜欢
            unlike: function () {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.unlike({ articleId: this.id, type: this.relation.isunlike ? 1 : 0 }).then(d => {
                    if (d && d.code === 200) {
                        this.relation.isunlike = !this.relation.isunlike
                    } else {
                        toast(d && d.errorMessage || '操作失败', 3)
                    }
                }).catch(() => {
                    toast('网络错误，请重试', 3)
                })
            },
            // 阅读行为
            read: function () {
                clearInterval(this.time.timer)
                if (this.$store.getters.isLoggedIn && this.id) {
                    Api.read({
                        articleId: this.id,
                        readDuration: this.time.readDuration,
                        percentage: this.time.percentage,
                        loadDuration: this.time.loadDuration
                    }).catch(function () {})
                }
            },
            formatDate: function (time) {
                if (time && typeof time === 'string') {
                    time = new Date(time).getTime()
                }
                return this.$date.format13(time)
            },
            scroller: function (e) {
                var y = Math.abs(e.target.scrollTop) + (Utils.getPageHeight() - 180)
                var height = e.target.scrollHeight
                if (height > 0) {
                    this.time.percentage = Math.max(parseInt((y * 100) / height), this.time.percentage)
                }
            },
            // 加载评论列表
            loadComments: function () {
                if (!this.id) return
                Api.getCommentList(this.id, 1, 3).then((d) => {
                    if (d && d.code === 200) {
                        this.comments = d.data || []
                    }
                }).catch(function () {})
            },
            // 处理评论提交
            handleCommentSubmit: function (content) {
                var _this = this
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                var data = {
                    articleId: this.id,
                    content: content
                }
                if (this.replyTarget) {
                    data.parentId = this.replyTarget.id
                }
                Api.addComment(data).then((d) => {
                    if (d && d.code === 200) {
                        toast('评论成功', 2)
                        _this.$refs.commentInput.reset()
                        _this.replyTarget = null
                        _this.loadComments()
                    } else {
                        toast(d && d.errorMessage || '评论失败', 3)
                        _this.$refs.commentInput.setSubmitting(false)
                    }
                }).catch(function () {
                    toast('网络错误，请重试', 3)
                    _this.$refs.commentInput.setSubmitting(false)
                })
            },
            // 处理回复
            handleReply: function (comment) {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                this.replyTarget = comment
            },
            // 取消回复
            cancelReply: function () {
                this.replyTarget = null
            },
            // 处理点赞
            handleLike: function (comment) {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.likeComment(comment.id).then((d) => {
                    if (d && d.code === 200) {
                        if (d.data) {
                            comment.liked = d.data.liked
                            comment.likeCount = d.data.likeCount
                        }
                    } else {
                        toast(d && d.errorMessage || '操作失败', 3)
                    }
                }).catch(function () {
                    toast('网络错误，请重试', 3)
                })
            },
            // 收藏
            handleCollect: function () {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.collect({ articleId: this.id, operation: this.relation.iscollection ? 1 : 0 }).then(d => {
                    if (d && d.code === 200) {
                        this.relation.iscollection = !this.relation.iscollection
                    } else {
                        toast(d && d.errorMessage || '操作失败', 3)
                    }
                }).catch(() => {
                    toast('网络错误，请重试', 3)
                })
            },
            // 关注
            handleFollow: function () {
                if (!this.$store.getters.isLoggedIn) {
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.follow({ articleId: this.id, operation: this.relation.isfollow ? 1 : 0 }).then(d => {
                    if (d && d.code === 200) {
                        this.relation.isfollow = !this.relation.isfollow
                    } else {
                        toast(d && d.errorMessage || '操作失败', 3)
                    }
                }).catch(() => {
                    toast('网络错误，请重试', 3)
                })
            },
            // 分享
            handleShare: function () {
                if (navigator.share) {
                    navigator.share({
                        title: this.title,
                        url: window.location.href
                    })
                } else {
                    navigator.clipboard.writeText(window.location.href)
                    toast('链接已复制', 2)
                }
            },
            // 举报
            handleReport: function () {
                toast('举报功能开发中', 2)
            },
            // 切换沉浸式阅读
            toggleImmersive: function () {
                this.isImmersive = !this.isImmersive
            },
            // 滚动到评论区
            scrollToComment: function () {
                this.$refs.commentSection.scrollIntoView({ behavior: 'smooth' })
            },
            // 图片点击
            handleImageClick: function (e) {
                if (e.target.tagName === 'IMG') {
                    this.lightboxImage = e.target.src
                    this.showLightbox = true
                    document.body.style.overflow = 'hidden'
                }
            },
            // 关闭图片预览
            closeLightbox: function () {
                this.showLightbox = false
                document.body.style.overflow = ''
            },
            // 格式化数字
            formatNumber: function (num) {
                if (!num) return 0
                if (num >= 10000) {
                    return (num / 10000).toFixed(1) + 'w'
                }
                return num
            },
            // 滚动监听
            onScroll: function () {
                var scrollTop = window.pageYOffset
                this.showMiniAvatar = scrollTop > 300
            },
            // 键盘事件
            handleKeydown: function (e) {
                if (e.key === 'Escape') {
                    this.closeLightbox()
                }
            }
        }
    }
</script>

<style scoped>
    .art-page {
        width: 100%;
        max-width: 1200px;
        margin: 0 auto;
        display: flex;
        flex-direction: column;
        min-height: 100vh;
    }
    .art-top {
        top: 0;
        position: fixed;
        left: 0;
        right: 0;
        z-index: 999;
        max-width: 1200px;
        margin: 0 auto;
    }
    .main-wrapper {
        display: flex;
        gap: 24px;
        padding-top: 56px;
    }
    .content-area {
        flex: 1;
        min-width: 0;
    }
    .sidebar {
        width: 280px;
        flex-shrink: 0;
        position: sticky;
        top: 64px;
        max-height: calc(100vh - 70px);
        overflow-y: auto;
    }
    .scroller {
        flex: 1;
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 0px 20px;
        margin: 90px auto;
        box-sizing: border-box;
    }
    .status-msg {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        padding: 40px 20px;
        color: #999;
        font-size: 28px;
    }
    .loading-spinner {
        width: 28px;
        height: 28px;
        border: 3px solid #e0e0e0;
        border-top-color: #3194ff;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }
    @keyframes spin {
        to { transform: rotate(360deg); }
    }
    .no-content-tip {
        text-align: center;
        color: #999;
        font-size: 28px;
        padding: 60px 20px;
        margin-top: 40px;
    }
    .title {
        font-size: 48px;
        font-weight: bold;
        margin: 10px 0px;
    }
    .content {
        display: flex;
        flex-direction: column;
        font-size: 30px;
        justify-content: flex-start;
        margin-top: 20px;
        color: #222;
        word-wrap: break-word;
        text-align: justify;
    }
    .content img {
        max-width: 100%;
        height: auto;
        border-radius: 5px;
        margin: 15px 0;
    }
    .tools {
        margin: 20px 0px 30px;
        display: flex;
        flex-direction: row;
        height: 60px;
        justify-content: center;
    }
    .comment-section {
        margin-top: 30px;
        padding: 24px 0;
        border-top: 1px solid #e4e6eb;
    }

    .author-info-card {
        background: #fff;
        border-radius: 4px;
        padding: 20px 0;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
        margin-bottom: 16px;
    }
    .author-info-card .author-avatar-wrap {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 0 20px;
        margin-bottom: 16px;
    }
    .author-info-card .avatar {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        object-fit: cover;
        margin-bottom: 10px;
        border: 2px solid #1e80ff;
    }
    .author-info-card .name {
        font-size: 16px;
        font-weight: 600;
        color: #252933;
        margin-bottom: 4px;
    }
    .author-info-card .badge {
        font-size: 12px;
        color: #1e80ff;
        background: #eaf2ff;
        padding: 2px 8px;
        border-radius: 4px;
        margin-bottom: 8px;
    }
    .author-info-card .job-title {
        font-size: 13px;
        color: #515767;
    }
    .author-info-card .company {
        font-size: 13px;
        color: #515767;
    }
    .author-info-card .stats {
        display: flex;
        justify-content: space-around;
        padding: 12px 16px;
        border-top: 1px solid #f2f3f5;
        margin-bottom: 12px;
    }
    .author-info-card .stat-item {
        text-align: center;
    }
    .author-info-card .stat-value {
        font-size: 16px;
        font-weight: 600;
        color: #252933;
    }
    .author-info-card .stat-label {
        font-size: 12px;
        color: #8a919f;
    }
    .author-info-card .action-btns {
        display: flex;
        gap: 8px;
        padding: 0 16px;
    }
    .author-info-card .follow-btn {
        flex: 1;
        padding: 8px;
        border: none;
        border-radius: 4px;
        background: #1e80ff;
        color: #fff;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
    }
    .author-info-card .follow-btn.active {
        background: #8a919f;
    }
    .author-info-card .message-btn {
        flex: 1;
        padding: 8px;
        border: 1px solid #e4e6eb;
        border-radius: 4px;
        background: #fff;
        color: #515767;
        font-size: 14px;
        cursor: pointer;
    }

    .toc-card {
        background: #fff;
        border-radius: 4px;
        padding: 16px;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }
    .toc-card .toc-title {
        font-size: 16px;
        font-weight: 600;
        color: #252933;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid #f2f3f5;
    }
    .toc-card .toc-list {
        max-height: 300px;
        overflow-y: auto;
    }
    .toc-card .toc-item {
        padding: 6px 0;
        font-size: 13px;
        color: #515767;
        cursor: pointer;
        transition: all 0.2s;
    }
    .toc-card .toc-item:hover {
        color: #1e80ff;
    }
    .toc-card .toc-item.h2 { padding-left: 0; }
    .toc-card .toc-item.h3 { padding-left: 12px; }
    .toc-card .toc-item.h4 { padding-left: 24px; }

    .author-header {
        display: flex;
        align-items: center;
        padding: 16px 0;
        margin-bottom: 8px;
    }
    .author-header .author-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        overflow: hidden;
        flex-shrink: 0;
    }
    .author-header .author-avatar img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .author-header .author-info {
        flex: 1;
        padding-left: 12px;
    }
    .author-header .author-name {
        font-size: 14px;
        font-weight: 600;
        color: #252933;
    }
    .publish-meta {
        font-size: 13px;
        color: #8a919f;
        display: flex;
        align-items: center;
        gap: 6px;
        margin-top: 4px;
    }
    .meta-divider {
        color: #c4c9d1;
    }
    .read-count, .read-time {
        font-size: 13px;
        color: #8a919f;
    }
    .author-header .follow-btn {
        padding: 6px 16px;
        border: none;
        border-radius: 4px;
        background: #1e80ff;
        color: #fff;
        font-size: 13px;
        cursor: pointer;
        flex-shrink: 0;
    }
    .author-header .follow-btn.active {
        background: #8a919f;
    }

    .action-sidebar {
        position: fixed;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 12px;
        background: rgba(255,255,255,0.95);
        border-radius: 0 8px 8px 0;
        box-shadow: 2px 0 8px rgba(0,0,0,0.08);
        z-index: 999;
    }
    .action-sidebar .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        padding: 8px;
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.2s;
        color: #515767;
    }
    .action-sidebar .action-item:hover {
        background: #f7f8fa;
    }
    .action-sidebar .action-item.active {
        color: #1e80ff;
    }
    .action-sidebar .action-item.hidden-item {
        opacity: 0;
        pointer-events: none;
        height: 0;
        overflow: hidden;
        transition: all 0.3s;
    }
    .action-sidebar .action-icon {
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
    }
    .action-sidebar .action-count {
        font-size: 12px;
        color: #8a919f;
    }
    .action-sidebar .avatar-wrap {
        position: relative;
    }
    .action-sidebar .author-mini-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid #1e80ff;
    }
    .action-sidebar .mini-follow-badge {
        position: absolute;
        bottom: -2px;
        left: 50%;
        transform: translateX(-50%);
        font-size: 10px;
        color: #fff;
        background: #1e80ff;
        padding: 1px 6px;
        border-radius: 10px;
        white-space: nowrap;
    }
    .action-sidebar .mini-follow-badge.active {
        background: #8a919f;
    }

    .image-lightbox {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.85);
        z-index: 2000;
        align-items: center;
        justify-content: center;
    }
    .image-lightbox.open {
        display: flex;
    }
    .image-lightbox .lightbox-content {
        max-width: 90%;
        max-height: 90%;
        object-fit: contain;
        border-radius: 4px;
    }
    .image-lightbox .close-btn {
        position: absolute;
        top: 20px;
        right: 20px;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: rgba(255,255,255,0.2);
        border: none;
        color: #fff;
        font-size: 24px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .art-page.immersive .sidebar {
        display: none;
    }
    .art-page.immersive .action-sidebar {
        display: none;
    }
    .art-page.immersive .main-wrapper {
        justify-content: center;
    }
    .art-page.immersive .content-area {
        max-width: 800px;
    }

    @media screen and (max-width: 960px) {
        .sidebar {
            display: none;
        }
        .action-sidebar {
            display: none;
        }
    }

    @media screen and (min-width: 768px) {
        .art-page {
            max-width: 750PX;
            margin: 0 auto;
            background-color: #fff;
            min-height: 100vh;
        }
        .art-top {
            max-width: 750PX;
        }
        .scroller {
            margin: 90PX auto;
            padding: 0 24PX;
            max-width: 750PX;
        }
        .title {
            font-size: 28PX;
            margin: 16PX 0;
            line-height: 1.4;
        }
        .content {
            font-size: 16PX;
            margin-top: 16PX;
            line-height: 1.8;
        }
        .content img {
            border-radius: 4PX;
            margin: 12PX 0;
        }
        .status-msg {
            font-size: 14PX;
            padding: 30PX 20PX;
            gap: 8PX;
        }
        .loading-spinner {
            width: 20PX;
            height: 20PX;
            border-width: 2PX;
        }
        .no-content-tip {
            font-size: 14PX;
            padding: 40PX 20PX;
        }
        .tools {
            margin: 20PX 0 30PX;
            height: 48PX;
        }
        .comment-section {
            margin-top: 30PX;
            padding: 24PX 0;
        }
    }
</style>