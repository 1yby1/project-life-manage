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
            {{ contract.fileUrl }}
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
.page { max-width: 1200px; margin: 0 auto; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.summary-card, .payment-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
}
.summary-card :deep(.el-card__body) { padding: 16px 20px; }
.payment-card :deep(.el-card__header) {
  padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
}
.payment-card :deep(.el-card__body) { padding: 0; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.contract-name { font-weight: 600; color: #0F172A; }
.amount { font-family: 'SF Mono', Menlo, Consolas, monospace; font-weight: 600; }
.text-success { color: #059669; }
.muted { color: #94A3B8; }
.file-block {
  margin-top: 16px; padding: 12px 16px; background: #F0F9FF;
  border: 1px solid #BAE6FD; border-radius: 8px;
}
.file-label {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #0369A1; margin-bottom: 6px; font-weight: 500;
}
:deep(.el-descriptions__label) {
  width: 110px; background: #F8FAFC !important;
  color: #475569 !important; font-weight: 500 !important;
}
:deep(.el-table th.el-table__cell) {
  background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
}
</style>
