<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">合同首页</h2>
      <div class="header-actions">
        <el-button v-if="canCreate" type="primary" :icon="Plus" @click="$router.push('/contracts/create')">
          创建合同
        </el-button>
        <el-button :icon="List" @click="$router.push('/contracts/list')">查看完整列表</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <div class="year-row">
        <span class="year-label">年份筛选:</span>
        <el-radio-group v-model="selectedYear" @change="reload">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button v-for="y in yearOptions" :key="y" :value="y">{{ y }}</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="board-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">
              <el-icon><Loading /></el-icon>
              执行中
              <el-tag type="warning" size="small" effect="plain" round>{{ executing.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="executing.length === 0" description="暂无执行中合同" :image-size="80" />
          <div v-else class="contract-list">
            <div
              v-for="c in executing"
              :key="c.id"
              class="contract-item"
              @click="goPreview(c.id)"
            >
              <div class="contract-name">{{ c.contractName }}</div>
              <div class="contract-meta">
                <span>{{ c.customerName || '-' }}</span>
                <span class="meta-sep">·</span>
                <span>{{ c.contractYear }} 年</span>
                <span class="meta-sep">·</span>
                <span class="amount">{{ formatMoney(c.totalAmount) }}</span>
              </div>
              <el-progress
                :percentage="paidRatio(c)"
                :stroke-width="6"
                :show-text="false"
              />
              <div class="paid-meta">已收款: {{ formatMoney(c.paidAmount) }} / {{ formatMoney(c.totalAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="board-card" shadow="never" v-loading="loading">
          <template #header>
            <div class="card-title">
              <el-icon><Check /></el-icon>
              已完成
              <el-tag type="success" size="small" effect="plain" round>{{ completed.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="completed.length === 0" description="暂无已完成合同" :image-size="80" />
          <div v-else class="contract-list">
            <div
              v-for="c in completed"
              :key="c.id"
              class="contract-item completed"
              @click="goPreview(c.id)"
            >
              <div class="contract-name">{{ c.contractName }}</div>
              <div class="contract-meta">
                <span>{{ c.customerName || '-' }}</span>
                <span class="meta-sep">·</span>
                <span>{{ c.contractYear }} 年</span>
                <span class="meta-sep">·</span>
                <span class="amount">{{ formatMoney(c.totalAmount) }}</span>
                <span class="meta-sep">·</span>
                <el-tag :type="contractStatusTagType(c.status)" size="small" effect="light">
                  {{ contractStatusLabel(c.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, List, Loading, Check } from '@element-plus/icons-vue'
import {
  contractApi, Contract,
  contractStatusLabel, contractStatusTagType, formatMoney,
} from '@/api/contract'
import { getAuthState } from '@/auth/authStore'

export default defineComponent({
  name: 'ContractBoardView',
  setup() {
    const router = useRouter()
    const auth = getAuthState()
    const canCreate = computed(() => !!auth.user?.roles?.includes('OPP_ADMIN'))

    const allContracts = ref<Contract[]>([])
    const loading = ref(false)
    const selectedYear = ref(0)  // 0 = 全部

    const currentYear = new Date().getFullYear()
    const yearOptions = [currentYear, currentYear - 1, 2019]

    const reload = async () => {
      loading.value = true
      try {
        allContracts.value = await contractApi.list({
          year: selectedYear.value === 0 ? undefined : selectedYear.value,
        })
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        allContracts.value = []
      } finally {
        loading.value = false
      }
    }

    const executing = computed(() => allContracts.value.filter((c) => c.status === 'EXECUTING'))
    const completed = computed(() => allContracts.value.filter((c) => c.status === 'COMPLETED' || c.status === 'CLOSED'))

    const paidRatio = (c: Contract) => {
      const total = Number(c.totalAmount || 0)
      if (total <= 0) return 0
      return Math.min(100, Math.round((Number(c.paidAmount || 0) / total) * 100))
    }

    const goPreview = (id: number) => router.push({ path: '/contracts/preview', query: { contractId: id } })

    onMounted(reload)

    return {
      allContracts, loading, selectedYear, yearOptions, canCreate,
      executing, completed,
      reload, paidRatio, goPreview,
      contractStatusLabel, contractStatusTagType, formatMoney,
      Plus, List, Loading, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.header-actions { display: flex; gap: 12px; }

.filter-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__body) { padding: 12px 20px; }
}
.year-row { display: flex; align-items: center; gap: 12px; }
.year-label { font-size: 13px; color: #475569; }

.board-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__header) {
    padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
  }
  :deep(.el-card__body) { padding: 12px 20px; min-height: 280px; }
}
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }

.contract-list { display: flex; flex-direction: column; gap: 12px; }
.contract-item {
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px 16px;
  cursor: pointer; transition: all 0.15s; background: #FFFFFF;
  &:hover { border-color: #0369A1; box-shadow: 0 1px 4px rgba(3, 105, 161, 0.1); }
  &.completed { opacity: 0.85; }
}
.contract-name { font-weight: 600; font-size: 14px; color: #0F172A; margin-bottom: 6px; }
.contract-meta {
  display: flex; align-items: center; flex-wrap: wrap; gap: 6px;
  font-size: 12px; color: #64748B; margin-bottom: 8px;
}
.meta-sep { color: #CBD5E1; }
.amount { font-family: 'SF Mono', Menlo, Consolas, monospace; font-weight: 600; color: #0369A1; }
.paid-meta { font-size: 12px; color: #475569; margin-top: 4px; }
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
