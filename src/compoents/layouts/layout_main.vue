<template>
    <div class="main-root">
        <!-- Web端三栏布局 -->
        <div class="desktop-layout" v-if="isDesktop">
            <div class="desktop-header">
                <div class="header-inner">
                    <div class="header-left">
                        <span class="logo-text">黑马头条</span>
                    </div>
                    <nav class="main-nav">
                        <span class="nav-link" :class="{ active: currentNav === 'home' }" @click="goToHome">首页</span>
                        <span class="nav-link" :class="{ active: currentNav === 'pins' }" @click="goToPins">沸点</span>
                        <span class="nav-link" :class="{ active: currentNav === 'course' }" @click="goToCourse">课程</span>
                        <span class="nav-link">数据标注</span>
                        <span class="nav-link">AI Coding</span>
                        <span class="nav-link more-link">更多 <span class="more-arrow">&#9662;</span></span>
                    </nav>
                    <div class="header-center">
                        <div class="web-search-box" ref="searchBox">
                            <span class="web-search-icon">&#xf002;</span>
                            <input
                                v-model="searchKeyword"
                                type="text"
                                class="web-search-input"
                                placeholder="搜索文章"
                                @focus="onSearchFocus"
                                @input="onSearchInput"
                                @blur="onSearchBlur"
                                @keyup.enter="onSearchSubmit"
                            />
                            <span class="web-search-btn" @click="onSearchSubmit">&#xf002;</span>
                            <span class="web-search-clear" v-if="searchKeyword" @click.stop="clearSearchInput">&#10005;</span>
                            <div class="search-dropdown" v-if="showSearchDropdown" @click.stop>
                                <template v-if="!searchKeyword && searchHistory.length > 0">
                                    <div class="dropdown-header">
                                        <span class="dropdown-title">搜索历史</span>
                                        <span class="dropdown-clear" @click="clearSearchHistory">清空</span>
                                    </div>
                                    <div class="history-tags">
                                        <span class="history-tag" v-for="(item, idx) in searchHistory" :key="idx" @click="doSearch(item)">
                                            {{ item }}
                                            <span class="history-delete" @click.stop="deleteHistoryItem(idx)">&#10005;</span>
                                        </span>
                                    </div>
                                </template>
                                <template v-if="searchKeyword && searchSuggestions.length > 0">
                                    <div class="suggestion-list">
                                        <div class="suggestion-item" v-for="(item, idx) in searchSuggestions" :key="idx" @click="doSearch(item)">
                                            <span class="suggestion-icon">&#xf002;</span>
                                            <span class="suggestion-text" v-html="highlightKeyword(item)"></span>
                                        </div>
                                    </div>
                                </template>
                                <div class="dropdown-empty" v-if="!searchKeyword && searchHistory.length === 0">
                                    <span>暂无搜索历史</span>
                                </div>
                                <div class="dropdown-empty" v-if="searchKeyword && searchSuggestions.length === 0">
                                    <span>暂无相关联想</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="header-right">
                        <span v-if="!isLoggedIn" class="header-btn write-btn" @click="showLogin">
                            <span class="btn-icon">&#xf040;</span>写文章
                        </span>
                        <span v-if="isLoggedIn" class="header-btn creator-btn" @click="openCreatorCenter">
                            <span class="btn-icon">&#xf040;</span>创作者中心
                        </span>
                        <span v-if="!isLoggedIn" class="header-btn login-btn" @click="showLogin">登录</span>
                        <div v-if="isLoggedIn" class="header-user" @click="toggleUserDropdown">
                            <img v-if="userAvatar" class="header-avatar" :src="userAvatar" alt="头像"/>
                            <span v-else class="header-avatar-default">&#xf007;</span>
                            <span class="header-username">{{ userName }}</span>
                            <div class="user-dropdown" v-if="showUserDropdown" @click.stop>
                                <!-- 用户信息区 -->
                                <div class="dropdown-user-section" @click="goToProfile">
                                    <img v-if="userAvatar" class="dropdown-avatar" :src="userAvatar" alt="头像" />
                                    <span v-else class="dropdown-avatar-icon">&#xf007;</span>
                                    <div class="dropdown-user-info">
                                        <div class="dropdown-username">{{ userName }}</div>
                                        <div class="dropdown-user-level">{{ levelBadge }}</div>
                                    </div>
                                    <div class="dropdown-diamond" @click.stop>
                                        <span class="diamond-icon">&#xf219;</span>
                                        <span class="diamond-text">矿石: {{ formattedDiamond }}</span>
                                        <span class="diamond-arrow">&#xf105;</span>
                                    </div>
                                </div>
                                <!-- 等级进度条 -->
                                <div class="dropdown-level-bar" @click="goToGrowth">
                                    <div class="level-label">逐日等级 {{ levelBadge }}</div>
                                    <div class="level-progress-wrap">
                                        <div class="level-progress-bar">
                                            <div class="level-progress-fill" :style="{ width: levelPercent + '%' }"></div>
                                        </div>
                                        <span class="level-text">{{ formattedLevelText }}</span>
                                    </div>
                                    <span class="level-arrow">&#xf105;</span>
                                </div>
                                <!-- 统计数据 -->
                                <div class="dropdown-stats">
                                    <div class="stat-item" @click="goToFollow">
                                        <div class="stat-value">{{ stats.followCount }}</div>
                                        <div class="stat-label">关注</div>
                                    </div>
                                    <div class="stat-item" @click="goToLikes">
                                        <div class="stat-value">{{ stats.likeCount }}</div>
                                        <div class="stat-label">赞过</div>
                                    </div>
                                    <div class="stat-item" @click="goToCollects">
                                        <div class="stat-value">{{ stats.collectCount }}</div>
                                        <div class="stat-label">收藏</div>
                                    </div>
                                </div>
                                <div class="dropdown-divider"></div>
                                <!-- 菜单项 -->
                                <div class="dropdown-menu-section">
                                    <div class="dropdown-item" @click="goToProfile">
                                        <span class="dropdown-icon">&#xf007;</span>
                                        <span class="dropdown-label">我的主页</span>
                                    </div>
                                    <div class="dropdown-item" @click="goToCheckin">
                                        <span class="dropdown-icon">&#xf091;</span>
                                        <span class="dropdown-label">成长福利</span>
                                    </div>
                                    <div class="dropdown-item" @click="goToCourses">
                                        <span class="dropdown-icon">&#xf19c;</span>
                                        <span class="dropdown-label">课程中心</span>
                                    </div>
                                    <div class="dropdown-item" @click="handleMyDiscount">
                                        <span class="dropdown-icon">&#xf155;</span>
                                        <span class="dropdown-label">我的优惠</span>
                                    </div>
                                    <div class="dropdown-item" @click="goToHistory">
                                        <span class="dropdown-icon">&#xf02d;</span>
                                        <span class="dropdown-label">我的足迹</span>
                                    </div>
                                </div>
                                <div class="dropdown-divider"></div>
                                <!-- 底部 -->
                                <div class="dropdown-bottom-section">
                                    <div class="dropdown-item" @click="goToSettings">
                                        <span class="dropdown-icon">&#xf013;</span>
                                        <span class="dropdown-label">我的设置</span>
                                    </div>
                                    <div class="dropdown-item logout-item" @click="handleLogout">
                                        <span class="dropdown-icon">&#xf08b;</span>
                                        <span class="dropdown-label">退出登录</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="desktop-container">
                <div class="desktop-sidebar">
                    <div class="sidebar-nav">
                        <div class="nav-item" :class="{ active: currentCategory === 'following' }" @click="selectCategory('following')">
                            <span class="nav-icon">&#xf004;</span>
                            <span class="nav-text">关注</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'comprehensive' }" @click="selectCategory('comprehensive')">
                            <span class="nav-icon">&#xf015;</span>
                            <span class="nav-text">综合</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'backend' }" @click="selectCategory('backend')">
                            <span class="nav-icon">&#xf233;</span>
                            <span class="nav-text">后端</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'frontend' }" @click="selectCategory('frontend')">
                            <span class="nav-icon">&#xf121;</span>
                            <span class="nav-text">前端</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'android' }" @click="selectCategory('android')">
                            <span class="nav-icon">&#xf17b;</span>
                            <span class="nav-text">Android</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'ios' }" @click="selectCategory('ios')">
                            <span class="nav-icon">&#xf179;</span>
                            <span class="nav-text">iOS</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'ai' }" @click="selectCategory('ai')">
                            <span class="nav-icon">&#xf2db;</span>
                            <span class="nav-text">人工智能</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'devtools' }" @click="selectCategory('devtools')">
                            <span class="nav-icon">&#xf0ad;</span>
                            <span class="nav-text">开发工具</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'coderslife' }" @click="selectCategory('coderslife')">
                            <span class="nav-icon">&#xf11c;</span>
                            <span class="nav-text">代码人生</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'reading' }" @click="selectCategory('reading')">
                            <span class="nav-icon">&#xf02d;</span>
                            <span class="nav-text">阅读</span>
                        </div>
                        <div class="nav-item" :class="{ active: currentCategory === 'ranking' }" @click="selectCategory('ranking')">
                            <span class="nav-icon">&#xf091;</span>
                            <span class="nav-text">排行榜</span>
                        </div>
                    </div>
                </div>

                <div class="desktop-content">
                    <router-view/>
                </div>

                <div class="desktop-aside">
                    <div class="aside-card">
                        <div class="aside-title">文章榜</div>
                        <div class="rank-list">
                            <div class="rank-item" v-for="(item, idx) in articleRankList" :key="'ar'+idx">
                                <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                                <span class="rank-title">{{ item }}</span>
                            </div>
                        </div>
                    </div>
                    <div class="aside-card">
                        <div class="aside-title">推荐作者</div>
                        <div class="author-item" v-for="i in 5" :key="i">
                            <img class="author-avatar" :src="'/static/images/avatar_head_' + i + '.png'" alt="作者头像"/>
                            <div class="author-info">
                                <div class="author-name">推荐作者{{ i }}</div>
                                <div class="author-desc">写了{{ i * 23 }}篇文章 · {{ i * 120 }}关注</div>
                            </div>
                            <span class="follow-btn" @click="showLogin">关注</span>
                        </div>
                        <div class="aside-more">查看更多 ›</div>
                    </div>
                    <div class="aside-card">
                        <div class="aside-title">热门标签</div>
                        <div class="tag-list">
                            <span class="tag-item">前端</span>
                            <span class="tag-item">Java</span>
                            <span class="tag-item">Python</span>
                            <span class="tag-item">Vue</span>
                            <span class="tag-item">React</span>
                            <span class="tag-item">Android</span>
                            <span class="tag-item">iOS</span>
                            <span class="tag-item">人工智能</span>
                        </div>
                    </div>
                    <div class="aside-card">
                        <div class="aside-title">作者榜</div>
                        <div class="author-list">
                            <div class="author-item" v-for="(item, idx) in authorRankList" :key="'au'+idx">
                                <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                                <img class="author-avatar" :src="'/static/images/avatar_head_' + (idx + 1) + '.png'" alt="作者头像"/>
                                <div class="author-info">
                                    <div class="author-name">{{ item.name }}</div>
                                    <div class="author-desc">{{ item.desc }}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="aside-footer">
                        <div class="footer-links">关于 · 联系我们 · 加入我们</div>
                        <div class="footer-copy">© 2024 黑马头条</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 移动端原有布局 -->
        <template v-else>
            <div class="router-body"><router-view/></div>
            <div class="app-open-btn" @click="handleAppOpen">
                <span class="app-open-icon">&#xf3cd;</span>
                <span class="app-open-text">App内打开</span>
            </div>
        </template>
    </div>
</template>

<script>
    import Utils from '@/utils/env';
    import { toast } from "@/utils/toast"
    import SearchApi from '@/apis/search/api'
    import { sanitizeHighlight } from '@/utils/sanitize'
    import { getUserStatistics } from '@/apis/user'

    var SEARCH_HISTORY_KEY = 'HEIMA_SEARCH_HISTORY'
    var MAX_HISTORY_COUNT = 6
    var MAX_SUGGESTION_COUNT = 10

    function getSearchHistory() {
        try {
            var str = localStorage.getItem(SEARCH_HISTORY_KEY)
            if (str) {
                var arr = JSON.parse(str)
                if (Array.isArray(arr)) {
                    return arr.slice(0, MAX_HISTORY_COUNT)
                }
            }
        } catch (e) {}
        return []
    }

    function saveSearchHistory(list) {
        try {
            localStorage.setItem(SEARCH_HISTORY_KEY, JSON.stringify(list.slice(0, MAX_HISTORY_COUNT)))
        } catch (e) {}
    }

    function addSearchHistory(keyword) {
        if (!keyword || !keyword.trim()) return
        keyword = keyword.trim()
        var list = getSearchHistory()
        var idx = list.indexOf(keyword)
        if (idx !== -1) {
            list.splice(idx, 1)
        }
        list.unshift(keyword)
        saveSearchHistory(list)
    }

    function deleteSearchHistoryByIndex(index) {
        var list = getSearchHistory()
        if (index >= 0 && index < list.length) {
            list.splice(index, 1)
            saveSearchHistory(list)
        }
        return list
    }

    function clearSearchHistoryAll() {
        try {
            localStorage.removeItem(SEARCH_HISTORY_KEY)
        } catch (e) {}
    }

    export default {
        name: "HeiMaLayoutMain",
        components: {},
        data() {
            return {
                showUserDropdown: false,
                searchKeyword: '',
                showSearchDropdown: false,
                searchHistory: [],
                searchSuggestions: [],
                searchTimer: null,
                _blurTimer: null,
                currentCategory: 'comprehensive',
                currentNav: 'home',
                stats: {
                    followCount: 0,
                    likeCount: 0,
                    collectCount: 0
                },
                diamondCount: '0',
                levelBadge: 'ZR.1',
                levelScore: 0,
                levelMax: 150,
                levelPercent: 0,
                articleRankList: [
                    'Vue 3.5 新特性全面解析',
                    '2024 前端开发趋势报告',
                    '深入理解 JavaScript 异步编程',
                    'React 19 新功能实战指南',
                    'TypeScript 5.5 类型体操',
                    'Node.js 性能优化实战',
                    'CSS Grid 布局完全指南',
                    'Webpack 到 Vite 迁移实践'
                ],
                authorRankList: [
                    { name: '张风捷特烈', desc: 'Android · 120k关注' },
                    { name: '捡田螺的小男孩', desc: '后端 · 98k关注' },
                    { name: '程序员鱼皮', desc: '全栈 · 87k关注' },
                    { name: '神三元', desc: '前端 · 75k关注' },
                    { name: '敖丙', desc: 'Java · 68k关注' }
                ]
            }
        },
        computed: {
            isDesktop() {
                return Utils.isDesktop()
            },
            isLoggedIn() {
                return this.$store.getters.isLoggedIn
            },
            userInfo() {
                return this.$store.getters.userInfo
            },
            userName() {
                return this.userInfo ? (this.userInfo.nickName || '用户') : '用户'
            },
            userAvatar() {
                if (this.userInfo && this.userInfo.avatar) {
                    return '/static/images/' + this.userInfo.avatar + '.png'
                }
                return ''
            },
            formattedDiamond() {
                const count = parseFloat(this.diamondCount)
                if (isNaN(count)) return '0'
                if (count >= 1000) {
                    return (count / 1000).toFixed(1) + 'k'
                }
                return String(Math.floor(count))
            },
            formattedLevelText() {
                return this.levelScore + ' / ' + this.levelMax
            }
        },
        mounted() {
            this.syncCategoryFromRoute()
            this.handleResize = () => {
                this.$forceUpdate()
            }
            window.addEventListener('resize', this.handleResize)
            this.closeDropdown = (e) => {
                if (this.showUserDropdown) {
                    var userEl = this.$el.querySelector('.header-user')
                    if (userEl && !userEl.contains(e.target)) {
                        this.showUserDropdown = false
                    }
                }
                if (this.showSearchDropdown) {
                    var searchEl = this.$refs.searchBox
                    if (searchEl && !searchEl.contains(e.target)) {
                        this.showSearchDropdown = false
                    }
                }
            }
            document.addEventListener('click', this.closeDropdown)
            this.searchHistory = getSearchHistory()
        },
        watch: {
            '$route.path': function(newPath) {
                this.syncCategoryFromRoute()
            }
        },
        beforeDestroy() {
            window.removeEventListener('resize', this.handleResize)
            document.removeEventListener('click', this.closeDropdown)
            if (this.searchTimer) {
                clearTimeout(this.searchTimer)
                this.searchTimer = null
            }
            if (this._blurTimer) {
                clearTimeout(this._blurTimer)
                this._blurTimer = null
            }
        },
        methods: {
            showLogin() {
                this.$store.dispatch('showLogin')
            },
            handleAppOpen() {
                toast('未发布App产品，敬请期待', 2)
            },
            toggleUserDropdown() {
                if (this.isLoggedIn) {
                    this.showUserDropdown = !this.showUserDropdown
                    if (this.showUserDropdown) {
                        this.loadUserStats()
                    }
                } else {
                    this.showLogin()
                }
            },
            async loadUserStats() {
                try {
                    const res = await getUserStatistics()
                    if (res && res.code === 200 && res.data) {
                        const data = res.data
                        this.stats.followCount = data.followCount || 0
                        this.stats.likeCount = data.likeCount || 0
                        this.stats.collectCount = data.collectCount || 0
                        this.diamondCount = data.diamondCount || '0'
                        if (data.levelInfo) {
                            const li = data.levelInfo
                            this.levelBadge = 'ZR.' + (li.dailyLevel || 1)
                            this.levelScore = li.dailyScore || 0
                            const levelMaxMap = { 1: 150, 2: 300, 3: 500, 4: 800, 5: 1200 }
                            this.levelMax = levelMaxMap[li.dailyLevel] || 150
                            const levelBaseMap = { 1: 0, 2: 150, 3: 300, 4: 500, 5: 800 }
                            const base = levelBaseMap[li.dailyLevel] || 0
                            const currentInLevel = this.levelScore - base
                            this.levelPercent = Math.min(Math.round(currentInLevel / this.levelMax * 100), 100)
                        }
                    }
                } catch (e) {
                    // Silently fail, use defaults
                }
            },
            goToProfile() {
                this.showUserDropdown = false
                const userId = this.userInfo && this.userInfo.userId ? this.userInfo.userId : 1
                this.$router.push('/user/' + userId)
            },
            goToSettings() {
                this.showUserDropdown = false
                this.$router.push('/user/settings')
            },
            goToGrowth() {
                this.showUserDropdown = false
                this.$router.push('/user/growth')
            },
            goToCheckin() {
                this.showUserDropdown = false
                this.$router.push('/user/checkin')
            },
            goToCourses() {
                this.showUserDropdown = false
                this.$router.push('/user/courses')
            },
            goToHistory() {
                this.showUserDropdown = false
                this.$router.push('/user/history')
            },
            goToFollow() {
                this.showUserDropdown = false
                const userId = this.userInfo && this.userInfo.userId ? this.userInfo.userId : 1
                this.$router.push('/user/' + userId + '?tab=follow&subTab=following')
            },
            goToLikes() {
                this.showUserDropdown = false
                const userId = this.userInfo && this.userInfo.userId ? this.userInfo.userId : 1
                this.$router.push('/user/' + userId + '?tab=likes&subTab=article')
            },
            goToCollects() {
                this.showUserDropdown = false
                const userId = this.userInfo && this.userInfo.userId ? this.userInfo.userId : 1
                this.$router.push('/user/' + userId + '?tab=collection')
            },
            handleMyDiscount() {
                this.showUserDropdown = false
                toast('我的优惠功能开发中', 2)
            },
            handleLogout() {
                this.showUserDropdown = false
                this.$store.dispatch('logout')
                toast('已退出登录', 2)
            },
            openCreatorCenter() {
                this.$router.push('/creator')
            },
            goHome() {
                if (this.$route.path !== '/home') {
                    this.$router.push('/home')
                }
            },
            goToHome() {
                this.currentNav = 'home'
                this.$router.push('/home')
            },
            goToPins() {
                this.currentNav = 'pins'
                this.$router.push('/pins')
            },
            goToCourse() {
                this.currentNav = 'course'
                this.$router.push('/course')
            },
            selectCategory(category) {
                this.currentCategory = category
                if (category === 'following') {
                    if (!this.isLoggedIn) {
                        this.showLogin()
                        return
                    }
                    this.$router.push('/home/following')
                } else if (category === 'comprehensive') {
                    this.$router.push('/home')
                } else {
                    this.$router.push(`/home/${category}`)
                }
            },
            syncCategoryFromRoute() {
                var path = this.$route.path
                if (path === '/home/following') {
                    this.currentCategory = 'following'
                    this.currentNav = 'home'
                } else if (path === '/home') {
                    this.currentCategory = 'comprehensive'
                    this.currentNav = 'home'
                } else if (path.indexOf('/home/') === 0) {
                    var cat = path.replace('/home/', '')
                    if (cat) {
                        this.currentCategory = cat
                    }
                    this.currentNav = 'home'
                } else if (path.indexOf('/pins') === 0) {
                    this.currentNav = 'pins'
                } else if (path.indexOf('/course') === 0) {
                    this.currentNav = 'course'
                }
            },
            onSearchFocus() {
                if (this._blurTimer) {
                    clearTimeout(this._blurTimer)
                    this._blurTimer = null
                }
                this.searchHistory = getSearchHistory()
                this.showSearchDropdown = true
            },
            onSearchInput() {
                var val = this.searchKeyword
                if (this.searchTimer) {
                    clearTimeout(this.searchTimer)
                }
                if (!val || !val.trim()) {
                    this.searchSuggestions = []
                    this.searchHistory = getSearchHistory()
                    this.showSearchDropdown = true
                    return
                }
                this.searchTimer = setTimeout(() => {
                    this.loadSearchSuggestions(val.trim())
                }, 300)
            },
            onSearchBlur() {
                this._blurTimer = setTimeout(() => {
                    this.showSearchDropdown = false
                }, 200)
            },
            onSearchSubmit() {
                var val = this.searchKeyword
                if (val && val.trim()) {
                    this.doSearch(val.trim())
                }
            },
            clearSearchInput() {
                this.searchKeyword = ''
                this.searchSuggestions = []
                this.searchHistory = getSearchHistory()
                var input = this.$el.querySelector('.web-search-input')
                if (input) input.focus()
            },
            loadSearchSuggestions(keyword) {
                var self = this
                SearchApi.associate_search(keyword).then(function (data) {
                    if (data && data.code === 200 && data.data) {
                        self.searchSuggestions = (data.data || []).map(function (item) {
                            return item && item.associateWords ? item.associateWords : item
                        }).slice(0, MAX_SUGGESTION_COUNT)
                    } else {
                        self.searchSuggestions = []
                    }
                    self.showSearchDropdown = true
                }).catch(function () {
                    self.searchSuggestions = []
                })
            },
            highlightKeyword(text) {
                var kw = this.searchKeyword
                if (!kw) return text
                try {
                    var reg = new RegExp('(' + kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi')
                    var highlighted = text.replace(reg, '<em style="color:#3194ff;font-style:normal">$1</em>')
                    return sanitizeHighlight(highlighted)
                } catch (e) {
                    return text
                }
            },
            deleteHistoryItem(index) {
                this.searchHistory = deleteSearchHistoryByIndex(index)
            },
            clearSearchHistory() {
                clearSearchHistoryAll()
                this.searchHistory = []
                toast('已清空搜索历史', 2)
            },
            doSearch(keyword) {
                if (!keyword || !keyword.trim()) return
                keyword = keyword.trim()
                addSearchHistory(keyword)
                this.searchKeyword = keyword
                this.showSearchDropdown = false
                this.searchSuggestions = []
                this.$router.push({ name: 'search_result', params: { keyword: keyword } })
            }
        }
    };
</script>

<style lang="less" scoped>
    @import '../../styles/common';

    .main-root {
        width: 100%;
        height: 100%;
        min-height: 100vh;
        background-color: #f5f5f5;
        position: relative;
    }

    .router-body {
        width: 100%;
        height: 100%;
        min-height: 100vh;
        padding-bottom: 100px;
        box-sizing: border-box;
    }

    .app-open-btn {
        position: fixed;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 28px;
        background: linear-gradient(135deg, #1e80ff, #4096ff);
        border-radius: 28px;
        box-shadow: 0 4px 16px rgba(30, 128, 255, 0.4);
        cursor: pointer;
        z-index: 99;
        transition: transform 0.2s, box-shadow 0.2s;
    }
    .app-open-btn:active {
        transform: translateX(-50%) scale(0.95);
        box-shadow: 0 2px 8px rgba(30, 128, 255, 0.3);
    }
    .app-open-icon {
        font-family: fontawesome;
        font-size: 22px;
        color: #fff;
    }
    .app-open-text {
        font-size: 16px;
        color: #fff;
        font-weight: 500;
    }

    /* ========== Web端样式 ========== */
    @media screen and (min-width: 768px) {
        .main-root {
            background-color: #f4f5f5;
            min-height: 100vh;
        }

        .router-body, .app-open-btn {
            display: none;
        }

        .desktop-layout {
            width: 100%;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .desktop-header {
            height: 60PX;
            background-color: #ffffff;
            box-shadow: 0 1PX 3PX rgba(0,0,0,0.08);
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 100;
        }

        .header-inner {
            max-width: 1440PX;
            margin: 0 auto;
            height: 60PX;
            display: flex;
            align-items: center;
            padding: 0 24PX;
            box-sizing: border-box;
        }

        .header-left {
            width: 180PX;
            flex-shrink: 0;
        }

        .logo-text {
            font-size: 22PX;
            font-weight: 700;
            color: @mian-color;
        }

        .main-nav {
            display: flex;
            align-items: center;
            gap: 0;
            flex-shrink: 0;
            margin: 0 24PX;
        }

        .nav-link {
            padding: 0 12PX;
            font-size: 14PX;
            color: #515767;
            cursor: pointer;
            white-space: nowrap;
            transition: color 0.2s;
            line-height: 60PX;
            position: relative;
        }
        .nav-link:hover {
            color: #1E80FF;
        }
        .nav-link.active {
            color: #1E80FF;
        }
        .nav-link.active::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 20PX;
            height: 2PX;
            background-color: #1E80FF;
            border-radius: 1PX;
        }

        .more-link {
            display: flex;
            align-items: center;
            gap: 4PX;
        }
        .more-arrow {
            font-size: 10PX;
            margin-top: 2PX;
        }

        .header-center {
            flex: 1;
            max-width: 380PX;
            margin: 0 0;
        }

        .web-search-box {
            position: relative;
            display: flex;
            align-items: center;
            height: 40PX;
            background-color: #f4f5f5;
            border-radius: 20PX;
            padding: 0 16PX 0 20PX;
            width: 100%;
            box-sizing: border-box;
            transition: background-color 0.2s;
        }
        .web-search-box:focus-within {
            background-color: #ffffff;
            box-shadow: 0 0 0 2PX rgba(49,148,255,0.2);
        }
        .web-search-icon {
            font-family: fontawesome;
            font-size: 15PX;
            color: #999;
            margin-right: 10PX;
            flex-shrink: 0;
        }
        .web-search-input {
            flex: 1;
            height: 100%;
            border: none;
            outline: none;
            background-color: transparent;
            font-size: 14PX;
            color: #333;
            min-width: 0;
        }
        .web-search-input::placeholder {
            color: #999;
        }
        .web-search-clear {
            width: 20PX;
            height: 20PX;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12PX;
            color: #999;
            cursor: pointer;
            border-radius: 50%;
            flex-shrink: 0;
            margin-left: 8PX;
        }
        .web-search-clear:hover {
            background-color: #e8e8e8;
            color: #666;
        }
        .web-search-btn {
            font-family: fontawesome;
            width: 32PX;
            height: 32PX;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 15PX;
            color: #999;
            cursor: pointer;
            flex-shrink: 0;
            border-radius: 50%;
            transition: color 0.2s;
        }
        .web-search-btn:hover {
            color: #1E80FF;
        }

        .search-dropdown {
            position: absolute;
            top: calc(100% + 8PX);
            left: 0;
            right: 0;
            background-color: #ffffff;
            border-radius: 8PX;
            box-shadow: 0 4PX 20PX rgba(0,0,0,0.12);
            z-index: 300;
            overflow: hidden;
            max-height: 400PX;
            overflow-y: auto;
        }

        .dropdown-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12PX 16PX;
            border-bottom: 1PX solid #f0f0f0;
        }
        .dropdown-title {
            font-size: 13PX;
            color: #999;
        }
        .dropdown-clear {
            font-size: 13PX;
            color: #999;
            cursor: pointer;
        }
        .dropdown-clear:hover {
            color: @mian-color;
        }

        .history-tags {
            padding: 12PX 16PX;
            display: flex;
            flex-wrap: wrap;
            gap: 8PX;
        }
        .history-tag {
            display: inline-flex;
            align-items: center;
            gap: 6PX;
            padding: 6PX 12PX;
            background-color: #f5f7fa;
            border-radius: 4PX;
            font-size: 13PX;
            color: #333;
            cursor: pointer;
            transition: all 0.2s;
        }
        .history-tag:hover {
            background-color: #e8f4ff;
            color: @mian-color;
        }
        .history-delete {
            font-size: 11PX;
            color: #c0c4cc;
            width: 16PX;
            height: 16PX;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }
        .history-delete:hover {
            background-color: rgba(0,0,0,0.05);
            color: #999;
        }

        .suggestion-list {
            padding: 4PX 0;
        }
        .suggestion-item {
            display: flex;
            align-items: center;
            padding: 10PX 16PX;
            cursor: pointer;
            transition: background-color 0.2s;
            gap: 10PX;
        }
        .suggestion-item:hover {
            background-color: #f5f7fa;
        }
        .suggestion-icon {
            font-family: fontawesome;
            font-size: 13PX;
            color: #c0c4cc;
            flex-shrink: 0;
        }
        .suggestion-text {
            font-size: 14PX;
            color: #333;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .dropdown-empty {
            padding: 30PX 16PX;
            text-align: center;
            font-size: 13PX;
            color: #c0c4cc;
        }

        .header-right {
            width: 180PX;
            min-width: 180PX;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            gap: 12PX;
        }

        .header-btn {
            padding: 6PX 16PX;
            border-radius: 4PX;
            font-size: 14PX;
            cursor: pointer;
            white-space: nowrap;
            transition: all 0.2s;
        }

        .write-btn {
            color: #333;
            background-color: #f4f5f5;
        }
        .write-btn:hover {
            background-color: #e8e8e8;
        }

        .creator-btn {
            color: #ffffff;
            background-color: @mian-color;
        }
        .creator-btn:hover {
            background-color: #1a7de8;
        }
        .btn-icon {
            font-family: fontawesome;
            margin-right: 4PX;
        }

        .login-btn {
            color: #ffffff;
            background-color: @mian-color;
        }
        .login-btn:hover {
            background-color: #1a7de8;
        }

        .header-user {
            display: flex;
            align-items: center;
            gap: 8PX;
            cursor: pointer;
        }

        .header-avatar {
            width: 32PX;
            height: 32PX;
            border-radius: 50%;
            object-fit: cover;
        }
        .header-avatar-default {
            width: 32PX;
            height: 32PX;
            border-radius: 50%;
            background-color: @mian-color;
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: fontawesome;
            font-size: 16PX;
        }
        .header-username {
            font-size: 14PX;
            color: #333;
            max-width: 80PX;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .header-user {
            position: relative;
        }

        .user-dropdown {
            position: absolute;
            top: 100%;
            right: 0;
            margin-top: 8PX;
            background-color: #ffffff;
            border-radius: 16PX;
            box-shadow: 0 8PX 32PX rgba(0,0,0,0.15);
            width: 280PX;
            z-index: 200;
            overflow: hidden;
        }

        .dropdown-user-section {
            display: flex;
            align-items: center;
            padding: 16PX 16PX 12PX;
            cursor: pointer;
            gap: 12PX;
        }
        .dropdown-user-section:hover {
            background-color: #f7f8fa;
        }
        .dropdown-avatar {
            width: 56PX;
            height: 56PX;
            border-radius: 50%;
            object-fit: cover;
            border: 2PX solid #1e80ff;
        }
        .dropdown-avatar-icon {
            width: 56PX;
            height: 56PX;
            border-radius: 50%;
            background: #e4e6eb;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: fontawesome;
            font-size: 24PX;
            color: #8a919f;
        }
        .dropdown-user-info {
            flex: 1;
        }
        .dropdown-username {
            font-size: 16PX;
            font-weight: 600;
            color: #252933;
            margin-bottom: 4PX;
        }
        .dropdown-user-level {
            font-size: 12PX;
            color: #1e80ff;
            background: #eaf2ff;
            padding: 2PX 8PX;
            border-radius: 4PX;
            display: inline-block;
        }
        .dropdown-diamond {
            display: flex;
            align-items: center;
            gap: 4PX;
            padding: 4PX 10PX;
            background: #fff7e6;
            border-radius: 12PX;
            cursor: pointer;
        }
        .dropdown-diamond .diamond-icon {
            font-family: fontawesome;
            font-size: 12PX;
            color: #fa8c16;
        }
        .dropdown-diamond .diamond-text {
            font-size: 12PX;
            color: #fa8c16;
        }
        .dropdown-diamond .diamond-arrow {
            font-family: fontawesome;
            font-size: 12PX;
            color: #fa8c16;
        }

        .dropdown-level-bar {
            display: flex;
            align-items: center;
            padding: 8PX 16PX;
            cursor: pointer;
            gap: 8PX;
            border-bottom: 1PX solid #f2f3f5;
        }
        .dropdown-level-bar:hover { background-color: #f7f8fa; }
        .dropdown-level-bar .level-label {
            font-size: 12PX;
            color: #1e80ff;
            white-space: nowrap;
        }
        .dropdown-level-bar .level-progress-wrap {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 6PX;
        }
        .dropdown-level-bar .level-progress-bar {
            flex: 1;
            height: 6PX;
            background: #e4e6eb;
            border-radius: 3PX;
            overflow: hidden;
        }
        .dropdown-level-bar .level-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #1e80ff, #4096ff);
            border-radius: 3PX;
            transition: width 0.3s;
        }
        .dropdown-level-bar .level-text {
            font-size: 11PX;
            color: #8a919f;
            white-space: nowrap;
        }
        .dropdown-level-bar .level-arrow {
            font-family: fontawesome;
            font-size: 14PX;
            color: #8a919f;
            margin-left: 4PX;
        }

        .dropdown-stats {
            display: flex;
            justify-content: space-around;
            padding: 8PX 16PX 0;
        }
        .dropdown-stats .stat-item {
            text-align: center;
            cursor: pointer;
            padding: 8PX 12PX;
            border-radius: 8PX;
        }
        .dropdown-stats .stat-item:hover { background: #f7f8fa; }
        .dropdown-stats .stat-value {
            font-size: 18PX;
            font-weight: 600;
            color: #252933;
        }
        .dropdown-stats .stat-label {
            font-size: 12PX;
            color: #8a919f;
            margin-top: 2PX;
        }

        .dropdown-divider {
            height: 1PX;
            background: #f2f3f5;
            margin: 4PX 0;
        }

        .dropdown-menu-section {
            padding: 4PX 0;
        }

        .dropdown-bottom-section {
            padding: 4PX 0;
        }

        .dropdown-item {
            display: flex;
            align-items: center;
            padding: 10PX 16PX;
            cursor: pointer;
            transition: background-color 0.2s;
            gap: 8PX;
        }

        .dropdown-item:hover {
            background-color: #f5f5f5;
        }

        .logout-item {
            color: #ff4d4f;
        }
        .logout-item:hover {
            background-color: #fff2f0;
        }

        .dropdown-icon {
            font-family: fontawesome;
            font-size: 14PX;
            width: 16PX;
            text-align: center;
        }

        .dropdown-label {
            font-size: 14PX;
            color: #333;
            flex: 1;
        }
        .logout-item .dropdown-label {
            color: #ff4d4f;
        }

        .desktop-container {
            max-width: 1280PX;
            margin: 0 auto;
            padding: 80PX 24PX 24PX;
            display: flex;
            gap: 20PX;
            box-sizing: border-box;
            width: 100%;
        }

        .desktop-sidebar {
            width: 220PX;
            flex-shrink: 0;
            position: sticky;
            top: 80PX;
            height: fit-content;
        }

        .sidebar-nav {
            background-color: #ffffff;
            border-radius: 8PX;
            padding: 8PX 0;
        }

        .nav-item {
            display: flex;
            align-items: center;
            padding: 9PX 20PX;
            cursor: pointer;
            font-size: 15PX;
            color: #333;
            transition: all 0.2s;
            gap: 12PX;
        }
        .nav-item:hover {
            background-color: #f5f5f5;
        }
        .nav-item.active {
            color: #1E80FF;
            font-weight: 500;
            background-color: #E8F3FF;
        }
        .nav-icon {
            font-family: fontawesome;
            font-size: 18PX;
            width: 20PX;
            text-align: center;
        }
        .nav-text {
            flex: 1;
        }

        .desktop-content {
            flex: 1;
            min-width: 0;
            max-width: none;
        }

        .desktop-aside {
            width: 280PX;
            flex-shrink: 0;
            position: sticky;
            top: 80PX;
            height: fit-content;
        }

        .aside-card {
            background-color: #ffffff;
            border-radius: 8PX;
            padding: 16PX 20PX;
            margin-bottom: 12PX;
        }

        .aside-title {
            font-size: 15PX;
            font-weight: 600;
            color: #333;
            margin-bottom: 12PX;
            padding-bottom: 8PX;
            border-bottom: 1PX solid #f0f0f0;
        }

        .author-item {
            display: flex;
            align-items: center;
            padding: 8PX 0;
            gap: 10PX;
        }

        .author-avatar {
            width: 40PX;
            height: 40PX;
            border-radius: 50%;
            object-fit: cover;
            flex-shrink: 0;
        }

        .author-info {
            flex: 1;
            min-width: 0;
        }

        .author-name {
            font-size: 14PX;
            color: #333;
            font-weight: 500;
            margin-bottom: 2PX;
        }

        .author-desc {
            font-size: 12PX;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .follow-btn {
            padding: 4PX 12PX;
            border: 1PX solid @mian-color;
            border-radius: 4PX;
            color: @mian-color;
            font-size: 12PX;
            cursor: pointer;
            transition: all 0.2s;
            flex-shrink: 0;
        }
        .follow-btn:hover {
            background-color: @mian-color;
            color: #fff;
        }

        .aside-more {
            text-align: center;
            padding-top: 8PX;
            font-size: 13PX;
            color: #999;
            cursor: pointer;
        }
        .aside-more:hover {
            color: @mian-color;
        }

        .rank-list {
            display: flex;
            flex-direction: column;
        }
        .rank-item {
            display: flex;
            align-items: center;
            padding: 6PX 0;
            cursor: pointer;
            gap: 10PX;
            transition: color 0.2s;
        }
        .rank-item:hover .rank-title {
            color: #1E80FF;
        }
        .rank-num {
            width: 20PX;
            height: 20PX;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 13PX;
            font-weight: 600;
            color: #C2C8D1;
            flex-shrink: 0;
            border-radius: 4PX;
        }
        .rank-1 { color: #F53F3F; }
        .rank-2 { color: #FF7D00; }
        .rank-3 { color: #FFB400; }
        .rank-title {
            font-size: 14PX;
            color: #515767;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            line-height: 1.8;
        }
        .author-list {
            display: flex;
            flex-direction: column;
        }
        .author-list .author-item {
            display: flex;
            align-items: center;
            padding: 8PX 0;
            gap: 10PX;
            cursor: pointer;
        }
        .author-list .author-avatar {
            width: 36PX;
            height: 36PX;
        }
        .author-list .author-name {
            font-size: 14PX;
            color: #333;
            font-weight: 500;
            margin-bottom: 2PX;
        }
        .author-list .author-desc {
            font-size: 12PX;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .tag-list {
            display: flex;
            flex-wrap: wrap;
            gap: 8PX;
        }

        .tag-item {
            padding: 4PX 10PX;
            background-color: #f5f5f5;
            border-radius: 3PX;
            font-size: 13PX;
            color: #666;
            cursor: pointer;
            transition: all 0.2s;
        }
        .tag-item:hover {
            background-color: @mian-color;
            color: #fff;
        }

        .aside-footer {
            padding: 12PX 16PX;
            text-align: center;
        }

        .footer-links {
            font-size: 12PX;
            color: #bbb;
            margin-bottom: 6PX;
        }

        .footer-copy {
            font-size: 12PX;
            color: #ddd;
        }
    }

    /* 1024px-1279px：隐藏左侧边栏 */
    @media screen and (min-width: 1024PX) and (max-width: 1279PX) {
        .desktop-sidebar {
            display: none;
        }
        .desktop-aside {
            width: 280PX;
        }
        .header-inner {
            padding: 0 20PX;
        }
        .desktop-container {
            padding: 80PX 20PX 24PX;
        }
    }

    /* 768px-1023px：隐藏左右两侧边栏 */
    @media screen and (min-width: 768PX) and (max-width: 1023PX) {
        .desktop-sidebar {
            display: none;
        }
        .desktop-aside {
            display: none;
        }
        .header-inner {
            padding: 0 16PX;
        }
        .desktop-container {
            padding: 80PX 16PX 24PX;
        }
        .main-nav {
            margin: 0 12PX;
        }
        .nav-link {
            padding: 0 8PX;
            font-size: 13PX;
        }
    }

    /* 更窄屏Web端适配：左侧导航缩为图标 */
    @media screen and (min-width: 768PX) and (max-width: 899PX) {
        .desktop-sidebar {
            width: 60PX;
        }
        .nav-text {
            display: none;
        }
        .nav-item {
            padding: 12PX 0;
            justify-content: center;
        }
        .header-center {
            margin: 0 16PX;
        }
        .header-left {
            width: auto;
        }
        .logo-text {
            font-size: 18PX;
        }
        .header-right {
            width: auto;
            gap: 8PX;
        }
        .write-btn {
            display: none;
        }
    }
</style>
