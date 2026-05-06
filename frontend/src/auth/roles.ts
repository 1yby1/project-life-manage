// 与后端 schema.sql 的 sys_role 表 INSERT 完全一致
// 改动这里时请同步 backend/src/main/resources/sql/schema.sql

export const ROLES = {
  ADMIN:            { code: 'ADMIN',            name: '系统管理员' },
  OPP_ADMIN:        { code: 'OPP_ADMIN',        name: '商机管理员' },
  CUSTOMER_MANAGER: { code: 'CUSTOMER_MANAGER', name: '客户经理' },
  SALES:            { code: 'SALES',            name: '销售人员' },
  SUPERVISOR:       { code: 'SUPERVISOR',       name: '主管' },
  SOLUTION_MANAGER: { code: 'SOLUTION_MANAGER', name: '解决方案经理' },
  DELIVERY_MANAGER: { code: 'DELIVERY_MANAGER', name: '交付经理' },
  PROJECT_MANAGER:  { code: 'PROJECT_MANAGER',  name: '项目经理' },
  REGION_HEAD:      { code: 'REGION_HEAD',      name: '区总' },
  PMO:              { code: 'PMO',              name: '项目管理部' },
  USER:             { code: 'USER',             name: '普通用户' },
} as const

export type RoleCode = keyof typeof ROLES

export const IRON_TRIANGLE: RoleCode[] = [
  'PROJECT_MANAGER',
  'SOLUTION_MANAGER',
  'DELIVERY_MANAGER',
]

export const ALL_ROLE_OPTIONS: Array<{ code: RoleCode; name: string }> =
  (Object.values(ROLES) as Array<{ code: RoleCode; name: string }>)

export function roleCodeToName(code: string): string {
  const r = (ROLES as Record<string, { code: string; name: string }>)[code]
  return r?.name ?? code
}

export function roleNameToCode(name: string): RoleCode | undefined {
  const found = ALL_ROLE_OPTIONS.find((r) => r.name === name)
  return found?.code
}

export function isValidRoleCode(code: string): code is RoleCode {
  return code in ROLES
}
