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
                    <div class="dropdown-user-section" @click="goToProfile">
                        <img v-if="userAvatar" class="dropdown-avatar" :src="userAvatar" alt="头像" />
                        <span v-else class="dropdown-avatar-icon">&#xf007;</span>
                        <div class="dropdown-user-info">
                            <div class="dropdown-username">{{ userName }}</div>
                            <div class="dropdown-user-level">掘友等级 Lv.2</div>
                        </div>
                    </div>
                    <div class="dropdown-stats">
                        <div class="stat-item">
                            <div class="stat-value">{{ userStats.followCount }}</div>
                            <div class="stat-label">关注</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ userStats.likeCount }}</div>
                            <div class="stat-label">赞过</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ userStats.collectCount }}</div>
                            <div class="stat-label">收藏</div>
                        </div>
                    </div>
                    <div class="dropdown-divider"></div>
                    <div class="dropdown-menu-section">
                        <div class="dropdown-item" @click="goToProfile">
                            <span class="dropdown-icon">&#xf007;</span>
                            <span class="dropdown-label">我的主页</span>
                        </div>
                        <div class="dropdown-item" @click="handleMyDiscount">
                            <span class="dropdown-icon">&#xf155;</span>
                            <span class="dropdown-label">我的优惠</span>
                        </div>
                        <div class="dropdown-item" @click="handleCourseCenter">
                            <span class="dropdown-icon">&#xf19c;</span>
                            <span class="dropdown-label">课程中心</span>
                        </div>
                        <div class="dropdown-item" @click="handleMyFootprint">
                            <span class="dropdown-icon">&#xf02d;</span>
                            <span class="dropdown-label">我的足迹</span>
                        </div>
                    </div>
                    <div class="dropdown-divider"></div>
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
    export default {
        name: "HomeBar",
        components: { Search},
        data:()=>{
            return {
                icon:'\uF002',
                showUserDropdown: false,
                userStats: {
                    followCount: 4,
                    likeCount: 0,
                    collectCount: 0
                }
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
                } else {
                    this.$store.dispatch('showLogin')
                }
            },
            handleLogout() {
                this.showUserDropdown = false
                this.$store.dispatch('logout')
                toast('已退出登录', 2)
            },
            goToProfile: function() {
                this.showUserDropdown = false
                this.$router.push('/user/' + (this.userInfo.userId || 1))
            },
            goToSettings: function() {
                this.showUserDropdown = false
                this.$router.push('/user/settings')
            },
            handleMyDiscount: function() {
                this.showUserDropdown = false
                toast('我的优惠功能开发中', 2)
            },
            handleCourseCenter: function() {
                this.showUserDropdown = false
                toast('课程中心功能开发中', 2)
            },
            handleMyFootprint: function() {
                this.showUserDropdown = false
                toast('我的足迹功能开发中', 2)
            },
            goToNotification: function() {
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
            document.addEventListener('click', this.closeDropdown)
        },
        beforeDestroy() {
            document.removeEventListener('click', this.closeDropdown)
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
    }
    .user-avatar {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        border: 2px solid rgba(255,255,255,0.5);
        object-fit: cover;
    }

    .user-info {
        position: relative;
    }

    .user-dropdown {
        position: absolute;
        top: 100%;
        right: 0;
        margin-top: 8px;
        background-color: #ffffff;
        border-radius: 6px;
        box-shadow: 0 4px 16px rgba(0,0,0,0.15);
        min-width: 240px;
        z-index: 200;
        overflow: hidden;
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

    .dropdown-user-section {
        display: flex;
        align-items: center;
        padding: 16px;
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

    .dropdown-stats {
        display: flex;
        justify-content: space-around;
        padding: 12px 16px;
        border-top: 1px solid #f2f3f5;
        border-bottom: 1px solid #f2f3f5;
    }
    .dropdown-stats .stat-item {
        text-align: center;
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
        height: 8px;
        background: #f7f8fa;
    }

    .dropdown-menu-section, .dropdown-bottom-section {
        padding: 4px 0;
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
