<template>
  <section class="chart">
    <header class="section-header">图文数据</header>
    <div ref="chart" class="chart-canvas"/>
    <el-row class="legend">
      <el-col :span="8">
        <div>23</div>
        <span>平均阅读进度</span>
      </el-col>
      <el-col :span="8">
        <div>283</div>
        <span>跳出率</span>
      </el-col>
      <el-col :span="8">
        <div>123</div>
        <span>平均阅读速度</span>
      </el-col>
    </el-row>
  </section>

</template>

<script>
import echarts from '@/utils/echarts-setup'
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
          trigger: 'item',
          formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        series: [{
          name:'图文数据',
          type:'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          label: {
            normal: {
                show: false,
                position: 'center'
            },
            emphasis: {
                show: false,
                textStyle: {
                    fontSize: '30',
                    fontWeight: 'bold'
                }
            }
          },
          labelLine: {
              normal: {
                  show: false
              }
          },
          data:[
              {value:335, name:'平均阅读进度'},
              {value:310, name:'跳出率'},
              {value:234, name:'平均阅读速度'},
          ]
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
