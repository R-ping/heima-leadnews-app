<template>
  <div class="course-list-page">
    <div class="page-header">
      <h2 class="page-title">课程管理</h2>
      <el-button type="primary" size="small" @click="handleCreate" :disabled="!hasPermission">
        <i class="el-icon-plus"></i> 新建课程
      </el-button>
    </div>

    <!-- 权限不足提示 -->
    <div class="permission-tip" v-if="!hasPermission && !loading">
      <i class="el-icon-warning"></i>
      <span>课程创作权限需要逐力值 Lv{{ requiredLevel }}，当前逐力值 Lv{{ powerLevel }}，继续努力吧！</span>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
        <el-radio-button :label="undefined">全部</el-radio-button>
        <el-radio-button :label="0">草稿</el-radio-button>
        <el-radio-button :label="1">审核中</el-radio-button>
        <el-radio-button :label="2">未通过</el-radio-button>
        <el-radio-button :label="9">已上架</el-radio-button>
        <el-radio-button :label="3">已下架</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="searchKeyword"
        placeholder="搜索课程标题"
        size="small"
        class="search-input"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <i slot="prefix" class="el-input__icon el-icon-search"></i>
      </el-input>
    </div>

    <!-- 课程列表 -->
    <div class="course-table" v-loading="loading">
      <el-table :data="courseList" style="width: 100%" v-if="courseList.length > 0">
        <el-table-column prop="title" label="课程标题" min-width="200">
          <template slot-scope="scope">
            <div class="course-title-cell">
              <img v-if="scope.row.coverImage" :src="scope.row.coverImage" class="course-cover" />
              <span class="course-title">{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.price > 0">¥{{ scope.row.price }}</span>
            <span v-else class="free-tag">免费</span>
          </template>
        </el-table-column>
        <el-table-column prop="chapterCount" label="章节数" width="80" />
        <el-table-column prop="studyCount" label="学习人数" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="statusType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="更新时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="text"
              size="small"
              @click="handleSubmit(scope.row)"
            >提交审核</el-button>
            <el-button
              v-if="scope.row.status === 9"
              type="text"
              size="small"
              @click="handleUnpublish(scope.row)"
            >下架</el-button>
            <el-button
              v-if="scope.row.status !== 9"
              type="text"
              size="small"
              style="color: #f56c6c"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && courseList.length === 0 && hasPermission">
        <i class="el-icon-document"></i>
        <p>还没有课程，开始创作你的第一门课程吧</p>
        <el-button type="primary" size="small" @click="handleCreate">新建课程</el-button>
      </div>

      <div class="empty-state" v-if="!loading && courseList.length === 0 && !hasPermission">
        <i class="el-icon-lock"></i>
        <p>暂无课程创作权限</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page.sync="currentPage"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import courseApi from '@/apis/course'

export default {
  name: 'CreatorCourseList',
  data() {
    return {
      loading: true,
      hasPermission: false,
      powerLevel: 1,
      requiredLevel: 5,
      courseList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      filterStatus: undefined,
      searchKeyword: ''
    }
  },
  mounted() {
    this.checkPermission()
  },
  methods: {
    async checkPermission() {
      try {
        const res = await courseApi.checkAuthorPermission()
        if (res && res.code === 200 && res.data) {
          this.hasPermission = res.data.hasPermission
          this.powerLevel = res.data.powerLevel
          this.requiredLevel = res.data.requiredLevel
          if (this.hasPermission) {
            this.loadList()
          } else {
            this.loading = false
          }
        }
      } catch (e) {
        this.loading = false
      }
    },
    async loadList() {
      this.loading = true
      try {
        const res = await courseApi.getManageList({
          page: this.currentPage,
          size: this.pageSize,
          status: this.filterStatus,
          keyword: this.searchKeyword || undefined
        })
        if (res && res.code === 200 && res.data) {
          this.courseList = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (e) {
        console.error('加载课程列表失败', e)
      } finally {
        this.loading = false
      }
    },
    handleFilterChange() {
      this.currentPage = 1
      this.loadList()
    },
    handleSearch() {
      this.currentPage = 1
      this.loadList()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadList()
    },
    async handleCreate() {
      try {
        const res = await courseApi.createCourse({ title: '未命名课程' })
        if (res && res.code === 200 && res.data) {
          this.$router.push(`/creator/course/edit?courseId=${res.data.id}`)
        }
      } catch (e) {
        this.$message.error('创建课程失败')
      }
    },
    handleEdit(row) {
      this.$router.push(`/creator/course/edit?courseId=${row.id}`)
    },
    async handleSubmit(row) {
      try {
        await this.$confirm('确定要提交审核吗？提交后不可编辑。', '提示', { type: 'warning' })
        const res = await courseApi.submitForReview({ courseId: row.id })
        if (res && res.code === 200) {
          this.$message.success('已提交审核')
          this.loadList()
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('提交失败')
      }
    },
    async handleUnpublish(row) {
      try {
        await this.$confirm('确定要下架该课程吗？已购买用户仍可继续学习。', '提示', { type: 'warning' })
        const res = await courseApi.unpublishCourse({ courseId: row.id })
        if (res && res.code === 200) {
          this.$message.success('已下架')
          this.loadList()
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('下架失败')
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该课程吗？此操作不可恢复。', '提示', { type: 'warning' })
        const res = await courseApi.deleteCourse({ courseId: row.id })
        if (res && res.code === 200) {
          this.$message.success('已删除')
          this.loadList()
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败')
      }
    },
    statusType(status) {
      const map = { 0: 'info', 1: 'warning', 2: 'danger', 9: 'success', 3: 'info' }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = { 0: '草稿', 1: '审核中', 2: '未通过', 9: '已上架', 3: '已下架' }
      return map[status] || '未知'
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style lang="less" scoped>
.course-list-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.permission-tip {
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  color: #f56c6c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  width: 240px;
}

.course-table {
  background: #fff;
  border-radius: 8px;
  min-height: 200px;
}

.course-title-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-cover {
  width: 48px;
  height: 32px;
  border-radius: 4px;
  object-fit: cover;
}

.course-title {
  font-size: 14px;
  color: #333;
}

.free-tag {
  color: #67c23a;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
  i {
    font-size: 48px;
    margin-bottom: 12px;
  }
  p {
    font-size: 14px;
    margin-bottom: 16px;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>