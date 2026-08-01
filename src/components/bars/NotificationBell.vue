<template>
    <div class="notification-bell" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
        <span class="bell-icon" @click.stop="$emit('go-to-notification', 'comment')">&#xf0f3;</span>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        <div class="notification-dropdown" v-if="showDropdown" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
            <div class="dropdown-item" @click.stop="$emit('go-to-notification', 'comment')">
                <span>评论</span>
                <span v-if="unreadCount > 0" class="item-unread">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </div>
            <div class="dropdown-item" @click.stop="$emit('go-to-notification', 'like')">
                <span>赞和收藏</span>
            </div>
            <div class="dropdown-item" @click.stop="$emit('go-to-notification', 'follow')">
                <span>新增粉丝</span>
            </div>
            <div class="dropdown-item" @click.stop="$emit('go-to-notification', 'message')">
                <span>私信</span>
            </div>
            <div class="dropdown-item" @click.stop="$emit('go-to-notification', 'system')">
                <span>系统通知</span>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'NotificationBell',
    props: {
        unreadCount: {
            type: Number,
            default: 0
        }
    },
    data() {
        return {
            showDropdown: false
        }
    }
}
</script>

<style lang="less" scoped>
.notification-bell {
    position: relative;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    cursor: pointer;
    transition: background-color 0.2s;
    flex-shrink: 0;
}
.notification-bell:hover {
    background-color: #f4f5f5;
}
.bell-icon {
    font-family: fontawesome;
    font-size: 20px;
    color: #515767;
}
.unread-badge {
    position: absolute;
    top: 0;
    right: 0;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    background: #ff4d4f;
    color: #fff;
    font-size: 10px;
    border-radius: 8px;
    padding: 0 4px;
    transform: translate(30%, -30%);
}
.notification-dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 8px;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.12);
    z-index: 300;
    overflow: hidden;
    min-width: 160px;
}
.dropdown-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    font-size: 14px;
    color: #333;
    cursor: pointer;
    white-space: nowrap;
    transition: background-color 0.2s;
}
.dropdown-item:hover {
    background-color: #f5f7fa;
}
.item-unread {
    display: inline-block;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    background: #ff4d4f;
    color: #fff;
    font-size: 10px;
    border-radius: 8px;
    padding: 0 4px;
    margin-left: 8px;
}
</style>