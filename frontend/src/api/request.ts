// 统一的 HTTP 客户端封装
// - 自动注入 Bearer token
// - 自动解包 Result<T>(后端响应格式: { code, message, data })
// - 401/403 时清登录态并跳到登录页

const API_PREFIX = '/api'
const TOKEN_KEY = 'auth_token'

type ApiResult<T> = {
  code: number
  message: string
  data: T | null
}

export class ApiError extends Error {
  constructor(public code: number, message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

function getToken(): string | null {
  return window.localStorage.getItem(TOKEN_KEY)
}

function buildUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`
  return `${API_PREFIX}${p}`
}

function buildHeaders(extra?: Record<string, string>): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json; charset=utf-8',
    ...(extra || {}),
  }
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (res.status === 401 || res.status === 403) {
    // 登录失效 — 清状态并跳登录
    window.localStorage.removeItem(TOKEN_KEY)
    window.localStorage.removeItem('auth_user')
    if (window.location.pathname !== '/login') {
      window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
    }
    throw new ApiError(res.status, '登录已失效')
  }

  let body: ApiResult<T>
  try {
    body = (await res.json()) as ApiResult<T>
  } catch (e) {
    throw new ApiError(res.status, `服务器响应格式错误 (HTTP ${res.status})`)
  }

  if (body.code !== 200) {
    throw new ApiError(body.code, body.message || '请求失败')
  }
  return body.data as T
}

export async function get<T>(path: string, params?: Record<string, string | number | undefined | null>): Promise<T> {
  let url = buildUrl(path)
  if (params) {
    const search = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&')
    if (search) url += (url.includes('?') ? '&' : '?') + search
  }
  const res = await fetch(url, { method: 'GET', headers: buildHeaders() })
  return handleResponse<T>(res)
}

export async function post<T>(path: string, body?: any): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'POST',
    headers: buildHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  })
  return handleResponse<T>(res)
}

export async function put<T>(path: string, body?: any): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'PUT',
    headers: buildHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  })
  return handleResponse<T>(res)
}

export async function patch<T>(path: string, body?: any): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'PATCH',
    headers: buildHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  })
  return handleResponse<T>(res)
}

export async function del<T>(path: string): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'DELETE',
    headers: buildHeaders(),
  })
  return handleResponse<T>(res)
}

/**
 * 文件上传(multipart/form-data)— 自动注入 token,同样走 handleResponse,
 * 与 get/post 共享 401/403 自动跳登录与 Result<T> 解包逻辑。
 * 注意:不要手动设置 Content-Type,让浏览器附带 boundary。
 */
export async function upload<T>(
  path: string,
  file: File,
  extra?: Record<string, string>,
): Promise<T> {
  const fd = new FormData()
  fd.append('file', file)
  if (extra) {
    for (const [k, v] of Object.entries(extra)) {
      if (v !== undefined && v !== null) fd.append(k, v)
    }
  }
  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(buildUrl(path), {
    method: 'POST',
    headers,
    body: fd,
  })
  return handleResponse<T>(res)
}
