<template>
  <div class="discount-page">
    <div class="page-header">
      <h2 class="page-title">折扣码管理</h2>
      <el-button type="primary" size="small" @click="showCreateDialog = true">
        <i class="el-icon-plus"></i> 创建折扣码
      </el-button>
    </div>

    <div class="discount-table" v-loading="loading">
      <el-table :data="discountList" style="width: 100%" v-if="discountList.length > 0">
        <el-table-column prop="code" label="折扣码" width="160" />
        <el-table-column prop="courseTitle" label="关联课程" min-width="180" />
        <el-table-column label="折扣类型" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.discountType === 1">固定金额 ¥{{ scope.row.discountValue }}</span>
            <span v-else>{{ scope.row.discountValue }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template slot-scope="scope">
            <span>{{ scope.row.startTime }} ~ {{ scope.row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用情况" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.usedCount }} / {{ scope.row.maxUses }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '有效' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.status === 1"
              type="text"
              size="small"
              style="color: #f56c6c"
              @click="handleDisable(scope.row)"
            >停用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="empty-state" v-if="!loading && discountList.length === 0">
        <i class="el-icon-discount"></i>
        <p>还没有折扣码，创建第一个折扣码来促进课程销售吧</p>
        <el-button type="primary" size="small" @click="showCreateDialog = true">创建折扣码</el-button>
      </div>
    </div>

    <!-- 创建折扣码弹窗 -->
    <el-dialog
      title="创建折扣码"
      :visible.sync="showCreateDialog"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="关联课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="选择课程" style="width: 100%" filterable>
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.title"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="折扣码" prop="code">
          <el-input v-model="form.code" placeholder="如：SUMMER50，留空自动生成" />
        </el-form-item>
        <el-form-item label="折扣类型" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio :label="1">固定金额</el-radio>
            <el-radio :label="2">百分比</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="折扣值" prop="discountValue">
          <el-input-number
            v-model="form.discountValue"
            :min="0.01"
            :max="form.discountType === 2 ? 100 : 999999"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="使用上限" prop="maxUses">
          <el-input-number
            v-model="form.maxUses"
            :min="1"
            :max="99999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="有效期" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import courseApi from '@/apis/course'

export default {
  name: 'CreatorCourseDiscount',
  data() {
    return {
      loading: true,
      discountList: [],
      courseList: [],
      showCreateDialog: false,
      creating: false,
      form: {
        courseId: null,
        code: '',
        discountType: 1,
        discountValue: 10,
        maxUses: 100,
        dateRange: []
      },
      rules: {
        courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
        discountType: [{ required: true, message: '请选择折扣类型', trigger: 'change' }],
        discountValue: [{ required: true, message: '请输入折扣值', trigger: 'blur' }],
        maxUses: [{ required: true, message: '请输入使用上限', trigger: 'blur' }],
        dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadCourseList()
  },
  methods: {
    async loadCourseList() {
      try {
        const res = await courseApi.getManageList({ page: 1, size: 100 })
        if (res && res.code === 200 && res.data) {
          this.courseList = res.data.list || []
        }
      } catch (e) {
        console.error('加载课程列表失败', e)
      }
    },
    async loadDiscountList(courseId) {
      this.loading = true
      try {
        const res = await courseApi.getDiscountList({ courseId })
        if (res && res.code === 200 && res.data) {
          this.discountList = res.data.list || []
        }
      } catch (e) {
        console.error('加载折扣码列表失败', e)
      } finally {
        this.loading = false
      }
    },
    async handleCreate() {
      try {
        await this.$refs.form.validate()
      } catch (e) {
        return
      }
      this.creating = true
      try {
        const res = await courseApi.createDiscount({
          courseId: this.form.courseId,
          code: this.form.code || undefined,
          discountType: this.form.discountType,
          discountValue: this.form.discountValue,
          maxUses: this.form.maxUses,
          startTime: this.form.dateRange[0],
          endTime: this.form.dateRange[1]
        })
        if (res && res.code === 200) {
          this.$message.success('创建成功')
          this.showCreateDialog = false
          this.resetForm()
          this.loadDiscountList(this.form.courseId)
        } else {
          this.$message.error(res.message || '创建失败')
        }
      } catch (e) {
        this.$message.error('创建失败')
      } finally {
        this.creating = false
      }
    },
    async handleDisable(row) {
      try {
        await this.$confirm('确定要停用该折扣码吗？', '提示', { type: 'warning' })
        const res = await courseApi.disableDiscount({ discountId: row.id })
        if (res && res.code === 200) {
          this.$message.success('已停用')
          this.loadDiscountList(row.courseId)
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('操作失败')
      }
    },
    resetForm() {
      this.form = {
        courseId: null,
        code: '',
        discountType: 1,
        discountValue: 10,
        maxUses: 100,
        dateRange: []
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
    }
  },
  watch: {
    'form.courseId'(val) {
      if (val) {
        this.loadDiscountList(val)
      }
    }
  }
}
</script>

<style lang="less" scoped>
.discount-page {
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

.discount-table {
  background: #fff;
  border-radius: 8px;
  min-height: 200px;
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
</style>