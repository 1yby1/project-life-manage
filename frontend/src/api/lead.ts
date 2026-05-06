// 线索 API — 与后端 LeadController (/leads) 对应
// 状态机: ENTRY → COLLECTED → DISTRIBUTED → CONVERTED
import { get, post, put } from './request'

export type LeadStatus = 'ENTRY' | 'COLLECTED' | 'DISTRIBUTED' | 'CONVERTED'
export type LeadFilter = 'all' | 'mine' | 'participate' | 'todo'

/** 培育详情(与后端 LeadCultivateRequest JSON 形态对齐) */
export type CultivateInfo = {
  winRate?: number  // 0-100
  projectName?: string
  expectedPurchaseTime?: string
  predictedAmount?: number
  clueLevel?: 'A' | 'B' | 'C'
  solutionManager?: string
  opportunityOwnerId?: number
  keyContact?: {
    name?: string
    title?: string
    phone?: string
  }
}

export type LeadListItem = {
  id: number
  title: string
  status: LeadStatus
  customerId?: number
  customerName?: string
  customerCity?: string
  winRate?: number  // 0-100
  requirement?: string
  projectScale?: string
  /** progress_desc JSON 原文,列表不解析 */
  progressDesc?: string
  bu?: string
  entryBy?: number
  entryByName?: string
  collectorBy?: number
  collectorByName?: string
  distributorBy?: number
  distributorByName?: string
  managerId?: number
  managerName?: string
  createTime?: string
  updateTime?: string
}

export type LeadDetail = LeadListItem & {
  /** 培育详情(已解析);未培育则 undefined */
  cultivateInfo?: CultivateInfo
}

export type LeadCreateRequest = {
  customerId: number
  title: string
  requirement: string
  bu?: string
}

export type LeadCollectRequest = {
  customerId?: number
  title?: string
  requirement?: string
}

export type LeadConvertResponse = {
  opportunityId: number
  opportunityName: string
}

export type LeadListQuery = {
  filter?: LeadFilter
  keyword?: string
  bu?: string
  status?: LeadStatus | string
}

export const leadApi = {
  /** 清单 */
  list(query: LeadListQuery = {}): Promise<LeadListItem[]> {
    return get<LeadListItem[]>('/leads', {
      filter: query.filter,
      keyword: query.keyword,
      bu: query.bu,
      status: query.status,
    })
  },

  /** 详情(含培育解析) */
  detail(id: number): Promise<LeadDetail> {
    return get<LeadDetail>(`/leads/${id}`)
  },

  /** 录入(任何已登录用户) */
  create(req: LeadCreateRequest): Promise<number> {
    return post<number>('/leads', req)
  },

  /** 临时保存(entry_by 本人 + ENTRY) */
  saveDraft(id: number, req: LeadCollectRequest): Promise<string> {
    return put<string>(`/leads/${id}`, req)
  },

  /** 确认收集(entry_by 本人, ENTRY → COLLECTED) */
  collect(id: number, req: LeadCollectRequest): Promise<string> {
    return post<string>(`/leads/${id}/collect`, req)
  },

  /** 分发(OPP_ADMIN, COLLECTED → DISTRIBUTED) */
  distribute(id: number, managerId: number): Promise<string> {
    return post<string>(`/leads/${id}/distribute`, { managerId })
  },

  /** 培育(CUSTOMER_MANAGER 自己的, DISTRIBUTED, 可多次保存) */
  cultivate(id: number, info: CultivateInfo): Promise<string> {
    return put<string>(`/leads/${id}/cultivate`, info)
  },

  /** 转商机(CUSTOMER_MANAGER 自己的, DISTRIBUTED → CONVERTED + 创建商机占位) */
  convert(id: number): Promise<LeadConvertResponse> {
    return post<LeadConvertResponse>(`/leads/${id}/convert`)
  },
}
