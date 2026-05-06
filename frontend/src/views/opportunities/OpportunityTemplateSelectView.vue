<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商机推进模板选择</h2>
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon class="hint-alert">
      <template #title>
        选择模板后将固化四大核心环节,并自动生成关键任务视图(必含:验证机会点 / 谈判与合同签订 / 项目实施 / 验收与交付)
      </template>
    </el-alert>

    <el-empty v-if="!loading && !opportunity" description="未找到商机" :image-size="100" />

    <template v-else-if="opportunity">
      <el-card class="summary-card" shadow="never">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="商机名称">
            <span class="opp-name">{{ opportunity.oppName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ opportunity.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="项目经理">{{ opportunity.pmName || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-alert
        v-if="opportunity.templateId"
        type="warning"
        :closable="false"
        show-icon
        class="alert-banner"
      >
        <template #title>该商机已应用模板「{{ opportunity.templateName }}」 — 重新选择会清空已生成的环节(任务存在则禁止)</template>
      </el-alert>

      <el-card class="template-card" shadow="never" v-loading="loadingTemplates">
        <template #header>
          <div class="card-title">
            <el-icon><Setting /></el-icon>
            选择推进模板
            <span class="card-meta">— 共 {{ templates.length }} 个</span>
          </div>
        </template>

        <el-empty v-if="!loadingTemplates && templates.length === 0" description="暂无可用模板" :image-size="80" />

        <el-row :gutter="16">
          <el-col v-for="tpl in templates" :key="tpl.id" :xs="24" :md="12">
            <div class="tpl" :class="{ selected: selectedTemplate === tpl.id }" @click="selectedTemplate = tpl.id">
              <div class="tpl-top">
                <el-radio v-model="selectedTemplate" :value="tpl.id">
                  {{ tpl.templateName }}
                  <el-tag v-if="tpl.isDefault" type="success" size="small" effect="plain" round>默认</el-tag>
                </el-radio>
              </div>
              <div v-if="tpl.description" class="tpl-desc">{{ tpl.description }}</div>
              <div class="tpl-steps">
                <div v-for="(s, idx) in tpl.stages" :key="s.id" class="step">
                  <span class="step-num">{{ idx + 1 }}</span>
                  <span class="step-name">{{ s.stageName }}</span>
                  <el-tag v-if="s.required" type="info" size="small" effect="plain">必含</el-tag>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="footer-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button
            type="primary"
            :icon="Check"
            :disabled="!selectedTemplate"
            :loading="applying"
            @click="apply"
          >
            生成环节并进入项目详情
          </el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Setting, Check } from '@element-plus/icons-vue'
import { opportunityApi, OpportunityDetail } from '@/api/opportunity'
import { oppTemplateApi, OppTemplate } from '@/api/oppTemplate'

export default defineComponent({
  name: 'OpportunityTemplateSelectView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const oppId = Number(route.query.opportunityId || 0)

    const opportunity = ref<OpportunityDetail | null>(null)
    const templates = ref<OppTemplate[]>([])
    const selectedTemplate = ref<number | ''>('')
    const loading = ref(false)
    const loadingTemplates = ref(false)
    const applying = ref(false)

    const loadAll = async () => {
      if (!oppId) return
      loading.value = true
      loadingTemplates.value = true
      try {
        const [opp, tpls] = await Promise.all([
          opportunityApi.detail(oppId),
          oppTemplateApi.list(),
        ])
        opportunity.value = opp
        templates.value = tpls || []
        // 默认选中: 已选模板 / 默认模板 / 第一个
        if (opp.templateId) {
          selectedTemplate.value = opp.templateId
        } else {
          const def = templates.value.find((t) => t.isDefault) || templates.value[0]
          selectedTemplate.value = def ? def.id : ''
        }
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        opportunity.value = null
      } finally {
        loading.value = false
        loadingTemplates.value = false
      }
    }

    const goBack = () => router.push({ path: '/opportunities/list' })

    const apply = async () => {
      if (!opportunity.value || typeof selectedTemplate.value !== 'number') return
      applying.value = true
      try {
        await opportunityApi.applyTemplate(opportunity.value.id, selectedTemplate.value)
        ElMessage.success('模板应用成功,环节已生成')
        setTimeout(() => router.push({ path: '/opportunities/detail', query: { opportunityId: opportunity.value!.id } }), 500)
      } catch (e: any) {
        ElMessage.error(e?.message || '应用模板失败')
      } finally {
        applying.value = false
      }
    }

    onMounted(loadAll)

    return {
      opportunity, templates, selectedTemplate, loading, loadingTemplates, applying,
      apply, goBack,
      ArrowLeft, Setting, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.hint-alert, .alert-banner { margin-bottom: 16px; border-radius: 10px; }
.summary-card, .template-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
}
.summary-card :deep(.el-card__body) { padding: 16px 20px; }
.template-card :deep(.el-card__header) {
  padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
}
.template-card :deep(.el-card__body) { padding: 20px; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.opp-name { font-weight: 600; color: #0F172A; }

.tpl {
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px;
  margin-bottom: 12px; cursor: pointer; transition: all 0.15s;
  &:hover { border-color: #0369A1; }
  &.selected { border-color: #0369A1; background: #F0F9FF; }
}
.tpl-top { margin-bottom: 8px; }
.tpl-desc { font-size: 12px; color: #64748B; margin-bottom: 12px; }
.tpl-steps { display: flex; flex-direction: column; gap: 6px; }
.step {
  display: flex; align-items: center; gap: 8px; font-size: 13px; color: #475569;
}
.step-num {
  width: 22px; height: 22px; border-radius: 50%;
  background: #0369A1; color: white; font-size: 12px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
}
.step-name { flex: 1; }

.footer-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 20px; padding-top: 20px; border-top: 1px solid #F1F5F9;
}
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
