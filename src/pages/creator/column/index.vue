<template>
  <div class="column-manage">
    <div class="page-title">专栏管理</div>
    <div class="status-tabs">
      <el-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="activeStatus === tab.value ? 'primary' : 'info'"
        :class="{ 'active-tab': activeStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >{{ tab.label }}</el-tag>
    </div>
    <div class="column-list">
      <div v-for="course in courseList" :key="course.id" class="column-item">
        <div class="column-cover">
          <img :src="course.coverImage || defaultCover" alt="cover" />
        </div>
        <div class="column-content">
          <div class="column-title">{{ course.title }}</div>
          <div class="column-desc">{{ course.description }}</div>
          <div class="column-footer">
            <span class="column-stat">文章数 {{ course.chapterCount || 0 }}</span>
            <span class="column-stat">订阅人数 {{ course.studyCount || 0 }}</span>
            <span class="column-stat">作者: {{ course.authorName }}</span>
          </div>
        </div>
        <div class="column-status">
          <el-tag :type="getStatusType(course.status)" size="small">{{ getStatusLabel(course.status) }}</el-tag>
        </div>
        <div class="column-actions">
          <el-dropdown trigger="click" @command="(cmd) => operateBtn(course.id, cmd)">
            <span class="el-dropdown-link">
              <i class="el-icon-more"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="modify">修改介绍</el-dropdown-item>
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
import { API_COURSE_LIST, API_COURSE_DELETE } from '../constants/api'

export default {
  name: 'ColumnManage',
  data() {
    return {
      courseList: [],
      total: 0,
      defaultCover: avatar,
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
    this.searchCourse()
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
      this.searchCourse({ status: statusMap[status] })
    },
    async searchCourse(newParams) {
      let params = { ...this.params, ...newParams }
      let result = await request({
        url: API_COURSE_LIST,
        method: 'get',
        params: params
      })
      this.courseList = result.data || []
      this.total = result.total || 0
    },
    async deleteCourseById(id) {
      let result = await request({
        url: API_COURSE_DELETE + id,
        method: 'delete'
      })
      if (result.code === 0) {
        this.$message({ type: 'success', message: '删除成功!' })
        this.searchCourse()
      } else {
        this.$message({ type: 'error', message: result.error_message })
      }
    },
    operateBtn(id, type) {
      switch (type) {
        case 'modify':
          this.$message({ type: 'info', message: '修改介绍功能开发中' })
          break
        case 'del':
          this.$confirm('此操作将永久删除该专栏, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.deleteCourseById(id)
          }).catch(() => {
            this.$message({ type: 'info', message: '已取消删除' })
          })
          break
        default:
      }
    },
    pageChange(newPage) {
      this.searchCourse({ page: newPage })
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

.column-manage {
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

  .column-list {
    background-color: #ffffff;
    border-radius: @cardRadius;
    box-shadow: @cardShadow;
    padding: 20px;
  }

  .column-item {
    display: flex;
    align-items: flex-start;
    padding: 16px 0;
    border-bottom: 1px solid #f2f3f5;
    &:last-child {
      border-bottom: none;
    }
  }

  .column-cover {
    width: 160px;
    height: 90px;
    border-radius: 6px;
    overflow: hidden;
    margin-right: 20px;
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .column-content {
    flex: 1;
  }

  .column-title {
    font-size: 16px;
    font-weight: 500;
    color: @textPrimary;
    margin-bottom: 8px;
  }

  .column-desc {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .column-footer {
    display: flex;
  }

  .column-stat {
    font-size: 12px;
    color: #909399;
    margin-right: 24px;
  }

  .column-status {
    margin-right: 16px;
    display: flex;
    align-items: center;
  }

  .column-actions {
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
