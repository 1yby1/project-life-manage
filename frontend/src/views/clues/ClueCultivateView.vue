<template>
  <div class="page">
    <div class="page-title">线索培育（转商机）</div>
    <div class="subhint">完善项目、人员、客户关键人全量信息（演示：必填校验通过后生成商机并进入“模板选择”）。</div>

    <div v-if="!clue" class="empty">未找到线索</div>

    <div v-else class="card">
      <div class="summary">
        <div class="summary-item">
          <span class="label">线索名称</span>
          <span class="value">{{ clue.clueName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">客户</span>
          <span class="value">{{ clue.customerName || '-' }}</span>
        </div>
        <div class="summary-item">
          <span class="label">当前阶段</span>
          <span class="value">{{ clue.stage }}</span>
        </div>
      </div>

      <h3 class="section-title">1. 项目信息（必填）</h3>
      <div class="grid">
        <div class="field">
          <label>项目名称 <span class="req">*</span></label>
          <input v-model="form.projectName" />
        </div>
        <div class="field">
          <label>预计采购时间 <span class="req">*</span></label>
          <input v-model="form.expectedPurchaseTime" placeholder="例如：2026-04" />
        </div>
        <div class="field">
          <label>签约概率（0~1） <span class="req">*</span></label>
          <input v-model.number="form.signingProbability" type="number" step="0.01" />
        </div>
        <div class="field">
          <label>预测金额 <span class="req">*</span></label>
          <input v-model.number="form.predictedAmount" type="number" />
        </div>
        <div class="field">
          <label>线索等级 <span class="req">*</span></label>
          <select v-model="form.clueLevel">
            <option value="">请选择</option>
            <option value="A">A</option>
            <option value="B">B</option>
            <option value="C">C</option>
          </select>
        </div>
      </div>

      <h3 class="section-title">2. 人员与关键联系人（必填）</h3>
      <div class="grid">
        <div class="field">
          <label>解决方案经理 <span class="req">*</span></label>
          <input v-model="form.solutionManager" placeholder="例如：张方案" />
        </div>
        <div class="field">
          <label>商机负责人（项目经理） <span class="req">*</span></label>
          <select v-model="form.opportunityOwnerId">
            <option value="">请选择负责人</option>
            <option v-for="e in projectManagers" :key="e.id" :value="e.id">{{ e.name }}（{{ e.dept }}）</option>
          </select>
        </div>
        <div class="field">
          <label>客户关键人姓名 <span class="req">*</span></label>
          <input v-model="form.keyContact.name" />
        </div>
        <div class="field">
          <label>客户关键人职位 <span class="req">*</span></label>
          <input v-model="form.keyContact.title" />
        </div>
        <div class="field">
          <label>客户关键人联系方式 <span class="req">*</span></label>
          <input v-model="form.keyContact.phone" />
        </div>
      </div>

      <div class="footer-actions">
        <button class="btn" type="button" @click="goBack">返回线索清单</button>
        <button class="btn btn-primary" type="button" @click="toTemplateSelect">完善后转商机</button>
      </div>

      <div v-if="message" class="message" :class="{ error: messageType === 'error' }">{{ message }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEmployeeIdByRoleLabel, useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'ClueCultivateView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const clueId = String(route.query.clueId || '')
    const clue = computed(() => store.clues.find((l) => l.id === clueId))

    const roleLabel = window.localStorage.getItem('demo_role') || '客户经理'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel)

    const projectManagers = computed(() => store.employees.filter((e) => e.roleTag === '项目经理'))

    const form = reactive({
      projectName: '',
      expectedPurchaseTime: '',
      signingProbability: 0.5,
      predictedAmount: 0,
      clueLevel: '',
      solutionManager: '',
      opportunityOwnerId: '',
      keyContact: {
        name: '',
        title: '',
        phone: '',
      },
    })

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const sync = () => {
      if (!clue.value) return
      form.projectName = clue.value.projectName || ''
      form.expectedPurchaseTime = clue.value.expectedPurchaseTime || ''
      form.signingProbability = clue.value.signingProbability ?? 0.5
      form.predictedAmount = clue.value.predictedAmount ?? 0
      form.clueLevel = clue.value.clueLevel || ''
      form.solutionManager = clue.value.solutionManager || ''
      form.opportunityOwnerId = clue.value.opportunityOwnerId || ''
      form.keyContact.name = clue.value.keyContact?.name || ''
      form.keyContact.title = clue.value.keyContact?.title || ''
      form.keyContact.phone = clue.value.keyContact?.phone || ''
    }
    sync()

    const goBack = () => router.push({ path: '/clues/list' })

    const validate = () => {
      if (!form.projectName) return '请填写项目名称'
      if (!form.expectedPurchaseTime) return '请填写预计采购时间'
      if (!(form.signingProbability >= 0 && form.signingProbability <= 1)) return '签约概率必须在 0~1 之间'
      if (!form.predictedAmount || form.predictedAmount <= 0) return '请填写预测金额'
      if (!form.clueLevel) return '请选择线索等级'
      if (!form.solutionManager) return '请填写解决方案经理'
      if (!form.opportunityOwnerId) return '请选择商机负责人'
      if (!form.keyContact.name || !form.keyContact.title || !form.keyContact.phone) return '请填写客户关键人完整信息'
      return ''
    }

    const toTemplateSelect = () => {
      if (!clue.value) return
      const err = validate()
      if (err) {
        messageType.value = 'error'
        message.value = err
        return
      }

      // 演示：把培育信息写回线索
      clue.value.projectName = form.projectName
      clue.value.expectedPurchaseTime = form.expectedPurchaseTime
      clue.value.signingProbability = form.signingProbability
      clue.value.predictedAmount = form.predictedAmount
      clue.value.clueLevel = form.clueLevel
      clue.value.solutionManager = form.solutionManager
      clue.value.opportunityOwnerId = form.opportunityOwnerId
      clue.value.keyContact = { ...form.keyContact }

      // 生成商机（阶段=模板选择）
      const opportunityId = `o_${Date.now()}`
      store.opportunities.push({
        id: opportunityId,
        clueId: clue.value.id,
        customerId: clue.value.customerId,
        opportunityName: `${clue.value.customerName || '客户'}-${clue.value.clueName}-商机`,
        stage: '模板选择',
        templateKey: undefined,
        managerId: form.opportunityOwnerId,
        triangleIds: employeeId ? [employeeId, form.opportunityOwnerId] : [form.opportunityOwnerId],
        coreGroupIds: [form.opportunityOwnerId],
        tasks: [],
      })

      messageType.value = 'success'
      message.value = '转商机成功：进入模板选择'
      setTimeout(() => router.push({ path: '/opportunities/template', query: { opportunityId } }), 500)
    }

    return {
      clue,
      projectManagers,
      form,
      message,
      messageType,
      toTemplateSelect,
      goBack,
    }
  },
})
</script>

<style scoped lang="scss">
.page-title {
  font-weight: 800;
  font-size: 16px;
  margin-bottom: 6px;
}
.subhint {
  color: #64748b;
  font-size: 13px;
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
  border: 1px solid #e5e7eb;
  background: #f8fafc;
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
  font-weight: 800;
  font-size: 13px;
}
.section-title {
  margin: 14px 0 10px;
  font-weight: 900;
}
.req {
  color: #ef4444;
  font-weight: 700;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 12px;
  color: #334155;
}
.field input,
.field select {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
}
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
.message {
  margin-top: 12px;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #334155;
  font-size: 13px;
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
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

