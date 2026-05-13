<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">已验收项目专题</h2>
      <span class="page-meta">PMO 月度汇报视角 — 含金额 / 时间 / 客户 / BU</span>
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
          <div class="stat-label">已验收项目数</div>
          <div class="stat-value">{{ pagination.total }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">合同总金额</div>
          <div class="stat-value stat-value--success">{{ formatMoney(totalAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">回款总金额</div>
          <div class="stat-value stat-value--success">{{ formatMoney(totalPaid) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never" v-loading="chartLoading">
      <template #header>
        <div class="chart-title">
          <el-icon><DataAnalysis /></el-icon>
          按年度的验收金额与项目数
          <span class="chart-meta">— 不受分页影响,展示全量数据</span>
        </div>
      </template>
      <el-empty v-if="byYear.length === 0 && !chartLoading" description="暂无可视化数据" :image-size="80" />
      <BarChart v-else :option="byYearOption" :height="320" />
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-if="!isMobile" :data="rows" stripe v-loading="loading" empty-text="暂无已验收项目">
        <el-table-column label="合同名称" prop="contractName" min-width="220" />
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="年份" prop="contractYear" width="80" />
        <el-table-column label="合同金额" align="right" min-width="140">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="已收款" align="right" min-width="140">
          <template #default="{ row }">{{ formatMoney(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="验收时间" prop="deliveryTime" min-width="170">
          <template #default="{ row }">{{ row.deliveryTime || '-' }}</template>
        </el-table-column>
      </el-table>

      <!-- 移动端:卡片列表 -->
      <div v-else v-loading="loading" class="card-list">
        <el-empty v-if="rows.length === 0" description="暂无已验收项目" :image-size="80" />
        <div v-for="row in rows" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <span class="mobile-card__title">{{ row.contractName }}</span>
            <span class="mobile-card__year">{{ row.contractYear }} 年</span>
          </div>
          <div class="mobile-card__meta">{{ row.customerName || '-' }}</div>
          <div class="mobile-card__amount">
            <span class="amount-label">合同金额</span>
            <span class="amount-value">{{ formatMoney(row.totalAmount) }}</span>
            <span class="amount-label amount-label--paid">已收款</span>
            <span class="amount-value amount-value--paid">{{ formatMoney(row.paidAmount) }}</span>
          </div>
          <div v-if="row.deliveryTime" class="mobile-card__footer">
            验收时间:{{ row.deliveryTime }}
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

export default defineComponent({
  name: 'AcceptedProjectsView',
  components: { BarChart },
  setup() {
    const { isMobile } = useBreakpoint()

    const rows = ref<Contract[]>([])
    const loading = ref(false)
    const totalAmount = ref(0)
    const totalPaid = ref(0)

    const byYear = ref<ContractDimensionAggregate[]>([])
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
        const res = await contractApi.accepted({
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

    /** 加载按年聚合图表数据(不受分页影响,仅随 bu 筛选变化) */
    const reloadChart = async () => {
      chartLoading.value = true
      try {
        byYear.value = await contractApi.acceptedByYear(query.bu || undefined)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载图表数据失败')
        byYear.value = []
      } finally {
        chartLoading.value = false
      }
    }

    const byYearOption = computed<EChartsOption>(() => {
      const years = byYear.value.map((r) => r.dimension)
      const amounts = byYear.value.map((r) => Number(r.totalAmount || 0))
      const counts = byYear.value.map((r) => Number(r.count || 0))
      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
        },
        legend: { data: ['合同金额', '项目数'], top: 0 },
        grid: { top: 40, left: 60, right: 60, bottom: 40 },
        xAxis: { type: 'category', data: years, axisLabel: { formatter: '{value} 年' } },
        yAxis: [
          {
            type: 'value',
            name: '金额(元)',
            axisLabel: {
              formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : String(v),
            },
          },
          { type: 'value', name: '项目数', minInterval: 1 },
        ],
        series: [
          {
            name: '合同金额',
            type: 'bar',
            data: amounts,
            itemStyle: { color: '#0369A1' },
            barMaxWidth: 40,
          },
          {
            name: '项目数',
            type: 'line',
            yAxisIndex: 1,
            data: counts,
            smooth: true,
            itemStyle: { color: '#059669' },
            lineStyle: { width: 2 },
          },
        ],
      }
    })

    /** 列表筛选/分页变化时,图表仍只随 bu 变 — 跟 totalAmount KPI 一致 */

    onMounted(() => {
      reload()
      reloadChart()
    })

    return {
      rows, loading, query, yearOptions, pagination, isMobile,
      reload, resetQuery, onSearch, onPageChange, onSizeChange,
      totalAmount, totalPaid,
      byYear, chartLoading, byYearOption,
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
  color: var(--color-text-primary);
}

.stat-value--success {
  color: var(--color-success);
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

.mobile-card__year {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

.mobile-card__meta {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
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

.mobile-card__footer {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
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
