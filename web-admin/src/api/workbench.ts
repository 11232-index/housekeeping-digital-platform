import request from '@/utils/request'

/** 工作台待办 */
export interface WorkbenchTodo {
  /** 待接单（status=2） */
  pendingAccept: number
  /** 待指派（status=2 且未指派家政员） */
  pendingAssign: number
  /** 待退款（status=6） */
  refunding: number
}

/** 工作台当日指标（营业额相关字段仅 admin/manager 有值） */
export interface WorkbenchKpi {
  /** 当日下单数 */
  orderCount: number
  /** 当日预约数 */
  appointmentCount: number
  /** 当日新增用户数 */
  newUser: number
  /** 当日营业额（非 admin/manager 为 null） */
  turnover: number | null
  /** 当日有效订单数（非 admin/manager 为 null） */
  validOrderCount: number | null
  /** 平均客单价（非 admin/manager 为 null） */
  avgOrderAmount: number | null
}

/** 预约时段分布 */
export interface ScheduleSlot {
  /** 时段，如 8:00~9:00 */
  time: string
  /** 该时段预约单数 */
  count: number
}

/** 工作台聚合数据 */
export interface WorkbenchOverview {
  /** 统计日期 yyyy-MM-dd */
  date: string
  todo: WorkbenchTodo
  kpi: WorkbenchKpi
  timeSlots: ScheduleSlot[]
}

/** 获取员工端工作台聚合数据（date 不传默认今天） */
export function getWorkbenchOverview(date?: string) {
  return request({ url: '/employee/workbench/overview', method: 'get', params: { date } })
}
