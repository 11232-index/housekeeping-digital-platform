import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // API基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const url = config.url || ''

    // 登录接口不需要token校验，直接放行
    const isLoginRequest = url.includes('/admin/auth/login') || url.includes('/employee/auth/login')

    if (isLoginRequest) {
      return config
    }

    // 判断是员工端还是管理端请求，使用不同的 token 键名
    const isEmployeeRequest = url.startsWith('/employee/')
    const tokenKey = isEmployeeRequest ? 'employeeToken' : 'token'
    const expireKey = isEmployeeRequest ? 'employeeTokenExpireTime' : 'tokenExpireTime'
    const loginPath = isEmployeeRequest ? '/employee/login' : '/login'

    // 获取 token：管理端请求优先用 token，找不到则 fallback 到 employeeToken
    // （因为员工端页面可能调用 /admin/** 接口，如分类管理、上传文件等）
    let token = sessionStorage.getItem(tokenKey)
    if (!token && !isEmployeeRequest) {
      token = sessionStorage.getItem('employeeToken')
    }

    if (token) {
      // 检查token是否过期（使用实际获取到 token 的那个 key 的过期时间）
      const actualExpireKey = sessionStorage.getItem(tokenKey) ? expireKey : 'employeeTokenExpireTime'
      const tokenExpireTime = sessionStorage.getItem(actualExpireKey)
      if (tokenExpireTime) {
        const expireTime = parseInt(tokenExpireTime)
        const currentTime = Date.now()

        if (currentTime >= expireTime) {
          // token过期，清除对应的本地存储并跳转
          const actualTokenKey = sessionStorage.getItem(tokenKey) ? tokenKey : 'employeeToken'
          const isActuallyEmployee = !isEmployeeRequest && !sessionStorage.getItem(tokenKey)
          sessionStorage.removeItem(actualTokenKey)
          sessionStorage.removeItem(actualExpireKey)
          if (isEmployeeRequest || isActuallyEmployee) {
            sessionStorage.removeItem('employeeName')
            sessionStorage.removeItem('employeeFullName')
            sessionStorage.removeItem('employeeId')
          } else {
            sessionStorage.removeItem('userName')
            sessionStorage.removeItem('userId')
          }
          // 如果是 admin 请求但实际在用 employeeToken，跳转到员工登录页
          const actualLoginPath = isActuallyEmployee ? '/employee/login' : loginPath
          window.location.href = actualLoginPath
          return Promise.reject(new Error('Token已过期，请重新登录'))
        }
      }

      config.headers['token'] = token
    }

    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    if (res.code === 1) {
      return res
    } else {
      console.error('接口错误:', res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 401: {
          // 判断是员工端还是管理端请求
          const requestUrl = error.config?.url || ''
          const isEmployeeRequest = requestUrl.startsWith('/employee/')
          const tokenKey = isEmployeeRequest ? 'employeeToken' : 'token'
          let loginPath = isEmployeeRequest ? '/employee/login' : '/login'

          // 如果是 /admin/** 请求但管理端 token 不存在，可能是员工在调用管理端接口
          // 此时应清除员工 token 并跳转到员工登录页
          if (!isEmployeeRequest && !sessionStorage.getItem('token') && sessionStorage.getItem('employeeToken')) {
            sessionStorage.removeItem('employeeToken')
            sessionStorage.removeItem('employeeTokenExpireTime')
            sessionStorage.removeItem('employeeName')
            sessionStorage.removeItem('employeeFullName')
            sessionStorage.removeItem('employeeId')
            loginPath = '/employee/login'
          } else {
            sessionStorage.removeItem(tokenKey)
          }

          console.error('未授权，请重新登录')
          window.location.href = loginPath
          break
        }
        case 403:
          console.error('拒绝访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error(`连接错误: ${status}`)
      }
    } else {
      console.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service
