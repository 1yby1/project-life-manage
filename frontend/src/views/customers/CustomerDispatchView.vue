<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">客户派单</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/customers/list')">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !customer" description="未找到客户(请从客户列表进入)" :image-size="100" />

    <template v-else-if="customer">
      <el-card class="customer-card" shadow="never">
        <div class="customer-row">
          <div class="customer-main">
            <div class="customer-name">{{ customer.customerName }}</div>
            <div class="customer-meta">
              <span><el-icon><Location /></el-icon> {{ customer.city }}</span>
              <span class="meta-sep">·</span>
              <span><el-icon><User /></el-icon> {{ customer.legalPerson }}</span>
            </div>
          </div>
          <el-tag
            :type="visitStageTagType(customer.visitStatus)"
            effect="light"
            round
            size="large"
          >
            {{ visitStatusToStage(customer.visitStatus) }}
          </el-tag>
        </div>
      </el-card>

      <el-alert
        v-if="customer.visitStatus"
        type="warning"
        :closable="false"
        show-icon
        class="alert-banner"
      >
        <template #title>
          <span class="alert-title">该客户已派单</span>
          <span class="alert-desc">
            {{ customer.assignedManagerName ? `已派单至 ${customer.assignedManagerName},无法重复派单` : '无法重复派单' }}
          </span>
        </template>
      </el-alert>

      <template v-if="!customer.visitStatus">
        <el-card class="filter-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Search /></el-icon>
              筛选客户经理
            </div>
          </template>
          <el-form :inline="true" :model="query" class="filter-form">
            <el-form-item label="姓名">
              <el-input v-model="query.name" placeholder="输入姓名" clearable />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="query.phone" placeholder="输入手机号" clearable />
            </el-form-item>
            <el-form-item>
              <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="manager-card" shadow="never" v-loading="loadingManagers">
          <template #header>
            <div class="card-title">
              <el-icon><User /></el-icon>
              可指派的客户经理
              <el-tag size="small" effect="plain" round>{{ filteredManagers.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="filteredManagers.length === 0" description="暂无匹配的客户经理" :image-size="80" />
          <div v-else class="manager-grid">
            <div v-for="m in filteredManagers" :key="m.id" class="manager-item">
              <div class="manager-info">
                <div class="manager-name">{{ m.realName || m.username }}</div>
                <div class="manager-meta">
                  <span class="phone">{{ m.phone || '—' }}</span>
                  <span v-if="m.email" class="meta-sep">·</span>
                  <span v-if="m.email">{{ m.email }}</span>
                </div>
              </div>
              <el-button type="primary" :icon="Position" size="small" @click="openConfirm(m)">
                派单
              </el-button>
            </div>
          </div>
        </el-card>
      </template>
    </template>

    <el-dialog v-model="confirmOpen" title="确认派单" width="460px">
      <div class="dialog-text">
        确认将客户「<strong>{{ customer && customer.customerName }}</strong>」
        派单至客户经理「<strong>{{ pendingManager && (pendingManager.realName || pendingManager.username) }}</strong>」?
      </div>
      <div class="dialog-warn">派单后不可撤回</div>
      <template #footer>
        <el-button @click="confirmOpen = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="doDispatch">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location, User, Search, RefreshLeft, Position } from '@element-plus/icons-vue'
import { customerApi, Customer } from '@/api/customer'
import { customerVisitApi } from '@/api/customerVisit'
import { userApi, AdminUser } from '@/api/user'
import { visitStatusToStage, visitStageTagType } from '@/utils/visitRecord'

export default defineComponent({
  name: 'CustomerDispatchView',
  components: { ArrowLeft, Location, User, Search, RefreshLeft, Position },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const customerId = Number(route.query.customerId || 0)
    const customer = ref<Customer | null>(null)
    const loading = ref(false)

    const managers = ref<AdminUser[]>([])
    const loadingManagers = ref(false)

    const query = reactive({ name: '', phone: '' })

    const filteredManagers = computed(() =>
      managers.value.filter((m) => {
        const name = (m.realName || m.username || '')
        const okName = query.name ? name.includes(query.name) : true
        const okPhone = query.phone ? (m.phone || '').includes(query.phone) : true
        return okName && okPhone
      }),
    )

    const resetQuery = () => {
      query.name = ''
      query.phone = ''
    }

    const confirmOpen = ref(false)
    const pendingManager = ref<AdminUser | null>(null)
    const dispatching = ref(false)

    const openConfirm = (m: AdminUser) => {
      pendingManager.value = m
      confirmOpen.value = true
    }

    const loadCustomer = async () => {
      if (!customerId) return
      loading.value = true
      try {
        customer.value = await customerApi.detail(customerId)
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户失败')
        customer.value = null
      } finally {
        loading.value = false
      }
    }

    const loadManagers = async () => {
      loadingManagers.value = true
      try {
        managers.value = await userApi.listCustomerManagers()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户经理列表失败')
        managers.value = []
      } finally {
        loadingManagers.value = false
      }
    }

    const doDispatch = async () => {
      if (!customer.value || !pendingManager.value) return
      dispatching.value = true
      try {
        await customerVisitApi.dispatch({
          customerId: customer.value.id,
          managerId: pendingManager.value.id,
        })
        ElMessage.success('派单成功')
        confirmOpen.value = false
        router.push('/customers/list')
      } catch (e: any) {
        ElMessage.error(e?.message || '派单失败')
      } finally {
        dispatching.value = false
      }
    }

    onMounted(() => {
      loadCustomer()
      loadManagers()
    })

    return {
      customer,
      loading,
      managers,
      loadingManagers,
      query,
      filteredManagers,
      resetQuery,
      confirmOpen,
      pendingManager,
      dispatching,
      openConfirm,
      doDispatch,
      visitStatusToStage,
      visitStageTagType,
      ArrowLeft, Location, User, Search, RefreshLeft, Position,
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

.customer-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 20px 24px;
  }
}

.customer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.customer-main {
  flex: 1;
  min-width: 0;
}

.customer-name {
  font-size: 16px;
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
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.meta-sep {
  color: #CBD5E1;
}

.alert-banner {
  margin-bottom: 16px;
  border-radius: 10px;
}

.alert-title {
  font-weight: 600;
  margin-right: 8px;
}

.alert-desc {
  font-weight: 400;
  font-size: 13px;
}

.filter-card,
.manager-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
}

.filter-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 16px;
  }

  :deep(.el-input) {
    width: 200px;
  }
}

.manager-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.manager-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 14px 16px;
  transition: all 0.15s;

  &:hover {
    border-color: #0369A1;
    background: #F0F9FF;
  }
}

.manager-info {
  flex: 1;
  min-width: 0;
}

.manager-name {
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
  margin-bottom: 4px;
}

.manager-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748B;
}

.phone {
  font-family: 'SF Mono', Menlo, Consolas, monospace;
}

.dialog-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;

  strong {
    color: #0F172A;
    font-weight: 600;
  }
}

.dialog-warn {
  font-size: 12px;
  color: #D97706;
  margin-top: 8px;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;

  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #E2E8F0;
  padding: 16px 20px;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #E2E8F0;
  padding: 12px 20px;
}
</style>
