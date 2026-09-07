import request from '@/utils/request'

export interface HotPicks {
  id: number
  name: string
  picture: string
  status: number
  sortOrder: number
  createUser: number
  updateUser: number
  createTime: string
  updateTime: string
}

export interface HotPicksDTO {
  id?: number
  name?: string
  picture?: string
  status?: number
  sortOrder?: number
}

export interface HotPicksPageQueryParams {
  page: number
  pageSize: number
  name?: string
  status?: number
}

export function getHotPicksPage(params: HotPicksPageQueryParams) {
  return request({ url: '/admin/hot-picks/page', method: 'get', params })
}

export function getHotPicksById(id: number) {
  return request({ url: `/admin/hot-picks/${id}`, method: 'get' })
}

export function addHotPicks(data: HotPicksDTO) {
  return request({ url: '/admin/hot-picks', method: 'post', data })
}

export function updateHotPicks(data: HotPicksDTO) {
  return request({ url: '/admin/hot-picks', method: 'put', data })
}

export function updateHotPicksStatus(status: number, id: number) {
  return request({ url: `/admin/hot-picks/status/${status}`, method: 'post', params: { id } })
}

export function deleteHotPicks(id: number) {
  return request({ url: `/admin/hot-picks/${id}`, method: 'delete' })
}
