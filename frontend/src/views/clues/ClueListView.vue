<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索清单</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/clues/create')">
        线索录入
      </el-button>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-radio-group v-model="filterTab" size="default">
        <el-radio-button value="all">全量（{{ counts.all }}）</el-radio-button>
        <el-radio-button value="mine">我录入的（{{ counts.mine }}）</el-radio-button>
        <el-radio-button value="participate">我参与的（{{ counts.participate }}）</el-radio-button>
        <el-radio-button value="todo">我的待办（{{ counts.todo }}）</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="paged" style="width: 100%" stripe>
        <el-table-column prop="clueName" label="线索名称" min-width="180" />
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            {{ row.customerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="阶段" width="120">
          <template #default="{ row }">
            <el-tag :type="getStageTagType(row.stage)" effect="light">
              {{ row.stage }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">
            {{ row.createdAt.slice(0, 10) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="canCollect(row)"
                size="small"
                @click="goCollect(row.id)"
              >
                去收集/确认
              </el-button>
              <el-button
                v-if="canDistribute(row)"
                size="small"
                @click="goDistribute(row.id)"
              >
                去分发
              </el-button>
              <el-button
                v-if="canCultivate(row)"
                type="primary"
                size="small"
                @click="goCultivate(row.id)"
              >
                去培育/转商机
              </el-button>
              <span v-if="!canCollect(row) && !canDistribute(row) && !canCultivate(row)" class="no-action">
                无可用操作
              </span>
            </div>
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
import { computed, defineComponent, ref } from 'vue'
import { useMockStore, getEmployeeIdByRoleLabel } from '@/store/mockStore'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

type FilterTab = 'all' | 'mine' | 'participate' | 'todo'

export default defineComponent({
  name: 'ClueListView',
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const roleLabel = window.localStorage.getItem('demo_role') || '线索收集人'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel)
    const myRoleTag = computed(() => store.employees.find((e) => e.id === employeeId)?.roleTag)

    // 隐藏已转商机线索：被任意商机引用 clueId
    const convertedClueIds = computed(() => new Set(store.opportunities.map((o) => o.clueId)))
    const baseClues = computed(() => store.clues.filter((l) => !convertedClueIds.value.has(l.id)))

    const filterTab = ref<FilterTab>('all')

    const counts = computed(() => {
      const list = baseClues.value
      const mine = employeeId ? list.filter((l) => l.createdByEmployeeId === employeeId).length : 0
      const participate = employeeId
        ? list.filter((l) => l.createdByEmployeeId === employeeId || l.assignedCustomerManagerId === employeeId).length
        : 0

      const todo = (() => {
        if (!employeeId) return 0
        const tag = myRoleTag.value
        if (tag === '线索收集人') return list.filter((l) => l.stage === '收集').length
        if (tag === '线索分发人') return list.filter((l) => l.stage === '分发').length
        if (tag === '客户经理') return list.filter((l) => l.stage === '培育' && l.assignedCustomerManagerId === employeeId).length
        return 0
      })()

      return {
        all: list.length,
        mine,
        participate,
        todo,
      }
    })

    const filtered = computed(() => {
      const list = baseClues.value
      if (filterTab.value === 'all') return list
      if (filterTab.value === 'mine') return employeeId ? list.filter((l) => l.createdByEmployeeId === employeeId) : []
      if (filterTab.value === 'participate') {
        return employeeId ? list.filter((l) => l.createdByEmployeeId === employeeId || l.assignedCustomerManagerId === employeeId) : []
      }
      // todo
      if (!employeeId) return []
      const tag = myRoleTag.value
      if (tag === '线索收集人') return list.filter((l) => l.stage === '收集')
      if (tag === '线索分发人') return list.filter((l) => l.stage === '分发')
      if (tag === '客户经理') return list.filter((l) => l.stage === '培育' && l.assignedCustomerManagerId === employeeId)
      return []
    })

    const pageSize = 10
    const pageIndex = ref(1)

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const goCollect = (clueId: string) => router.push({ path: '/clues/collect', query: { clueId } })
    const goDistribute = (clueId: string) => router.push({ path: '/clues/distribute', query: { clueId } })
    const goCultivate = (clueId: string) => router.push({ path: '/clues/cultivate', query: { clueId } })

    const canCollect = (l: any) => myRoleTag.value === '线索收集人' && l.stage === '收集'
    const canDistribute = (l: any) => myRoleTag.value === '线索分发人' && l.stage === '分发'
    const canCultivate = (l: any) => myRoleTag.value === '客户经理' && l.stage === '培育' && l.assignedCustomerManagerId === employeeId

    const getStageTagType = (stage: string) => {
      switch (stage) {
        case '收集': return 'info'
        case '分发': return 'warning'
        case '培育': return 'success'
        default: return ''
      }
    }

    return {
      Plus,
      filterTab,
      counts,
      filtered,
      paged,
      pageIndex,
      pageSize,
      totalPages,

      goCollect,
      goDistribute,
      goCultivate,
      canCollect,
      canDistribute,
      canCultivate,
      getStageTagType,
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

  :deep(.el-radio-button__inner) {
    border-radius: 8px !important;
    border: none;
    background: #F1F5F9;
    color: #334155;
    font-size: 13px;
    padding: 8px 16px;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: #0369A1;
    color: #fff;
    box-shadow: none;
  }

  :deep(.el-radio-group) {
    gap: 8px;
    display: flex;
    flex-wrap: wrap;
  }

  :deep(.el-radio-button) {
    margin: 0;
  }

  :deep(.el-radio-button:first-child .el-radio-button__inner),
  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 8px !important;
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

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.no-action {
  color: #94A3B8;
  font-size: 12px;
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

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #0369A1;
}
</style>

