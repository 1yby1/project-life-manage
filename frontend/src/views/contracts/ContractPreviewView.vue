<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">合同预览</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/contracts/list')">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !contract" description="未找到合同" :image-size="100" />

    <template v-else-if="contract">
      <el-card class="summary-card" shadow="never">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="合同名称">
            <span class="contract-name">{{ contract.contractName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ contract.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="contractStatusTagType(contract.status)" effect="light" round>
              {{ contractStatusLabel(contract.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="合同类型">{{ contract.contractType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同年份">{{ contract.contractYear }}</el-descriptions-item>
          <el-descriptions-item label="来源商机">{{ contract.oppName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同金额">
            <span class="amount">{{ formatMoney(contract.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已收款">
            <span class="amount text-success">{{ formatMoney(contract.paidAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收款率">
            <el-progress :percentage="paidRatio" :stroke-width="8" />
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ contract.creatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ contract.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="contract.deliveryTime" label="验收时间">{{ contract.deliveryTime }}</el-descriptions-item>
          <el-descriptions-item v-if="contract.closeTime" label="关闭信息" :span="3">
            {{ contract.closerName || '-' }} 于 {{ contract.closeTime }} 关闭
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="contract.fileUrl" class="file-block">
          <div class="file-label">
            <el-icon><Link /></el-icon>
            合同正文附件
          </div>
          <el-link :href="contract.fileUrl" target="_blank" type="primary">
            下载附件
          </el-link>
        </div>
      </el-card>

      <el-card class="payment-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><Money /></el-icon>
            付款节点
            <span class="card-meta">— 共 {{ contract.payments?.length || 0 }} 个</span>
          </div>
        </template>

        <el-table :data="contract.payments || []" stripe empty-text="暂无付款节点">
          <el-table-column label="节点" prop="nodeName" min-width="160" />
          <el-table-column label="计划金额" align="right" min-width="140">
            <template #default="{ row }">{{ formatMoney(row.planAmount) }}</template>
          </el-table-column>
          <el-table-column label="计划日期" prop="planDate" width="130">
            <template #default="{ row }">{{ row.planDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="实付金额" align="right" min-width="140">
            <template #default="{ row }">
              <span v-if="row.status === 1" class="text-success">{{ formatMoney(row.actualAmount) }}</span>
              <span v-else class="muted">未付</span>
            </template>
          </el-table-column>
          <el-table-column label="付款时间" width="170">
            <template #default="{ row }">{{ row.payTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '已付' : '未付' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Link, Money } from '@element-plus/icons-vue'
import {
  contractApi, ContractDetail,
  contractStatusLabel, contractStatusTagType, formatMoney,
} from '@/api/contract'

export default defineComponent({
  name: 'ContractPreviewView',
  setup() {
    const route = useRoute()
    const contractId = Number(route.query.contractId || 0)

    const contract = ref<ContractDetail | null>(null)
    const loading = ref(false)

    const reload = async () => {
      if (!contractId) return
      loading.value = true
      try {
        contract.value = await contractApi.detail(contractId)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        contract.value = null
      } finally {
        loading.value = false
      }
    }

    const paidRatio = computed(() => {
      if (!contract.value || !contract.value.totalAmount) return 0
      const total = Number(contract.value.totalAmount)
      const paid = Number(contract.value.paidAmount || 0)
      return total > 0 ? Math.min(100, Math.round((paid / total) * 100)) : 0
    })

    onMounted(reload)

    return {
      contract, loading, paidRatio,
      contractStatusLabel, contractStatusTagType, formatMoney,
      ArrowLeft, Link, Money,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  max-width: 1200px;
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

.summary-card,
.payment-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);
}

.summary-card :deep(.el-card__body) {
  padding: var(--space-4) var(--space-6);
}

.payment-card :deep(.el-card__header) {
  padding: var(--space-3) var(--space-6);
  background: var(--color-bg-soft);
  border-bottom: 1px solid var(--color-border);
}

.payment-card :deep(.el-card__body) {
  padding: 0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.card-meta {
  color: var(--color-text-tertiary);
  font-weight: var(--weight-normal);
  font-size: var(--text-sm);
}

.contract-name {
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
}

.amount {
  font-family: var(--font-mono);
  font-weight: var(--weight-semibold);
}

.text-success {
  color: var(--color-success);
}

.muted {
  color: var(--color-text-disabled);
}

.file-block {
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--color-primary-bg);
  border: 1px solid var(--color-primary-border);
  border-radius: var(--radius-md);
}

.file-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
  font-weight: var(--weight-medium);
}

:deep(.el-descriptions__label) {
  width: 110px;
  background: var(--color-bg-soft) !important;
  color: var(--color-text-secondary) !important;
  font-weight: var(--weight-medium) !important;
}

:deep(.el-table th.el-table__cell) {
  background: var(--color-bg-soft) !important;
  color: var(--color-text-primary);
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
}
</style>
