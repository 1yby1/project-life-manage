import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import { ensureUser, userHasAnyRole } from '@/auth/authStore'
import { RoleCode } from '@/auth/roles'
import { getLandingPath } from '@/auth/landing'

export type SidebarGroup = '工作台' | '业务漏斗' | '运营专题' | '系统管理'

export type MenuMeta = {
  title: string
  icon: string  // Element Plus 图标组件名(main.ts 已全局注册)
  group: SidebarGroup
  order: number
}

export type RouteMeta = {
  public?: boolean
  requiresAuth?: boolean
  roles?: RoleCode[]
  menu?: MenuMeta
  /** 非菜单页的中文页名,topbar 面包屑使用;菜单页直接读 menu.title */
  title?: string
}

function meta(m: RouteMeta): RouteMeta {
  return m
}

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    // 实际重定向在 router.beforeEach 中按角色派发(那里能 await ensureUser)
    component: () => import('../views/common/ForbiddenView.vue'),
    meta: meta({ requiresAuth: true }),
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: meta({ public: true }),
  },
  {
    path: '/403',
    name: 'forbidden',
    component: () => import('../views/common/ForbiddenView.vue'),
    meta: meta({ public: true }),
  },

  // 系统管理(仅 ADMIN)
  {
    path: '/admin/users',
    name: 'admin_users',
    component: () => import('../views/admin/UserManageView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['ADMIN'],
      menu: { title: '用户管理', icon: 'UserFilled', group: '系统管理', order: 1 },
    }),
  },
  {
    path: '/admin/roles',
    name: 'admin_roles',
    component: () => import('../views/admin/RoleManageView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['ADMIN'],
      menu: { title: '角色管理', icon: 'Avatar', group: '系统管理', order: 2 },
    }),
  },
  {
    path: '/admin/templates',
    name: 'admin_templates',
    component: () => import('../views/admin/TemplateManageView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['ADMIN'],
      menu: { title: '商机模板管理', icon: 'Files', group: '系统管理', order: 3 },
    }),
  },

  // 客户管理
  {
    path: '/customers/list',
    name: 'customers_list',
    component: () => import('../views/customers/CustomerListView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['OPP_ADMIN', 'CUSTOMER_MANAGER'],
      menu: { title: '客户管理', icon: 'User', group: '业务漏斗', order: 10 },
    }),
  },
  { path: '/customers/create',   name: 'customers_create',   component: () => import('../views/customers/CustomerCreateView.vue'),   meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN'], title: '新建客户' }) },
  { path: '/customers/dispatch', name: 'customers_dispatch', component: () => import('../views/customers/CustomerDispatchView.vue'), meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN'], title: '客户派单' }) },
  { path: '/customers/visits',   name: 'customers_visits',   component: () => import('../views/customers/CustomerVisitView.vue'),    meta: meta({ requiresAuth: true, roles: ['CUSTOMER_MANAGER'], title: '客户走访' }) },
  { path: '/customers/complete', name: 'customers_complete', component: () => import('../views/customers/CustomerInfoEditView.vue'), meta: meta({ requiresAuth: true, roles: ['CUSTOMER_MANAGER'], title: '完善客户信息' }) },
  { path: '/customers/:id',      name: 'customers_detail',   component: () => import('../views/customers/CustomerDetailView.vue'),   meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN', 'CUSTOMER_MANAGER'], title: '客户详情' }) },

  // 合同管理
  {
    path: '/contracts/board',
    name: 'contracts_board',
    component: () => import('../views/contracts/ContractBoardView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['OPP_ADMIN', 'SUPERVISOR', 'PROJECT_MANAGER'],
      menu: { title: '合同管理', icon: 'Document', group: '业务漏斗', order: 20 },
    }),
  },
  { path: '/contracts/list',    name: 'contracts_list',    component: () => import('../views/contracts/ContractListView.vue'),    meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN', 'SUPERVISOR', 'PROJECT_MANAGER'], title: '合同列表' }) },
  { path: '/contracts/create',  name: 'contracts_create',  component: () => import('../views/contracts/ContractCreateView.vue'),  meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN'], title: '新建合同' }) },
  { path: '/contracts/:id',     name: 'contracts_preview', component: () => import('../views/contracts/ContractPreviewView.vue'), meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN', 'SUPERVISOR', 'PROJECT_MANAGER'], title: '合同详情' }) },

  // 线索管理 — 需求文档明确"所有人均可录入/查看线索"
  {
    path: '/clues/list',
    name: 'clues_list',
    component: () => import('../views/clues/ClueListView.vue'),
    meta: meta({
      requiresAuth: true,
      // 不设 roles: 任何登录用户均可访问
      menu: { title: '线索管理', icon: 'Bell', group: '业务漏斗', order: 30 },
    }),
  },
  { path: '/clues/create',     name: 'clues_create',     component: () => import('../views/clues/ClueCreateView.vue'),     meta: meta({ requiresAuth: true, title: '录入线索' }) },
  { path: '/clues/collect',    name: 'clues_collect',    component: () => import('../views/clues/ClueCollectView.vue'),    meta: meta({ requiresAuth: true, title: '线索收集' }) },
  { path: '/clues/distribute', name: 'clues_distribute', component: () => import('../views/clues/ClueDistributeView.vue'), meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN'], title: '线索分发' }) },
  { path: '/clues/cultivate',  name: 'clues_cultivate',  component: () => import('../views/clues/ClueCultivateView.vue'),  meta: meta({ requiresAuth: true, roles: ['CUSTOMER_MANAGER'], title: '线索培育' }) },

  // 商机管理
  {
    path: '/opportunities/list',
    name: 'opportunities_list',
    component: () => import('../views/opportunities/OpportunityListView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['OPP_ADMIN', 'CUSTOMER_MANAGER', 'PROJECT_MANAGER', 'SOLUTION_MANAGER', 'DELIVERY_MANAGER'],
      menu: { title: '商机管理', icon: 'TrendCharts', group: '业务漏斗', order: 40 },
    }),
  },
  { path: '/opportunities/template', name: 'opportunities_template', component: () => import('../views/opportunities/OpportunityTemplateSelectView.vue'), meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN', 'PROJECT_MANAGER', 'CUSTOMER_MANAGER'], title: '选择商机模板' }) },
  { path: '/opportunities/:id',      name: 'opportunities_detail',   component: () => import('../views/opportunities/OpportunityDetailView.vue'),         meta: meta({ requiresAuth: true, roles: ['OPP_ADMIN', 'CUSTOMER_MANAGER', 'PROJECT_MANAGER', 'SOLUTION_MANAGER', 'DELIVERY_MANAGER'], title: '商机详情' }) },

  // 工作台 + 运营专题
  {
    path: '/reports/submit',
    name: 'reports_submit',
    component: () => import('../views/reports/WeeklyReportSubmitView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['SALES'],
      menu: { title: '提交周报', icon: 'Edit', group: '工作台', order: 50 },
    }),
  },
  {
    path: '/reports/review',
    name: 'reports_review',
    component: () => import('../views/reports/WeeklyReportReviewView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['SUPERVISOR'],
      menu: { title: '点评周报', icon: 'ChatDotRound', group: '工作台', order: 51 },
    }),
  },
  {
    path: '/analytics/inflight',
    name: 'analytics_inflight',
    component: () => import('../views/analytics/InFlightContractsView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['REGION_HEAD', 'OPP_ADMIN'],
      menu: { title: '在途合同专题', icon: 'DataAnalysis', group: '运营专题', order: 60 },
    }),
  },
  {
    path: '/analytics/accepted',
    name: 'analytics_accepted',
    component: () => import('../views/analytics/AcceptedProjectsView.vue'),
    meta: meta({
      requiresAuth: true,
      roles: ['PMO', 'PROJECT_MANAGER'],
      menu: { title: '已验收项目专题', icon: 'Files', group: '运营专题', order: 61 },
    }),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to, _from, next) => {
  const m = to.meta as RouteMeta

  if (m.public) return next()
  if (!m.requiresAuth) return next()

  const user = await ensureUser()
  if (!user) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  // 根路径 → 角色专属落地页
  if (to.path === '/') {
    return next(getLandingPath(user))
  }

  if (!userHasAnyRole(user, m.roles)) {
    return next({ name: 'forbidden' })
  }

  return next()
})

export default router
