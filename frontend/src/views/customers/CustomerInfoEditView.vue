<template>
  <div class="edit-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户信息完善</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item" @click="$router.push('/customers/list')">客户管理</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">信息完善</span>
        </div>
      </div>
    </header>

    <el-empty v-if="!selectedCustomer" description="未找到客户（请从客户列表进入）" />

    <template v-else>
      <!-- 客户信息卡片 -->
      <div class="customer-card">
        <div class="customer-avatar">{{ selectedCustomer.customerName.charAt(0) }}</div>
        <div class="customer-info">
          <h2 class="customer-name">{{ selectedCustomer.customerName }}</h2>
          <div class="customer-meta">
            <span><el-icon><Location /></el-icon> {{ selectedCustomer.city }}</span>
            <span><el-icon><User /></el-icon> {{ selectedCustomer.legalPerson }}</span>
          </div>
        </div>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <div class="section-header">
          <h3 class="section-title">企业信息</h3>
          <p class="section-desc">完善企业基本信息</p>
        </div>

        <div class="form-grid">
          <div class="form-item">
            <label class="form-label">统一信用编码 <span class="required">*</span></label>
            <el-input
              v-model="form.unifyCreditCode"
              :placeholder="selectedCustomer.unifyCreditCode || '例如：91440000...'"
              clearable
            >
              <template #prefix><el-icon><CreditCard /></el-icon></template>
            </el-input>
          </div>
          <div class="form-item">
            <label class="form-label">所属行业 <span class="required">*</span></label>
            <el-input
              v-model="form.industry"
              :placeholder="selectedCustomer.industry || '例如：软件与信息技术服务'"
              clearable
            >
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>

      <!-- 联系人区域 -->
      <div class="form-section">
        <div class="section-header">
          <h3 class="section-title">联系人信息</h3>
          <p class="section-desc">至少添加一位联系人，所有字段必填</p>
        </div>

        <div class="contacts-list">
          <div v-for="(ct, idx) in form.contacts" :key="ct.id" class="contact-item">
            <div class="contact-fields">
              <div class="field">
                <label>姓名</label>
                <el-input v-model="ct.name" placeholder="联系人姓名" clearable />
              </div>
              <div class="field">
                <label>职位</label>
                <el-input v-model="ct.title" placeholder="联系人职位" clearable />
              </div>
              <div class="field">
                <label>联系方式</label>
                <el-input v-model="ct.phone" placeholder="手机号/座机" clearable />
              </div>
            </div>
            <button 
              class="remove-btn" 
              @click="removeContact(idx)"
              :disabled="form.contacts.length === 1"
              title="删除联系人"
            >
              <el-icon><Delete /></el-icon>
            </button>
          </div>
        </div>

        <button class="add-contact-btn" @click="addContact">
          <el-icon><Plus /></el-icon>
          添加联系人
        </button>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button class="cancel-btn" @click="$router.push('/customers/list')">
          <el-icon><Back /></el-icon>
          返回列表
        </el-button>
        <el-button type="primary" class="save-btn" @click="save">
          <el-icon><Check /></el-icon>
          保存信息
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
    </template>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMockStore } from '@/store/mockStore'
import { 
  Location, User, CreditCard, OfficeBuilding, Delete, Plus, 
  Back, Check, CircleCheck, CircleClose 
} from '@element-plus/icons-vue'

type ContactForm = { id: string; name: string; title: string; phone: string }

export default defineComponent({
  name: 'CustomerInfoEditView',
  components: { Location, User, CreditCard, OfficeBuilding, Delete, Plus, Back, Check, CircleCheck, CircleClose },
  setup() {
    const store = useMockStore()
    const route = useRoute()

    const customerId = String(route.query.customerId || '')
    const selectedCustomer = computed(() => store.customers.find((c) => c.id === customerId))

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const form = reactive({
      unifyCreditCode: '',
      industry: '',
      contacts: [] as ContactForm[],
    })

    const init = () => {
      if (!selectedCustomer.value) return
      form.unifyCreditCode = selectedCustomer.value.unifyCreditCode || ''
      form.industry = selectedCustomer.value.industry || ''
      form.contacts = (selectedCustomer.value.contacts || []).map((c) => ({
        id: c.id,
        name: c.name,
        title: c.title,
        phone: c.phone,
      }))
      if (form.contacts.length === 0) {
        form.contacts = [{ id: `ctf_${Date.now()}`, name: '', title: '', phone: '' }]
      }
    }

    init()

    const addContact = () => {
      form.contacts.push({ id: `ctf_${Date.now()}`, name: '', title: '', phone: '' })
    }

    const removeContact = (idx: number) => {
      if (form.contacts.length > 1) {
        form.contacts.splice(idx, 1)
      }
    }

    const save = () => {
      if (!selectedCustomer.value) return

      if (!form.unifyCreditCode || !form.industry) {
        messageType.value = 'error'
        message.value = '请填写统一信用编码与所属行业'
        return
      }

      const allValid = form.contacts.every((ct) => ct.name && ct.title && ct.phone)
      if (!allValid) {
        messageType.value = 'error'
        message.value = '请确保所有联系人信息均已填写完整'
        return
      }

      selectedCustomer.value.unifyCreditCode = form.unifyCreditCode
      selectedCustomer.value.industry = form.industry
      selectedCustomer.value.contacts = form.contacts.map((ct) => ({
        id: ct.id,
        name: ct.name,
        title: ct.title,
        phone: ct.phone,
      }))

      messageType.value = 'success'
      message.value = '客户信息保存成功'
    }

    return {
      selectedCustomer,
      form,
      addContact,
      removeContact,
      save,
      message,
      messageType,
    }
  },
})
</script>

<style scoped lang="scss">
.edit-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 32px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header { margin-bottom: 32px; }

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 8px;
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

// 客户卡片
.customer-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.customer-avatar {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #FFFFFF;
}

.customer-info { flex: 1; }

.customer-name {
  font-size: 20px;
  font-weight: 700;
  color: #FFFFFF;
  margin: 0 0 8px;
}

.customer-meta {
  display: flex;
  gap: 20px;
  color: #94A3B8;
  font-size: 14px;
  
  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

// 表单区域
.form-section {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px;
}

.section-desc {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  
  .required { color: #EF4444; }
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #E2E8F0 inset;
  background: #F8FAFC;
  
  &:hover, &.is-focus {
    box-shadow: 0 0 0 2px #3B82F6 inset;
    background: #FFFFFF;
  }
}

// 联系人列表
.contacts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.contact-item {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  padding: 20px;
}

.contact-fields {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  flex: 1;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  
  label {
    font-size: 13px;
    font-weight: 500;
    color: #64748B;
  }
}

.remove-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FEE2E2;
  border: none;
  border-radius: 10px;
  color: #DC2626;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover:not(:disabled) {
    background: #FECACA;
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.add-contact-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  background: #F8FAFC;
  border: 2px dashed #E2E8F0;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: #3B82F6;
    color: #3B82F6;
    background: #EFF6FF;
  }
}

// 操作按钮
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.cancel-btn, .save-btn {
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
}

.save-btn {
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
  margin-top: 24px;
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
  .edit-page { padding: 20px 16px; }
  .form-grid { grid-template-columns: 1fr; }
  .contact-fields { grid-template-columns: 1fr; }
  .form-actions { flex-direction: column; }
  .cancel-btn, .save-btn { width: 100%; justify-content: center; }
}
</style>
