<template>
  <aside class="right-aside">
    <!-- 创作活动 -->
    <div class="aside-card activity-card">
      <h4 class="aside-title">创作活动</h4>
      <div class="activity-list">
        <div class="activity-item">
          <span class="activity-tag">创作活动</span>
        </div>
        <div class="activity-item">
          <span class="activity-tag activity-ai">TRAE AI 创造力大赛</span>
        </div>
      </div>
      <a href="javascript:;" class="more-link">查看更多 ></a>
    </div>

    <!-- 热门话题 -->
    <div class="aside-card topics-card">
      <h4 class="aside-title">热门话题</h4>
      <div class="topic-list">
        <div
          class="topic-item"
          v-for="(topic, idx) in topics"
          :key="idx"
          @click="goTopic(topic)"
        >
          <span class="topic-title">
            <span class="hash">#</span>{{ topic.title }}
          </span>
          <span class="topic-meta">{{ topic.participate }}k参与 · {{ topic.read }}m阅读</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'RightAside',
  data() {
    return {
      topics: [
        { title: '每日精选文章', participate: 26, read: 8.6 },
        { title: '日新计划', participate: 29, read: 12.0 },
        { title: '每天一个知识点', participate: 156, read: 58.5 },
        { title: '沸点周刊', participate: 7.6, read: 2.3 },
        { title: '新人报道', participate: 57, read: 43.7 }
      ]
    }
  },
  methods: {
    goTopic(topic) {
      // TODO: 跳转话题详情
    }
  }
}
</script>

<style lang="less" scoped>
  @import '../../layout/styles/variables.less';

  .right-aside {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .aside-card {
    background: #fff;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }

  .aside-title {
    margin: 0 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: @textPrimary;
  }

  .activity-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 12px;
  }

  .activity-tag {
    display: inline-block;
    padding: 6px 12px;
    font-size: 13px;
    font-weight: 500;
    color: @brandBlue;
    background-color: #E8F3FF;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #D6E9FF;
    }

    &.activity-ai {
      color: #7B4DFE;
      background-color: #F0EBFF;

      &:hover {
        background-color: #E4DCFF;
      }
    }
  }

  .more-link {
    display: inline-block;
    font-size: 13px;
    color: @textMuted;
    text-decoration: none;

    &:hover {
      color: @brandBlue;
    }
  }

  .topic-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .topic-item {
    display: flex;
    flex-direction: column;
    padding: 8px 0;
    cursor: pointer;
    border-bottom: 1px solid @borderLight;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      .topic-title {
        color: #1171ee;
      }
    }
  }

  .topic-title {
    font-size: 14px;
    font-weight: 500;
    color: @brandBlue;
    margin-bottom: 4px;
    transition: color 0.2s;

    .hash {
      margin-right: 2px;
    }
  }

  .topic-meta {
    font-size: 12px;
    color: @colorStatLabel;
  }
</style>
