// 合同管理 API — 与后端 ContractController + ContractPaymentController 对应
import { get, post, put } from './request'

export type ContractStatus = 'EXECUTING' | 'COMPLETED' | 'CLOSED'

export type ContractPayment = {
  id: number
  contractId: number
  nodeName: string
  planAmount: number
  actualAmount?: number
  status: number  // 0 未付 / 1 已付
  planDate?: string
  payTime?: string
  createTime?: string
}

export type Contract = {
  id: number
  contractName: string
  customerId: number
  customerName?: string
  oppId?: number
  oppName?: string
  contractType?: string
  totalAmount: number
  paidAmount?: number
  status: ContractStatus
  contractYear: number
  fileUrl?: string
  deliveryTime?: string
  closeTime?: string
  closeBy?: number
  closerName?: string
  createBy?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

export type ContractDetail = Contract & { payments: ContractPayment[] }

export type PaymentNodeRequest = {
  nodeName: string
  planAmount: number
  planDate?: string
}

export type ContractCreateRequest = {
  contractName: string
  customerId: number
  oppId?: number
  contractType?: string
  totalAmount: number
  contractYear: number
  fileUrl?: string
  deliveryTime?: string
  paymentNodes: PaymentNodeRequest[]
}

export type ContractListQuery = {
  page?: number
  size?: number
  keyword?: string
  customerName?: string
  status?: ContractStatus
  year?: number
  bu?: string
}

export type ContractListResponse = {
  records: Contract[]
  total: number
}

export type ContractTopicQuery = {
  page?: number
  size?: number
  year?: number
  bu?: string
}

export type ContractTopicListResponse = {
  records: Contract[]
  total: number
  /** 全量合同金额合计(不受分页影响) */
  totalAmountSum: number
  /** 全量已收款合计(不受分页影响) */
  totalPaidSum: number
}

export type ContractDimensionAggregate = {
  /** 分组键(year 是数字字符串、bu 是字符串) */
  dimension: string
  count: number
  totalAmount: number
  totalPaid: number
}

export const contractApi = {
  list(query: ContractListQuery = {}): Promise<ContractListResponse> {
    return get<ContractListResponse>('/contracts', {
      page: query.page || 1,
      size: query.size || 10,
      keyword: query.keyword,
      customerName: query.customerName,
      status: query.status,
      year: query.year,
      bu: query.bu,
    })
  },
  detail(id: number): Promise<ContractDetail> {
    return get<ContractDetail>(`/contracts/${id}`)
  },
  create(req: ContractCreateRequest): Promise<number> {
    return post<number>('/contracts', req)
  },
  close(id: number): Promise<string> {
    return post<string>(`/contracts/${id}/close`)
  },
  /** 标记验收时间(OPP_ADMIN, EXECUTING) — 全付 + 已验收 时合同自动 COMPLETED */
  setDelivery(id: number, deliveryTime: string): Promise<string> {
    return put<string>(`/contracts/${id}/delivery`, { deliveryTime })
  },
  inFlight(query: ContractTopicQuery = {}): Promise<ContractTopicListResponse> {
    return get<ContractTopicListResponse>('/contracts/in-flight', {
      page: query.page || 1,
      size: query.size || 10,
      year: query.year,
      bu: query.bu,
    })
  },
  accepted(query: ContractTopicQuery = {}): Promise<ContractTopicListResponse> {
    return get<ContractTopicListResponse>('/contracts/accepted', {
      page: query.page || 1,
      size: query.size || 10,
      year: query.year,
      bu: query.bu,
    })
  },
  acceptedByYear(bu?: string): Promise<ContractDimensionAggregate[]> {
    return get<ContractDimensionAggregate[]>('/contracts/accepted/by-year', { bu })
  },
  inFlightByBu(year?: number): Promise<ContractDimensionAggregate[]> {
    return get<ContractDimensionAggregate[]>('/contracts/in-flight/by-bu', { year })
  },
  markPaymentPaid(paymentId: number, actualAmount: number, payTime?: string): Promise<string> {
    return put<string>(`/contract-payments/${paymentId}/pay`, { actualAmount, payTime })
  },
}

export function contractStatusLabel(s?: string): string {
  switch (s) {
    case 'EXECUTING': return '执行中'
    case 'COMPLETED': return '已交付'
    case 'CLOSED': return '已关闭'
    default: return s || '-'
  }
}

export function contractStatusTagType(s?: string): 'info' | 'warning' | 'success' | 'danger' | '' {
  switch (s) {
    case 'EXECUTING': return 'warning'
    case 'COMPLETED': return 'success'
    case 'CLOSED': return 'info'
    default: return 'info'
  }
}

export function formatMoney(n?: number | null): string {
  if (n == null) return '-'
  return Number(n).toLocaleString('zh-CN', { style: 'currency', currency: 'CNY', minimumFractionDigits: 2 })
}
