<template>
  <section class="dashboard-stats">
    <header class="section-header">
      <span>数据概览</span>
      <a class="more-link" href="javascript:;">查看更多 ></a>
    </header>
    <div class="stats-grid">
      <div class="stat-card" v-for="item in stats" :key="item.label">
        <div class="stat-value">{{ item.value }}</div>
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-compare">较前日 --</div>
      </div>
    </div>
  </section>
</template>

<script>

export default {
  data() {
    return {
      stats: [
        { label: '总粉丝数', value: 0 },
        { label: '文章展现数', value: 0 },
        { label: '文章阅读数', value: 0 },
        { label: '文章点赞数', value: 0 },
        { label: '文章评论数', value: 0 },
        { label: '文章收藏数', value: 0 }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
  @import '../../layout/styles/variables.less';

  .dashboard-stats {
    display: flex;
    flex-direction: column;
    height: 100%;

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 24px;
      font-size: 14px;
      font-weight: 500;
      color: @textSecondary;
      border-bottom: 1px solid #f2f3f5;
      flex-shrink: 0;

      .more-link {
        font-size: 14px;
        font-weight: 400;
        color: @textMuted;
        text-decoration: none;

        &:hover {
          color: @brandBlue;
        }
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      padding: 24px;
      flex: 1;
    }

    .stat-card {
      padding: 20px 24px;
      background: #FFFFFF;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
      transition: all 0.25s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.10);
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: @textPrimary;
        margin-bottom: 8px;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: @colorStatLabel;
        margin-bottom: 8px;
        font-weight: 400;
      }

      .stat-compare {
        font-size: 13px;
        color: @colorStatLabel;
        font-weight: 400;
      }
    }
  }
</style>
