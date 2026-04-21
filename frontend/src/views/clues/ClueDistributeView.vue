<template>
  <div class="page">
    <div class="page-title">线索分发（指派客户经理）</div>
    <div class="subhint">将线索从“分发”推进到“培育”。</div>

    <div v-if="!clue" class="empty">未找到线索</div>

    <div v-else class="card">
      <div class="summary">
        <div class="summary-item">
          <span class="label">线索名称</span>
          <span class="value">{{ clue.clueName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">客户</span>
          <span class="value">{{ clue.customerName || '-' }}</span>
        </div>
        <div class="summary-item">
          <span class="label">当前阶段</span>
          <span class="value">{{ clue.stage }}</span>
        </div>
      </div>

      <section class="toolbar">
        <div class="field">
          <label>部门</label>
          <input v-model="query.dept" placeholder="例如：客户部" />
        </div>
        <div class="field">
          <label>姓名</label>
          <input v-model="query.name" placeholder="输入姓名" />
        </div>
        <div class="field">
          <label>手机号</label>
          <input v-model="query.phone" placeholder="输入手机号" />
        </div>
      </section>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>部门</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in filtered" :key="e.id">
              <td>{{ e.dept }}</td>
              <td>{{ e.name }}</td>
              <td>{{ e.phone }}</td>
              <td>
                <button class="btn btn-small btn-primary" type="button" @click="assign(e.id)">指派为培育负责人</button>
              </td>
            </tr>
            <tr v-if="filtered.length === 0">
              <td colspan="4" class="empty">暂无匹配客户经理</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="message" class="message" :class="{ error: messageType === 'error' }">{{ message }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'ClueDistributeView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const clueId = String(route.query.clueId || '')
    const clue = computed(() => store.clues.find((l) => l.id === clueId))

    const query = reactive({
      dept: '',
      name: '',
      phone: '',
    })

    const filtered = computed(() => {
      return store.employees
        .filter((e) => e.roleTag === '客户经理')
        .filter((e) => {
          const okDept = query.dept ? e.dept.includes(query.dept) : true
          const okName = query.name ? e.name.includes(query.name) : true
          const okPhone = query.phone ? e.phone.includes(query.phone) : true
          return okDept && okName && okPhone
        })
    })

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const assign = (employeeId: string) => {
      if (!clue.value) return
      clue.value.assignedCustomerManagerId = employeeId
      clue.value.stage = '培育'

      const emp = store.employees.find((e) => e.id === employeeId)
      messageType.value = 'success'
      message.value = `指派成功：${emp?.name || '客户经理'}，线索已进入“培育”阶段`
      setTimeout(() => router.push({ path: '/clues/list' }), 450)
    }

    return {
      clue,
      query,
      filtered,
      message,
      messageType,
      assign,
    }
  },
})
</script>

<style scoped lang="scss">
.page-title {
  font-weight: 800;
  font-size: 16px;
  margin-bottom: 6px;
}
.subhint {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 12px;
}
.empty {
  padding: 16px;
  color: #64748b;
  background: #fff;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
}
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
}
.summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}
.summary-item {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px;
}
.label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}
.value {
  font-weight: 800;
  font-size: 13px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 200px;
}
.field label {
  font-size: 12px;
  color: #334155;
}
.field input {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
}
.table-wrap {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  font-size: 13px;
}
.table th {
  background: #f8fafc;
  font-weight: 800;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}
.btn-small {
  padding: 7px 10px;
}
.btn-primary {
  background: #0369a1;
  color: #fff;
  border-color: #0369a1;
}
.message {
  margin-top: 12px;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #334155;
}
.message.error {
  border-color: #fca5a5;
  background: #fff1f2;
  color: #b91c1c;
}
@media (max-width: 980px) {
  .summary {
    grid-template-columns: 1fr;
  }
  .field {
    min-width: unset;
    width: 100%;
  }
  .toolbar {
    flex-direction: column;
  }
}
</style>

