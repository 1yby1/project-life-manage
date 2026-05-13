<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">创建合同</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/contracts/list')">返回列表</el-button>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><Document /></el-icon>
            合同基本信息
            <span class="card-meta">— 合同名称需全局唯一,合同金额必须等于付款节点之和</span>
          </div>
        </template>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="合同名称" prop="contractName">
              <el-input v-model="form.contractName" placeholder="请输入合同名称(全局唯一)" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="form.customerId"
                placeholder="请选择客户"
                filterable
                class="full-width-control"
                :loading="customerLoading"
              >
                <el-option v-for="c in customers" :key="c.id" :label="c.customerName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同类型">
              <el-select v-model="form.contractType" placeholder="选择类型" clearable class="full-width-control">
                <el-option label="服务合同" value="服务合同" />
                <el-option label="产品销售" value="产品销售" />
                <el-option label="解决方案" value="解决方案" />
                <el-option label="运维服务" value="运维服务" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同年份" prop="contractYear">
              <el-input-number v-model="form.contractYear" :min="2019" :max="2099" controls-position="right" class="full-width-control" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="合同总金额(元)" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :step="10000" :precision="2" controls-position="right" class="full-width-control" />
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
                      <el-link :href="form.fileUrl" target="_blank" type="primary">{{ uploadedName || '附件' }}</el-link>
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
      </el-card>

      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><Money /></el-icon>
            付款节点
            <span class="card-meta">
              — 已配置 {{ form.paymentNodes.length }} 个,合计 {{ formatMoney(nodesSum) }}
              <span :class="amountMatchClass">/ {{ formatMoney(form.totalAmount) }}</span>
              <el-icon v-if="amountMatch" class="match-icon match-icon--ok"><CircleCheckFilled /></el-icon>
              <el-icon v-else class="match-icon match-icon--err"><WarningFilled /></el-icon>
            </span>
          </div>
        </template>

        <el-table :data="form.paymentNodes" stripe>
          <el-table-column label="节点名称" min-width="200">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`paymentNodes.${$index}.nodeName`"
                :rules="nodeNameRule"
                class="row-form-item"
              >
                <el-input v-model="row.nodeName" placeholder="例如:首付款" />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="计划金额(元)" width="220">
            <template #default="{ row, $index }">
              <el-form-item
                :prop="`paymentNodes.${$index}.planAmount`"
                :rules="planAmountRule"
                class="row-form-item"
              >
                <el-input-number
                  v-model="row.planAmount"
                  :min="0"
                  :step="1000"
                  :precision="2"
                  controls-position="right"
                  class="full-width-control"
                />
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="计划日期" width="180">
            <template #default="{ row }">
              <el-date-picker v-model="row.planDate" type="date" value-format="YYYY-MM-DD" class="full-width-control" />
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
        <div v-if="form.paymentNodes.length === 0" class="nodes-empty-hint">
          <el-icon><WarningFilled /></el-icon>
          至少添加一个付款节点
        </div>
        <div v-else-if="!amountMatch" class="nodes-amount-hint">
          <el-icon><WarningFilled /></el-icon>
          付款节点之和({{ formatMoney(nodesSum) }})必须等于合同总额({{ formatMoney(form.totalAmount) }})
        </div>
      </el-card>
    </el-form>

    <div class="footer-actions">
      <el-button @click="$router.push('/contracts/list')">取消</el-button>
      <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">保存合同</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { ArrowLeft, Document, Money, Plus, Delete, Check, Upload, CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue'
import { contractApi, formatMoney, PaymentNodeRequest } from '@/api/contract'
import { customerApi, Customer } from '@/api/customer'
import { fileApi } from '@/api/file'

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

    const formRef = ref<FormInstance>()
    const submitting = ref(false)

    const rules: FormRules = {
      contractName: [
        { required: true, message: '请填写合同名称', trigger: 'blur' },
      ],
      customerId: [
        { required: true, message: '请选择客户', trigger: 'change' },
      ],
      contractYear: [
        { required: true, message: '请填写合同年份', trigger: 'blur' },
      ],
      totalAmount: [
        { required: true, message: '请填写合同金额', trigger: 'blur' },
        {
          validator: (_r, value, cb) => {
            if (!value || Number(value) <= 0) cb(new Error('合同金额必须大于 0'))
            else cb()
          },
          trigger: 'blur',
        },
      ],
    }

    /** 付款节点字段级 inline 校验规则(动态数组项共用) */
    const nodeNameRule = [
      { required: true, message: '节点名称必填', trigger: 'blur' },
    ]
    const planAmountRule = [
      { required: true, message: '金额必填', trigger: 'blur' },
      {
        validator: (_r: any, value: any, cb: (e?: Error) => void) => {
          if (!value || Number(value) <= 0) cb(new Error('金额必须大于 0'))
          else cb()
        },
        trigger: 'blur',
      },
    ]

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
        const result = await fileApi.upload(file, 'contracts')
        form.fileUrl = result.url
        uploadedName.value = result.originalName
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
      const valid = await formRef.value?.validate().catch(() => false)
      if (!valid) {
        ElMessage.error('请检查表单必填项')
        return
      }
      if (form.paymentNodes.length === 0) {
        ElMessage.error('至少添加一个付款节点')
        return
      }
      if (!amountMatch.value) {
        ElMessage.error('付款节点之和必须等于合同总额')
        return
      }

      submitting.value = true
      try {
        await contractApi.create({
          contractName: form.contractName.trim(),
          customerId: form.customerId!,
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
      form, formRef, rules, nodeNameRule, planAmountRule, submitting,
      nodesSum, amountMatch, amountMatchClass,
      uploading, uploadedName, beforeUpload, customUpload, clearUpload,
      addNode, removeNode, submit,
      formatMoney,
      ArrowLeft, Document, Money, Plus, Delete, Check, Upload, CircleCheckFilled, WarningFilled,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.page-title {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.form-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: var(--space-4);

  :deep(.el-card__header) {
    padding: var(--space-3) var(--space-6);
    background: var(--color-bg-soft);
    border-bottom: 1px solid var(--color-border);
  }

  :deep(.el-card__body) {
    padding: var(--space-6);
  }

  :deep(.el-table th.el-table__cell) {
    background: var(--color-bg-soft) !important;
    color: var(--color-text-primary);
    font-weight: var(--weight-semibold);
    font-size: var(--text-sm);
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--weight-semibold);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.card-meta {
  color: var(--color-text-tertiary);
  font-weight: var(--weight-normal);
  font-size: var(--text-sm);
}

.match-icon {
  margin-left: var(--space-1);
  vertical-align: middle;

  &--ok  { color: var(--color-success); }
  &--err { color: var(--color-error); }
}

.add-node-row {
  padding: var(--space-3);
}

/* 表格内 form-item:去掉默认下边距,让错误提示紧贴在控件下方而不撑高行 */
.row-form-item {
  margin-bottom: 0;

  :deep(.el-form-item__error) {
    position: relative;
    padding-top: var(--space-1);
  }
}

.nodes-empty-hint,
.nodes-amount-hint {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--color-error);
  padding: 0 var(--space-3) var(--space-2);
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.full-width-control {
  width: 100%;
}

.upload-tip {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}

.text-success {
  color: var(--color-success);
  font-weight: var(--weight-semibold);
}

.text-error {
  color: var(--color-error);
  font-weight: var(--weight-semibold);
}
</style>
