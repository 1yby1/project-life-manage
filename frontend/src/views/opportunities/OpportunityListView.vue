<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商机列表</h2>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="商机名称">
          <el-input
            v-model="query.opportunityName"
            placeholder="输入商机名称"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="客户">
          <el-input
            v-model="query.customerName"
            placeholder="输入客户名称"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="query.stage" placeholder="选择阶段" clearable style="width: 140px">
            <el-option label="全部" value="all" />
            <el-option label="模板选择" value="模板选择" />
            <el-option label="推进中" value="推进中" />
            <el-option label="已结束" value="已结束" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="pageIndex = 1">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="paged" style="width: 100%" stripe>
        <el-table-column prop="opportunityName" label="商机名称" min-width="180" />
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            {{ customerName(row.customerId) }}
          </template>
        </el-table-column>
        <el-table-column label="阶段" width="120">
          <template #default="{ row }">
            <el-tag :type="getStageTagType(row.stage)" effect="light">
              {{ row.stage }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="180">
          <template #default="{ row }">
            <el-progress
              :percentage="getProgress(row)"
              :color="getProgressColor(row)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column prop="templateKey" label="模板" width="120">
          <template #default="{ row }">
            {{ row.templateKey || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.stage === '模板选择'"
              type="primary"
              size="small"
              @click="goTemplate(row.id)"
            >
              选择商机推进模板
            </el-button>
            <el-button
              v-else
              size="small"
              @click="goDetail(row.id)"
            >
              进入项目详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无数据" :image-size="100" />
        </template>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageIndex"
          :page-size="pageSize"
          :total="filtered.length"
          layout="prev, pager, next, total"
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
import { Search, User } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'OpportunityListView',
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const query = reactive({
      opportunityName: '',
      customerName: '',
      stage: 'all' as 'all' | '模板选择' | '推进中' | '已结束',
    })

    const pageSize = 10
    const pageIndex = ref(1)

    const filtered = computed(() => {
      return store.opportunities.filter((o) => {
        const okName = query.opportunityName ? o.opportunityName.includes(query.opportunityName) : true
        const okCustomer = query.customerName
          ? (store.customers.find((c) => c.id === o.customerId)?.customerName || '').includes(query.customerName)
          : true
        const okStage = query.stage === 'all' ? true : o.stage === query.stage
        return okName && okCustomer && okStage
      })
    })

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const customerName = (customerId?: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'

    const goTemplate = (opportunityId: string) => router.push({ path: '/opportunities/template', query: { opportunityId } })
    const goDetail = (opportunityId: string) => router.push({ path: `/opportunities/${opportunityId}` })

    const getStageTagType = (stage: string) => {
      switch (stage) {
        case '模板选择': return 'info'
        case '推进中': return 'warning'
        case '已结束': return 'success'
        default: return ''
      }
    }

    const getProgress = (row: any) => {
      switch (row.stage) {
        case '模板选择': return 10
        case '推进中': return 50
        case '已结束': return 100
        default: return 0
      }
    }

    const getProgressColor = (row: any) => {
      switch (row.stage) {
        case '模板选择': return '#94A3B8'
        case '推进中': return '#D97706'
        case '已结束': return '#059669'
        default: return '#E2E8F0'
      }
    }

    return {
      Search,
      User,
      query,
      pageIndex,
      pageSize,
      totalPages,
      filtered,
      paged,
      customerName,
      goTemplate,
      goDetail,
      getStageTagType,
      getProgress,
      getProgressColor,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  padding: 20px;
  background: #F8FAFC;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-weight: 700;
  font-size: 20px;
  color: #0F172A;
  margin: 0;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__body) {
    padding: 16px;
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: flex-end;
  }

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 0;
  }

  :deep(.el-form-item__label) {
    color: #334155;
    font-size: 13px;
    font-weight: 500;
  }

  :deep(.el-input) {
    width: 180px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #E2E8F0;

    &:hover {
      box-shadow: 0 0 0 1px #CBD5E1;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #0369A1;
    }
  }

  :deep(.el-select .el-input__wrapper) {
    border-radius: 8px;
  }
}

.table-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table) {
    --el-table-header-bg-color: #F8FAFC;
    --el-table-header-text-color: #334155;
    --el-table-row-hover-bg-color: #F1F5F9;
    --el-table-border-color: #E2E8F0;
    font-size: 13px;
  }

  :deep(.el-table th) {
    font-weight: 600;
  }

  :deep(.el-table__empty-block) {
    padding: 40px 0;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
  border-top: 1px solid #E2E8F0;
}

:deep(.el-button--primary) {
  --el-button-bg-color: #0369A1;
  --el-button-border-color: #0369A1;
  --el-button-hover-bg-color: #0284C7;
  --el-button-hover-border-color: #0284C7;
}

:deep(.el-tag--success) {
  --el-tag-bg-color: #ECFDF5;
  --el-tag-border-color: #A7F3D0;
  --el-tag-text-color: #059669;
}

:deep(.el-tag--warning) {
  --el-tag-bg-color: #FFFBEB;
  --el-tag-border-color: #FDE68A;
  --el-tag-text-color: #D97706;
}

:deep(.el-tag--info) {
  --el-tag-bg-color: #F1F5F9;
  --el-tag-border-color: #CBD5E1;
  --el-tag-text-color: #334155;
}

:deep(.el-tag--danger) {
  --el-tag-bg-color: #FEF2F2;
  --el-tag-border-color: #FECACA;
  --el-tag-text-color: #DC2626;
}

:deep(.el-progress-bar__outer) {
  border-radius: 4px;
  background-color: #E2E8F0;
}

:deep(.el-progress-bar__inner) {
  border-radius: 4px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0369A1;
}
</style>

