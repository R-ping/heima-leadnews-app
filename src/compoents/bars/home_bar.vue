<template>
    <div class="bar_bg">
        <span class="bar-icon menu-icon">&#xf0c9;</span>
        <Search class="search-comp" type="search" @onClick="onClick" :icon="icon" :height="56" :left-width="15" :right-width="15" placeholder="搜索文章"/>
        <span class="bar-icon login-btn" v-if="!isLoggedIn" @click="showLogin">&#xf007;</span>
        <div class="user-info" v-if="isLoggedIn" @click="toggleUserDropdown">
            <div class="notification-bell" @click.stop="goToNotification">
                <span class="bell-icon">&#xf0f3;</span>
            </div>
            <img v-if="userAvatar" class="user-avatar" :src="userAvatar" alt="头像" />
            <span v-else class="bar-icon user-btn">&#xf007;</span>
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
</template>

<script>
    import Search from '@/compoents/inputs/search_buttion';
    import { toast } from "@/utils/toast"
    import { getUserStatistics } from '@/apis/user'

    export default {
        name: "HomeBar",
        components: { Search},
        data:()=>{
            return {
                icon:'\uF002',
                showUserDropdown: false,
                stats: {
                    followCount: 0,
                    likeCount: 0,
                    collectCount: 0
                },
                levelScore: 0,
                levelMax: 150,
                levelPercent: 0,
                diamondCount: '0',
                levelBadge: 'ZR.1'
            }
        },
        computed: {
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
        methods: {
            onClick : function(){
                this.$router.push('/search')
            },
            showLogin() {
                this.$store.dispatch('showLogin')
            },
            toggleUserDropdown() {
                if (this.isLoggedIn) {
                    this.showUserDropdown = !this.showUserDropdown
                    if (this.showUserDropdown) {
                        this.loadUserStats()
                    }
                } else {
                    this.$store.dispatch('showLogin')
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
                            // Calculate max score for current level
                            const levelMaxMap = { 1: 150, 2: 300, 3: 500, 4: 800, 5: 1200 }
                            this.levelMax = levelMaxMap[li.dailyLevel] || 150
                            // Calculate base score for current level
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
            handleLogout() {
                this.showUserDropdown = false
                this.$store.dispatch('logout')
                toast('已退出登录', 2)
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
            goToNotification() {
                this.showUserDropdown = false
                this.$router.push('/notification')
            }
        },
        mounted() {
            this.closeDropdown = (e) => {
                if (this.showUserDropdown && !this.$el.querySelector('.user-info').contains(e.target)) {
                    this.showUserDropdown = false
                }
            }
            this.escClose = (e) => {
                if (e.key === 'Escape' && this.showUserDropdown) {
                    this.showUserDropdown = false
                }
            }
            document.addEventListener('click', this.closeDropdown)
            document.addEventListener('keydown', this.escClose)
        },
        beforeDestroy() {
            document.removeEventListener('click', this.closeDropdown)
            document.removeEventListener('keydown', this.escClose)
        }
    };
</script>

<style lang="less" scoped>
    @import '../../styles/common';

    .bar_bg{
        width: 100%;
        display: flex;
        flex-direction: row;
        align-items: center;
        background-color: @mian-color;
        height: @top-height;
        padding: 0 15px;
        box-sizing: border-box;
        gap: 8px;
    }
    .bar-icon{
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        font-family: fontawesome;
        font-size: 34px;
        text-align: center;
        flex-shrink: 0;
    }
    .menu-icon {
        font-size: 36px;
    }
    .login-btn, .user-btn {
        cursor: pointer;
    }
    .search-comp {
        flex: 1;
        min-width: 0;
    }
    .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        flex-shrink: 0;
        position: relative;
    }
    .user-avatar {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        border: 2px solid rgba(255,255,255,0.5);
        object-fit: cover;
    }

    .user-dropdown {
        position: absolute;
        top: 100%;
        right: 0;
        margin-top: 8px;
        background-color: #ffffff;
        border-radius: 16px;
        box-shadow: 0 8px 32px rgba(0,0,0,0.15);
        width: 280px;
        z-index: 200;
        overflow: hidden;
    }

    .dropdown-user-section {
        display: flex;
        align-items: center;
        padding: 16px 16px 12px;
        cursor: pointer;
        gap: 12px;
    }
    .dropdown-user-section:hover {
        background-color: #f7f8fa;
    }
    .dropdown-avatar {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid #1e80ff;
    }
    .dropdown-avatar-icon {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        background: #e4e6eb;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family: fontawesome;
        font-size: 24px;
        color: #8a919f;
    }
    .dropdown-user-info {
        flex: 1;
    }
    .dropdown-username {
        font-size: 16px;
        font-weight: 600;
        color: #252933;
        margin-bottom: 4px;
    }
    .dropdown-user-level {
        font-size: 12px;
        color: #1e80ff;
        background: #eaf2ff;
        padding: 2px 8px;
        border-radius: 4px;
        display: inline-block;
    }
    .dropdown-diamond {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: #fff7e6;
        border-radius: 12px;
        cursor: pointer;
        .diamond-icon {
            font-family: fontawesome;
            font-size: 12px;
            color: #fa8c16;
        }
        .diamond-text {
            font-size: 12px;
            color: #fa8c16;
        }
        .diamond-arrow {
            font-family: fontawesome;
            font-size: 12px;
            color: #fa8c16;
        }
    }

    .dropdown-level-bar {
        display: flex;
        align-items: center;
        padding: 8px 16px;
        cursor: pointer;
        gap: 8px;
        &:hover { background-color: #f7f8fa; }
        .level-label {
            font-size: 12px;
            color: #1e80ff;
            white-space: nowrap;
        }
        .level-progress-wrap {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .level-progress-bar {
            flex: 1;
            height: 6px;
            background: #e4e6eb;
            border-radius: 3px;
            overflow: hidden;
        }
        .level-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #1e80ff, #4096ff);
            border-radius: 3px;
            transition: width 0.3s;
        }
        .level-text {
            font-size: 11px;
            color: #8a919f;
            white-space: nowrap;
        }
        .level-arrow {
            font-family: fontawesome;
            font-size: 14px;
            color: #8a919f;
        }
    }

    .dropdown-stats {
        display: flex;
        justify-content: space-around;
        padding: 8px 16px 0;
    }
    .dropdown-stats .stat-item {
        text-align: center;
        cursor: pointer;
        padding: 8px 12px;
        border-radius: 8px;
        &:hover { background: #f7f8fa; }
    }
    .dropdown-stats .stat-value {
        font-size: 18px;
        font-weight: 600;
        color: #252933;
    }
    .dropdown-stats .stat-label {
        font-size: 12px;
        color: #8a919f;
        margin-top: 2px;
    }

    .dropdown-divider {
        height: 1px;
        background: #f2f3f5;
        margin: 4px 0;
    }

    .dropdown-menu-section {
        padding: 4px 0;
    }

    .dropdown-bottom-section {
        padding: 4px 0;
    }

    .dropdown-item {
        display: flex;
        align-items: center;
        padding: 10px 16px;
        cursor: pointer;
        transition: background-color 0.2s;
        gap: 8px;
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
        font-size: 14px;
        width: 16px;
        text-align: center;
    }
    .dropdown-label {
        font-size: 14px;
        color: #333;
        flex: 1;
    }
    .logout-item .dropdown-label {
        color: #ff4d4f;
    }

    .notification-bell {
        width: 44px;
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        border-radius: 50%;
        transition: background-color 0.2s;
        margin-right: 8px;
    }
    .notification-bell:hover {
        background-color: rgba(255,255,255,0.15);
    }
    .bell-icon {
        font-family: fontawesome;
        font-size: 24px;
        color: #ffffff;
    }
</style>