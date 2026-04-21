import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '../views/auth/LoginView.vue'
import { ensureUser, userHasAnyRole } from '@/auth/authStore'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true },
  },

  // 客户管理
  { path: '/customers/list', name: 'customers_list', component: () => import('../views/customers/CustomerListView.vue'), meta: { requiresAuth: true, roles: ['商机管理员', '客户经理'] } },
  { path: '/customers/create', name: 'customers_create', component: () => import('../views/customers/CustomerCreateView.vue'), meta: { requiresAuth: true, roles: ['商机管理员'] } },
  { path: '/customers/dispatch', name: 'customers_dispatch', component: () => import('../views/customers/CustomerDispatchView.vue'), meta: { requiresAuth: true, roles: ['商机管理员'] } },
  { path: '/customers/visits', name: 'customers_visits', component: () => import('../views/customers/CustomerVisitView.vue'), meta: { requiresAuth: true, roles: ['客户经理'] } },
  { path: '/customers/complete', name: 'customers_complete', component: () => import('../views/customers/CustomerInfoEditView.vue'), meta: { requiresAuth: true, roles: ['客户经理'] } },
  { path: '/customers/:id', name: 'customers_detail', component: () => import('../views/customers/CustomerDetailView.vue'), meta: { requiresAuth: true, roles: ['商机管理员', '客户经理'] } },

  // 合同管理
  { path: '/contracts/board', name: 'contracts_board', component: () => import('../views/contracts/ContractBoardView.vue'), meta: { requiresAuth: true, roles: ['合同管理员', '销售主管/管理层', '项目经理'] } },
  { path: '/contracts/list', name: 'contracts_list', component: () => import('../views/contracts/ContractListView.vue'), meta: { requiresAuth: true, roles: ['合同管理员', '销售主管/管理层', '项目经理'] } },
  { path: '/contracts/create', name: 'contracts_create', component: () => import('../views/contracts/ContractCreateView.vue'), meta: { requiresAuth: true, roles: ['合同管理员'] } },
  { path: '/contracts/:id', name: 'contracts_preview', component: () => import('../views/contracts/ContractPreviewView.vue'), meta: { requiresAuth: true, roles: ['合同管理员', '销售主管/管理层', '项目经理'] } },

  // 线索管理
  { path: '/clues/list', name: 'clues_list', component: () => import('../views/clues/ClueListView.vue'), meta: { requiresAuth: true, roles: ['线索收集人', '线索分发人', '客户经理', '客户/线索培育'] } },
  { path: '/clues/create', name: 'clues_create', component: () => import('../views/clues/ClueCreateView.vue'), meta: { requiresAuth: true, roles: ['销售人员', '线索收集人'] } },
  { path: '/clues/collect', name: 'clues_collect', component: () => import('../views/clues/ClueCollectView.vue'), meta: { requiresAuth: true, roles: ['线索收集人'] } },
  { path: '/clues/distribute', name: 'clues_distribute', component: () => import('../views/clues/ClueDistributeView.vue'), meta: { requiresAuth: true, roles: ['线索分发人'] } },
  { path: '/clues/cultivate', name: 'clues_cultivate', component: () => import('../views/clues/ClueCultivateView.vue'), meta: { requiresAuth: true, roles: ['客户经理', '客户/线索培育'] } },

  // 商机管理
  { path: '/opportunities/list', name: 'opportunities_list', component: () => import('../views/opportunities/OpportunityListView.vue'), meta: { requiresAuth: true, roles: ['商机管理员', '客户经理', '客户/线索培育', '项目经理'] } },
  { path: '/opportunities/template', name: 'opportunities_template', component: () => import('../views/opportunities/OpportunityTemplateSelectView.vue'), meta: { requiresAuth: true, roles: ['商机管理员', '项目经理'] } },
  { path: '/opportunities/:id', name: 'opportunities_detail', component: () => import('../views/opportunities/OpportunityDetailView.vue'), meta: { requiresAuth: true, roles: ['项目经理', '铁三角'] } },

  // 辅助模块
  { path: '/reports/submit', name: 'reports_submit', component: () => import('../views/reports/WeeklyReportSubmitView.vue'), meta: { requiresAuth: true, roles: ['销售人员'] } },
  { path: '/reports/review', name: 'reports_review', component: () => import('../views/reports/WeeklyReportReviewView.vue'), meta: { requiresAuth: true, roles: ['销售主管/管理层'] } },
  { path: '/analytics/inflight', name: 'analytics_inflight', component: () => import('../views/analytics/InFlightContractsView.vue'), meta: { requiresAuth: true, roles: ['销售主管/管理层'] } },
  { path: '/analytics/accepted', name: 'analytics_accepted', component: () => import('../views/analytics/AcceptedProjectsView.vue'), meta: { requiresAuth: true, roles: ['项目经理', '销售主管/管理层'] } },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const meta = to.meta as { requiresAuth?: boolean; roles?: string[] }

  if (!meta.requiresAuth) return next()

  // TODO: 开发阶段跳过权限校验，上线前删除
  if (import.meta.env.DEV) return next()

  const user = await ensureUser()
  if (!user) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  if (!userHasAnyRole(user, meta.roles)) {
    // 演示：没有权限直接跳回首页（后续可改成“无权限页面”）
    return next({ name: 'home' })
  }

  return next()
})

export default router
