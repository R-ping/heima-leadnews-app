<template>
  <el-dialog
    :visible.sync="visible"
    :title="'掘力值明细'"
    width="720px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    custom-class="power-detail-modal"
    @close="handleClose"
  >
    <div class="power-composition">
      <div class="composition-title">当前掘力值</div>
      <div class="composition-formula">
        <span class="total">{{ currentPower }}</span>
        <span class="operator">=</span>
        <span class="item positive">创作行为 {{ detail.actionScore || 0 }}</span>
        <span class="operator">+</span>
        <span class="item positive">创作影响力 {{ detail.influenceScore || 0 }}</span>
        <span class="operator">+</span>
        <span class="item positive">创作质量 {{ detail.qualityScore || 0 }}</span>
        <span class="operator">-</span>
        <span class="item negative">创作违规 {{ detail.violationScore || 0 }}</span>
      </div>
    </div>

    <div class="history-section">
      <div class="history-header">
        <span class="history-title">近30日掘力值明细</span>
        <span class="history-range">{{ dateRange }}</span>
      </div>

      <el-table
        :data="historyData"
        :show-header="true"
        stripe
        size="small"
        max-height="300"
        class="history-table"
      >
        <el-table-column prop="date" label="时间" width="120" />
        <el-table-column label="总掘力值" width="100">
          <template slot-scope="{ row }">
            <span :class="{ 'positive': row.total > 0 }">
              {{ row.total > 0 ? '+' : '' }}{{ row.total }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创作行为" width="100">
          <template slot-scope="{ row }">
            <span>{{ formatValue(row.actionScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创作影响力" width="100">
          <template slot-scope="{ row }">
            <span>{{ formatValue(row.influenceScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创作质量" width="100">
          <template slot-scope="{ row }">
            <span>{{ formatValue(row.qualityScore) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创作违规" width="100">
          <template slot-scope="{ row }">
            <span class="negative">{{ formatValue(row.violationScore) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="rules-section">
      <div class="rules-title">掘力值规则</div>
      <ul class="rules-list">
        <li>当前为{{ dateRange }}数据表现，每日凌晨5点更新前日数据</li>
        <li>该模块仅展示各具体分项出现变化的日期，--表示无变化</li>
        <li>如遇文章被删除，文章评论、点赞、收藏、推荐被删除或取消，创作违规等情形，对应日期的掘力值会被扣减</li>
        <li>创作违规行为被撤销会恢复相关扣减分值</li>
      </ul>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'PowerDetailModal',
  props: {
    visible: { type: Boolean, default: false },
    currentPower: { type: Number, default: 0 },
    detail: {
      type: Object,
      default: () => ({
        actionScore: 0,
        influenceScore: 0,
        qualityScore: 0,
        violationScore: 0
      })
    },
    historyData: { type: Array, default: () => [] }
  },
  computed: {
    dateRange() {
      const end = new Date()
      const start = new Date()
      start.setDate(end.getDate() - 30)
      const fmt = (d) => {
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      }
      return `${fmt(start)} ~ ${fmt(end)}`
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
    },
    formatValue(val) {
      if (val === null || val === undefined || val === 0) {
        return '--'
      }
      const prefix = val > 0 ? '+' : ''
      return `${prefix}${val}`
    }
  }
}
</script>

<style lang="less" scoped>
@import '../../layout/styles/variables.less';

.power-detail-modal {
  .el-dialog__body {
    padding: 20px;
  }

  .power-composition {
    padding: 20px;
    background: linear-gradient(135deg, #f0f7ff 0%, #e6f4ff 100%);
    border-radius: 12px;
    margin-bottom: 24px;

    .composition-title {
      font-size: 14px;
      color: @textMuted;
      margin-bottom: 12px;
    }

    .composition-formula {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      font-size: 14px;

      .total {
        font-size: 32px;
        font-weight: 700;
        color: @brandBlue;
      }

      .operator {
        color: @textMuted;
        font-weight: 500;
      }

      .item {
        padding: 4px 12px;
        border-radius: 16px;
        font-weight: 500;
        font-size: 13px;

        &.positive {
          background: #e6f7e6;
          color: #52c41a;
        }

        &.negative {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }

  .history-section {
    margin-bottom: 24px;

    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .history-title {
        font-size: 14px;
        font-weight: 600;
        color: @textPrimary;
      }

      .history-range {
        font-size: 12px;
        color: @textMuted;
      }
    }

    .history-table {
      .el-table__body-wrapper {
        border-radius: 8px;
      }

      .positive {
        color: #52c41a;
      }

      .negative {
        color: #ff4d4f;
      }
    }
  }

  .rules-section {
    padding: 16px;
    background: #f9f9f9;
    border-radius: 8px;

    .rules-title {
      font-size: 13px;
      font-weight: 600;
      color: @textPrimary;
      margin-bottom: 8px;
    }

    .rules-list {
      margin: 0;
      padding-left: 20px;
      font-size: 12px;
      color: @textMuted;
      line-height: 1.8;

      li {
        margin-bottom: 4px;
      }
    }
  }
}
</style>