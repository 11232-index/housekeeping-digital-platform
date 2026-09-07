import axios from 'axios'

const pyApi = axios.create({
  baseURL: 'http://localhost:8000',
  timeout: 60000
})

// 请求拦截：携带 token
pyApi.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers['token'] = token
  }
  return config
})

export interface SummaryData {
  sections: { icon: string; title: string; content: string }[]
  overallRating: {
    label: string
    type: string
    comment: string
  }
}

/**
 * 生成员工智能总结（非流式）
 */
export function generateSummary(employeeId: number) {
  return pyApi.post<{ code: number; msg: string; data: SummaryData }>(
    `/admin/employee/${employeeId}/summary`
  )
}

/**
 * 生成员工智能总结（SSE 流式）
 * 返回 fetch Response 对象，调用方自行读取 body stream
 */
export function generateSummaryStream(employeeId: number): Promise<Response> {
  const token = sessionStorage.getItem('token') || ''
  return fetch(`http://localhost:8000/admin/employee/${employeeId}/summary/stream`, {
    headers: { token }
  })
}
