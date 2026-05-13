// 文件上传 API — 与后端 FileController(POST /files/upload, OPP_ADMIN)对应
import { upload } from './request'

export type FileUploadResult = {
  url: string
  originalName: string
  size: number
}

export const fileApi = {
  /**
   * 通用文件上传:走统一 axios-like 封装,自动注入 Bearer token,
   * 401/403 自动跳登录页,Result<T> 自动解包。
   * @param file 浏览器 File 对象
   * @param category 业务分类(如 'contracts'、'weekly-reports'),后端用于归档目录
   */
  upload(file: File, category: string): Promise<FileUploadResult> {
    return upload<FileUploadResult>('/files/upload', file, { category })
  },
}
