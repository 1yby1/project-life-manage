<template>
  <div class="page">
    <div class="page-title">合同看板</div>

    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar-content">
        <div class="field">
          <label>年份筛选（仅影响"正在执行合同"）</label>
          <div class="filter-group">
            <el-button-group>
              <el-button
                :type="yearFilter === 'this' ? 'primary' : 'default'"
                @click="yearFilter = 'this'"
              >
                今年
              </el-button>
              <el-button
                :type="yearFilter === 'last' ? 'primary' : 'default'"
                @click="yearFilter = 'last'"
              >
                去年
              </el-button>
              <el-button
                :type="yearFilter === 'custom' ? 'primary' : 'default'"
                @click="yearFilter = 'custom'"
              >
                指定年份
              </el-button>
            </el-button-group>
            <el-input-number
              v-model="customYear"
              :min="2000"
              :max="2100"
              :controls="false"
              placeholder="例如：2026"
              class="year-input"
            />
          </div>
        </div>
        <div class="actions">
          <el-button type="primary" @click="toAllContracts">更多</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="column-card" shadow="never">
          <template #header>
            <div class="column-header">
              <span class="col-title">正在执行合同</span>
              <el-tag type="primary" effect="plain">共 {{ filteredExecuting.length }} 条</el-tag>
            </div>
          </template>
          <div class="cards">
            <el-card
              v-for="c in filteredExecuting"
              :key="c.id"
              class="contract-card"
              shadow="hover"
            >
              <div class="card-content">
                <div class="drag-handle">
                  <el-icon><Rank /></el-icon>
                </div>
                <div class="card-main">
                  <div class="item-top">
                    <div class="item-name">{{ c.contractName }}</div>
                    <div class="item-amount">{{ formatMoney(c.contractAmount) }}</div>
                  </div>
                  <div class="item-sub">
                    客户：{{ customerName(c.customerId) }} / 类型：{{ c.contractType }}
                    <br />创建：{{ formatDate(c.createdAt) }}
                  </div>
                  <div class="item-actions">
                    <el-button size="small" @click="$router.push(`/contracts/${c.id}`)">预览</el-button>
                    <el-button size="small" @click="toList">查看列表</el-button>
                  </div>
                </div>
              </div>
            </el-card>
            <el-empty v-if="filteredExecuting.length === 0" description="暂无符合条件的数据" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="column-card" shadow="never">
          <template #header>
            <div class="column-header">
              <span class="col-title">执行完成合同</span>
              <el-tag type="success" effect="plain">共 {{ completedContracts.length }} 条</el-tag>
            </div>
          </template>
          <div class="cards">
            <el-card
              v-for="c in completedContracts"
              :key="c.id"
              class="contract-card"
              shadow="hover"
            >
              <div class="card-content">
                <div class="drag-handle">
                  <el-icon><Rank /></el-icon>
                </div>
                <div class="card-main">
                  <div class="item-top">
                    <div class="item-name">{{ c.contractName }}</div>
                    <div class="item-amount">{{ formatMoney(c.contractAmount) }}</div>
                  </div>
                  <div class="item-sub">
                    客户：{{ customerName(c.customerId) }} / 类型：{{ c.contractType }}
                    <br />创建：{{ formatDate(c.createdAt) }}
                  </div>
                  <div class="item-actions">
                    <el-button size="small" @click="$router.push(`/contracts/${c.id}`)">预览</el-button>
                  </div>
                </div>
              </div>
            </el-card>
            <el-empty v-if="completedContracts.length === 0" description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'
import { Rank } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'ContractBoardView',
  components: { Rank },
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const yearFilter = ref<'this' | 'last' | 'custom'>('this')
    const customYear = ref(new Date().getFullYear())

    const now = new Date()
    const currentYear = now.getFullYear()
    const lastYear = currentYear - 1

    const executingContracts = computed(() => store.contracts.filter((c) => c.status === '执行中'))
    const completedContracts = computed(() => store.contracts.filter((c) => c.status === '执行完成'))

    const filteredExecuting = computed(() => {
      const targetYear =
        yearFilter.value === 'this' ? currentYear : yearFilter.value === 'last' ? lastYear : customYear.value
      return executingContracts.value.filter((c) => new Date(c.createdAt).getFullYear() === targetYear)
    })

    const customerName = (customerId: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'

    const formatMoney = (n: number) => `￥${Number(n).toLocaleString()}`
    const formatDate = (s: string) => s ? new Date(s).toLocaleDateString() : '-'

    const toAllContracts = () => {
      router.push({ path: '/contracts/list' })
    }

    const toList = () => router.push({ path: '/contracts/list' })

    return {
      yearFilter,
      customYear,
      executingContracts,
      completedContracts,
      filteredExecuting,
      customerName,
      formatMoney,
      formatDate,
      toAllContracts,
      toList,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  background: #F8FAFC;
  min-height: 100%;
  padding: 16px;
}

.page-title {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 16px;
  color: #0F172A;
}

.toolbar-card {
  margin-bottom: 16px;
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.toolbar-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.year-input {
  width: 120px;
}

.actions {
  display: flex;
  align-items: center;
}

.column-card {
  border-radius: 12px;
  margin-bottom: 12px;
  
  :deep(.el-card__header) {
    padding: 14px 16px;
    background: #F8FAFC;
    border-bottom: 1px solid #E5E7EB;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.col-title {
  font-weight: 700;
  font-size: 15px;
  color: #0F172A;
}

.cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contract-card {
  border-radius: 10px;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(3, 105, 161, 0.15);
    border-color: #0369A1;
  }
  
  :deep(.el-card__body) {
    padding: 14px;
  }
}

.card-content {
  display: flex;
  gap: 12px;
}

.drag-handle {
  display: flex;
  align-items: center;
  color: #94A3B8;
  cursor: grab;
  
  &:hover {
    color: #0369A1;
  }
  
  .el-icon {
    font-size: 18px;
  }
}

.card-main {
  flex: 1;
  min-width: 0;
}

.item-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.item-name {
  font-weight: 600;
  color: #0F172A;
  font-size: 14px;
}

.item-amount {
  font-weight: 700;
  color: #0369A1;
  white-space: nowrap;
  font-size: 14px;
}

.item-sub {
  color: #475569;
  font-size: 13px;
  margin-bottom: 12px;
  line-height: 1.6;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;
  
  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}

:deep(.el-tag--primary) {
  color: #0369A1;
  border-color: #BAE6FD;
  background-color: #F0F9FF;
}

:deep(.el-tag--success) {
  color: #059669;
  border-color: #A7F3D0;
  background-color: #ECFDF5;
}

:deep(.el-empty) {
  padding: 24px 0;
}

@media (max-width: 768px) {
  .toolbar-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-wrap: wrap;
  }
  
  .actions {
    justify-content: flex-end;
  }
}
</style>
