<template>
  <div class="page">
    <div class="page-title">客户接触（走访补录与完成走访锁定）</div>
    <div class="subhint">
      规则：点击「完成走访」后该客户走访数据锁定，不允许更新。流程不可逆。
    </div>

    <div class="layout">
      <aside class="list">
        <div class="list-title">派单客户</div>
        <div v-if="assignedCustomers.length === 0" class="empty">暂无派单客户（请先在"客户派单"完成指派）</div>
        <div v-else class="list-items">
          <button
            v-for="c in assignedCustomers"
            :key="c.id"
            class="list-item"
            :class="{ active: c.id === selectedCustomerId }"
            type="button"
            @click="selectedCustomerId = c.id"
          >
            <div class="item-name">{{ c.customerName }}</div>
            <div class="item-meta">
              {{ c.city }} / {{ c.legalPerson }}
            </div>
            <div class="item-status">
              <span class="tag" :class="{ locked: c.visitLocked }">
                {{ c.visitLocked ? '已锁定' : '可编辑' }}
              </span>
            </div>
          </button>
        </div>
      </aside>

      <section class="editor">
        <div class="editor-header">
          <div class="editor-title">走访信息</div>
          <div class="editor-actions">
            <button
              v-if="selectedVisit?.completed"
              class="btn"
              type="button"
              @click="toEditCustomerInfo"
            >
              去信息完善
            </button>
          </div>
        </div>

        <div v-if="!selectedVisit" class="empty">未找到对应走访记录</div>
        <div v-else class="form">
          <div class="lock-banner" v-if="selectedVisit.completed">
            本客户走访已完成并永久锁定（不可更新）
          </div>

          <div class="grid">
            <div class="field">
              <label>开始时间</label>
              <input type="datetime-local" v-model="form.startAt" :disabled="selectedVisit.completed" />
            </div>
            <div class="field">
              <label>结束时间</label>
              <input type="datetime-local" v-model="form.endAt" :disabled="selectedVisit.completed" />
            </div>
            <div class="field">
              <label>接触方式</label>
              <input v-model="form.contactMethod" :disabled="selectedVisit.completed" placeholder="例如：上门走访/电话沟通" />
            </div>
            <div class="field">
              <label>接待人</label>
              <input v-model="form.host" :disabled="selectedVisit.completed" placeholder="接待人姓名" />
            </div>
            <div class="field">
              <label>走访地址</label>
              <input v-model="form.address" :disabled="selectedVisit.completed" placeholder="填写走访地址" />
            </div>
            <div class="field">
              <label>纪要</label>
              <textarea v-model="form.minutes" :disabled="selectedVisit.completed" rows="4" placeholder="会议纪要/走访要点" />
            </div>
            <div class="field">
              <label>结果</label>
              <textarea v-model="form.result" :disabled="selectedVisit.completed" rows="3" placeholder="走访结果/下一步计划" />
            </div>
          </div>

          <div class="form-footer">
            <button
              class="btn"
              type="button"
              :disabled="selectedVisit.completed"
              @click="saveDraft"
            >
              暂存
            </button>
            <button
              class="btn btn-primary"
              type="button"
              :disabled="selectedVisit.completed"
              @click="openCompleteConfirm"
            >
              完成走访（锁定）
            </button>
          </div>

          <div v-if="message" class="message" :class="{ error: messageType === 'error' }">
            {{ message }}
          </div>
        </div>
      </section>
    </div>

    <!-- 完成走访确认弹窗 -->
    <Modal :open="confirmOpen" title="确认完成走访" confirmText="确认锁定" @close="confirmOpen = false" @confirm="completeVisit">
      <p>确认完成走访？</p>
      <p class="warn-text">完成后走访数据将永久锁定，不可修改。</p>
    </Modal>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEmployeeIdByRoleLabel, useMockStore } from '@/store/mockStore'
import Modal from '@/components/common/Modal.vue'

export default defineComponent({
  name: 'CustomerVisitView',
  components: { Modal },
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const selectedCustomerId = ref('')
    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')
    const confirmOpen = ref(false)

    const roleLabel = window.localStorage.getItem('demo_role') || '客户经理'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel)

    const assignedCustomers = computed(() => {
      if (!employeeId) return []
      const ids = store.dispatches.filter((d) => d.employeeId === employeeId).map((d) => d.customerId)
      const unique = Array.from(new Set(ids))
      return store.customers.filter((c) => unique.includes(c.id))
    })

    const selectedCustomer = computed(() => store.customers.find((c) => c.id === selectedCustomerId.value))

    const selectedVisit = computed(() => {
      if (!employeeId) return undefined
      return store.visits.find((v) => v.customerId === selectedCustomerId.value && v.employeeId === employeeId)
    })

    const form = reactive({
      startAt: '',
      endAt: '',
      contactMethod: '',
      host: '',
      address: '',
      minutes: '',
      result: '',
    })

    const syncFormFromVisit = () => {
      if (!selectedVisit.value) return
      form.startAt = selectedVisit.value.startAt || ''
      form.endAt = selectedVisit.value.endAt || ''
      form.contactMethod = selectedVisit.value.contactMethod || ''
      form.host = selectedVisit.value.host || ''
      form.address = selectedVisit.value.address || ''
      form.minutes = selectedVisit.value.minutes || ''
      form.result = selectedVisit.value.result || ''
    }

    const setInitialCustomer = () => {
      const qId = String(route.query.customerId || '')
      if (qId && assignedCustomers.value.some((c) => c.id === qId)) {
        selectedCustomerId.value = qId
        return
      }
      if (assignedCustomers.value.length > 0) selectedCustomerId.value = assignedCustomers.value[0].id
    }

    const saveDraft = () => {
      if (!selectedVisit.value || !selectedCustomer.value) return
      if (selectedVisit.value.completed) return

      selectedVisit.value.startAt = form.startAt
      selectedVisit.value.endAt = form.endAt
      selectedVisit.value.contactMethod = form.contactMethod
      selectedVisit.value.host = form.host
      selectedVisit.value.address = form.address
      selectedVisit.value.minutes = form.minutes
      selectedVisit.value.result = form.result

      // 首次暂存时更新阶段为"走访中"
      if (selectedCustomer.value.stage === '已派单') {
        selectedCustomer.value.stage = '走访中'
      }

      messageType.value = 'success'
      message.value = '走访暂存成功（数据已更新，但未锁定）'
    }

    const openCompleteConfirm = () => {
      if (!selectedVisit.value || selectedVisit.value.completed) return

      // 校验必填项
      if (!form.startAt || !form.endAt || !form.contactMethod || !form.host || !form.address || !form.minutes) {
        messageType.value = 'error'
        message.value = '请填写开始/结束时间、接触方式、接待人、走访地址、纪要'
        return
      }

      confirmOpen.value = true
    }

    const completeVisit = () => {
      if (!selectedVisit.value || !selectedCustomer.value) return
      if (selectedVisit.value.completed) return

      selectedVisit.value.startAt = form.startAt
      selectedVisit.value.endAt = form.endAt
      selectedVisit.value.contactMethod = form.contactMethod
      selectedVisit.value.host = form.host
      selectedVisit.value.address = form.address
      selectedVisit.value.minutes = form.minutes
      selectedVisit.value.result = form.result
      selectedVisit.value.completed = true

      // 客户走访锁定 + 阶段更新
      selectedCustomer.value.visitLocked = true
      selectedCustomer.value.stage = '走访完成'

      confirmOpen.value = false
      messageType.value = 'success'
      message.value = '完成走访成功：该客户走访数据已永久锁定'
    }

    const toEditCustomerInfo = () => {
      if (!selectedCustomer.value) return
      router.push({ path: '/customers/complete', query: { customerId: selectedCustomer.value.id } })
    }

    onMounted(() => {
      setInitialCustomer()
      syncFormFromVisit()
    })

    watch(selectedCustomerId, () => {
      syncFormFromVisit()
      message.value = ''
    })

    return {
      assignedCustomers,
      selectedCustomerId,
      selectedCustomer,
      selectedVisit,
      form,
      saveDraft,
      openCompleteConfirm,
      completeVisit,
      message,
      messageType,
      toEditCustomerInfo,
      confirmOpen,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  padding: 24px;
  background: #f1f5f9;
  min-height: 100vh;
}

.page-title {
  font-weight: 800;
  font-size: 22px;
  margin-bottom: 8px;
  color: #0f172a;
}

.subhint {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 24px;
  line-height: 1.5;
}

.layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
}

.list {
  background: #ffffff;
  border: none;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  height: fit-content;
}

.list-title {
  font-weight: 800;
  font-size: 16px;
  margin-bottom: 16px;
  color: #1e293b;
}

.list-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  text-align: left;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: #cbd5e1;
    transform: translateY(-1px);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  }
}

.list-item.active {
  border-color: #0ea5e9;
  background: #f0f9ff;
  box-shadow: 0 0 0 1px #0ea5e9;
}

.item-name {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 8px;
  color: #0f172a;
}

.item-meta {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 12px;
}

.tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #475569;
}

.tag.locked {
  background: #dbeafe;
  color: #1d4ed8;
}

.editor {
  background: #ffffff;
  border: none;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e2e8f0;
}

.editor-title {
  font-weight: 800;
  font-size: 18px;
  color: #0f172a;
}

.lock-banner {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 600;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.field input,
.field textarea {
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0 16px;
  font-family: inherit;
  font-size: 14px;
  color: #1e293b;
  transition: all 0.2s;
  height: 44px;
  background: #ffffff;

  &:focus {
    outline: none;
    border-color: #0ea5e9;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
  }

  &:disabled {
    background: #f8fafc;
    color: #94a3b8;
    cursor: not-allowed;
  }
}

.field textarea {
  padding: 12px 16px;
  height: auto;
  resize: vertical;
}

.form-footer {
  display: flex;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px dashed #e2e8f0;
}

.btn {
  border: 1px solid #e2e8f0;
  background: #ffffff;
  border-radius: 8px;
  padding: 0 24px;
  height: 44px;
  font-weight: 600;
  font-size: 14px;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #f8fafc;
    border-color: #cbd5e1;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-primary {
  background: #0ea5e9;
  color: #fff;
  border-color: #0ea5e9;

  &:hover:not(:disabled) {
    background: #0284c7;
    border-color: #0284c7;
    box-shadow: 0 4px 6px -1px rgba(2, 132, 199, 0.2);
  }
}

.message {
  margin-top: 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
}

.message.error {
  border-color: #fca5a5;
  background: #fef2f2;
  color: #991b1b;
}

.warn-text {
  color: #dc2626;
  font-size: 13px;
  margin-top: 6px;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
