// 角色 → 登录后落地页映射
// 多角色用户按 LANDING_PRIORITY 顺序取第一个命中的 path
// 顺序逻辑: 管理类 > 运营类 > 业务漏斗 > 兜底

import { RoleCode } from '@/auth/roles'

export type AuthUserLike = { roles: RoleCode[] } | null

const LANDING_PRIORITY: Array<{ role: RoleCode; path: string }> = [
  { role: 'ADMIN',            path: '/admin/users' },
  { role: 'SUPERVISOR',       path: '/reports/review' },
  { role: 'REGION_HEAD',      path: '/analytics/inflight' },
  { role: 'PMO',              path: '/analytics/accepted' },
  { role: 'OPP_ADMIN',        path: '/customers/list' },
  { role: 'PROJECT_MANAGER',  path: '/opportunities/list' },
  { role: 'SOLUTION_MANAGER', path: '/opportunities/list' },
  { role: 'DELIVERY_MANAGER', path: '/opportunities/list' },
  { role: 'CUSTOMER_MANAGER', path: '/customers/list' },
  { role: 'SALES',            path: '/reports/submit' },
  { role: 'USER',             path: '/clues/list' },
]

export function getLandingPath(user: AuthUserLike): string {
  if (!user) return '/login'
  for (const { role, path } of LANDING_PRIORITY) {
    if (user.roles.indexOf(role) !== -1) return path
  }
  return '/clues/list'
}
