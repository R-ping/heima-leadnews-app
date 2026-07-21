<template>
  <div class="pins-manage">
    <div class="page-title">沸点管理</div>
    <div class="status-tabs">
      <el-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="activeStatus === tab.value ? 'primary' : 'info'"
        :class="{ 'active-tab': activeStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >{{ tab.label }}</el-tag>
    </div>
    <div class="pins-list">
      <div v-for="pin in pinsList" :key="pin.id" class="pins-item">
        <div class="pins-avatar">
          <img :src="pin.userAvatar || defaultAvatar" alt="avatar" />
        </div>
        <div class="pins-content">
          <div class="pins-header">
            <span class="pins-author">{{ pin.userName }}</span>
            <span class="pins-time">{{ dateFormat(pin.createdTime) }}</span>
          </div>
          <div class="pins-text">{{ pin.content }}</div>
          <div class="pins-footer">
            <span class="pins-stat">点赞 {{ pin.likeCount || 0 }}</span>
            <span class="pins-stat">评论 {{ pin.commentCount || 0 }}</span>
            <span class="pins-stat">分享 {{ pin.shareCount || 0 }}</span>
          </div>
        </div>
        <div class="pins-status">
          <el-tag :type="getStatusType(pin.status)" size="small">{{ getStatusLabel(pin.status) }}</el-tag>
        </div>
        <div class="pins-actions">
          <el-dropdown trigger="click" @command="(cmd) => operateBtn(pin.id, cmd)">
            <span class="el-dropdown-link">
              <i class="el-icon-more"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="del">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
    <div class="pagination">
      <el-pagination
        layout="total, prev, pager, next"
        @current-change='pageChange'
        :current-page.sync='listPage.currentPage'
        :page-size="params.size"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import DateUtil from '../utils/date'
import avatar from '@/static/images/creator/avatar.jpg'
import request from '@/common/request'
import { API_PINS_LIST, API_PINS_DELETE } from '../constants/api'

export default {
  name: 'PinsManage',
  data() {
    return {
      pinsList: [],
      total: 0,
      defaultAvatar: avatar,
      params: {
        page: 1,
        size: 10
      },
      activeStatus: 'all',
      statusTabs: [
        { label: '全部', value: 'all' },
        { label: '已发布', value: 'published' },
        { label: '审核中', value: 'reviewing' },
        { label: '未通过', value: 'rejected' }
      ],
      listPage: {
        currentPage: 1
      }
    }
  },
  created() {
    this.searchPins()
  },
  methods: {
    changeStatus(status) {
      this.activeStatus = status
      let statusMap = {
        'all': null,
        'published': 9,
        'reviewing': 1,
        'rejected': 2
      }
      this.searchPins({ status: statusMap[status] })
    },
    async searchPins(newParams) {
      let params = { ...this.params, ...newParams }
      let result = await request({
        url: API_PINS_LIST,
        method: 'get',
        params: params
      })
      this.pinsList = result.data || []
      this.total = result.total || 0
    },
    async deletePinsById(id) {
      let result = await request({
        url: API_PINS_DELETE + id,
        method: 'delete'
      })
      if (result.code === 0) {
        this.$message({ type: 'success', message: '删除成功!' })
        this.searchPins()
      } else {
        this.$message({ type: 'error', message: result.error_message })
      }
    },
    operateBtn(id, type) {
      switch (type) {
        case 'del':
          this.$confirm('此操作将永久删除该沸点, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.deletePinsById(id)
          }).catch(() => {
            this.$message({ type: 'info', message: '已取消删除' })
          })
          break
        default:
      }
    },
    pageChange(newPage) {
      this.searchPins({ page: newPage })
    },
    dateFormat(time) {
      return DateUtil.format13HH(time)
    },
    getStatusLabel(status) {
      const statusMap = {
        '0': '草稿',
        '1': '审核中',
        '2': '未通过',
        '9': '已发布'
      }
      return statusMap[status] || '未知'
    },
    getStatusType(status) {
      const typeMap = {
        '0': 'info',
        '1': 'warning',
        '2': 'danger',
        '9': 'success'
      }
      return typeMap[status] || 'info'
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

  .page-title {
    font-size: 20px;
    font-weight: 500;
    color: @textPrimary;
    margin-bottom: 20px;
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

  .pins-list {
    background-color: #ffffff;
    border-radius: @cardRadius;
    box-shadow: @cardShadow;
    padding: 20px;
  }

  .pins-item {
    display: flex;
    align-items: flex-start;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
      border-bottom: none;
    }
  }

  .pins-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 16px;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .pins-content {
    flex: 1;
  }

  .pins-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }

  .pins-author {
    font-weight: 500;
    color: @textPrimary;
    margin-right: 12px;
  }

  .pins-time {
    font-size: 12px;
    color: #909399;
  }

  .pins-text {
    font-size: 14px;
    color: @textPrimary;
    line-height: 1.6;
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }

  .pins-footer {
    display: flex;
  }

  .pins-stat {
    font-size: 12px;
    color: #909399;
    margin-right: 20px;
  }

  .pins-status {
    margin-right: 16px;
    display: flex;
    align-items: center;
  }

  .pins-actions {
    .el-dropdown-link {
      cursor: pointer;
      color: #909399;
      &:hover {
        color: #1e80ff;
      }
    }
  }

  .pagination {
    text-align: right;
    margin-top: 20px;
  }
}
</style>
