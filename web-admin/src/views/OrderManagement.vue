<template>
  <div class="order-management">
    <!-- 当前版块标题 -->
    <div class="page-title">{{ pageTitle }}</div>

    <!-- 顶部状态导航 -->
    <el-tabs v-model="activeTab" class="order-tabs" @tab-change="handleTabChange">
      <el-tab-pane v-for="item in statusTabs" :key="item.name" :label="item.label" :name="item.name" />
    </el-tabs>

    <!-- 查询栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchForm.number" placeholder="请输入订单号" clearable style="width:220px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable style="width:200px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-date-picker
          v-model="timeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="下单开始日期"
          end-placeholder="下单结束日期"
          value-format="YYYY-MM-DD"
          style="margin-right:10px"
        />
        <el-button type="default" style="background:#000;color:#fff" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <!-- 订单列表 -->
    <el-table :data="tableData" style="width:100%;margin-top:20px" border>
      <el-table-column prop="number" label="订单号" min-width="200" />
      <el-table-column label="订单名称" min-width="160">
        <template #default="{ row }">
          <span>{{ orderName(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="家政员" min-width="150">
        <template #default="{ row }">
          <span>{{ housekeeperText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="地址" min-width="240" show-overflow-tooltip />
      <el-table-column label="预计到达时间" min-width="170">
        <template #default="{ row }">
          <span>{{ row.estimatedServiceTime || '' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="150">
        <template #default="{ row }">
          <span>{{ row.remark || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款/取消原因" min-width="180">
        <template #default="{ row }">
          <span>{{ reasonText(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="260" fixed="right">
        <template #default="{ row }">
          <!-- 待接单：服务订单可指派（家政员订单在用户下单时已指定人选） -->
          <el-button v-if="row.status === 2 && canAssign(row)" link type="primary" @click="openAssign(row)">指派</el-button>
          <!-- 待接单 / 已接单 / 服务中 都可取消（中途出现突发情况时终止订单） -->
          <el-button v-if="canCancel(row)" link type="warning" @click="openCancel(row)">取消</el-button>
          <el-button v-if="row.status === 3" link type="primary" @click="advanceStatus(row, 4)">开始服务</el-button>
          <el-button v-if="row.status === 4" link type="success" @click="advanceStatus(row, 5)">完成服务</el-button>
          <el-button v-if="row.status === 6" link type="danger" @click="openRefund(row)">退款</el-button>
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :page-sizes="[5,10,20,50]" :total="pagination.total"
        layout="total,sizes,prev,pager,next,jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="760px" :close-on-click-modal="false">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ detail.number }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="statusTagType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ detail.orderTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">{{ detail.amount != null ? '¥' + detail.amount : '—' }}</el-descriptions-item>
            <el-descriptions-item label="优惠券">{{ voucherText(detail) }}</el-descriptions-item>
            <el-descriptions-item label="优惠金额">{{ detail.discountAmount != null ? '-¥' + detail.discountAmount : '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="预约日期">{{ detail.scheduleDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="预约时段">{{ detail.scheduleTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="服务时长">{{ detail.serviceDuration || '—' }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ payMethodText(detail.payMethod) }}</el-descriptions-item>
            <el-descriptions-item label="预计到达时间">{{ detail.estimatedServiceTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="实际上门时间">{{ detail.actualServiceTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="付款时间">{{ detail.payTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ payStatusText(detail.payStatus) }}</el-descriptions-item>
            <el-descriptions-item label="家政员">{{ housekeeperText(detail) }}</el-descriptions-item>
            <el-descriptions-item label="指派时间">{{ detail.assignTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="取消/拒单原因" :span="2">{{ detail.cancelReason || '—' }}</el-descriptions-item>
            <el-descriptions-item label="退款原因" :span="2">{{ detail.refundReason || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>

          <h4 style="margin:16px 0 8px">服务明细</h4>
          <el-table :data="detail.orderDetailList || []" border size="small">
            <el-table-column prop="name" label="服务名称" min-width="160" />
            <el-table-column label="服务要求" min-width="160">
              <template #default="{ row }">
                <span>{{ row.requirement || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" min-width="110">
              <template #default="{ row }">
                <span>{{ row.amount != null ? '¥' + row.amount : '—' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else description="暂无订单数据" />
      </div>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 指派家政员弹窗 -->
    <el-dialog v-model="assignVisible" title="指派家政员" width="640px" :close-on-click-modal="false">
      <div v-loading="assignLoading">
        <p v-if="assignOrderInfo" class="dialog-tip">订单服务时段：{{ assignOrderInfo }}</p>
        <el-table
          v-if="candidates.length > 0"
          :data="candidates"
          border
          size="small"
          row-key="housekeeperId"
        >
          <el-table-column width="70" label="选择" align="center">
            <template #default="{ row }">
              <el-radio v-model="selectedHousekeeperId" :value="row.housekeeperId"><span></span></el-radio>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" min-width="100" />
          <el-table-column label="身价" min-width="90">
            <template #default="{ row }">{{ rankText(row.rank) }}</template>
          </el-table-column>
          <el-table-column label="评分" min-width="80">
            <template #default="{ row }">{{ row.rating != null ? row.rating : '—' }}</template>
          </el-table-column>
          <el-table-column label="单价" min-width="100">
            <template #default="{ row }">{{ row.price != null ? '¥' + row.price : '—' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="!assignLoading" description="该时段没有空闲的家政员" />
      </div>
      <template #footer>
        <el-button @click="assignVisible = false">返回</el-button>
        <el-button type="primary" :loading="assignSubmitting" :disabled="selectedHousekeeperId == null" @click="submitAssign">确定指派</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="520px" :close-on-click-modal="false">
      <div class="quick-reasons">
        <el-tag v-for="r in CANCEL_REASONS" :key="r" class="quick-reason" @click="cancelForm.cancelReason = r">{{ r }}</el-tag>
      </div>
      <el-input
        v-model="cancelForm.cancelReason"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="请填写取消原因（必填）"
      />
      <template #footer>
        <el-button @click="cancelVisible = false">返回</el-button>
        <el-button type="warning" :loading="cancelSubmitting" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 确认退款弹窗 -->
    <el-dialog v-model="refundVisible" title="确认退款" width="520px" :close-on-click-modal="false">
      <div class="quick-reasons">
        <el-tag v-for="r in REFUND_REASONS" :key="r" class="quick-reason" @click="refundForm.refundReason = r">{{ r }}</el-tag>
      </div>
      <el-input
        v-model="refundForm.refundReason"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="请填写退款原因（必填）"
      />
      <template #footer>
        <el-button @click="refundVisible = false">返回</el-button>
        <el-button type="danger" :loading="refundSubmitting" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderPage, getOrderById, getAssignCandidates, assignOrder,
  staffCancelOrder, refundOrder, updateOrderStatus,
  type Order, type OrderItem, type HousekeeperCandidate
} from '@/api/order'

const route = useRoute()

// 订单类型由路由 props 传入：service=服务订单，housekeeper=家政员订单
const props = defineProps<{
  orderType?: 'service' | 'housekeeper'
}>()

// 当前版块标题
const pageTitle = computed(() => {
  if (props.orderType === 'housekeeper') return '家政员订单管理'
  if (props.orderType === 'service') return '服务订单管理'
  return '订单管理'
})

// 顶部状态导航（name 对应后端 status，all 表示全部）
const statusTabs = [
  { name: 'all', label: '全部订单', status: undefined },
  { name: '1', label: '待付款', status: 1 },
  { name: '2', label: '待接单', status: 2 },
  { name: '6', label: '待退款', status: 6 },
  { name: '3', label: '已接单', status: 3 },
  { name: '4', label: '服务中', status: 4 },
  { name: '5', label: '已完成', status: 5 },
  { name: '7', label: '已取消', status: 7 }
]

const activeTab = ref('all')
const searchForm = reactive({ number: '', phone: '' })
const timeRange = ref<string[]>([])
const tableData = ref<Order[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const searchLoading = ref(false)

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<Order | null>(null)

// 当前选中导航对应的订单状态
const currentStatus = (): number | undefined => {
  const tab = statusTabs.find((t) => t.name === activeTab.value)
  return tab ? tab.status : undefined
}

const loadList = async () => {
  const [begin, end] = timeRange.value || []
  const params: any = {
    page: pagination.page,
    pageSize: pagination.pageSize,
    status: currentStatus(),
    number: searchForm.number || undefined,
    phone: searchForm.phone || undefined,
    beginTime: begin || undefined,
    // 结束日期要包含当天，补到 23:59:59
    endTime: end ? end + ' 23:59:59' : undefined,
    // 当前版块对应订单类型
    orderType: props.orderType
  }
  const res: any = await getOrderPage(params)
  if (res.code === 1) {
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } else {
    ElMessage.error(res.msg || '查询失败')
  }
}

const handleSearch = async () => {
  searchLoading.value = true
  try {
    pagination.page = 1
    await loadList()
  } finally {
    searchLoading.value = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  loadList()
}

const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }

// 订单名称：明细首项名称 + 等N项
const orderName = (row: Order): string => {
  const list: OrderItem[] = row.orderDetailList || []
  if (list.length === 0) return '—'
  const first = list[0]?.name || ''
  return list.length > 1 ? `${first} 等${list.length}项` : first || '—'
}

// 状态映射
const statusText = (s?: number) => {
  const map: Record<number, string> = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '服务中', 5: '已完成', 6: '待退款', 7: '已取消' }
  return s !== undefined && map[s] ? map[s] : '未知'
}
const statusTagType = (s?: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'warning', 3: 'primary', 4: 'primary', 5: 'success', 6: 'warning', 7: 'info' }
  return (s !== undefined && map[s] ? map[s] : 'info') as any
}
const payMethodText = (m?: number) => (m === 1 ? '微信支付' : m === 2 ? '支付宝支付' : '—')
const payStatusText = (s?: number) => (s === 0 ? '未支付' : s === 1 ? '已支付' : s === 2 ? '退款' : '—')

// 退款/取消原因：
// - 待退款(6)：cancelReason 记录的是「为什么转入待退款」（超时/员工取消/家政员拒单）
// - 已取消(7)：refundReason 是员工确认退款时填的，cancelReason 保留此前转入待退款的原因
const reasonText = (row: Order): string => {
  // 待接单：可能是被家政员拒单后退回来的，cancelReason 里存着拒单原因
  if (row.status === 2 && row.cancelReason) return '上次拒单原因：' + row.cancelReason
  if (row.status === 6) return '待退款原因：' + (row.cancelReason || '—')
  if (row.status === 7) {
    const parts: string[] = []
    if (row.refundReason) parts.push('退款原因：' + row.refundReason)
    if (row.cancelReason) parts.push('取消原因：' + row.cancelReason)
    return parts.length > 0 ? parts.join('；') : '—'
  }
  return '—'
}

// 查看订单全部信息
const openDetail = async (row: Order) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res: any = await getOrderById(row.id)
    detail.value = res.code === 1 && res.data ? res.data : row
  } catch (e: any) {
    detail.value = row
    ElMessage.warning('详情加载失败，展示列表数据')
  } finally {
    detailLoading.value = false
  }
}

// ==================== 指派家政员 ====================
const RANK_TEXT: Record<number, string> = { 0: '无牌', 1: '金牌', 2: '银牌', 3: '铜牌' }
const rankText = (r?: number | null) => (r != null && RANK_TEXT[r] ? RANK_TEXT[r] : '—')

/** 只有尚未指定家政员的订单（即服务订单）才需要指派；家政员订单在下单时就有人了 */
const canAssign = (row: Order) => !row.housekeeperId

/** 可取消的状态：待接单 / 已接单 / 服务中。终态（已完成/待退款/已取消）不可取消 */
const canCancel = (row: Order) => row.status === 2 || row.status === 3 || row.status === 4

const assignVisible = ref(false)
const assignLoading = ref(false)
const assignSubmitting = ref(false)
const candidates = ref<HousekeeperCandidate[]>([])
const selectedHousekeeperId = ref<number | string | null>(null)
const assignOrderInfo = ref('')
const assignTarget = ref<Order | null>(null)

// 候选随时段被占用而变化，所以每次打开弹窗时重新拉，不在列表加载时预取
const openAssign = async (row: Order) => {
  if (assignLoading.value) {
    ElMessage.warning('正在加载候选家政员，请稍候')
    return
  }
  assignTarget.value = row
  selectedHousekeeperId.value = null
  candidates.value = []
  assignOrderInfo.value = `${row.scheduleDate || ''} ${row.scheduleTime || ''}`.trim()
  assignVisible.value = true
  assignLoading.value = true
  try {
    const res: any = await getAssignCandidates(row.id)
    if (res.code === 1) {
      candidates.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载候选家政员失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载候选家政员失败')
    assignVisible.value = false
  } finally {
    assignLoading.value = false
  }
}

const submitAssign = async () => {
  if (assignSubmitting.value) {
    ElMessage.warning('正在指派中，请稍候')
    return
  }
  const row = assignTarget.value
  if (!row || selectedHousekeeperId.value == null) return
  assignSubmitting.value = true
  try {
    const res: any = await assignOrder(row.id, selectedHousekeeperId.value)
    if (res.code === 1) {
      ElMessage.success('指派成功，等待家政员确认接单')
      assignVisible.value = false
    } else {
      ElMessage.error(res.msg || '指派失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '指派失败')
  } finally {
    // 等列表刷新完再放开按钮：刷新还没回来时行上仍是旧状态、按钮还在，
    // 用户再点一次就会打到后端、报「该订单当前不可指派」这类状态错误
    try { await loadList() } catch { /* 刷新失败不影响主流程提示 */ }
    assignSubmitting.value = false
  }
}

// ==================== 取消 / 终止订单 ====================
const CANCEL_REASONS = ['超时无人接单', '客户要求取消', '家政员无法到场', '地址无法服务', '服务内容有变', '其他原因']
const cancelVisible = ref(false)
const cancelSubmitting = ref(false)
const cancelForm = reactive<{ orderId: number | string | null; cancelReason: string }>({
  orderId: null,
  cancelReason: ''
})

const openCancel = (row: Order) => {
  if (cancelSubmitting.value) {
    ElMessage.warning('正在取消订单，请稍候')
    return
  }
  cancelForm.orderId = row.id
  cancelForm.cancelReason = ''
  cancelVisible.value = true
}

const submitCancel = async () => {
  if (cancelSubmitting.value) {
    ElMessage.warning('正在取消订单，请稍候')
    return
  }
  if (cancelForm.orderId == null) return
  if (!cancelForm.cancelReason.trim()) {
    ElMessage.warning('请填写取消原因')
    return
  }
  cancelSubmitting.value = true
  try {
    const res: any = await staffCancelOrder(cancelForm.orderId, cancelForm.cancelReason.trim())
    if (res.code === 1) {
      ElMessage.success('已取消，订单转入待退款')
      cancelVisible.value = false
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '取消失败')
  } finally {
    // 等列表刷新完再放开按钮，避免刷新空档里重复点击打到后端
    try { await loadList() } catch { /* 刷新失败不影响主流程提示 */ }
    cancelSubmitting.value = false
  }
}

// ==================== 确认退款 ====================
const REFUND_REASONS = ['超时无人接单', '家政员拒单', '客户要求退款', '无法提供服务', '其他原因']
const refundVisible = ref(false)
const refundSubmitting = ref(false)
const refundForm = reactive<{ orderId: number | string | null; refundReason: string }>({
  orderId: null,
  refundReason: ''
})

const openRefund = (row: Order) => {
  if (refundSubmitting.value) {
    ElMessage.warning('正在确认退款，请稍候')
    return
  }
  refundForm.orderId = row.id
  refundForm.refundReason = ''
  refundVisible.value = true
}

const submitRefund = async () => {
  if (refundSubmitting.value) {
    ElMessage.warning('正在确认退款，请稍候')
    return
  }
  if (refundForm.orderId == null) return
  if (!refundForm.refundReason.trim()) {
    ElMessage.warning('请填写退款原因')
    return
  }
  refundSubmitting.value = true
  try {
    const res: any = await refundOrder(refundForm.orderId, refundForm.refundReason.trim())
    if (res.code === 1) {
      ElMessage.success('退款完成，订单已取消')
      refundVisible.value = false
    } else {
      ElMessage.error(res.msg || '退款失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '退款失败')
  } finally {
    // 等列表刷新完再放开按钮，避免刷新空档里重复点击打到后端
    try { await loadList() } catch { /* 刷新失败不影响主流程提示 */ }
    refundSubmitting.value = false
  }
}

// ==================== 推进状态（开始服务 / 完成服务） ====================
const advancingId = ref<string | null>(null)

const advanceStatus = async (row: Order, target: number) => {
  const label = target === 4 ? '开始服务' : '完成服务'
  if (advancingId.value !== null) {
    ElMessage.warning(`正在${label}，请稍候`)
    return
  }
  try {
    await ElMessageBox.confirm(`确认将订单 ${row.number || ''} 标记为「${label}」吗？`, '提示', {
      type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消'
    })
  } catch {
    return
  }
  advancingId.value = String(row.id)
  try {
    const res: any = await updateOrderStatus(row.id, target)
    if (res.code === 1) {
      ElMessage.success(`${label}成功`)
    } else {
      ElMessage.error(res.msg || `${label}失败`)
    }
  } catch (e: any) {
    ElMessage.error(e.message || `${label}失败`)
  } finally {
    // 等列表刷新完再放开按钮，避免刷新空档里重复点击打到后端
    try { await loadList() } catch { /* 刷新失败不影响主流程提示 */ }
    advancingId.value = null
  }
}

/**
 * 家政员展示。
 * housekeeperName 由后端在订单查询时填充（OrderServiceImpl#toOrderVO）。
 * 「已指派」这个兜底分支只在极端情况出现 —— housekeeper_id 有值但对应家政员记录已被删除。
 */
/** 订单用券情况：没用券显示「未使用」，用了则显示「券标题（8折）」 */
const voucherText = (row: Order) => {
  if (!row.voucherId) return '未使用'
  const rate = row.voucherDiscount ? `${row.voucherDiscount / 10}折` : ''
  const title = row.voucherTitle || '优惠券'
  return rate ? `${title}（${rate}）` : title
}

const housekeeperText = (row: Order) => {
  if (row.housekeeperName) return `已指派（${row.housekeeperName}）`
  if (row.housekeeperId) return '已指派'
  return row.status === 2 ? '待指派' : '—'
}

// 两个订单版块共用本组件，切换版块时重新从第一页加载
watch(
  () => props.orderType,
  () => {
    pagination.page = 1
    loadList()
  }
)

onMounted(() => {
  loadList()
})

// 从消息中心跳转进来：只要路由变化（含同订单再次点击的时间戳），就重新打开对应订单详情
watch(
  () => route.fullPath,
  () => {
    const orderId = route.query.orderId
    if (orderId) {
      openDetail({ id: String(orderId) } as Order)
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.order-management { padding: 0; min-height: 100%; }
.page-title { font-size: 18px; font-weight: 600; color: #1e3a8a; margin-bottom: 16px; }
.dialog-tip { font-size: 13px; color: #909399; margin: 0 0 12px; }
.quick-reasons { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.quick-reason { cursor: pointer; }
.quick-reason:hover { color: #1e3a8a; border-color: #1e3a8a; }
.order-tabs { background: #fff; padding: 10px 20px 0; border-radius: 4px; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; margin-top: 20px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
</style>
