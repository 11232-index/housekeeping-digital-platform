import request from '@/utils/request'

// 订单明细项
export interface OrderItem {
  id?: number | string
  orderId?: number | string
  housekeeperId?: number | string
  serviceItemId?: number | string
  servicePackageId?: number | string
  name?: string
  image?: string
  requirement?: string
  amount?: number | string | null
}

// 订单（列表/详情共用，分页接口每个元素也带 orderDetailList）
export interface Order {
  id: number | string
  number?: string
  status?: number
  payStatus?: number
  payMethod?: number
  /** 订单金额（用了优惠券时是折后实付金额） */
  amount?: number | string | null
  /** 使用的优惠券id（没用券为 null） */
  voucherId?: number | string | null
  /** 优惠券标题 */
  voucherTitle?: string | null
  /** 折扣率（80=八折） */
  voucherDiscount?: number | null
  /** 优惠金额 */
  discountAmount?: number | string | null
  phone?: string
  address?: string
  contactName?: string
  remark?: string
  orderTime?: string
  payTime?: string
  scheduleDate?: string
  scheduleTime?: string
  serviceDuration?: string
  estimatedServiceTime?: string
  actualServiceTime?: string
  cancelReason?: string
  /** 退款原因（员工端确认退款时填写） */
  refundReason?: string
  cancelTime?: string
  /** 该订单由哪位家政员接单（用户指定 或 员工指派） */
  housekeeperId?: number | string | null
  /** 家政员姓名 */
  housekeeperName?: string | null
  /** 员工指派时刻 */
  assignTime?: string
  orderDetailList?: OrderItem[]
}

export interface OrderPageQueryParams {
  page: number
  pageSize: number
  // 全部订单时 status 传 undefined
  status?: number
  number?: string
  phone?: string
  beginTime?: string
  endTime?: string
  // 订单类型：service=服务订单（服务项目/服务套餐），housekeeper=家政员订单，不传=全部
  orderType?: 'service' | 'housekeeper'
}

export function getOrderPage(params: OrderPageQueryParams) {
  return request({ url: '/employee/order/page', method: 'get', params })
}

export function getOrderById(id: number | string) {
  return request({ url: `/employee/order/${id}`, method: 'get' })
}

// 指派候选家政员（只返回该订单服务时段内空闲的家政员）
export interface HousekeeperCandidate {
  housekeeperId: number | string
  name?: string
  picture?: string
  price?: number | string | null
  rating?: number | string | null
  rank?: number | null
}

export function getAssignCandidates(orderId: number | string) {
  return request({ url: '/employee/order/candidates', method: 'get', params: { orderId } })
}

// 指派家政员（服务订单专用；指派后订单仍停在待接单，等家政员确认）
export function assignOrder(id: number | string, housekeeperId: number | string) {
  return request({ url: `/employee/order/${id}/assign`, method: 'post', data: { housekeeperId } })
}

// 取消待接单订单（原因必填）→ 待退款
export function staffCancelOrder(id: number | string, cancelReason: string) {
  return request({ url: `/employee/order/${id}/cancel`, method: 'post', data: { cancelReason } })
}

// 确认退款（退款原因必填）：待退款 → 已取消
export function refundOrder(id: number | string, refundReason: string) {
  return request({ url: `/employee/order/${id}/refund`, method: 'post', data: { refundReason } })
}

// 推进订单状态（后端只放行 4=开始服务、5=完成服务）
export function updateOrderStatus(id: number | string, status: number) {
  return request({ url: `/employee/order/status/${id}`, method: 'put', params: { status } })
}
