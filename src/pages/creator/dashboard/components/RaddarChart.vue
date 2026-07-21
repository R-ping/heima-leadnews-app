<template>
  <section class="chart">
    <header class="section-header">粉丝数据</header>
    <div ref="chart" class="chart-canvas"/>
    <el-row class="legend">
      <el-col :span="8">
        <div>23</div>
        <span>新增粉丝</span>
      </el-col>
      <el-col :span="8">
        <div>283</div>
        <span>活跃粉丝</span>
      </el-col>
      <el-col :span="8">
        <div>123</div>
        <span>总粉丝数</span>
      </el-col>
    </el-row>
  </section>

</template>

<script>
import * as echarts from 'echarts'
import 'echarts/theme/macarons' // echarts theme
// import { debounce } from '@/utils'

const animationDuration = 3000

export default {
  data() {
    return {
      chart: null
    }
  },
  mounted() {
    this.initChart()
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chart, 'macarons')

      this.chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        radar: {
          radius: '66%',
          indicator: [
            { name: '引用', max: 10000 },
            { name: '产量', max: 20000 },
            { name: '贡献', max: 20000 },
            { name: '热度', max: 20000 },
            { name: '口碑', max: 20000 },
          ]
        },
        series: [{
          type: 'radar',
          symbolSize: 0,
          data: [
            {
              value: [5000, 7000, 12000, 11000, 15000, 14000],
              name: 'Allocated Budget'
            },
            {
              value: [4000, 9000, 15000, 15000, 13000, 11000],
              name: 'Expected Spending'
            },
            {
              value: [5500, 11000, 12000, 15000, 12000, 12000],
              name: 'Actual Spending'
            }
          ],
          animationDuration: animationDuration
        }]
      })
    }
  }
}
</script>
<style lang="less" scoped>
  @import '../../layout/styles/variables.less';

  .chart {
    display: flex;
    flex-direction: column;
    height: 100%;

    .section-header {
      padding: 16px 24px;
      font-size: 18px;
      font-weight: 600;
      color: @textPrimary;
      border-bottom: 1px solid #f2f3f5;
      flex-shrink: 0;
    }

    .chart-canvas {
      flex: 1;
      min-height: 280px;
      width: 100%;
    }

    .legend {
      border-top: 1px solid #f2f3f5;
      margin: 0 16px 16px;
      padding-top: 12px;
      flex-shrink: 0;

      .el-col {
        padding: 10px 0;
        border-right: 1px solid #f2f3f5;

        &>div {
          color: @textPrimary;
          font-size: 20px;
          font-weight: 600;
        }

        span {
          color: @textSecondary;
          font-size: 14px;
        }

        &:last-child {
          border: none
        }
      }
    }
  }
</style>
