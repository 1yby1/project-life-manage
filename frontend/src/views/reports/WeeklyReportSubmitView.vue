<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">提交周报</h2>
      <span class="page-meta">
        本周: {{ form.year }} 年第 {{ form.weekNum }} 周
      </span>
    </div>

    <el-card class="form-card" shadow="never" v-loading="loading">
      <template #header>
        <div class="card-title">
          <el-icon><Edit /></el-icon>
          周报内容
          <el-tag v-if="currentReport" :type="reportStatusTagType(currentReport.status)" size="small" effect="light" round>
            {{ reportStatusLabel(currentReport.status) }}
          </el-tag>
          <span v-else class="card-meta">— 新建草稿</span>
        </div>
      </template>

      <el-alert
        v-if="locked"
        type="warning"
        :closable="false"
        show-icon
        class="hint-alert"
      >
        <template #title>
          该周报已 <strong>{{ reportStatusLabel(currentReport?.status) }}</strong>,不可再修改(业务规则 #19)
        </template>
      </el-alert>

      <el-form :model="form" label-position="top" :disabled="locked">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="出勤情况(JSON: 每天一条)">
              <el-input
                v-model="form.attendance"
                type="textarea"
                :rows="4"
                placeholder='{"mon":"上班","tue":"上班","wed":"出差客户A",...}'
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="本周工作情况" required>
              <el-input
                v-model="form.thisWeekWork"
                type="textarea"
                :rows="4"
                placeholder="本周完成的关键工作、客户对接、合同进展..."
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="下周计划" required>
              <el-input
                v-model="form.nextWeekPlan"
                type="textarea"
                :rows="4"
                placeholder="下周重点任务、需要的支持..."
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="footer-actions">
        <el-button @click="$router.push('/')">取消</el-button>
        <el-button v-if="!locked" :icon="DocumentCopy" :loading="saving" @click="saveDraft">保存草稿</el-button>
        <el-button
          v-if="!locked"
          type="primary"
          :icon="Check"
          :loading="submitting"
          @click="submit"
        >
          提交周报
        </el-button>
      </div>
    </el-card>

    <el-card v-if="comments.length > 0" class="comment-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><ChatLineRound /></el-icon>
          主管点评
          <el-tag size="small" effect="plain" round>{{ comments.length }}</el-tag>
        </div>
      </template>
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <strong>{{ c.commenterName || '主管' }}</strong>
          <span class="comment-time">{{ c.createTime }}</span>
        </div>
        <div class="comment-body">{{ c.content }}</div>
      </div>
    </el-card>

    <el-card class="history-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Calendar /></el-icon>
          历史周报
          <span class="card-meta">— 共 {{ history.length }} 条</span>
        </div>
      </template>
      <el-table :data="history" stripe empty-text="暂无历史周报">
        <el-table-column label="年份" prop="year" width="80" />
        <el-table-column label="周数" prop="weekNum" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="reportStatusTagType(row.status)" size="small">
              {{ reportStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="点评数" prop="commentCount" width="90" />
        <el-table-column label="提交时间" prop="submitTime" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="loadReport(row.year, row.weekNum)">查看 / 编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, DocumentCopy, Check, ChatLineRound, Calendar } from '@element-plus/icons-vue'
import {
  weeklyReportApi, WeeklyReport, WeeklyReportComment,
  reportStatusLabel, reportStatusTagType, getISOWeek,
} from '@/api/weeklyReport'

export default defineComponent({
  name: 'WeeklyReportSubmitView',
  setup() {
    const { year, weekNum } = getISOWeek(new Date())

    const form = reactive({
      year, weekNum,
      attendance: '',
      thisWeekWork: '',
      nextWeekPlan: '',
    })

    const currentReport = ref<WeeklyReport | null>(null)
    const comments = ref<WeeklyReportComment[]>([])
    const history = ref<WeeklyReport[]>([])
    const loading = ref(false)
    const saving = ref(false)
    const submitting = ref(false)

    const locked = computed(() =>
      !!currentReport.value && currentReport.value.status !== 'DRAFT'
    )

    const reload = async () => {
      loading.value = true
      try {
        const list = await weeklyReportApi.listMy()
        history.value = list || []
        // 优先匹配当前 year/weekNum
        const match = list.find((r) => r.year === form.year && r.weekNum === form.weekNum)
        if (match) {
          await loadFullReport(match.id)
        } else {
          currentReport.value = null
          comments.value = []
          form.attendance = ''
          form.thisWeekWork = ''
          form.nextWeekPlan = ''
        }
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const loadFullReport = async (id: number) => {
      const detail = await weeklyReportApi.detail(id)
      currentReport.value = detail
      comments.value = detail.comments || []
      form.year = detail.year
      form.weekNum = detail.weekNum
      form.attendance = detail.attendance || ''
      form.thisWeekWork = detail.thisWeekWork || ''
      form.nextWeekPlan = detail.nextWeekPlan || ''
    }

    const loadReport = async (y: number, w: number) => {
      form.year = y
      form.weekNum = w
      await reload()
    }

    const saveDraft = async () => {
      if (!form.thisWeekWork || !form.nextWeekPlan) {
        ElMessage.error('请填写本周工作情况和下周计划')
        return
      }
      saving.value = true
      try {
        await weeklyReportApi.saveDraft({
          year: form.year,
          weekNum: form.weekNum,
          attendance: form.attendance,
          thisWeekWork: form.thisWeekWork,
          nextWeekPlan: form.nextWeekPlan,
        })
        ElMessage.success('草稿已保存')
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const submit = async () => {
      if (!form.thisWeekWork || !form.nextWeekPlan) {
        ElMessage.error('请填写本周工作情况和下周计划')
        return
      }
      try {
        await ElMessageBox.confirm('提交后周报将锁定,只能由主管点评。确认提交?', '确认提交', {
          confirmButtonText: '提交', cancelButtonText: '取消', type: 'warning',
        })
      } catch { return }

      submitting.value = true
      try {
        // 先保存最新内容
        const id = await weeklyReportApi.saveDraft({
          year: form.year,
          weekNum: form.weekNum,
          attendance: form.attendance,
          thisWeekWork: form.thisWeekWork,
          nextWeekPlan: form.nextWeekPlan,
        })
        await weeklyReportApi.submit(id)
        ElMessage.success('周报已提交,等待主管点评')
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }

    onMounted(reload)

    return {
      form, currentReport, comments, history, loading, saving, submitting, locked,
      saveDraft, submit, loadReport,
      reportStatusLabel, reportStatusTagType,
      Edit, DocumentCopy, Check, ChatLineRound, Calendar,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1100px; margin: 0 auto; }
.page-header { margin-bottom: 16px; display: flex; align-items: baseline; gap: 12px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.page-meta { color: #64748B; font-size: 13px; }
.form-card, .comment-card, .history-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__header) {
    padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
  }
  :deep(.el-card__body) { padding: 20px; }
}
.history-card :deep(.el-card__body) { padding: 0; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.hint-alert { margin-bottom: 16px; border-radius: 10px; }
.footer-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 16px; padding-top: 16px; border-top: 1px solid #F1F5F9;
}
.comment-item {
  padding: 12px 0; border-bottom: 1px solid #F1F5F9;
  &:last-child { border-bottom: none; }
}
.comment-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 4px;
  strong { color: #0F172A; font-size: 13px; }
}
.comment-time { color: #94A3B8; font-size: 12px; }
.comment-body { color: #475569; font-size: 13px; line-height: 1.6; }
:deep(.el-table th.el-table__cell) {
  background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
}
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
