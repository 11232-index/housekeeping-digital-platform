<template>
  <div class="summary-page">
    <!-- 顶部标题栏 -->
    <div class="summary-header">
      <div class="header-left">
        <span class="ai-icon">🤖</span>
        <span class="header-title">智能总结</span>
        <el-divider direction="vertical" />
        <span class="header-sub" v-if="employee">员工：{{ employee.name }}</span>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="goBack">返回员工管理</el-button>
      </div>
    </div>

    <div class="summary-body" v-if="employee">
      <!-- 左侧：原始数据 -->
      <div class="left-panel">
        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-icon">📋</span>
              <span>基本信息</span>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ employee.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">账号</span>
              <span class="info-value">{{ employee.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别</span>
              <span class="info-value">{{ employee.sex === '1' ? '男' : '女' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">年龄</span>
              <span class="info-value">{{ employee.age || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ employee.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">身份证号</span>
              <span class="info-value">{{ employee.idNumber }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">账号状态</span>
              <span class="info-value">
                <el-tag :type="employee.status === 1 ? 'success' : 'danger'" size="small">
                  {{ employee.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ employee.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后操作</span>
              <span class="info-value">{{ employee.updateTime }}</span>
            </div>
          </div>
        </el-card>

        <!-- 详细信息卡片 -->
        <el-card shadow="never" class="info-card" v-if="detail">
          <template #header>
            <div class="card-header">
              <span class="card-icon">📝</span>
              <span>详细信息</span>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item" v-if="detail.position">
              <span class="info-label">岗位</span>
              <span class="info-value">{{ detail.position }}</span>
            </div>
            <div class="info-item" v-if="detail.salary">
              <span class="info-label">工资</span>
              <span class="info-value highlight-value">{{ detail.salary }}</span>
            </div>
            <div class="info-item" v-if="detail.workStatus">
              <span class="info-label">工作状态</span>
              <span class="info-value">
                <el-tag
                  :type="workStatusType(detail.workStatus)"
                  size="small"
                >{{ detail.workStatus }}</el-tag>
              </span>
            </div>
            <div class="info-item info-full" v-if="educationText">
              <span class="info-label">学历</span>
              <span class="info-value">{{ educationText }}</span>
            </div>
            <div class="info-item info-full" v-if="experienceText">
              <span class="info-label">过往经历</span>
              <span class="info-value">{{ experienceText }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：AI 智能总结 -->
      <div class="right-panel">
        <el-card shadow="never" class="ai-card">
          <template #header>
            <div class="card-header">
              <span class="card-icon">✨</span>
              <span>AI 智能分析总结</span>
              <el-button
                size="small"
                type="primary"
                @click="generateSummary"
                :loading="generating"
                class="generate-btn"
              >
                {{ summary ? '重新生成' : '生成总结' }}
              </el-button>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-if="!summary && !generating" class="empty-state">
            <div class="empty-icon">💡</div>
            <p class="empty-text">点击"生成总结"，AI 将自动分析员工数据</p>
            <p class="empty-hint">包括基本信息、学历背景、工作经历等综合评估</p>
          </div>

          <!-- 生成中 -->
          <div v-if="generating" class="generating-state">
            <div class="typing-dots">
              <span></span><span></span><span></span>
            </div>
            <p>AI 正在分析员工数据...</p>
          </div>

          <!-- 总结内容 -->
          <div v-if="summary && !generating" class="summary-content">
            <div class="summary-section" v-for="(section, i) in summary.sections" :key="i">
              <div class="section-title">
                <span class="section-icon">{{ section.icon }}</span>
                {{ section.title }}
              </div>
              <div class="section-body">{{ section.content }}</div>
            </div>
            <el-divider />
            <div class="summary-footer">
              <span class="footer-label">综合评估</span>
              <el-tag :type="summary.overallRating.type" size="large" effect="dark">
                {{ summary.overallRating.label }}
              </el-tag>
              <p class="footer-text">{{ summary.overallRating.comment }}</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 未找到员工 -->
    <div v-else-if="!loading" class="not-found">
      <el-empty description="未找到该员工信息" />
      <el-button type="primary" @click="goBack">返回员工管理</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getEmployeeById,
  getEmployeeDetail,
  type Employee,
  type EmployeeDetail
} from '@/api/employee'
import { generateSummary as fetchSummary } from '@/api/summary'
import type { SummaryData } from '@/api/summary'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const generating = ref(false)
const employee = ref<Employee | null>(null)
const detail = ref<EmployeeDetail | null>(null)

const summary = ref<SummaryData | null>(null)

// 学历文本
const educationText = computed(() => {
  if (!detail.value?.education) return ''
  try {
    const list = JSON.parse(detail.value.education)
    if (!list.length) return ''
    return list.map((e: any) => `${e.school}（${e.degree}）`).join('、')
  } catch {
    return ''
  }
})

// 经历文本
const experienceText = computed(() => {
  if (!detail.value?.pastExperience) return ''
  try {
    const list = JSON.parse(detail.value.pastExperience)
    if (!list.length) return ''
    return list.map((e: any) => {
      const from = e.dateRange?.[0] || '?'
      const to = e.dateRange?.[1] || '?'
      return `${e.description}（${from} 至 ${to}）`
    }).join('；')
  } catch {
    return ''
  }
})

// 工作状态标签颜色
const workStatusType = (status: string) => {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    '在职': 'success',
    '试用期': 'warning',
    '休假': 'info',
    '离职': 'danger'
  }
  return map[status] || 'info'
}

// 生成总结（调用真实 AI 接口）
const generateSummary = async () => {
  if (!employee.value) return

  generating.value = true
  summary.value = null

  try {
    const res = await fetchSummary(employee.value.id)
    if (res.data.code === 1 && res.data.data) {
      summary.value = res.data.data as SummaryData
    } else {
      ElMessage.error((res.data as any)?.msg || 'AI 总结生成失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '请求失败，请确保 Python 后端已启动')
  } finally {
    generating.value = false
  }
}

const goBack = () => {
  router.push('/employee')
}

const loadData = async () => {
  const id = Number(route.query.id)
  if (!id) {
    loading.value = false
    return
  }

  try {
    const [empRes, detailRes]: any[] = await Promise.all([
      getEmployeeById(id),
      getEmployeeDetail(id).catch(() => ({ code: 0 }))
    ])

    if (empRes.code === 1 && empRes.data) {
      employee.value = empRes.data
    }
    if (detailRes.code === 1 && detailRes.data) {
      detail.value = detailRes.data
    }
  } catch {
    ElMessage.error('加载员工数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.summary-page {
  min-height: 100%;
  background: transparent;
}

/* 顶部栏 */
.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-icon {
  font-size: 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-sub {
  font-size: 14px;
  color: #606266;
}

/* 主体 */
.summary-body {
  display: flex;
  gap: 16px;
  height: calc(100vh - 180px);
}

/* 左侧面板 */
.left-panel {
  width: 420px;
  min-width: 420px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.generate-btn {
  margin-left: auto;
}

.card-icon {
  font-size: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-full {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.highlight-value {
  color: #e6a23c;
  font-weight: 600;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  overflow-y: auto;
}

.ai-card {
  border-radius: 8px;
  min-height: 100%;
  border: 2px solid #e8f0fe;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #303133;
  margin: 0 0 8px;
}

.empty-hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* 生成中 */
.generating-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.generating-state p {
  font-size: 14px;
  color: #606266;
}

.typing-dots {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1e3a8a;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
  30% { opacity: 1; transform: scale(1.2); }
}

/* 总结内容 */
.summary-content {
  padding: 4px 0;
}

.summary-section {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #1e3a8a;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-icon {
  font-size: 16px;
}

.section-body {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.8;
}

.summary-footer {
  text-align: center;
  padding: 12px 0;
}

.footer-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.footer-text {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.8;
  margin: 12px 0 0;
  padding: 0 20px;
}

/* 未找到 */
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 4px;
}

/* Element Plus 深色按钮覆盖 */
:deep(.el-button--primary) {
  background-color: #1e3a8a;
  border-color: #1e3a8a;
}
</style>
