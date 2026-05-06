// 商机组员 API — 与后端 OppTeamController 对应
// 权限: CORE 仅 PM; SUPPORT 铁三角(PM/SM/DM/manager)
import { get, post, del } from './request'

export type MemberType = 'CORE' | 'SUPPORT'

export type TeamMember = {
  id: number
  oppId: number
  userId: number
  userName?: string
  username?: string
  memberType: MemberType
  groupName?: string
  role?: string
  addBy?: number
  addByName?: string
  createTime?: string
}

export type TeamMemberAddRequest = {
  oppId: number
  userId: number
  memberType: MemberType
  groupName?: string
  role?: string
}

export const oppTeamApi = {
  list(oppId: number): Promise<TeamMember[]> {
    return get<TeamMember[]>('/opp-team', { oppId })
  },
  add(req: TeamMemberAddRequest): Promise<number> {
    return post<number>('/opp-team', req)
  },
  remove(id: number): Promise<string> {
    return del<string>(`/opp-team/${id}`)
  },
}

export function memberTypeLabel(t?: string): string {
  return t === 'CORE' ? '核心组' : t === 'SUPPORT' ? '支撑组' : '-'
}
