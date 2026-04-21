<template>
  <div class="detail-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户详情</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item" @click="router.push('/customers/list')">客户管理</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">详情</span>
        </div>
      </div>
      <el-button class="back-btn" @click="router.push('/customers/list')">
        <el-icon><Back /></el-icon>
        返回列表
      </el-button>
    </header>

    <el-empty v-if="!customer" description="未找到客户信息" />

    <template v-else>
      <!-- 客户概览卡片 -->
      <div class="overview-card">
        <div class="overview-left">
          <div class="customer-avatar">
            {{ customer.customerName.charAt(0) }}
          </div>
          <div class="customer-main">
            <h2 class="customer-name">{{ customer.customerName }}</h2>
            <div class="customer-meta">
              <span class="meta-item">
                <el-icon><Location /></el-icon>
                {{ customer.city }}
              </span>
              <span class="meta-divider">|</span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ customer.legalPerson }}
              </span>
            </div>
          </div>
        </div>
        <div class="overview-right">
          <span class="stage-badge" :class="getStageClass(customer.stage)">
            {{ customer.stage }}
          </span>
        </div>
      </div>

      <!-- 标签页内容 -->
      <div class="tabs-container">
        <div class="tabs-header">
          <button 
            v-for="tab in tabs" 
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <el-icon><component :is="tab.icon" /></el-icon>
            {{ tab.label }}
          </button>
        </div>

        <div class="tabs-content">
          <!-- 基本信息 -->
          <div v-if="activeTab === 'info'" class="tab-panel">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">公司地址</span>
                <span class="info-value">{{ customer.address || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">注册地址</span>
                <span class="info-value">{{ customer.registeredAddress || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">注册机构</span>
                <span class="info-value">{{ customer.registeredOrg || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">统一信用编码</span>
                <span class="info-value">{{ customer.unifyCreditCode || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">所属行业</span>
                <span class="info-value">{{ customer.industry || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">录入时间</span>
                <span class="info-value">{{ customer.createdAt ? new Date(customer.createdAt).toLocaleString() : '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 联系人 -->
          <div v-if="activeTab === 'contacts'" class="tab-panel">
            <div v-if="!customer.contacts || customer.contacts.length === 0" class="empty-state">
              <el-icon :size="48"><User /></el-icon>
              <p>暂无联系人信息</p>
            </div>
            <div v-else class="contacts-grid">
              <div v-for="ct in customer.contacts" :key="ct.id" class="contact-card">
                <div class="contact-avatar">{{ ct.name.charAt(0) }}</div>
                <div class="contact-info">
                  <span class="contact-name">{{ ct.name }}</span>
                  <span class="contact-title">{{ ct.title }}</span>
                </div>
                <span class="contact-phone">{{ ct.phone }}</span>
              </div>
            </div>
          </div>

          <!-- 派单记录 -->
          <div v-if="activeTab === 'dispatch'" class="tab-panel">
            <div v-if="dispatchRecords.length === 0" class="empty-state">
              <el-icon :size="48"><Document /></el-icon>
              <p>暂无派单记录</p>
            </div>
            <div v-else class="timeline">
              <div v-for="d in dispatchRecords" :key="d.id" class="timeline-item">
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <span class="timeline-title">派单至 {{ d.employeeName }}</span>
                    <span class="timeline-time">{{ new Date(d.createdAt).toLocaleString() }}</span>
                  </div>
                  <div class="timeline-body">
                    <span class="timeline-tag">{{ d.dept }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 走访记录 -->
          <div v-if="activeTab === 'visit'" class="tab-panel">
            <div v-if="visitRecords.length === 0" class="empty-state">
              <el-icon :size="48"><MapLocation /></el-icon>
              <p>暂无走访记录</p>
            </div>
            <div v-else class="timeline">
              <div v-for="v in visitRecords" :key="v.id" class="timeline-item">
                <div class="timeline-dot" :class="{ completed: v.completed }"></div>
                <div class="timeline-content">
                  <div class="timeline-header">
                    <span class="timeline-title">{{ v.employeeName }} 走访</span>
                    <span class="timeline-time">{{ v.startAt ? new Date(v.startAt).toLocaleString() : '-' }}</span>
                  </div>
                  <div class="timeline-body">
                    <span class="timeline-meta">接触方式: {{ v.contactMethod || '-' }}</span>
                    <span class="visit-status" :class="{ completed: v.completed }">
                      {{ v.completed ? '已完成' : '进行中' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'
import type { CustomerStage } from '@/store/mockStore'
import { Back, Location, User, Document, MapLocation, Tickets, InfoFilled } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerDetailView',
  components: { Back, Location, User, Document, MapLocation, Tickets, InfoFilled },
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()
    const activeTab = ref('info')

    const tabs = [
      { key: 'info', label: '基本信息', icon: 'InfoFilled' },
      { key: 'contacts', label: '联系人', icon: 'User' },
      { key: 'dispatch', label: '派单记录', icon: 'Document' },
      { key: 'visit', label: '走访记录', icon: 'MapLocation' },
    ]

    const customerId = String(route.params.id || '')
    const customer = computed(() => store.customers.find((c) => c.id === customerId))

    const dispatchRecords = computed(() => {
      return store.dispatches
        .filter((d) => d.customerId === customerId)
        .map((d) => {
          const emp = store.employees.find((e) => e.id === d.employeeId)
          return {
            id: d.id,
            employeeName: emp?.name || '-',
            dept: emp?.dept || '-',
            createdAt: d.createdAt,
          }
        })
    })

    const visitRecords = computed(() => {
      return store.visits
        .filter((v) => v.customerId === customerId)
        .map((v) => {
          const emp = store.employees.find((e) => e.id === v.employeeId)
          return {
            id: v.id,
            employeeName: emp?.name || '-',
            contactMethod: v.contactMethod,
            startAt: v.startAt,
            endAt: v.endAt,
            completed: v.completed,
          }
        })
    })

    const getStageClass = (stage: CustomerStage) => {
      const map: Record<CustomerStage, string> = {
        '已录入': 'stage-entered',
        '已派单': 'stage-dispatched',
        '走访中': 'stage-visiting',
        '走访完成': 'stage-completed',
      }
      return map[stage] || ''
    }

    return { customer, dispatchRecords, visitRecords, getStageClass, router, activeTab, tabs }
  },
})
</script>

<style scoped lang="scss">
.detail-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}

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

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 500;
  color: #64748B;
  border-color: #E2E8F0;
}

// 概览卡片
.overview-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
}

.overview-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.customer-avatar {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #FFFFFF;
}

.customer-name {
  font-size: 24px;
  font-weight: 700;
  color: #FFFFFF;
  margin: 0 0 8px;
}

.customer-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #94A3B8;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-divider { color: #475569; }

.stage-badge {
  padding: 8px 20px;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  
  &.stage-entered { background: #F1F5F9; color: #475569; }
  &.stage-dispatched { background: #FEF3C7; color: #D97706; }
  &.stage-visiting { background: #DBEAFE; color: #2563EB; }
  &.stage-completed { background: #DCFCE7; color: #16A34A; }
}

// 标签页
.tabs-container {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 500;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover { color: #3B82F6; }
  
  &.active {
    color: #3B82F6;
    background: #FFFFFF;
    border-bottom: 2px solid #3B82F6;
    margin-bottom: -1px;
  }
}

.tabs-content {
  padding: 24px;
}

// 基本信息
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
}

.info-label {
  font-size: 13px;
  color: #64748B;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
}

// 联系人
.contacts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.contact-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #3B82F6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  }
}

.contact-avatar {
  width: 44px;
  height: 44px;
  background: #3B82F6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-weight: 600;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.contact-name {
  font-weight: 600;
  color: #1E293B;
}

.contact-title {
  font-size: 13px;
  color: #64748B;
}

.contact-phone {
  font-size: 14px;
  color: #475569;
  font-family: monospace;
}

// 时间线
.timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timeline-item {
  display: flex;
  gap: 16px;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  background: #3B82F6;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
  
  &.completed { background: #16A34A; }
}

.timeline-content {
  flex: 1;
  background: #F8FAFC;
  border-radius: 12px;
  padding: 16px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-title {
  font-weight: 600;
  color: #1E293B;
}

.timeline-time {
  font-size: 13px;
  color: #94A3B8;
}

.timeline-body {
  display: flex;
  align-items: center;
  gap: 12px;
}

.timeline-tag {
  background: #E2E8F0;
  color: #475569;
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 13px;
}

.timeline-meta {
  font-size: 14px;
  color: #64748B;
}

.visit-status {
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;
  background: #DBEAFE;
  color: #2563EB;
  
  &.completed {
    background: #DCFCE7;
    color: #16A34A;
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
  
  p {
    margin: 16px 0 0;
    font-size: 15px;
  }
}

@media (max-width: 768px) {
  .detail-page { padding: 20px 16px; }
  .overview-card { flex-direction: column; gap: 20px; align-items: flex-start; }
  .info-grid { grid-template-columns: 1fr; }
  .tabs-header { overflow-x: auto; }
}
</style>
