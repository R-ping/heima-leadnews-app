<template>
    <div class="user-center-sidebar">
        <div class="user-card">
            <div class="user-avatar">
                <img v-if="userInfo && userInfo.avatar" :src="userInfo.avatar" alt="avatar" />
                <span v-else class="avatar-default">&#xf007;</span>
            </div>
            <div class="user-name">{{ displayName }}</div>
            <div class="user-level">{{ displayLevel }}</div>
        </div>

        <div class="side-menu">
            <div
                v-for="item in menuList"
                :key="item.key"
                class="menu-item"
                :class="{ active: item.key === activeMenu }"
                @click="handleMenuClick(item.key)"
            >
                <span class="menu-icon" v-html="item.icon"></span>
                <span class="menu-text">{{ item.label }}</span>
            </div>
        </div>

        <div class="side-footer">用户协议 · 法律声明 ©{{ currentYear }} 稀土掘金</div>
    </div>
</template>

<script>
import store from '@/stores/store'

const MENU_CONFIG = [
    { key: 'checkin', label: '每日签到', icon: '&#xf058;' },
    { key: 'growth', label: '成长等级', icon: '&#xf201;' },
    { key: 'lottery', label: '幸运抽奖', icon: '&#xf1d3;' },
    { key: 'welfare', label: '福利兑换', icon: '&#xf290;' },
    { key: 'harvest', label: '我的收获', icon: '&#xf091;' }
]

export default {
    name: 'UserCenterSidebar',
    props: {
        activeMenu: {
            type: String,
            default: 'checkin',
            validator: function (val) {
                return ['checkin', 'growth', 'lottery', 'welfare', 'harvest'].indexOf(val) !== -1
            }
        },
        userInfo: {
            type: Object,
            default: null
        }
    },
    data() {
        return {
            currentYear: new Date().getFullYear(),
            menuList: MENU_CONFIG
        }
    },
    computed: {
        resolvedUserInfo() {
            if (this.userInfo && Object.keys(this.userInfo).length) {
                return this.userInfo
            }
            return store.state.userInfo || {}
        },
        displayName() {
            const info = this.resolvedUserInfo
            return info.nickname || info.nickName || '用户'
        },
        displayLevel() {
            const info = this.resolvedUserInfo
            return info.level || 'JY.1'
        }
    },
    methods: {
        handleMenuClick(key) {
            this.$emit('menu-click', key)
        }
    }
}
</script>

<style lang="less" scoped>
.user-center-sidebar {
    width: 180px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
}

.user-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px 16px;
    text-align: center;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.user-avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    overflow: hidden;
    margin: 0 auto 10px;
    background: linear-gradient(135deg, #1e80ff, #69b1ff);
    display: flex;
    align-items: center;
    justify-content: center;
}

.user-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-default {
    font-family: fontawesome;
    font-size: 28px;
    color: #fff;
}

.user-name {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 4px;
}

.user-level {
    font-size: 12px;
    color: #fa8c16;
    background: #fff7e6;
    padding: 2px 10px;
    border-radius: 10px;
    display: inline-block;
}

.side-menu {
    background: #fff;
    border-radius: 12px;
    padding: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    flex: 1;
}

.menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 14px;
    font-size: 14px;
    color: #666;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: #f5f7fa;
        color: #1e80ff;

        .menu-icon {
            color: #1e80ff;
        }
    }

    &.active {
        background: linear-gradient(135deg, #1e80ff, #69b1ff);
        color: #fff;

        .menu-icon {
            color: #fff;
        }
    }
}

.menu-icon {
    font-family: fontawesome;
    font-size: 16px;
    color: #999;
    width: 20px;
    text-align: center;
}

.side-footer {
    text-align: center;
    font-size: 11px;
    color: #ccc;
    margin-top: 16px;
}
</style>
