import request from '@/utils/request'

// 服务介绍类型
export interface ServiceIntro {
  id: number
  title: string
  picture: string
  content: string
  createUser: number
  updateUser: number
  createTime: string
  updateTime: string
}

// 新增/编辑服务介绍参数类型
export interface ServiceIntroDTO {
  id?: number
  title: string
  picture?: string
  content?: string
}

// 分页查询参数类型
export interface ServiceIntroPageQueryParams {
  page: number
  pageSize: number
  title?: string
}

// 分页查询响应类型
export interface PageResult<T> {
  total: number
  records: T[]
}

/**
 * 分页查询服务介绍
 */
export function getServiceIntroPage(params: ServiceIntroPageQueryParams) {
  return request({
    url: '/admin/service-intro/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询服务介绍
 */
export function getServiceIntroById(id: number) {
  return request({
    url: `/admin/service-intro/${id}`,
    method: 'get'
  })
}

/**
 * 新增服务介绍
 */
export function addServiceIntro(data: ServiceIntroDTO) {
  return request({
    url: '/admin/service-intro',
    method: 'post',
    data
  })
}

/**
 * 修改服务介绍
 */
export function updateServiceIntro(data: ServiceIntroDTO) {
  return request({
    url: '/admin/service-intro',
    method: 'put',
    data
  })
}

/**
 * 删除服务介绍
 */
export function deleteServiceIntro(id: number) {
  return request({
    url: `/admin/service-intro/${id}`,
    method: 'delete'
  })
}
