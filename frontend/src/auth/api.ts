// 后端响应统一包装格式: Result<T> = { code, message, data }
type ApiResult<T> = {
  code: number
  message: string
  data: T | null
}

export type MeResponse = {
  id: string
  name: string
  roles: string[]
}

type LoginData = {
  token: string
  userId: number
  username: string
  roleId: number
  realName?: string
  roles?: string[]
}

// 前端调用 /api/* — 通过 Vite proxy 转发到后端 http://localhost:8080/*
// (proxy 自动去掉 /api 前缀)
const API_PREFIX = '/api'

export function getApiUrl(path: string) {
  const p = path.startsWith('/') ? path : `/${path}`
  return `${API_PREFIX}${p}`
}

export async function fetchMe(token: string): Promise<MeResponse | null> {
  try {
    const res = await fetch(getApiUrl('/auth/me'), {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (!res.ok) return null
    const data = (await res.json()) as ApiResult<LoginData>
    if (data.code !== 200 || !data.data) return null

    return {
      id: String(data.data.userId),
      name: data.data.realName || data.data.username,
      roles: data.data.roles || [],
    }
  } catch (e) {
    console.error('fetchMe failed:', e)
    return null
  }
}

// 返回完整登录响应,供 authStore 直接构造 user(避免再调一次 /auth/me)
export async function loginWithPassword(
  username: string,
  password: string,
): Promise<LoginData | null> {
  try {
    const res = await fetch(getApiUrl('/auth/login'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

    if (!res.ok) return null
    const data = (await res.json()) as ApiResult<LoginData>
    if (data.code !== 200 || !data.data) return null
    return data.data
  } catch (e) {
    console.error('loginWithPassword failed:', e)
    return null
  }
}
