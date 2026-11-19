<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部：标题 + 统计区间 -->
    <div class="dashboard-toolbar">
      <div class="toolbar-left">
        <span class="title-icon">📊</span>
        <span class="dashboard-title">工作台</span>
        <span class="dashboard-sub">数据总览</span>
      </div>
      <div class="toolbar-right">
        <el-button-group class="range-group">
          <el-button
            v-for="item in quickRanges"
            :key="item.key"
            size="small"
            :type="activeQuick === item.key ? 'primary' : 'default'"
            @click="selectQuickRange(item.key)"
          >{{ item.label }}</el-button>
        </el-button-group>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          class="range-picker"
          @change="handleCustomRange"
        />
        <el-button size="small" :loading="loading" @click="loadAll">刷新</el-button>
      </div>
    </div>

    <!-- KPI 卡片 -->
    <div class="kpi-grid">
      <div class="kpi-card blue">
        <div class="kpi-top">
          <span class="kpi-label">总营业额</span>
          <span class="kpi-icon">💰</span>
        </div>
        <div class="kpi-value">¥{{ formatMoney(turnover.totalTurnover) }}</div>
        <div class="kpi-sub">已完成且已支付</div>
      </div>
      <div class="kpi-card green">
        <div class="kpi-top">
          <span class="kpi-label">有效订单</span>
          <span class="kpi-icon">✅</span>
        </div>
        <div class="kpi-value">{{ turnover.validOrderCount }} <span class="kpi-unit">单</span></div>
        <div class="kpi-sub">区间订单共 {{ order.totalOrder }} 单</div>
      </div>
      <div class="kpi-card orange">
        <div class="kpi-top">
          <span class="kpi-label">平均客单价</span>
          <span class="kpi-icon">🧾</span>
        </div>
        <div class="kpi-value">¥{{ formatMoney(turnover.avgOrderAmount) }}</div>
        <div class="kpi-sub">营业额 ÷ 有效订单</div>
      </div>
      <div class="kpi-card purple">
        <div class="kpi-top">
          <span class="kpi-label">用户总数</span>
          <span class="kpi-icon">👥</span>
        </div>
        <div class="kpi-value">{{ user.totalUser }} <span class="kpi-unit">人</span></div>
        <div class="kpi-sub">区间新增 {{ user.newUser }} 人</div>
      </div>
    </div>

    <!-- 营业额趋势 + 订单状态分布 -->
    <div class="chart-grid">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">💰 营业额趋势</span>
            <span class="card-tip">单位：元</span>
          </div>
        </template>
        <div class="line-wrap">
          <svg :viewBox="`0 0 ${LINE_W} ${LINE_H}`" class="line-svg">
            <defs>
              <linearGradient id="turnoverArea" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#1976d2" stop-opacity="0.26" />
                <stop offset="100%" stop-color="#1976d2" stop-opacity="0.02" />
              </linearGradient>
            </defs>
            <!-- 横向网格线与 Y 轴刻度 -->
            <g v-for="tick in turnoverChart.yTicks" :key="'y' + tick.y">
              <line :x1="LINE_PAD_L" :x2="LINE_W - LINE_PAD_R" :y1="tick.y" :y2="tick.y" stroke="#eef1f5" stroke-width="1" />
              <text :x="LINE_PAD_L - 8" :y="tick.y + 4" text-anchor="end" class="axis-text">{{ tick.label }}</text>
            </g>
            <!-- 面积与折线 -->
            <path :d="turnoverChart.areaPath" fill="url(#turnoverArea)" />
            <polyline
              :points="turnoverChart.polyline"
              fill="none"
              stroke="#1976d2"
              stroke-width="2"
              stroke-linejoin="round"
              stroke-linecap="round"
            />
            <!-- 数据点 -->
            <g v-for="(point, index) in turnoverChart.points" :key="'p' + index">
              <circle :cx="point.x" :cy="point.y" r="2.5" fill="#1976d2">
                <title>{{ turnover.dateList[index] }}：¥{{ formatMoney(point.value) }}</title>
              </circle>
            </g>
            <!-- X 轴日期 -->
            <text
              v-for="label in turnoverChart.xLabels"
              :key="'x' + label.x"
              :x="label.x"
              :y="LINE_H - 8"
              text-anchor="middle"
              class="axis-text"
            >{{ label.label }}</text>
          </svg>
          <div v-if="turnover.totalTurnover === 0" class="empty-tip">当前区间暂无营业额数据</div>
        </div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">🧭 订单状态分布</span>
            <span class="card-tip">共 {{ order.totalOrder }} 单</span>
          </div>
        </template>
        <div class="donut-wrap">
          <div class="donut" :style="donutStyle">
            <div class="donut-hole">
              <span class="donut-total">{{ order.totalOrder }}</span>
              <span class="donut-label">总订单</span>
            </div>
          </div>
          <div class="donut-legend">
            <div v-for="item in order.statusList" :key="item.status" class="legend-item">
              <span class="legend-dot" :style="{ background: statusColor(item.status) }"></span>
              <span class="legend-name">{{ item.name }}</span>
              <span class="legend-count">{{ item.count }}</span>
            </div>
            <div v-if="order.statusList.length === 0" class="donut-empty">暂无订单数据</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 订单趋势 + 用户新增趋势 -->
    <div class="chart-grid equal">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">📦 订单趋势</span>
            <span class="card-tip">有效 {{ order.validOrder }} · 取消 {{ order.cancelledOrder }}</span>
          </div>
        </template>
        <div class="bar-chart" :class="{ single: orderBars.length === 1 }">
          <div
            v-for="(bar, index) in orderBars"
            :key="'o' + index"
            class="bar-col"
            :title="`${bar.date}：${bar.value} 单`"
          >
            <div class="bar-track">
              <div class="bar-fill blue-fill" :style="{ height: bar.height + '%' }"></div>
            </div>
            <div class="bar-label">{{ showBarLabel(index, orderBars.length) ? bar.label : '' }}</div>
          </div>
        </div>
        <div v-if="order.totalOrder === 0" class="empty-tip">当前区间暂无订单数据</div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">👥 用户新增趋势</span>
            <span class="card-tip">区间新增 {{ user.newUser }} 人</span>
          </div>
        </template>
        <div class="bar-chart" :class="{ single: userBars.length === 1 }">
          <div
            v-for="(bar, index) in userBars"
            :key="'u' + index"
            class="bar-col"
            :title="`${bar.date}：新增 ${bar.value} 人`"
          >
            <div class="bar-track">
              <div class="bar-fill green-fill" :style="{ height: bar.height + '%' }"></div>
            </div>
            <div class="bar-label">{{ showBarLabel(index, userBars.length) ? bar.label : '' }}</div>
          </div>
        </div>
        <div v-if="user.newUser === 0" class="empty-tip">当前区间暂无新增用户</div>
      </el-card>
    </div>

    <!-- 销量 Top 10 -->
    <el-card shadow="never" class="chart-card rank-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">🏆 销量排名 Top 10</span>
          <span class="card-tip">仅统计服务项目 / 服务套餐（已完成订单）</span>
        </div>
      </template>
      <div v-if="salesRank.nameList.length > 0" class="rank-list">
        <div v-for="(name, index) in salesRank.nameList" :key="index" class="rank-item">
          <span class="rank-no" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
          <span class="rank-name" :title="name">{{ name }}</span>
          <div class="rank-bar">
            <div class="rank-fill" :style="{ width: rankWidth(index) + '%' }"></div>
          </div>
          <span class="rank-number">{{ salesRank.numberList[index] }} 单</span>
          <span class="rank-amount">¥{{ formatMoney(salesRank.amountList[index]) }}</span>
        </div>
      </div>
      <el-empty v-else description="当前区间暂无销量数据" :image-size="80" />
    </el-card>

    <div class="dashboard-footnote">
      营业额 / 有效订单：已完成且已支付订单，按付款日期统计；订单数与销量按下单日期统计。
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderStatistics,
  getSalesRank
} from '@/api/statistics'
import type {
  OrderStatistics,
  SalesRank,
  TurnoverStatistics,
  UserStatistics
} from '@/api/statistics'

// 折线图画布尺寸（viewBox 坐标系）
const LINE_W = 720
const LINE_H = 220
const LINE_PAD_L = 56
const LINE_PAD_R = 16
const LINE_PAD_T = 16
const LINE_PAD_B = 28

interface QuickRange {
  key: string
  label: string
}

interface ChartPoint {
  x: number
  y: number
  value: number
}

interface BarItem {
  date: string
  value: number
  height: number
  label: string
}

const quickRanges: QuickRange[] = [
  { key: 'today', label: '今天' },
  { key: '7', label: '近7天' },
  { key: '30', label: '近30天' },
  { key: '90', label: '近90天' },
  { key: 'month', label: '本月' }
]

const loading = ref(false)
const activeQuick = ref('30')
const dateRange = ref<[string, string] | null>(null)

const turnover = ref<TurnoverStatistics>({
  totalTurnover: 0,
  validOrderCount: 0,
  avgOrderAmount: 0,
  dateList: [],
  turnoverList: []
})

const user = ref<UserStatistics>({
  totalUser: 0,
  newUser: 0,
  dateList: [],
  newUserList: []
})

const order = ref<OrderStatistics>({
  totalOrder: 0,
  validOrder: 0,
  cancelledOrder: 0,
  dateList: [],
  orderList: [],
  statusList: []
})

const salesRank = ref<SalesRank>({
  nameList: [],
  numberList: [],
  amountList: []
})

// ---------------- 日期处理 ----------------

const pad2 = (value: number) => String(value).padStart(2, '0')

const formatDate = (date: Date) =>
  `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`

/** 把 yyyy-MM-dd 缩成 MM-dd，供坐标轴使用 */
const shortDate = (date: string) => (date ? date.slice(5) : '')

/** 设置快捷区间（不触发请求） */
const setQuickRange = (key: string) => {
  const today = new Date()
  let begin: Date
  if (key === 'today') {
    // 今天：起止为同一天
    begin = today
  } else if (key === 'month') {
    begin = new Date(today.getFullYear(), today.getMonth(), 1)
  } else {
    const days = Number(key) || 30
    begin = new Date(today.getFullYear(), today.getMonth(), today.getDate() - (days - 1))
  }
  dateRange.value = [formatDate(begin), formatDate(today)]
  activeQuick.value = key
}

const selectQuickRange = (key: string) => {
  setQuickRange(key)
  loadAll()
}

/** 自定义日期范围变化 */
const handleCustomRange = (value: [string, string] | null) => {
  if (!value || value.length !== 2) {
    // 清空时回到近 30 天，避免请求没有区间
    setQuickRange('30')
  } else {
    activeQuick.value = 'custom'
  }
  loadAll()
}

// ---------------- 数据加载 ----------------

const loadAll = async () => {
  if (!dateRange.value) return
  const params = {
    beginTime: dateRange.value[0],
    endTime: dateRange.value[1]
  }
  loading.value = true
  try {
    const results: any[] = await Promise.all([
      getTurnoverStatistics(params),
      getUserStatistics(params),
      getOrderStatistics(params),
      getSalesRank(params)
    ])
    const [turnoverRes, userRes, orderRes, salesRes] = results
    if (turnoverRes.code === 1 && turnoverRes.data) {
      turnover.value = turnoverRes.data
    }
    if (userRes.code === 1 && userRes.data) {
      user.value = userRes.data
    }
    if (orderRes.code === 1 && orderRes.data) {
      order.value = orderRes.data
    }
    if (salesRes.code === 1 && salesRes.data) {
      salesRank.value = salesRes.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || '统计数据加载失败')
  } finally {
    loading.value = false
  }
}

// ---------------- 格式化 ----------------

const formatMoney = (value: number | string | null | undefined) => {
  const num = Number(value ?? 0) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** Y 轴刻度：过万显示「万」 */
const formatAxisMoney = (value: number) => {
  if (value >= 10000) return `${(value / 10000).toFixed(1)}万`
  return Number(value.toFixed(2)).toString()
}

// ---------------- 折线图 ----------------

const turnoverChart = computed(() => {
  const values = (turnover.value.turnoverList || []).map((item) => Number(item) || 0)
  const dates = turnover.value.dateList || []
  const n = values.length
  const innerW = LINE_W - LINE_PAD_L - LINE_PAD_R
  const innerH = LINE_H - LINE_PAD_T - LINE_PAD_B
  const max = Math.max(...values, 1)
  const baseline = LINE_PAD_T + innerH

  const points: ChartPoint[] = values.map((value, index) => ({
    x: LINE_PAD_L + (n <= 1 ? innerW / 2 : (innerW * index) / (n - 1)),
    y: LINE_PAD_T + innerH - (value / max) * innerH,
    value
  }))

  const polyline = points.map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(' ')
  const areaPath = points.length > 0
    ? `M ${points[0].x.toFixed(1)} ${baseline} L ` +
      points.map((point) => `${point.x.toFixed(1)} ${point.y.toFixed(1)}`).join(' L ') +
      ` L ${points[points.length - 1].x.toFixed(1)} ${baseline} Z`
    : ''

  const yTicks = [0, 0.5, 1].map((ratio) => ({
    y: baseline - ratio * innerH,
    label: formatAxisMoney(max * ratio)
  }))

  // 日期太多时抽稀，避免坐标轴文字重叠。
  // 不再强制补最后一个日期：它可能紧挨着上一个刻度（如 10-28、10-29），造成文字重叠。
  const step = Math.max(1, Math.ceil(n / 6))
  const xLabels = dates
    .map((date, index) => ({ date, index }))
    .filter(({ index }) => index % step === 0)
    .map(({ date, index }) => ({ x: points[index]?.x ?? 0, label: shortDate(date) }))

  return { points, polyline, areaPath, yTicks, xLabels }
})

// ---------------- 柱状图 ----------------

const buildBars = (dateList: string[], values: number[]): BarItem[] => {
  const nums = (values || []).map((item) => Number(item) || 0)
  const max = Math.max(...nums, 1)
  return nums.map((value, index) => ({
    date: dateList[index] || '',
    value,
    // 有数据时至少给 3% 高度，保证小值也看得见
    height: value > 0 ? Math.max((value / max) * 100, 3) : 0,
    label: shortDate(dateList[index] || '')
  }))
}

const orderBars = computed(() => buildBars(order.value.dateList, order.value.orderList))
const userBars = computed(() => buildBars(user.value.dateList, user.value.newUserList))

/**
 * 柱子较多时抽稀日期标签。
 * 最多显示 7 个；不强制补最后一个日期，避免它与上一个刻度贴在一起（如 10-28、10-29）。
 */
const showBarLabel = (index: number, total: number) => {
  if (total <= 7) return true
  const step = Math.ceil(total / 7)
  return index % step === 0
}

// ---------------- 环形图 / 排名 ----------------

const STATUS_COLORS: Record<number, string> = {
  1: '#90a4ae',
  2: '#42a5f5',
  3: '#5c6bc0',
  4: '#26a69a',
  5: '#66bb6a',
  6: '#ffa726',
  7: '#ef5350'
}

const statusColor = (status: number) => STATUS_COLORS[status] || '#bdbdbd'

const donutStyle = computed(() => {
  const list = order.value.statusList.filter((item) => Number(item.count) > 0)
  const total = list.reduce((sum, item) => sum + Number(item.count), 0)
  if (total === 0) {
    return { background: '#f0f2f5' }
  }
  let acc = 0
  const segments = list.map((item) => {
    const start = (acc / total) * 100
    acc += Number(item.count)
    const end = (acc / total) * 100
    return `${statusColor(item.status)} ${start}% ${end}%`
  })
  return { background: `conic-gradient(${segments.join(', ')})` }
})

const salesMax = computed(() => Math.max(...salesRank.value.numberList.map((item) => Number(item) || 0), 1))

const rankWidth = (index: number) => {
  const value = Number(salesRank.value.numberList[index]) || 0
  return (value / salesMax.value) * 100
}

onMounted(() => {
  setQuickRange('30')
  loadAll()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ---------- 顶部工具栏 ---------- */
.dashboard-toolbar {
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

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 20px;
}

.dashboard-title {
  font-size: 18px;
  font-weight: 600;
  color: #1976d2;
}

.dashboard-sub {
  font-size: 12px;
  color: #999;
  padding-left: 10px;
  border-left: 1px solid #eee;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.range-picker {
  width: 240px;
}

/* ---------- KPI 卡片 ---------- */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.kpi-card {
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.kpi-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.kpi-card.blue::before { background: linear-gradient(180deg, #42a5f5, #1976d2); }
.kpi-card.green::before { background: linear-gradient(180deg, #66bb6a, #2e7d32); }
.kpi-card.orange::before { background: linear-gradient(180deg, #ffb74d, #f57c00); }
.kpi-card.purple::before { background: linear-gradient(180deg, #ab47bc, #7b1fa2); }

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
  margin-top: 10px;
  font-size: 26px;
  font-weight: 700;
  color: #222;
  line-height: 1.2;
  word-break: break-all;
}

.kpi-unit {
  font-size: 13px;
  font-weight: 400;
  color: #999;
}

.kpi-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #aaa;
}

/* ---------- 图表卡片 ---------- */
.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-grid.equal {
  grid-template-columns: 1fr 1fr;
}

.chart-card {
  position: relative;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

/* 折线图 */
.line-wrap {
  position: relative;
}

.line-svg {
  width: 100%;
  height: auto;
  display: block;
}

.axis-text {
  font-size: 11px;
  fill: #999;
}

.empty-tip {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  color: #bbb;
  pointer-events: none;
  white-space: nowrap;
}

/* 柱状图 */
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 170px;
  padding: 0 2px;
}

.bar-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

/* 只有一天数据时，避免单根柱子铺满整个图表 */
.bar-chart.single {
  justify-content: center;
}

.bar-chart.single .bar-col {
  flex: 0 0 48px;
}

.bar-track {
  width: 100%;
  flex: 1;
  display: flex;
  align-items: flex-end;
  background: #f7f9fc;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: 3px 3px 0 0;
  transition: height 0.4s ease;
}

.blue-fill {
  background: linear-gradient(180deg, #64b5f6, #1976d2);
}

.green-fill {
  background: linear-gradient(180deg, #81c784, #388e3c);
}

.bar-label {
  margin-top: 6px;
  height: 14px;
  font-size: 10px;
  color: #999;
  white-space: nowrap;
}

/* 环形图 */
.donut-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  min-height: 220px;
  flex-wrap: wrap;
}

.donut {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
}

.donut-hole {
  position: absolute;
  inset: 26px;
  background: white;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-total {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.donut-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
}

.legend-count {
  font-weight: 600;
  color: #333;
}

/* 订单状态分布的暂无数据提示：放在图例区，正常流式布局，不遮挡环形图 */
.donut-empty {
  font-size: 12px;
  color: #bbb;
  text-align: center;
  padding: 6px 0;
}

/* 销量排名 */
.rank-card {
  margin-top: 0;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-no {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #f0f2f5;
  color: #888;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rank-no.top3 {
  background: linear-gradient(135deg, #ffd54f, #ffb300);
  color: #7a4f00;
}

.rank-name {
  width: 180px;
  font-size: 13px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}

.rank-bar {
  flex: 1;
  height: 12px;
  background: #f7f9fc;
  border-radius: 6px;
  overflow: hidden;
}

.rank-fill {
  height: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, #64b5f6, #1976d2);
  transition: width 0.4s ease;
}

.rank-number {
  width: 56px;
  text-align: right;
  font-size: 12px;
  color: #666;
}

.rank-amount {
  width: 90px;
  text-align: right;
  font-size: 12px;
  color: #f57c00;
  font-weight: 600;
}

.dashboard-footnote {
  font-size: 12px;
  color: #aaa;
  text-align: right;
  padding: 0 4px 4px;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-grid,
  .chart-grid.equal {
    grid-template-columns: 1fr;
  }
}
</style>
