<template>
  <div class="page">
    <div class="page-title">线索收集（临时保存/确认收集）</div>
    <div class="subhint">将线索从“收集”推进到“分发”。确认后进入分发阶段。</div>

    <el-empty v-if="!clue" description="未找到线索" />

    <el-card v-else class="form-card" shadow="never">
      <template #header>
        <span class="section-title">基本信息</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        label-width="auto"
      >
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%">
                <el-option
                  v-for="c in customers"
                  :key="c.id"
                  :label="c.customerName"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="线索名称" prop="clueName">
              <el-input v-model="form.clueName" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="线索描述" prop="clueDesc">
          <el-input
            v-model="form.clueDesc"
            type="textarea"
            :rows="5"
          />
        </el-form-item>

        <el-form-item label="附件（可增补）">
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
          <el-button @click="tempSave">临时保存</el-button>
          <el-button type="primary" @click="confirmCollect">确认收集（进入分发）</el-button>
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
import { useRoute, useRouter } from 'vue-router'
import { useMockStore } from '@/store/mockStore'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

export default defineComponent({
  name: 'ClueCollectView',
  setup() {
    const store = useMockStore()
    const route = useRoute()
    const router = useRouter()

    const clueId = String(route.query.clueId || '')
    const clue = computed(() => store.clues.find((l) => l.id === clueId))

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

    const sync = () => {
      if (!clue.value) return
      form.customerId = clue.value.customerId || ''
      form.clueName = clue.value.clueName
      form.clueDesc = clue.value.clueDesc
      form.attachments = clue.value.attachments ? [...clue.value.attachments] : []
    }

    sync()

    const onFileChange = (file: UploadFile) => {
      if (file.name) {
        form.attachments.push(file.name)
      }
    }

    const tempSave = () => {
      if (!clue.value) return
      clue.value.customerId = form.customerId
      clue.value.customerName = store.customers.find((c) => c.id === form.customerId)?.customerName || clue.value.customerName
      clue.value.clueName = form.clueName
      clue.value.clueDesc = form.clueDesc
      clue.value.attachments = [...form.attachments]
      messageType.value = 'success'
      message.value = '临时保存成功（演示：数据已更新，但阶段未推进）'
    }

    const confirmCollect = async () => {
      if (!clue.value) return
      
      if (formRef.value) {
        try {
          await formRef.value.validate()
        } catch {
          messageType.value = 'error'
          message.value = '请填写客户、线索名称、线索描述'
          return
        }
      }

      if (form.attachments.length === 0) {
        messageType.value = 'error'
        message.value = '确认收集需要至少保留一个附件'
        return
      }

      clue.value.customerId = form.customerId
      clue.value.customerName = store.customers.find((c) => c.id === form.customerId)?.customerName || form.clueName
      clue.value.clueName = form.clueName
      clue.value.clueDesc = form.clueDesc
      clue.value.attachments = [...form.attachments]

      clue.value.stage = '分发'
      messageType.value = 'success'
      message.value = '确认收集成功：线索已进入"分发"阶段'
      setTimeout(() => router.push({ path: '/clues/list' }), 500)
    }

    return {
      clue,
      customers,
      form,
      formRef,
      rules,
      fileList,
      onFileChange,
      tempSave,
      confirmCollect,
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
  color: #0F172A;
}

.subhint {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 16px;
}

.form-card {
  border-radius: 14px;
  border: 1px solid #e5e7eb;

  :deep(.el-card__header) {
    padding: 14px 20px;
    border-bottom: 1px solid #e5e7eb;
    background: #F8FAFC;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.section-title {
  font-weight: 900;
  font-size: 14px;
  color: #0F172A;
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

