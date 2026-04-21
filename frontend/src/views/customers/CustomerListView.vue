<template>
  <div class="page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">客户列表</h1>
        <span class="user-role-badge" v-if="isOpportunityManager">商机管理员</span>
        <span class="user-role-badge" v-if="isCustomerManager">客户经理</span>
      </div>
      <el-button
        v-if="isOpportunityManager"
        type="primary"
        :icon="Plus"
        @click="router.push('/customers/create')"
        class="action-button"
      >
        新建客户
      </el-button>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="query" size="large" class="search-form">
        <el-form-item label="客户名称">
          <el-input
            v-model="query.customerName"
            placeholder="输入客户名称"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="地市">
          <el-input
            v-model="query.city"
            placeholder="输入地市"
            clearable
            :prefix-icon="Location"
          />
        </el-form-item>
        <el-form-item label="法人">
          <el-input
            v-model="query.legalPerson"
            placeholder="输入法人"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="流程阶段">
          <el-select
            v-model="query.stage"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="s in stages"
              :key="s"
              :label="s"
              :value="s"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button type="primary" :icon="Search" @click="pageIndex = 1">
            搜索
          </el-button>
          <el-button :icon="RefreshLeft" @click="reset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <el-table :data="paged" style="width: 100%" :header-cell-style="{ background: '#f8fafc', color: '#1e293b', fontWeight: 600 }">
        <el-table-column prop="customerName" label="客户名称" min-width="180">
          <template #default="{ row }">
            <span class="company-name">{{ row.customerName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="地市" min-width="120" />
        <el-table-column prop="legalPerson" label="法人" min-width="120" />
        <el-table-column label="流程阶段" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getStageType(row.stage)" effect="plain">
              {{ row.stage }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                type="primary"
                :icon="View"
                size="small"
                link
                @click="toDetail(row.id)"
              >
                查看
              </el-button>
              <el-button
                v-if="isOpportunityManager && row.stage === '已录入'"
                type="success"
                :icon="Position"
                size="small"
                link
                @click="toDispatch(row.id)"
              >
                派单
              </el-button>
              <el-button
                v-if="isCustomerManager && (row.stage === '已派单' || row.stage === '走访中')"
                type="warning"
                :icon="LocationInformation"
                size="small"
                link
                @click="toVisits(row.id)"
              >
                走访
              </el-button>
              <el-button
                v-if="isCustomerManager && row.stage !== '已录入'"
                type="info"
                :icon="Edit"
                size="small"
                link
                @click="toComplete(row.id)"
              >
                完善
              </el-button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageIndex"
          :page-size="pageSize"
          :total="filtered.length"
          layout="total, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useMockStore } from '@/store/mockStore'
import { useRouter } from 'vue-router'
import type { CustomerStage } from '@/store/mockStore'
import {
  User,
  Plus,
  Search,
  Location,
  RefreshLeft,
  View,
  Position,
  LocationInformation,
  Edit,
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerListView',
  components: {
    User,
    Plus,
    Search,
    Location,
    RefreshLeft,
    View,
    Position,
    LocationInformation,
    Edit,
  },
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const roleLabel = window.localStorage.getItem('demo_role') || '商机管理员'
    const isOpportunityManager = computed(() => roleLabel === '商机管理员')
    const isCustomerManager = computed(() => roleLabel === '客户经理')

    const stages: CustomerStage[] = ['已录入', '已派单', '走访中', '走访完成']

    const query = reactive({
      customerName: '',
      city: '',
      legalPerson: '',
      stage: '' as string,
    })

    const pageSize = 8
    const pageIndex = ref(1)

    const filtered = computed(() => {
      return store.customers.filter((c) => {
        const okName = query.customerName ? c.customerName.includes(query.customerName) : true
        const okCity = query.city ? c.city.includes(query.city) : true
        const okLegal = query.legalPerson ? c.legalPerson.includes(query.legalPerson) : true
        const okStage = query.stage ? c.stage === query.stage : true
        return okName && okCity && okLegal && okStage
      })
    })

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))

    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const reset = () => {
      query.customerName = ''
      query.city = ''
      query.legalPerson = ''
      query.stage = ''
      pageIndex.value = 1
    }

    const toDetail = (customerId: string) => {
      router.push({ path: `/customers/${customerId}` })
    }

    const toDispatch = (customerId: string) => {
      router.push({ path: '/customers/dispatch', query: { customerId } })
    }

    const toVisits = (customerId: string) => {
      router.push({ path: '/customers/visits', query: { customerId } })
    }

    const toComplete = (customerId: string) => {
      router.push({ path: '/customers/complete', query: { customerId } })
    }

    const getStageType = (stage: CustomerStage) => {
      const typeMap: Record<CustomerStage, 'info' | 'warning' | 'success' | ''> = {
        '已录入': 'info',
        '已派单': 'warning',
        '走访中': '',
        '走访完成': 'success',
      }
      return typeMap[stage] || 'info'
    }

    return {
      query,
      pageIndex,
      pageSize,
      totalPages,
      paged,
      filtered,
      reset,
      toDetail,
      toDispatch,
      toVisits,
      toComplete,
      isOpportunityManager,
      isCustomerManager,
      stages,
      getStageType,
      router,
      User,
      Plus,
      Search,
      Location,
      RefreshLeft,
      View,
      Position,
      LocationInformation,
      Edit,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  padding: 32px;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.5px;
}

.user-role-badge {
  background: #f1f5f9;
  color: #64748b;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

.action-button {
  padding: 10px 20px;
  height: auto;
  border-radius: 8px;
  font-weight: 500;
}

.search-card, .table-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 12px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #475569;
  }

  :deep(.el-input__wrapper), :deep(.el-select__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
    background-color: #f8fafc;

    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px #0ea5e9 inset;
      background-color: #ffffff;
    }
  }
}

.company-name {
  font-weight: 600;
  color: #0f172a;
}

.table-actions {
  display: flex;
  gap: 16px;

  :deep(.el-button) {
    padding: 0;
    font-weight: 500;

    &.el-button--primary.is-link {
      color: #0ea5e9;
      &:hover { color: #0284c7; }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

:deep(.el-button--primary:not(.is-link)) {
  background-color: #0ea5e9;
  border-color: #0ea5e9;

  &:hover {
    background-color: #0284c7;
    border-color: #0284c7;
  }
}

:deep(.el-tag) {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 8px;
  border: none;
}

:deep(.el-tag--info) { background-color: #f1f5f9; color: #475569; }
:deep(.el-tag--warning) { background-color: #fef3c7; color: #d97706; }
:deep(.el-tag--success) { background-color: #dcfce7; color: #16a34a; }

@media (max-width: 768px) {
  .page { padding: 16px; }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
