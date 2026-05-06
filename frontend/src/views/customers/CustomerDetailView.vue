<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">客户详情</h2>
      <el-button :icon="ArrowLeft" @click="router.push('/customers/list')">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !customer" description="未找到客户信息" :image-size="100" />

    <template v-else-if="customer">
      <el-card class="overview-card" shadow="never" v-loading="loading">
        <div class="overview-row">
          <div class="overview-main">
            <div class="customer-name">{{ customer.customerName }}</div>
            <div class="customer-meta">
              <span><el-icon><Location /></el-icon> {{ customer.city }}</span>
              <span class="meta-sep">·</span>
              <span><el-icon><User /></el-icon> {{ customer.legalPerson }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-time">录入于 {{ formatDate(customer.createTime) }}</span>
            </div>
          </div>
          <div class="overview-side">
            <el-tag :type="visitStageTagType(customer.visitStatus)" effect="light" round size="large">
              {{ visitStageLabel(customer.visitStatus) }}
            </el-tag>
            <div v-if="customer.assignedManagerName" class="assigned-meta">
              派单至 <strong>{{ customer.assignedManagerName }}</strong>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="tabs-card" shadow="never">
        <el-tabs v-model="activeTab">
          <el-tab-pane name="info">
            <template #label>
              <span class="tab-label"><el-icon><InfoFilled /></el-icon> 基本信息</span>
            </template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="公司地址">{{ customer.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册地址">{{ customer.regAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册机构">{{ customer.regAgency || '-' }}</el-descriptions-item>
              <el-descriptions-item label="统一信用编码">{{ customer.creditCode || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属行业">{{ customer.industry || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属业务单元(BU)">{{ customer.bu || '-' }}</el-descriptions-item>
              <el-descriptions-item label="录入时间">
                {{ formatDateTime(customer.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="最近更新">
                {{ formatDateTime(customer.updateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane name="contacts">
            <template #label>
              <span class="tab-label"><el-icon><User /></el-icon> 联系人</span>
            </template>
            <el-empty
              v-if="!customer.contactName"
              description="暂无联系人信息(可在「客户信息完善」页补充)"
              :image-size="80"
            />
            <div v-else class="contact-grid">
              <div class="contact-item">
                <div class="contact-info">
                  <div class="contact-name">{{ customer.contactName }}</div>
                  <div class="contact-title">{{ customer.contactTitle || '-' }}</div>
                </div>
                <div class="contact-phone">
                  <el-icon><Phone /></el-icon>
                  {{ customer.contactPhone || '-' }}
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { customerApi, Customer } from '@/api/customer'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location, User, InfoFilled, Phone } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerDetailView',
  components: { Location, User, InfoFilled, Phone },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const activeTab = ref('info')

    const customer = ref<Customer | null>(null)
    const loading = ref(false)

    const customerId = Number(route.params.id || route.query.customerId || 0)

    const load = async () => {
      if (!customerId) return
      loading.value = true
      try {
        customer.value = await customerApi.detail(customerId)
      } catch (e: any) {
        customer.value = null
        ElMessage.error(e?.message || '加载客户详情失败')
      } finally {
        loading.value = false
      }
    }

    const visitStageLabel = (s?: string | null): string => {
      switch (s) {
        case 'PENDING': return '已派单'
        case 'DOING': return '走访中'
        case 'COMPLETED': return '走访完成'
        default: return '已录入'
      }
    }

    const visitStageTagType = (s?: string | null): 'info' | 'warning' | 'success' | '' => {
      switch (s) {
        case 'PENDING': return 'warning'
        case 'DOING': return ''
        case 'COMPLETED': return 'success'
        default: return 'info'
      }
    }

    const formatDate = (s?: string) => (s ? new Date(s).toLocaleDateString() : '-')
    const formatDateTime = (s?: string) => (s ? new Date(s).toLocaleString() : '-')

    onMounted(load)

    return {
      customer,
      loading,
      activeTab,
      visitStageLabel,
      visitStageTagType,
      formatDate,
      formatDateTime,
      router,
      ArrowLeft,
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

.overview-card, .tabs-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;
}

.overview-card :deep(.el-card__body) { padding: 18px 24px; }
.tabs-card :deep(.el-card__body) { padding: 12px 20px 20px; }

.overview-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.overview-main { flex: 1; min-width: 0; }
.overview-side { text-align: right; display: flex; flex-direction: column; gap: 6px; align-items: flex-end; }

.customer-name {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin-bottom: 6px;
}

.customer-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748B;
  font-size: 13px;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.meta-sep { color: #CBD5E1; }
.meta-time { color: #94A3B8; }

.assigned-meta {
  font-size: 12px; color: #64748B;
  strong { color: #0F172A; font-weight: 600; }
}

.tab-label { display: inline-flex; align-items: center; gap: 6px; }

.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 14px 16px;
}

.contact-info { min-width: 0; }
.contact-name { font-weight: 600; font-size: 14px; color: #0F172A; }
.contact-title { font-size: 12px; color: #64748B; margin-top: 2px; }
.contact-phone {
  display: inline-flex; align-items: center; gap: 4px;
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 13px;
  color: #475569;
}

:deep(.el-descriptions__label) {
  width: 130px;
  background: #F8FAFC !important;
  color: #475569 !important;
  font-weight: 500 !important;
}
</style>
