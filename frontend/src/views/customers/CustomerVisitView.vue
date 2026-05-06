<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">客户接触</h2>
      <div class="page-subtitle">走访补录与完成走访锁定。完成走访后数据永久锁定,不可修改</div>
    </div>

    <el-row :gutter="16" class="layout">
      <el-col :xs="24" :md="8" :lg="7">
        <el-card class="list-card" shadow="never" v-loading="loadingList">
          <template #header>
            <div class="card-title">
              <el-icon><User /></el-icon>
              派单客户
              <el-tag size="small" effect="plain" round>{{ visits.length }}</el-tag>
            </div>
          </template>
          <el-empty
            v-if="visits.length === 0"
            description="暂无派单客户(需 OPP_ADMIN 在客户列表派单到我)"
            :image-size="80"
          />
          <div v-else class="customer-list">
            <div
              v-for="v in visits"
              :key="v.id"
              class="customer-item"
              :class="{ active: v.id === selectedVisitId }"
              @click="selectedVisitId = v.id"
            >
              <div class="item-row">
                <div class="item-name">{{ v.customerName }}</div>
                <el-tag
                  size="small"
                  :type="v.status === 'COMPLETED' ? 'success' : v.status === 'DOING' ? '' : 'warning'"
                  effect="light"
                  round
                >
                  {{ statusLabel(v.status) }}
                </el-tag>
              </div>
              <div class="item-meta">{{ v.customerCity }} · {{ v.customerLegalPerson }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16" :lg="17">
        <el-card class="editor-card" shadow="never">
          <template #header>
            <div class="card-title">
              <el-icon><Edit /></el-icon>
              走访信息
              <span v-if="selectedVisit" class="card-title-meta">— {{ selectedVisit.customerName }}</span>
              <div class="header-spacer"></div>
              <el-button
                v-if="selectedVisit && selectedVisit.status === 'COMPLETED'"
                type="primary"
                size="default"
                :icon="Edit"
                @click="toEditCustomerInfo"
              >
                去信息完善
              </el-button>
            </div>
          </template>

          <el-empty
            v-if="!selectedVisit"
            description="请选择左侧的派单客户"
            :image-size="100"
          />

          <template v-else>
            <el-alert
              v-if="selectedVisit.status === 'COMPLETED'"
              type="warning"
              :closable="false"
              show-icon
              class="lock-banner"
            >
              <template #title>
                <span style="font-weight: 600;">该客户走访已完成并永久锁定 — 任何字段都不可再修改</span>
              </template>
            </el-alert>

            <el-form :model="form" label-position="top" :disabled="selectedVisit.status === 'COMPLETED'">
              <el-row :gutter="16">
                <el-col :xs="24" :md="12">
                  <el-form-item label="开始时间">
                    <el-date-picker
                      v-model="form.startAt"
                      type="datetime"
                      placeholder="选择开始时间"
                      value-format="YYYY-MM-DDTHH:mm"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="结束时间">
                    <el-date-picker
                      v-model="form.endAt"
                      type="datetime"
                      placeholder="选择结束时间"
                      value-format="YYYY-MM-DDTHH:mm"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="接触方式">
                    <el-input v-model="form.contactMethod" placeholder="例如:上门走访 / 电话沟通" clearable />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="接待人">
                    <el-input v-model="form.host" placeholder="接待人姓名" clearable />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="走访地址">
                    <el-input v-model="form.address" placeholder="填写走访地址" clearable />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="纪要">
                    <el-input
                      v-model="form.minutes"
                      type="textarea"
                      :rows="4"
                      placeholder="会议纪要 / 走访要点"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="结果">
                    <el-input
                      v-model="form.result"
                      type="textarea"
                      :rows="3"
                      placeholder="走访结果 / 下一步计划"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>

            <div v-if="selectedVisit.status !== 'COMPLETED'" class="form-footer">
              <el-button :icon="Document" :loading="saving" @click="saveDraft">暂存</el-button>
              <el-button type="primary" :icon="Lock" :loading="completing" @click="openCompleteConfirm">
                完成走访(锁定)
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Document, Lock } from '@element-plus/icons-vue'
import { customerVisitApi, VisitListItem } from '@/api/customerVisit'
import {
  parseVisitRecord, serializeVisitForm, EMPTY_VISIT_FORM,
} from '@/utils/visitRecord'

export default defineComponent({
  name: 'CustomerVisitView',
  components: { User, Edit, Document, Lock },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const visits = ref<VisitListItem[]>([])
    const loadingList = ref(false)
    const selectedVisitId = ref<number | null>(null)

    const selectedVisit = computed(() =>
      visits.value.find((v) => v.id === selectedVisitId.value) || null,
    )

    const form = reactive({ ...EMPTY_VISIT_FORM })

    const saving = ref(false)
    const completing = ref(false)

    const statusLabel = (s: string) => {
      switch (s) {
        case 'PENDING': return '待走访'
        case 'DOING': return '走访中'
        case 'COMPLETED': return '已完成'
        default: return s
      }
    }

    const syncFormFromVisit = () => {
      if (!selectedVisit.value) {
        Object.assign(form, EMPTY_VISIT_FORM)
        return
      }
      Object.assign(form, parseVisitRecord(selectedVisit.value.visitRecord))
    }

    const loadList = async () => {
      loadingList.value = true
      try {
        const list = await customerVisitApi.myList()
        visits.value = list || []
        const queryCustomerId = Number(route.query.customerId || 0)
        if (queryCustomerId > 0) {
          const match = visits.value.find((v) => v.customerId === queryCustomerId)
          if (match) {
            selectedVisitId.value = match.id
            return
          }
        }
        if (visits.value.length > 0 && selectedVisitId.value == null) {
          selectedVisitId.value = visits.value[0].id
        }
      } catch (e: any) {
        ElMessage.error(e?.message || '加载走访清单失败')
        visits.value = []
      } finally {
        loadingList.value = false
      }
    }

    const validateForComplete = (): string | null => {
      if (!form.startAt || !form.endAt || !form.contactMethod || !form.host || !form.address || !form.minutes) {
        return '请填写开始/结束时间、接触方式、接待人、走访地址、纪要'
      }
      return null
    }

    const saveDraft = async () => {
      if (!selectedVisit.value || selectedVisit.value.status === 'COMPLETED') return
      saving.value = true
      try {
        await customerVisitApi.saveRecord(selectedVisit.value.id, serializeVisitForm(form))
        ElMessage.success('走访暂存成功')
        await loadList()
      } catch (e: any) {
        ElMessage.error(e?.message || '暂存失败')
      } finally {
        saving.value = false
      }
    }

    const openCompleteConfirm = () => {
      if (!selectedVisit.value || selectedVisit.value.status === 'COMPLETED') return
      const err = validateForComplete()
      if (err) {
        ElMessage.error(err)
        return
      }
      ElMessageBox.confirm(
        '完成后走访数据将永久锁定,不可修改。确认完成走访?',
        '确认完成走访',
        { confirmButtonText: '确认锁定', cancelButtonText: '取消', type: 'warning' },
      )
        .then(() => completeVisit())
        .catch(() => undefined)
    }

    const completeVisit = async () => {
      if (!selectedVisit.value) return
      completing.value = true
      try {
        await customerVisitApi.complete(selectedVisit.value.id, serializeVisitForm(form))
        ElMessage.success('完成走访成功:走访数据已永久锁定')
        await loadList()
      } catch (e: any) {
        ElMessage.error(e?.message || '完成走访失败')
      } finally {
        completing.value = false
      }
    }

    const toEditCustomerInfo = () => {
      if (!selectedVisit.value) return
      router.push({ path: '/customers/complete', query: { customerId: String(selectedVisit.value.customerId) } })
    }

    onMounted(loadList)

    watch(selectedVisitId, () => {
      syncFormFromVisit()
    })

    return {
      visits,
      loadingList,
      selectedVisitId,
      selectedVisit,
      form,
      saving,
      completing,
      statusLabel,
      saveDraft,
      openCompleteConfirm,
      toEditCustomerInfo,
      User, Edit, Document, Lock,
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
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #0F172A;
}

.page-subtitle {
  font-size: 13px;
  color: #64748B;
  line-height: 1.6;
}

.list-card,
.editor-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.editor-card :deep(.el-card__body) {
  padding: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
}

.card-title-meta {
  color: #64748B;
  font-weight: 400;
  font-size: 13px;
}

.header-spacer {
  flex: 1;
}

.customer-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 600px;
  overflow-y: auto;
}

.customer-item {
  border: 1px solid #E2E8F0;
  background: #FFFFFF;
  border-radius: 8px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    border-color: #94A3B8;
    background: #F8FAFC;
  }

  &.active {
    border-color: #0369A1;
    background: #F0F9FF;
    box-shadow: 0 0 0 1px #0369A1;
  }
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.item-name {
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.item-meta {
  font-size: 12px;
  color: #64748B;
}

.lock-banner {
  margin-bottom: 16px;
  border-radius: 8px;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 20px;
  border-top: 1px solid #F1F5F9;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;

  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}
</style>
