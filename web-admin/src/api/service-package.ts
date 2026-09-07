import request from '@/utils/request'

// 套餐状态常量（与服务管理保持一致）
export const PACKAGE_STATUS = {
  RESTING: 0,     // 休息中
  AVAILABLE: 1,   // 可预约
} as const

export const PACKAGE_STATUS_MAP: Record<number, string> = {
  0: '休息中',
  1: '可预约',
}

export const PACKAGE_STATUS_OPTIONS = [
  { label: '休息中', value: 0 },
  { label: '可预约', value: 1 },
]

// 套餐要求类型（与 ServiceRequirement 字段完全一致）
export interface ServicePackageItem {
  id?: number
  servicePackageId?: number
  name: string
  value: string
}

// 服务套餐数据类型
export interface ServicePackage {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  price: number
  image: string
  description: string
  status: number
  servicePackageItems: ServicePackageItem[]
  createTime: string
  updateTime: string
  createUser: number
  updateUser: number
}

// 分页查询参数
export interface ServicePackagePageQueryParams {
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

// 添加/编辑服务套餐参数
export interface ServicePackageDTO {
  id?: number
  name: string
  categoryId: number
  price: number
  image: string
  description: string
  status?: number
  servicePackageItems: ServicePackageItem[]
}

/**
 * 分页查询服务套餐列表
 */
export function getServicePackagePage(params: ServicePackagePageQueryParams) {
  return request({
    url: '/employee/service-package/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询服务套餐
 */
export function getServicePackageById(id: number) {
  return request({
    url: `/employee/service-package/${id}`,
    method: 'get'
  })
}

/**
 * 新增服务套餐
 */
export function addServicePackage(data: ServicePackageDTO) {
  return request({
    url: '/employee/service-package',
    method: 'post',
    data
  })
}

/**
 * 编辑服务套餐
 */
export function updateServicePackage(data: ServicePackageDTO) {
  return request({
    url: '/employee/service-package',
    method: 'put',
    data
  })
}

/**
 * 更新服务套餐状态
 */
export function updateServicePackageStatus(status: number, id: number) {
  return request({
    url: `/employee/service-package/status/${status}`,
    method: 'post',
    params: { id }
  })
}

/**
 * 删除服务套餐
 */
export function deleteServicePackage(id: number) {
  return request({
    url: `/employee/service-package/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除服务套餐
 */
export function batchDeleteServicePackage(ids: number[]) {
  return request({
    url: '/employee/service-package/batch',
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
