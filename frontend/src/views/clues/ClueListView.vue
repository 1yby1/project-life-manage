<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索清单</h2>
      <el-button type="primary" :icon="Plus" @click="$router.push('/clues/create')">
        线索录入
      </el-button>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-radio-group v-model="filterTab">
        <el-radio-button value="all">全量({{ counts.all }})</el-radio-button>
        <el-radio-button value="mine">我录入的({{ counts.mine }})</el-radio-button>
        <el-radio-button value="participate">我参与的({{ counts.participate }})</el-radio-button>
        <el-radio-button value="todo">我的待办({{ counts.todo }})</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table :data="paged" stripe style="width: 100%">
        <el-table-column prop="title" label="线索名称" min-width="200">
          <template #default="{ row }">
            <span class="clue-name">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户" min-width="160">
          <template #default="{ row }">{{ row.customerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="阶段" width="120">
          <template #default="{ row }">
            <el-tag :type="leadStageTagType(row.status)" effect="light" round>
              {{ leadStatusToStage(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="录入人" width="110">
          <template #default="{ row }">{{ row.entryByName || '-' }}</template>
        </el-table-column>
        <el-table-column label="客户经理" width="110">
          <template #default="{ row }">{{ row.managerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">{{ (row.createTime || '').slice(0, 10) || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canCollect(row)" link type="primary" :icon="Document" @click="goCollect(row.id)">
              收集 / 确认
            </el-button>
            <el-button v-if="canDistribute(row)" link type="warning" :icon="Position" @click="goDistribute(row.id)">
              分发
            </el-button>
            <el-button v-if="canCultivate(row)" link type="primary" :icon="MagicStick" @click="goCultivate(row.id)">
              培育 / 转商机
            </el-button>
            <span v-if="!canCollect(row) && !canDistribute(row) && !canCultivate(row)" class="no-action">
              无可用操作
            </span>
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
          :total="filtered.length"
          layout="prev, pager, next, total"
          background
          @update:current-page="pageIndex = $event"
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Document, Position, MagicStick } from '@element-plus/icons-vue'
import { leadApi, LeadListItem, LeadFilter } from '@/api/lead'
import { getAuthState } from '@/auth/authStore'
import { leadStatusToStage, leadStageTagType } from '@/utils/leadDetail'

export default defineComponent({
  name: 'ClueListView',
  components: { Plus, Document, Position, MagicStick },
  setup() {
    const router = useRouter()
    const authState = getAuthState()

    const userId = computed(() => {
      const id = authState.user?.id
      return id ? Number(id) : null
    })
    const roles = computed(() => authState.user?.roles || [])
    const isOppAdmin = computed(() => roles.value.indexOf('OPP_ADMIN') !== -1)
    const isCustomerManager = computed(() => roles.value.indexOf('CUSTOMER_MANAGER') !== -1)

    const all = ref<LeadListItem[]>([])
    const loading = ref(false)
    const filterTab = ref<LeadFilter>('all')

    const loadAll = async () => {
      loading.value = true
      try {
        // filter=all 默认隐藏 CONVERTED;一次拿全量,前端做 tab 过滤
        all.value = await leadApi.list({ filter: 'all' })
      } catch (e: any) {
        ElMessage.error(e?.message || '加载线索清单失败')
        all.value = []
      } finally {
        loading.value = false
      }
    }

    const isMine = (l: LeadListItem) => userId.value != null && l.entryBy === userId.value
    const isParticipating = (l: LeadListItem) => {
      if (userId.value == null) return false
      return l.entryBy === userId.value
        || l.collectorBy === userId.value
        || l.distributorBy === userId.value
        || l.managerId === userId.value
    }
    const isMyTodo = (l: LeadListItem) => {
      if (userId.value == null) return false
      if (isOppAdmin.value && l.status === 'COLLECTED') return true
      if (isCustomerManager.value && l.status === 'DISTRIBUTED' && l.managerId === userId.value) return true
      // 普通用户/SALES: 自己录入的待收集
      if (!isOppAdmin.value && !isCustomerManager.value
          && l.status === 'ENTRY' && l.entryBy === userId.value) return true
      return false
    }

    const counts = computed(() => ({
      all: all.value.length,
      mine: all.value.filter(isMine).length,
      participate: all.value.filter(isParticipating).length,
      todo: all.value.filter(isMyTodo).length,
    }))

    const filtered = computed(() => {
      switch (filterTab.value) {
        case 'mine': return all.value.filter(isMine)
        case 'participate': return all.value.filter(isParticipating)
        case 'todo': return all.value.filter(isMyTodo)
        case 'all':
        default: return all.value
      }
    })

    const pageSize = 10
    const pageIndex = ref(1)
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    // 操作按钮可见性(归属 + 状态)
    const canCollect = (l: LeadListItem) =>
      l.status === 'ENTRY' && userId.value != null && l.entryBy === userId.value
    const canDistribute = (l: LeadListItem) =>
      l.status === 'COLLECTED' && isOppAdmin.value
    const canCultivate = (l: LeadListItem) =>
      l.status === 'DISTRIBUTED' && isCustomerManager.value && userId.value != null && l.managerId === userId.value

    const goCollect = (id: number) => router.push({ path: '/clues/collect', query: { clueId: String(id) } })
    const goDistribute = (id: number) => router.push({ path: '/clues/distribute', query: { clueId: String(id) } })
    const goCultivate = (id: number) => router.push({ path: '/clues/cultivate', query: { clueId: String(id) } })

    onMounted(loadAll)

    return {
      Plus, Document, Position, MagicStick,
      filterTab,
      counts,
      filtered,
      paged,
      pageIndex,
      pageSize,
      loading,
      goCollect,
      goDistribute,
      goCultivate,
      canCollect,
      canDistribute,
      canCultivate,
      leadStatusToStage,
      leadStageTagType,
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
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.table-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important;
    color: #0F172A;
    font-weight: 600;
    font-size: 13px;
  }

  :deep(.el-table td.el-table__cell) {
    font-size: 13px;
  }
}

.clue-name {
  font-weight: 500;
  color: #0F172A;
}

.no-action {
  color: #94A3B8;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  background: #FAFBFC;
  border-top: 1px solid #E2E8F0;
  border-radius: 0 0 12px 12px;
}

:deep(.el-button--primary:not(.is-link)) {
  background-color: #0369A1;
  border-color: #0369A1;

  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}

:deep(.el-button--primary.is-link) {
  color: #0369A1;

  &:hover {
    color: #0284C7;
  }
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #0369A1;
  border-color: #0369A1;
  box-shadow: -1px 0 0 0 #0369A1;
}
</style>
