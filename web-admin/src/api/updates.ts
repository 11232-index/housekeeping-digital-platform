import request from '@/utils/request'

// 动态消息类型
export interface Updates {
  id: number
  title: string
  picture: string
  content: string
  createUser: number
  updateUser: number
  createTime: string
  updateTime: string
}

// 新增/编辑动态消息参数类型
export interface UpdatesDTO {
  id?: number
  title: string
  picture?: string
  content?: string
}

// 分页查询参数类型
export interface UpdatesPageQueryParams {
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
 * 分页查询动态消息
 */
export function getUpdatesPage(params: UpdatesPageQueryParams) {
  return request({
    url: '/admin/updates/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询动态消息
 */
export function getUpdatesById(id: number) {
  return request({
    url: `/admin/updates/${id}`,
    method: 'get'
  })
}

/**
 * 新增动态消息
 */
export function addUpdates(data: UpdatesDTO) {
  return request({
    url: '/admin/updates',
    method: 'post',
    data
  })
}

/**
 * 修改动态消息
 */
export function updateUpdates(data: UpdatesDTO) {
  return request({
    url: '/admin/updates',
    method: 'put',
    data
  })
}

/**
 * 删除动态消息
 */
export function deleteUpdates(id: number) {
  return request({
    url: `/admin/updates/${id}`,
    method: 'delete'
  })
}
