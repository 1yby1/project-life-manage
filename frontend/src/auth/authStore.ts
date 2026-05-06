import { reactive } from 'vue'
import { fetchMe, loginWithPassword } from '@/auth/api'
import { RoleCode, isValidRoleCode } from '@/auth/roles'

export type AuthUser = {
  id: string
  name: string
  roles: RoleCode[]
}

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

// roleId → roleCode 映射,与 backend/src/main/resources/sql/schema.sql 中 sys_role INSERT 顺序一致
const ROLE_ID_TO_CODE: Record<number, RoleCode> = {
  1: 'ADMIN',
  2: 'OPP_ADMIN',
  3: 'CUSTOMER_MANAGER',
  4: 'SALES',
  5: 'SUPERVISOR',
  6: 'SOLUTION_MANAGER',
  7: 'DELIVERY_MANAGER',
  8: 'PROJECT_MANAGER',
  9: 'REGION_HEAD',
  10: 'PMO',
  11: 'USER',
}

function getToken() {
  return window.localStorage.getItem(TOKEN_KEY)
}

function setToken(token: string) {
  window.localStorage.setItem(TOKEN_KEY, token)
}

function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY)
}

function persistUser(user: AuthUser) {
  window.localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function loadPersistedUser(): AuthUser | null {
  try {
    const raw = window.localStorage.getItem(USER_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as AuthUser
    if (!parsed.id || !Array.isArray(parsed.roles)) return null
    return parsed
  } catch {
    return null
  }
}

function clearPersistedUser() {
  window.localStorage.removeItem(USER_KEY)
}

const state = reactive({
  user: null as AuthUser | null,
  loading: false,
})

let mePromise: Promise<AuthUser | null> | null = null

export function getAuthState() {
  return state
}

export async function ensureUser(): Promise<AuthUser | null> {
  const token = getToken()
  if (!token) return null

  if (state.user) return state.user
  if (mePromise) return mePromise

  mePromise = (async () => {
    state.loading = true
    try {
      // 优先用 login 时持久化的 user(避免每次刷新都打 /auth/me)
      const cached = loadPersistedUser()
      if (cached) {
        state.user = cached
        return cached
      }

      // 兜底: 调后端 /auth/me 拿用户信息
      const me = await fetchMe(token)
      if (!me) return null

      const roles = (me.roles || []).filter(isValidRoleCode)
      const user: AuthUser = { id: me.id, name: me.name, roles }
      state.user = user
      persistUser(user)
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
  clearPersistedUser()
  state.user = null
}

export async function login(username: string, password: string): Promise<boolean> {
  state.loading = true
  try {
    const result = await loginWithPassword(username, password)
    if (!result) return false

    setToken(result.token)

    // 后端如果返回 roles 数组直接用,否则根据 roleId 推导(兜底)
    let roles: RoleCode[] = []
    if (result.roles && result.roles.length > 0) {
      roles = result.roles.filter(isValidRoleCode)
    } else if (result.roleId && ROLE_ID_TO_CODE[result.roleId]) {
      roles = [ROLE_ID_TO_CODE[result.roleId]]
    }
    // 所有人兜底 USER 角色
    if (roles.indexOf('USER') === -1) roles.push('USER')

    const user: AuthUser = {
      id: String(result.userId),
      name: result.realName || result.username,
      roles,
    }

    state.user = user
    persistUser(user)
    return true
  } finally {
    state.loading = false
  }
}

export function userHasAnyRole(user: AuthUser | null, roles: RoleCode[] | undefined) {
  if (!roles || roles.length === 0) return true
  if (!user) return false
  return roles.some((r) => user.roles.indexOf(r) !== -1)
}
