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
  keyword?: string
  customerName?: string
  status?: ContractStatus
  year?: number
  bu?: string
}

export const contractApi = {
  list(query: ContractListQuery = {}): Promise<Contract[]> {
    return get<Contract[]>('/contracts', query)
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
  inFlight(year?: number, bu?: string): Promise<Contract[]> {
    return get<Contract[]>('/contracts/in-flight', { year, bu })
  },
  accepted(year?: number, bu?: string): Promise<Contract[]> {
    return get<Contract[]>('/contracts/accepted', { year, bu })
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
