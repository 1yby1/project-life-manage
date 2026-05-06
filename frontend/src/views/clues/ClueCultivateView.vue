<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索培育</h2>
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon class="hint-alert">
      <template #title>
        完善项目信息、人员与客户关键人 — 可多次「保存培育」;字段填全后点「转商机」
      </template>
    </el-alert>

    <el-empty v-if="!loading && !lead" description="未找到线索" :image-size="100" />

    <template v-else-if="lead">
      <el-card class="summary-card" shadow="never">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="线索名称">
            <span class="clue-name">{{ lead.title }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ lead.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段">
            <el-tag :type="leadStageTagType(lead.status)" effect="light" round>
              {{ leadStatusToStage(lead.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-alert
        v-if="lead.status !== 'DISTRIBUTED'"
        type="warning"
        :closable="false"
        show-icon
        class="alert-banner"
      >
        <template #title>当前阶段不可培育(需 DISTRIBUTED 状态)</template>
      </el-alert>

      <el-card class="form-card" shadow="never" v-loading="loading">
        <el-form :model="form" label-position="top" :disabled="lead.status !== 'DISTRIBUTED'">
          <div class="section-title">项目信息</div>
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="项目名称" required>
                <el-input v-model="form.projectName" placeholder="请输入项目名称" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="预计采购时间" required>
                <el-input v-model="form.expectedPurchaseTime" placeholder="例如:2026-04" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="签约概率(0~1)" required>
                <el-input-number v-model="form.winRate" :min="0" :max="1" :step="0.05" :precision="2" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="预测金额(元)" required>
                <el-input-number v-model="form.predictedAmount" :min="0" :step="10000" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="线索等级" required>
                <el-select v-model="form.clueLevel" placeholder="请选择" style="width: 100%">
                  <el-option label="A 级" value="A" />
                  <el-option label="B 级" value="B" />
                  <el-option label="C 级" value="C" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title">人员与关键联系人</div>
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="解决方案经理" required>
                <el-input v-model="form.solutionManager" placeholder="例如:张方案" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="商机负责人(项目经理)" required>
                <el-select v-model="form.opportunityOwnerId" placeholder="请选择负责人" filterable style="width: 100%" :loading="pmLoading">
                  <el-option v-for="p in projectManagers" :key="p.id" :label="p.realName || p.username" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="8">
              <el-form-item label="客户关键人姓名" required>
                <el-input v-model="form.keyContact.name" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="8">
              <el-form-item label="客户关键人职位" required>
                <el-input v-model="form.keyContact.title" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="8">
              <el-form-item label="客户关键人联系方式" required>
                <el-input v-model="form.keyContact.phone" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <div v-if="lead.status === 'DISTRIBUTED'" class="footer-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button :icon="Check" :loading="saving" @click="saveCultivate">保存培育</el-button>
            <el-button type="primary" :icon="MagicStick" :loading="converting" @click="convert">
              完善后转商机
            </el-button>
          </div>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, MagicStick, Check } from '@element-plus/icons-vue'
import { leadApi, LeadDetail } from '@/api/lead'
import { userApi, AdminUser } from '@/api/user'
import {
  parseCultivateInfo, serializeCultivateForm, EMPTY_CULTIVATE_FORM, CultivateForm,
  leadStatusToStage, leadStageTagType,
} from '@/utils/leadDetail'

export default defineComponent({
  name: 'ClueCultivateView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const clueId = Number(route.query.clueId || 0)

    const lead = ref<LeadDetail | null>(null)
    const loading = ref(false)
    const projectManagers = ref<AdminUser[]>([])
    const pmLoading = ref(false)
    const saving = ref(false)
    const converting = ref(false)

    const form = reactive<CultivateForm>({
      ...EMPTY_CULTIVATE_FORM,
      keyContact: { ...EMPTY_CULTIVATE_FORM.keyContact },
    })

    const sync = () => {
      const parsed = parseCultivateInfo(lead.value)
      Object.assign(form, parsed)
      form.keyContact = { ...parsed.keyContact }
    }

    const loadAll = async () => {
      if (!clueId) return
      loading.value = true
      pmLoading.value = true
      try {
        const [d, pms] = await Promise.all([
          leadApi.detail(clueId),
          userApi.listProjectManagers(),
        ])
        lead.value = d
        projectManagers.value = pms || []
        sync()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        lead.value = null
      } finally {
        loading.value = false
        pmLoading.value = false
      }
    }

    const goBack = () => router.push({ path: '/clues/list' })

    const validate = (): string => {
      if (!form.projectName) return '请填写项目名称'
      if (!form.expectedPurchaseTime) return '请填写预计采购时间'
      if (!(form.winRate >= 0 && form.winRate <= 1)) return '签约概率必须在 0~1 之间'
      if (!form.predictedAmount || form.predictedAmount <= 0) return '请填写预测金额'
      if (!form.clueLevel) return '请选择线索等级'
      if (!form.solutionManager) return '请填写解决方案经理'
      if (typeof form.opportunityOwnerId !== 'number') return '请选择商机负责人'
      if (!form.keyContact.name || !form.keyContact.title || !form.keyContact.phone) return '请填写客户关键人完整信息'
      return ''
    }

    const saveCultivate = async () => {
      if (!lead.value || lead.value.status !== 'DISTRIBUTED') return
      saving.value = true
      try {
        await leadApi.cultivate(lead.value.id, serializeCultivateForm(form))
        ElMessage.success('培育详情已保存')
        await loadAll()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    const convert = async () => {
      if (!lead.value || lead.value.status !== 'DISTRIBUTED') return
      const err = validate()
      if (err) {
        ElMessage.error(err)
        return
      }
      try {
        await ElMessageBox.confirm('转商机后线索将进入 CONVERTED 终态,且自动创建商机占位。确认转商机?', '确认转商机', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        })
      } catch {
        return
      }
      converting.value = true
      try {
        // 先保存最新培育详情(后端 convert 会读 progress_desc)
        await leadApi.cultivate(lead.value.id, serializeCultivateForm(form))
        const resp = await leadApi.convert(lead.value.id)
        ElMessage.success(`转商机成功:${resp.opportunityName}`)
        setTimeout(() => router.push({ path: '/opportunities/list' }), 600)
      } catch (e: any) {
        ElMessage.error(e?.message || '转商机失败')
      } finally {
        converting.value = false
      }
    }

    onMounted(loadAll)

    return {
      lead,
      loading,
      projectManagers,
      pmLoading,
      saving,
      converting,
      form,
      saveCultivate,
      convert,
      goBack,
      leadStatusToStage,
      leadStageTagType,
      ArrowLeft, MagicStick, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.hint-alert { margin-bottom: 16px; border-radius: 10px; }
.summary-card, .form-card { border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px; }
.summary-card :deep(.el-card__body) { padding: 16px 20px; }
.form-card :deep(.el-card__body) { padding: 24px; }
.alert-banner { margin-bottom: 16px; border-radius: 10px; }
.clue-name { font-weight: 600; color: #0F172A; }
:deep(.el-descriptions__label) { width: 120px; background: #F8FAFC !important; color: #475569 !important; font-weight: 500 !important; }
.section-title {
  font-weight: 600; font-size: 14px; color: #0F172A;
  margin: 8px 0 16px; padding-left: 8px; border-left: 3px solid #0369A1;
}
.footer-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; padding-top: 20px; border-top: 1px solid #F1F5F9; }
:deep(.el-button--primary) { background-color: #0369A1; border-color: #0369A1; &:hover { background-color: #0284C7; border-color: #0284C7; } }
</style>
