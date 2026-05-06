<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">下属周报点评</h2>
      <span class="page-meta">主管视角 — 查看下属提交的周报并撰写点评</span>
    </div>

    <!-- 主管 dashboard: 本周聚合摘要 -->
    <el-card v-if="summary" class="summary-card" shadow="never">
      <template #header>
        <div class="summary-header">
          <div class="card-title">
            <el-icon><DataLine /></el-icon>
            <span>本周下属提交概览</span>
            <el-tag size="small" effect="plain" round>{{ summary.year }} 年 第 {{ summary.weekNum }} 周</el-tag>
          </div>
          <div class="summary-actions">
            <el-button size="small" :icon="ArrowLeft" :disabled="summaryLoading" @click="shiftWeek(-1)">上一周</el-button>
            <el-button size="small" :disabled="!canGoNext || summaryLoading" @click="shiftWeek(1)">下一周 <el-icon><ArrowRight /></el-icon></el-button>
            <el-button size="small" type="primary" :disabled="summaryLoading" @click="resetToCurrentWeek">回到本周</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="12" class="stat-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-tile">
            <div class="stat-num">{{ summary.totalCount }}</div>
            <div class="stat-label">下属总数</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-tile submitted">
            <div class="stat-num">{{ summary.submittedCount }} <span class="ratio">/ {{ summary.totalCount }}</span></div>
            <div class="stat-label">已提交 ({{ submitRatio }}%)</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-tile commented">
            <div class="stat-num">{{ summary.commentedCount }} <span class="ratio">/ {{ summary.submittedCount }}</span></div>
            <div class="stat-label">已点评 ({{ commentRatio }}%)</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-tile pending">
            <div class="stat-num">{{ pendingMembers.length }}</div>
            <div class="stat-label">未提交</div>
          </div>
        </el-col>
      </el-row>

      <div v-if="pendingMembers.length > 0" class="pending-list">
        <span class="pending-label">未提交人员:</span>
        <el-tag
          v-for="m in pendingMembers"
          :key="m.userId"
          type="warning"
          effect="plain"
          size="small"
          class="pending-tag"
        >
          {{ m.userName }}
        </el-tag>
      </div>
      <div v-else-if="summary.totalCount > 0" class="pending-empty">
        <el-icon><CircleCheck /></el-icon>
        本周下属周报已全部提交
      </div>
      <div v-else class="pending-empty muted">
        当前账号没有直接下属(检查 sys_user.supervisor_id 配置)
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card class="list-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">
              <el-icon><List /></el-icon>
              下属周报清单
              <el-tag size="small" effect="plain" round>{{ filtered.length }}</el-tag>
            </div>
          </template>

          <div class="filter-row">
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 130px" @change="reload">
              <el-option label="全部" value="" />
              <el-option label="待点评" value="SUBMITTED" />
              <el-option label="已点评" value="COMMENTED" />
            </el-select>
          </div>

          <el-empty v-if="filtered.length === 0" description="暂无周报" :image-size="80" />
          <div v-else class="report-list">
            <div
              v-for="r in filtered"
              :key="r.id"
              class="report-item"
              :class="{ active: selectedId === r.id }"
              @click="select(r.id)"
            >
              <div class="report-meta">
                <strong>{{ r.userName || '-' }}</strong>
                <span>{{ r.year }} 年 第 {{ r.weekNum }} 周</span>
              </div>
              <div class="report-status">
                <el-tag :type="reportStatusTagType(r.status)" size="small">
                  {{ reportStatusLabel(r.status) }}
                </el-tag>
                <span v-if="r.commentCount" class="comment-badge">{{ r.commentCount }} 条点评</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="14">
        <el-empty v-if="!detail" description="左侧选择一份周报" :image-size="100" class="empty-detail" />

        <template v-else>
          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-title">
                <el-icon><Reading /></el-icon>
                周报内容
                <span class="card-meta">— {{ detail.userName }} · {{ detail.year }}/W{{ detail.weekNum }}</span>
                <el-tag :type="reportStatusTagType(detail.status)" size="small" effect="light">
                  {{ reportStatusLabel(detail.status) }}
                </el-tag>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="出勤情况">
                <pre class="pre-content">{{ detail.attendance || '-' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="本周工作">
                <pre class="pre-content">{{ detail.thisWeekWork || '-' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="下周计划">
                <pre class="pre-content">{{ detail.nextWeekPlan || '-' }}</pre>
              </el-descriptions-item>
              <el-descriptions-item v-if="detail.submitTime" label="提交时间">
                {{ detail.submitTime }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="comment-card" shadow="never">
            <template #header>
              <div class="card-title">
                <el-icon><ChatLineRound /></el-icon>
                历史点评
                <el-tag size="small" effect="plain" round>{{ detail.comments?.length || 0 }}</el-tag>
              </div>
            </template>
            <el-empty v-if="!detail.comments || detail.comments.length === 0" description="暂无点评" :image-size="60" />
            <div v-else>
              <div v-for="c in detail.comments" :key="c.id" class="comment-item">
                <div class="comment-header">
                  <strong>{{ c.commenterName }}</strong>
                  <span class="comment-time">{{ c.createTime }}</span>
                </div>
                <div class="comment-body">{{ c.content }}</div>
              </div>
            </div>
          </el-card>

          <el-card v-if="detail.status !== 'DRAFT'" class="comment-input-card" shadow="never">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="4"
              placeholder="撰写点评内容,提交后将通知该员工..."
            />
            <div class="footer-actions">
              <el-button
                type="primary"
                :icon="Check"
                :loading="commenting"
                :disabled="!commentText.trim()"
                @click="submitComment"
              >
                提交点评
              </el-button>
            </div>
          </el-card>
          <el-alert v-else type="info" :closable="false" show-icon class="hint-alert">
            <template #title>员工尚未提交,无法点评</template>
          </el-alert>
        </template>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { List, Reading, ChatLineRound, Check, DataLine, ArrowLeft, ArrowRight, CircleCheck } from '@element-plus/icons-vue'
import {
  weeklyReportApi, WeeklyReport, WeeklyReportDetail, TeamSummary,
  reportStatusLabel, reportStatusTagType, getISOWeek,
} from '@/api/weeklyReport'

export default defineComponent({
  name: 'WeeklyReportReviewView',
  setup() {
    const reports = ref<WeeklyReport[]>([])
    const detail = ref<WeeklyReportDetail | null>(null)
    const selectedId = ref<number | null>(null)
    const loading = ref(false)
    const commenting = ref(false)
    const filterStatus = ref<'' | 'SUBMITTED' | 'COMMENTED'>('')
    const commentText = ref('')

    /** 主管 dashboard: 当前选中的周 */
    const summary = ref<TeamSummary | null>(null)
    const summaryLoading = ref(false)
    const initial = getISOWeek(new Date())
    const summaryYear = ref(initial.year)
    const summaryWeek = ref(initial.weekNum)

    const pendingMembers = computed(() =>
      summary.value ? summary.value.members.filter((m) => !m.status) : [],
    )
    const submitRatio = computed(() => {
      if (!summary.value || summary.value.totalCount === 0) return 0
      return Math.round((summary.value.submittedCount / summary.value.totalCount) * 100)
    })
    const commentRatio = computed(() => {
      if (!summary.value || summary.value.submittedCount === 0) return 0
      return Math.round((summary.value.commentedCount / summary.value.submittedCount) * 100)
    })
    const canGoNext = computed(() => {
      if (!summary.value) return false
      return summary.value.year < initial.year ||
        (summary.value.year === initial.year && summary.value.weekNum < initial.weekNum)
    })

    const loadSummary = async () => {
      summaryLoading.value = true
      try {
        summary.value = await weeklyReportApi.teamSummary(summaryYear.value, summaryWeek.value)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载概览失败')
        summary.value = null
      } finally {
        summaryLoading.value = false
      }
    }
    const shiftWeek = (delta: number) => {
      // 简化: 直接加减周数,跨年时减 52 / 加 52(ISO 周数 52 或 53,这里近似)
      let w = summaryWeek.value + delta
      let y = summaryYear.value
      if (w <= 0) { y -= 1; w += 52 }
      if (w > 52) { y += 1; w -= 52 }
      summaryYear.value = y
      summaryWeek.value = w
      loadSummary()
    }
    const resetToCurrentWeek = () => {
      summaryYear.value = initial.year
      summaryWeek.value = initial.weekNum
      loadSummary()
    }

    const filtered = computed(() => {
      if (!filterStatus.value) return reports.value
      return reports.value.filter((r) => r.status === filterStatus.value)
    })

    const reload = async () => {
      loading.value = true
      try {
        reports.value = await weeklyReportApi.listTeam()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        reports.value = []
      } finally {
        loading.value = false
      }
    }

    const select = async (id: number) => {
      selectedId.value = id
      commentText.value = ''
      try {
        detail.value = await weeklyReportApi.detail(id)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载详情失败')
        detail.value = null
      }
    }

    const submitComment = async () => {
      if (!detail.value || !commentText.value.trim()) return
      commenting.value = true
      try {
        await weeklyReportApi.comment(detail.value.id, commentText.value.trim())
        ElMessage.success('点评已提交')
        const id = detail.value.id
        commentText.value = ''
        await reload()
        await select(id)
      } catch (e: any) {
        ElMessage.error(e?.message || '提交失败')
      } finally {
        commenting.value = false
      }
    }

    onMounted(() => {
      reload()
      loadSummary()
    })

    return {
      reports, detail, selectedId, loading, commenting, filterStatus, commentText,
      filtered,
      reload, select, submitComment,
      // dashboard
      summary, summaryLoading, pendingMembers, submitRatio, commentRatio, canGoNext,
      shiftWeek, resetToCurrentWeek,
      reportStatusLabel, reportStatusTagType,
      List, Reading, ChatLineRound, Check, DataLine, ArrowLeft, ArrowRight, CircleCheck,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header {
  margin-bottom: 16px; display: flex; align-items: baseline; gap: 12px;
}
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.page-meta { color: #64748B; font-size: 13px; }

.list-card, .detail-card, .comment-card, .comment-input-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__header) {
    padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
  }
  :deep(.el-card__body) { padding: 12px 20px; }
}
.detail-card :deep(.el-card__body) { padding: 0; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-meta { color: #64748B; font-weight: 400; font-size: 13px; }

.filter-row { padding: 8px 0; }

.report-list { display: flex; flex-direction: column; gap: 8px; max-height: 480px; overflow-y: auto; }
.report-item {
  border: 1px solid #E2E8F0; border-radius: 8px; padding: 10px 12px;
  cursor: pointer; transition: all 0.15s; background: #FFFFFF;
  &:hover { border-color: #0369A1; }
  &.active { border-color: #0369A1; background: #F0F9FF; }
}
.report-meta {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: #475569; margin-bottom: 4px;
  strong { color: #0F172A; }
}
.report-status {
  display: flex; align-items: center; gap: 8px;
  font-size: 12px; color: #64748B;
}
.comment-badge { color: #059669; }

.empty-detail {
  margin: 60px auto;
}

.pre-content {
  white-space: pre-wrap; word-break: break-word; margin: 0;
  font-family: inherit; line-height: 1.6;
}

.comment-item {
  padding: 10px 0; border-bottom: 1px solid #F1F5F9;
  &:last-child { border-bottom: none; }
}
.comment-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 4px;
  strong { color: #0F172A; font-size: 13px; }
}
.comment-time { color: #94A3B8; font-size: 12px; }
.comment-body { color: #475569; font-size: 13px; line-height: 1.6; }

.footer-actions { display: flex; justify-content: flex-end; margin-top: 12px; }
.hint-alert { margin-bottom: 16px; border-radius: 10px; }

:deep(.el-descriptions__label) {
  width: 100px; background: #F8FAFC !important;
  color: #475569 !important; font-weight: 500 !important;
}

:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}

/* 主管 dashboard 摘要卡 */
.summary-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__header) {
    padding: 12px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
  }
  :deep(.el-card__body) { padding: 16px 20px; }
}
.summary-header {
  display: flex; justify-content: space-between; align-items: center; gap: 8px;
}
.summary-actions { display: flex; gap: 6px; }
.stat-row { margin-bottom: 12px; }
.stat-tile {
  border: 1px solid #E2E8F0; border-radius: 10px;
  padding: 14px 16px; background: #FFFFFF;
  &.submitted { border-color: #BAE6FD; background: #F0F9FF; }
  &.commented { border-color: #BBF7D0; background: #F0FDF4; }
  &.pending   { border-color: #FED7AA; background: #FFF7ED; }
}
.stat-num {
  font-size: 22px; font-weight: 700; color: #0369A1; line-height: 1.2;
  .ratio { font-size: 13px; color: #94A3B8; font-weight: 500; }
}
.stat-tile.submitted .stat-num { color: #0369A1; }
.stat-tile.commented .stat-num { color: #059669; }
.stat-tile.pending   .stat-num { color: #D97706; }
.stat-label { font-size: 12px; color: #64748B; margin-top: 4px; }
.pending-list {
  display: flex; flex-wrap: wrap; align-items: center; gap: 6px;
  padding-top: 8px; border-top: 1px dashed #F1F5F9;
}
.pending-label { font-size: 12px; color: #475569; margin-right: 4px; }
.pending-tag { margin: 0; }
.pending-empty {
  display: flex; align-items: center; gap: 6px;
  padding-top: 8px; border-top: 1px dashed #F1F5F9;
  color: #059669; font-size: 13px;
  &.muted { color: #94A3B8; }
}
.muted { color: #94A3B8; }
</style>
