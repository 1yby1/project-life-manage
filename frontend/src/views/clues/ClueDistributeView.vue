<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索分发</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/clues/list')">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !lead" description="未找到线索" :image-size="100" />

    <template v-else-if="lead">
      <el-card class="summary-card" shadow="never" v-loading="loading">
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
        v-if="lead.status !== 'COLLECTED'"
        type="warning"
        :closable="false"
        show-icon
        class="alert-banner"
      >
        <template #title>当前阶段不可分发(仅 COLLECTED 状态可指派)</template>
      </el-alert>

      <template v-if="lead.status === 'COLLECTED'">
        <el-card class="filter-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Search /></el-icon>
              筛选客户经理
            </div>
          </template>
          <el-form :inline="true" :model="query" class="filter-form">
            <el-form-item label="姓名">
              <el-input v-model="query.name" placeholder="输入姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="query.phone" placeholder="输入手机号" clearable />
            </el-form-item>
            <el-form-item>
              <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="manager-card" shadow="never" v-loading="managersLoading">
          <template #header>
            <div class="card-title">
              <el-icon><User /></el-icon>
              可指派的客户经理
              <el-tag size="small" effect="plain" round>{{ filteredManagers.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="filteredManagers.length === 0" description="暂无匹配的客户经理" :image-size="80" />
          <div v-else class="manager-grid">
            <div v-for="m in filteredManagers" :key="m.id" class="manager-item">
              <div class="manager-info">
                <div class="manager-name">{{ m.realName || m.username }}</div>
                <div class="manager-meta">
                  <span class="phone">{{ m.phone || '—' }}</span>
                  <span v-if="m.email" class="meta-sep">·</span>
                  <span v-if="m.email">{{ m.email }}</span>
                </div>
              </div>
              <el-button type="primary" :icon="Position" size="small" :loading="assigning === m.id" @click="assign(m)">
                指派为培育负责人
              </el-button>
            </div>
          </div>
        </el-card>
      </template>
    </template>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, RefreshLeft, User, Position } from '@element-plus/icons-vue'
import { leadApi, LeadDetail } from '@/api/lead'
import { userApi, AdminUser } from '@/api/user'
import { leadStatusToStage, leadStageTagType } from '@/utils/leadDetail'

export default defineComponent({
  name: 'ClueDistributeView',
  components: { Search, User },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const clueId = Number(route.query.clueId || 0)

    const lead = ref<LeadDetail | null>(null)
    const loading = ref(false)
    const managers = ref<AdminUser[]>([])
    const managersLoading = ref(false)

    const query = reactive({ name: '', phone: '' })
    const assigning = ref<number | null>(null)

    const filteredManagers = computed(() =>
      managers.value.filter((m) => {
        const name = m.realName || m.username || ''
        const okName = query.name ? name.includes(query.name) : true
        const okPhone = query.phone ? (m.phone || '').includes(query.phone) : true
        return okName && okPhone
      }),
    )

    const resetQuery = () => {
      query.name = ''
      query.phone = ''
    }

    const loadAll = async () => {
      if (!clueId) return
      loading.value = true
      managersLoading.value = true
      try {
        const [d, ms] = await Promise.all([
          leadApi.detail(clueId),
          userApi.listCustomerManagers(),
        ])
        lead.value = d
        managers.value = ms || []
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        lead.value = null
        managers.value = []
      } finally {
        loading.value = false
        managersLoading.value = false
      }
    }

    const assign = async (m: AdminUser) => {
      if (!lead.value) return
      assigning.value = m.id
      try {
        await leadApi.distribute(lead.value.id, m.id)
        ElMessage.success(`指派成功:${m.realName || m.username},线索已进入「培育」阶段`)
        setTimeout(() => router.push({ path: '/clues/list' }), 450)
      } catch (e: any) {
        ElMessage.error(e?.message || '指派失败')
      } finally {
        assigning.value = null
      }
    }

    onMounted(loadAll)

    return {
      lead,
      loading,
      managers,
      managersLoading,
      filteredManagers,
      query,
      assigning,
      resetQuery,
      assign,
      leadStatusToStage,
      leadStageTagType,
      ArrowLeft, RefreshLeft, Position,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.summary-card, .filter-card, .manager-card { border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px; }
.summary-card :deep(.el-card__body) { padding: 16px 20px; }
.filter-card :deep(.el-card__header), .manager-card :deep(.el-card__header) {
  padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
}
.filter-card :deep(.el-card__body), .manager-card :deep(.el-card__body) { padding: 16px 20px; }
.alert-banner { margin-bottom: 16px; border-radius: 10px; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.clue-name { font-weight: 600; color: #0F172A; }
:deep(.el-descriptions__label) { width: 120px; background: #F8FAFC !important; color: #475569 !important; font-weight: 500 !important; }
.filter-form {
  :deep(.el-form-item) { margin-bottom: 0; margin-right: 16px; }
  :deep(.el-input) { width: 200px; }
}
.manager-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 12px; }
.manager-item {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 14px 16px;
  transition: all 0.15s;
  &:hover { border-color: #0369A1; background: #F0F9FF; }
}
.manager-info { flex: 1; min-width: 0; }
.manager-name { font-weight: 600; font-size: 14px; color: #0F172A; margin-bottom: 4px; }
.manager-meta { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748B; }
.meta-sep { color: #CBD5E1; }
.phone { font-family: 'SF Mono', Menlo, Consolas, monospace; }
:deep(.el-button--primary) { background-color: #0369A1; border-color: #0369A1; &:hover { background-color: #0284C7; border-color: #0284C7; } }
</style>
