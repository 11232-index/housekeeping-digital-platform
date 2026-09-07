import request from '@/utils/request'

// 分类类型常量
export const CATEGORY_TYPE = {
  SERVICE_ITEM: 1,   // 服务分类
  SERVICE_PACKAGE: 2,   // 服务套餐分类
} as const

export const CATEGORY_TYPE_MAP: Record<number, string> = {
  1: '服务分类',
  2: '服务套餐分类',
}

// 分类数据类型
export interface Category {
  id: number
  name: string
  type: number
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 分页查询参数
export interface CategoryPageQueryParams {
  page: number
  pageSize: number
  name?: string
  type?: number
}

// 分页查询响应
export interface PageResult<T> {
  total: number
  records: T[]
}

// 添加/编辑分类参数
export interface CategoryDTO {
  id?: number
  name: string
  type: number
  sort?: number
}

/**
 * 分页查询分类列表
 */
export function getCategoryPage(params: CategoryPageQueryParams) {
  return request({
    url: '/admin/category/page',
    method: 'get',
    params
  })
}

/**
 * 查询全部分类（下拉列表用）
 */
export function getCategoryList(type?: number) {
  return request({
    url: '/admin/category/list',
    method: 'get',
    params: type !== undefined ? { type } : undefined
  })
}

/**
 * 根据ID查询分类
 */
export function getCategoryById(id: number) {
  return request({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

/**
 * 新增分类
 */
export function addCategory(data: CategoryDTO) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

/**
 * 编辑分类
 */
export function updateCategory(data: CategoryDTO) {
  return request({
    url: '/admin/category',
    method: 'put',
    data
  })
}

/**
 * 启用/禁用分类
 */
export function updateCategoryStatus(status: number, id: number) {
  return request({
    url: `/admin/category/status/${status}`,
    method: 'post',
    params: { id }
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id: number) {
  return request({
    url: `/admin/category/${id}`,
    method: 'delete'
  })
}
