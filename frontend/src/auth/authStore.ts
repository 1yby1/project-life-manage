import { reactive } from 'vue'
import { fetchMe, isDemoToken, loginWithPassword } from '@/auth/api'

export type AuthUser = {
  id: string
  name: string
  roles: string[]
}

const TOKEN_KEY = 'auth_token'

function getToken() {
  return window.localStorage.getItem(TOKEN_KEY)
}

function setToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token)
}

function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY)
}

const state = reactive({
  user: null as AuthUser | null,
  loading: false,
})

let mePromise: Promise<AuthUser | null> | null = null

function mapDemoRoleToUser(roleLabel: string): AuthUser {
  // demo：把顶部角色选择映射到前端角色字符串
  const roleMap: Record<string, string[]> = {
    商机管理员: ['商机管理员'],
    客户经理: ['客户经理'],
    合同管理员: ['合同管理员'],
    '线索收集人': ['线索收集人'],
    '线索分发人': ['线索分发人'],
    '客户/线索培育': ['客户/线索培育'],
    '项目经理': ['项目经理'],
    '销售人员': ['销售人员'],
    '销售主管/管理层': ['销售主管/管理层'],
  }

  const roles = roleMap[roleLabel] || ['客户经理']
  const id = `demo_${roleLabel}_${Date.now()}`
  return {
    id,
    name: roleLabel,
    roles,
  }
}

export function getAuthState() {
  return state
}

export function isDemoSession() {
  const token = getToken()
  return isDemoToken(token)
}

export async function ensureUser(): Promise<AuthUser | null> {
  const token = getToken()
  if (!token) return null

  // 避免重复请求
  if (state.user) return state.user
  if (mePromise) return mePromise

  mePromise = (async () => {
    state.loading = true
    try {
      if (isDemoToken(token)) {
        const roleLabel = window.localStorage.getItem('demo_role') || '客户经理'
        const demoUser = mapDemoRoleToUser(roleLabel)
        state.user = demoUser
        return demoUser
      }

      const me = await fetchMe(token)
      if (!me) return null

      const user: AuthUser = { id: me.id, name: me.name, roles: me.roles }
      state.user = user
      return user
    } finally {
      state.loading = false
      mePromise = null
    }
  })()

  return mePromise
}

export function logout() {
  clearToken()
  state.user = null
}

export async function loginAsDemoRole(roleLabel: string): Promise<void> {
  window.localStorage.setItem('demo_role', roleLabel)
  setToken(`demo_${roleLabel}_${Date.now()}`)
  state.user = mapDemoRoleToUser(roleLabel)
}

export async function login(username: string, password: string): Promise<boolean> {
  state.loading = true
  try {
    const token = await loginWithPassword(username, password)
    if (!token) return false
    setToken(token)
    state.user = null
    return true
  } finally {
    state.loading = false
  }
}

export function userHasAnyRole(user: AuthUser | null, roles: string[] | undefined) {
  if (!roles || roles.length === 0) return true
  if (!user) return false
  return roles.some((r) => user.roles.includes(r))
}

