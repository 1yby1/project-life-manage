<template>
  <div class="page">
    <div class="page-title">商机项目详情</div>

    <div v-if="!opportunity" class="empty">未找到商机</div>

    <div v-else class="card">
      <div class="summary">
        <div class="summary-item">
          <span class="label">商机名称</span>
          <span class="value">{{ opportunity.opportunityName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">阶段</span>
          <span class="value">{{ opportunity.stage }}</span>
        </div>
        <div class="summary-item">
          <span class="label">模板</span>
          <span class="value">{{ opportunity.templateKey || '-' }}</span>
        </div>
      </div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'responsibility' }" type="button" @click="tab = 'responsibility'">
          环节责任人选择
        </button>
        <button class="tab" :class="{ active: tab === 'groups' }" type="button" @click="tab = 'groups'">
          组员管理
        </button>
        <button class="tab" :class="{ active: tab === 'operation' }" type="button" @click="tab = 'operation'">
          项目运作
        </button>
        <button class="tab" :class="{ active: tab === 'tasks' }" type="button" @click="tab = 'tasks'">
          任务管理
        </button>
      </div>

      <div class="content">
        <!-- 环节责任人选择 -->
        <section v-if="tab === 'responsibility'" class="section">
          <div class="section-title">为四大核心环节指定责任人</div>
          <div class="section-hint">
            演示规则：若当前角色不是项目经理，则只能从“铁三角人员”中分配任务负责人。
          </div>

          <div class="step-grid">
            <div v-for="t in opportunity.tasks" :key="t.id" class="step-card">
              <div class="step-card-title">{{ t.taskName }}</div>
              <div class="field">
                <label>责任人</label>
                <select
                  v-model="taskOwnerDraft[t.id]"
                  :disabled="!canEditResponsibility"
                >
                  <option v-for="e in ownerOptions(t)" :key="e.id" :value="e.id">{{ e.name }}（{{ e.dept }}）</option>
                </select>
              </div>
              <div class="progressline">
                <div class="bar-wrap">
                  <div class="bar" :style="{ width: `${t.progress}%` }" />
                </div>
                <div class="progress-meta">{{ t.progress }}%</div>
              </div>
            </div>
          </div>

          <div class="footer-actions">
            <button class="btn btn-primary" type="button" :disabled="!canEditResponsibility" @click="saveOwners">
              保存责任人
            </button>
          </div>
        </section>

        <!-- 组员管理 -->
        <section v-else-if="tab === 'groups'" class="section">
          <div class="section-title">组员管理</div>
          <div class="section-hint">核心组：仅项目经理可新增/删除；其他工作组仅演示展示。</div>

          <div class="group-block">
            <div class="group-title">核心组（可编辑）</div>
            <div class="group-actions" v-if="canEditCoreGroup">
              <select v-model="addCoreId" class="select">
                <option value="">选择人员</option>
                <option v-for="e in candidateCoreMembers" :key="e.id" :value="e.id">{{ e.name }}（{{ e.dept }}）</option>
              </select>
              <button class="btn btn-primary" type="button" @click="addCoreMember">新增成员</button>
            </div>

            <div class="member-list">
              <div v-for="id in opportunity.coreGroupIds || []" :key="id" class="member">
                <div class="member-name">{{ empName(id) }}</div>
                <div class="member-meta">{{ empDept(id) }}</div>
                <button v-if="canEditCoreGroup" class="btn btn-small btn-danger" type="button" @click="removeCoreMember(id)">
                  删除
                </button>
              </div>
              <div v-if="(opportunity.coreGroupIds || []).length === 0" class="empty">暂无核心组成员</div>
            </div>
          </div>

          <div class="workgroup">
            <div class="work-title">四大工作组（演示展示）</div>
            <div class="work-grid">
              <div class="work">
                <div class="work-name">客户商务</div>
                <div class="work-meta">列表来源待接后端</div>
              </div>
              <div class="work">
                <div class="work-name">解决方案</div>
                <div class="work-meta">列表来源待接后端</div>
              </div>
              <div class="work">
                <div class="work-name">交付履行</div>
                <div class="work-meta">列表来源待接后端</div>
              </div>
              <div class="work">
                <div class="work-name">项目支撑</div>
                <div class="work-meta">列表来源待接后端</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 项目运作 -->
        <section v-else-if="tab === 'operation'" class="section">
          <div class="section-title">项目运作（环节视图 + 关键任务）</div>
          <div class="timeline">
            <div v-for="t in opportunity.tasks" :key="t.id" class="timeline-item">
              <div class="timeline-step">{{ t.taskName }}</div>
              <div class="timeline-meta">
                责任人：{{ empName(t.ownerId) }} / 进度：{{ t.progress }}%
              </div>
              <div class="bar-wrap">
                <div class="bar" :style="{ width: `${t.progress}%` }" />
              </div>
            </div>
            <div v-if="opportunity.tasks.length === 0" class="empty">暂无任务（请先选择模板生成）</div>
          </div>
        </section>

        <!-- 任务管理 -->
        <section v-else-if="tab === 'tasks'" class="section">
          <div class="section-title">任务管理</div>

          <div class="task-layout">
            <aside class="task-list">
              <div class="task-list-title">关键任务</div>
              <div v-for="t in opportunity.tasks" :key="t.id" class="task-item" :class="{ active: t.id === selectedTaskId }">
                <button class="task-item-btn" type="button" @click="selectedTaskId = t.id">
                  <div class="task-item-name">{{ t.taskName }}</div>
                  <div class="task-item-meta">
                    负责人：{{ empName(t.ownerId) }} / 状态：{{ t.status }}
                  </div>
                  <div class="task-item-progress">{{ t.progress }}%</div>
                </button>
              </div>
              <div v-if="opportunity.tasks.length === 0" class="empty">暂无任务</div>
            </aside>

            <div class="task-editor">
              <div v-if="!selectedTask" class="empty">请选择一个任务</div>
              <div v-else>
                <div class="task-editor-title">
                  {{ selectedTask.taskName }}
                </div>
                <div class="task-editor-sub">
                  负责人：{{ empName(selectedTask.ownerId) }}（未关闭任务可编辑） 
                </div>

                <div class="edit-block">
                  <div class="field-inline">
                    <label>任务责任人指定</label>
                    <select
                      v-model="ownerDraft"
                      :disabled="!canEditOwner(selectedTask)"
                    >
                      <option v-for="e in ownerOptionsForTask(selectedTask)" :key="e.id" :value="e.id">
                        {{ e.name }}（{{ e.dept }}）
                      </option>
                    </select>
                    <button
                      class="btn btn-small"
                      type="button"
                      :disabled="selectedTask.status !== '未关闭' || !canEditOwner(selectedTask) || ownerDraft === selectedTask.ownerId"
                      @click="saveOwner(selectedTask)"
                    >
                      编辑完成
                    </button>
                  </div>
                </div>

                <div class="edit-block">
                  <div class="field">
                    <label>回复进展（0~100） <span class="req">*</span></label>
                    <input type="number" v-model.number="reply.progress" min="0" max="100" :disabled="selectedTask.status !== '未关闭' || !canReply" />
                  </div>
                  <div class="field">
                    <label>修改内容 <span class="req">*</span></label>
                    <textarea v-model="reply.content" rows="4" :disabled="selectedTask.status !== '未关闭' || !canReply" />
                  </div>
                  <div class="upload">
                    <label>上传附件（演示：仅记录文件名）</label>
                    <input type="file" multiple :disabled="selectedTask.status !== '未关闭' || !canReply" @change="onReplyFiles" />
                    <div class="file-meta">{{ reply.attachments.length ? reply.attachments.join(', ') : '未上传' }}</div>
                  </div>

                  <div class="footer-actions">
                    <button
                      class="btn btn-primary"
                      type="button"
                      :disabled="selectedTask.status !== '未关闭' || !canReply"
                      @click="submitReply"
                    >
                      提交回复
                    </button>
                  </div>

                  <div v-if="replyMessage" class="message" :class="{ error: replyMessageType === 'error' }">{{ replyMessage }}</div>
                </div>

                <div class="updates">
                  <div class="updates-title">历史回复</div>
                  <div v-if="selectedTask.updates.length === 0" class="muted">暂无回复</div>
                  <div v-for="u in selectedTask.updates" :key="u.id" class="update">
                    <div class="update-top">
                      <div class="update-by">{{ empName(u.updatedById) }}</div>
                      <div class="update-at">{{ u.updatedAt.slice(0, 19).replace('T', ' ') }}</div>
                    </div>
                    <div class="update-content">{{ u.replyContent }}</div>
                    <div class="update-progress" v-if="typeof u.progress === 'number'">进度：{{ u.progress }}%</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEmployeeIdByRoleLabel, useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'OpportunityDetailView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const opportunityId = String(route.params.id || '')
    const opportunity = computed(() => store.opportunities.find((o) => o.id === opportunityId))

    const roleLabel = window.localStorage.getItem('demo_role') || '项目经理'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel)
    const myRoleTag = computed(() => store.employees.find((e) => e.id === employeeId)?.roleTag)

    const empName = (id: string) => store.employees.find((e) => e.id === id)?.name || '-'
    const empDept = (id: string) => store.employees.find((e) => e.id === id)?.dept || '-'

    const tab = ref<'responsibility' | 'groups' | 'operation' | 'tasks'>('responsibility')

    const triangleIds = computed(() => opportunity.value?.triangleIds || [])

    const canEditResponsibility = computed(() => {
      if (!employeeId) return false
      if (myRoleTag.value === '项目经理') return true
      return triangleIds.value.includes(employeeId) || opportunity.value?.managerId === employeeId
    })

    const taskOwnerDraft = reactive<Record<string, string>>({})
    const initOwnerDraft = () => {
      if (!opportunity.value) return
      for (const t of opportunity.value.tasks) {
        taskOwnerDraft[t.id] = t.ownerId
      }
    }
    watch(opportunity, initOwnerDraft, { immediate: true })

    const ownerOptions = (t: any) => {
      if (!opportunity.value) return []
      const all = store.employees
      if (myRoleTag.value === '项目经理' || (employeeId && opportunity.value.managerId === employeeId)) return all
      const allowed = new Set([...triangleIds.value, opportunity.value.managerId].filter(Boolean) as string[])
      return all.filter((e) => allowed.has(e.id))
    }

    const saveOwners = () => {
      if (!opportunity.value) return
      for (const t of opportunity.value.tasks) {
        t.ownerId = taskOwnerDraft[t.id]
      }
    }

    // 组员管理
    const canEditCoreGroup = computed(() => myRoleTag.value === '项目经理')
    const candidateCoreMembers = computed(() => store.employees.filter((e) => triangleIds.value.includes(e.id) || e.roleTag === '项目经理'))
    const addCoreId = ref('')

    const addCoreMember = () => {
      if (!opportunity.value) return
      if (!addCoreId.value) return
      if (!(opportunity.value.coreGroupIds || []).includes(addCoreId.value)) {
        opportunity.value.coreGroupIds = [...(opportunity.value.coreGroupIds || []), addCoreId.value]
      }
      addCoreId.value = ''
    }

    const removeCoreMember = (id: string) => {
      if (!opportunity.value) return
      opportunity.value.coreGroupIds = (opportunity.value.coreGroupIds || []).filter((x) => x !== id)
    }

    // 任务管理
    const selectedTaskId = ref('')
    watch(
      () => opportunity.value?.tasks?.length,
      () => {
        const list = opportunity.value?.tasks || []
        if (!selectedTaskId.value && list.length > 0) selectedTaskId.value = list[0].id
      },
      { immediate: true },
    )

    const selectedTask = computed(() => opportunity.value?.tasks.find((t) => t.id === selectedTaskId.value))

    const canReply = computed(() => {
      if (!employeeId) return false
      if (!selectedTask.value) return false
      if (selectedTask.value.status !== '未关闭') return false
      // 允许铁三角或商机负责人回复
      const ownerOk = triangleIds.value.includes(employeeId) || opportunity.value?.managerId === employeeId
      return ownerOk
    })

    const ownerDraft = ref('')
    watch(selectedTask, (t) => {
      if (!t) return
      ownerDraft.value = t.ownerId
    }, { immediate: true })

    const canEditOwner = (t: any) => {
      if (!employeeId) return false
      if (t.status !== '未关闭') return false
      return triangleIds.value.includes(employeeId) || opportunity.value?.managerId === employeeId || myRoleTag.value === '项目经理'
    }

    const ownerOptionsForTask = (t: any) => {
      // 演示：同“环节责任人”权限
      if (myRoleTag.value === '项目经理') return store.employees
      const allowed = new Set([...triangleIds.value, opportunity.value?.managerId].filter(Boolean) as string[])
      return store.employees.filter((e) => allowed.has(e.id))
    }

    const saveOwner = (t: any) => {
      t.ownerId = ownerDraft.value
    }

    const reply = reactive({
      progress: 0,
      content: '',
      attachments: [] as string[],
    })

    watch(selectedTask, (t) => {
      if (!t) return
      reply.progress = t.progress ?? 0
      reply.content = t.content || ''
      reply.attachments = []
      replyMessage.value = ''
    })

    const replyMessage = ref('')
    const replyMessageType = ref<'error' | 'success'>('success')

    const onReplyFiles = (evt: Event) => {
      const input = evt.target as HTMLInputElement
      const files = input.files ? Array.from(input.files) : []
      reply.attachments = files.map((f) => f.name)
    }

    const submitReply = () => {
      if (!selectedTask.value || !opportunity.value) return
      if (!canReply.value) return

      if (typeof reply.progress !== 'number' || reply.progress < 0 || reply.progress > 100) {
        replyMessageType.value = 'error'
        replyMessage.value = '请填写正确的进度（0~100）'
        return
      }
      if (!reply.content.trim()) {
        replyMessageType.value = 'error'
        replyMessage.value = '请填写修改内容'
        return
      }
      if (!employeeId) return

      const task = selectedTask.value
      task.progress = reply.progress
      task.content = reply.content
      if (reply.attachments.length > 0) {
        task.attachments = [...(task.attachments || []), ...reply.attachments]
      }

      task.updates.push({
        id: `tu_${Date.now()}_${Math.random().toString(16).slice(2)}`,
        updatedById: employeeId,
        updatedAt: new Date().toISOString(),
        replyContent: reply.content,
        progress: reply.progress,
      })

      replyMessageType.value = 'success'
      replyMessage.value = '提交成功：已记录更新与进度'
      reply.attachments = []
    }

    return {
      opportunity,
      tab,

      empName,
      empDept,
      triangleIds,

      canEditResponsibility,
      taskOwnerDraft,
      ownerOptions,
      saveOwners,

      canEditCoreGroup,
      candidateCoreMembers,
      addCoreId,
      addCoreMember,
      removeCoreMember,

      selectedTaskId,
      selectedTask,
      canReply,

      ownerDraft,
      canEditOwner,
      ownerOptionsForTask,
      saveOwner,

      reply,
      onReplyFiles,
      submitReply,
      replyMessage,
      replyMessageType,

      router,
    }
  },
})
</script>

<style scoped lang="scss">
.page-title {
  font-weight: 800;
  font-size: 16px;
  margin-bottom: 12px;
}
.empty {
  padding: 16px;
  color: #64748b;
  background: #fff;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
}
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
}
.summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}
.summary-item {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px;
}
.label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}
.value {
  font-weight: 900;
  font-size: 13px;
}
.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.tab {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}
.tab.active {
  border-color: #7dd3fc;
  background: #f0f9ff;
}
.section-title {
  font-weight: 900;
  margin-bottom: 8px;
}
.section-hint {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
}
.section {
  padding-bottom: 10px;
}
.step-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.step-card {
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 14px;
  padding: 12px;
}
.step-card-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.field label {
  font-size: 12px;
  color: #334155;
}
.field select {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
  background: #fff;
}
.progressline {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}
.bar-wrap {
  flex: 1;
  height: 10px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}
.bar {
  height: 100%;
  background: #38bdf8;
}
.progress-meta {
  width: 54px;
  text-align: right;
  color: #334155;
  font-weight: 800;
  font-size: 12px;
}
.footer-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}
.btn-primary {
  background: #0369a1;
  color: #fff;
  border-color: #0369a1;
}
.btn-danger {
  background: #fff1f2;
  border-color: #fecdd3;
  color: #be123c;
}
.group-block {
  margin-top: 8px;
}
.group-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.group-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
.select {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
}
.member-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.member {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 10px;
  background: #f8fafc;
  width: 240px;
}
.member-name {
  font-weight: 900;
}
.member-meta {
  color: #475569;
  font-size: 13px;
  margin-top: 6px;
  margin-bottom: 10px;
}
.workgroup {
  margin-top: 16px;
}
.work-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.work-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.work {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
}
.work-name {
  font-weight: 900;
  margin-bottom: 6px;
}
.work-meta {
  color: #64748b;
  font-size: 13px;
}
.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.timeline-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
}
.timeline-step {
  font-weight: 900;
  margin-bottom: 6px;
}
.timeline-meta {
  color: #334155;
  font-size: 13px;
  margin-bottom: 10px;
}
.task-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 12px;
}
.task-list {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 14px;
  padding: 12px;
  height: fit-content;
}
.task-list-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.task-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 10px;
  background: #f8fafc;
  margin-bottom: 10px;
}
.task-item.active {
  border-color: #7dd3fc;
  background: #f0f9ff;
}
.task-item-btn {
  width: 100%;
  text-align: left;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}
.task-item-name {
  font-weight: 900;
}
.task-item-meta {
  color: #475569;
  font-size: 13px;
  margin-top: 6px;
}
.task-item-progress {
  margin-top: 10px;
  font-weight: 900;
  color: #0369a1;
}
.task-editor {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #fff;
}
.task-editor-title {
  font-weight: 900;
  font-size: 16px;
  margin-bottom: 6px;
}
.task-editor-sub {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
}
.edit-block {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
  margin-bottom: 12px;
}
.field-inline {
  display: flex;
  gap: 10px;
  align-items: center;
}
.field-inline label {
  font-size: 12px;
  color: #334155;
}
.field-inline select {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
}
.req {
  color: #ef4444;
  font-weight: 700;
}
.edit-block textarea,
.edit-block input {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 10px;
}
.edit-block textarea {
  padding: 10px;
  resize: vertical;
}
.upload {
  margin-top: 10px;
}
.updates {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #fff;
}
.updates-title {
  font-weight: 900;
  margin-bottom: 10px;
}
.muted {
  color: #64748b;
  font-size: 13px;
}
.update {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
  margin-top: 10px;
}
.update-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}
.update-by {
  font-weight: 900;
}
.update-at {
  color: #64748b;
  font-size: 12px;
}
.update-content {
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}
.update-progress {
  margin-top: 8px;
  color: #0369a1;
  font-weight: 900;
  font-size: 13px;
}
.message {
  margin-top: 12px;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #334155;
}
.message.error {
  border-color: #fca5a5;
  background: #fff1f2;
  color: #b91c1c;
}
@media (max-width: 980px) {
  .summary {
    grid-template-columns: 1fr;
  }
  .step-grid {
    grid-template-columns: 1fr;
  }
  .task-layout {
    grid-template-columns: 1fr;
  }
  .work-grid {
    grid-template-columns: 1fr;
  }
}
</style>

