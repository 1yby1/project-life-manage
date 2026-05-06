// 用户管理 API — 与后端 AdminUserController + UserController 对应
import { get, post, put, patch } from './request'

export type AdminUser = {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  roleId?: number
  roleCode?: string
  roleName?: string
  status: number    // 1 正常 / 0 禁用
  createTime?: string
}

export type AdminUserCreateRequest = {
  username: string
  password: string
  realName?: string
  roleId?: number
}

export type AdminUserUpdateRequest = {
  realName?: string
  roleId?: number
  status?: number
}

export type UserListQuery = {
  page?: number
  size?: number
  keyword?: string
}

export type UserPage = {
  total: number
  records: AdminUser[]
}

export const userApi = {
  list(query: UserListQuery = {}): Promise<UserPage> {
    return get<UserPage>('/admin/users', {
      page: query.page || 1,
      size: query.size || 10,
      keyword: query.keyword,
    })
  },

  create(req: AdminUserCreateRequest): Promise<string> {
    return post<string>('/admin/users', req)
  },

  update(id: number, req: AdminUserUpdateRequest): Promise<string> {
    return put<string>(`/admin/users/${id}`, req)
  },

  resetPassword(id: number, password: string): Promise<string> {
    return patch<string>(`/admin/users/${id}/password`, { password })
  },

  /** 全部 CUSTOMER_MANAGER 角色的用户(派单选择用,OPP_ADMIN 可调) */
  listCustomerManagers(): Promise<AdminUser[]> {
    return get<AdminUser[]>('/users/customer-managers')
  },

  /** 全部 PROJECT_MANAGER 角色的用户(线索培育选商机负责人,任何登录用户可调) */
  listProjectManagers(): Promise<AdminUser[]> {
    return get<AdminUser[]>('/users/project-managers')
  },

  /** 按角色 code 查询用户(任何已登录用户可调,商机组员添加等场景用) */
  listByRole(roleCode: string): Promise<AdminUser[]> {
    return get<AdminUser[]>(`/users/by-role/${roleCode}`)
  },
}
