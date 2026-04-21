<template>
  <div class="page">
    <div class="page-title">已验收项目专题</div>
    <div class="subhint">项目管理部月度汇报数据视图（演示：使用“执行完成合同”作为已验收项目的近似）。</div>

    <section class="toolbar">
      <div class="field">
        <label>月份（yyyy-mm）</label>
        <input v-model="query.month" type="month" />
      </div>
      <div class="field">
        <label>客户</label>
        <input v-model="query.customerName" placeholder="输入客户名称" />
      </div>
      <div class="field actions">
        <button class="btn btn-primary" type="button" @click="pageIndex = 1">搜索</button>
        <button class="btn" type="button" @click="reset">重置</button>
      </div>
    </section>

    <section class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>客户</th>
            <th>合同</th>
            <th>合同签订金额</th>
            <th>签订时间（月）</th>
            <th>业务归属子公司</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in paged" :key="c.id">
            <td>{{ customerName(c.customerId) }}</td>
            <td>{{ c.contractName }}</td>
            <td>{{ formatMoney(c.contractAmount) }}</td>
            <td>{{ monthOf(c.createdAt) }}</td>
            <td>{{ registeredOrg(c.customerId) }}</td>
          </tr>
          <tr v-if="paged.length === 0">
            <td colspan="5" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button class="btn" type="button" :disabled="pageIndex === 1" @click="pageIndex--">上一页</button>
        <div class="page-meta">第 {{ pageIndex }} 页 / 共 {{ totalPages }} 页</div>
        <button class="btn" type="button" :disabled="pageIndex === totalPages" @click="pageIndex++">下一页</button>
      </div>
    </section>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'AcceptedProjectsView',
  setup() {
    const store = useMockStore()
    const query = reactive({
      month: '',
      customerName: '',
    })

    const pageSize = 10
    const pageIndex = ref(1)

    const completed = computed(() => store.contracts.filter((c) => c.status === '执行完成'))

    const filtered = computed(() => {
      return completed.value.filter((c) => {
        const okMonth = query.month ? monthOf(c.createdAt) === query.month : true
        const okCustomer = query.customerName
          ? (store.customers.find((cc) => cc.id === c.customerId)?.customerName || '').includes(query.customerName)
          : true
        return okMonth && okCustomer
      })
    })

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const monthOf = (iso: string) => {
      const d = new Date(iso)
      const yyyy = d.getFullYear()
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      return `${yyyy}-${mm}`
    }

    const customerName = (customerId: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'
    const registeredOrg = (customerId: string) => store.customers.find((c) => c.id === customerId)?.registeredOrg || '-'
    const formatMoney = (n: number) => `￥${Number(n).toLocaleString()}`

    const reset = () => {
      query.month = ''
      query.customerName = ''
      pageIndex.value = 1
    }

    return {
      query,
      pageIndex,
      totalPages,
      paged,
      reset,
      customerName,
      registeredOrg,
      monthOf,
      formatMoney,
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
  line-height: 1.6;
}
.toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 12px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 12px;
  color: #334155;
}
.field input {
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 10px;
}
.field.actions {
  justify-content: flex-end;
  flex-direction: row;
  align-items: flex-end;
  gap: 10px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}
.btn-primary {
  background: #0369a1;
  color: #fff;
  border-color: #0369a1;
}
.table-wrap {
  background: #fff;
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
  vertical-align: top;
}
.table th {
  background: #f8fafc;
  font-weight: 800;
}
.empty {
  padding: 14px;
  text-align: center;
  color: #64748b;
}
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
}
.page-meta {
  font-size: 12px;
  color: #334155;
}
</style>

