import request from '@/utils/request'

/** 统计查询参数：均为 yyyy-MM-dd，不传时后端默认统计最近 30 天 */
export interface StatisticsQueryParams {
  beginTime?: string
  endTime?: string
}

/** 营业额统计 */
export interface TurnoverStatistics {
  /** 总营业额 */
  totalTurnover: number
  /** 有效订单数（已完成且已支付） */
  validOrderCount: number
  /** 平均客单价 */
  avgOrderAmount: number
  /** 日期列表 */
  dateList: string[]
  /** 每日营业额，与 dateList 一一对应 */
  turnoverList: number[]
}

/** 用户统计 */
export interface UserStatistics {
  /** 用户总数 */
  totalUser: number
  /** 区间内新增用户数 */
  newUser: number
  /** 日期列表 */
  dateList: string[]
  /** 每日新增用户数，与 dateList 一一对应 */
  newUserList: number[]
}

/** 订单状态分布 */
export interface StatusCount {
  /** 1待付款 2待接单 3已接单 4服务中 5已完成 6退款中 7已取消 */
  status: number
  /** 状态名称 */
  name: string
  /** 该状态订单数 */
  count: number
}

/** 订单统计 */
export interface OrderStatistics {
  /** 订单总数 */
  totalOrder: number
  /** 有效订单数（已完成且已支付） */
  validOrder: number
  /** 已取消订单数（退款中 + 已取消） */
  cancelledOrder: number
  /** 日期列表 */
  dateList: string[]
  /** 每日订单数，与 dateList 一一对应 */
  orderList: number[]
  /** 各状态订单数分布 */
  statusList: StatusCount[]
}

/** 销量排名统计 */
export interface SalesRank {
  /** 服务名称列表（按销量从高到低） */
  nameList: string[]
  /** 销量列表 */
  numberList: number[]
  /** 销售额列表 */
  amountList: number[]
}

/** 营业额统计 */
export function getTurnoverStatistics(params?: StatisticsQueryParams) {
  return request({ url: '/admin/statistics/turnover', method: 'get', params })
}

/** 用户统计 */
export function getUserStatistics(params?: StatisticsQueryParams) {
  return request({ url: '/admin/statistics/user', method: 'get', params })
}

/** 订单统计 */
export function getOrderStatistics(params?: StatisticsQueryParams) {
  return request({ url: '/admin/statistics/order', method: 'get', params })
}

/** 销量排名统计（Top 10） */
export function getSalesRank(params?: StatisticsQueryParams) {
  return request({ url: '/admin/statistics/sales', method: 'get', params })
}
