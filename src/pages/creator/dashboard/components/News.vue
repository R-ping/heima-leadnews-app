<template>
  <section class="dashboard-activities">
    <header class="section-header">
      <span>创作活动</span>
      <a class="more-link" href="javascript:;">查看更多 ></a>
    </header>
    <div class="activities-list">
      <div class="activity-card" v-for="(activity, index) in activities" :key="index">
        <div class="activity-banner" :style="{ background: activity.gradient }">
          <span class="activity-tag">{{ activity.tag }}</span>
        </div>
        <div class="activity-info">
          <h4 class="activity-title">{{ activity.title }}</h4>
          <p class="activity-desc">{{ activity.desc }}</p>
        </div>
      </div>
      <div class="topic-section">
        <h5 class="topic-title">热门话题</h5>
        <div class="topic-tags">
          <span class="topic-tag" v-for="tag in topics" :key="tag">#{{ tag }}#</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script>

export default {
  data() {
    return {
      activities: [
        {
          tag: '活动',
          title: '每日精选文章征集',
          desc: '分享你的技术心得，赢取流量扶持',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          tag: '活动',
          title: '新人创作者计划',
          desc: '首次发布即享专属权益',
          gradient: 'linear-gradient(135deg, #1e80ff 0%, #36cfc9 100%)'
        }
      ],
      topics: ['每日精选文章', '前端开发', '后端架构']
    }
  }
}
</script>

<style lang="less" scoped>
  @import '../../layout/styles/variables.less';

  .dashboard-activities {
    display: flex;
    flex-direction: column;
    height: 100%;

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 24px;
      font-size: 18px;
      font-weight: 600;
      color: @textPrimary;
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

    .activities-list {
      padding: 20px 24px 24px;
      flex: 1;
    }

    .activity-card {
      border-radius: @cardRadius;
      overflow: hidden;
      margin-bottom: 16px;
      border: 1px solid #f2f3f5;
      transition: box-shadow 0.2s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        box-shadow: @cardShadow;
      }

      .activity-banner {
        height: 90px;
        display: flex;
        align-items: flex-start;
        padding: 14px;
        position: relative;

        .activity-tag {
          display: inline-block;
          padding: 3px 10px;
          background: rgba(255,255,255,0.25);
          color: #fff;
          font-size: 13px;
          border-radius: 2px;
          backdrop-filter: blur(4px);
        }
      }

      .activity-info {
        padding: 14px;
        background: #fff;

        .activity-title {
          margin: 0 0 6px;
          font-size: 15px;
          font-weight: 500;
          color: @textPrimary;
        }

        .activity-desc {
          margin: 0;
          font-size: 13px;
          color: @textMuted;
        }
      }
    }

    .topic-section {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #f2f3f5;

      .topic-title {
        margin: 0 0 12px;
        font-size: 15px;
        font-weight: 500;
        color: @textSecondary;
      }

      .topic-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
      }

      .topic-tag {
        display: inline-block;
        padding: 5px 12px;
        font-size: 13px;
        color: @brandBlue;
        background: #eaf2ff;
        border-radius: 2px;
        cursor: pointer;
        transition: background 0.2s;

        &:hover {
          background: #d5e5ff;
        }
      }
    }
  }
</style>
