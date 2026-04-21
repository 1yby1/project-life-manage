<template>
  <div class="page">
    <div class="page-title">周报管理 - 提交周报</div>
    
    <el-alert
      type="info"
      :closable="false"
      class="hint-alert"
    >
      内容要求：一周每日出勤情况、本周工作情况、下周工作计划（演示：必填校验）。
    </el-alert>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent
      >
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="周日期（周起始）" prop="weekOf">
              <el-date-picker
                v-model="form.weekOf"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="提交人（演示）">
              <el-input :model-value="submitEmployeeName" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="section-title">每日出勤情况（必填）</div>
        <el-row :gutter="12" class="attendance-grid">
          <el-col v-for="d in attendanceDays" :key="d.key" :xs="24" :sm="12" :md="12" :lg="8">
            <el-card class="day-card" shadow="never">
              <div class="day-top">
                <span class="day-label">{{ d.label }}</span>
                <el-checkbox v-model="d.present">出勤</el-checkbox>
              </div>
              <el-input
                v-model="d.notes"
                type="textarea"
                :rows="2"
                placeholder="备注（可选）"
              />
            </el-card>
          </el-col>
        </el-row>

        <el-form-item label="本周工作情况" prop="thisWeekWork" class="mt-16">
          <el-input
            v-model="form.thisWeekWork"
            type="textarea"
            :rows="4"
            placeholder="填写本周完成的工作/产出/关键推进内容"
          />
        </el-form-item>

        <el-form-item label="下周工作计划" prop="nextWeekPlan">
          <el-input
            v-model="form.nextWeekPlan"
            type="textarea"
            :rows="4"
            placeholder="填写下周计划/目标/行动项"
          />
        </el-form-item>

        <div class="footer-actions">
          <el-button @click="goReview">查看点评</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </div>

        <el-alert
          v-if="message"
          :type="messageType === 'error' ? 'error' : 'success'"
          :title="message"
          show-icon
          class="message-alert"
        />
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue'
import { useMockStore, getEmployeeIdByRoleLabel } from '@/store/mockStore'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'

export default defineComponent({
  name: 'WeeklyReportSubmitView',
  setup() {
    const store = useMockStore()
    const router = useRouter()
    const formRef = ref<FormInstance>()

    const roleLabel = window.localStorage.getItem('demo_role') || '销售人员'
    const employeeId = getEmployeeIdByRoleLabel(roleLabel) || store.employees[0]?.id

    const submitEmployeeName = computed(() => store.employees.find((e) => e.id === employeeId)?.name || '-')

    const today = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const weekDefault = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`

    const attendanceDays = reactive(
      [
        { key: 'mon', label: '周一', present: true, notes: '' },
        { key: 'tue', label: '周二', present: true, notes: '' },
        { key: 'wed', label: '周三', present: true, notes: '' },
        { key: 'thu', label: '周四', present: true, notes: '' },
        { key: 'fri', label: '周五', present: true, notes: '' },
        { key: 'sat', label: '周六', present: false, notes: '' },
        { key: 'sun', label: '周日', present: false, notes: '' },
      ] as Array<{ key: string; label: string; present: boolean; notes: string }>
    )

    const form = reactive({
      weekOf: weekDefault,
      thisWeekWork: '',
      nextWeekPlan: '',
    })

    const rules: FormRules = {
      weekOf: [
        { required: true, message: '请选择周日期', trigger: 'change' }
      ],
      thisWeekWork: [
        { required: true, message: '请填写本周工作情况', trigger: 'blur' }
      ],
      nextWeekPlan: [
        { required: true, message: '请填写下周工作计划', trigger: 'blur' }
      ],
    }

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const goReview = () => router.push({ path: '/reports/review' })

    const submit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
      } catch {
        messageType.value = 'error'
        message.value = '请填写所有必填项'
        return
      }

      if (!employeeId) {
        messageType.value = 'error'
        message.value = '未找到提交人'
        return
      }

      const attendanceByDay = attendanceDays.map((d) => ({ day: d.label, present: d.present, notes: d.notes || undefined }))

      store.weeklyReports.push({
        id: `wr_${Date.now()}`,
        employeeId,
        weekOf: form.weekOf,
        attendanceByDay,
        thisWeekWork: form.thisWeekWork,
        nextWeekPlan: form.nextWeekPlan,
      })

      messageType.value = 'success'
      message.value = '提交成功：周报已进入待点评列表（演示）'
      setTimeout(() => router.push({ path: '/reports/review' }), 500)
    }

    return {
      formRef,
      form,
      rules,
      attendanceDays,
      submitEmployeeName,
      message,
      messageType,
      submit,
      goReview,
    }
  },
})
</script>

<style scoped lang="scss">
.page {
  background: #F8FAFC;
  min-height: 100%;
  padding: 16px;
}

.page-title {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 12px;
  color: #0F172A;
}

.hint-alert {
  margin-bottom: 16px;
  border-radius: 10px;
  
  :deep(.el-alert__content) {
    line-height: 1.7;
  }
}

.form-card {
  border-radius: 12px;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.section-title {
  font-weight: 700;
  font-size: 14px;
  margin-top: 8px;
  margin-bottom: 12px;
  color: #0F172A;
}

.attendance-grid {
  margin-bottom: 8px;
}

.day-card {
  margin-bottom: 12px;
  border-radius: 10px;
  background: #F8FAFC;
  
  :deep(.el-card__body) {
    padding: 12px;
  }
}

.day-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.day-label {
  font-weight: 700;
  color: #0F172A;
}

.mt-16 {
  margin-top: 16px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #E5E7EB;
}

.message-alert {
  margin-top: 16px;
  border-radius: 10px;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;
  
  &:hover {
    background-color: #0284C7;
    border-color: #0284C7;
  }
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #059669;
  border-color: #059669;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #059669;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-date-editor.el-input) {
  width: 100%;
}

@media (max-width: 768px) {
  .page {
    padding: 12px;
  }
  
  .form-card {
    :deep(.el-card__body) {
      padding: 14px;
    }
  }
}
</style>
