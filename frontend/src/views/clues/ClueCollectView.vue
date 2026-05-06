<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索收集</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/clues/list')">返回列表</el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon class="hint-alert">
      <template #title>
        将线索从「收集」推进到「分发」 — 仅录入人本人可操作;临时保存可反复修改,确认后状态变更不可撤回
      </template>
    </el-alert>

    <el-empty v-if="!loading && !lead" description="未找到线索" :image-size="100" />

    <el-card v-else-if="lead" class="form-card" shadow="never" v-loading="loading">
      <template #header>
        <div class="card-title">
          <el-icon><Document /></el-icon>
          线索基本信息
          <span class="card-title-meta">— 当前阶段: {{ leadStatusToStage(lead.status) }}</span>
        </div>
      </template>

      <el-alert
        v-if="lead.status !== 'ENTRY'"
        type="warning"
        :closable="false"
        show-icon
        class="lock-banner"
      >
        <template #title>该线索已确认收集,不可再修改基础信息</template>
      </el-alert>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="lead.status !== 'ENTRY'">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户" prop="customerId" required>
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%">
                <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="线索名称" prop="title" required>
              <el-input v-model="form.title" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="线索描述" prop="requirement" required>
          <el-input v-model="form.requirement" type="textarea" :rows="5" />
        </el-form-item>

        <div v-if="lead.status === 'ENTRY'" class="footer-actions">
          <el-button :icon="DocumentCopy" :loading="saving" @click="tempSave">临时保存</el-button>
          <el-button type="primary" :icon="Check" :loading="confirming" @click="confirmCollect">
            确认收集(进入分发)
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { ArrowLeft, Document, DocumentCopy, Check } from '@element-plus/icons-vue'
import { leadApi, LeadDetail } from '@/api/lead'
import { customerApi, Customer } from '@/api/customer'
import { leadStatusToStage } from '@/utils/leadDetail'

export default defineComponent({
  name: 'ClueCollectView',
  components: { Document },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const clueId = Number(route.query.clueId || 0)

    const lead = ref<LeadDetail | null>(null)
    const customers = ref<Customer[]>([])
    const loading = ref(false)
    const saving = ref(false)
    const confirming = ref(false)
    const formRef = ref<FormInstance>()

    const form = reactive({
      customerId: '' as number | '',
      title: '',
      requirement: '',
    })

    const rules = reactive<FormRules>({
      customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
      title: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
      requirement: [{ required: true, message: '请输入线索描述', trigger: 'blur' }],
    })

    const sync = () => {
      if (!lead.value) return
      form.customerId = lead.value.customerId || ''
      form.title = lead.value.title || ''
      form.requirement = lead.value.requirement || ''
    }

    const loadAll = async () => {
      if (!clueId) return
      loading.value = true
      try {
        const [d, cs] = await Promise.all([
          leadApi.detail(clueId),
          customerApi.list({ page: 1, size: 200 }),
        ])
        lead.value = d
        customers.value = cs.records || []
        sync()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载线索失败')
        lead.value = null
      } finally {
        loading.value = false
      }
    }

    const buildReq = () => ({
      customerId: typeof form.customerId === 'number' ? form.customerId : undefined,
      title: form.title || undefined,
      requirement: form.requirement || undefined,
    })

    const tempSave = async () => {
      if (!lead.value || lead.value.status !== 'ENTRY') return
      saving.value = true
      try {
        await leadApi.saveDraft(lead.value.id, buildReq())
        ElMessage.success('临时保存成功')
        await loadAll()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const confirmCollect = async () => {
      if (!lead.value || lead.value.status !== 'ENTRY') return
      if (!formRef.value) return
      try {
        await formRef.value.validate()
      } catch {
        ElMessage.error('请完整填写客户/线索名称/线索描述')
        return
      }
      confirming.value = true
      try {
        await leadApi.collect(lead.value.id, buildReq())
        ElMessage.success('确认收集成功:线索已进入「分发」阶段')
        setTimeout(() => router.push({ path: '/clues/list' }), 500)
      } catch (e: any) {
        ElMessage.error(e?.message || '确认收集失败')
      } finally {
        confirming.value = false
      }
    }

    onMounted(loadAll)

    return {
      lead,
      customers,
      loading,
      saving,
      confirming,
      form,
      formRef,
      rules,
      tempSave,
      confirmCollect,
      leadStatusToStage,
      ArrowLeft, DocumentCopy, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.hint-alert { margin-bottom: 16px; border-radius: 10px; }
.form-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  :deep(.el-card__header) { padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0; }
  :deep(.el-card__body) { padding: 24px; }
}
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-title-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.lock-banner { margin-bottom: 16px; border-radius: 8px; }
.footer-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; padding-top: 20px; border-top: 1px solid #F1F5F9; }
:deep(.el-button--primary) { background-color: #0369A1; border-color: #0369A1; &:hover { background-color: #0284C7; border-color: #0284C7; } }
</style>
