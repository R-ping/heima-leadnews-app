<template>
  <div class="content-data-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">内容数据</h2>
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

    <!-- 内容类型切换 -->
    <div class="content-tabs">
      <div
        v-for="tab in contentTypeTabs"
        :key="tab.key"
        class="content-tab-item"
        :class="{ active: activeContentType === tab.key }"
        @click="switchContentType(tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading" />
      <span>数据加载中...</span>
    </div>

    <template v-else>
      <!-- KPI指标卡片 -->
      <div class="metric-cards">
        <div
          v-for="metric in currentMetrics"
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
          <div v-if="metric.desc" class="metric-desc">{{ metric.desc }}</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="chart-section">
        <div class="chart-toolbar">
          <div class="chart-view-tabs">
            <span
              class="view-tab"
              :class="{ active: analysisView === 'overall' }"
              @click="switchAnalysisView('overall')"
            >
              {{ overallLabel }}
            </span>
            <span
              class="view-tab"
              :class="{ active: analysisView === 'detail' }"
              @click="switchAnalysisView('detail')"
            >
              {{ detailLabel }}
            </span>
          </div>
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

        <div class="chart-container" ref="chartRef" v-loading="chartLoading"></div>
      </div>

      <!-- 单篇分析表格 -->
      <div v-if="analysisView === 'detail'" class="table-section">
        <div class="table-header">
          <span class="table-title">{{ detailTableTitle }}</span>
        </div>
        <el-table
          :data="detailTableData"
          style="width: 100%"
          v-loading="tableLoading"
          :default-sort="{ prop: detailTableColumns[0]?.sortKey, order: 'descending' }"
          @sort-change="handleTableSort"
        >
          <el-table-column
            v-for="col in detailTableColumns"
            :key="col.key"
            :prop="col.key"
            :label="col.label"
            :width="col.width"
            :sortable="col.sortable ? 'custom' : false"
            :align="col.align || 'left'"
          >
            <template slot-scope="scope">
              <template v-if="col.key === 'title'">
                <a class="table-link" @click="handleDetailClick(scope.row)">
                  {{ scope.row.title || scope.row.content || '—' }}
                </a>
              </template>
              <template v-else-if="col.key === 'action'">
                <el-button type="text" size="small" @click="handleDetailClick(scope.row)">
                  详情
                </el-button>
              </template>
              <template v-else>
                {{ scope.row[col.key] !== undefined ? scope.row[col.key] : '—' }}
              </template>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="table-pagination" v-if="tableTotal > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="tableTotal"
            :page-size="tablePageSize"
            :current-page.sync="tableCurrentPage"
            @current-change="loadDetailTable"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import echarts from '@/utils/echarts-setup'
import {
  getArticleStatistics,
  getArticleTrend,
  getArticleDetailList,
  getColumnStatistics,
  getColumnTrend,
  getColumnDetailList,
  getPinStatistics,
  getPinTrend,
  getPinDetailList
} from '@/apis/creator/data'
import { toast } from '@/utils/toast'

export default {
  name: 'CreatorContentData',
  data() {
    return {
      loading: false,
      dateRange: [],
      activeContentType: 'article',
      contentTypeTabs: [
        { key: 'article', label: '文章数据' },
        { key: 'column', label: '专栏数据' },
        { key: 'pin', label: '沸点数据' }
      ],
      analysisView: 'overall',
      activeTimeRange: 7,
      timeButtons: [
        { value: 7, label: '最近7天' },
        { value: 14, label: '最近14天' },
        { value: 30, label: '最近30天' }
      ],
      chartLoading: false,
      chartInstance: null,
      // 数据
      articleMetrics: [],
      articleTrend: [],
      columnMetrics: [],
      columnTrend: [],
      pinMetrics: [],
      pinTrend: [],
      detailTableData: [],
      tableLoading: false,
      tableTotal: 0,
      tablePageSize: 10,
      tableCurrentPage: 1,
      tableSortField: '',
      tableSortOrder: ''
    }
  },
  computed: {
    currentMetrics() {
      switch (this.activeContentType) {
        case 'article': return this.articleMetrics
        case 'column': return this.columnMetrics
        case 'pin': return this.pinMetrics
        default: return []
      }
    },
    overallLabel() {
      const map = { article: '整体分析', column: '整体分析', pin: '整体分析' }
      return map[this.activeContentType] || '整体分析'
    },
    detailLabel() {
      const map = { article: '单篇分析', column: '单个分析', pin: '单条分析' }
      return map[this.activeContentType] || '单篇分析'
    },
    detailTableTitle() {
      const map = { article: '文章列表', column: '专栏列表', pin: '沸点列表' }
      return map[this.activeContentType] || '数据列表'
    },
    detailTableColumns() {
      switch (this.activeContentType) {
        case 'article':
          return [
            { key: 'title', label: '文章标题', sortable: true, sortKey: 'title' },
            { key: 'publishTime', label: '发布时间', width: '160', sortable: true, sortKey: 'publishTime' },
            { key: 'readCount', label: '阅读数', width: '100', align: 'center', sortable: true, sortKey: 'readCount' },
            { key: 'likeCount', label: '点赞数', width: '100', align: 'center', sortable: true, sortKey: 'likeCount' },
            { key: 'commentCount', label: '评论数', width: '100', align: 'center', sortable: true, sortKey: 'commentCount' },
            { key: 'collectCount', label: '收藏数', width: '100', align: 'center', sortable: true, sortKey: 'collectCount' },
            { key: 'action', label: '操作', width: '80', align: 'center' }
          ]
        case 'column':
          return [
            { key: 'title', label: '专栏标题', sortable: true, sortKey: 'title' },
            { key: 'createTime', label: '创建时间', width: '160', sortable: true, sortKey: 'createTime' },
            { key: 'articleCount', label: '文章数', width: '100', align: 'center', sortable: true, sortKey: 'articleCount' },
            { key: 'subscribeCount', label: '订阅人数', width: '120', align: 'center', sortable: true, sortKey: 'subscribeCount' },
            { key: 'action', label: '操作', width: '80', align: 'center' }
          ]
        case 'pin':
          return [
            { key: 'content', label: '沸点正文', sortable: true, sortKey: 'content' },
            { key: 'publishTime', label: '发布时间', width: '160', sortable: true, sortKey: 'publishTime' },
            { key: 'commentCount', label: '评论数', width: '100', align: 'center', sortable: true, sortKey: 'commentCount' },
            { key: 'likeCount', label: '点赞数', width: '100', align: 'center', sortable: true, sortKey: 'likeCount' },
            { key: 'action', label: '操作', width: '80', align: 'center' }
          ]
        default: return []
      }
    },
    // 图表配置
    chartColors() {
      return ['#1A73E8', '#13C2C2', '#FA8C16', '#722ED1', '#EB2F96']
    }
  },
  created() {
    // 初始化默认日期范围（最近7天）
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
          this.loadArticleData(),
          this.loadColumnData(),
          this.loadPinData()
        ])
      } catch (e) {
        toast('数据加载失败，请稍后重试', 2)
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.renderChart()
        })
      }
    },

    // 加载文章数据
    async loadArticleData() {
      try {
        const dateParams = this.getDateRangeParams()
        const [statsRes, trendRes] = await Promise.all([
          getArticleStatistics(dateParams),
          getArticleTrend({ ...dateParams, days: this.activeTimeRange })
        ])
        if (statsRes && statsRes.code === 200 && statsRes.data) {
          const d = statsRes.data
          this.articleMetrics = [
            { key: 'total', label: '总文章数', value: d.totalCount || 0, trend: d.totalTrend, desc: '' },
            { key: 'show', label: '文章展现数', value: d.showCount || 0, trend: d.showTrend, desc: '文章卡片曝光给用户的次数，涉及 PC、APP、H5 等位置' },
            { key: 'read', label: '文章阅读数', value: d.readCount || 0, trend: d.readTrend, desc: '' },
            { key: 'like', label: '文章点赞数', value: d.likeCount || 0, trend: d.likeTrend, desc: '' },
            { key: 'comment', label: '文章评论数', value: d.commentCount || 0, trend: d.commentTrend, desc: '' },
            { key: 'collect', label: '文章收藏数', value: d.collectCount || 0, trend: d.collectTrend, desc: '' }
          ]
        }
        if (trendRes && trendRes.code === 200 && trendRes.data) {
          this.articleTrend = trendRes.data.trendData || trendRes.data || []
        }
      } catch (e) {
        // 保持默认值
      }
    },

    // 加载专栏数据
    async loadColumnData() {
      try {
        const dateParams = this.getDateRangeParams()
        const [statsRes, trendRes] = await Promise.all([
          getColumnStatistics(dateParams),
          getColumnTrend({ ...dateParams, days: this.activeTimeRange })
        ])
        if (statsRes && statsRes.code === 200 && statsRes.data) {
          const d = statsRes.data
          this.columnMetrics = [
            { key: 'total', label: '总专栏数', value: d.totalCount || 0, trend: d.totalTrend, desc: '' },
            { key: 'subscribe', label: '专栏订阅数', value: d.subscribeCount || 0, trend: d.subscribeTrend, desc: '' }
          ]
        }
        if (trendRes && trendRes.code === 200 && trendRes.data) {
          this.columnTrend = trendRes.data.trendData || trendRes.data || []
        }
      } catch (e) {
        // 保持默认值
      }
    },

    // 加载沸点数据
    async loadPinData() {
      try {
        const dateParams = this.getDateRangeParams()
        const [statsRes, trendRes] = await Promise.all([
          getPinStatistics(dateParams),
          getPinTrend({ ...dateParams, days: this.activeTimeRange })
        ])
        if (statsRes && statsRes.code === 200 && statsRes.data) {
          const d = statsRes.data
          this.pinMetrics = [
            { key: 'total', label: '总沸点数', value: d.totalCount || 0, trend: d.totalTrend, desc: '' },
            { key: 'like', label: '沸点赞数', value: d.likeCount || 0, trend: d.likeTrend, desc: '' },
            { key: 'comment', label: '沸点评论数', value: d.commentCount || 0, trend: d.commentTrend, desc: '' }
          ]
        }
        if (trendRes && trendRes.code === 200 && trendRes.data) {
          this.pinTrend = trendRes.data.trendData || trendRes.data || []
        }
      } catch (e) {
        // 保持默认值
      }
    },

    // 加载单篇分析表格
    async loadDetailTable() {
      this.tableLoading = true
      try {
        const dateParams = this.getDateRangeParams()
        const params = {
          ...dateParams,
          page: this.tableCurrentPage,
          size: this.tablePageSize
        }
        if (this.tableSortField) {
          params.sortField = this.tableSortField
          params.sortOrder = this.tableSortOrder
        }
        let res
        switch (this.activeContentType) {
          case 'article':
            res = await getArticleDetailList(params)
            break
          case 'column':
            res = await getColumnDetailList(params)
            break
          case 'pin':
            res = await getPinDetailList(params)
            break
        }
        if (res && res.code === 200 && res.data) {
          this.detailTableData = res.data.list || res.data || []
          this.tableTotal = res.data.total || 0
        }
      } catch (e) {
        toast('列表数据加载失败', 2)
      } finally {
        this.tableLoading = false
      }
    },

    // 切换内容类型
    switchContentType(type) {
      this.activeContentType = type
      this.analysisView = 'overall'
      this.detailTableData = []
      this.tableTotal = 0
      this.tableCurrentPage = 1
      this.$nextTick(() => {
        this.renderChart()
      })
    },

    // 切换分析视图
    switchAnalysisView(view) {
      this.analysisView = view
      if (view === 'detail') {
        this.loadDetailTable()
      }
      this.$nextTick(() => {
        this.renderChart()
      })
    },

    // 切换时间范围
    switchTimeRange(days) {
      this.activeTimeRange = days
      this.loadAllData()
    },

    // 日期变更
    handleDateChange() {
      this.loadAllData()
    },

    // 表格排序
    handleTableSort({ prop, order }) {
      this.tableSortField = prop
      this.tableSortOrder = order || ''
      this.tableCurrentPage = 1
      this.loadDetailTable()
    },

    // 趋势样式
    trendClass(val) {
      if (val === undefined || val === null) return 'trend-zero'
      return val > 0 ? 'trend-up' : val < 0 ? 'trend-down' : 'trend-zero'
    },
    trendText(val) {
      if (val === undefined || val === null) return '较前日 —'
      const sign = val > 0 ? '+' : ''
      return `较前日 ${sign}${val}`
    },

    // 渲染图表
    renderChart() {
      if (!this.$refs.chartRef) return
      if (!this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.chartRef)
      }
      const option = this.buildChartOption()
      this.chartInstance.setOption(option, true)
    },

    buildChartOption() {
      const trendData = this.getCurrentTrend()
      const dates = trendData.map(item => item.date || '')
      const seriesConfig = this.getSeriesConfig()
      const series = seriesConfig.map((cfg, idx) => ({
        name: cfg.name,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2 },
        itemStyle: { color: this.chartColors[idx] },
        data: trendData.map(item => item[cfg.key] || 0)
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

    getCurrentTrend() {
      switch (this.activeContentType) {
        case 'article': return this.articleTrend
        case 'column': return this.columnTrend
        case 'pin': return this.pinTrend
        default: return []
      }
    },

    getSeriesConfig() {
      switch (this.activeContentType) {
        case 'article':
          return [
            { key: 'showCount', name: '展现数' },
            { key: 'readCount', name: '阅读数' },
            { key: 'likeCount', name: '点赞数' },
            { key: 'commentCount', name: '评论数' },
            { key: 'collectCount', name: '收藏数' }
          ]
        case 'column':
          return [
            { key: 'subscribeCount', name: '订阅数' }
          ]
        case 'pin':
          return [
            { key: 'likeCount', name: '点赞数' },
            { key: 'commentCount', name: '评论数' }
          ]
        default: return []
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

    handleDetailClick(row) {
      toast('详情页开发中，敬请期待', 2)
    }
  }
}
</script>

<style lang="less" scoped>
@import '../layout/styles/variables.less';

.content-data-page {
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

// 内容类型Tabs
.content-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px 8px 0 0;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

  .content-tab-item {
    padding: 14px 20px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: color 0.2s, border-color 0.2s;

    &:hover {
      color: #1A73E8;
    }

    &.active {
      color: #1A73E8;
      font-weight: 500;
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
  grid-template-columns: repeat(3, 1fr);
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

  .metric-desc {
    font-size: 12px;
    color: #BFBFBF;
    margin-top: 6px;
    line-height: 1.5;
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
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-view-tabs {
  display: flex;
  gap: 0;

  .view-tab {
    padding: 6px 16px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border: 1px solid #e8e8e8;
    transition: all 0.2s;

    &:first-child {
      border-radius: 4px 0 0 4px;
    }
    &:last-child {
      border-radius: 0 4px 4px 0;
      border-left: none;
    }

    &.active {
      color: #1A73E8;
      background: #E8F3FF;
      border-color: #1A73E8;
    }

    &:hover:not(.active) {
      color: #1A73E8;
    }
  }
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

// 表格区域
.table-section {
  margin-top: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  padding: 20px 24px;
}

.table-header {
  margin-bottom: 16px;

  .table-title {
    font-size: 16px;
    font-weight: 500;
    color: @textPrimary;
  }
}

.table-link {
  color: #1A73E8;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
  max-width: 300px;

  &:hover {
    color: #1171ee;
    text-decoration: underline;
  }
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

// 响应式适配
@media screen and (max-width: 1199px) {
  .metric-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-container {
    height: 260px;
  }
}

@media screen and (max-width: 767px) {
  .content-data-page {
    padding: 12px;
  }
  .content-tabs {
    overflow-x: auto;
    padding: 0 12px;
    .content-tab-item {
      padding: 12px 14px;
      font-size: 14px;
      white-space: nowrap;
    }
  }
  .metric-cards {
    grid-template-columns: 1fr;
    padding: 12px;
  }
  .chart-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .chart-actions {
    flex-wrap: wrap;
  }
  .chart-container {
    height: 220px;
  }
  .table-section {
    padding: 12px;
  }
}
</style>