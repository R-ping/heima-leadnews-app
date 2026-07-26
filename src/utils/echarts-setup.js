/**
 * ECharts 按需导入配置
 * 替代全量 import * as echarts from 'echarts'，减少 ~300KB bundle 体积
 */
import * as echarts from 'echarts/core'
import { PieChart, RadarChart, LineChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import 'echarts/theme/macarons'

echarts.use([
  PieChart, RadarChart, LineChart, BarChart,
  TitleComponent, TooltipComponent, LegendComponent,
  GridComponent, ToolboxComponent,
  CanvasRenderer
])

export default echarts