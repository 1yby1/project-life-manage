<template>
  <div class="page">
    <div class="page-title">合同正文预览</div>
    <div v-if="!contract" class="empty">未找到合同</div>

    <div v-else class="card">
      <div class="summary">
        <div class="summary-item">
          <span class="label">合同名称</span>
          <span class="value">{{ contract.contractName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">客户</span>
          <span class="value">{{ customerName(contract.customerId) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">状态</span>
          <span class="value">{{ contract.status }}</span>
        </div>
        <div class="summary-item">
          <span class="label">合同类型</span>
          <span class="value">{{ contract.contractType }}</span>
        </div>
        <div class="summary-item">
          <span class="label">合同金额</span>
          <span class="value">{{ formatMoney(contract.contractAmount) }}</span>
        </div>
      </div>

      <div class="section-title">附件（演示）</div>
      <div class="attachments">
        <div class="att">
          <div class="att-name">合同正文</div>
          <div class="att-meta">{{ contract.attachments.contractBody || '-' }}</div>
          <button class="btn btn-small" type="button" @click="download('合同正文', contract.attachments.contractBody)">下载</button>
        </div>
        <div class="att">
          <div class="att-name">毛利率分析表</div>
          <div class="att-meta">{{ contract.attachments.grossMarginTable || '-' }}</div>
          <button class="btn btn-small" type="button" @click="download('毛利率分析表', contract.attachments.grossMarginTable)">下载</button>
        </div>
        <div class="att">
          <div class="att-name">价格清单表</div>
          <div class="att-meta">{{ contract.attachments.priceListTable || '-' }}</div>
          <button class="btn btn-small" type="button" @click="download('价格清单表', contract.attachments.priceListTable)">下载</button>
        </div>
      </div>

      <div class="section-title">付款节点</div>
      <table class="inner-table">
        <thead>
          <tr>
            <th>节点</th>
            <th>金额</th>
            <th>预计时间</th>
            <th>回款时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pn in contract.paymentNodes" :key="pn.id">
            <td>{{ pn.nodeName }}</td>
            <td>{{ formatMoney(pn.amount) }}</td>
            <td>{{ pn.dueAt || '-' }}</td>
            <td>{{ pn.paidAt || '-' }}</td>
          </tr>
        </tbody>
      </table>

      <div class="footer">
        <button class="btn" type="button" @click="$router.back()">返回</button>
        <button class="btn btn-primary" type="button" @click="goList">返回列表</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'

export default defineComponent({
  name: 'ContractPreviewView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const id = String(route.params.id || '')
    const contract = computed(() => store.contracts.find((c) => c.id === id))

    const customerName = (customerId: string) => store.customers.find((c) => c.id === customerId)?.customerName || '-'
    const formatMoney = (n: number) => `￥${Number(n).toLocaleString()}`

    const download = (name: string, fileName?: string) => {
      window.alert(`${name}（演示）\n当前文件：${fileName || '未上传'}\n后续请替换成真实下载接口。`)
    }

    const goList = () => router.push({ path: '/contracts/list' })

    return {
      contract,
      customerName,
      formatMoney,
      download,
      goList,
    }
  },
})
</script>

<style scoped lang="scss">
.page-title {
  font-weight: 800;
  font-size: 16px;
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
.section-title {
  margin: 14px 0 10px;
  font-weight: 900;
}
.attachments {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}
.att {
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  border-radius: 14px;
  padding: 12px;
}
.att-name {
  font-weight: 800;
  margin-bottom: 6px;
}
.att-meta {
  color: #475569;
  font-size: 13px;
  margin-bottom: 10px;
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
  border-color: #0369a1;
  color: #fff;
}
.btn-small {
  padding: 7px 10px;
}
.inner-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 14px;
}
.inner-table th,
.inner-table td {
  border-bottom: 1px solid #e5e7eb;
  padding: 12px;
  text-align: left;
  font-size: 13px;
}
.inner-table th {
  background: #f8fafc;
  font-weight: 800;
}
.footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
@media (max-width: 980px) {
  .summary {
    grid-template-columns: 1fr;
  }
  .attachments {
    grid-template-columns: 1fr;
  }
}
</style>

