<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商机项目详情</h2>
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !opp" description="未找到商机" :image-size="100" />

    <template v-else-if="opp">
      <!-- 概览 -->
      <el-card class="summary-card" shadow="never">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="商机名称">
            <span class="opp-name">{{ opp.oppName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ opp.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            <el-tag :type="oppStageTagType(opp.stage)" effect="light" round>
              {{ oppStageLabel(opp.stage) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="项目经理">{{ opp.pmName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="解决方案经理">{{ opp.smName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交付经理">{{ opp.dmName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="模板">{{ opp.templateName || '未选择' }}</el-descriptions-item>
          <el-descriptions-item label="环节数">{{ opp.stages?.length || 0 }}</el-descriptions-item>
          <el-descriptions-item label="任务数">{{ opp.tasks?.length || 0 }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="!opp.templateId"
          type="warning"
          :closable="false"
          show-icon
          class="alert-banner"
        >
          <template #title>
            该商机尚未选择模板,请先 <el-link type="primary" @click="goTemplate">选择推进模板</el-link> 生成环节
          </template>
        </el-alert>

        <div v-if="isPM && opp.templateId" class="stage-actions">
          <span class="stage-info">
            下一阶段: <strong>{{ nextStageLabel || '已是终态(DELIVERY)' }}</strong>
          </span>
          <el-button
            type="primary"
            :icon="Promotion"
            :disabled="!nextStageLabel"
            :loading="advancing"
            @click="advanceStage"
          >
            推进到下一阶段
          </el-button>
        </div>
      </el-card>

      <el-card class="tabs-card" shadow="never">
        <el-tabs v-model="tab">
          <!-- TAB 1: 环节责任人 -->
          <el-tab-pane name="responsibility">
            <template #label>
              <span class="tab-label"><el-icon><User /></el-icon> 环节责任人</span>
            </template>

            <el-alert type="info" :closable="false" show-icon class="hint-alert">
              <template #title>
                项目经理可指定全部环节的责任人;其他角色仅可改自己当前负责的环节。未指定时由「铁三角」(PM/SM/DM)操作该环节任务
              </template>
            </el-alert>

            <el-empty
              v-if="!opp.stages || opp.stages.length === 0"
              description="未生成环节(请先选择模板)"
              :image-size="80"
            />
            <el-row v-else :gutter="16">
              <el-col v-for="s in opp.stages" :key="s.id" :xs="24" :md="12">
                <div class="step-card">
                  <div class="step-card-title">
                    <span class="step-num">{{ s.sortOrder }}</span>
                    {{ s.stageName }}
                    <el-tag size="small" effect="plain" :type="s.status === 'DONE' ? 'success' : 'info'">
                      {{ stageStatusLabel(s.status) }}
                    </el-tag>
                  </div>
                  <div class="step-form">
                    <div class="step-label">责任人</div>
                    <el-select
                      v-model="stageOwnerDraft[s.id]"
                      placeholder="选择责任人"
                      clearable
                      filterable
                      style="width: 100%"
                      :disabled="!canEditStageOwner(s)"
                    >
                      <el-option
                        v-for="m in ironTriangleOptions"
                        :key="m.id"
                        :label="`${m.name}(${m.role})`"
                        :value="m.id"
                      />
                    </el-select>
                    <div v-if="canEditStageOwner(s)" class="step-action">
                      <el-button
                        size="small"
                        type="primary"
                        :disabled="stageOwnerDraft[s.id] === s.ownerId"
                        @click="saveStageOwner(s)"
                      >
                        保存
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- TAB 2: 组员管理 -->
          <el-tab-pane name="groups">
            <template #label>
              <span class="tab-label"><el-icon><UserFilled /></el-icon> 组员管理</span>
            </template>

            <el-alert type="info" :closable="false" show-icon class="hint-alert">
              <template #title>
                <strong>核心组</strong> 仅<strong>项目经理</strong>可增删(业务规则 #7);<strong>支撑组</strong>由<strong>铁三角</strong>(PM/SM/DM/商机负责人)管理。
              </template>
            </el-alert>

            <div class="group-section">
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Star /></el-icon>
                  核心组
                  <el-tag type="danger" size="small" effect="plain" round>{{ coreMembers.length }}</el-tag>
                </div>
                <el-button v-if="canEditCore" type="primary" :icon="Plus" size="small" @click="openAddDialog('CORE')">
                  添加核心组成员
                </el-button>
              </div>
              <el-empty v-if="coreMembers.length === 0" description="暂无核心组成员" :image-size="60" />
              <div v-else class="member-grid">
                <div v-for="m in coreMembers" :key="m.id" class="member-item core">
                  <div class="member-info">
                    <div class="member-name">{{ m.userName || m.username || '-' }}</div>
                    <div class="member-meta">
                      <span v-if="m.groupName">{{ m.groupName }}</span>
                      <span v-if="m.groupName && m.role" class="meta-sep">·</span>
                      <span v-if="m.role">{{ m.role }}</span>
                    </div>
                  </div>
                  <el-button
                    v-if="canEditCore"
                    type="danger"
                    :icon="Delete"
                    size="small"
                    circle
                    @click="removeMember(m)"
                  />
                </div>
              </div>
            </div>

            <div class="group-section">
              <div class="section-header">
                <div class="section-title">
                  <el-icon><Connection /></el-icon>
                  支撑组
                  <el-tag size="small" effect="plain" round>{{ supportMembers.length }}</el-tag>
                </div>
                <el-button v-if="canEditSupport" type="primary" :icon="Plus" size="small" @click="openAddDialog('SUPPORT')">
                  添加支撑组成员
                </el-button>
              </div>
              <el-empty v-if="supportMembers.length === 0" description="暂无支撑组成员" :image-size="60" />
              <div v-else class="member-grid">
                <div v-for="m in supportMembers" :key="m.id" class="member-item support">
                  <div class="member-info">
                    <div class="member-name">{{ m.userName || m.username || '-' }}</div>
                    <div class="member-meta">
                      <span v-if="m.groupName">{{ m.groupName }}</span>
                      <span v-if="m.groupName && m.role" class="meta-sep">·</span>
                      <span v-if="m.role">{{ m.role }}</span>
                    </div>
                  </div>
                  <el-button
                    v-if="canEditSupport"
                    type="danger"
                    :icon="Delete"
                    size="small"
                    circle
                    @click="removeMember(m)"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- TAB 3: 项目运作 -->
          <el-tab-pane name="operation">
            <template #label>
              <span class="tab-label"><el-icon><TrendCharts /></el-icon> 项目运作</span>
            </template>

            <el-empty
              v-if="!opp.stages || opp.stages.length === 0"
              description="暂无环节"
              :image-size="80"
            />
            <el-timeline v-else>
              <el-timeline-item
                v-for="s in opp.stages"
                :key="s.id"
                placement="top"
                :color="stageTimelineColor(s)"
                :timestamp="`步骤 ${s.sortOrder} · 责任人: ${s.ownerName || '未指定(铁三角)'}`"
              >
                <div class="timeline-title">{{ s.stageName }}</div>
                <div class="timeline-meta">
                  关联任务: {{ tasksOfStage(s.id).length }} 条 ·
                  完成: {{ tasksOfStage(s.id).filter(t => t.status === 'DONE').length }} 条
                </div>
                <el-progress
                  :percentage="stageProgress(s)"
                  :status="stageProgress(s) === 100 ? 'success' : ''"
                  :stroke-width="8"
                />
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>

          <!-- TAB 4: 任务管理 -->
          <el-tab-pane name="tasks">
            <template #label>
              <span class="tab-label"><el-icon><Tickets /></el-icon> 任务管理</span>
            </template>

            <div class="task-toolbar">
              <el-button
                v-if="canCreateTask"
                type="primary"
                :icon="Plus"
                @click="openTaskDialog()"
              >
                新增任务
              </el-button>
              <span v-else class="muted">仅商机的项目经理/解决方案经理/交付经理/负责人可创建任务</span>
            </div>

            <el-table :data="opp.tasks || []" stripe empty-text="暂无任务">
              <el-table-column label="任务名称" prop="taskName" min-width="200" />
              <el-table-column label="所属环节" prop="stageName" width="140">
                <template #default="{ row }">{{ row.stageName || '-' }}</template>
              </el-table-column>
              <el-table-column label="受理人" width="120">
                <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
              </el-table-column>
              <el-table-column label="派发人" width="120">
                <template #default="{ row }">{{ row.assignByName || '-' }}</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="taskStatusTagType(row.status)" size="small">
                    {{ taskStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="进度" width="160">
                <template #default="{ row }">
                  <el-progress
                    :percentage="taskProgress(row)"
                    :status="row.status === 'DONE' ? 'success' : ''"
                    :stroke-width="6"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="320" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="canEditProgress(row)"
                    link
                    type="primary"
                    @click="openProgressDialog(row)"
                  >
                    调进度
                  </el-button>
                  <el-button
                    v-if="canReplyTask(row)"
                    link
                    type="primary"
                    @click="openReplyDialog(row)"
                  >
                    回复
                  </el-button>
                  <el-button
                    v-if="canEditTask(row)"
                    link
                    type="primary"
                    @click="openTaskDialog(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    v-if="canCloseTask(row)"
                    link
                    type="danger"
                    @click="closeTask(row)"
                  >
                    关闭
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>

    <!-- 任务编辑对话框 -->
    <el-dialog v-model="taskDialogOpen" :title="editingTask ? '编辑任务' : '新增任务'" width="540px">
      <el-form :model="taskForm" label-position="top">
        <el-form-item label="任务名称" required>
          <el-input v-model="taskForm.taskName" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="所属环节">
          <el-select v-model="taskForm.stageId" placeholder="选择环节" clearable filterable style="width: 100%">
            <el-option v-for="s in opp?.stages || []" :key="s.id" :label="s.stageName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务内容">
          <el-input v-model="taskForm.content" type="textarea" :rows="4" placeholder="任务描述" />
        </el-form-item>
        <el-form-item label="受理人">
          <el-select v-model="taskForm.assigneeId" placeholder="选择受理人" clearable filterable style="width: 100%">
            <el-option
              v-for="m in ironTriangleOptions"
              :key="m.id"
              :label="`${m.name}(${m.role})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="taskSaving" @click="saveTask">保存</el-button>
      </template>
    </el-dialog>

    <!-- 任务回复对话框 -->
    <el-dialog v-model="replyDialogOpen" title="任务回复" width="540px">
      <div v-if="replyingTask" class="reply-task-meta">
        <div><strong>{{ replyingTask.taskName }}</strong></div>
        <div class="muted">受理人: {{ replyingTask.assigneeName || '-' }} · 派发人: {{ replyingTask.assignByName || '-' }}</div>
      </div>
      <el-form :model="replyForm" label-position="top">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.replyContent" type="textarea" :rows="6" placeholder="任务进展、风险、需要的支持..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="replySaving" @click="saveReply">保存</el-button>
      </template>
    </el-dialog>

    <!-- 任务进度对话框 -->
    <el-dialog v-model="progressDialogOpen" title="调整任务进度" width="480px">
      <div v-if="progressTask" class="reply-task-meta">
        <div><strong>{{ progressTask.taskName }}</strong></div>
        <div class="muted">受理人: {{ progressTask.assigneeName || '-' }}</div>
      </div>
      <el-form :model="progressForm" label-position="top">
        <el-form-item label="进度(0~100)">
          <el-slider
            v-model="progressForm.progress"
            :min="0"
            :max="100"
            :step="5"
            show-input
            :marks="{ 0: '0%', 25: '25%', 50: '50%', 75: '75%', 100: '100%' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="progressSaving" @click="saveProgress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加组员对话框 -->
    <el-dialog v-model="memberDialogOpen" :title="`添加${memberTypeLabel(memberForm.memberType)}成员`" width="540px">
      <el-form :model="memberForm" label-position="top">
        <el-form-item label="按角色筛选用户" required>
          <el-select
            v-model="memberForm.roleCode"
            placeholder="选择角色"
            style="width: 100%"
            @change="loadCandidatesByRole"
          >
            <el-option label="解决方案经理(SM)" value="SOLUTION_MANAGER" />
            <el-option label="交付经理(DM)" value="DELIVERY_MANAGER" />
            <el-option label="项目经理(PM)" value="PROJECT_MANAGER" />
            <el-option label="客户经理(CM)" value="CUSTOMER_MANAGER" />
            <el-option label="销售(SALES)" value="SALES" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择用户" required>
          <el-select
            v-model="memberForm.userId"
            placeholder="先选角色,再选用户"
            filterable
            :loading="memberCandidatesLoading"
            style="width: 100%"
          >
            <el-option
              v-for="u in memberCandidates"
              :key="u.id"
              :label="`${u.realName || u.username}(${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分组名称(可选)">
          <el-input v-model="memberForm.groupName" placeholder="例如:研发 / 测试 / 商务" clearable />
        </el-form-item>
        <el-form-item label="组内职责(可选)">
          <el-input v-model="memberForm.role" placeholder="例如:研发负责人 / 测试组长" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="memberSaving" @click="saveMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, User, UserFilled, TrendCharts, Tickets, Plus,
  Promotion, Star, Connection, Delete,
} from '@element-plus/icons-vue'
import {
  opportunityApi, OpportunityDetail, OppStageInstance, oppStageLabel, oppStageTagType,
} from '@/api/opportunity'
import {
  oppTaskApi, TaskListItem, taskStatusLabel, taskStatusTagType,
} from '@/api/oppTask'
import { oppTeamApi, TeamMember, MemberType, memberTypeLabel } from '@/api/oppTeam'
import { userApi, AdminUser } from '@/api/user'
import { getAuthState } from '@/auth/authStore'

type IronOption = { id: number; name: string; role: string }

const STAGE_ORDER = ['VALIDATE', 'NEGOTIATE', 'IMPLEMENT', 'DELIVERY']

export default defineComponent({
  name: 'OpportunityDetailView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const oppId = Number(route.query.opportunityId || 0)

    const opp = ref<OpportunityDetail | null>(null)
    const loading = ref(false)
    const tab = ref('responsibility')
    const stageOwnerDraft = reactive<Record<number, number | undefined>>({})
    const advancing = ref(false)

    const auth = getAuthState()
    const currentUserId = computed(() => Number(auth.user?.id || 0))

    const isPM = computed(() => opp.value?.pmId === currentUserId.value)
    const isManager = computed(() => opp.value?.managerId === currentUserId.value)
    const isSM = computed(() => opp.value?.smId === currentUserId.value)
    const isDM = computed(() => opp.value?.dmId === currentUserId.value)
    const isIronTriangle = computed(() => isPM.value || isManager.value || isSM.value || isDM.value)
    const canCreateTask = computed(() => isIronTriangle.value)

    /** 阶段推进相关 */
    const nextStageLabel = computed(() => {
      if (!opp.value) return ''
      const idx = STAGE_ORDER.indexOf(opp.value.stage)
      if (idx < 0 || idx >= STAGE_ORDER.length - 1) return ''
      return oppStageLabel(STAGE_ORDER[idx + 1])
    })

    /** 铁三角候选人(用于责任人/受理人下拉) */
    const ironTriangleOptions = computed<IronOption[]>(() => {
      if (!opp.value) return []
      const list: IronOption[] = []
      const seen = new Set<number>()
      const push = (id?: number, name?: string, role?: string) => {
        if (id && !seen.has(id)) {
          list.push({ id, name: name || '未知', role: role || '' })
          seen.add(id)
        }
      }
      push(opp.value.pmId, opp.value.pmName, '项目经理')
      push(opp.value.smId, opp.value.smName, '解决方案经理')
      push(opp.value.dmId, opp.value.dmName, '交付经理')
      push(opp.value.managerId, opp.value.managerName, '商机负责人')
      return list
    })

    const syncStageDraft = () => {
      if (!opp.value) return
      ;(opp.value.stages || []).forEach((s) => {
        stageOwnerDraft[s.id] = s.ownerId
      })
    }

    /** 组员管理 */
    const teamMembers = ref<TeamMember[]>([])
    const coreMembers = computed(() => teamMembers.value.filter((m) => m.memberType === 'CORE'))
    const supportMembers = computed(() => teamMembers.value.filter((m) => m.memberType === 'SUPPORT'))
    const canEditCore = computed(() => isPM.value)
    const canEditSupport = computed(() => isIronTriangle.value)

    const reloadTeam = async () => {
      if (!oppId) return
      try {
        teamMembers.value = await oppTeamApi.list(oppId)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载组员失败')
        teamMembers.value = []
      }
    }

    const reload = async () => {
      if (!oppId) return
      loading.value = true
      try {
        opp.value = await opportunityApi.detail(oppId)
        syncStageDraft()
        await reloadTeam()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        opp.value = null
      } finally {
        loading.value = false
      }
    }

    const advanceStage = async () => {
      if (!opp.value || !nextStageLabel.value) return
      try {
        await ElMessageBox.confirm(
          `确认将商机推进到「${nextStageLabel.value}」阶段?推进后不可倒退`,
          '推进阶段',
          { confirmButtonText: '推进', cancelButtonText: '取消', type: 'warning' },
        )
      } catch { return }

      advancing.value = true
      try {
        await opportunityApi.advanceStage(opp.value.id)
        ElMessage.success('已推进到下一阶段')
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '推进失败')
      } finally {
        advancing.value = false
      }
    }

    const canEditStageOwner = (s: OppStageInstance) => isPM.value || s.ownerId === currentUserId.value
    const saveStageOwner = async (s: OppStageInstance) => {
      try {
        const ownerId = stageOwnerDraft[s.id] ?? null
        await opportunityApi.setStageOwner(s.id, ownerId)
        ElMessage.success('已更新责任人')
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      }
    }

    const tasksOfStage = (stageId: number) =>
      (opp.value?.tasks || []).filter((t) => t.stageId === stageId)
    const stageProgress = (s: OppStageInstance): number => {
      const tasks = tasksOfStage(s.id)
      if (tasks.length === 0) return s.status === 'DONE' ? 100 : 0
      const done = tasks.filter((t) => t.status === 'DONE').length
      return Math.round((done / tasks.length) * 100)
    }
    const stageTimelineColor = (s: OppStageInstance) => {
      if (s.status === 'DONE') return '#059669'
      if (stageProgress(s) > 0) return '#0369A1'
      return '#94A3B8'
    }
    const stageStatusLabel = (st?: string) => {
      switch (st) {
        case 'PENDING': return '待开始'
        case 'DOING': return '进行中'
        case 'DONE': return '已完成'
        default: return '-'
      }
    }
    /** 任务进度: 优先读 row.progress(后端独立 INT 列),无值时按 status 派生 */
    const taskProgress = (row: TaskListItem): number => {
      if (typeof row.progress === 'number') {
        return Math.max(0, Math.min(100, row.progress))
      }
      switch (row.status) {
        case 'TODO': return 0
        case 'DOING': return 50
        case 'DONE': return 100
        default: return 0
      }
    }

    /** 任务对话框 */
    const taskDialogOpen = ref(false)
    const editingTask = ref<TaskListItem | null>(null)
    const taskForm = reactive({
      taskName: '',
      stageId: undefined as number | undefined,
      content: '',
      assigneeId: undefined as number | undefined,
    })
    const taskSaving = ref(false)
    const openTaskDialog = (t?: TaskListItem) => {
      editingTask.value = t || null
      taskForm.taskName = t?.taskName || ''
      taskForm.stageId = t?.stageId
      taskForm.content = t?.content || ''
      taskForm.assigneeId = t?.assigneeId
      taskDialogOpen.value = true
    }
    const saveTask = async () => {
      if (!opp.value || !taskForm.taskName) {
        ElMessage.error('请填写任务名称')
        return
      }
      taskSaving.value = true
      try {
        if (editingTask.value) {
          await oppTaskApi.update(editingTask.value.id, {
            taskName: taskForm.taskName,
            stageId: taskForm.stageId,
            content: taskForm.content,
            assigneeId: taskForm.assigneeId,
          })
          ElMessage.success('任务已更新')
        } else {
          await oppTaskApi.create({
            oppId: opp.value.id,
            taskName: taskForm.taskName,
            stageId: taskForm.stageId,
            content: taskForm.content,
            assigneeId: taskForm.assigneeId,
          })
          ElMessage.success('任务已创建')
        }
        taskDialogOpen.value = false
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        taskSaving.value = false
      }
    }

    /** 任务回复对话框 */
    const replyDialogOpen = ref(false)
    const replyingTask = ref<TaskListItem | null>(null)
    const replyForm = reactive({ replyContent: '' })
    const replySaving = ref(false)
    const openReplyDialog = (t: TaskListItem) => {
      replyingTask.value = t
      replyForm.replyContent = t.replyContent || ''
      replyDialogOpen.value = true
    }
    const saveReply = async () => {
      if (!replyingTask.value) return
      replySaving.value = true
      try {
        await oppTaskApi.reply(replyingTask.value.id, replyForm.replyContent || '')
        ElMessage.success('回复已保存')
        replyDialogOpen.value = false
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        replySaving.value = false
      }
    }

    const canEditTask = (t: TaskListItem) => isIronTriangle.value && t.status !== 'DONE'
    const canCloseTask = (t: TaskListItem) => isIronTriangle.value && t.status !== 'DONE'
    const canReplyTask = (t: TaskListItem) =>
      t.status !== 'DONE' && (isIronTriangle.value || t.assigneeId === currentUserId.value)
    const canEditProgress = (t: TaskListItem) =>
      t.status !== 'DONE' && (isIronTriangle.value || t.assigneeId === currentUserId.value)

    /** 进度对话框 */
    const progressDialogOpen = ref(false)
    const progressTask = ref<TaskListItem | null>(null)
    const progressForm = reactive({ progress: 0 })
    const progressSaving = ref(false)
    const openProgressDialog = (t: TaskListItem) => {
      progressTask.value = t
      progressForm.progress = typeof t.progress === 'number' ? t.progress : taskProgress(t)
      progressDialogOpen.value = true
    }
    const saveProgress = async () => {
      if (!progressTask.value) return
      progressSaving.value = true
      try {
        await oppTaskApi.updateProgress(progressTask.value.id, progressForm.progress)
        ElMessage.success('进度已更新')
        progressDialogOpen.value = false
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        progressSaving.value = false
      }
    }

    const closeTask = async (t: TaskListItem) => {
      try {
        await ElMessageBox.confirm(`关闭任务「${t.taskName}」?关闭后不可再回复或修改`, '确认关闭', {
          confirmButtonText: '关闭任务', cancelButtonText: '取消', type: 'warning',
        })
      } catch { return }
      try {
        await oppTaskApi.close(t.id)
        ElMessage.success('任务已关闭')
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '关闭失败')
      }
    }

    /** 添加组员对话框 */
    const memberDialogOpen = ref(false)
    const memberCandidates = ref<AdminUser[]>([])
    const memberCandidatesLoading = ref(false)
    const memberSaving = ref(false)
    const memberForm = reactive({
      memberType: 'CORE' as MemberType,
      roleCode: '',
      userId: undefined as number | undefined,
      groupName: '',
      role: '',
    })
    const openAddDialog = (memberType: MemberType) => {
      memberForm.memberType = memberType
      memberForm.roleCode = ''
      memberForm.userId = undefined
      memberForm.groupName = ''
      memberForm.role = ''
      memberCandidates.value = []
      memberDialogOpen.value = true
    }
    const loadCandidatesByRole = async () => {
      if (!memberForm.roleCode) return
      memberCandidatesLoading.value = true
      memberForm.userId = undefined
      try {
        memberCandidates.value = await userApi.listByRole(memberForm.roleCode)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载用户列表失败')
        memberCandidates.value = []
      } finally {
        memberCandidatesLoading.value = false
      }
    }
    const saveMember = async () => {
      if (!opp.value || !memberForm.userId) {
        ElMessage.error('请选择用户')
        return
      }
      memberSaving.value = true
      try {
        await oppTeamApi.add({
          oppId: opp.value.id,
          userId: memberForm.userId,
          memberType: memberForm.memberType,
          groupName: memberForm.groupName || undefined,
          role: memberForm.role || undefined,
        })
        ElMessage.success('已添加')
        memberDialogOpen.value = false
        await reloadTeam()
      } catch (e: any) {
        ElMessage.error(e?.message || '添加失败')
      } finally {
        memberSaving.value = false
      }
    }
    const removeMember = async (m: TeamMember) => {
      try {
        await ElMessageBox.confirm(
          `从${memberTypeLabel(m.memberType)}中移除「${m.userName || m.username}」?`,
          '确认移除',
          { confirmButtonText: '移除', cancelButtonText: '取消', type: 'warning' },
        )
      } catch { return }
      try {
        await oppTeamApi.remove(m.id)
        ElMessage.success('已移除')
        await reloadTeam()
      } catch (e: any) {
        ElMessage.error(e?.message || '移除失败')
      }
    }

    const goBack = () => router.push({ path: '/opportunities/list' })
    const goTemplate = () => router.push({ path: '/opportunities/template', query: { opportunityId: oppId } })

    onMounted(reload)

    return {
      opp, loading, tab, stageOwnerDraft,
      advancing, advanceStage, nextStageLabel,
      ironTriangleOptions,
      isPM, isIronTriangle, canCreateTask,
      canEditStageOwner, saveStageOwner,
      tasksOfStage, stageProgress, stageTimelineColor, stageStatusLabel,
      taskProgress,
      // tasks
      taskDialogOpen, editingTask, taskForm, taskSaving,
      openTaskDialog, saveTask,
      replyDialogOpen, replyingTask, replyForm, replySaving,
      openReplyDialog, saveReply,
      canEditTask, canCloseTask, canReplyTask, canEditProgress, closeTask,
      // progress
      progressDialogOpen, progressTask, progressForm, progressSaving,
      openProgressDialog, saveProgress,
      // team
      coreMembers, supportMembers, canEditCore, canEditSupport,
      memberDialogOpen, memberCandidates, memberCandidatesLoading, memberSaving, memberForm,
      openAddDialog, loadCandidatesByRole, saveMember, removeMember,
      memberTypeLabel,
      goBack, goTemplate,
      oppStageLabel, oppStageTagType, taskStatusLabel, taskStatusTagType,
      ArrowLeft, User, UserFilled, TrendCharts, Tickets, Plus,
      Promotion, Star, Connection, Delete,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.summary-card, .tabs-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
}
.summary-card :deep(.el-card__body) { padding: 16px 20px; }
.tabs-card :deep(.el-card__body) { padding: 12px 20px 20px; }
.opp-name { font-weight: 600; color: #0F172A; }
:deep(.el-descriptions__label) { width: 120px; background: #F8FAFC !important; color: #475569 !important; font-weight: 500 !important; }
.alert-banner, .hint-alert { margin: 12px 0; border-radius: 10px; }

.stage-actions {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 12px; padding-top: 12px; border-top: 1px solid #F1F5F9;
}
.stage-info { font-size: 13px; color: #475569; strong { color: #0369A1; font-weight: 600; } }

.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.step-card {
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px;
  margin-bottom: 16px; background: #FFFFFF;
  &:hover { border-color: #0369A1; }
}
.step-card-title {
  display: flex; align-items: center; gap: 8px;
  font-weight: 600; color: #0F172A; margin-bottom: 12px;
}
.step-num {
  width: 24px; height: 24px; border-radius: 50%;
  background: #0369A1; color: white; font-size: 12px; font-weight: 600;
  display: inline-flex; align-items: center; justify-content: center;
}
.step-form { display: flex; flex-direction: column; gap: 8px; }
.step-label { font-size: 12px; color: #64748B; }
.step-action { margin-top: 8px; display: flex; justify-content: flex-end; }

.timeline-title { font-weight: 600; color: #0F172A; }
.timeline-meta { font-size: 12px; color: #64748B; margin: 4px 0 8px; }

.task-toolbar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
}
.muted { color: #94A3B8; font-size: 13px; }
.reply-task-meta {
  padding: 8px 12px; background: #F8FAFC; border-radius: 8px; margin-bottom: 12px;
  strong { color: #0F172A; }
}

/* 组员管理样式 */
.group-section {
  margin-top: 20px;
  &:first-of-type { margin-top: 0; }
}
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E2E8F0;
}
.section-title {
  display: flex; align-items: center; gap: 8px;
  font-weight: 600; font-size: 14px; color: #0F172A;
}
.member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 10px;
}
.member-item {
  display: flex; align-items: center; justify-content: space-between;
  border: 1px solid #E2E8F0; border-radius: 8px;
  padding: 10px 14px; background: #FFFFFF;
  &.core {
    background: linear-gradient(0deg, rgba(220, 38, 38, 0.04), rgba(220, 38, 38, 0.04)), #FFFFFF;
    border-color: #FEE2E2;
  }
  &.support {
    background: linear-gradient(0deg, rgba(3, 105, 161, 0.04), rgba(3, 105, 161, 0.04)), #FFFFFF;
    border-color: #BAE6FD;
  }
}
.member-info { flex: 1; min-width: 0; }
.member-name {
  font-weight: 600; font-size: 13px; color: #0F172A; margin-bottom: 2px;
}
.member-meta {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: #64748B;
}
.meta-sep { color: #CBD5E1; }

:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
