<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">客户信息完善</h2>
      <el-button :icon="ArrowLeft" @click="$router.push('/customers/list')">返回列表</el-button>
    </div>

    <el-empty v-if="!loading && !customer" description="未找到客户(请从客户列表进入)" :image-size="100" />

    <template v-else-if="customer">
      <el-card class="customer-card" shadow="never">
        <div class="customer-row">
          <div class="customer-main">
            <div class="customer-name">{{ customer.customerName }}</div>
            <div class="customer-meta">
              <span><el-icon><Location /></el-icon> {{ customer.city }}</span>
              <span class="meta-sep">·</span>
              <span><el-icon><User /></el-icon> {{ customer.legalPerson }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><OfficeBuilding /></el-icon>
            企业信息
            <span class="card-title-meta">— 完善企业基础资料</span>
          </div>
        </template>
        <el-form label-position="top">
          <el-row :gutter="16">
            <el-col :xs="24" :md="12">
              <el-form-item label="统一信用编码" required>
                <el-input
                  v-model="form.creditCode"
                  placeholder="例如:91440000..."
                  clearable
                  :prefix-icon="CreditCard"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="所属行业" required>
                <el-input
                  v-model="form.industry"
                  placeholder="例如:软件与信息技术服务"
                  clearable
                  :prefix-icon="OfficeBuilding"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="公司地址">
                <el-input v-model="form.address" placeholder="公司地址" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="注册地址">
                <el-input v-model="form.regAddress" placeholder="注册地址" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="注册机构">
                <el-input v-model="form.regAgency" placeholder="注册机构" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="所属业务单元(BU)">
                <el-input v-model="form.bu" placeholder="可选" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-card class="form-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><User /></el-icon>
            主联系人信息
            <span class="card-title-meta">— 后端单条结构,如需多人请在新增客户时分别录入</span>
          </div>
        </template>
        <el-form label-position="top">
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
        </el-form>
      </el-card>

      <div class="footer-actions">
        <el-button @click="$router.push('/customers/list')">取消</el-button>
        <el-button type="primary" :icon="Check" :loading="saving" @click="save">保存信息</el-button>
      </div>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Location, User, CreditCard, OfficeBuilding, Check,
} from '@element-plus/icons-vue'
import { customerApi, Customer } from '@/api/customer'

export default defineComponent({
  name: 'CustomerInfoEditView',
  components: { ArrowLeft, Location, User, CreditCard, OfficeBuilding, Check },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const customerId = Number(route.query.customerId || 0)

    const customer = ref<Customer | null>(null)
    const loading = ref(false)
    const saving = ref(false)

    const form = reactive({
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

    const syncFormFromCustomer = () => {
      if (!customer.value) return
      form.address = customer.value.address || ''
      form.regAddress = customer.value.regAddress || ''
      form.regAgency = customer.value.regAgency || ''
      form.creditCode = customer.value.creditCode || ''
      form.industry = customer.value.industry || ''
      form.bu = customer.value.bu || ''
      form.contactName = customer.value.contactName || ''
      form.contactTitle = customer.value.contactTitle || ''
      form.contactPhone = customer.value.contactPhone || ''
    }

    const load = async () => {
      if (!customerId) return
      loading.value = true
      try {
        customer.value = await customerApi.detail(customerId)
        syncFormFromCustomer()
      } catch (e: any) {
        ElMessage.error(e?.message || '加载客户失败')
        customer.value = null
      } finally {
        loading.value = false
      }
    }

    const save = async () => {
      if (!customer.value) return
      if (!form.creditCode || !form.industry) {
        ElMessage.error('请填写统一信用编码与所属行业')
        return
      }
      saving.value = true
      try {
        await customerApi.update(customer.value.id, {
          customerName: customer.value.customerName,
          city: customer.value.city,
          legalPerson: customer.value.legalPerson,
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
        ElMessage.success('客户信息保存成功')
        setTimeout(() => router.push('/customers/list'), 400)
      } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }

    onMounted(load)

    return {
      customer,
      loading,
      saving,
      form,
      save,
      ArrowLeft, Location, User, CreditCard, OfficeBuilding, Check,
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

.customer-card,
.form-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;
}

.customer-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.form-card :deep(.el-card__header) {
  padding: 14px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}

.form-card :deep(.el-card__body) {
  padding: 20px;
}

.customer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.customer-main {
  flex: 1;
  min-width: 0;
}

.customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #0F172A;
  margin-bottom: 4px;
}

.customer-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748B;
  font-size: 13px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.meta-sep {
  color: #CBD5E1;
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

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
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
