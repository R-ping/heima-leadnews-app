<template>
  <section class="hot-topics">
    <header class="section-header">
      <span>创作活动 / 热门话题</span>
    </header>
    <div class="topic-list">
      <div class="topic-item" v-for="(topic, idx) in topics" :key="idx" @click="goTopic(topic)">
        <div class="topic-info">
          <span class="topic-title"><span>#</span>{{ topic.title }}</span>
          <span class="topic-meta">{{ topic.participate }}k参与 · {{ topic.read }}m阅读</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'HotTopics',
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

  .hot-topics {
    display: flex;
    flex-direction: column;
    height: 100%;

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      font-size: 18px;
      font-weight: 600;
      color: @textPrimary;
      border-bottom: 1px solid #f2f3f5;
      flex-shrink: 0;
    }

    .topic-list {
      padding: 4px;
      display: flex;
      flex-direction: column;
    }

    .topic-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-radius: 4px;

      &:not(:last-child) {
        border-bottom: 1px solid #F0F1F5;
      }

      &:hover {
        background-color: @colorTopicHover;
      }

      .topic-info {
        flex: 1;
        .topic-title {
          font-size: 15px;
          font-weight: 500;
          color: @brandBlue;
          margin-bottom: 4px;
          span {
            color: @brandBlue;
          }
        }
        .topic-meta {
          font-size: 13px;
          font-weight: 400;
          color: @colorStatLabel;
        }
      }
    }
  }
</style>