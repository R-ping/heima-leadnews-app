<template>
  <div class="pins-manage">
    <div class="header-bar">
      <div class="page-title">沸点</div>
      <el-button type="primary" @click="showPublishModal = true">发布沸点</el-button>
    </div>

    <div class="status-tabs">
      <el-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="activeStatus === tab.value ? 'primary' : 'info'"
        :class="{ 'active-tab': activeStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >{{ tab.label }} ({{ tab.count }})</el-tag>
    </div>

    <div v-if="pinsList.length === 0" class="empty-state">
      <div class="empty-icon">💬</div>
      <div class="empty-text">这里什么都没有</div>
      <el-button type="primary" @click="showPublishModal = true">发布沸点</el-button>
    </div>

    <div v-else class="pins-list">
      <div v-for="pins in pinsList" :key="pins.id" class="pins-item">
        <div class="pins-content">
          <div class="pins-text">{{ pins.content }}</div>
          <div class="pins-tags">
            <span v-if="pins.circleName" class="pins-circle">{{ pins.circleName }}</span>
            <span v-if="pins.topicName" class="pins-topic">{{ pins.topicName }}</span>
          </div>
          <div class="pins-meta">
            <span>{{ formatTime(pins.createdTime) }}</span>
            <span class="status-tag" :class="getStatusClass(pins.status)">{{ getStatusLabel(pins.status) }}</span>
          </div>
        </div>
        <div class="pins-actions">
          <el-dropdown trigger="click" @command="(cmd) => operatePins(pins.id, cmd)">
            <span class="el-dropdown-link"><i class="el-icon-more"></i></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="del">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        layout="total, prev, pager, next"
        @current-change='pageChange'
        :current-page.sync='currentPage'
        :page-size="pageSize"
        :total="total"
      />
    </div>

    <div class="modal-overlay" v-if="showPublishModal" @click="showPublishModal = false">
      <div class="publish-modal" @click.stop>
        <div class="modal-header">
          <span class="modal-title">发布沸点</span>
          <button class="modal-close" @click="showPublishModal = false">&#xf00d;</button>
        </div>
        <div class="publish-box">
          <textarea
            class="publish-input"
            v-model="publishContent"
            placeholder="#新人报道#"
            maxlength="1000"
          ></textarea>
          <div class="publish-footer">
            <div class="publish-actions">
              <button class="action-btn">😊</button>
              <button class="action-btn">📷</button>
              <button class="action-btn">🔗</button>
              <button class="action-btn circle-btn" @click="showCircleSelector = true">
                <span class="action-icon">&#xf02e;</span>
                <span>{{ selectedCircle ? selectedCircle.name : '请选择圈子' }}</span>
              </button>
              <button class="action-btn topic-btn" @click="showTopicSelector = true">
                <span class="action-icon">&#xf02b;</span>
                <span>{{ selectedTopic ? selectedTopic.name : '话题' }}</span>
              </button>
            </div>
            <div class="publish-count">{{ publishContent.length }}/1000</div>
            <button class="publish-btn" :disabled="!publishContent.trim()" @click="publishPins">发布</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="showCircleSelector" @click="showCircleSelector = false">
      <div class="circle-modal" @click.stop>
        <div class="modal-header">
          <span class="modal-title">选择圈子</span>
          <button class="modal-close" @click="showCircleSelector = false">&#xf00d;</button>
        </div>
        <div class="circle-search">
          <input type="text" class="search-input" placeholder="搜索圈子名称" v-model="circleSearchKeyword">
        </div>
        <div class="circle-list">
          <div
            class="circle-card"
            v-for="circle in filteredCircles"
            :key="circle.id"
            :class="{ 'selected': selectedCircle && selectedCircle.id === circle.id }"
            @click="selectCircle(circle)"
          >
            <div class="circle-icon">{{ circle.icon || '📌' }}</div>
            <div class="circle-info">
              <div class="circle-name">{{ circle.name }}</div>
              <div class="circle-stats">{{ circle.memberCount }} 掘友 · {{ circle.pinsCount }} 沸点</div>
            </div>
            <div class="circle-check" v-if="selectedCircle && selectedCircle.id === circle.id">&#xf00c;</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showCircleSelector = false">不选择圈子</button>
          <button class="confirm-btn" @click="showCircleSelector = false">确认</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="showTopicSelector" @click="showTopicSelector = false">
      <div class="topic-modal" @click.stop>
        <div class="modal-header">
          <span class="modal-title">选择话题</span>
          <button class="modal-close" @click="showTopicSelector = false">&#xf00d;</button>
        </div>
        <div class="topic-search">
          <input type="text" class="search-input" placeholder="搜索话题名称" v-model="topicSearchKeyword">
        </div>
        <div class="topic-list">
          <div
            class="topic-item"
            v-for="topic in filteredTopics"
            :key="topic.id"
            :class="{ 'selected': selectedTopic && selectedTopic.id === topic.id }"
            @click="selectTopic(topic)"
          >
            <span class="topic-name">{{ topic.name }}</span>
            <span class="topic-count">{{ topic.count }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPinsList, getPinsStatistics, createPins, deletePins } from '@/apis/creator/content'

export default {
  name: 'PinsManage',
  data() {
    return {
      pinsList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      activeStatus: 'all',
      showPublishModal: false,
      showCircleSelector: false,
      showTopicSelector: false,
      publishContent: '',
      selectedCircle: null,
      selectedTopic: null,
      circleSearchKeyword: '',
      topicSearchKeyword: '',
      statusTabs: [
        { label: '全部', value: 'all', count: 0 },
        { label: '已发布', value: 'published', count: 0 },
        { label: '审核中', value: 'reviewing', count: 0 },
        { label: '未通过', value: 'rejected', count: 0 }
      ],
      circles: [
        { id: 13, name: '大模型生态圈', memberCount: 35000, pinsCount: 80000, icon: '🤖' },
        { id: 14, name: '微服务生态圈', memberCount: 25000, pinsCount: 60000, icon: '🏗️' },
        { id: 15, name: '前端开发圈', memberCount: 40000, pinsCount: 120000, icon: '🎨' },
        { id: 16, name: '服务端与架构', memberCount: 30000, pinsCount: 70000, icon: '⚙️' },
        { id: 17, name: '技术交流圈', memberCount: 50000, pinsCount: 150000, icon: '💬' },
        { id: 18, name: '上班摸鱼', memberCount: 40000, pinsCount: 100000, icon: '🐟' },
        { id: 19, name: '内推招聘广场', memberCount: 35000, pinsCount: 80000, icon: '📋' },
        { id: 20, name: '美食探店', memberCount: 50000, pinsCount: 150000, icon: '🍜' }
      ],
      topics: [
        { id: 1, name: '#新人报道#', count: 5000 },
        { id: 2, name: '#程序员脱单到底有多难#', count: 15000 },
        { id: 3, name: '#每日快讯#', count: 20000 },
        { id: 4, name: '#每日精选文章#', count: 18000 },
        { id: 5, name: '#日新计划#', count: 12000 },
        { id: 6, name: '#每天一个知识点#', count: 8000 },
        { id: 7, name: '#代码人生#', count: 10000 },
        { id: 8, name: '#优秀开源项目#', count: 7000 },
        { id: 9, name: '#技术交流#', count: 13000 },
        { id: 10, name: '#上班摸鱼#', count: 25000 }
      ]
    }
  },
  created() {
    this.loadData()
  },
  computed: {
    filteredCircles() {
      if (!this.circleSearchKeyword) return this.circles
      return this.circles.filter(c => c.name.includes(this.circleSearchKeyword))
    },
    filteredTopics() {
      if (!this.topicSearchKeyword) return this.topics
      return this.topics.filter(t => t.name.includes(this.topicSearchKeyword))
    }
  },
  methods: {
    async loadData() {
      await this.loadStatistics()
      await this.loadList()
    },
    async loadStatistics() {
      const res = await getPinsStatistics()
      if (res && res.code === 200 && res.data) {
        const data = res.data
        this.statusTabs = [
          { label: '全部', value: 'all', count: data.published || 0 },
          { label: '已发布', value: 'published', count: data.published || 0 },
          { label: '审核中', value: 'reviewing', count: data.reviewing || 0 },
          { label: '未通过', value: 'rejected', count: data.rejected || 0 }
        ]
      }
    },
    async loadList() {
      const params = {
        page: this.currentPage,
        size: this.pageSize,
        status: this.activeStatus === 'all' ? '' : this.activeStatus
      }
      const res = await getPinsList(params)
      if (res && res.code === 200 && res.data) {
        this.pinsList = res.data.list || []
        this.total = res.data.total || 0
      }
    },
    changeStatus(status) {
      this.activeStatus = status
      this.currentPage = 1
      this.loadList()
    },
    pageChange(newPage) {
      this.currentPage = newPage
      this.loadList()
    },
    selectCircle(circle) {
      this.selectedCircle = circle
    },
    selectTopic(topic) {
      this.selectedTopic = topic
      this.showTopicSelector = false
    },
    async publishPins() {
      if (!this.publishContent.trim()) return
      const data = {
        content: this.publishContent,
        circleId: this.selectedCircle?.id || null,
        circleName: this.selectedCircle?.name || null,
        topicId: this.selectedTopic?.id || null,
        topicName: this.selectedTopic?.name || null
      }
      const res = await createPins(data)
      if (res && res.code === 200) {
        this.$message.success('发布成功')
        this.showPublishModal = false
        this.publishContent = ''
        this.selectedCircle = null
        this.selectedTopic = null
        this.loadData()
      } else {
        this.$message.error(res?.errorMessage || '发布失败')
      }
    },
    operatePins(id, type) {
      if (type === 'del') {
        this.$confirm('确定要删除该沸点吗？', '提示', { type: 'warning' }).then(() => {
          this.doDelete(id)
        })
      }
    },
    async doDelete(id) {
      const res = await deletePins(id)
      if (res && res.code === 200) {
        this.$message.success('删除成功')
        this.loadData()
      } else {
        this.$message.error(res?.errorMessage || '删除失败')
      }
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getStatusLabel(status) {
      const map = { '0': '草稿', '1': '审核中', '2': '未通过', '9': '已发布' }
      return map[status] || '未知'
    },
    getStatusClass(status) {
      const map = { '0': 'status-draft', '1': 'status-reviewing', '2': 'status-rejected', '9': 'status-published' }
      return map[status] || ''
    }
  }
}
</script>

<style lang="less" scoped>
@import '../layout/styles/variables.less';

.pins-manage {
  min-height: calc(100vh - 50px);
  background-color: @bgGray;
  padding: 20px;

  .header-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 20px;
      font-weight: 500;
      color: @textPrimary;
    }
  }

  .status-tabs {
    margin-bottom: 16px;
    el-tag {
      cursor: pointer;
      margin-right: 8px;
      padding: 4px 16px;
      &.active-tab {
        background-color: #1e80ff;
        border-color: #1e80ff;
        color: #fff;
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 0;

    .empty-icon {
      font-size: 64px;
      margin-bottom: 16px;
    }

    .empty-text {
      font-size: 14px;
      color: #909399;
      margin-bottom: 24px;
    }
  }

  .pins-list {
    background-color: #fff;
    border-radius: 8px;
    padding: 16px;

    .pins-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      &:last-child { border-bottom: none; }

      .pins-content {
        flex: 1;

        .pins-text {
          font-size: 14px;
          color: #303133;
          line-height: 1.6;
          margin-bottom: 10px;
          word-break: break-word;
        }

        .pins-tags {
          display: flex;
          gap: 8px;
          margin-bottom: 10px;

          .pins-circle {
            padding: 2px 8px;
            background: #eaf2ff;
            color: #1e80ff;
            font-size: 12px;
            border-radius: 4px;
          }

          .pins-topic {
            padding: 2px 8px;
            background: #fff7e6;
            color: #fa8c16;
            font-size: 12px;
            border-radius: 4px;
          }
        }

        .pins-meta {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: #909399;

          .status-tag {
            margin-left: 12px;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 11px;
          }

          .status-draft { background: #f5f5f5; color: #999; }
          .status-reviewing { background: #fff7e6; color: #d48806; }
          .status-rejected { background: #fef0f0; color: #f56c6c; }
          .status-published { background: #f0f9eb; color: #67c23a; }
        }
      }

      .pins-actions {
        .el-dropdown-link {
          cursor: pointer;
          color: #909399;
          &:hover { color: #1e80ff; }
        }
      }
    }
  }

  .pagination {
    text-align: right;
    margin-top: 20px;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.publish-modal {
  background: #fff;
  border-radius: 8px;
  width: 600px;
  overflow: hidden;
}

.circle-modal, .topic-modal {
  background: #fff;
  border-radius: 8px;
  width: 600px;
  max-height: 70vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f2f3f5;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #252933;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-family: fontawesome;
  font-size: 16px;
  color: #8a919f;
  cursor: pointer;
  border-radius: 50%;
  &:hover {
    background: #f2f3f5;
    color: #515767;
  }
}

.publish-box {
  padding: 16px 20px;
}

.publish-input {
  width: 100%;
  height: 80px;
  border: none;
  resize: none;
  font-size: 14px;
  line-height: 1.6;
  color: #252933;
  &::placeholder { color: #c4c9d1; }
  &:focus { outline: none; }
}

.publish-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
}

.publish-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #8a919f;
  cursor: pointer;
  &:hover {
    color: #1e80ff;
    background: #f0f5ff;
    border-radius: 4px;
  }
}

.action-icon {
  font-family: fontawesome;
}

.circle-btn, .topic-btn {
  padding: 6px 12px;
  border-radius: 4px;
}

.publish-count {
  font-size: 13px;
  color: #c4c9d1;
}

.publish-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 4px;
  background: #1e80ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #4096ff; }
  &:disabled { background: #c4c9d1; cursor: not-allowed; }
}

.circle-search, .topic-search {
  padding: 12px 20px;
}

.search-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e4e6eb;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  &:focus { border-color: #1e80ff; }
}

.circle-list {
  padding: 12px 20px;
  max-height: 300px;
  overflow-y: auto;
}

.circle-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover { background: #f7f8fa; }
  &.selected { background: #eaf2ff; }
}

.circle-icon { font-size: 24px; }

.circle-info { flex: 1; }

.circle-name {
  font-size: 14px;
  color: #252933;
  margin-bottom: 2px;
}

.circle-stats {
  font-size: 12px;
  color: #8a919f;
}

.circle-check {
  font-family: fontawesome;
  font-size: 16px;
  color: #1e80ff;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f2f3f5;
}

.cancel-btn {
  padding: 8px 24px;
  border: 1px solid #e4e6eb;
  border-radius: 4px;
  background: #fff;
  color: #515767;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #f7f8fa; }
}

.confirm-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 4px;
  background: #1e80ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  &:hover { background: #4096ff; }
}

.topic-list {
  padding: 12px 20px;
  max-height: 400px;
  overflow-y: auto;
}

.topic-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  &:hover { background: #f7f8fa; }
  &.selected {
    background: #eaf2ff;
    .topic-name { color: #1e80ff; }
  }
}

.topic-name {
  font-size: 14px;
  color: #252933;
}

.topic-count {
  font-size: 12px;
  color: #8a919f;
}
</style>
