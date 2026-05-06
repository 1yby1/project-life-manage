// 周报 API — 与后端 WeeklyReportController 对应
import { get, post } from './request'

export type ReportStatus = 'DRAFT' | 'SUBMITTED' | 'COMMENTED'

export type WeeklyReportComment = {
  id: number
  reportId: number
  commenterId: number
  commenterName?: string
  content: string
  createTime?: string
}

export type WeeklyReport = {
  id: number
  userId: number
  userName?: string
  supervisorId: number
  supervisorName?: string
  year: number
  weekNum: number
  attendance?: string
  thisWeekWork?: string
  nextWeekPlan?: string
  status: ReportStatus
  submitTime?: string
  commentCount?: number
  createTime?: string
  updateTime?: string
}

export type WeeklyReportDetail = WeeklyReport & { comments: WeeklyReportComment[] }

export type WeeklyReportSaveRequest = {
  year: number
  weekNum: number
  attendance?: string
  thisWeekWork?: string
  nextWeekPlan?: string
}

export const weeklyReportApi = {
  listMy(): Promise<WeeklyReport[]> {
    return get<WeeklyReport[]>('/weekly-reports/my')
  },
  listTeam(): Promise<WeeklyReport[]> {
    return get<WeeklyReport[]>('/weekly-reports/team')
  },
  detail(id: number): Promise<WeeklyReportDetail> {
    return get<WeeklyReportDetail>(`/weekly-reports/${id}`)
  },
  saveDraft(req: WeeklyReportSaveRequest): Promise<number> {
    return post<number>('/weekly-reports/draft', req)
  },
  submit(id: number): Promise<string> {
    return post<string>(`/weekly-reports/${id}/submit`)
  },
  comment(id: number, content: string): Promise<string> {
    return post<string>(`/weekly-reports/${id}/comment`, { content })
  },
  teamSummary(year: number, weekNum: number): Promise<TeamSummary> {
    return get<TeamSummary>('/weekly-reports/team/summary', { year, weekNum })
  },
}

export type TeamSummaryMember = {
  userId: number
  userName: string
  /** null 表示未提交 */
  status?: ReportStatus | null
  reportId?: number
  submitTime?: string
}

export type TeamSummary = {
  year: number
  weekNum: number
  totalCount: number
  submittedCount: number
  commentedCount: number
  members: TeamSummaryMember[]
}

export function reportStatusLabel(s?: string): string {
  switch (s) {
    case 'DRAFT': return '草稿'
    case 'SUBMITTED': return '已提交'
    case 'COMMENTED': return '已点评'
    default: return s || '-'
  }
}

export function reportStatusTagType(s?: string): 'info' | 'warning' | 'success' | '' {
  switch (s) {
    case 'DRAFT': return 'info'
    case 'SUBMITTED': return 'warning'
    case 'COMMENTED': return 'success'
    default: return 'info'
  }
}

/** ISO 周数计算(取当前时间所在周) */
export function getISOWeek(date: Date = new Date()): { year: number; weekNum: number } {
  const tempDate = new Date(date.valueOf())
  const dayNum = (tempDate.getDay() + 6) % 7
  tempDate.setDate(tempDate.getDate() - dayNum + 3)
  const firstThursday = tempDate.valueOf()
  tempDate.setMonth(0, 1)
  if (tempDate.getDay() !== 4) {
    tempDate.setMonth(0, 1 + ((4 - tempDate.getDay()) + 7) % 7)
  }
  const weekNum = 1 + Math.ceil((firstThursday - tempDate.valueOf()) / 604800000)
  return { year: date.getFullYear(), weekNum }
}
