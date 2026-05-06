<template>
  <el-container v-if="!hideShell" class="app-shell">
    <el-aside width="240px" class="sidebar">
      <div class="brand">
        <div class="brand-logo">
          <el-icon :size="20"><Suitcase /></el-icon>
        </div>
        <div class="brand-name">项目全生命周期管理系统</div>
      </div>

      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <template v-for="g in groupedMenu" :key="g.group">
          <div class="group-title">{{ g.group }}</div>
          <el-menu-item
            v-for="item in g.items"
            :key="item.path"
            :index="item.path"
            class="sidebar-item"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>

        <div v-if="groupedMenu.length === 0" class="empty-menu">
          当前角色暂无可访问模块
        </div>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="breadcrumb">
          <span v-if="parentLabel" class="bc-parent">{{ parentLabel }}</span>
          <span v-if="parentLabel" class="bc-sep">/</span>
          <span class="bc-current">{{ currentLabel }}</span>
        </div>
        <div class="topbar-right">
          <div class="user-info">
            <el-icon class="user-icon"><UserFilled /></el-icon>
            <div class="user-text">
              <div class="user-name">{{ displayName }}</div>
              <div class="user-roles">
                <el-tag
                  v-for="r in displayRoles"
                  :key="r"
                  size="small"
                  :type="r === '系统管理员' ? 'danger' : 'primary'"
                  effect="light"
                  round
                >
                  {{ r }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-button type="default" :icon="SwitchButton" @click="doLogout">
            退出登录
          </el-button>
        </div>
      </el-header>

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
import { computed, defineComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAuthState, logout, userHasAnyRole } from '@/auth/authStore'
import { roleCodeToName } from '@/auth/roles'
import { UserFilled, SwitchButton, Suitcase } from '@element-plus/icons-vue'
import type { RouteMeta, SidebarGroup } from '@/router'

type MenuItem = { path: string; title: string; icon: string; order: number }

// 分组渲染顺序(自上而下)
const GROUP_ORDER: SidebarGroup[] = ['工作台', '业务漏斗', '运营专题', '系统管理']

export default defineComponent({
  name: 'MainLayout',
  components: { UserFilled, SwitchButton, Suitcase },
  setup() {
    const authState = getAuthState()
    const route = useRoute()
    const router = useRouter()

    const hideShell = computed(() => route.path === '/login' || route.path === '/403')

    /** 当前用户真实姓名(后端返回的 realName 或 username) */
    const displayName = computed(() => authState.user?.name || '未登录')

    /** 当前用户全部角色的中文名(已过滤兜底 USER) */
    const displayRoles = computed(() => {
      const roles = authState.user?.roles || []
      // 当用户除 USER 外还有其他角色时,隐藏 USER(避免视觉冗余)
      const filtered = roles.length > 1 ? roles.filter((r) => r !== 'USER') : roles
      return filtered.map(roleCodeToName)
    })

    const doLogout = () => {
      logout()
      router.push({ name: 'login' })
    }

    /** 当前用户可见的菜单,按 group 聚合后输出 */
    const groupedMenu = computed(() => {
      const user = authState.user
      const buckets = new Map<SidebarGroup, MenuItem[]>()

      for (const r of router.options.routes) {
        const m = r.meta as RouteMeta | undefined
        if (!m || !m.menu) continue
        if (!userHasAnyRole(user, m.roles)) continue
        const group = m.menu.group
        const item: MenuItem = {
          path: r.path as string,
          title: m.menu.title,
          icon: m.menu.icon,
          order: m.menu.order,
        }
        const arr = buckets.get(group)
        if (arr) arr.push(item)
        else buckets.set(group, [item])
      }

      // 按预定义顺序输出非空组,组内按 order 升序
      const out: Array<{ group: SidebarGroup; items: MenuItem[] }> = []
      for (const g of GROUP_ORDER) {
        const items = buckets.get(g)
        if (!items) continue
        out.push({ group: g, items: items.slice().sort((a, b) => a.order - b.order) })
      }
      return out
    })

    /** 当前激活的菜单: 路径前缀最长匹配 */
    const activeMenu = computed(() => {
      const current = route.path
      const all: MenuItem[] = []
      for (const g of groupedMenu.value) {
        for (const it of g.items) all.push(it)
      }
      const exact = all.find((m) => m.path === current)
      if (exact) return exact.path
      const seg = '/' + current.split('/')[1]
      const prefix = all.find((m) => m.path.indexOf(seg + '/') === 0 || m.path === seg)
      return prefix ? prefix.path : ''
    })

    /**
     * 面包屑父级:当前路由所属菜单分组名(工作台/业务漏斗/...)
     * 子页(没有 menu 字段)取其前缀菜单的 group
     */
    const parentLabel = computed(() => {
      const m = route.meta as RouteMeta | undefined
      if (m?.menu) return m.menu.group
      // 子页 → 找前缀菜单
      const seg = '/' + route.path.split('/')[1]
      for (const g of groupedMenu.value) {
        const hit = g.items.find((it) => it.path.indexOf(seg + '/') === 0 || it.path === seg)
        if (hit) return g.group
      }
      return ''
    })

    /** 面包屑当前页:菜单页用 menu.title,非菜单页用 meta.title,兜底用末段 */
    const currentLabel = computed(() => {
      const m = route.meta as RouteMeta | undefined
      if (m?.menu) return m.menu.title
      if (m?.title) return m.title
      const last = route.path.split('/').filter(Boolean).pop() || ''
      return last
    })

    return {
      hideShell,
      displayName,
      displayRoles,
      doLogout,
      groupedMenu,
      activeMenu,
      parentLabel,
      currentLabel,
      UserFilled,
      SwitchButton,
      Suitcase,
    }
  },
})
</script>

<style lang="scss" scoped>
.app-shell {
  min-height: 100vh;
  background: #f3f4f6;
  color: #111827;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* ---------- Sidebar ---------- */

.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.brand-logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #0369a1;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  padding: 8px 0 16px;
  overflow-y: auto;
}

.group-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  letter-spacing: 0.5px;
  padding: 16px 16px 8px;
  user-select: none;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
}

:deep(.el-menu-item) {
  color: #6b7280;
  border-radius: 6px;
  margin: 4px 12px;
  padding-left: 16px !important;
  transition: background 200ms ease, color 200ms ease;
  height: 40px;
  line-height: 40px;
  position: relative;
}

:deep(.el-menu-item:hover) {
  background: #f3f4f6 !important;
  color: #0369a1;
}

:deep(.el-menu-item.is-active) {
  background: #e0f2fe !important;
  color: #0369a1;
  font-weight: 600;
}

/* Active 项左侧 2px 强调条 */
:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 2px;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 24px;
  background: #0369a1;
  border-radius: 2px;
}

.empty-menu {
  padding: 32px 16px;
  color: #9ca3af;
  font-size: 14px;
  text-align: center;
}

/* ---------- Topbar ---------- */

.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: none;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.bc-parent {
  color: #9ca3af;
}

.bc-sep {
  color: #d1d5db;
}

.bc-current {
  color: #111827;
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.user-icon {
  font-size: 16px;
  color: #0369a1;
}

.user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.2;
}

.user-name {
  font-weight: 600;
  font-size: 14px;
  color: #111827;
}

.user-roles {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.user-roles :deep(.el-tag) {
  font-size: 12px;
  height: 20px;
  padding: 0 6px;
  line-height: 20px;
  border-radius: 4px;
}

:deep(.el-button--default) {
  background: #ffffff;
  border: 1px solid #d1d5db;
  color: #6b7280;
  border-radius: 6px;
  font-size: 14px;
  padding: 8px 16px;
  transition: all 200ms ease;
  height: 32px;
}

:deep(.el-button--default:hover) {
  background: #f9fafb;
  color: #111827;
  border-color: #9ca3af;
}

/* ---------- Content ---------- */

.content {
  flex: 1;
  padding: 24px;
  min-width: 0;
  background: #f3f4f6;
}
</style>
