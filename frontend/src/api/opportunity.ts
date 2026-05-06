// 商机 API — 与后端 OpportunityController 对应
import { get, post, put } from './request'
import type { TaskListItem } from './oppTask'

export type OppStageStatus = 'PENDING' | 'DOING' | 'DONE'
export type OppStageCode = 'VALIDATE' | 'NEGOTIATE' | 'IMPLEMENT' | 'DELIVERY'

export type Opportunity = {
  id: number
  leadId?: number
  customerId: number
  customerName?: string
  oppName: string
  stage: string  // VALIDATE / NEGOTIATE / IMPLEMENT / DELIVERY
  templateId?: number
  templateName?: string
  managerId?: number
  managerName?: string
  pmId?: number
  pmName?: string
  smId?: number
  smName?: string
  dmId?: number
  dmName?: string
  createTime?: string
  updateTime?: string
}

export type OppStageInstance = {
  id: number
  oppId: number
  stageCode: string
  stageName: string
  sortOrder: number
  ownerId?: number
  ownerName?: string
  status: OppStageStatus
  startTime?: string
  endTime?: string
}

export type OpportunityDetail = Opportunity & {
  stages: OppStageInstance[]
  tasks: TaskListItem[]
}

export type OpportunityListQuery = {
  keyword?: string
  customerId?: number
  stage?: string
}

export const opportunityApi = {
  list(query: OpportunityListQuery = {}): Promise<Opportunity[]> {
    return get<Opportunity[]>('/opportunities', query)
  },
  detail(id: number): Promise<OpportunityDetail> {
    return get<OpportunityDetail>(`/opportunities/${id}`)
  },
  applyTemplate(id: number, templateId: number): Promise<string> {
    return post<string>(`/opportunities/${id}/apply-template`, { templateId })
  },
  setStageOwner(stageId: number, ownerId: number | null): Promise<string> {
    return put<string>(`/opportunities/stages/${stageId}/owner`, { ownerId })
  },
  advanceStage(id: number): Promise<string> {
    return post<string>(`/opportunities/${id}/advance-stage`)
  },
}

export function oppStageLabel(stage?: string): string {
  switch (stage) {
    case 'VALIDATE': return '验证机会点'
    case 'NEGOTIATE': return '谈判与签约'
    case 'IMPLEMENT': return '项目实施'
    case 'DELIVERY': return '验收与交付'
    default: return stage || '-'
  }
}

export function oppStageTagType(stage?: string): 'info' | 'warning' | 'success' | '' {
  switch (stage) {
    case 'VALIDATE': return 'info'
    case 'NEGOTIATE': return 'warning'
    case 'IMPLEMENT': return ''
    case 'DELIVERY': return 'success'
    default: return 'info'
  }
}

export function isTemplateApplied(opp?: Opportunity | OpportunityDetail | null): boolean {
  return !!(opp && opp.templateId)
}
