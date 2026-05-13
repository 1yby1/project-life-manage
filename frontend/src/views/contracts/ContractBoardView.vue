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
              class="contract-item contract-item--completed"
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
        const res = await contractApi.list({
          page: 1,
          size: 200,
          year: selectedYear.value === 0 ? undefined : selectedYear.value,
        })
        allContracts.value = res.records || []
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
.page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.page-title {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: var(--space-3);
}

.filter-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);

  :deep(.el-card__body) {
    padding: var(--space-3) var(--space-6);
  }
}

.year-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.year-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.board-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);

  :deep(.el-card__header) {
    padding: var(--space-3) var(--space-6);
    background: var(--color-bg-soft);
    border-bottom: 1px solid var(--color-border);
  }

  :deep(.el-card__body) {
    padding: var(--space-3) var(--space-6);
    min-height: 280px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.contract-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* 内项卡:仅 border,不叠加 shadow,hover 仅改边框色 */
.contract-item {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  background: var(--color-bg);
  transition: border-color var(--duration-fast) var(--easing),
              background var(--duration-fast) var(--easing);

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-bg-soft);
  }

  &--completed {
    opacity: 0.85;
  }
}

.contract-name {
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.contract-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-2);
}

.meta-sep {
  color: var(--color-border-strong);
}

.amount {
  font-family: var(--font-mono);
  font-weight: var(--weight-semibold);
  color: var(--color-primary);
}

.paid-meta {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  margin-top: var(--space-1);
}
</style>
