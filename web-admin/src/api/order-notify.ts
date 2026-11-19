import request from '@/utils/request'

// 订单提醒消息
export interface OrderNotifyItem {
  id: number | string
  type?: string
  title?: string
  content?: string
  orderId?: number | string
  orderNumber?: string
  amount?: number | string | null
  createTime?: string
  read?: boolean
}

export function getOrderNotifyList(limit = 30) {
  return request({ url: '/employee/order-notify/list', method: 'get', params: { limit } })
}

export function getUnreadCount() {
  return request({ url: '/employee/order-notify/unread-count', method: 'get' })
}

export function markNotifyRead(id: number | string) {
  return request({ url: `/employee/order-notify/${id}/read`, method: 'post' })
}

export function markAllNotifyRead() {
  return request({ url: '/employee/order-notify/read-all', method: 'post' })
}
