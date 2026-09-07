import request from '@/utils/request'

/** 获取店铺营业状态 */
export function getShopStatus() {
  return request({
    url: '/admin/shop/getStatus',
    method: 'get'
  })
}

/** 设置店铺营业状态（1-营业中，0-打烊中）*/
export function setShopStatus(status: number) {
  return request({
    url: `/admin/shop/setStatus/${status}`,
    method: 'get'
  })
}
