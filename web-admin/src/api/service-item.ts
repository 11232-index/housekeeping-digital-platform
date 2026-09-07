import request from '@/utils/request'

// 服务状态常量
export const SERVICE_STATUS = {
  RESTING: 0,     // 休息中
  AVAILABLE: 1,   // 可预约
} as const

export const SERVICE_STATUS_MAP: Record<number, string> = {
  0: '休息中',
  1: '可预约',
}

export const SERVICE_STATUS_OPTIONS = [
  { label: '休息中', value: 0 },
  { label: '可预约', value: 1 },
]

// 服务要求类型
export interface ServiceRequirement {
  id?: number
  serviceItemId?: number
  name: string
  value: string
}

// 服务项目数据类型
export interface ServiceItem {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  price: number
  image: string
  description: string
  status: number
  requirements: ServiceRequirement[]
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
}

// 分页查询参数
export interface ServiceItemPageQueryParams {
  page: number
  pageSize: number
  name?: string
  categoryId?: number
  status?: number
}

// 分页查询响应
export interface PageResult<T> {
  total: number
  records: T[]
}

// 添加/编辑服务项目参数
export interface ServiceItemDTO {
  id?: number
  name: string
  categoryId: number
  price: number
  image: string
  description: string
  status?: number
  requirements: ServiceRequirement[]
}

/**
 * 分页查询服务项目列表
 */
export function getServiceItemPage(params: ServiceItemPageQueryParams) {
  return request({
    url: '/employee/service-item/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询服务项目
 */
export function getServiceItemById(id: number) {
  return request({
    url: `/employee/service-item/${id}`,
    method: 'get'
  })
}

/**
 * 新增服务项目
 */
export function addServiceItem(data: ServiceItemDTO) {
  return request({
    url: '/employee/service-item',
    method: 'post',
    data
  })
}

/**
 * 编辑服务项目
 */
export function updateServiceItem(data: ServiceItemDTO) {
  return request({
    url: '/employee/service-item',
    method: 'put',
    data
  })
}

/**
 * 更新服务项目状态
 */
export function updateServiceItemStatus(status: number, id: number) {
  return request({
    url: `/employee/service-item/status/${status}`,
    method: 'post',
    params: { id }
  })
}

/**
 * 删除服务项目
 */
export function deleteServiceItem(id: number) {
  return request({
    url: `/employee/service-item/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除服务项目
 */
export function batchDeleteServiceItem(ids: number[]) {
  return request({
    url: '/employee/service-item/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 上传图片（后端 CommonController: POST /admin/common/upload）
 */
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/common/upload',
    method: 'post',
    data: formData
  })
}
