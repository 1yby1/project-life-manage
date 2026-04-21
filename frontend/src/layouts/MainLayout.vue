<template>
  <el-container v-if="!hideShell" class="app-shell">
    <el-header class="topbar">
      <div class="brand">
        <el-icon :size="24" style="margin-right: 8px"><Suitcase /></el-icon>
        项目全生命周期管理系统
      </div>
      <div class="topbar-right">
        <el-dropdown trigger="click" :disabled="!isDemo">
          <el-button type="primary" :icon="UserFilled">
            {{ role }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="r in roleOptions" :key="r" @click="role = r">
                {{ r }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="default" :icon="RefreshRight" @click="resetDemoData">
          重置演示数据
        </el-button>
      </div>
    </el-header>

    <el-container>
      <el-aside width="240px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          @select="handleMenuSelect"
        >
          <el-menu-item index="/customers/list">
            <el-icon><User /></el-icon>
            <span>客户管理</span>
          </el-menu-item>
          <el-menu-item index="/contracts/board">
            <el-icon><Document /></el-icon>
            <span>合同管理</span>
          </el-menu-item>
          <el-menu-item index="/clues/list">
            <el-icon><Bell /></el-icon>
            <span>线索管理</span>
          </el-menu-item>
          <el-menu-item index="/opportunities/list">
            <el-icon><TrendCharts /></el-icon>
            <span>商机管理</span>
          </el-menu-item>
          <el-divider />
          <el-menu-item index="/reports/submit">
            <el-icon><Edit /></el-icon>
            <span>周报管理</span>
          </el-menu-item>
          <el-menu-item index="/analytics/inflight">
            <el-icon><DataAnalysis /></el-icon>
            <span>专题分析</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <div v-else class="login-container">
    <router-view />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { resetDemoData } from '@/store/mockStore'
import { getAuthState, isDemoSession, loginAsDemoRole } from '@/auth/authStore'

export default defineComponent({
  name: 'MainLayout',
  setup() {
    const role = ref('商机管理员')
    const authState = getAuthState()
    const route = useRoute()

    const hideShell = computed(() => route.path === '/login')
    const isDemo = computed(() => isDemoSession())

    onMounted(() => {
      const saved = window.localStorage.getItem('demo_role')
      if (saved) role.value = saved

      // 如果是后端鉴权，登录后展示后端角色（本地 demo 下会锁定为选择项）
      if (authState.user?.roles?.length) {
        // 尽量取第一角色作为展示
        role.value = authState.user.roles[0] || role.value
      }
    })

    watch(
      role,
      async (next) => {
        // 仅 demo 会影响“当前用户角色”
        if (!isDemo.value) return
        await loginAsDemoRole(next)
      },
      { immediate: true },
    )

    return {
      role,
      resetDemoData,
      hideShell,
      isDemo,
    }
  },
})
</script>

<style lang="scss" scoped>

.app-shell {
  min-height: 100vh;
  background: #f4f6f8;
  color: #1e293b;
  font-family: system-ui, -apple-system, sans-serif;
}

.topbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px 0 rgba(0,0,0,0.03);
}

.brand {
  font-weight: 600;
  font-size: 18px;
  display: flex;
  align-items: center;
  color: #0f172a;
}

.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
}

:deep(.el-menu-item) {
  color: #475569;
  border-radius: 8px;
  margin: 4px 12px;
  transition: all 0.2s ease;
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu-item:hover) {
  background: #f8fafc !important;
  color: #0284c7;
}

:deep(.el-menu-item.is-active) {
  background: #e0f2fe !important;
  color: #0284c7;
  font-weight: 600;
}

:deep(.el-button--default) {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #475569;
  border-radius: 6px;
}

:deep(.el-button--primary) {
  background: #0ea5e9;
  border-color: #0ea5e9;
  color: #ffffff;
  border-radius: 6px;
  font-weight: 500;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  flex: 1;
  padding: 24px;
  min-width: 0;
  background: #f4f6f8;
}

</style>

