<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">合同列表</h2>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="$router.push('/contracts/create')">
        创建合同
      </el-button>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="合同名称">
          <el-input v-model="query.keyword" placeholder="输入合同名称" clearable :prefix-icon="Search" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="query.customerName" placeholder="输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="执行中" value="EXECUTING" />
            <el-option label="已交付" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-select v-model="query.year" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="reload">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe v-loading="loading" empty-text="暂无合同">
        <el-table-column label="合同名称" min-width="200">
          <template #default="{ row }">
            <span class="contract-name">{{ row.contractName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="类型" prop="contractType" width="120" />
        <el-table-column label="金额" align="right" width="140">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="已收款" align="right" width="140">
          <template #default="{ row }">{{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="contractStatusTagType(row.status)" effect="light" round>
              {{ contractStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="年份" prop="contractYear" width="80" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="goPreview(row.id)">预览</el-button>
            <el-divider direction="vertical" />
            <el-button link type="primary" :icon="Money" @click="openPaymentDialog(row)">付款节点</el-button>
            <template v-if="canCreate && row.status === 'EXECUTING'">
              <el-divider direction="vertical" />
              <el-button v-if="!row.deliveryTime" link type="success" :icon="CircleCheck" @click="openDeliveryDialog(row)">标记验收</el-button>
              <el-divider direction="vertical" />
              <el-button link type="warning" :icon="Close" @click="confirmClose(row)">关闭</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 付款节点弹窗 -->
    <el-dialog v-model="paymentOpen" title="付款节点" width="720px">
      <div v-if="paymentContract" class="dialog-meta">
        合同: <strong>{{ paymentContract.contractName }}</strong> ·
        总额: <strong>{{ formatMoney(paymentContract.totalAmount) }}</strong> ·
        已付: <strong class="text-success">{{ formatMoney(paymentContract.paidAmount) }}</strong>
      </div>
      <el-table :data="payments" stripe style="margin-top: 12px;">
        <el-table-column label="节点" prop="nodeName" min-width="140" />
        <el-table-column label="计划金额" align="right" min-width="120">
          <template #default="{ row }">{{ formatMoney(row.planAmount) }}</template>
        </el-table-column>
        <el-table-column label="计划日期" prop="planDate" width="130">
          <template #default="{ row }">{{ row.planDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="实付金额" align="right" min-width="120">
          <template #default="{ row }">
            <span v-if="row.status === 1">{{ formatMoney(row.actualAmount) }}</span>
            <span v-else class="muted">未付</span>
          </template>
        </el-table-column>
        <el-table-column label="付款时间" prop="payTime" width="170">
          <template #default="{ row }">
            <span v-if="row.payTime">{{ row.payTime }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canCreate" label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 1" link type="primary" @click="openPayDialog(row)">标记已付</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 标记已付 -->
    <el-dialog v-model="payOpen" title="标记已付" width="420px">
      <el-form v-if="payingNode" :model="payForm" label-position="top">
        <el-form-item label="节点名称">
          <el-input :value="payingNode.nodeName" disabled />
        </el-form-item>
        <el-form-item label="计划金额">
          <el-input :value="formatMoney(payingNode.planAmount)" disabled />
        </el-form-item>
        <el-form-item label="实付金额" required>
          <el-input-number v-model="payForm.actualAmount" :min="0" :step="1000" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payOpen = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="markPaid">确认</el-button>
      </template>
    </el-dialog>

    <!-- 关闭合同 -->
    <el-dialog v-model="closeOpen" title="确认关闭合同" width="460px">
      <div v-if="closeTarget" class="dialog-text">
        关闭合同「<strong>{{ closeTarget.contractName }}</strong>」?
      </div>
      <div class="dialog-warn">关闭后状态变为「已关闭」,不可撤销;已交付合同不可关闭</div>
      <template #footer>
        <el-button @click="closeOpen = false">取消</el-button>
        <el-button type="danger" :loading="closing" @click="doClose">确认关闭</el-button>
      </template>
    </el-dialog>

    <!-- 标记验收 -->
    <el-dialog v-model="deliveryOpen" title="标记验收时间" width="460px">
      <div v-if="deliveryTarget" class="dialog-text">
        合同「<strong>{{ deliveryTarget.contractName }}</strong>」标记验收
      </div>
      <el-form :model="deliveryForm" label-position="top" style="margin-top: 12px;">
        <el-form-item label="验收时间" required>
          <el-date-picker
            v-model="deliveryForm.deliveryTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择验收时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-hint">若该合同所有付款节点均已标记已付,提交后将自动推进到「已交付」状态</div>
      <template #footer>
        <el-button @click="deliveryOpen = false">取消</el-button>
        <el-button type="primary" :loading="deliverySaving" @click="saveDelivery">确认验收</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, Plus, View, Money, Close, CircleCheck } from '@element-plus/icons-vue'
import {
  contractApi, Contract, ContractPayment,
  contractStatusLabel, contractStatusTagType, formatMoney,
} from '@/api/contract'
import { getAuthState } from '@/auth/authStore'

export default defineComponent({
  name: 'ContractListView',
  setup() {
    const router = useRouter()
    const auth = getAuthState()
    const canCreate = computed(() => !!auth.user?.roles?.includes('OPP_ADMIN'))

    const rows = ref<Contract[]>([])
    const loading = ref(false)

    const query = reactive({
      keyword: '',
      customerName: '',
      status: '' as 'EXECUTING' | 'COMPLETED' | 'CLOSED' | '',
      year: undefined as number | undefined,
    })

    const currentYear = new Date().getFullYear()
    const yearOptions = [currentYear, currentYear - 1, currentYear - 2, 2019]

    const reload = async () => {
      loading.value = true
      try {
        rows.value = await contractApi.list({
          keyword: query.keyword.trim() || undefined,
          customerName: query.customerName.trim() || undefined,
          status: query.status || undefined,
          year: query.year,
        })
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        rows.value = []
      } finally {
        loading.value = false
      }
    }

    const resetQuery = () => {
      query.keyword = ''
      query.customerName = ''
      query.status = ''
      query.year = undefined
      reload()
    }

    /** 付款节点弹窗 */
    const paymentOpen = ref(false)
    const paymentContract = ref<Contract | null>(null)
    const payments = ref<ContractPayment[]>([])
    const openPaymentDialog = async (row: Contract) => {
      paymentContract.value = row
      paymentOpen.value = true
      try {
        const detail = await contractApi.detail(row.id)
        payments.value = detail.payments || []
      } catch (e: any) {
        ElMessage.error(e?.message || '加载付款节点失败')
        payments.value = []
      }
    }

    /** 标记已付 */
    const payOpen = ref(false)
    const payingNode = ref<ContractPayment | null>(null)
    const payForm = reactive({ actualAmount: 0 })
    const paying = ref(false)
    const openPayDialog = (node: ContractPayment) => {
      payingNode.value = node
      payForm.actualAmount = Number(node.planAmount || 0)
      payOpen.value = true
    }
    const markPaid = async () => {
      if (!payingNode.value) return
      paying.value = true
      try {
        await contractApi.markPaymentPaid(payingNode.value.id, payForm.actualAmount)
        ElMessage.success('已标记')
        payOpen.value = false
        if (paymentContract.value) await openPaymentDialog(paymentContract.value)
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        paying.value = false
      }
    }

    /** 关闭合同 */
    const closeOpen = ref(false)
    const closeTarget = ref<Contract | null>(null)
    const closing = ref(false)
    const confirmClose = (row: Contract) => {
      closeTarget.value = row
      closeOpen.value = true
    }
    const doClose = async () => {
      if (!closeTarget.value) return
      closing.value = true
      try {
        await contractApi.close(closeTarget.value.id)
        ElMessage.success('合同已关闭')
        closeOpen.value = false
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '关闭失败')
      } finally {
        closing.value = false
      }
    }

    /** 标记验收 */
    const deliveryOpen = ref(false)
    const deliveryTarget = ref<Contract | null>(null)
    const deliverySaving = ref(false)
    const deliveryForm = reactive({ deliveryTime: '' })
    const openDeliveryDialog = (row: Contract) => {
      deliveryTarget.value = row
      const now = new Date()
      const pad = (n: number) => String(n).padStart(2, '0')
      deliveryForm.deliveryTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}:00`
      deliveryOpen.value = true
    }
    const saveDelivery = async () => {
      if (!deliveryTarget.value || !deliveryForm.deliveryTime) {
        ElMessage.error('请选择验收时间')
        return
      }
      deliverySaving.value = true
      try {
        await contractApi.setDelivery(deliveryTarget.value.id, deliveryForm.deliveryTime)
        ElMessage.success('已标记验收')
        deliveryOpen.value = false
        await reload()
      } catch (e: any) {
        ElMessage.error(e?.message || '操作失败')
      } finally {
        deliverySaving.value = false
      }
    }

    const goPreview = (id: number) => router.push({ path: '/contracts/preview', query: { contractId: id } })

    onMounted(reload)

    return {
      rows, loading, query, yearOptions, canCreate,
      reload, resetQuery,
      paymentOpen, paymentContract, payments, openPaymentDialog,
      payOpen, payingNode, payForm, paying, openPayDialog, markPaid,
      closeOpen, closeTarget, closing, confirmClose, doClose,
      deliveryOpen, deliveryTarget, deliverySaving, deliveryForm,
      openDeliveryDialog, saveDelivery,
      goPreview,
      contractStatusLabel, contractStatusTagType, formatMoney,
      Search, RefreshLeft, Plus, View, Money, Close, CircleCheck,
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
.filter-card, .table-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__body) { padding: 16px 20px; }
  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
  }
}
.search-form :deep(.el-form-item) { margin-bottom: 0; margin-right: 16px; }
.contract-name { font-weight: 600; color: #0F172A; }
.dialog-meta {
  font-size: 13px; color: #475569;
  padding: 8px 12px; background: #F8FAFC; border-radius: 8px;
  strong { color: #0F172A; }
}
.dialog-text { font-size: 14px; color: #475569; line-height: 1.7; strong { color: #0F172A; } }
.dialog-warn { font-size: 12px; color: #D97706; margin-top: 8px; }
.text-success { color: #059669; }
.muted { color: #94A3B8; }
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
