// 客户管理 API — 与后端 CustomerController 对应
import { get, post, put } from './request'

export type VisitStatus = 'PENDING' | 'DOING' | 'COMPLETED'

export type Customer = {
  id: number
  customerName: string
  city: string
  legalPerson: string
  address?: string
  regAddress?: string
  regAgency?: string
  creditCode?: string
  industry?: string
  contactName?: string
  contactPhone?: string
  contactTitle?: string
  bu?: string
  status?: number
  createBy?: number
  createTime?: string
  updateTime?: string
  // ===== 派生字段(list/detail 接口含;create/update 不返回)=====
  /** 当前走访状态;null 表示未派单 */
  visitStatus?: VisitStatus | null
  assignedManagerId?: number | null
  assignedManagerName?: string | null
}

export type CustomerSaveRequest = {
  customerName: string
  city: string
  legalPerson: string
  address?: string
  regAddress?: string
  regAgency?: string
  creditCode?: string
  industry?: string
  contactName?: string
  contactPhone?: string
  contactTitle?: string
  bu?: string
}

export type CustomerListResponse = {
  records: Customer[]
  total: number
}

export type CustomerListQuery = {
  page?: number
  size?: number
  keyword?: string
  city?: string
  /** 走访状态过滤 PENDING/DOING/COMPLETED;NONE 表示"未派单"(已录入) */
  visitStatus?: VisitStatus | 'NONE'
}

export const customerApi = {
  list(query: CustomerListQuery = {}): Promise<CustomerListResponse> {
    return get<CustomerListResponse>('/customers', {
      page: query.page || 1,
      size: query.size || 10,
      keyword: query.keyword,
      city: query.city,
      visitStatus: query.visitStatus,
    })
  },

  detail(id: number | string): Promise<Customer> {
    return get<Customer>(`/customers/${id}`)
  },

  create(req: CustomerSaveRequest): Promise<Customer> {
    return post<Customer>('/customers', req)
  },

  update(id: number | string, req: CustomerSaveRequest): Promise<Customer> {
    return put<Customer>(`/customers/${id}`, req)
  },
}
