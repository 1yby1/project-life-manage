<template>
  <div class="page">
    <div class="page-title">新增合同</div>

    <div class="hint">
      校验规则（按需求落地）：<br />
      1）必填项缺失不允许保存；2）合同金额与支付总金额必须一致；3）合同名称不允许重复；4）合同附件三类均必填。
    </div>

    <div class="card">
      <h3 class="section">1. 基础信息</h3>
      <div class="grid">
        <div class="field">
          <label>合同名称 <span class="req">*</span></label>
          <input v-model="form.contractName" placeholder="例如：星河-软件服务合同-2026" />
        </div>
        <div class="field">
          <label>客户名称 <span class="req">*</span></label>
          <select v-model="form.customerId">
            <option value="">请选择客户</option>
            <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.customerName }}</option>
          </select>
        </div>
      </div>

      <h3 class="section">2. 合同类型与金额</h3>
      <div class="grid">
        <div class="field">
          <label>合同类型 <span class="req">*</span></label>
          <select v-model="form.contractType">
            <option value="">请选择合同类型</option>
            <option value="软件服务">软件服务</option>
            <option value="交付">交付</option>
            <option value="咨询服务">咨询服务</option>
          </select>
        </div>
        <div class="field">
          <label>合同金额（应与支付总金额一致） <span class="req">*</span></label>
          <input v-model.number="form.contractAmount" type="number" placeholder="例如：120000" />
        </div>
      </div>

      <h3 class="section">3. 支付信息</h3>
      <div class="grid">
        <div class="field">
          <label>支付总金额（应与合同金额一致） <span class="req">*</span></label>
          <input v-model.number="form.paymentTotal" type="number" placeholder="例如：120000" />
        </div>
        <div class="field">
          <label>备注</label>
          <input v-model="form.note" placeholder="可选" />
        </div>
      </div>

      <h3 class="section">4. 合同附件</h3>
      <div class="upload-grid">
        <div class="upload-item">
          <label>合同正文（必填）<span class="req">*</span></label>
          <input type="file" @change="onFile($event, 'contractBody')" />
          <div class="file-meta">{{ form.attachments.contractBody || '未上传' }}</div>
        </div>
        <div class="upload-item">
          <label>毛利率分析表（必填）<span class="req">*</span></label>
          <input type="file" @change="onFile($event, 'grossMarginTable')" />
          <div class="file-meta">{{ form.attachments.grossMarginTable || '未上传' }}</div>
        </div>
        <div class="upload-item">
          <label>价格清单表（必填）<span class="req">*</span></label>
          <input type="file" @change="onFile($event, 'priceListTable')" />
          <div class="file-meta">{{ form.attachments.priceListTable || '未上传' }}</div>
        </div>
      </div>

      <div class="footer-actions">
        <button class="btn" type="button" @click="goBack">返回</button>
        <button class="btn btn-primary" type="button" @click="save">保存</button>
      </div>

      <div v-if="message" class="message" :class="{ error: messageType === 'error' }">{{ message }}</div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'

type AttachmentKey = 'contractBody' | 'grossMarginTable' | 'priceListTable'

export default defineComponent({
  name: 'ContractCreateView',
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const customers = computed(() => store.customers)

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const form = reactive({
      contractName: '',
      customerId: '',
      contractType: '',
      contractAmount: 0,
      paymentTotal: 0,
      note: '',
      attachments: {
        contractBody: '',
        grossMarginTable: '',
        priceListTable: '',
      } as Record<AttachmentKey, string>,
    })

    const onFile = (evt: Event, key: AttachmentKey) => {
      const input = evt.target as HTMLInputElement
      const file = input.files?.[0]
      form.attachments[key] = file ? file.name : ''
    }

    const goBack = () => router.push({ path: '/contracts/list' })

    const save = () => {
      // 必填校验
      if (!form.contractName || !form.customerId || !form.contractType) {
        messageType.value = 'error'
        message.value = '请填写合同名称、客户名称、合同类型'
        return
      }
      if (!form.contractAmount || !form.paymentTotal) {
        messageType.value = 'error'
        message.value = '请填写合同金额与支付总金额'
        return
      }
      if (!form.attachments.contractBody || !form.attachments.grossMarginTable || !form.attachments.priceListTable) {
        messageType.value = 'error'
        message.value = '请上传合同正文、毛利率分析表、价格清单表（均为必填）'
        return
      }

      // 唯一性校验：合同名称不重复
      const dup = store.contracts.some((c) => c.contractName === form.contractName)
      if (dup) {
        messageType.value = 'error'
        message.value = '合同名称重复：请更换后再保存'
        return
      }

      // 金额一致性
      if (Number(form.contractAmount) !== Number(form.paymentTotal)) {
        messageType.value = 'error'
        message.value = '合同金额与支付总金额必须一致'
        return
      }

      const amount = Number(form.contractAmount)
      const node1 = Math.round(amount * 0.5)
      const node2 = amount - node1

      store.contracts.push({
        id: `ct_${Date.now()}`,
        contractName: form.contractName,
        customerId: form.customerId,
        contractType: form.contractType,
        contractAmount: amount,
        paymentTotal: Number(form.paymentTotal),
        status: '执行中',
        createdAt: new Date().toISOString(),
        attachments: {
          contractBody: form.attachments.contractBody,
          grossMarginTable: form.attachments.grossMarginTable,
          priceListTable: form.attachments.priceListTable,
        },
        paymentNodes: [
          { id: `pn_${Date.now()}_1`, nodeName: '首付款', amount: node1, dueAt: '', paidAt: '' },
          { id: `pn_${Date.now()}_2`, nodeName: '尾款', amount: node2, dueAt: '', paidAt: '' },
        ],
      })

      messageType.value = 'success'
      message.value = '保存成功：合同已创建（状态=执行中）'
      setTimeout(() => router.push({ path: '/contracts/list' }), 400)
    }

    return {
      customers,
      form,
      onFile,
      save,
      goBack,
      message,
      messageType,
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
.hint {
  color: #475569;
  background: #fff;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  padding: 12px 14px;
  margin-bottom: 12px;
  line-height: 1.7;
  font-size: 13px;
}
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
}
.section {
  font-weight: 900;
  font-size: 13px;
  margin: 14px 0 10px;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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
.field input,
.field select {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
}
.req {
  color: #ef4444;
  font-weight: 700;
}
.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.upload-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f8fafc;
}
.upload-item label {
  display: block;
  font-size: 12px;
  color: #334155;
  margin-bottom: 8px;
}
.file-meta {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
}
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
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
  .grid {
    grid-template-columns: 1fr;
  }
  .upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>

