<template>
  <div class="chart-content">
    <header class="section-header">头条粉丝</header>
    <div class="chart">
      <div ref="chart" :style="{height:height,width:width}"/>
      <el-table :data="fansAgePerc" class="age-table">
        <el-table-column
          prop="age"
          label="年龄">
        </el-table-column>
        <el-table-column
          prop="perc"
          label="粉丝占比">
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import 'echarts/theme/macarons' // echarts theme

export default {
  props: ["className","width","height","autoResize","ageRangeValue","ageRangeKey", "fansAgePerc"],
  data() {
    return {
    }
  },
  watch: {
    chartData: {
      deep: true,
      handler(val) {
        this.setOptions(val)
      }
    },
    ageRangeValue: {
      deep: true,
      handler (val) {
        this.ageRangeValue = val;
        this.initChart()
      }
    },
    ageRangeKey: {
      deep: true,
      handler (val) {
        this.ageRangeKey = val;
        this.initChart()
      }
    }
  },
  mounted() {
    this.initChart()
    // 监听侧边栏的变化
    this.sidebarElm = document.getElementsByClassName('sidebar-container')[0]
    this.sidebarElm && this.sidebarElm.addEventListener('transitionend', this.sidebarResizeHandler)
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    if (this.autoResize) {
      window.removeEventListener('resize', this.__resizeHandler)
    }

    this.sidebarElm && this.sidebarElm.removeEventListener('transitionend', this.sidebarResizeHandler)

    this.chart.dispose()
    this.chart = null
  },
  methods: {
    sidebarResizeHandler(e) {
      if (e.propertyName === 'width') {
        this.__resizeHandler()
      }
    },
    setOptions() {
      this.chart.setOption({
        color: ['#3398DB'],
        tooltip : {
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                data : this.ageRangeKey,
                axisTick: {
                    alignWithLabel: true
                }
            }
        ],
        yAxis : [
            {
                type : 'value'
            }
        ],
        series : [
            {
                name:'直接访问',
                type:'bar',
                barWidth: '30%',
                color: '#1e80ff',
                data: this.ageRangeValue
            }
        ]
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.chart, 'macarons')
      this.setOptions(this.chartData)
    }
  }
}
</script>

<style rel="stylesheet/less" lang="less" scoped>
@import '../../../layout/styles/variables.less';
  .chart-content {
    font-size: 14px;
    .section-header {
      font-size: 16px;
      color: @textPrimary;
      margin-bottom: 16px;
      font-weight: 500;
    }
    .chart {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      .age-table {
        width: 280px;
        margin-left: 20px;
        flex-shrink: 0;
      }
    }
  }
</style>
