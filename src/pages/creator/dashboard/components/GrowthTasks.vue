<template>
  <section class="growth-tasks">
    <header class="section-header">
      <span>创作任务</span>
    </header>
    <div class="task-list">
      <div class="task-item" v-for="(task, idx) in tasks" :key="idx">
        <div class="task-info">
          <div class="task-title">{{ task.title }}</div>
          <div class="task-desc">{{ task.desc }}</div>
        </div>
        <div class="task-actions">
          <button class="btn-complete" :class="{ completed: task.completed }" @click="completeTask(task)">
            {{ task.completed ? '已完成' : '去完成' }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'GrowthTasks',
  data() {
    return {
      tasks: [
        {
          title: '完善个人资料',
          desc: '补充个人简介，让读者更了解你',
          reward: '+ 20 矿石',
          completed: false
        },
        {
          title: '发布第一篇文章',
          desc: '写下你的第一篇技术文章',
          reward: '+ 50 矿石',
          completed: false
        },
        {
          title: '参加创作活动',
          desc: '参与每周征文，赢取官方奖励',
          reward: '+ 100 矿石',
          completed: false
        },
        {
          title: '关注3位创作者',
          desc: '发现优秀创作者，拓展创作圈子',
          reward: '+ 10 矿石',
          completed: false
        }
      ]
    }
  },
  methods: {
    completeTask(task) {
      if (!task.completed) {
        task.completed = true
        // TODO: 后续可添加奖励逻辑
      }
    }
  }
}
</script>

<style lang="less" scoped>
  @import '../../layout/styles/variables.less';

  .growth-tasks {
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

    .task-list {
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .task-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      background-color: @colorTaskBg;
      border-radius: 8px;

      .task-info {
        .task-title {
          font-size: 14px;
          font-weight: 500;
          color: @textPrimary;
          margin-bottom: 4px;
        }
        .task-desc {
          font-size: 13px;
          font-weight: 400;
          color: @colorStatLabel;
        }
      }

      .btn-complete {
        padding: 4px 16px;
        font-size: 14px;
        font-weight: 400;
        color: @brandBlue;
        border: 1px solid @brandBlue;
        border-radius: 4px;
        background: transparent;
        cursor: pointer;
        transition: all 0.2s;

        &:hover:not(.completed) {
          background-color: #e8f3ff;
        }

        &.completed {
          color: @colorStatLabel;
          border-color: @borderLight;
          cursor: default;
        }
      }
    }
  }
</style>