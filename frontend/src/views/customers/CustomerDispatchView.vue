<template>
  <div class="dispatch-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户派单</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item" @click="$router.push('/customers/list')">客户管理</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">派单</span>
        </div>
      </div>
    </header>

    <el-empty v-if="!customer" description="未找到客户（请从客户列表进入）" />

    <template v-else>
      <!-- 客户信息卡片 -->
      <div class="customer-card">
        <div class="customer-avatar">{{ customer.customerName.charAt(0) }}</div>
        <div class="customer-info">
          <h2 class="customer-name">{{ customer.customerName }}</h2>
          <div class="customer-meta">
            <span><el-icon><Location /></el-icon> {{ customer.city }}</span>
            <span><el-icon><User /></el-icon> {{ customer.legalPerson }}</span>
          </div>
        </div>
        <span class="stage-badge" :class="customer.stage === '已录入' ? 'can-dispatch' : 'dispatched'">
          {{ customer.stage }}
        </span>
      </div>

      <!-- 已派单提示 -->
      <div v-if="customer.stage !== '已录入'" class="alert-card">
        <el-icon :size="24"><WarningFilled /></el-icon>
        <div class="alert-content">
          <span class="alert-title">该客户已派单</span>
          <span class="alert-desc">{{ assignedManagerName ? `已派单至 ${assignedManagerName}，无法重复派单` : '无法重复派单' }}</span>
        </div>
      </div>

      <template v-if="customer.stage === '已录入'">
        <!-- 筛选区域 -->
        <div class="filter-card">
          <h3 class="filter-title">选择客户经理</h3>
          <div class="filter-grid">
            <div class="filter-item">
              <label>部门</label>
              <el-input v-model="query.dept" placeholder="输入部门" clearable />
            </div>
            <div class="filter-item">
              <label>姓名</label>
              <el-input v-model="query.name" placeholder="输入姓名" clearable />
            </div>
            <div class="filter-item">
              <label>手机号</label>
              <el-input v-model="query.phone" placeholder="输入手机号" clearable />
            </div>
            <div class="filter-actions">
              <el-button type="primary" @click="pageIndex = 1">
                <el-icon><Search /></el-icon>
                筛选
              </el-button>
              <el-button @click="resetQuery">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>

        <!-- 客户经理列表 -->
        <div class="manager-list">
          <div v-if="paged.length === 0" class="empty-state">
            <el-icon :size="48"><User /></el-icon>
            <p>暂无匹配的客户经理</p>
          </div>
          <div v-else class="manager-grid">
            <div v-for="emp in paged" :key="emp.id" class="manager-card">
              <div class="manager-avatar">{{ emp.name.charAt(0) }}</div>
              <div class="manager-info">
                <span class="manager-name">{{ emp.name }}</span>
                <span class="manager-dept">{{ emp.dept }}</span>
                <span class="manager-phone">{{ emp.phone }}</span>
              </div>
              <button class="dispatch-btn" @click="openConfirm(emp.id)">
                <el-icon><Position /></el-icon>
                派单
              </button>
            </div>
          </div>
        </div>
      </template>
    </template>

    <!-- 确认弹窗 -->
    <el-dialog v-model="confirmOpen" title="确认派单" width="420px" class="modern-dialog">
      <div class="dialog-content">
        <div class="dialog-icon">
          <el-icon :size="32"><Position /></el-icon>
        </div>
        <p class="dialog-text">
          确认将客户「<strong>{{ customer?.customerName }}</strong>」
          派单至客户经理「<strong>{{ pendingEmployeeName }}</strong>」？
        </p>
        <p class="dialog-warn">⚠️ 派单后不可撤回</p>
      </div>
      <template #footer>
        <el-button @click="confirmOpen = false">取消</el-button>
        <el-button type="primary" @click="doDispatch">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'
import { Location, User, Search, RefreshLeft, Position, WarningFilled } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerDispatchView',
  components: { Location, User, Search, RefreshLeft, Position, WarningFilled },
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const customerId = String(route.query.customerId || '')
    const customer = computed(() => store.customers.find((c) => c.id === customerId))

    const assignedManagerName = computed(() => {
      if (!customer.value?.assignedEmployeeId) return ''
      const emp = store.employees.find((e) => e.id === customer.value!.assignedEmployeeId)
      return emp?.name || ''
    })

    const query = reactive({ dept: '', name: '', phone: '' })
    const pageSize = 8
    const pageIndex = ref(1)

    const managers = computed(() =>
      store.employees.filter((e) => e.roleTag === '客户经理').filter((e) => {
        const okDept = query.dept ? e.dept.includes(query.dept) : true
        const okName = query.name ? e.name.includes(query.name) : true
        const okPhone = query.phone ? e.phone.includes(query.phone) : true
        return okDept && okName && okPhone
      })
    )

    const totalPages = computed(() => Math.max(1, Math.ceil(managers.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return managers.value.slice(start, start + pageSize)
    })

    const resetQuery = () => {
      query.dept = ''
      query.name = ''
      query.phone = ''
      pageIndex.value = 1
    }

    const confirmOpen = ref(false)
    const pendingEmployeeId = ref('')
    const pendingEmployeeName = computed(() => {
      const emp = store.employees.find((e) => e.id === pendingEmployeeId.value)
      return emp?.name || ''
    })

    const openConfirm = (employeeId: string) => {
      pendingEmployeeId.value = employeeId
      confirmOpen.value = true
    }

    const doDispatch = () => {
      if (!customer.value || !pendingEmployeeId.value) return

      store.dispatches.push({
        id: `d_${Date.now()}`,
        customerId: customer.value.id,
        employeeId: pendingEmployeeId.value,
        createdAt: new Date().toISOString(),
      })

      const existing = store.visits.find((v) => v.customerId === customer.value!.id && v.employeeId === pendingEmployeeId.value)
      if (!existing) {
        store.visits.push({
          id: `v_${Date.now()}`,
          customerId: customer.value.id,
          employeeId: pendingEmployeeId.value,
          completed: false,
        })
      }

      customer.value.stage = '已派单'
      customer.value.assignedEmployeeId = pendingEmployeeId.value

      confirmOpen.value = false
      router.push({ path: '/customers/list' })
    }

    return {
      customer,
      assignedManagerName,
      query,
      pageIndex,
      paged,
      totalPages,
      resetQuery,
      confirmOpen,
      pendingEmployeeName,
      openConfirm,
      doDispatch,
    }
  },
})
</script>

<style scoped lang="scss">
.dispatch-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 32px;
}

.page-header { margin-bottom: 32px; }

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px;
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
  &:hover { text-decoration: underline; }
}

.breadcrumb-separator { color: #94A3B8; }
.breadcrumb-current { color: #64748B; }

// 客户卡片
.customer-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.customer-avatar {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #FFFFFF;
}

.customer-info { flex: 1; }

.customer-name {
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
}

.customer-meta {
  display: flex;
  gap: 20px;
  color: #64748B;
  font-size: 14px;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.stage-badge {
  padding: 8px 20px;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  
  &.can-dispatch { background: #DCFCE7; color: #16A34A; }
  &.dispatched { background: #FEF3C7; color: #D97706; }
}

// 警告提示
.alert-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #FEF3C7;
  border: 1px solid #FDE68A;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  color: #92400E;
}

.alert-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.alert-title {
  font-weight: 600;
  font-size: 15px;
}

.alert-desc {
  font-size: 14px;
  opacity: 0.8;
}

// 筛选区域
.filter-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 20px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr) auto;
  gap: 16px;
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  label {
    font-size: 13px;
    font-weight: 500;
    color: #64748B;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #E2E8F0 inset;
    background: #F8FAFC;
    
    &:hover, &.is-focus {
      box-shadow: 0 0 0 2px #3B82F6 inset;
      background: #FFFFFF;
    }
  }
}

.filter-actions {
  display: flex;
  gap: 10px;
  
  .el-button {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    height: auto;
    border-radius: 10px;
    font-weight: 500;
  }
}

// 客户经理列表
.manager-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.manager-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  padding: 20px;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #3B82F6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  }
}

.manager-avatar {
  width: 48px;
  height: 48px;
  background: #E0E7FF;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: #4F46E5;
}

.manager-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.manager-name {
  font-weight: 600;
  color: #1E293B;
}

.manager-dept {
  font-size: 13px;
  color: #64748B;
}

.manager-phone {
  font-size: 13px;
  color: #94A3B8;
  font-family: monospace;
}

.dispatch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #3B82F6;
  color: #FFFFFF;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    background: #2563EB;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94A3B8;
  
  p { margin: 16px 0 0; font-size: 15px; }
}

// 弹窗
.modern-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
  }
  
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #F1F5F9;
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #F1F5F9;
  }
}

.dialog-content {
  text-align: center;
}

.dialog-icon {
  width: 64px;
  height: 64px;
  background: #DBEAFE;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #3B82F6;
}

.dialog-text {
  font-size: 15px;
  color: #475569;
  margin: 0 0 12px;
  line-height: 1.6;
  
  strong { color: #1E293B; }
}

.dialog-warn {
  font-size: 13px;
  color: #DC2626;
  margin: 0;
}

@media (max-width: 768px) {
  .dispatch-page { padding: 20px 16px; }
  .filter-grid { grid-template-columns: 1fr; }
  .manager-grid { grid-template-columns: 1fr; }
}
</style>
