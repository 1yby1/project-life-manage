<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">客户列表</h2>
      <el-button
        v-if="canCreate"
        type="primary"
        :icon="Plus"
        @click="router.push('/customers/create')"
      >
        新建客户
      </el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="query.keyword" placeholder="客户名/法人/联系人" clearable :prefix-icon="Search" />
        </el-form-item>
        <el-form-item label="地市">
          <el-input v-model="query.city" placeholder="输入地市" clearable :prefix-icon="Location" />
        </el-form-item>
        <el-form-item label="流程阶段">
          <el-select v-model="query.visitStatus" placeholder="全部" clearable style="width: 160px">
            <el-option label="已录入(未派单)" value="NONE" />
            <el-option label="已派单" value="PENDING" />
            <el-option label="走访中" value="DOING" />
            <el-option label="走访完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table :data="rows" stripe style="width: 100%">
        <el-table-column prop="customerName" label="客户名称" min-width="200">
          <template #default="{ row }">
            <span class="company-name">{{ row.customerName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="地市" width="120" />
        <el-table-column prop="legalPerson" label="法人" width="120" />
        <el-table-column label="流程阶段" width="140">
          <template #default="{ row }">
            <el-tag :type="visitStageTagType(row.visitStatus)" effect="light" round>
              {{ visitStatusToStage(row.visitStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户经理" width="120">
          <template #default="{ row }">
            <span v-if="row.assignedManagerName">{{ row.assignedManagerName }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="toDetail(row.id)">查看</el-button>
            <template v-if="isOpportunityManager && !row.visitStatus">
              <el-divider direction="vertical" />
              <el-button link type="success" :icon="Position" @click="toDispatch(row.id)">派单</el-button>
            </template>
            <template v-if="isCustomerManager && (row.visitStatus === 'PENDING' || row.visitStatus === 'DOING')">
              <el-divider direction="vertical" />
              <el-button link type="warning" :icon="LocationInformation" @click="toVisits(row.id)">走访</el-button>
            </template>
            <template v-if="isCustomerManager && row.visitStatus">
              <el-divider direction="vertical" />
              <el-button link type="primary" :icon="Edit" @click="toComplete(row.id)">完善</el-button>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无数据" :image-size="80" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page="pageIndex"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          background
          @update:current-page="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { customerApi, Customer, VisitStatus } from '@/api/customer'
import { getAuthState } from '@/auth/authStore'
import { visitStatusToStage, visitStageTagType } from '@/utils/visitRecord'
import {
  Plus, Search, Location, RefreshLeft, View, Position, LocationInformation, Edit,
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerListView',
  setup() {
    const router = useRouter()
    const authState = getAuthState()

    const isOpportunityManager = computed(() => (authState.user?.roles || []).indexOf('OPP_ADMIN') !== -1)
    const isCustomerManager = computed(() => (authState.user?.roles || []).indexOf('CUSTOMER_MANAGER') !== -1)
    const canCreate = computed(() => isOpportunityManager.value)

    const query = reactive({
      keyword: '',
      city: '',
      visitStatus: '' as '' | VisitStatus | 'NONE',
    })

    const pageSize = 10
    const pageIndex = ref(1)
    const total = ref(0)
    const rows = ref<Customer[]>([])
    const loading = ref(false)

    const load = async () => {
      loading.value = true
      try {
        const res = await customerApi.list({
          page: pageIndex.value,
          size: pageSize,
          keyword: query.keyword || undefined,
          city: query.city || undefined,
          visitStatus: query.visitStatus || undefined,
        })
        rows.value = res.records || []
        total.value = res.total || 0
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户列表失败')
        rows.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    const onSearch = () => {
      pageIndex.value = 1
      load()
    }

    const onPageChange = (p: number) => {
      pageIndex.value = p
      load()
    }

    const reset = () => {
      query.keyword = ''
      query.city = ''
      query.visitStatus = ''
      pageIndex.value = 1
      load()
    }

    onMounted(load)

    const toDetail = (id: number) => router.push({ path: `/customers/${id}` })
    const toDispatch = (id: number) => router.push({ path: '/customers/dispatch', query: { customerId: String(id) } })
    const toVisits = (id: number) => router.push({ path: '/customers/visits', query: { customerId: String(id) } })
    const toComplete = (id: number) => router.push({ path: '/customers/complete', query: { customerId: String(id) } })

    return {
      query,
      pageIndex,
      pageSize,
      total,
      rows,
      loading,
      onSearch,
      onPageChange,
      reset,
      toDetail,
      toDispatch,
      toVisits,
      toComplete,
      isOpportunityManager,
      isCustomerManager,
      canCreate,
      visitStatusToStage,
      visitStageTagType,
      router,
      Plus, Search, Location, RefreshLeft, View, Position, LocationInformation, Edit,
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
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;

  :deep(.el-card__body) {
    padding: 16px 24px;
  }
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }

  :deep(.el-form-item__label) {
    color: #6b7280;
    font-size: 14px;
    font-weight: 400;
  }

  :deep(.el-input) {
    width: 200px;
  }
}

.table-card {
  border-radius: 8px;
  border: 1px solid #e5e7eb;

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table th.el-table__cell) {
    background: #f9fafb !important;
    color: #6b7280;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #e5e7eb;
  }

  :deep(.el-table td.el-table__cell) {
    font-size: 14px;
    color: #111827;
    border-bottom: 1px solid #e5e7eb;
  }

  :deep(.el-table__row) {
    cursor: pointer;
    transition: background-color 200ms ease;
  }

  :deep(.el-table__body tr:hover > td.el-table__cell) {
    background-color: #f3f4f6 !important;
  }
}

.company-name {
  font-weight: 600;
  color: #111827;
}

.muted {
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  border-radius: 0 0 8px 8px;
}

:deep(.el-button) {
  border-radius: 6px;
  font-size: 14px;
}

:deep(.el-button--primary:not(.is-link)) {
  background-color: #0369a1;
  border-color: #0369a1;

  &:hover {
    background-color: #0284c7;
    border-color: #0284c7;
  }
}

:deep(.el-button--primary.is-link) {
  color: #0369a1;

  &:hover {
    color: #0284c7;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
}
</style>
