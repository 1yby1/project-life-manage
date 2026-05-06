<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
    </div>

    <el-card class="role-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Avatar /></el-icon>
          系统内置角色
          <span class="card-meta">— 共 {{ roles.length }} 个</span>
        </div>
      </template>

      <el-table :data="roles" stripe>
        <el-table-column label="ID" width="60" prop="id" />
        <el-table-column label="角色编码" width="180">
          <template #default="{ row }">
            <code class="code-text">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column label="角色名称" width="160">
          <template #default="{ row }">
            <el-tag :type="row.code === 'ADMIN' ? 'danger' : 'primary'" effect="light" round>
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="说明" prop="description" />
      </el-table>
    </el-card>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint-alert"
    >
      <template #title>
        系统按需求设计内置 11 个角色(<code>schema.sql</code> 的 <code>sys_role</code> 表),不开放动态增删。同一用户可在 <code>sys_user_role</code> 中持有多角色;角色与用户的绑定可在用户管理页调整。
      </template>
    </el-alert>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { Avatar } from '@element-plus/icons-vue'
import { ALL_ROLE_OPTIONS } from '@/auth/roles'

const ROLE_DESCRIPTIONS: Record<string, string> = {
  ADMIN: '用户管理、角色管理、模板管理',
  OPP_ADMIN: '录入客户、派单给客户经理、整体调度',
  CUSTOMER_MANAGER: '走访客户、完善客户信息、线索培育',
  SALES: '提交周报、录入线索',
  SUPERVISOR: '审阅并点评下属周报',
  SOLUTION_MANAGER: '商机方案制定,铁三角成员',
  DELIVERY_MANAGER: '项目交付,铁三角成员',
  PROJECT_MANAGER: '项目运作,铁三角核心',
  REGION_HEAD: '查看在途合同专题',
  PMO: '查看已验收项目专题、月度汇报',
  USER: '录入线索,所有人默认拥有',
}

export default defineComponent({
  name: 'RoleManageView',
  components: { Avatar },
  setup() {
    const roles = ALL_ROLE_OPTIONS.map((r, idx) => ({
      id: idx + 1,
      code: r.code,
      name: r.name,
      description: ROLE_DESCRIPTIONS[r.code] || '-',
    }))

    return { roles }
  },
})
</script>

<style scoped lang="scss">
.page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.role-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important;
    color: #0F172A;
    font-weight: 600;
    font-size: 13px;
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

.card-meta {
  color: #64748B;
  font-weight: 400;
  font-size: 13px;
}

.code-text {
  font-family: 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12px;
  background: #F1F5F9;
  color: #0369A1;
  padding: 2px 8px;
  border-radius: 4px;
}

.hint-alert {
  border-radius: 10px;
}
</style>
