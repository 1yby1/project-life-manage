<template>
  <div class="page">
    <div class="page-title">线索录入</div>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint-alert"
    >
      全员可录入：客户名称、线索名称、线索描述、附件为必填项（演示校验）。
    </el-alert>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        label-width="auto"
      >
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%">
                <el-option
                  v-for="c in customers"
                  :key="c.id"
                  :label="`${c.customerName}（${c.city}）`"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="线索名称" prop="clueName">
              <el-input v-model="form.clueName" placeholder="例如：星河-二期需求" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="线索描述" prop="clueDesc">
          <el-input
            v-model="form.clueDesc"
            type="textarea"
            :rows="5"
            placeholder="描述线索内容、背景、预期目标等"
          />
        </el-form-item>

        <el-form-item label="附件" prop="attachments">
          <el-upload
            action="#"
            :auto-upload="false"
            multiple
            :on-change="onFileChange"
            :file-list="fileList"
          >
            <el-button type="primary" plain>选择文件</el-button>
            <template #tip>
              <div class="file-meta">
                {{ form.attachments.length ? form.attachments.join(', ') : '未上传' }}
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <div class="footer-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </div>

        <el-alert
          v-if="message"
          :title="message"
          :type="messageType === 'error' ? 'error' : 'success'"
          show-icon
          :closable="false"
          class="message-alert"
        />
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getEmployeeIdByRoleLabel, useMockStore } from '@/store/mockStore'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

export default defineComponent({
  name: 'ClueCreateView',
  setup() {
    const store = useMockStore()
    const router = useRouter()

    const roleLabel = window.localStorage.getItem('demo_role') || '销售人员'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel) || store.employees[0]?.id

    const customers = computed(() => store.customers)
    const formRef = ref<FormInstance>()
    const fileList = ref<UploadFile[]>([])

    const form = reactive({
      customerId: '',
      clueName: '',
      clueDesc: '',
      attachments: [] as string[],
    })

    const rules = reactive<FormRules>({
      customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
      clueName: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
      clueDesc: [{ required: true, message: '请输入线索描述', trigger: 'blur' }],
    })

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const onFileChange = (file: UploadFile) => {
      if (file.name) {
        form.attachments.push(file.name)
      }
    }

    const goBack = () => router.push({ path: '/clues/list' })

    const submit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
      } catch {
        messageType.value = 'error'
        message.value = '请填写所有必填项'
        return
      }

      if (form.attachments.length === 0) {
        messageType.value = 'error'
        message.value = '请上传线索附件'
        return
      }

      const customer = store.customers.find((c) => c.id === form.customerId)
      if (!customer) {
        messageType.value = 'error'
        message.value = '请选择有效的客户'
        return
      }

      store.clues.push({
        id: `l_${Date.now()}`,
        customerId: customer.id,
        customerName: customer.customerName,
        clueName: form.clueName,
        clueDesc: form.clueDesc,
        attachments: [...form.attachments],
        stage: '收集',
        createdByEmployeeId: employeeId || '',
        createdAt: new Date().toISOString(),
      })

      messageType.value = 'success'
      message.value = '提交成功：线索已进入“收集”阶段'
      setTimeout(() => router.push({ path: '/clues/list' }), 400)
    }

    return {
      customers,
      form,
      formRef,
      rules,
      fileList,
      onFileChange,
      submit,
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
  margin-bottom: 12px;
  color: #0F172A;
}

.hint-alert {
  margin-bottom: 16px;
  border-radius: 12px;
  background: #F8FAFC;
}

.form-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 10px;
}

.file-meta {
  margin-top: 8px;
  color: #475569;
  font-size: 13px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;

  &:hover {
    background-color: #0284c7;
    border-color: #0284c7;
  }
}

.message-alert {
  margin-top: 16px;
  border-radius: 12px;
}
</style>

