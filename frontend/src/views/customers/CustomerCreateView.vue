<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">录入新客户</h2>
      <el-button :icon="ArrowLeft" @click="router.push('/customers/list')">返回列表</el-button>
    </div>

    <el-card class="form-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><User /></el-icon>
          客户基本信息
          <span class="card-title-meta">— 带 * 为必填项</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <div class="section-title">必填信息</div>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="客户名称" prop="customerName" required>
              <el-input v-model="form.customerName" placeholder="请输入客户名称" clearable :prefix-icon="OfficeBuilding" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="地市" prop="city" required>
              <el-input v-model="form.city" placeholder="请输入所在地市" clearable :prefix-icon="Location" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="法人" prop="legalPerson" required>
              <el-input v-model="form.legalPerson" placeholder="请输入法人姓名" clearable :prefix-icon="User" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="section-title">补充信息</div>
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="公司地址">
              <el-input v-model="form.address" placeholder="请输入公司地址" clearable :prefix-icon="MapLocation" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="注册地址">
              <el-input v-model="form.regAddress" placeholder="请输入注册地址" clearable :prefix-icon="Postcard" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="注册机构">
              <el-input v-model="form.regAgency" placeholder="请输入注册机构" clearable :prefix-icon="House" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="统一信用编码">
              <el-input v-model="form.creditCode" placeholder="91440300MA5..." clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所属行业">
              <el-input v-model="form.industry" placeholder="例如:软件与信息技术服务" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所属业务单元(BU)">
              <el-input v-model="form.bu" placeholder="可选" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="section-title">联系人(可选)</div>
        <el-row :gutter="16">
          <el-col :xs="24" :md="8">
            <el-form-item label="姓名">
              <el-input v-model="form.contactName" placeholder="联系人姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="职位">
              <el-input v-model="form.contactTitle" placeholder="例如:采购经理" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="13900000000" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="footer-actions">
          <el-button @click="router.push('/customers/list')">取消</el-button>
          <el-button type="primary" :icon="Check" :loading="submitting" @click="submit">确认提交</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { customerApi } from '@/api/customer'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  ArrowLeft, User, Location, OfficeBuilding, MapLocation, Postcard, House, Check,
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerCreateView',
  components: { User },
  setup() {
    const router = useRouter()
    const formRef = ref<FormInstance>()
    const submitting = ref(false)

    const form = reactive({
      customerName: '',
      city: '',
      legalPerson: '',
      address: '',
      regAddress: '',
      regAgency: '',
      creditCode: '',
      industry: '',
      bu: '',
      contactName: '',
      contactTitle: '',
      contactPhone: '',
    })

    const rules: FormRules = {
      customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
      city: [{ required: true, message: '请输入地市', trigger: 'blur' }],
      legalPerson: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
    }

    const submit = async () => {
      if (!formRef.value) return

      try {
        await formRef.value.validate()
      } catch {
        ElMessage.error('请填写客户名称、地市、法人(必填项)')
        return
      }

      submitting.value = true
      try {
        await customerApi.create({
          customerName: form.customerName,
          city: form.city,
          legalPerson: form.legalPerson,
          address: form.address || undefined,
          regAddress: form.regAddress || undefined,
          regAgency: form.regAgency || undefined,
          creditCode: form.creditCode || undefined,
          industry: form.industry || undefined,
          bu: form.bu || undefined,
          contactName: form.contactName || undefined,
          contactTitle: form.contactTitle || undefined,
          contactPhone: form.contactPhone || undefined,
        })
        ElMessage.success('客户录入成功')
        setTimeout(() => router.push('/customers/list'), 400)
      } catch (e: any) {
        ElMessage.error(e.message || '提交失败')
      } finally {
        submitting.value = false
      }
    }

    return {
      form,
      formRef,
      rules,
      submitting,
      submit,
      router,
      ArrowLeft, Location, OfficeBuilding, MapLocation, Postcard, House, Check,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.form-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-card__body) {
    padding: 24px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
}

.card-title-meta {
  color: #64748B;
  font-weight: 400;
  font-size: 13px;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
  margin: 8px 0 16px;
  padding-left: 8px;
  border-left: 3px solid #0369A1;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 20px;
  border-top: 1px solid #F1F5F9;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;

  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}
</style>
