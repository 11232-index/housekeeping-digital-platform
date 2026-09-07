import request from '@/utils/request'

// 登录接口参数类型
export interface LoginParams {
  username: string
  password: string
}

// 登录响应数据类型
export interface LoginResponse {
  code: number
  msg?: string
  data: {
    id: number
    userName: string
    name: string
    token: string
  }
}

// 员工信息类型
export interface Employee {
  id: number
  username: string
  name: string
  phone: string
  sex: string
  age: string
  idNumber: string
  status: number
  role: string
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
}

// 修改密码参数类型
export interface PasswordEditParams {
  empId?: number
  oldPassword: string
  newPassword: string
}

// 分页查询参数类型
export interface EmployeePageQueryParams {
  page: number
  pageSize: number
  name?: string
}

// 分页查询响应类型
export interface PageResult<T> {
  total: number
  records: T[]
}

// 添加/编辑员工参数类型
export interface EmployeeDTO {
  id?: number
  username: string
  password?: string
  name: string
  phone: string
  sex: string
  age?: string
  idNumber: string
  role?: string
}

/**
 * 员工登录
 */
export function login(data: LoginParams): Promise<LoginResponse> {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

/**
 * 员工退出登录
 */
export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

/**
 * 根据ID查询员工信息
 */
export function getEmployeeById(id: number) {
  return request({
    url: `/admin/employee/${id}`,
    method: 'get'
  })
}

/**
 * 修改密码
 */
export function editPassword(data: PasswordEditParams) {
  return request({
    url: '/admin/auth/editPassword',
    method: 'put',
    data
  })
}

/**
 * 分页查询员工列表
 */
export function getEmployeePage(params: EmployeePageQueryParams) {
  return request({
    url: '/admin/employee/page',
    method: 'get',
    params
  })
}

/**
 * 新增员工
 */
export function addEmployee(data: EmployeeDTO) {
  return request({
    url: '/admin/employee',
    method: 'post',
    data
  })
}

/**
 * 编辑员工
 */
export function updateEmployee(data: EmployeeDTO) {
  return request({
    url: '/admin/employee',
    method: 'put',
    data
  })
}

/**
 * 启用/禁用员工账号
 */
export function updateEmployeeStatus(status: number, id: number) {
  return request({
    url: `/admin/employee/status/${status}`,
    method: 'post',
    params: { id }
  })
}

// 员工详细信息类型
export interface EmployeeDetail {
  id?: number
  employeeId: number
  education?: string
  pastExperience?: string
  position?: string
  salary?: string
  workStatus?: string
  contract?: string
  healthCertificate?: string
  updateTime?: string
}

/**
 * 根据员工ID查询详细信息
 */
export function getEmployeeDetail(employeeId: number) {
  return request({
    url: `/admin/employee/detail/${employeeId}`,
    method: 'get'
  })
}

/**
 * 新增员工详细信息
 */
export function addEmployeeDetail(data: EmployeeDetail) {
  return request({
    url: '/admin/employee/detail',
    method: 'post',
    data
  })
}

/**
 * 修改员工详细信息
 */
export function updateEmployeeDetail(data: EmployeeDetail) {
  return request({
    url: '/admin/employee/detail',
    method: 'put',
    data
  })
}

/**
 * 根据员工ID删除详细信息
 */
export function deleteEmployeeDetail(employeeId: number) {
  return request({
    url: `/admin/employee/detail/${employeeId}`,
    method: 'delete'
  })
}

/**
 * 文件上传
 */
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/common/upload',
    method: 'post',
    data: formData,
    // 不设置 Content-Type，让浏览器自动设置 boundary
  })
}

/**
 * 下载/查看文件（返回blob URL）
 * 支持阿里云 OSS 远程 URL（https:// 开头）和本地相对路径
 */
export async function fetchFileAsBlob(fileUrl: string): Promise<string | null> {
  const token = sessionStorage.getItem('token')
  try {
    // 判断是否为 OSS 远程 URL（以 http:// 或 https:// 开头）
    const isRemoteUrl = fileUrl.startsWith('http://') || fileUrl.startsWith('https://')
    const fetchUrl = isRemoteUrl ? fileUrl : `http://localhost:8080${fileUrl}`
    const response = await fetch(fetchUrl, {
      headers: isRemoteUrl ? {} : { token: token || '' }
    })
    if (!response.ok) return null
    const blob = await response.blob()
    return URL.createObjectURL(blob)
  } catch {
    return null
  }
}

/**
 * 删除员工
 */
export function deleteEmployee(id: number) {
  return request({
    url: `/admin/employee/${id}`,
    method: 'delete'
  })
}

/**
 * 修改管理员姓名
 */
export function editName(name: string) {
  return request({
    url: '/admin/auth/editName',
    method: 'put',
    data: name
  })
}

/**
 * 获取密保问题（忘记密码，无需登录）
 */
export function getSecurityQuestion(username: string) {
  return request({
    url: '/admin/auth/securityQuestion',
    method: 'get',
    params: { username },
    // 此接口无需登录，request拦截器无token时会放行
  })
}

/**
 * 设置密保（登录后）
 */
export function setSecurity(question: string, answer: string) {
  return request({
    url: '/admin/auth/security',
    method: 'put',
    data: { question, answer }
  })
}

/**
 * 重置密码（忘记密码，无需登录）
 */
export function resetPassword(username: string, answer: string, newPassword: string) {
  return request({
    url: '/admin/auth/resetPassword',
    method: 'post',
    data: { username, answer, newPassword }
  })
}
