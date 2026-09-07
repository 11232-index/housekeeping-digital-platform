import request from '@/utils/request'

export interface Voucher {
  id: number | string
  orderId: number | string | null
  title: string
  subTitle: string
  rules: string
  payValue: number
  actualValue: number
  type: number
  status: number
  createTime: string
  updateTime: string
  stock: number | null
  beginTime: string | null
  endTime: string | null
}

export interface VoucherDTO {
  id?: number | string
  orderId?: number | string | null
  title?: string
  subTitle?: string
  rules?: string
  payValue?: number
  actualValue?: number
  type?: number
  status?: number
  stock?: number | null
  beginTime?: string | null
  endTime?: string | null
}

export interface VoucherPageQueryParams {
  page: number
  pageSize: number
  title?: string
  type?: number
  status?: number
}

export function getVoucherPage(params: VoucherPageQueryParams) {
  return request({ url: '/employee/voucher/page', method: 'get', params })
}

export function getVoucherById(id: number | string) {
  return request({ url: `/employee/voucher/${id}`, method: 'get' })
}

export function addVoucher(data: VoucherDTO) {
  return request({ url: '/employee/voucher', method: 'post', data })
}

export function updateVoucher(data: VoucherDTO) {
  return request({ url: '/employee/voucher', method: 'put', data })
}

export function updateVoucherStatus(status: number, id: number | string) {
  return request({ url: `/employee/voucher/status/${status}`, method: 'post', params: { id } })
}

export function deleteVoucher(id: number | string) {
  return request({ url: `/employee/voucher/${id}`, method: 'delete' })
}
