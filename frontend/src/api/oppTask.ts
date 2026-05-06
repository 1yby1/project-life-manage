// 商机任务 API — 与后端 OppTaskController 对应
import { get, post, put } from './request'

export type TaskStatus = 'TODO' | 'DOING' | 'DONE'

export type TaskListItem = {
  id: number
  oppId: number
  stageId?: number
  stageName?: string
  taskName: string
  content?: string
  assigneeId?: number
  assigneeName?: string
  assignBy?: number
  assignByName?: string
  replyContent?: string
  status: TaskStatus
  /** 完成进度 0-100 */
  progress?: number
  closeTime?: string
  createTime?: string
  updateTime?: string
}

export type TaskCreateRequest = {
  oppId: number
  stageId?: number
  taskName: string
  content?: string
  assigneeId?: number
}

export type TaskUpdateRequest = {
  taskName?: string
  content?: string
  stageId?: number
  assigneeId?: number
}

export const oppTaskApi = {
  list(oppId?: number, stageId?: number): Promise<TaskListItem[]> {
    return get<TaskListItem[]>('/opp-tasks', { oppId, stageId })
  },
  create(req: TaskCreateRequest): Promise<number> {
    return post<number>('/opp-tasks', req)
  },
  update(id: number, req: TaskUpdateRequest): Promise<string> {
    return put<string>(`/opp-tasks/${id}`, req)
  },
  reply(id: number, replyContent: string): Promise<string> {
    return post<string>(`/opp-tasks/${id}/reply`, { replyContent })
  },
  close(id: number): Promise<string> {
    return post<string>(`/opp-tasks/${id}/close`)
  },
  updateProgress(id: number, progress: number): Promise<string> {
    return put<string>(`/opp-tasks/${id}/progress`, { progress })
  },
}

export function taskStatusLabel(s?: string): string {
  switch (s) {
    case 'TODO': return '待办'
    case 'DOING': return '进行中'
    case 'DONE': return '已关闭'
    default: return '-'
  }
}

export function taskStatusTagType(s?: string): 'info' | 'warning' | 'success' | '' {
  switch (s) {
    case 'TODO': return 'info'
    case 'DOING': return 'warning'
    case 'DONE': return 'success'
    default: return 'info'
  }
}
