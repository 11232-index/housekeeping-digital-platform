<template>
  <div class="workbench" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="wb-toolbar">
      <div class="wb-title">
        <span class="wb-icon">🏠</span>
        <span class="wb-name">工作台</span>
        <span class="wb-sub">当日待办与排期</span>
      </div>
      <div class="wb-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          size="small"
          class="wb-date"
          @change="loadData"
        />
        <el-button size="small" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </div>

    <!-- 待办事项 -->
    <div class="section-title">待办事项</div>
    <div class="todo-grid">
      <div
        v-for="card in todoCards"
        :key="card.label"
        class="todo-card"
        :class="[card.color, { clickable: canManage }]"
        :title="canManage ? '前往订单管理' : ''"
        @click="goOrders"
      >
        <span class="todo-icon">{{ card.icon }}</span>
        <span class="todo-value">{{ card.value }}</span>
        <span class="todo-label">{{ card.label }}</span>
        <span class="todo-hint">{{ card.hint }}</span>
      </div>
      <div class="todo-card purple clickable" title="打开订单提醒" @click="emit('open-notify')">
        <span class="todo-icon">🔔</span>
        <span class="todo-value">{{ unreadCount }}</span>
        <span class="todo-label">未读提醒</span>
        <span class="todo-hint">来单 / 客户催单</span>
      </div>
    </div>

    <!-- 当日概况 -->
    <div class="section-title">当日概况</div>
    <div class="kpi-grid">
      <div v-for="card in kpiCards" :key="card.label" class="kpi-card" :class="card.color">
        <div class="kpi-top">
          <span class="kpi-label">{{ card.label }}</span>
          <span class="kpi-icon">{{ card.icon }}</span>
        </div>
        <div class="kpi-value">{{ card.value }}</div>
      </div>
    </div>

    <!-- 当日预约时段 -->
    <el-card shadow="never" class="wb-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">📅 当日预约时段</span>
          <span class="card-tip">共 {{ overview.kpi.appointmentCount }} 单</span>
        </div>
      </template>
      <div v-if="overview.timeSlots.length > 0" class="timeline">
        <div v-for="slot in overview.timeSlots" :key="slot.time" class="slot">
          <span class="slot-dot" :class="{ busy: slot.count > 0 }"></span>
          <div class="slot-time">{{ slot.time }}</div>
          <div class="slot-count">{{ slot.count }} 单</div>
        </div>
      </div>
      <el-empty v-else description="当天暂无预约" :image-size="70" />
    </el-card>

    <!-- 快捷入口（仅管理角色可用） -->
    <el-card v-if="canManage" shadow="never" class="wb-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">⚡ 快捷入口</span>
        </div>
      </template>
      <div class="quick-grid">
        <div v-for="entry in quickEntries" :key="entry.path" class="quick-item" @click="router.push(entry.path)">
          <span class="quick-icon">{{ entry.icon }}</span>
          <div class="quick-text">
            <span class="quick-label">{{ entry.label }}</span>
            <span class="quick-desc">{{ entry.desc }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkbenchOverview } from '@/api/workbench'
import type { WorkbenchOverview } from '@/api/workbench'

const props = defineProps<{
  role?: string
  /** 未读提醒数，由父组件（EmployeeHome）统一轮询维护，保证跟顶部铃铛一致 */
  unreadCount?: number
}>()

const emit = defineEmits<{
  (e: 'open-notify'): void
}>()

const router = useRouter()

interface TodoCard {
  label: string
  value: number
  icon: string
  color: string
  hint: string
}

interface KpiCard {
  label: string
  value: string
  icon: string
  color: string
}

interface QuickEntry {
  icon: string
  label: string
  desc: string
  path: string
}

const loading = ref(false)
const selectedDate = ref(formatDate(new Date()))

const overview = ref<WorkbenchOverview>({
  date: '',
  todo: { pendingAccept: 0, pendingAssign: 0, refunding: 0 },
  kpi: {
    orderCount: 0,
    appointmentCount: 0,
    newUser: 0,
    turnover: null,
    validOrderCount: null,
    avgOrderAmount: null
  },
  timeSlots: []
})

/** admin / manager 才能操作订单、查看营业额 */
const canManage = computed(() => ['admin', 'manager'].includes(props.role || 'staff'))
/** 后端对非管理角色返回 null，这里双重保证不展示 */
const showMoney = computed(() => canManage.value && overview.value.kpi.turnover !== null)

const todoCards = computed<TodoCard[]>(() => [
  { label: '待接单', value: overview.value.todo.pendingAccept, icon: '🕒', color: 'orange', hint: '等待家政员确认' },
  { label: '待指派', value: overview.value.todo.pendingAssign, icon: '🧑‍🔧', color: 'blue', hint: '还没有家政员' },
  { label: '待退款', value: overview.value.todo.refunding, icon: '💸', color: 'red', hint: '待确认退款' }
])

const kpiCards = computed<KpiCard[]>(() => {
  const kpi = overview.value.kpi
  const list: KpiCard[] = [
    { label: '当日订单', value: `${kpi.orderCount} 单`, icon: '📋', color: 'blue' },
    { label: '当日预约', value: `${kpi.appointmentCount} 单`, icon: '📅', color: 'cyan' }
  ]
  if (showMoney.value) {
    list.push(
      { label: '当日营业额', value: `¥${formatMoney(kpi.turnover)}`, icon: '💰', color: 'green' },
      { label: '平均客单价', value: `¥${formatMoney(kpi.avgOrderAmount)}`, icon: '🧾', color: 'orange' }
    )
  }
  list.push({ label: '当日新增用户', value: `${kpi.newUser} 人`, icon: '👥', color: 'purple' })
  return list
})

const quickEntries: QuickEntry[] = [
  { icon: '📋', label: '订单管理', desc: '接单 / 指派 / 退款', path: '/employee/order/service' },
  { icon: '📅', label: '预约排班', desc: '家政员可约时段', path: '/employee/housekeeper-schedule' },
  { icon: '🛠', label: '服务管理', desc: '服务项目上下架', path: '/employee/service-item' },
  { icon: '🎟️', label: '优惠券', desc: '发券与秒杀', path: '/employee/voucher' }
]

function formatDate(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function formatMoney(value: number | null | undefined) {
  const num = Number(value ?? 0) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const goOrders = () => {
  if (!canManage.value) return
  router.push('/employee/order/service')
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getWorkbenchOverview(selectedDate.value)
    if (res.code === 1 && res.data) {
      overview.value = res.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || '工作台数据加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ---------- 顶部 ---------- */
.wb-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  background: white;
  border-radius: 12px;
  padding: 14px 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.wb-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.wb-icon {
  font-size: 20px;
}

.wb-name {
  font-size: 18px;
  font-weight: 600;
  color: #1976d2;
}

.wb-sub {
  font-size: 12px;
  color: #999;
  padding-left: 10px;
  border-left: 1px solid #eee;
}

.wb-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.wb-date {
  width: 150px;
}

/* ---------- 区块标题 ---------- */
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  padding-left: 4px;
  margin-top: 4px;
}

/* ---------- 待办 ---------- */
.todo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.todo-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2px;
  background: white;
  border-radius: 12px;
  padding: 16px 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #e0e0e0;
  transition: transform 0.2s, box-shadow 0.2s;
}

.todo-card.clickable {
  cursor: pointer;
}

.todo-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.todo-card.orange { border-left-color: #fb8c00; }
.todo-card.blue { border-left-color: #1e88e5; }
.todo-card.red { border-left-color: #e53935; }
.todo-card.purple { border-left-color: #8e24aa; }

.todo-icon {
  font-size: 18px;
}

.todo-value {
  font-size: 28px;
  font-weight: 700;
  color: #222;
  line-height: 1.15;
  margin-top: 4px;
}

.todo-label {
  font-size: 13px;
  color: #555;
  font-weight: 500;
}

.todo-hint {
  font-size: 12px;
  color: #aaa;
  margin-top: 2px;
}

/* ---------- 当日概况 ---------- */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 16px;
}

.kpi-card {
  background: white;
  border-radius: 12px;
  padding: 16px 18px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border-top: 3px solid #e0e0e0;
}

.kpi-card.blue { border-top-color: #1e88e5; }
.kpi-card.cyan { border-top-color: #00acc1; }
.kpi-card.green { border-top-color: #43a047; }
.kpi-card.orange { border-top-color: #fb8c00; }
.kpi-card.purple { border-top-color: #8e24aa; }

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-label {
  font-size: 13px;
  color: #888;
}

.kpi-icon {
  font-size: 16px;
}

.kpi-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #222;
}

/* ---------- 卡片 ---------- */
.wb-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.card-tip {
  font-size: 12px;
  color: #999;
}

/* ---------- 预约时间轴 ---------- */
.timeline {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 4px 2px 8px;
  overflow-x: auto;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 15px;
  height: 2px;
  background: #e8eef5;
}

.slot {
  position: relative;
  z-index: 1;
  min-width: 108px;
  padding: 0 10px;
}

.slot-dot {
  display: block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #c5d9ee;
  border: 2px solid #fff;
  box-shadow: 0 0 0 2px #e3f2fd;
  margin: 8px 0 10px 2px;
}

.slot-dot.busy {
  background: #1976d2;
}

.slot-time {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.slot-count {
  font-size: 12px;
  color: #1976d2;
  margin-top: 2px;
}

/* ---------- 快捷入口 ---------- */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f7f9fc;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-item:hover {
  background: #e3f2fd;
  transform: translateY(-1px);
}

.quick-icon {
  font-size: 22px;
}

.quick-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quick-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.quick-desc {
  font-size: 12px;
  color: #999;
}

@media (max-width: 1200px) {
  .todo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
