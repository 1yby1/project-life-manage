// 走访记录 form ↔ JSON 字符串互转
// 后端 crm_customer_visit.visit_record 是 TEXT,前端把 7 个字段序列化成 JSON 存入。
// 与 CustomerVisitView.vue 的表单结构对齐。

export type VisitForm = {
  startAt: string
  endAt: string
  contactMethod: string
  host: string
  address: string
  minutes: string
  result: string
}

export const EMPTY_VISIT_FORM: VisitForm = {
  startAt: '',
  endAt: '',
  contactMethod: '',
  host: '',
  address: '',
  minutes: '',
  result: '',
}

/** form → JSON 字符串 */
export function serializeVisitForm(form: VisitForm): string {
  return JSON.stringify(form)
}

/** JSON 字符串 → form(解析失败回退到空 form) */
export function parseVisitRecord(visitRecord: string | null | undefined): VisitForm {
  if (!visitRecord) return { ...EMPTY_VISIT_FORM }
  try {
    const parsed = JSON.parse(visitRecord) as Partial<VisitForm> | null
    if (!parsed || typeof parsed !== 'object') return { ...EMPTY_VISIT_FORM }
    return {
      startAt: parsed.startAt || '',
      endAt: parsed.endAt || '',
      contactMethod: parsed.contactMethod || '',
      host: parsed.host || '',
      address: parsed.address || '',
      minutes: parsed.minutes || '',
      result: parsed.result || '',
    }
  } catch {
    return { ...EMPTY_VISIT_FORM }
  }
}

/**
 * 把后端 visitStatus 映射到前端中文阶段
 * null/undefined → '已录入'(尚未派单)
 */
export function visitStatusToStage(status: string | null | undefined): string {
  switch (status) {
    case 'PENDING': return '已派单'
    case 'DOING': return '走访中'
    case 'COMPLETED': return '走访完成'
    default: return '已录入'
  }
}

export function visitStageTagType(status: string | null | undefined): 'info' | 'warning' | 'success' | '' {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'DOING': return ''
    case 'COMPLETED': return 'success'
    default: return 'info'
  }
}
