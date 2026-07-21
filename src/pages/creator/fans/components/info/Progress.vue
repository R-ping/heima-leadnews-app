<template>
  <div class="content">
    <header>{{name}}
      <div class="legend">
        <span class="m">{{legend[0]}}（{{percentage}}%）</span>
        <span class="f">{{legend[1]}}（{{100-percentage}}%）</span>
      </div>
    </header>
    <el-progress :text-inside="true" :stroke-width="30" :percentage="percentage" color="#1e80ff"></el-progress>
  </div>
</template>

<script>
export default {
  data() {
    return {
        
    }
  },
  props: {
    name: {
      type: String,
      required: true
    },
    percentage: {
      type: Number,
      required: true
    },
    legend: {
      type: Array,
      default:  () => ['','']
    },
  },
  components: {
  },
  computed: {

  },
  methods: {

  }
}
</script>

<style rel="stylesheet/less" lang="less" scoped>
@import '../../../layout/styles/variables.less';
  .content {
    font-size: 14px;
    header {
      margin-bottom: 16px;
      display: flex;
      justify-content:space-between;
      color: @textPrimary;
      font-weight: 500;
      .legend span {
        position: relative;
        margin-left: 20px;
        font-weight: normal;
        color: @textSecondary;
        &.m:before {
          background-color: @brandBlue;
        }
        &.f:before {
          background-color: #fa8665;
        }
        &:before {
          content: '';
          position: absolute;
          width: 10px;
          height: 10px;
          left: -15px;
          top: 6px;
          border-radius: 3px;
        }
      }
    }
  }
</style>
