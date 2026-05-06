// 商机模板 API — 与后端 OppTemplateController 对应
import { get } from './request'

export type TemplateStage = {
  id: number
  stageCode: string
  stageName: string
  sortOrder: number
  required: boolean
}

export type OppTemplate = {
  id: number
  templateName: string
  description?: string
  isDefault: boolean
  stages: TemplateStage[]
}

export const oppTemplateApi = {
  list(): Promise<OppTemplate[]> {
    return get<OppTemplate[]>('/opp-templates')
  },
}
