import request from '@/utils/request'

export interface About {
  id: number
  title: string
  sortOrder: number
  content: string
  createUser: number
  updateUser: number
  createTime: string
  updateTime: string
}

export interface AboutDTO {
  id?: number
  title: string
  sortOrder?: number
  content?: string
}

export interface AboutPageQueryParams {
  page: number
  pageSize: number
  title?: string
}

export function getAboutPage(params: AboutPageQueryParams) {
  return request({ url: '/admin/about/page', method: 'get', params })
}

export function addAbout(data: AboutDTO) {
  return request({ url: '/admin/about', method: 'post', data })
}

export function updateAbout(data: AboutDTO) {
  return request({ url: '/admin/about', method: 'put', data })
}

export function deleteAbout(id: number) {
  return request({ url: `/admin/about/${id}`, method: 'delete' })
}
