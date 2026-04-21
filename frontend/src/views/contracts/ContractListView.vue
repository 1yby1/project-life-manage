<template>
  <div class="page">
    <div class="page-title">合同列表</div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="合同名称">
          <el-input v-model="query.contractName" placeholder="输入合同名称" clearable />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="query.customerName" placeholder="输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="选择状态">
            <el-option label="全部" value="all" />
            <el-option label="执行中" value="执行中" />
            <el-option label="执行完成" value="执行完成" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="pageIndex = 1">搜索</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" @click="toCreate">创建合同</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="paged" stripe style="width: 100%">
        <el-table-column prop="contractName" label="合同名称" min-width="150" />
        <el-table-column label="客户" min-width="120">
          <template #default="{ row }">{{ customerName(row.customerId) }}</template>
        </el-table-column>
        <el-table-column prop="contractType" label="类型" width="100" />
        <el-table-column label="金额" width="140" :formatter="formatAmountColumn" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '执行中' ? 'primary' : 'success'" effect="light">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="toPreview(row.id)">正文预览</el-button>
            <el-button size="small" @click="openPaymentNodes(row)">付款节点</el-button>
            <el-button
              v-if="row.status === '执行中'"
              size="small"
              type="warning"
              @click="openCloseConfirm(row)"
            >
              关闭合同
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageIndex"
          :page-size="pageSize"
          :total="filtered.length"
          layout="prev, pager, next, total"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="paymentModalOpen" :title="paymentModalTitle" width="600px">
      <div v-if="paymentContract">
        <div class="dialog-subtitle">合同：{{ paymentContract.contractName }}</div>
        <el-table :data="paymentContract.paymentNodes" stripe style="width: 100%; margin-top: 12px;">
          <el-table-column prop="nodeName" label="节点" />
          <el-table-column label="金额">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="预计时间">
            <template #default="{ row }">{{ row.dueAt || '-' }}</template>
          </el-table-column>
          <el-table-column label="回款时间">
            <template #default="{ row }">{{ row.paidAt || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="paymentModalOpen = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="closeModalOpen" title="确认关闭合同" width="450px">
      <div class="dialog-text">
        关闭后将自动移入“执行完成合同”，且该合同在看板右侧可见。
      </div>
      <div v-if="closeTarget" class="dialog-subtitle">合同：{{ closeTarget.contractName }}</div>
      <template #footer>
        <el-button @click="closeModalOpen = false">取消</el-button>
        <el-button type="danger" @click="confirmClose">关闭合同</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useMockStore } from '@/store/mockStore'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'ContractListView',
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const query = reactive({
      contractName: '',
      customerName: '',
      status: 'all' as 'all' | '执行中' | '执行完成',
    })

    const pageSize = 10
    const pageIndex = ref(1)

    const filtered = computed(() => {
      return store.contracts.filter((c) => {
        const okName = query.contractName ? c.contractName.includes(query.contractName) : true
        const okCustomer = query.customerName
          ? (store.customers.find((cc) => cc.id === c.customerId)?.customerName || '').includes(query.customerName)
          : true
        const okStatus = query.status === 'all' ? true : c.status === query.status
        return okName && okCustomer && okStatus
      })
    })

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const reset = () => {
      query.contractName = ''
      query.customerName = ''
      query.status = 'all'
      pageIndex.value = 1
    }

    const customerName = (customerId: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'
    const formatMoney = (n: number) => `￥${Number(n).toLocaleString()}`
    const formatAmountColumn = (row: any) => formatMoney(row.contractAmount)

    // 付款节点弹框
    const paymentModalOpen = ref(false)
    const paymentModalTitle = ref('付款节点')
    const paymentContract = ref<any>(null)

    const openPaymentNodes = (c: any) => {
      paymentContract.value = c
      paymentModalOpen.value = true
    }

    // 关闭合同弹框
    const closeModalOpen = ref(false)
    const closeTarget = ref<any>(null)

    const openCloseConfirm = (c: any) => {
      closeTarget.value = c
      closeModalOpen.value = true
    }

    const confirmClose = () => {
      if (!closeTarget.value) return
      closeTarget.value.status = '执行完成'
      closeTarget.value.paymentNodes = closeTarget.value.paymentNodes.map((pn: any, idx: number) => ({
        ...pn,
        paidAt: pn.paidAt || new Date(Date.now() - idx * 86400000).toISOString().slice(0, 10),
      }))
      closeModalOpen.value = false
      closeTarget.value = null
    }

    const toPreview = (id: string) => router.push({ path: `/contracts/${id}` })
    const toCreate = () => router.push({ path: '/contracts/create' })

    return {
      query,
      pageIndex,
      pageSize,
      totalPages,
      filtered,
      paged,
      reset,
      customerName,
      formatMoney,
      formatAmountColumn,
      openPaymentNodes,
      paymentModalOpen,
      paymentModalTitle,
      paymentContract,
      closeModalOpen,
      closeTarget,
      openCloseConfirm,
      confirmClose,
      toPreview,
      toCreate,
    }
  },
})
</script>

<style scoped lang="scss">
.page-title {
  font-weight: 800;
  font-size: 16px;
  margin-bottom: 12px;
  color: #0F172A;
}

.search-card {
  margin-bottom: 12px;
  border-radius: 14px;
  background: #fff;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }
  
  :deep(.el-form-item__label) {
    color: #334155;
    font-size: 13px;
  }
  
  :deep(.el-input) {
    width: 180px;
  }
  
  :deep(.el-select) {
    width: 140px;
  }
}

.table-card {
  border-radius: 14px;
  background: #fff;
  
  :deep(.el-card__body) {
    padding: 0;
  }
  
  :deep(.el-table) {
    border-radius: 14px 14px 0 0;
  }
  
  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC;
    color: #0F172A;
    font-weight: 700;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 0 0 14px 14px;
}

.dialog-subtitle {
  font-weight: 700;
  margin-top: 6px;
  color: #0F172A;
}

.dialog-text {
  color: #334155;
  line-height: 1.6;
  margin-bottom: 12px;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;
  
  &:hover {
    background-color: #0284c7;
    border-color: #0284c7;
  }
}

:deep(.el-button--warning) {
  background-color: #D97706;
  border-color: #D97706;
  
  &:hover {
    background-color: #f59e0b;
    border-color: #f59e0b;
  }
}

:deep(.el-button--danger) {
  background-color: #DC2626;
  border-color: #DC2626;
  
  &:hover {
    background-color: #ef4444;
    border-color: #ef4444;
  }
}

:deep(.el-tag--primary) {
  background-color: #dbeafe;
  border-color: #93c5fd;
  color: #1e40af;
}

:deep(.el-tag--success) {
  background-color: #d1fae5;
  border-color: #6ee7b7;
  color: #059669;
}

:deep(.el-dialog) {
  border-radius: 14px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 20px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e5e7eb;
  padding: 12px 20px;
}
</style>

