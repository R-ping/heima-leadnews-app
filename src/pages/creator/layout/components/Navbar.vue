<template>
  <div class="navbar">
    <div class="left-menu">
      <hamburger :toggle-click="toggleSideBar" class="hamburger-container"/>
      <router-link to="/home" class="brand-container">
        <img src="/static/images/logo-icon.svg" width="24" height="24" class="brand-icon" alt="黑马头条">
        <span class="brand-name">黑马头条</span>
        <span class="brand-divider">·</span>
        <span class="brand-subtitle">创作者中心</span>
      </router-link>
    </div>
    <div class="right-menu">
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
import defaultAvatar from '@/static/images/avatar_head_1.png'
export default {
  components: {
    Hamburger
  },
  computed: {
    ...mapGetters(['userInfo']),
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
  }
}
</style>
