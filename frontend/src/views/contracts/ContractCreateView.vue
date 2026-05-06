<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">创建合同</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/contracts/list')">返回列表</el-button>
    </div>

    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Document /></el-icon>
          合同基本信息
          <span class="card-meta">— 合同名称需全局唯一,合同金额必须等于付款节点之和</span>
        </div>
      </template>

      <el-form :model="form" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="合同名称" required>
              <el-input v-model="form.contractName" placeholder="请输入合同名称(全局唯一)" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="客户" required>
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户"
                filterable
                style="width: 100%"
                :loading="customerLoading"
              >
                <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同类型">
              <el-select v-model="form.contractType" placeholder="选择类型" clearable style="width: 100%">
                <el-option label="服务合同" value="服务合同" />
                <el-option label="产品销售" value="产品销售" />
                <el-option label="解决方案" value="解决方案" />
                <el-option label="运维服务" value="运维服务" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同年份" required>
              <el-input-number v-model="form.contractYear" :min="2019" :max="2099" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同总金额(元)" required>
              <el-input-number v-model="form.totalAmount" :min="0" :step="10000" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24">
            <el-form-item label="合同正文附件">
              <el-upload
                :http-request="customUpload"
                :before-upload="beforeUpload"
                :show-file-list="false"
                accept=".pdf,.doc,.docx"
              >
                <el-button :icon="Upload" :loading="uploading">选择文件上传</el-button>
                <template #tip>
                  <div class="upload-tip">
                    <span v-if="form.fileUrl" class="uploaded">
                      已上传:
                      <el-link :href="form.fileUrl" target="_blank" type="primary">{{ uploadedName || form.fileUrl }}</el-link>
                      <el-button :icon="Delete" link type="danger" size="small" @click="clearUpload">移除</el-button>
                    </span>
                    <span v-else class="upload-hint">
                      推荐 PDF / DOCX,单文件 ≤ 20MB
                    </span>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Money /></el-icon>
          付款节点
          <span class="card-meta">— 已配置 {{ form.paymentNodes.length }} 个,合计 {{ formatMoney(nodesSum) }} <span :class="amountMatchClass">/ {{ formatMoney(form.totalAmount) }}</span></span>
        </div>
      </template>

      <el-table :data="form.paymentNodes" stripe>
        <el-table-column label="节点名称" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.nodeName" placeholder="例如:首付款" />
          </template>
        </el-table-column>
        <el-table-column label="计划金额(元)" width="200">
          <template #default="{ row }">
            <el-input-number v-model="row.planAmount" :min="0" :step="1000" :precision="2" controls-position="right" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="计划日期" width="180">
          <template #default="{ row }">
            <el-date-picker v-model="row.planDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ $index }">
            <el-button type="danger" :icon="Delete" size="small" circle @click="removeNode($index)" />
          </template>
        </el-table-column>
      </el-table>

      <div class="add-node-row">
        <el-button :icon="Plus" @click="addNode">新增付款节点</el-button>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button @click="$router.push('/contracts/list')">取消</el-button>
      <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">保存合同</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, Money, Plus, Delete, Check, Upload } from '@element-plus/icons-vue'
import { contractApi, formatMoney, PaymentNodeRequest } from '@/api/contract'
import { customerApi, Customer } from '@/api/customer'

export default defineComponent({
  name: 'ContractCreateView',
  setup() {
    const router = useRouter()

    const customers = ref<Customer[]>([])
    const customerLoading = ref(false)

    const form = reactive({
      contractName: '',
      customerId: undefined as number | undefined,
      contractType: '',
      contractYear: new Date().getFullYear(),
      totalAmount: 0,
      fileUrl: '',
      paymentNodes: [] as PaymentNodeRequest[],
    })

    const submitting = ref(false)

    /** 文件上传(POST /files/upload, OPP_ADMIN) */
    const uploading = ref(false)
    const uploadedName = ref('')
    const beforeUpload = (file: File): boolean => {
      if (file.size > 20 * 1024 * 1024) {
        ElMessage.error('文件不可超过 20MB')
        return false
      }
      return true
    }
    const customUpload = async ({ file }: { file: File }) => {
      uploading.value = true
      try {
        const fd = new FormData()
        fd.append('file', file)
        fd.append('category', 'contracts')
        const token = window.localStorage.getItem('auth_token') || ''
        const res = await fetch('/api/files/upload', {
          method: 'POST',
          headers: token ? { Authorization: `Bearer ${token}` } : {},
          body: fd,
        })
        const body = await res.json()
        if (res.status === 401 || res.status === 403) {
          ElMessage.error('未登录或无权上传(仅 OPP_ADMIN)')
          return
        }
        if (body.code !== 200) {
          ElMessage.error(body.message || '上传失败')
          return
        }
        form.fileUrl = body.data.url
        uploadedName.value = body.data.originalName
        ElMessage.success('上传成功')
      } catch (e: any) {
        ElMessage.error(e?.message || '上传失败')
      } finally {
        uploading.value = false
      }
    }
    const clearUpload = () => {
      form.fileUrl = ''
      uploadedName.value = ''
    }

    const loadCustomers = async () => {
      customerLoading.value = true
      try {
        const res = await customerApi.list({ page: 1, size: 200 })
        customers.value = res.records || []
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户列表失败')
        customers.value = []
      } finally {
        customerLoading.value = false
      }
    }

    const addNode = () => {
      form.paymentNodes.push({ nodeName: '', planAmount: 0, planDate: undefined })
    }
    const removeNode = (idx: number) => {
      form.paymentNodes.splice(idx, 1)
    }

    const nodesSum = computed(() => form.paymentNodes.reduce((s, n) => s + Number(n.planAmount || 0), 0))
    const amountMatch = computed(() => Math.abs(nodesSum.value - Number(form.totalAmount || 0)) < 0.01)
    const amountMatchClass = computed(() => amountMatch.value ? 'text-success' : 'text-error')

    const submit = async () => {
      if (!form.contractName.trim()) {
        ElMessage.error('请填写合同名称')
        return
      }
      if (!form.customerId) {
        ElMessage.error('请选择客户')
        return
      }
      if (!form.totalAmount || form.totalAmount <= 0) {
        ElMessage.error('请填写合同金额')
        return
      }
      if (form.paymentNodes.length === 0) {
        ElMessage.error('至少添加一个付款节点')
        return
      }
      for (const n of form.paymentNodes) {
        if (!n.nodeName || !n.nodeName.trim()) {
          ElMessage.error('付款节点名称必填')
          return
        }
        if (!n.planAmount || n.planAmount <= 0) {
          ElMessage.error('付款节点金额必须大于 0')
          return
        }
      }
      if (!amountMatch.value) {
        ElMessage.error(`付款节点之和(${formatMoney(nodesSum.value)})必须等于合同总额(${formatMoney(form.totalAmount)})`)
        return
      }

      submitting.value = true
      try {
        await contractApi.create({
          contractName: form.contractName.trim(),
          customerId: form.customerId,
          contractType: form.contractType || undefined,
          contractYear: form.contractYear,
          totalAmount: form.totalAmount,
          fileUrl: form.fileUrl || undefined,
          paymentNodes: form.paymentNodes,
        })
        ElMessage.success('合同创建成功')
        setTimeout(() => router.push('/contracts/list'), 400)
      } catch (e: any) {
        ElMessage.error(e?.message || '创建失败')
      } finally {
        submitting.value = false
      }
    }

    onMounted(() => {
      loadCustomers()
      addNode()
    })

    return {
      customers, customerLoading,
      form, submitting,
      nodesSum, amountMatch, amountMatchClass,
      uploading, uploadedName, beforeUpload, customUpload, clearUpload,
      addNode, removeNode, submit,
      formatMoney,
      ArrowLeft, Document, Money, Plus, Delete, Check, Upload,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1200px; margin: 0 auto; }
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.form-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__header) { padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0; }
  :deep(.el-card__body) { padding: 20px; }
  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
  }
}
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.add-node-row { padding: 12px; }
.footer-actions {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 16px; padding-top: 16px; border-top: 1px solid #E2E8F0;
}
.text-success { color: #059669; font-weight: 600; }
.text-error { color: #DC2626; font-weight: 600; }
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
