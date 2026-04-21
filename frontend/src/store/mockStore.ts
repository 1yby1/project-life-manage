import { reactive } from 'vue'

type Id = string

export type CustomerManagerRole = '商机管理员' | '客户经理' | '合同管理员' | '线索收集人' | '线索分发人' | '客户/线索培育' | '项目经理'

export type CustomerStage = '已录入' | '已派单' | '走访中' | '走访完成'

export type Customer = {
  id: Id
  customerName: string
  city: string
  legalPerson: string
  address?: string
  registeredAddress?: string
  registeredOrg?: string
  unifyCreditCode?: string
  industry?: string
  contacts?: Array<{
    id: Id
    name: string
    title: string
    phone: string
  }>
  // 走访完成后锁定，不允许更新
  visitLocked: boolean
  // 流程阶段
  stage: CustomerStage
  // 录入人（商机管理员）
  createdByEmployeeId: Id
  // 录入时间
  createdAt: string
  // 当前指派的客户经理
  assignedEmployeeId?: Id
}

export type Employee = {
  id: Id
  dept: string
  name: string
  phone: string
  roleTag: CustomerManagerRole
}

export type CustomerDispatch = {
  id: Id
  customerId: Id
  employeeId: Id
  createdAt: string
}

export type CustomerVisit = {
  id: Id
  customerId: Id
  employeeId: Id
  startAt?: string
  endAt?: string
  contactMethod?: string
  host?: string
  address?: string
  minutes?: string
  result?: string
  completed: boolean
}

export type ContractStatus = '执行中' | '执行完成'

export type Contract = {
  id: Id
  contractName: string
  customerId: Id
  contractType: string
  contractAmount: number
  paymentTotal: number
  status: ContractStatus
  createdAt: string
  attachments: {
    contractBody?: string
    grossMarginTable?: string
    priceListTable?: string
  }
  paymentNodes: Array<{
    id: Id
    nodeName: string
    amount: number
    dueAt?: string
    paidAt?: string
  }>
}

export type ClueStage = '收集' | '分发' | '培育'
export type Clue = {
  id: Id
  customerId?: Id
  customerName?: string
  clueName: string
  clueDesc: string
  attachments?: string[]
  stage: ClueStage
  createdByEmployeeId: Id
  assignedCustomerManagerId?: Id
  // 培育必填信息（演示）
  projectName?: string
  expectedPurchaseTime?: string
  signingProbability?: number
  predictedAmount?: number
  clueLevel?: string
  solutionManager?: string
  opportunityOwnerId?: Id
  keyContact?: {
    name: string
    title: string
    phone: string
  }
  createdAt: string
}

export type OpportunityStage = '模板选择' | '推进中' | '已结束'
export type Opportunity = {
  id: Id
  clueId: Id
  customerId?: Id
  opportunityName: string
  stage: OpportunityStage
  templateKey?: string
  managerId?: Id
  // 组员/责任人（演示字段）
  triangleIds?: Id[]
  coreGroupIds?: Id[]
  tasks: Array<{
    id: Id
    taskName: string
    ownerId: Id
    status: '未关闭' | '关闭'
    progress: number
    content?: string
    attachments?: string[]
    updates: Array<{
      id: Id
      updatedById: Id
      updatedAt: string
      replyContent: string
      progress?: number
    }>
  }>
}

export type WeeklyReport = {
  id: Id
  employeeId: Id
  weekOf: string // yyyy-mm-dd
  attendanceByDay: Array<{ day: string; present: boolean; notes?: string }>
  thisWeekWork: string
  nextWeekPlan: string
  reviewerId?: Id
  reviewComment?: string
  reviewAt?: string
}

const state = reactive({
  employees: [] as Employee[],
  customers: [] as Customer[],
  dispatches: [] as CustomerDispatch[],
  visits: [] as CustomerVisit[],
  contracts: [] as Contract[],
  clues: [] as Clue[],
  opportunities: [] as Opportunity[],
  weeklyReports: [] as WeeklyReport[],
})

function initDemoData() {
  state.employees = [
    { id: 'e_1', dept: '市场部', name: '商机管理员A', phone: '13800000001', roleTag: '商机管理员' },
    { id: 'e_2', dept: '客户部', name: '客户经理B', phone: '13800000002', roleTag: '客户经理' },
    { id: 'e_3', dept: '合同部', name: '合同管理员C', phone: '13800000003', roleTag: '合同管理员' },
    { id: 'e_4', dept: '销售部', name: '线索收集D', phone: '13800000004', roleTag: '线索收集人' },
    { id: 'e_5', dept: '销售部', name: '线索分发E', phone: '13800000005', roleTag: '线索分发人' },
    { id: 'e_6', dept: '解决方案部', name: '方案培育F', phone: '13800000006', roleTag: '客户/线索培育' },
    { id: 'e_7', dept: '交付项目部', name: '项目经理G', phone: '13800000007', roleTag: '项目经理' },
    { id: 'e_8', dept: '铁三角', name: '铁三角H', phone: '13800000008', roleTag: '项目经理' },
  ]

  state.customers = [
    {
      id: 'c_1',
      customerName: '星河科技有限公司',
      city: '深圳',
      legalPerson: '张三',
      address: '深圳市南山区XX路1号',
      registeredAddress: '深圳市南山区XX路1号',
      registeredOrg: '星河控股',
      unifyCreditCode: '91440300MA5...',
      industry: '软件与信息技术服务',
      visitLocked: false,
      stage: '走访中',
      createdByEmployeeId: 'e_1',
      createdAt: new Date().toISOString(),
      assignedEmployeeId: 'e_2',
      contacts: [
        { id: 'ct_1', name: '李四', title: '采购经理', phone: '13900000001' },
      ],
    },
    {
      id: 'c_2',
      customerName: '北斗通信集团',
      city: '北京',
      legalPerson: '王五',
      registeredOrg: '北斗集团',
      visitLocked: false,
      stage: '已录入',
      createdByEmployeeId: 'e_1',
      createdAt: new Date().toISOString(),
      contacts: [],
    },
  ]

  state.dispatches = [
    { id: 'd_1', customerId: 'c_1', employeeId: 'e_2', createdAt: new Date().toISOString() },
  ]

  state.visits = [
    {
      id: 'v_1',
      customerId: 'c_1',
      employeeId: 'e_2',
      completed: false,
    },
  ]

  state.contracts = [
    {
      id: 'ct_1_contract',
      contractName: '星河科技-软件服务合同-2026',
      customerId: 'c_1',
      contractType: '软件服务',
      contractAmount: 120000,
      paymentTotal: 120000,
      status: '执行中',
      createdAt: new Date().toISOString(),
      attachments: {
        contractBody: '已上传',
        grossMarginTable: '已上传',
        priceListTable: '已上传',
      },
      paymentNodes: [
        { id: 'pn_1', nodeName: '首付款', amount: 60000, dueAt: '2026-04-01' },
        { id: 'pn_2', nodeName: '尾款', amount: 60000, dueAt: '2026-06-01' },
      ],
    },
    {
      id: 'ct_2_contract',
      contractName: '北斗通信-一期交付合同-2025',
      customerId: 'c_2',
      contractType: '交付',
      contractAmount: 80000,
      paymentTotal: 80000,
      status: '执行完成',
      createdAt: new Date(Date.now() - 365 * 24 * 3600 * 1000).toISOString(),
      attachments: {
        contractBody: '已上传',
        grossMarginTable: '已上传',
        priceListTable: '已上传',
      },
      paymentNodes: [
        { id: 'pn_3', nodeName: '首付款', amount: 40000, dueAt: '2025-03-01', paidAt: '2025-03-05' },
        { id: 'pn_4', nodeName: '尾款', amount: 40000, dueAt: '2025-05-01', paidAt: '2025-05-06' },
      ],
    },
  ]

  state.clues = [
    {
      id: 'l_1',
      customerId: 'c_1',
      customerName: '星河科技有限公司',
      clueName: '星河-二期需求',
      clueDesc: '对接二期采购与实施计划',
      attachments: ['附件1.pdf'],
      stage: '培育',
      createdByEmployeeId: 'e_4',
      assignedCustomerManagerId: 'e_2',
      projectName: '星河二期实施',
      expectedPurchaseTime: '2026-04',
      signingProbability: 0.65,
      predictedAmount: 120000,
      clueLevel: 'A',
      solutionManager: '张方案',
      opportunityOwnerId: 'e_7',
      keyContact: { name: '李四', title: '采购经理', phone: '13900000001' },
      createdAt: new Date().toISOString(),
    },
    {
      id: 'l_2',
      customerName: '北斗通信集团',
      clueName: '北斗-试点合作',
      clueDesc: '试点方案评审与资源协调',
      stage: '收集',
      createdByEmployeeId: 'e_4',
      createdAt: new Date().toISOString(),
    },
  ]

  state.opportunities = [
    {
      id: 'o_1',
      clueId: 'l_1',
      customerId: 'c_1',
      opportunityName: '星河科技-二期商机',
      stage: '推进中',
      templateKey: 'standard_4_steps',
      managerId: 'e_7',
      triangleIds: ['e_2', 'e_7', 'e_6'],
      coreGroupIds: ['e_7', 'e_8'],
      tasks: [
        {
          id: 't_1',
          taskName: '验证机会点',
          ownerId: 'e_7',
          status: '未关闭',
          progress: 35,
          content: '完成需求范围确认，待补齐机会点证据材料。',
          attachments: [],
          updates: [
            { id: 'tu_1', updatedById: 'e_7', updatedAt: new Date().toISOString(), replyContent: '已完成初稿梳理', progress: 35 },
          ],
        },
      ],
    },
  ]

  const today = new Date()
  const yyyy = today.getFullYear()
  const mm = String(today.getMonth() + 1).padStart(2, '0')
  const dd = String(today.getDate()).padStart(2, '0')

  state.weeklyReports = [
    {
      id: 'wr_1',
      employeeId: 'e_2',
      weekOf: `${yyyy}-${mm}-${dd}`,
      attendanceByDay: [
        { day: '周一', present: true },
        { day: '周二', present: true },
        { day: '周三', present: true },
        { day: '周四', present: true },
        { day: '周五', present: false, notes: '外出走访' },
      ],
      thisWeekWork: '完成对星河客户的走访与需求整理，推进合同资料准备。',
      nextWeekPlan: '继续与合同管理员对齐合同关键条款，安排现场验收准备。',
    },
  ]
}

initDemoData()

export function useMockStore() {
  return state
}

export function resetDemoData() {
  initDemoData()
}

export function getEmployeeIdByRoleLabel(roleLabel: string): string | undefined {
  // 演示：把顶部选择的角色映射到 mock 员工
  const roleTag = ((): CustomerManagerRole | undefined => {
    switch (roleLabel) {
      case '商机管理员':
        return '商机管理员'
      case '客户经理':
        return '客户经理'
      case '合同管理员':
        return '合同管理员'
      case '线索收集人':
        return '线索收集人'
      case '线索分发人':
        return '线索分发人'
      case '客户/线索培育':
        return '客户/线索培育'
      case '项目经理':
        return '项目经理'
      case '销售人员':
        return '客户经理'
      case '销售主管/管理层':
        return '客户经理'
      default:
        return undefined
    }
  })()

  if (!roleTag) return undefined
  const match = state.employees.find((e) => e.roleTag === roleTag)
  return match?.id
}

