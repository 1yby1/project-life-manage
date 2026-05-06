<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <span class="page-meta">仅系统管理员可见 · 共 {{ total }} 个账号</span>
    </div>

    <el-card class="user-card" shadow="never">
      <template #header>
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名 / 真实姓名"
            clearable
            style="width: 260px"
            @keyup.enter="reload"
            @clear="reload"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="reload">查询</el-button>
        </div>
      </template>

      <el-table :data="users" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="用户名" prop="username" width="160" />
        <el-table-column label="真实姓名" prop="realName" width="160" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag v-if="row.roleName" :type="row.roleCode === 'ADMIN' ? 'danger' : 'primary'" effect="light" round>
              {{ row.roleName }}
            </el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" min-width="180" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="reload"
          @size-change="reload"
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userApi, AdminUser } from '@/api/user'

export default defineComponent({
  name: 'UserManageView',
  components: { Search, Plus },
  setup() {
    const users = ref<AdminUser[]>([])
    const total = ref(0)
    const page = ref(1)
    const size = ref(10)
    const keyword = ref('')
    const loading = ref(false)

    async function reload() {
      loading.value = true
      try {
        const res = await userApi.list({
          page: page.value,
          size: size.value,
          keyword: keyword.value.trim() || undefined,
        })
        users.value = res.records || []
        total.value = res.total || 0
      } catch (e: any) {
        ElMessage.error(e?.message || '加载用户列表失败')
        users.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    onMounted(reload)

    return { users, total, page, size, keyword, loading, reload }
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
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.page-meta {
  color: #64748B;
  font-size: 13px;
}

.user-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important;
    color: #0F172A;
    font-weight: 600;
    font-size: 13px;
  }
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spacer {
  flex: 1;
}

.muted {
  color: #94A3B8;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
}

.hint-alert {
  border-radius: 10px;
}
</style>
