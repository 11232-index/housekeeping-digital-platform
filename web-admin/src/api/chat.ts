import request from '@/utils/request'

export interface ChatSession {
  id?: number
  adminId?: number
  title: string
  createTime?: string
  updateTime?: string
}

export interface ChatSessionPageResult {
  total: number
  records: ChatSession[]
}

/**
 * 调用 Python AI 智能体，将问题总结为 7~12 字标题
 */
export function summarizeTitle(message: string): Promise<Response> {
  const token = sessionStorage.getItem('token') || ''
  return fetch('http://localhost:8000/admin/chat/session', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      token,
    },
    body: JSON.stringify({ message }),
  })
}

/**
 * 创建对话会话（首次提问时调用，title = AI 总结后的标题）
 */
export function createChatSession(title: string) {
  return request({
    url: '/admin/chat/session',
    method: 'post',
    data: { title }
  })
}

/**
 * 分页查询历史会话（支持按标题搜索）
 */
export function getChatSessionPage(page: number, pageSize: number, title?: string) {
  return request({
    url: '/admin/chat/session/page',
    method: 'get',
    params: { page, pageSize, title }
  })
}

/**
 * 重命名会话
 */
export function renameChatSession(id: number, title: string) {
  return request({
    url: `/admin/chat/session/${id}`,
    method: 'put',
    data: { title }
  })
}

/**
 * 删除会话
 */
export function deleteChatSession(id: number) {
  return request({
    url: `/admin/chat/session/${id}`,
    method: 'delete'
  })
}

/**
 * 删除当前管理员所有历史会话
 */
export function deleteAllChatSessions() {
  return request({
    url: '/admin/chat/session',
    method: 'delete'
  })
}

/**
 * 智能问答 —— SSE 流式请求
 * 调用 Python 后端 /admin/chat/stream，返回 fetch Response 供调用方读取 body stream
 */
export function chatStream(message: string): Promise<Response> {
  const token = sessionStorage.getItem('token') || ''
  return fetch('http://localhost:8000/admin/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      token,
    },
    body: JSON.stringify({ message }),
  })
}

/**
 * 智能问答 —— 获取 AI 随机生成的引导问题
 * 每次调用返回 3 个不同的员工管理相关问题
 */
export function getSuggestions(): Promise<Response> {
  const token = sessionStorage.getItem('token') || ''
  return fetch('http://localhost:8000/admin/chat/suggestions', {
    method: 'GET',
    headers: { token },
  })
}

