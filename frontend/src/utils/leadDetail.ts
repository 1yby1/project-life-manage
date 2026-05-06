// 线索培育详情 form ↔ CultivateInfo 互转
// 后端 progress_desc TEXT 存 JSON;winRate 单独存 win_rate 列(0-100)
// 前端 form 用 0-1 概率,显示用 % 或简单 number
import type { CultivateInfo, LeadDetail } from '@/api/lead'

export type CultivateForm = {
  winRate: number  // 前端 0-1
  projectName: string
  expectedPurchaseTime: string
  predictedAmount: number
  clueLevel: '' | 'A' | 'B' | 'C'
  solutionManager: string
  opportunityOwnerId: number | ''
  keyContact: {
    name: string
    title: string
    phone: string
  }
}

export const EMPTY_CULTIVATE_FORM: CultivateForm = {
  winRate: 0.5,
  projectName: '',
  expectedPurchaseTime: '',
  predictedAmount: 0,
  clueLevel: '',
  solutionManager: '',
  opportunityOwnerId: '',
  keyContact: { name: '', title: '', phone: '' },
}

/** detail 里的 cultivateInfo + 主体 winRate → form */
export function parseCultivateInfo(detail: LeadDetail | null | undefined): CultivateForm {
  if (!detail) return { ...EMPTY_CULTIVATE_FORM }
  const info: CultivateInfo | undefined = detail.cultivateInfo
  // winRate 优先取主体 detail.winRate(0-100),否则从 cultivateInfo 取
  const winRateRaw = typeof detail.winRate === 'number' ? detail.winRate
                   : typeof info?.winRate === 'number' ? info.winRate
                   : 50
  return {
    winRate: winRateRaw / 100,
    projectName: info?.projectName || '',
    expectedPurchaseTime: info?.expectedPurchaseTime || '',
    predictedAmount: info?.predictedAmount || 0,
    clueLevel: (info?.clueLevel as CultivateForm['clueLevel']) || '',
    solutionManager: info?.solutionManager || '',
    opportunityOwnerId: typeof info?.opportunityOwnerId === 'number' ? info.opportunityOwnerId : '',
    keyContact: {
      name: info?.keyContact?.name || '',
      title: info?.keyContact?.title || '',
      phone: info?.keyContact?.phone || '',
    },
  }
}

/** form → CultivateInfo(用于 PUT /leads/:id/cultivate) */
export function serializeCultivateForm(form: CultivateForm): CultivateInfo {
  return {
    winRate: Math.round((form.winRate || 0) * 100),
    projectName: form.projectName || undefined,
    expectedPurchaseTime: form.expectedPurchaseTime || undefined,
    predictedAmount: form.predictedAmount || undefined,
    clueLevel: form.clueLevel ? form.clueLevel : undefined,
    solutionManager: form.solutionManager || undefined,
    opportunityOwnerId: typeof form.opportunityOwnerId === 'number' ? form.opportunityOwnerId : undefined,
    keyContact: {
      name: form.keyContact.name || undefined,
      title: form.keyContact.title || undefined,
      phone: form.keyContact.phone || undefined,
    },
  }
}

/** 后端 status → 前端中文阶段 */
export function leadStatusToStage(status?: string | null): string {
  switch (status) {
    case 'ENTRY': return '收集'
    case 'COLLECTED': return '分发'
    case 'DISTRIBUTED': return '培育'
    case 'CONVERTED': return '已转商机'
    default: return '-'
  }
}

export function leadStageTagType(status?: string | null): 'info' | 'warning' | 'success' | '' {
  switch (status) {
    case 'ENTRY': return 'info'
    case 'COLLECTED': return 'warning'
    case 'DISTRIBUTED': return 'success'
    case 'CONVERTED': return ''
    default: return 'info'
  }
}
