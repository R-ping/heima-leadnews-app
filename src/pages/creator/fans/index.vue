<template>
  <div class="fans-data-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">粉丝数据</h2>
      <div class="header-right">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          :clearable="false"
          @change="handleDateChange"
          size="small"
        />
      </div>
    </div>

    <!-- 二级Tab：粉丝数据 / 粉丝列表 -->
    <div class="view-tabs">
      <div
        class="view-tab-item"
        :class="{ active: activeView === 'data' }"
        @click="switchView('data')"
      >
        粉丝数据
      </div>
      <div
        class="view-tab-item"
        :class="{ active: activeView === 'list' }"
        @click="switchView('list')"
      >
        粉丝列表
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading" />
      <span>数据加载中...</span>
    </div>

    <template v-else>
      <!-- 视图一：粉丝数据概览 -->
      <template v-if="activeView === 'data'">
        <!-- KPI指标卡片 -->
        <div class="metric-cards">
          <div
            v-for="metric in fansMetrics"
            :key="metric.key"
            class="metric-card"
          >
            <div class="metric-value">
              {{ metric.value }}
              <span v-if="metric.trend !== undefined" class="metric-trend" :class="trendClass(metric.trend)">
                {{ trendText(metric.trend) }}
              </span>
            </div>
            <div class="metric-label">{{ metric.label }}</div>
          </div>
        </div>

        <!-- 图表区域 -->
        <div class="chart-section">
          <div class="chart-toolbar">
            <div class="chart-actions">
              <div class="time-buttons">
                <button
                  v-for="btn in timeButtons"
                  :key="btn.value"
                  class="time-btn"
                  :class="{ active: activeTimeRange === btn.value }"
                  @click="switchTimeRange(btn.value)"
                >
                  {{ btn.label }}
                </button>
              </div>
              <el-button type="text" class="export-btn" @click="handleExport">
                <i class="el-icon-download" /> 导出数据
              </el-button>
            </div>
          </div>

          <div class="chart-container" ref="fansChartRef" v-loading="chartLoading"></div>
        </div>
      </template>

      <!-- 视图二：粉丝列表 -->
      <template v-if="activeView === 'list'">
        <div class="fans-list-section">
          <div class="list-header">
            <span class="list-title">粉丝列表</span>
            <span class="list-count">共 {{ fansTotal }} 位粉丝</span>
          </div>

          <!-- 空状态 -->
          <div v-if="!fansListLoading && fansList.length === 0" class="empty-state">
            <div class="empty-icon">👥</div>
            <div class="empty-text">暂无粉丝，快去创作优质内容吸引关注吧！</div>
            <el-button type="primary" size="small" class="empty-btn" @click="goPublish">
              去写文章
            </el-button>
          </div>

          <!-- 粉丝列表 -->
          <div v-else class="fans-list" v-loading="fansListLoading">
            <div
              v-for="fan in fansList"
              :key="fan.userId || fan.id"
              class="fans-item"
            >
              <div class="fans-avatar">
                <img
                  v-if="fan.avatar"
                  :src="fan.avatar"
                  alt="avatar"
                  class="avatar-img"
                />
                <span v-else class="avatar-placeholder">{{ (fan.nickName || '用')[0] }}</span>
              </div>
              <div class="fans-name">{{ fan.nickName || fan.userName || '用户' }}</div>
              <div class="fans-action">
                <button
                  class="follow-btn"
                  :class="{ followed: fan.isFollowed }"
                  @click="handleFollow(fan)"
                  :disabled="fan.isFollowed"
                >
                  {{ fan.isFollowed ? '已关注' : '+关注' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="list-pagination" v-if="fansTotal > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="fansTotal"
              :page-size="fansPageSize"
              :current-page.sync="fansCurrentPage"
              @current-change="loadFansList"
            />
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script>
import echarts from '@/utils/echarts-setup'
import { getFansStatistics, getFansTrend, getFansList, followFans } from '@/apis/creator/fans'
import { toast } from '@/utils/toast'

export default {
  name: 'CreatorFansData',
  data() {
    return {
      loading: false,
      dateRange: [],
      activeView: 'data',
      activeTimeRange: 7,
      timeButtons: [
        { value: 7, label: '最近7天' },
        { value: 14, label: '最近14天' },
        { value: 30, label: '最近30天' }
      ],
      chartLoading: false,
      chartInstance: null,
      // 粉丝概览数据
      fansMetrics: [],
      fansTrend: [],
      // 粉丝列表
      fansList: [],
      fansListLoading: false,
      fansTotal: 0,
      fansPageSize: 10,
      fansCurrentPage: 1,
      // 图表颜色
      chartColors: ['#1A73E8', '#13C2C2', '#FA8C16', '#722ED1', '#EB2F96']
    }
  },
  created() {
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - 6)
    this.dateRange = [
      this.formatDate(start),
      this.formatDate(end)
    ]
  },
  mounted() {
    this.loadAllData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },

    getDateRangeParams() {
      const [start, end] = this.dateRange || []
      return { startDate: start, endDate: end }
    },

    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadStatistics(),
          this.loadTrend()
        ])
      } catch (e) {
        toast('数据加载失败，请稍后重试', 2)
      } finally {
        this.loading = false
        this.$nextTick(() => {
          if (this.activeView === 'data') {
            this.renderChart()
          }
        })
      }
    },

    async loadStatistics() {
      try {
        const dateParams = this.getDateRangeParams()
        const res = await getFansStatistics(dateParams)
        if (res && res.code === 200 && res.data) {
          const d = res.data
          this.fansMetrics = [
            { key: 'total', label: '总粉丝', value: d.totalFans || 0, trend: d.totalTrend },
            { key: 'interactive', label: '互动粉丝', value: d.interactiveFans || 0, trend: d.interactiveTrend },
            { key: 'newFans', label: '新增粉丝', value: d.newFans || 0, trend: d.newFansTrend },
            { key: 'unfollow', label: '取消关注', value: d.unfollowCount || 0, trend: d.unfollowTrend },
            { key: 'netGrowth', label: '净增关注', value: d.netGrowth || 0, trend: d.netGrowthTrend }
          ]
        }
      } catch (e) {
        // 保持默认值
      }
    },

    async loadTrend() {
      try {
        const dateParams = this.getDateRangeParams()
        const res = await getFansTrend({ ...dateParams, days: this.activeTimeRange })
        if (res && res.code === 200 && res.data) {
          this.fansTrend = res.data.trendData || res.data || []
        }
      } catch (e) {
        // 保持默认值
      }
    },

    async loadFansList() {
      this.fansListLoading = true
      try {
        const res = await getFansList({
          page: this.fansCurrentPage,
          size: this.fansPageSize
        })
        if (res && res.code === 200 && res.data) {
          this.fansList = res.data.list || res.data || []
          this.fansTotal = res.data.total || 0
        }
      } catch (e) {
        toast('粉丝列表加载失败', 2)
      } finally {
        this.fansListLoading = false
      }
    },

    switchView(view) {
      this.activeView = view
      if (view === 'list') {
        this.fansCurrentPage = 1
        this.loadFansList()
      } else {
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    },

    switchTimeRange(days) {
      this.activeTimeRange = days
      this.loadAllData()
    },

    handleDateChange() {
      this.loadAllData()
    },

    trendClass(val) {
      if (val === undefined || val === null) return 'trend-zero'
      return val > 0 ? 'trend-up' : val < 0 ? 'trend-down' : 'trend-zero'
    },
    trendText(val) {
      if (val === undefined || val === null) return '较前日 —'
      const sign = val > 0 ? '+' : ''
      return `较前日 ${sign}${val}`
    },

    // 图表渲染
    renderChart() {
      if (!this.$refs.fansChartRef) return
      if (!this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.fansChartRef)
      }
      const option = this.buildChartOption()
      this.chartInstance.setOption(option, true)
    },

    buildChartOption() {
      const dates = this.fansTrend.map(item => item.date || '')
      const seriesConfig = [
        { key: 'totalFans', name: '总粉丝' },
        { key: 'interactiveFans', name: '互动粉丝' },
        { key: 'newFans', name: '新增粉丝' },
        { key: 'unfollowCount', name: '取消关注' },
        { key: 'netGrowth', name: '净增关注' }
      ]

      const series = seriesConfig.map((cfg, idx) => ({
        name: cfg.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2 },
        itemStyle: { color: this.chartColors[idx] },
        data: this.fansTrend.map(item => item[cfg.key] || 0)
      }))

      return {
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: '#e8e8e8',
          textStyle: { color: '#1a1a1a', fontSize: 13 },
          boxShadow: '0 2px 12px rgba(0,0,0,0.1)'
        },
        legend: {
          data: seriesConfig.map(s => s.name),
          top: 0,
          textStyle: { fontSize: 12, color: '#666' }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '40px',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates,
          axisLine: { lineStyle: { color: '#e8e8e8' } },
          axisTick: { show: false },
          axisLabel: { color: '#999', fontSize: 12 }
        },
        yAxis: {
          type: 'value',
          splitLine: { lineStyle: { color: '#f0f2f5', type: 'dashed' } },
          axisLabel: { color: '#999', fontSize: 12 }
        },
        series
      }
    },

    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },

    handleExport() {
      toast('导出功能开发中，敬请期待', 2)
    },

    goPublish() {
      this.$router.push('/creator/publish')
    },

    async handleFollow(fan) {
      const userId = fan.userId || fan.id
      if (!userId) {
        toast('操作失败，用户信息不完整', 2)
        return
      }
      try {
        const res = await followFans(userId)
        if (res && res.code === 200) {
          fan.isFollowed = true
          toast('关注成功', 2)
        } else {
          toast(res && res.message ? res.message : '关注失败', 2)
        }
      } catch (e) {
        toast('关注失败，请稍后重试', 2)
      }
    }
  }
}
</script>

<style lang="less" scoped>
@import '../layout/styles/variables.less';

.fans-data-page {
  padding: 24px;
  background: @bgGray;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: @textPrimary;
    margin: 0;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// 二级视图Tabs
.view-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px 8px 0 0;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

  .view-tab-item {
    padding: 14px 20px;
    font-size: 16px;
    font-weight: 500;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: color 0.2s, border-color 0.2s;

    &:hover {
      color: #1A73E8;
    }

    &.active {
      color: #1A73E8;
      border-bottom-color: #1A73E8;
    }
  }
}

// 加载状态
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px;
  background: #fff;
  border-radius: 0 0 8px 8px;
  color: #999;
  font-size: 14px;
  gap: 8px;

  i {
    font-size: 24px;
    color: #1A73E8;
  }
}

// KPI指标卡片
.metric-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

  .metric-card {
    padding: 16px;
    background: #F8FAFC;
    border-radius: 8px;
    transition: transform 0.25s, box-shadow 0.25s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  }

  .metric-value {
    font-size: 28px;
    font-weight: 700;
    color: #1A1A1A;
    display: flex;
    align-items: baseline;
    gap: 8px;
    flex-wrap: wrap;
  }

  .metric-trend {
    font-size: 13px;
    font-weight: 400;

    &.trend-up { color: #52C41A; }
    &.trend-down { color: #E65A5A; }
    &.trend-zero { color: #BFBFBF; }
  }

  .metric-label {
    font-size: 13px;
    color: #8C8C8C;
    margin-top: 4px;
  }
}

// 图表区域
.chart-section {
  margin-top: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px 24px;
}

.chart-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.time-buttons {
  display: flex;
  gap: 0;

  .time-btn {
    padding: 5px 14px;
    font-size: 13px;
    color: #666;
    background: #fff;
    border: 1px solid #e8e8e8;
    cursor: pointer;
    transition: all 0.2s;

    &:first-child {
      border-radius: 4px 0 0 4px;
    }
    &:last-child {
      border-radius: 0 4px 4px 0;
      border-left: none;
    }

    &.active {
      color: #fff;
      background: #1A73E8;
      border-color: #1A73E8;
    }

    &:hover:not(.active) {
      border-color: #1A73E8;
      color: #1A73E8;
    }
  }
}

.export-btn {
  font-size: 13px !important;
  color: #1A73E8 !important;
  padding: 0 !important;

  &:hover {
    color: #1171ee !important;
  }
}

.chart-container {
  width: 100%;
  height: 320px;
}

// 粉丝列表
.fans-list-section {
  margin-top: 0;
  background: #fff;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px 24px;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .list-title {
    font-size: 16px;
    font-weight: 500;
    color: @textPrimary;
  }

  .list-count {
    font-size: 13px;
    color: @textMuted;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    color: #999;
    margin-bottom: 20px;
  }

  .empty-btn {
    border-radius: 20px;
  }
}

.fans-list {
  min-height: 200px;
}

.fans-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #F0F2F5;
  transition: background-color 0.2s;

  &:hover {
    background-color: #F8FAFC;
  }

  &:last-child {
    border-bottom: none;
  }
}

.fans-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #E8E8E8;
    color: #999;
    font-size: 16px;
    font-weight: 500;
  }
}

.fans-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #1A1A1A;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fans-action {
  flex-shrink: 0;
  margin-left: 12px;
}

.follow-btn {
  padding: 4px 16px;
  font-size: 13px;
  color: #1A73E8;
  background: transparent;
  border: 1px solid #1A73E8;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #F0F5FF;
    border-color: #1171ee;
  }

  &.followed {
    color: #999;
    background: #F0F2F5;
    border-color: #F0F2F5;
    cursor: default;
  }

  &:disabled {
    cursor: default;
  }
}

.list-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 响应式适配
@media screen and (max-width: 1199px) {
  .metric-cards {
    grid-template-columns: repeat(3, 1fr);
  }
  .chart-container {
    height: 260px;
  }
}

@media screen and (max-width: 767px) {
  .fans-data-page {
    padding: 12px;
  }
  .view-tabs {
    padding: 0 12px;
  }
  .metric-cards {
    grid-template-columns: repeat(2, 1fr);
    padding: 12px;
  }
  .chart-container {
    height: 220px;
  }
  .fans-item {
    padding: 10px 12px;
  }
  .fans-avatar {
    width: 36px;
    height: 36px;
  }
}
</style>