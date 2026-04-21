<template>
  <div class="page">
    <div class="page-title">商机推进模板选择</div>
    <div class="subhint">选择模板后将固化四大核心环节，并自动生成关键任务视图（演示：任务=四大环节）。</div>

    <div v-if="!opportunity" class="empty">未找到商机</div>

    <div v-else class="card">
      <div class="summary">
        <div class="summary-item">
          <span class="label">商机名称</span>
          <span class="value">{{ opportunity.opportunityName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">客户</span>
          <span class="value">{{ customerName(opportunity.customerId) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">当前阶段</span>
          <span class="value">{{ opportunity.stage }}</span>
        </div>
      </div>

      <div class="template-grid">
        <div
          v-for="tpl in templates"
          :key="tpl.key"
          class="tpl"
          :class="{ selected: selectedTemplate === tpl.key }"
          @click="selectedTemplate = tpl.key"
        >
          <div class="tpl-top">
            <div class="tpl-name">{{ tpl.name }}</div>
            <input type="radio" :checked="selectedTemplate === tpl.key" />
          </div>
          <div class="tpl-steps">
            <div v-for="s in tpl.steps" :key="s" class="step">• {{ s }}</div>
          </div>
        </div>
      </div>

      <div class="footer-actions">
        <button class="btn" type="button" @click="goBack">返回列表</button>
        <button class="btn btn-primary" type="button" :disabled="!selectedTemplate" @click="generate">
          生成环节/任务并进入项目详情
        </button>
      </div>

      <div v-if="message" class="message" :class="{ error: messageType === 'error' }">{{ message }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'

type TemplateKey = string

export default defineComponent({
  name: 'OpportunityTemplateSelectView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const opportunityId = String(route.query.opportunityId || '')
    const opportunity = computed(() => store.opportunities.find((o) => o.id === opportunityId))

    const customerName = (customerId?: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'

    const templates = [
      {
        key: 'standard_4_steps',
        name: '标准四环节模板',
        steps: ['验证机会点', '谈判与合同签订', '项目实施', '验收与交付'],
      },
      {
        key: 'accelerate_4_steps',
        name: '加速四环节模板',
        steps: ['验证机会点', '谈判与合同签订', '项目实施', '验收与交付'],
      },
    ]

    const selectedTemplate = ref<TemplateKey | ''>('standard_4_steps')
    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const goBack = () => router.push({ path: '/opportunities/list' })

    const generate = () => {
      if (!opportunity.value) return
      if (!selectedTemplate.value) return

      const steps = templates.find((t) => t.key === selectedTemplate.value)?.steps || []
      if (steps.length !== 4) {
        messageType.value = 'error'
        message.value = '模板步骤数量不符合要求（演示：必须包含四大核心环节）'
        return
      }

      const defaultOwnerId =
        opportunity.value.managerId || store.employees.find((e) => e.roleTag === '项目经理')?.id || store.employees[0]?.id

      opportunity.value.templateKey = selectedTemplate.value
      opportunity.value.stage = '推进中'

      opportunity.value.tasks = steps.map((s, idx) => ({
        id: `t_${Date.now()}_${idx}`,
        taskName: s,
        ownerId: defaultOwnerId || '',
        status: '未关闭',
        progress: 0,
        content: '',
        attachments: [],
        updates: [],
      }))

      messageType.value = 'success'
      message.value = '生成成功：环节与关键任务已创建'
      setTimeout(() => router.push({ path: `/opportunities/${opportunity.value!.id}` }), 500)
    }

    return {
      opportunity,
      customerName,
      templates,
      selectedTemplate,
      goBack,
      generate,
      message,
      messageType,
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
  font-weight: 800;
  font-size: 13px;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.tpl {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
  cursor: pointer;
}
.tpl.selected {
  border-color: #7dd3fc;
  background: #f0f9ff;
}
.tpl-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tpl-name {
  font-weight: 900;
}
.tpl-steps {
  margin-top: 10px;
  color: #334155;
  line-height: 1.7;
  font-size: 13px;
}
.step {
  margin-top: 4px;
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
  .template-grid {
    grid-template-columns: 1fr;
  }
  .summary {
    grid-template-columns: 1fr;
  }
}
</style>

