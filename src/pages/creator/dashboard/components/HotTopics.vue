<template>
  <section class="hot-topics">
    <header class="section-header">
      <span>推荐话题</span>
      <span class="refresh-btn" @click="refreshTopics">换一换</span>
    </header>
    <div class="topic-list">
      <div class="topic-item" v-for="topic in topics" :key="topic.id" @click="goTopic(topic)">
        <div class="topic-info">
          <span class="topic-title">
            <span class="topic-badge" v-if="topic.badge">{{ topic.badge }}</span>
            <span>#</span>{{ topic.name }}
          </span>
          <span class="topic-meta">{{ formatCount(topic.participantCount) }}位掘友已发布 · {{ formatCount(topic.viewCount) }}阅读</span>
        </div>
      </div>
      <div v-if="topics.length === 0 && !loading" class="empty-tip">暂无推荐话题</div>
    </div>
    <footer class="section-footer">
      <span class="more-link" @click="goSquare">查看更多 &gt;</span>
    </footer>
  </section>
</template>

<script>
import { getRecommendTopics } from '@/apis/topic'

export default {
  name: 'HotTopics',
  data() {
    return {
      topics: [],
      page: 0,
      loading: false
    }
  },
  mounted() {
    this.loadTopics()
  },
  methods: {
    async loadTopics() {
      this.loading = true
      try {
        const res = await getRecommendTopics(this.page, 5)
        if (res.data && res.data.code === 200) {
          this.topics = res.data.data.list || []
        }
      } catch (e) {
        console.error('加载推荐话题失败:', e)
      } finally {
        this.loading = false
      }
    },
    refreshTopics() {
      this.page++
      this.loadTopics()
    },
    formatCount(num) {
      if (!num) return '0'
      if (num >= 10000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return num.toString()
    },
    goTopic(topic) {
      this.$router.push(`/pin/topic/${topic.id}`)
    },
    goSquare() {
      this.$router.push('/pin/topics')
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

      .refresh-btn {
        font-size: 13px;
        font-weight: 400;
        color: @brandBlue;
        cursor: pointer;
        &:hover { opacity: 0.8; }
      }
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
          .topic-badge {
            display: inline-block;
            background: #ff6b35;
            color: #fff;
            font-size: 11px;
            padding: 1px 5px;
            border-radius: 3px;
            margin-right: 4px;
            vertical-align: middle;
          }
        }
        .topic-meta {
          font-size: 13px;
          font-weight: 400;
          color: @colorStatLabel;
        }
      }
    }

    .empty-tip {
      padding: 20px;
      text-align: center;
      color: #999;
      font-size: 13px;
    }

    .section-footer {
      padding: 12px 20px;
      border-top: 1px solid #f2f3f5;
      text-align: center;
      flex-shrink: 0;

      .more-link {
        font-size: 14px;
        color: @brandBlue;
        cursor: pointer;
        &:hover { opacity: 0.8; }
      }
    }
  }
</style>