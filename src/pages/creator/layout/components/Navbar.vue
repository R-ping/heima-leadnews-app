<template>
  <div class="navbar">
    <div class="left-menu">
      <hamburger :toggle-click="toggleSideBar" class="hamburger-container"/>
      <router-link to="/home" class="brand-container">
        <img src="/static/images/logo-icon.svg" width="24" height="24" class="brand-icon" alt="逐日Coding">
        <span class="brand-name">逐日Coding</span>
        <span class="brand-divider">·</span>
        <span class="brand-subtitle">创作者中心</span>
      </router-link>
    </div>
    <div class="right-menu">
      <div class="notification-bell right-menu-item" v-if="isLoggedIn" @click="goToNotification">
        <span class="bell-icon">&#xf0f3;</span>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>
      <el-dropdown class="avatar-container right-menu-item" trigger="click">
        <div class="avatar-wrapper">
          <img class="user-avatar" :src="avatarUrl" alt="头像">
          <span class="user-name">{{ nickName }}</span>
          <i class="el-icon-caret-bottom"/>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/creator/user">
            <el-dropdown-item>
              个人信息
            </el-dropdown-item>
          </router-link>
          <el-dropdown-item divided>
            <span style="display:block;" @click="logout">退出</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Hamburger from '../../components/Hamburger/index.vue'
import { clearUser } from '../../utils/store'
import emitter from '../../utils/event'
import request from '@/common/request'
import conf from '@/common/conf'
import defaultAvatar from '@/static/images/avatar_head_1.png'
export default {
  components: {
    Hamburger
  },
  data() {
    return {
      unreadCount: 0,
      unreadTimer: null
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    isLoggedIn() {
      return this.$store.getters.isLoggedIn
    },
    avatarUrl () {
      if (this.userInfo && this.userInfo.avatar) {
        return '/static/images/' + this.userInfo.avatar + '.png'
      }
      return defaultAvatar
    },
    nickName () {
      return this.userInfo ? (this.userInfo.nickName || '用户') : '用户'
    }
  },
  methods: {
    toggleSideBar() {
      // 收缩或者展开左侧菜单
      emitter.$emit('changeCollapse')
    },
    logout() {
      clearUser() // 退出前要清除掉用户的信息
      this.$store.dispatch('logout')
      this.$router.replace({ path: '/login' })
    },
    fetchUnreadCount() {
      if (!this.isLoggedIn) return
      request.get(conf.urls.get('notifications_unread'), {}).then(d => {
        if (d && d.code === 200 && d.data) {
          this.unreadCount = d.data.total || 0
        }
      }).catch(() => {})
    },
    goToNotification() {
      this.$router.push('/notification')
    }
  },
  mounted() {
    this.fetchUnreadCount()
    this.unreadTimer = setInterval(() => this.fetchUnreadCount(), 30000)
  },
  beforeDestroy() {
    if (this.unreadTimer) {
      clearInterval(this.unreadTimer)
      this.unreadTimer = null
    }
  }
}
</script>

<style lang="less" scoped>
@import '../styles/variables.less';

.navbar {
  height: 60px;
  line-height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e6eb;
  box-shadow: @cardShadow;
  position: sticky;
  top: 0;
  z-index: 10;
  width: 100%;
  flex-shrink: 0;

  .left-menu {
    float: left;
    height: 100%;

    .hamburger-container {
      float: left;
      padding: 0 10px;
      line-height: 56px;
      cursor: pointer;
    }

    .brand-container {
      display: inline-flex;
      align-items: center;
      height: 60px;
      padding: 0 10px;
      font-size: 16px;
      color: @textPrimary;
      text-decoration: none;

      .brand-icon {
        vertical-align: middle;
        margin-right: 6px;
        flex-shrink: 0;
      }

      .brand-name {
        font-weight: 600;
      }

      .brand-divider {
        margin: 0 6px;
        color: @textMuted;
      }

      .brand-subtitle {
        color: @textSecondary;
      }
    }
  }

  .right-menu {
    float: right;
    height: 100%;
    padding-right: 20px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      margin: 0 8px;
    }

    .avatar-container {
      height: 60px;
      margin-right: 10px;

      .avatar-wrapper {
        cursor: pointer;
        position: relative;
        line-height: 60px;

        .user-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          object-fit: cover;
          vertical-align: middle;
          margin-right: 8px;
        }

        .user-name {
          font-size: 15px;
          color: @textPrimary;
          vertical-align: middle;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          margin-left: 6px;
          font-size: 12px;
          color: @textMuted;
          vertical-align: middle;
        }
      }
    }

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
      margin-top: 12px;

      &:hover {
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
    }
  }
}
</style>
