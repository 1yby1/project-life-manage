<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">在途合同专题</h2>
      <span class="page-meta">区总 / OPP_ADMIN 视角 — 催流程催签约</span>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="年份">
          <el-select v-model="query.year" placeholder="全部" clearable class="filter-select">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="BU">
          <el-input v-model="query.bu" placeholder="所属业务单元" clearable class="filter-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">在途合同数</div>
          <div class="stat-value">{{ pagination.total }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">总合同金额</div>
          <div class="stat-value">{{ formatMoney(totalAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">已收款金额</div>
          <div class="stat-value stat-value--success">{{ formatMoney(totalPaid) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never" v-loading="chartLoading">
      <template #header>
        <div class="chart-title">
          <el-icon><DataAnalysis /></el-icon>
          按 BU 在途合同金额 vs 已收款
          <span class="chart-meta">— 不受分页影响,展示全量数据</span>
        </div>
      </template>
      <el-empty v-if="byBu.length === 0 && !chartLoading" description="暂无可视化数据" :image-size="80" />
      <BarChart v-else :option="byBuOption" :height="320" />
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-if="!isMobile" :data="rows" stripe v-loading="loading" empty-text="暂无在途合同">
        <el-table-column label="合同名称" prop="contractName" min-width="220" />
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="年份" prop="contractYear" width="80" />
        <el-table-column label="合同金额" align="right" min-width="140">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="已收款" align="right" min-width="140">
          <template #default="{ row }">
            <span :class="{ 'text-success': row.paidAmount > 0 }">{{ formatMoney(row.paidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收款率" width="220">
          <template #default="{ row }">
            <div class="ratio-cell">
              <el-progress
                :percentage="paidRatio(row)"
                :color="ratioColor(paidRatio(row))"
                :stroke-width="8"
                :show-text="false"
                class="ratio-bar"
              />
              <el-tag
                :type="ratioTagType(paidRatio(row))"
                size="small"
                effect="plain"
                round
                class="ratio-label"
              >
                {{ ratioLabel(paidRatio(row)) }} {{ paidRatio(row) }}%
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="170" />
      </el-table>

      <!-- 移动端:卡片列表 -->
      <div v-else v-loading="loading" class="card-list">
        <el-empty v-if="rows.length === 0" description="暂无在途合同" :image-size="80" />
        <div v-for="row in rows" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <span class="mobile-card__title">{{ row.contractName }}</span>
          </div>
          <div class="mobile-card__meta">
            <span>{{ row.customerName || '-' }}</span>
            <span class="meta-sep">·</span>
            <span>{{ row.contractYear }} 年</span>
          </div>
          <div class="mobile-card__amount">
            <span class="amount-label">合同金额</span>
            <span class="amount-value">{{ formatMoney(row.totalAmount) }}</span>
            <span class="amount-label amount-label--paid">已收款</span>
            <span class="amount-value amount-value--paid">{{ formatMoney(row.paidAmount) }}</span>
          </div>
          <div class="mobile-card__ratio">
            <el-progress
              :percentage="paidRatio(row)"
              :color="ratioColor(paidRatio(row))"
              :stroke-width="8"
              :show-text="false"
              class="ratio-bar"
            />
            <el-tag :type="ratioTagType(paidRatio(row))" size="small" effect="plain" round>
              {{ ratioLabel(paidRatio(row)) }} {{ paidRatio(row) }}%
            </el-tag>
          </div>
        </div>
      </div>

      <div class="pagination-bar">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          :layout="isMobile ? 'total, prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :small="isMobile"
          background
          @size-change="onSizeChange"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, DataAnalysis } from '@element-plus/icons-vue'
import type { EChartsOption } from 'echarts'
import { contractApi, Contract, ContractDimensionAggregate, formatMoney } from '@/api/contract'
import BarChart from '@/components/common/BarChart.vue'
import { useBreakpoint } from '@/composables/useBreakpoint'

type RatioTagType = 'success' | 'primary' | 'warning' | 'danger'

export default defineComponent({
  name: 'InFlightContractsView',
  components: { BarChart },
  setup() {
    const { isMobile } = useBreakpoint()

    const rows = ref<Contract[]>([])
    const loading = ref(false)
    const totalAmount = ref(0)
    const totalPaid = ref(0)

    const byBu = ref<ContractDimensionAggregate[]>([])
    const chartLoading = ref(false)

    const query = reactive({
      year: undefined as number | undefined,
      bu: '',
    })

    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0,
    })

    const currentYear = new Date().getFullYear()
    const yearOptions = [currentYear, currentYear - 1, currentYear - 2, 2019]

    const reload = async () => {
      loading.value = true
      try {
        const res = await contractApi.inFlight({
          page: pagination.page,
          size: pagination.size,
          year: query.year,
          bu: query.bu || undefined,
        })
        rows.value = res.records || []
        pagination.total = res.total || 0
        totalAmount.value = Number(res.totalAmountSum || 0)
        totalPaid.value = Number(res.totalPaidSum || 0)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        rows.value = []
        pagination.total = 0
        totalAmount.value = 0
        totalPaid.value = 0
      } finally {
        loading.value = false
      }
    }

    const onSearch = () => {
      pagination.page = 1
      reload()
      reloadChart()
    }

    const resetQuery = () => {
      query.year = undefined
      query.bu = ''
      pagination.page = 1
      reload()
      reloadChart()
    }

    const onPageChange = (p: number) => {
      pagination.page = p
      reload()
    }

    const onSizeChange = (s: number) => {
      pagination.size = s
      pagination.page = 1
      reload()
    }

    /** 图表数据(只随 year 筛选变化,与分页解耦) */
    const reloadChart = async () => {
      chartLoading.value = true
      try {
        byBu.value = await contractApi.inFlightByBu(query.year)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载图表数据失败')
        byBu.value = []
      } finally {
        chartLoading.value = false
      }
    }

    const byBuOption = computed<EChartsOption>(() => {
      const bus = byBu.value.map((r) => r.dimension)
      const amounts = byBu.value.map((r) => Number(r.totalAmount || 0))
      const paid = byBu.value.map((r) => Number(r.totalPaid || 0))
      const unpaid = byBu.value.map((_r, i) => Math.max(0, amounts[i] - paid[i]))
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          valueFormatter: (v: any) => formatMoney(Number(v || 0)),
        },
        legend: { data: ['已收款', '未收款'], top: 0 },
        grid: { top: 40, left: 80, right: 30, bottom: 40 },
        xAxis: { type: 'category', data: bus, axisLabel: { interval: 0, rotate: bus.length > 6 ? 30 : 0 } },
        yAxis: {
          type: 'value',
          name: '金额(元)',
          axisLabel: {
            formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : String(v),
          },
        },
        series: [
          {
            name: '已收款',
            type: 'bar',
            stack: 'amount',
            data: paid,
            itemStyle: { color: '#059669' },
            barMaxWidth: 40,
          },
          {
            name: '未收款',
            type: 'bar',
            stack: 'amount',
            data: unpaid,
            itemStyle: { color: '#D97706' },
            barMaxWidth: 40,
          },
        ],
      }
    })

    const paidRatio = (row: Contract): number => {
      if (!row.totalAmount || Number(row.totalAmount) <= 0) return 0
      return Math.min(100, Math.round((Number(row.paidAmount || 0) / Number(row.totalAmount)) * 100))
    }
    const ratioColor = (n: number): string => {
      if (n >= 80) return 'var(--color-success)'
      if (n >= 50) return 'var(--color-primary)'
      if (n >= 20) return 'var(--color-warning)'
      return 'var(--color-error)'
    }
    const ratioTagType = (n: number): RatioTagType => {
      if (n >= 80) return 'success'
      if (n >= 50) return 'primary'
      if (n >= 20) return 'warning'
      return 'danger'
    }
    const ratioLabel = (n: number): string => {
      if (n >= 80) return '健康'
      if (n >= 50) return '正常'
      if (n >= 20) return '偏低'
      return '告警'
    }

    onMounted(() => {
      reload()
      reloadChart()
    })

    return {
      rows, loading, query, yearOptions, pagination, isMobile,
      reload, resetQuery, onSearch, onPageChange, onSizeChange,
      totalAmount, totalPaid,
      byBu, chartLoading, byBuOption,
      paidRatio, ratioColor, ratioTagType, ratioLabel,
      formatMoney,
      Search, RefreshLeft, DataAnalysis,
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
  margin-bottom: var(--space-4);
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.page-title {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.page-meta {
  color: var(--color-text-tertiary);
  font-size: var(--text-sm);
}

.filter-card,
.table-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);

  :deep(.el-card__body) {
    padding: var(--space-4) var(--space-6);
  }

  :deep(.el-table th.el-table__cell) {
    background: var(--color-bg-soft) !important;
    color: var(--color-text-primary);
    font-weight: var(--weight-semibold);
    font-size: var(--text-sm);
  }
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: var(--space-4);
}

.filter-select { width: 144px; }
.filter-input  { width: 200px; }

.stats-row {
  margin-bottom: var(--space-4);
}

.stat-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);

  :deep(.el-card__body) {
    padding: var(--space-4) var(--space-6);
  }
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-1);
}

.stat-value {
  font-size: var(--text-xl);
  font-weight: var(--weight-bold);
  color: var(--color-primary);
}

.stat-value--success {
  color: var(--color-success);
}

.text-success {
  color: var(--color-success);
  font-weight: var(--weight-semibold);
}

.ratio-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.ratio-bar {
  flex: 1;
  min-width: 0;
}

.ratio-label {
  flex-shrink: 0;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--space-4);
}

.chart-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);

  :deep(.el-card__header) {
    padding: var(--space-3) var(--space-6);
    background: var(--color-bg-soft);
    border-bottom: 1px solid var(--color-border);
  }

  :deep(.el-card__body) {
    padding: var(--space-4) var(--space-6);
  }
}

.chart-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.chart-meta {
  color: var(--color-text-tertiary);
  font-weight: var(--weight-normal);
  font-size: var(--text-sm);
}

/* ========== 移动端卡片列表 ========== */
.card-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  min-height: 120px;
}

.mobile-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.mobile-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-2);
}

.mobile-card__title {
  font-weight: var(--weight-semibold);
  font-size: var(--text-base);
  color: var(--color-text-primary);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mobile-card__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.meta-sep {
  color: var(--color-border-strong);
}

.mobile-card__amount {
  display: grid;
  grid-template-columns: auto 1fr auto 1fr;
  align-items: baseline;
  gap: var(--space-1) var(--space-2);
  font-size: var(--text-sm);

  .amount-label {
    color: var(--color-text-tertiary);
    font-size: var(--text-xs);
  }

  .amount-value {
    font-family: var(--font-mono);
    font-weight: var(--weight-semibold);
    color: var(--color-text-primary);
  }

  .amount-value--paid {
    color: var(--color-success);
  }
}

.mobile-card__ratio {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding-top: var(--space-1);
  border-top: 1px solid var(--color-border);
}

@media (max-width: 768px) {
  .search-form {
    :deep(.el-form-item) {
      width: 100%;
      margin-right: 0 !important;
      margin-bottom: var(--space-2) !important;
    }

    :deep(.el-form-item:last-child) {
      margin-bottom: 0 !important;
    }

    :deep(.el-input),
    :deep(.el-select),
    .filter-select,
    .filter-input {
      width: 100%;
    }
  }

  .page-header {
    flex-wrap: wrap;
  }

  .pagination-bar {
    justify-content: center;
  }
}
</style>
