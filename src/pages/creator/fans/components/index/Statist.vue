<template>
  <div class="content">
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="grid-content">
          <i class="icon icon-article"></i>
          <div>
            <div class="number">{{article}} 个</div>
            <span>图文发布量</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <i class="icon icon-like"></i>
          <div>
            <div class="number">{{likes}} 个</div>
            <span>点赞数量</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <i class="icon icon-collection"></i>
          <div>
            <div class="number">{{collection}}</div>
            <span>收藏量</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  props:["collection","likes","article"],
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
  padding-bottom: 20px;
}
.el-row {
  .el-col {
    .grid-content {
      background-color: #ffffff;
      border: 1px solid #e8e8e8;
      border-radius: @cardRadius;
      box-shadow: @cardShadow;
      padding: 24px 0;
      display: flex;
      justify-content: center;
      align-items: center;
      .icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        &.icon-article {
          background-color: @brandBlue;
        }
        &.icon-like {
          background-color: #fa8665;
        }
        &.icon-collection {
          background-color: #30B08F;
        }
      }
      & > div {
        margin: 0 0 0 12px;
        span {
          color: @textSecondary;
          font-size: 14px;
        }
        .number {
          font-size: 24px;
          color: @textPrimary;
          margin-bottom: 4px;
        }
      }
    }
  }
}

</style>
