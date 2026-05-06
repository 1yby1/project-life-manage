<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">线索录入</h2>
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
    </div>

    <el-alert type="info" :closable="false" show-icon class="hint-alert">
      <template #title>
        <span style="font-weight: 500;">全员可录入</span> — 客户、线索名称、线索描述为必填项;附件 UI 暂不上传(后续轮次接入文件服务)
      </template>
    </el-alert>

    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Bell /></el-icon>
          线索信息
          <span class="card-title-meta">— 带 * 为必填项</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="客户名称" prop="customerId" required>
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable style="width: 100%" :loading="customersLoading">
                <el-option v-for="c in customers" :key="c.id" :label="`${c.customerName}(${c.city})`" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="线索名称" prop="title" required>
              <el-input v-model="form.title" placeholder="例如:星河-二期需求" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="所属业务单元(BU)">
              <el-input v-model="form.bu" placeholder="可选,例如:政企" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="线索描述" prop="requirement" required>
          <el-input v-model="form.requirement" type="textarea" :rows="5" placeholder="描述线索内容、背景、预期目标等" />
        </el-form-item>

        <el-form-item label="附件(可选,本轮不上传)">
          <el-upload action="#" :auto-upload="false" multiple :on-change="onFileChange" :file-list="fileList" class="upload-wrap">
            <el-button :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="file-meta">{{ form.attachments.length ? form.attachments.join(', ') : '未上传(本轮文件不真实上传)' }}</div>
            </template>
          </el-upload>
        </el-form-item>

        <div class="footer-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { ArrowLeft, Bell, Upload, Check } from '@element-plus/icons-vue'
import { customerApi, Customer } from '@/api/customer'
import { leadApi } from '@/api/lead'

export default defineComponent({
  name: 'ClueCreateView',
  components: { Bell },
  setup() {
    const router = useRouter()

    const customers = ref<Customer[]>([])
    const customersLoading = ref(false)

    const formRef = ref<FormInstance>()
    const fileList = ref<UploadFile[]>([])
    const submitting = ref(false)

    const form = reactive({
      customerId: '' as number | '',
      title: '',
      requirement: '',
      bu: '',
      attachments: [] as string[],
    })

    const rules = reactive<FormRules>({
      customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
      title: [{ required: true, message: '请输入线索名称', trigger: 'blur' }],
      requirement: [{ required: true, message: '请输入线索描述', trigger: 'blur' }],
    })

    const loadCustomers = async () => {
      customersLoading.value = true
      try {
        const res = await customerApi.list({ page: 1, size: 200 })
        customers.value = res.records || []
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户列表失败')
        customers.value = []
      } finally {
        customersLoading.value = false
      }
    }

    const onFileChange = (file: UploadFile) => {
      if (file.name) form.attachments.push(file.name)
    }

    const goBack = () => router.push({ path: '/clues/list' })

    const submit = async () => {
      if (!formRef.value) return
      try {
        await formRef.value.validate()
      } catch {
        ElMessage.error('请填写所有必填项')
        return
      }
      if (typeof form.customerId !== 'number') {
        ElMessage.error('请选择客户')
        return
      }
      submitting.value = true
      try {
        await leadApi.create({
          customerId: form.customerId,
          title: form.title,
          requirement: form.requirement,
          bu: form.bu || undefined,
        })
        ElMessage.success('提交成功:线索已进入「收集」阶段')
        setTimeout(() => router.push({ path: '/clues/list' }), 400)
      } catch (e: any) {
        ElMessage.error(e?.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }

    onMounted(loadCustomers)

    return {
      customers,
      customersLoading,
      form,
      formRef,
      rules,
      fileList,
      submitting,
      onFileChange,
      submit,
      goBack,
      ArrowLeft, Bell, Upload, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.hint-alert { margin-bottom: 16px; border-radius: 10px; }
.form-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  :deep(.el-card__header) { padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0; }
  :deep(.el-card__body) { padding: 24px; }
}
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 14px; color: #0F172A; }
.card-title-meta { color: #64748B; font-weight: 400; font-size: 13px; }
.upload-wrap { width: 100%; }
.file-meta { margin-top: 8px; color: #64748B; font-size: 12px; }
.footer-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; padding-top: 20px; border-top: 1px solid #F1F5F9; }
:deep(.el-button--primary) { background-color: #0369A1; border-color: #0369A1; &:hover { background-color: #0284C7; border-color: #0284C7; } }
</style>
