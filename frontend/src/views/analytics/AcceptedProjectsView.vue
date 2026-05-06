<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">已验收项目专题</h2>
      <span class="page-meta">PMO 月度汇报视角 — 含金额 / 时间 / 客户 / BU</span>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="年份">
          <el-select v-model="query.year" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="BU">
          <el-input v-model="query.bu" placeholder="所属业务单元" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">已验收项目数</div>
          <div class="stat-value">{{ rows.length }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">合同总金额</div>
          <div class="stat-value">{{ formatMoney(totalAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-label">回款总金额</div>
          <div class="stat-value">{{ formatMoney(totalPaid) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe v-loading="loading" empty-text="暂无已验收项目">
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
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { contractApi, Contract, formatMoney } from '@/api/contract'

export default defineComponent({
  name: 'AcceptedProjectsView',
  setup() {
    const rows = ref<Contract[]>([])
    const loading = ref(false)

    const query = reactive({
      year: undefined as number | undefined,
      bu: '',
    })

    const currentYear = new Date().getFullYear()
    const yearOptions = [currentYear, currentYear - 1, currentYear - 2, 2019]

    const reload = async () => {
      loading.value = true
      try {
        rows.value = await contractApi.accepted(query.year, query.bu || undefined)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        rows.value = []
      } finally {
        loading.value = false
      }
    }

    const resetQuery = () => {
      query.year = undefined
      query.bu = ''
      reload()
    }

    const totalAmount = computed(() => rows.value.reduce((s, r) => s + Number(r.totalAmount || 0), 0))
    const totalPaid = computed(() => rows.value.reduce((s, r) => s + Number(r.paidAmount || 0), 0))

    onMounted(reload)

    return {
      rows, loading, query, yearOptions,
      reload, resetQuery,
      totalAmount, totalPaid,
      formatMoney,
      Search, RefreshLeft,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 16px; display: flex; align-items: baseline; gap: 12px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.page-meta { color: #64748B; font-size: 13px; }
.filter-card, .table-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__body) { padding: 16px 20px; }
  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
  }
}
.search-form :deep(.el-form-item) { margin-bottom: 0; margin-right: 16px; }
.stats-row { margin-bottom: 16px; }
.stat-card {
  border-radius: 12px; border: 1px solid #E2E8F0;
  :deep(.el-card__body) { padding: 16px 20px; }
}
.stat-label { font-size: 13px; color: #64748B; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; color: #059669; }
</style>
