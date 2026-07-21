<template>
  <div class="creator-dashboard">
    <!-- 左侧主内容区 -->
    <div class="dashboard-main">
      <user-info class="dashboard-header" />

      <!-- 数据概览卡片 -->
      <Article class="dashboard-card dashboard-article" />

      <!-- 创作成长 + 热门话题 两栏 -->
      <div class="dashboard-bottom">
        <div class="dashboard-card dashboard-growth">
          <GrowthTasks />
        </div>
        <div class="dashboard-card dashboard-hot">
          <HotTopics />
        </div>
      </div>
    </div>

    <!-- 右侧边栏 -->
    <RightAside class="dashboard-aside" />
  </div>
</template>

<script>
import UserInfo from './components/UserInfo.vue'
import Article from './components/Article.vue'
import GrowthTasks from './components/GrowthTasks.vue'
import HotTopics from './components/HotTopics.vue'
import RightAside from './components/RightAside.vue'

export default {
  name: 'CreatorDashboard',
  components: {
    UserInfo,
    Article,
    GrowthTasks,
    HotTopics,
    RightAside
  }
}
</script>

<style lang="less">
  @import '../layout/styles/variables.less';

  .creator-dashboard {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 280px;
    gap: 20px;
    padding: 24px;
    background: @bgGray;
    min-height: calc(100vh - 60px);

    .dashboard-main {
      display: flex;
      flex-direction: column;
      gap: 20px;
      min-width: 0;
    }

    .dashboard-header {
      background: @colorCreatorHeaderBg;
      border-bottom: 1px solid @colorCreatorHeaderBorder;
      border-radius: 8px;
      overflow: hidden;
      transition: box-shadow 0.25s ease;
    }

    .dashboard-card {
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
      overflow: hidden;
      transition: box-shadow 0.25s ease, transform 0.25s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.10);
        transform: translateY(-2px);
      }
    }

    .dashboard-article {
      flex: none;
    }

    .dashboard-bottom {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      min-height: 280px;
    }

    .dashboard-growth {
      min-height: 280px;
    }

    .dashboard-hot {
      min-height: 280px;
    }

    .dashboard-aside {
      min-width: 0;
    }
  }

  @media screen and (max-width: 768px) {
    .creator-dashboard {
      grid-template-columns: 1fr;
      padding: 12px;

      .dashboard-main {
        gap: 12px;
      }

      .dashboard-bottom {
        grid-template-columns: 1fr;
        gap: 12px;
      }

      .dashboard-aside {
        display: none;
      }

      .dashboard-card:hover {
        transform: none;
      }
    }
  }
</style>
