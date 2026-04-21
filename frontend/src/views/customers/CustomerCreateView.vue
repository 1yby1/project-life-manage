<template>
  <div class="create-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">录入新客户</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item" @click="router.push('/customers/list')">客户管理</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">新建客户</span>
        </div>
      </div>
    </header>

    <!-- 表单区域 -->
    <div class="form-container">
      <div class="form-card">
        <div class="card-header">
          <div class="header-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="header-text">
            <h2 class="card-title">客户基本信息</h2>
            <p class="card-desc">请填写客户基础信息，带 * 为必填项</p>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="modern-form"
        >
          <div class="form-grid">
            <el-form-item label="客户名称" prop="customerName" class="form-item">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" clearable>
                <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="地市" prop="city" class="form-item">
              <el-input v-model="form.city" placeholder="请输入所在地市" clearable>
                <template #prefix><el-icon><Location /></el-icon></template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="法人" prop="legalPerson" class="form-item">
              <el-input v-model="form.legalPerson" placeholder="请输入法人姓名" clearable>
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="公司地址" class="form-item">
              <el-input v-model="form.address" placeholder="请输入公司地址（选填）" clearable>
                <template #prefix><el-icon><MapLocation /></el-icon></template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="注册地址" class="form-item">
              <el-input v-model="form.registeredAddress" placeholder="请输入注册地址（选填）" clearable>
                <template #prefix><el-icon><Postcard /></el-icon></template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="注册机构" class="form-item">
              <el-input v-model="form.registeredOrg" placeholder="请输入注册机构（选填）" clearable>
                <template #prefix><el-icon><House /></el-icon></template>
              </el-input>
            </el-form-item>
          </div>
        </el-form>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button class="cancel-btn" @click="router.push('/customers/list')">
            <el-icon><Back /></el-icon>
            取消返回
          </el-button>
          <el-button type="primary" class="submit-btn" @click="submit">
            <el-icon><Check /></el-icon>
            确认提交
          </el-button>
        </div>

        <!-- 消息提示 -->
        <transition name="slide-fade">
          <div v-if="message" class="message-toast" :class="messageType">
            <el-icon v-if="messageType === 'success'"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            <span>{{ message }}</span>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMockStore, getEmployeeIdByRoleLabel } from '@/store/mockStore'
import type { FormInstance, FormRules } from 'element-plus'
import { 
  User, Location, OfficeBuilding, MapLocation, Postcard, House, 
  Back, Check, CircleCheck, CircleClose 
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'CustomerCreateView',
  components: {
    User, Location, OfficeBuilding, MapLocation, Postcard, House,
    Back, Check, CircleCheck, CircleClose
  },
  setup() {
    const store = useMockStore()
    const router = useRouter()
    const formRef = ref<FormInstance>()

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const roleLabel = window.localStorage.getItem('demo_role') || '商机管理员'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel)

    const form = reactive({
      customerName: '',
      city: '',
      legalPerson: '',
      address: '',
      registeredAddress: '',
      registeredOrg: '',
    })

    const rules: FormRules = {
      customerName: [
        { required: true, message: '请输入客户名称', trigger: 'blur' }
      ],
      city: [
        { required: true, message: '请输入地市', trigger: 'blur' }
      ],
      legalPerson: [
        { required: true, message: '请输入法人姓名', trigger: 'blur' }
      ],
    }

    const submit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
      } catch {
        messageType.value = 'error'
        message.value = '请填写客户名称、地市、法人（必填项）'
        return
      }

      const dup = store.customers.some((c) => c.customerName === form.customerName)
      if (dup) {
        messageType.value = 'error'
        message.value = '客户名称已存在，请勿重复录入'
        return
      }

      store.customers.push({
        id: `c_${Date.now()}`,
        customerName: form.customerName,
        city: form.city,
        legalPerson: form.legalPerson,
        address: form.address || undefined,
        registeredAddress: form.registeredAddress || undefined,
        registeredOrg: form.registeredOrg || undefined,
        visitLocked: false,
        stage: '已录入',
        createdByEmployeeId: employeeId || '',
        createdAt: new Date().toISOString(),
        contacts: [],
      })

      messageType.value = 'success'
      message.value = '客户录入成功'
      setTimeout(() => router.push('/customers/list'), 400)
    }

    return { form, formRef, rules, submit, message, messageType, router }
  },
})
</script>

<style scoped lang="scss">
.create-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 32px;
}

// 页面头部
.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  color: #3B82F6;
  cursor: pointer;
  &:hover { text-decoration: underline; }
}

.breadcrumb-separator { color: #94A3B8; }
.breadcrumb-current { color: #64748B; }

// 表单容器
.form-container {
  max-width: 800px;
}

.form-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 32px;
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #60A5FA;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #FFFFFF;
  margin: 0 0 4px;
}

.card-desc {
  font-size: 14px;
  color: #94A3B8;
  margin: 0;
}

.modern-form {
  padding: 32px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.form-item {
  :deep(.el-form-item__label) {
    font-size: 14px;
    font-weight: 500;
    color: #475569;
    padding-bottom: 8px;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 4px 16px;
    box-shadow: 0 0 0 1px #E2E8F0 inset;
    background: #F8FAFC;
    
    &:hover, &.is-focus {
      box-shadow: 0 0 0 2px #3B82F6 inset;
      background: #FFFFFF;
    }
  }
  
  :deep(.el-input__prefix) {
    color: #94A3B8;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 32px;
  border-top: 1px solid #F1F5F9;
  background: #FAFBFC;
}

.cancel-btn, .submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  height: auto;
  border-radius: 12px;
  font-weight: 500;
  font-size: 15px;
}

.cancel-btn {
  color: #64748B;
  border-color: #E2E8F0;
  background: #FFFFFF;
  
  &:hover {
    color: #475569;
    border-color: #CBD5E1;
    background: #F8FAFC;
  }
}

.submit-btn {
  background: #3B82F6;
  border-color: #3B82F6;
  
  &:hover {
    background: #2563EB;
    border-color: #2563EB;
  }
}

// 消息提示
.message-toast {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 32px 24px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  
  &.success {
    background: #F0FDF4;
    color: #16A34A;
    border: 1px solid #BBF7D0;
  }
  
  &.error {
    background: #FEF2F2;
    color: #DC2626;
    border: 1px solid #FECACA;
  }
}

.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from, .slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .create-page { padding: 20px 16px; }
  .form-grid { grid-template-columns: 1fr; }
  .form-actions { flex-direction: column; }
  .cancel-btn, .submit-btn { width: 100%; justify-content: center; }
}
</style>
