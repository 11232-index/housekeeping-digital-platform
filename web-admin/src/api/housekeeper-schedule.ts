import request from '@/utils/request'

export interface HousekeeperSchedule {
  id: number
  housekeeperId: number
  housekeeperName: string
  scheduleDate: string
  startTime: string
  endTime: string
  status: number
  createTime: string
  updateTime: string
}

export interface HousekeeperScheduleDTO {
  id?: number
  housekeeperId?: number
  scheduleDate?: string
  startTime?: string
  endTime?: string
  status?: number
}

export interface HousekeeperSchedulePageQueryParams {
  page: number
  pageSize: number
  housekeeperId?: number
  scheduleDate?: string
}

export function getSchedulePage(params: HousekeeperSchedulePageQueryParams) {
  return request({ url: '/employee/housekeeper-schedule/page', method: 'get', params })
}

export function addSchedule(data: HousekeeperScheduleDTO) {
  return request({ url: '/employee/housekeeper-schedule', method: 'post', data })
}

export function updateSchedule(data: HousekeeperScheduleDTO) {
  return request({ url: '/employee/housekeeper-schedule', method: 'put', data })
}

export function deleteSchedule(id: number) {
  return request({ url: `/employee/housekeeper-schedule/${id}`, method: 'delete' })
}

/** 切换预约时段状态（可约 ↔ 不可约） */
export function toggleScheduleStatus(id: number, status: number) {
  return request({ url: '/employee/housekeeper-schedule', method: 'put', data: { id, status } })
}
