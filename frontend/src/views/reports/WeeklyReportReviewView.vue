<template>
  <div class="page">
    <div class="page-title">周报管理 - 主管点评</div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar-content">
        <div class="filter-section">
          <span class="filter-label">筛选：</span>
          <el-select v-model="filterReviewed" placeholder="请选择" style="width: 140px">
            <el-option label="全部" value="all" />
            <el-option label="待点评" value="pending" />
            <el-option label="已点评" value="reviewed" />
          </el-select>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="filtered" style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="employeeId" label="提交人" min-width="100">
          <template #default="{ row }">
            {{ empName(row.employeeId) }}
          </template>
        </el-table-column>
        <el-table-column prop="weekOf" label="周日期" min-width="110" />
        <el-table-column label="出勤" min-width="200">
          <template #default="{ row }">
            <span v-for="(d, idx) in row.attendanceByDay" :key="idx" class="attendance-item">
              {{ d.day }}:{{ d.present ? '出勤' : '缺勤' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="thisWeekWork" label="本周工作" min-width="200" show-overflow-tooltip />
        <el-table-column prop="nextWeekPlan" label="下周计划" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.reviewAt ? 'success' : 'info'" effect="plain">
              {{ row.reviewAt ? '已点评' : '待点评' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                v-if="!row.reviewAt"
                type="primary"
                size="small"
                @click="openReview(row)"
              >
                去点评
              </el-button>
              <el-button
                v-else
                size="small"
                @click="openReview(row)"
              >
                查看点评
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="reviewModalOpen"
      title="周报点评"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="reviewTarget">
        <div class="dialog-subtitle">
          提交人：{{ empName(reviewTarget.employeeId) }} / 周日期：{{ reviewTarget.weekOf }}
        </div>
        <div class="dialog-field">
          <label>点评内容（演示必填）</label>
          <el-input
            v-model="reviewDraft"
            type="textarea"
            :rows="4"
            placeholder="填写点评/建议/问题追踪"
          />
        </div>
        <el-alert
          v-if="reviewTarget.reviewAt"
          type="info"
          :closable="false"
          class="prev-comment"
        >
          <template #title>
            已有点评：{{ reviewTarget.reviewComment }}
          </template>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="reviewModalOpen = false">取消</el-button>
        <el-button type="primary" @click="confirmReview">提交点评</el-button>
      </template>
    </el-dialog>

    <el-alert
      v-if="message"
      :type="messageType === 'error' ? 'error' : 'success'"
      :title="message"
      show-icon
      closable
      class="message-alert"
      @close="message = ''"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'WeeklyReportReviewView',
  setup() {
    const store = useMockStore()

    const filterReviewed = ref<'all' | 'pending' | 'reviewed'>('all')

    const filtered = computed(() => {
      if (filterReviewed.value === 'all') return store.weeklyReports
      if (filterReviewed.value === 'pending') return store.weeklyReports.filter((r) => !r.reviewAt)
      return store.weeklyReports.filter((r) => !!r.reviewAt)
    })

    const empName = (id: string) => store.employees.find((e) => e.id === id)?.name || '-'

    const reviewModalOpen = ref(false)
    const reviewTarget = ref<any>(null)
    const reviewDraft = ref('')

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const openReview = (r: any) => {
      reviewTarget.value = r
      reviewDraft.value = r.reviewComment || ''
      reviewModalOpen.value = true
    }

    const confirmReview = () => {
      if (!reviewTarget.value) return
      if (!reviewDraft.value.trim()) {
        messageType.value = 'error'
        message.value = '请填写点评内容'
        return
      }
      const reviewerId = store.employees[0]?.id

      reviewTarget.value.reviewerId = reviewerId
      reviewTarget.value.reviewComment = reviewDraft.value
      reviewTarget.value.reviewAt = new Date().toISOString()

      messageType.value = 'success'
      message.value = '点评提交成功'
      reviewModalOpen.value = false
    }

    return {
      filterReviewed,
      filtered,
      empName,
      reviewModalOpen,
      reviewTarget,
      reviewDraft,
      openReview,
      confirmReview,
      message,
      messageType,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  background: #F8FAFC;
  min-height: 100%;
  padding: 16px;
}

.page-title {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 16px;
  color: #0F172A;
}

.toolbar-card {
  margin-bottom: 16px;
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 14px 16px;
  }
}

.toolbar-content {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.table-card {
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 0;
  }
}

:deep(.el-table) {
  border-radius: 12px;
  
  th.el-table__cell {
    background: #F8FAFC;
    color: #0F172A;
    font-weight: 700;
    font-size: 13px;
  }
  
  td.el-table__cell {
    font-size: 13px;
    color: #334155;
  }
}

.attendance-item {
  display: inline-block;
  margin-right: 8px;
  font-size: 12px;
  color: #475569;
}

.dialog-subtitle {
  font-size: 14px;
  color: #475569;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E5E7EB;
}

.dialog-field {
  margin-bottom: 16px;
  
  label {
    display: block;
    font-size: 13px;
    color: #334155;
    font-weight: 500;
    margin-bottom: 8px;
  }
}

.prev-comment {
  margin-top: 12px;
  border-radius: 8px;
}

.message-alert {
  margin-top: 16px;
  border-radius: 10px;
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: auto;
  max-width: 400px;
  z-index: 2000;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;
  
  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}

:deep(.el-tag--success) {
  color: #059669;
  border-color: #A7F3D0;
  background-color: #ECFDF5;
}

:deep(.el-tag--info) {
  color: #475569;
  border-color: #E2E8F0;
  background-color: #F1F5F9;
}

:deep(.el-dialog) {
  border-radius: 12px;
  
  .el-dialog__header {
    border-bottom: 1px solid #E5E7EB;
    padding: 16px 20px;
    margin-right: 0;
  }
  
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-dialog__footer {
    border-top: 1px solid #E5E7EB;
    padding: 14px 20px;
  }
}

@media (max-width: 768px) {
  .page {
    padding: 12px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
  }
  
  .message-alert {
    left: 12px;
    right: 12px;
    max-width: none;
  }
}
</style>

