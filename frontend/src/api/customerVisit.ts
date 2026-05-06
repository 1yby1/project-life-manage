// 客户走访 / 派单 API — 与后端 CustomerVisitController (/customer-visits) 对应
import { get, post, put } from './request'

export type VisitStatus = 'PENDING' | 'DOING' | 'COMPLETED'

export type VisitListItem = {
  id: number
  customerId: number
  customerName: string
  customerCity: string
  customerLegalPerson: string
  managerId: number
  managerName: string
  assignBy: number
  assignByName: string
  /** JSON 字符串形式,前端用 utils/visitRecord 解包 */
  visitRecord: string | null
  status: VisitStatus
  assignTime?: string
  visitTime?: string
  createTime?: string
  updateTime?: string
}

export type VisitDispatchRequest = {
  customerId: number
  managerId: number
}

export const customerVisitApi = {
  /** 派单(OPP_ADMIN) */
  dispatch(req: VisitDispatchRequest): Promise<unknown> {
    return post('/customer-visits', req)
  },

  /** 我的待走访清单(CUSTOMER_MANAGER 自动按当前用户过滤) */
  myList(status?: VisitStatus): Promise<VisitListItem[]> {
    return get<VisitListItem[]>('/customer-visits/my', status ? { status } : undefined)
  },

  /** 走访详情 */
  detail(id: number): Promise<VisitListItem> {
    return get<VisitListItem>(`/customer-visits/${id}`)
  },

  /** 暂存走访记录(传 JSON 字符串) */
  saveRecord(id: number, visitRecord: string): Promise<string> {
    return put<string>(`/customer-visits/${id}/record`, { visitRecord })
  },

  /** 完成走访(不可逆,传最终的 JSON 字符串) */
  complete(id: number, visitRecord: string): Promise<string> {
    return post<string>(`/customer-visits/${id}/complete`, { visitRecord })
  },
}
