<template>
  <div class="page">
    <div class="page-title">在途合同专题</div>
    <div class="subhint">面向区总的合同进度视图：用于催流程、催客户签订合同（演示：根据付款节点是否已回款展示进度）。</div>

    <section class="toolbar">
      <div class="field">
        <label>合同名称</label>
        <input v-model="query.contractName" placeholder="输入关键字" />
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
            <th>合同</th>
            <th>客户</th>
            <th>金额</th>
            <th>回款节点</th>
            <th>进度</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in paged" :key="c.id">
            <td>
              <div class="name">{{ c.contractName }}</div>
              <div class="meta">类型：{{ c.contractType }}</div>
            </td>
            <td>{{ customerName(c.customerId) }}</td>
            <td>{{ formatMoney(c.contractAmount) }}</td>
            <td>
              <div class="nodes">
                <div v-for="pn in c.paymentNodes" :key="pn.id" class="node" :class="{ paid: !!pn.paidAt }">
                  {{ pn.nodeName }}：{{ pn.paidAt ? '已回款' : '未回款' }}
                </div>
              </div>
            </td>
            <td>
              <div class="progress">
                <div class="bar-wrap">
                  <div class="bar" :style="{ width: `${progressPct(c)}%` }" />
                </div>
                <div class="pct">{{ progressPct(c) }}%</div>
              </div>
            </td>
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
  name: 'InFlightContractsView',
  setup() {
    const store = useMockStore()

    const query = reactive({
      contractName: '',
      customerName: '',
    })

    const pageSize = 8
    const pageIndex = ref(1)

    const executing = computed(() => store.contracts.filter((c) => c.status === '执行中'))

    const filtered = computed(() => {
      return executing.value.filter((c) => {
        const okContract = query.contractName ? c.contractName.includes(query.contractName) : true
        const okCustomer = query.customerName
          ? (store.customers.find((cc) => cc.id === c.customerId)?.customerName || '').includes(query.customerName)
          : true
        return okContract && okCustomer
      })
    })

    const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
    const paged = computed(() => {
      const start = (pageIndex.value - 1) * pageSize
      return filtered.value.slice(start, start + pageSize)
    })

    const customerName = (customerId: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'
    const formatMoney = (n: number) => `￥${Number(n).toLocaleString()}`

    const progressPct = (c: any) => {
      const total = c.paymentNodes.reduce((sum: number, pn: any) => sum + Number(pn.amount), 0) || 1
      const paid = c.paymentNodes.reduce((sum: number, pn: any) => sum + (pn.paidAt ? Number(pn.amount) : 0), 0)
      return Math.round((paid / total) * 100)
    }

    const reset = () => {
      query.contractName = ''
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
      formatMoney,
      progressPct,
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
  text-decoration: none;
  color: inherit;
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
.name {
  font-weight: 900;
  margin-bottom: 6px;
}
.meta {
  color: #64748b;
  font-size: 12px;
}
.nodes {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.node {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #334155;
}
.node.paid {
  background: #ecfdf5;
  border-color: #bbf7d0;
  color: #065f46;
}
.progress {
  display: flex;
  align-items: center;
  gap: 12px;
}
.bar-wrap {
  width: 160px;
  height: 10px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}
.bar {
  height: 100%;
  background: #38bdf8;
}
.pct {
  width: 46px;
  text-align: right;
  font-weight: 900;
  color: #0369a1;
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

