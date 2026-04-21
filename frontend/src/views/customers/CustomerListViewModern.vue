<template>
  <div class="customer-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户列表</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">客户管理</span>
        </div>
      </div>
      <div class="header-right">
        <div class="role-badge" v-if="isOpportunityManager">
          <span class="role-dot"></span>
          商机管理员
        </div>
        <div class="role-badge secondary" v-if="isCustomerManager">
          <span class="role-dot"></span>
          客户经理
        </div>
        <el-button
          v-if="isOpportunityManager"
          type="primary"
          class="create-btn"
          @click="router.push('/customers/create')"
        >
          <el-icon :size="18"><Plus /></el-icon>
          新建客户
        </el-button>
      </div>
    </header>

    <!-- 搜索筛选区 -->
    <section class="filter-section">
      <div class="filter-grid">
        <div class="filter-item">
          <label class="filter-label">客户名称</label>
          <el-input
            v-model="query.customerName"
            placeholder="搜索客户名称"
            clearable
            class="filter-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">地市</label>
          <el-input
            v-model="query.city"
            placeholder="输入地市"
            clearable
            class="filter-input"
          >
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">法人</label>
          <el-input
            v-model="query.legalPerson"
            placeholder="输入法人"
            clearable
            class="filter-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-item">
          <label class="filter-label">流程阶段</label>
          <el-select
            v-model="query.stage"
            placeholder="全部阶段"
            clearable
            class="filter-select"
          >
            <el-option
              v-for="s in stages"
              :key="s"
              :label="s"
              :value="s"
            />
          </el-select>
        </div>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="pageIndex = 1" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="reset" class="reset-btn">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>
    </section>

    <!-- 数据统计 -->
    <section class="stats-bar">
      <div class="stat-chip">
        <span class="stat-chip-label">全部</span>
        <span class="stat-chip-value">{{ filtered.length }}</span>
      </div>
      <div class="stat-chip" v-for="s in stages" :key="s">
        <span class="stat-chip-label">{{ s }}</span>
        <span class="stat-chip-value">{{ getStageCount(s) }}</span>
      </div>
    </section>

    <!-- 客户列表 -->
    <section class="table-section">
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th class="th-name">客户名称</th>
              <th class="th-city">地市</th>
              <th class="th-legal">法人</th>
              <th class="th-stage">流程阶段</th>
              <th class="th-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in paged" :key="row.id" class="table-row">
              <td class="td-name">
                <div class="customer-info">
                  <div class="customer-avatar">
                    {{ row.customerName.charAt(0) }}
                  </div>
                  <span class="customer-name">{{ row.customerName }}</span>
                </div>
              </td>
              <td class="td-city">
                <span class="city-text">{{ row.city }}</span>
              </td>
              <td class="td-legal">{{ row.legalPerson }}</td>
              <td class="td-stage">
                <span class="stage-badge" :class="getStageClass(row.stage)">
                  {{ row.stage }}
                </span>
              </td>
              <td class="td-actions">
                <div class="action-buttons">
                  <button class="action-btn view" @click="toDetail(row.id)" title="查看详情">
                    <el-icon><View /></el-icon>
                    <span>查看</span>
                  </button>
                  <button 
                    v-if="isOpportunityManager && row.stage === '已录入'"
                    class="action-btn dispatch"
                    @click="toDispatch(row.id)"
                    title="派单"
                  >
                    <el-icon><Position /></el-icon>
                    <span>派单</span>
                  </button>
                  <button 
                    v-if="isCustomerManager && (row.stage === '已派单' || row.stage === '走访中')"
                    class="action-btn visit"
                    @click="toVisits(row.id)"
                    title="走访"
                  >
                    <el-icon><LocationInformation /></el-icon>
                    <span>走访</span>
                  </button>
                  <button 
                    v-if="isCustomerManager && row.stage !== '已录入'"
                    class="action-btn complete"
                    @click="toComplete(row.id)"
                    title="完善信息"
                  >
                    <el-icon><Edit /></el-icon>
                    <span>完善</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 空状态 -->
        <div v-if="paged.length === 0" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="48"><Document /></el-icon>
          </div>
          <p class="empty-title">暂无客户数据</p>
          <p class="empty-desc">点击右上角"新建客户"添加第一个客户</p>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section" v-if="filtered.length > 0">
        <div class="pagination-info">
          共 <strong>{{ filtered.length }}</strong> 条记录，
          当前第 <strong>{{ pageIndex }}</strong> / <strong>{{ totalPages }}</strong> 页
        </div>
        <el-pagination
          v-model:current-page="pageIndex"
          :page-size="pageSize"
          :total="filtered.length"
          layout="prev, pager, next"
          background
          class="custom-pagination"
        />
      </div>
    </section>
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
  Document,
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerListViewModern',
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
    Document,
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

    const pageSize = 10
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

    const getStageCount = (stage: CustomerStage) => {
      return store.customers.filter(c => c.stage === stage).length
    }

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

    const getStageClass = (stage: CustomerStage) => {
      const classMap: Record<CustomerStage, string> = {
        '已录入': 'stage-entered',
        '已派单': 'stage-dispatched',
        '走访中': 'stage-visiting',
        '走访完成': 'stage-completed',
      }
      return classMap[stage] || ''
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
      getStageClass,
      getStageCount,
      router,
    }
  },
})
</script>

<style scoped lang="scss">
// 现代简约设计系统
// Primary: #3B82F6, Success: #10B981, Warning: #F59E0B

.customer-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 32px;
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  letter-spacing: -0.5px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  color: #3B82F6;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

.breadcrumb-separator {
  color: #94A3B8;
}

.breadcrumb-current {
  color: #64748B;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #EFF6FF;
  color: #3B82F6;
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  
  &.secondary {
    background: #F0FDF4;
    color: #10B981;
    
    .role-dot {
      background: #10B981;
    }
  }
}

.role-dot {
  width: 6px;
  height: 6px;
  background: #3B82F6;
  border-radius: 50%;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  height: auto;
  border-radius: 10px;
  font-weight: 500;
  background: #3B82F6;
  border-color: #3B82F6;
  
  &:hover {
    background: #2563EB;
    border-color: #2563EB;
  }
}

// 筛选区域
.filter-section {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.filter-input,
.filter-select {
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #E2E8F0 inset;
    background: #F8FAFC;
    padding: 8px 12px;
    
    &:hover,
    &.is-focus {
      box-shadow: 0 0 0 2px #3B82F6 inset;
      background: #FFFFFF;
    }
  }
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  height: auto;
  border-radius: 10px;
  font-weight: 500;
  background: #3B82F6;
  border-color: #3B82F6;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  height: auto;
  border-radius: 10px;
  font-weight: 500;
  color: #64748B;
  border-color: #E2E8F0;
  background: #FFFFFF;
  
  &:hover {
    color: #475569;
    border-color: #CBD5E1;
  }
}

// 统计条
.stats-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 100px;
  padding: 8px 16px;
  font-size: 13px;
}

.stat-chip-label {
  color: #64748B;
}

.stat-chip-value {
  font-weight: 600;
  color: #1E293B;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 100px;
  font-size: 12px;
}

// 表格区域
.table-section {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  overflow: hidden;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  
  th, td {
    padding: 16px 20px;
    text-align: left;
  }
  
  th {
    background: #F8FAFC;
    font-size: 13px;
    font-weight: 600;
    color: #475569;
    border-bottom: 1px solid #E2E8F0;
    white-space: nowrap;
  }
  
  .th-name { width: 25%; }
  .th-city { width: 15%; }
  .th-legal { width: 15%; }
  .th-stage { width: 15%; }
  .th-actions { width: 30%; }
}

.table-row {
  border-bottom: 1px solid #F1F5F9;
  transition: background 0.15s ease;
  
  &:hover {
    background: #F8FAFC;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-weight: 600;
  font-size: 16px;
}

.customer-name {
  font-weight: 600;
  color: #1E293B;
}

.city-text {
  color: #475569;
}

.td-legal {
  color: #64748B;
}

// 阶段标签
.stage-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  
  &.stage-entered {
    background: #F1F5F9;
    color: #475569;
  }
  
  &.stage-dispatched {
    background: #FEF3C7;
    color: #D97706;
  }
  
  &.stage-visiting {
    background: #DBEAFE;
    color: #2563EB;
  }
  
  &.stage-completed {
    background: #DCFCE7;
    color: #16A34A;
  }
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  
  &.view {
    background: #EFF6FF;
    color: #3B82F6;
    
    &:hover {
      background: #DBEAFE;
    }
  }
  
  &.dispatch {
    background: #F0FDF4;
    color: #16A34A;
    
    &:hover {
      background: #DCFCE7;
    }
  }
  
  &.visit {
    background: #FEF3C7;
    color: #D97706;
    
    &:hover {
      background: #FDE68A;
    }
  }
  
  &.complete {
    background: #F1F5F9;
    color: #475569;
    
    &:hover {
      background: #E2E8F0;
    }
  }
}

// 空状态
.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  color: #CBD5E1;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

// 分页
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid #F1F5F9;
}

.pagination-info {
  font-size: 14px;
  color: #64748B;
  
  strong {
    color: #1E293B;
  }
}

.custom-pagination {
  :deep(.el-pager li) {
    border-radius: 8px;
    font-weight: 500;
    
    &.is-active {
      background: #3B82F6;
    }
  }
  
  :deep(.btn-prev),
  :deep(.btn-next) {
    border-radius: 8px;
  }
}

// 响应式
@media (max-width: 1024px) {
  .filter-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .customer-page {
    padding: 20px 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .filter-grid {
    grid-template-columns: 1fr;
  }
  
  .action-btn span {
    display: none;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
