export type MeResponse = {
  id: string
  name: string
  roles: string[]
}

// Vite 环境变量：VITE_API_BASE_URL（建议）；兼容旧变量名不做强依赖
const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

export function isDemoToken(token: string | null) {
  return !!token && token.startsWith('demo_')
}

export function getApiUrl(path: string) {
  const base = API_BASE.replace(/\/+$/, '')
  const p = path.startsWith('/') ? path : `/${path}`
  return `${base}${p}`
}

export async function fetchMe(token: string): Promise<MeResponse | null> {
  if (!API_BASE) return null

  const res = await fetch(getApiUrl('/api/auth/me'), {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  if (!res.ok) return null
  const data = (await res.json()) as MeResponse
  return data
}

export async function loginWithPassword(username: string, password: string): Promise<string | null> {
  if (!API_BASE) return null

  const res = await fetch(getApiUrl('/api/auth/login'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })

  if (!res.ok) return null
  const data = (await res.json()) as { token?: string }
  return data.token || null
}

