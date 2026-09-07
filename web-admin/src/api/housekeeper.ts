import request from '@/utils/request'

export interface Housekeeper {
  id: number
  employeeId: number
  name: string
  picture: string
  carousel: string
  price: number
  rating: number
  rank: number
  review: string
  createTime: string
  updateTime: string
}

export interface HousekeeperDTO {
  id?: number
  name?: string
  picture?: string
  carousel?: string
  price?: number
  rating?: number
  rank?: number
  review?: string
}

export interface HousekeeperPageQueryParams {
  page: number
  pageSize: number
  name?: string
}

export function getHousekeeperPage(params: HousekeeperPageQueryParams) {
  return request({ url: '/admin/housekeeper/page', method: 'get', params })
}

export function getHousekeeperById(id: number) {
  return request({ url: `/admin/housekeeper/${id}`, method: 'get' })
}

export function updateHousekeeper(data: HousekeeperDTO) {
  return request({ url: '/admin/housekeeper', method: 'put', data })
}
