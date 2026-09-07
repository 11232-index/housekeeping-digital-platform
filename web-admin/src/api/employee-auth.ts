import request from '@/utils/request'

// 员工登录参数类型
export interface EmployeeLoginParams {
  username: string
  password: string
}

// 员工登录响应类型
export interface EmployeeLoginResponse {
  code: number
  msg?: string
  data: {
    id: number
    userName: string
    name: string
    token: string
    role: string
  }
}

// 员工修改密码参数类型
export interface EmployeePasswordEditParams {
  empId?: number
  oldPassword: string
  newPassword: string
}

/**
 * 员工登录
 */
export function employeeLogin(data: EmployeeLoginParams): Promise<EmployeeLoginResponse> {
  return request({
    url: '/employee/auth/login',
    method: 'post',
    data
  })
}

/**
 * 员工退出登录
 */
export function employeeLogout() {
  return request({
    url: '/employee/auth/logout',
    method: 'post'
  })
}

/**
 * 员工修改密码
 */
export function employeeEditPassword(data: EmployeePasswordEditParams) {
  return request({
    url: '/employee/auth/editPassword',
    method: 'put',
    data
  })
}
