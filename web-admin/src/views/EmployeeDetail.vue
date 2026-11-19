<template>
  <div class="employee-detail-page">
    <!-- 左侧员工列表 -->
    <div class="left-panel">
      <div class="panel-title">员工列表</div>
      <div class="panel-search">
        <el-input
          v-model="employeeSearch.keyword"
          placeholder="搜索员工姓名"
          clearable
          @keyup.enter="searchEmployees"
        />
      </div>
      <div class="employee-items">
        <div
          v-for="emp in employeeList"
          :key="emp.id"
          :class="['employee-item', { active: selectedEmployeeId === emp.id }]"
          @click="selectEmployee(emp)"
        >
          <div class="item-name">{{ emp.name }}</div>
          <div class="item-username">{{ emp.username }}</div>
        </div>
        <el-empty v-if="employeeList.length === 0 && !employeeLoading" description="暂无员工" />
      </div>
      <div class="panel-pagination">
        <el-pagination
          v-model:current-page="employeeSearch.page"
          :page-size="employeeSearch.pageSize"
          :total="employeeTotal"
          layout="prev, pager, next"
          size="small"
          @current-change="loadEmployeeList"
        />
      </div>
    </div>

    <!-- 右侧详情 -->
    <div class="right-panel">
      <template v-if="selectedEmployeeId">
        <!-- 顶部操作栏 -->
        <div class="form-header">
          <span class="form-title">
            员工详细信息 - {{ selectedEmployeeName }}
          </span>
          <div class="form-actions">
            <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
            <el-button type="danger" @click="handleDelete" :loading="deleting" v-if="detailExists">删除</el-button>
          </div>
        </div>

        <!-- 表单区域 -->
        <div class="form-body" v-loading="detailLoading">
          <el-form label-width="100px" v-if="!detailLoading">
            <!-- 学历 -->
            <div class="form-section">
              <div class="section-header">
                <span class="section-label">学历</span>
                <el-button size="small" type="primary" link @click="addEducation">+ 添加学历</el-button>
              </div>
              <div v-for="(edu, index) in form.educationList" :key="index" class="entry-row">
                <el-select v-model="edu.degree" placeholder="学历" style="width: 120px">
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                  <el-option label="大专" value="大专" />
                  <el-option label="高中" value="高中" />
                  <el-option label="其他" value="其他" />
                </el-select>
                <el-input v-model="edu.school" placeholder="学校名称" style="width: 200px" />
                <span class="entry-preview" v-if="edu.school && edu.degree">
                  {{ edu.school }}（{{ edu.degree }}）
                </span>
                <el-button size="small" type="danger" link @click="removeEducation(index)">删除</el-button>
              </div>
            </div>

            <!-- 过去经历 -->
            <div class="form-section">
              <div class="section-header">
                <span class="section-label">过去经历</span>
                <el-button size="small" type="primary" link @click="addExperience">+ 添加经历</el-button>
              </div>
              <div v-for="(exp, index) in form.experienceList" :key="index" class="entry-row entry-row-vertical">
                <div class="exp-row">
                  <el-date-picker
                    v-model="exp.dateRange[0]"
                    type="date"
                    placeholder="开始日期"
                    format="YYYY年M月D日"
                    value-format="YYYY-MM-DD"
                    style="width: 180px"
                  />
                  <span style="color: #909399; margin: 0 4px">至</span>
                  <el-date-picker
                    v-model="exp.dateRange[1]"
                    type="date"
                    placeholder="结束日期"
                    format="YYYY年M月D日"
                    value-format="YYYY-MM-DD"
                    style="width: 180px"
                  />
                  <el-input
                    v-model="exp.description"
                    placeholder="入职情况描述"
                    style="width: 300px"
                  />
                  <el-button size="small" type="danger" link @click="removeExperience(index)">删除</el-button>
                </div>
                <span class="entry-preview" v-if="exp.dateRange && exp.dateRange.length === 2 && exp.description">
                  {{ exp.description }}（{{ formatDate(exp.dateRange[0]) }}至{{ formatDate(exp.dateRange[1]) }}）
                </span>
              </div>
            </div>

            <!-- 岗位 -->
            <el-form-item label="岗位">
              <el-input v-model="form.position" placeholder="请输入岗位" style="width: 300px" />
            </el-form-item>

            <!-- 工资 -->
            <el-form-item label="工资">
              <el-input v-model="form.salary" placeholder="请输入工资" style="width: 300px" />
            </el-form-item>

            <!-- 工作状态 -->
            <el-form-item label="工作状态">
              <el-select v-model="form.workStatus" placeholder="请选择工作状态" style="width: 200px">
                <el-option label="在职" value="在职" />
                <el-option label="离职" value="离职" />
                <el-option label="休假" value="休假" />
                <el-option label="试用期" value="试用期" />
              </el-select>
            </el-form-item>

            <!-- 排班：每个星期几各自配一条或多条时段 -->
            <el-form-item label="排班">
              <div class="schedule-editor">
                <div class="schedule-days">
                  <span class="schedule-tip">勾选需要排班的星期：</span>
                  <el-checkbox-group v-model="form.scheduleDays">
                    <el-checkbox-button v-for="d in WEEK_DAYS" :key="d.value" :value="d.value">
                      {{ d.label }}
                    </el-checkbox-button>
                  </el-checkbox-group>
                </div>

                <div v-for="day in selectedDays" :key="day" class="schedule-day-block">
                  <span class="schedule-day-label">{{ weekLabel(day) }}</span>
                  <div class="schedule-slots">
                    <div v-for="(slot, i) in form.scheduleMap[day]" :key="i" class="entry-row">
                      <el-time-picker
                        v-model="slot.start"
                        placeholder="开始"
                        format="HH:mm"
                        value-format="HH:mm"
                        style="width: 130px"
                      />
                      <span style="color: #909399; margin: 0 4px">~</span>
                      <el-time-picker
                        v-model="slot.end"
                        placeholder="结束"
                        format="HH:mm"
                        value-format="HH:mm"
                        style="width: 130px"
                      />
                      <el-button
                        v-if="form.scheduleMap[day].length > 1"
                        size="small"
                        type="danger"
                        link
                        @click="removeScheduleSlot(day, i)"
                      >
                        删除
                      </el-button>
                    </div>
                    <el-button size="small" type="primary" plain @click="addScheduleSlot(day)">
                      + 添加时段
                    </el-button>
                  </div>
                </div>

                <div v-if="schedulePreview" class="entry-preview">预览：{{ schedulePreview }}</div>
              </div>
            </el-form-item>

            <!-- 合同文件 -->
            <el-form-item label="合同文件">
              <div class="file-upload">
                <el-upload
                  :auto-upload="false"
                  :limit="1"
                  :on-change="(file: any) => handleFileChange(file, 'contract')"
                  :on-remove="() => handleFileRemove('contract')"
                  :file-list="contractFileList"
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                >
                  <el-button size="small" type="primary">上传合同</el-button>
                </el-upload>
                <div v-if="form.contract || pendingFiles['contract']" class="file-actions">
                  <span v-if="pendingFiles['contract']" class="file-tip">未保存，请点击上方"保存"按钮</span>
                  <el-button v-if="form.contract" size="small" type="success" link @click="handleDownloadFile(form.contract)">
                    下载并查看
                  </el-button>
                </div>
              </div>
            </el-form-item>

            <!-- 健康证 -->
            <el-form-item label="健康证">
              <div class="file-upload">
                <el-upload
                  :auto-upload="false"
                  :limit="1"
                  :on-change="(file: any) => handleFileChange(file, 'healthCertificate')"
                  :on-remove="() => handleFileRemove('healthCertificate')"
                  :file-list="healthFileList"
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                >
                  <el-button size="small" type="primary">上传健康证</el-button>
                </el-upload>
                <div v-if="form.healthCertificate || pendingFiles['healthCertificate']" class="file-actions">
                  <span v-if="pendingFiles['healthCertificate']" class="file-tip">未保存，请点击上方"保存"按钮</span>
                  <el-button v-if="form.healthCertificate" size="small" type="success" link @click="handleDownloadFile(form.healthCertificate)">
                    下载并查看
                  </el-button>
                </div>
              </div>
            </el-form-item>

            <!-- 最后操作时间 -->
            <el-form-item label="最后操作时间">
              <span class="update-time">{{ form.updateTime || '-' }}</span>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 未选择员工 -->
      <div v-else class="no-selection">
        <el-empty description="请在左侧选择员工" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import {
  getEmployeePage,
  getEmployeeDetail,
  addEmployeeDetail,
  updateEmployeeDetail,
  deleteEmployeeDetail,
  uploadFile,
  fetchFileAsBlob,
  type Employee,
  type EmployeeDetail
} from '@/api/employee'

const route = useRoute()
const router = useRouter()

// ========== 左侧员工列表 ==========
const employeeSearch = reactive({ keyword: '', page: 1, pageSize: 10 })
const employeeList = ref<Employee[]>([])
const employeeTotal = ref(0)
const employeeLoading = ref(false)
const selectedEmployeeId = ref<number | null>(null)
const selectedEmployeeName = ref('')

const loadEmployeeList = async () => {
  employeeLoading.value = true
  try {
    const res: any = await getEmployeePage({
      page: employeeSearch.page,
      pageSize: employeeSearch.pageSize,
      name: employeeSearch.keyword || undefined
    })
    if (res.code === 1) {
      employeeList.value = res.data.records
      employeeTotal.value = res.data.total
    }
  } finally {
    employeeLoading.value = false
  }
}

const searchEmployees = () => {
  employeeSearch.page = 1
  loadEmployeeList()
}

const selectEmployee = (emp: Employee) => {
  selectedEmployeeId.value = emp.id
  selectedEmployeeName.value = emp.name
  loadDetail(emp.id)
}

// ========== 右侧详情表单 ==========
const detailLoading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const detailExists = ref(false)

// 周一~周日（1=周一 … 7=周日，与入库 JSON 的 days 一致）
const WEEK_DAYS = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const form = reactive({
  educationList: [] as { degree: string; school: string }[],
  experienceList: [] as { dateRange: string[]; description: string }[],
  position: '',
  salary: '',
  workStatus: '',
  // 排班：勾选的星期 + 每个星期各自的一组时段
  scheduleDays: [] as number[],
  scheduleMap: {} as Record<number, { start: string; end: string }[]>,
  contract: '',
  healthCertificate: '',
  updateTime: ''
})

const contractFileList = ref<any[]>([])
const healthFileList = ref<any[]>([])

const loadDetail = async (employeeId: number) => {
  detailLoading.value = true
  resetForm()
  try {
    const res: any = await getEmployeeDetail(employeeId)
    if (res.code === 1 && res.data) {
      const detail: EmployeeDetail = res.data
      detailExists.value = true

      // 解析学历JSON
      if (detail.education) {
        try {
          form.educationList = JSON.parse(detail.education)
        } catch { form.educationList = [] }
      }
      // 解析经历JSON
      if (detail.pastExperience) {
        try {
          form.experienceList = JSON.parse(detail.pastExperience)
        } catch { form.experienceList = [] }
      }
      // 解析排班JSON：{"1":[{"start":"09:00","end":"11:00"}], "3":[...]}
      // 空值或格式损坏都回落到空白，不阻塞页面
      form.scheduleDays = []
      form.scheduleMap = {}
      if (detail.schedule) {
        try {
          const sch = JSON.parse(detail.schedule)
          Object.keys(sch || {}).forEach((key) => {
            const day = Number(key)
            const slots = sch[key]
            if (
              Number.isInteger(day) && day >= 1 && day <= 7 &&
              Array.isArray(slots) && slots.length > 0
            ) {
              form.scheduleDays.push(day)
              form.scheduleMap[day] = slots.map((s: any) => ({
                start: (s && s.start) || '',
                end: (s && s.end) || ''
              }))
            }
          })
          form.scheduleDays.sort((a, b) => a - b)
        } catch {
          form.scheduleDays = []
          form.scheduleMap = {}
        }
      }
      form.position = detail.position || ''
      form.salary = detail.salary || ''
      form.workStatus = detail.workStatus || ''
      form.contract = detail.contract || ''
      form.healthCertificate = detail.healthCertificate || ''
      form.updateTime = detail.updateTime || ''

      // 文件列表显示
      if (detail.contract) {
        contractFileList.value = [{ name: '已上传合同', url: detail.contract }]
      }
      if (detail.healthCertificate) {
        healthFileList.value = [{ name: '已上传健康证', url: detail.healthCertificate }]
      }
    } else {
      detailExists.value = false
    }
  } catch {
    detailExists.value = false
  } finally {
    detailLoading.value = false
  }
}

const resetForm = () => {
  form.educationList = []
  form.experienceList = []
  form.position = ''
  form.salary = ''
  form.workStatus = ''
  form.scheduleDays = []
  form.scheduleMap = {}
  form.contract = ''
  form.healthCertificate = ''
  form.updateTime = ''
  contractFileList.value = []
  healthFileList.value = []
  detailExists.value = false
}

// 学历操作
const addEducation = () => {
  form.educationList.push({ degree: '', school: '' })
}
const removeEducation = (index: number) => {
  form.educationList.splice(index, 1)
}

// 经历操作
const addExperience = () => {
  form.experienceList.push({ dateRange: [], description: '' })
}
const removeExperience = (index: number) => {
  form.experienceList.splice(index, 1)
}

// ========== 排班 ==========
// 入库结构：{"1":[{"start":"09:00","end":"11:00"}], "3":[{"start":"14:00","end":"16:00"}]}
// key 是星期（1=周一 … 7=周日），value 是该天自己的时段列表；没有 key 的天表示不排班

// 已勾选的星期，升序
const selectedDays = computed(() => [...form.scheduleDays].sort((a, b) => a - b))

const weekLabel = (day: number) => WEEK_DAYS[day - 1]?.label || ''

// 勾上某天后，给它一个空的时段行；取消勾选后不清 map，重新勾选能恢复已填内容
watch(
  () => [...form.scheduleDays],
  (days: number[]) => {
    for (const d of days) {
      if (!form.scheduleMap[d] || form.scheduleMap[d].length === 0) {
        form.scheduleMap[d] = [{ start: '', end: '' }]
      }
    }
  }
)

const addScheduleSlot = (day: number) => {
  form.scheduleMap[day].push({ start: '', end: '' })
}
const removeScheduleSlot = (day: number, index: number) => {
  form.scheduleMap[day].splice(index, 1)
}

// 某天里起止都填了的时段
const validSlotsOf = (day: number) =>
  (form.scheduleMap[day] || []).filter((s) => s.start && s.end)

// 实时预览："周一 09:00-11:00；周三 14:00-16:00、18:00-20:00"
const schedulePreview = computed(() => {
  const parts: string[] = []
  for (const d of selectedDays.value) {
    const slots = validSlotsOf(d)
    if (slots.length === 0) continue
    parts.push(`${weekLabel(d)} ${slots.map((s) => `${s.start}-${s.end}`).join('、')}`)
  }
  return parts.join('；')
})

// 文件上传
const pendingFiles = reactive<Record<string, File | null>>({})

const handleFileChange = (uploadFile: UploadFile, field: string) => {
  const rawFile = uploadFile.raw as File
  if (rawFile) {
    pendingFiles[field] = rawFile
  }
}

const handleFileRemove = (field: string) => {
  delete pendingFiles[field]
  if (field === 'contract') {
    form.contract = ''
  } else {
    form.healthCertificate = ''
  }
}

const uploadPendingFile = async (field: string): Promise<string | null> => {
  const file = pendingFiles[field]
  if (!file) return (form as any)[field] || null

  try {
    const res: any = await uploadFile(file)
    if (res.code === 1) {
      const url = res.data
      ;(form as any)[field] = url
      delete pendingFiles[field]
      return url
    } else {
      ElMessage.error(`${field === 'contract' ? '合同' : '健康证'}上传失败`)
      return null
    }
  } catch {
    ElMessage.error(`${field === 'contract' ? '合同' : '健康证'}上传失败`)
    return null
  }
}

// 下载并查看文件
const handleDownloadFile = async (fileUrl: string) => {
  const blobUrl = await fetchFileAsBlob(fileUrl)
  if (blobUrl) {
    const a = document.createElement('a')
    a.href = blobUrl
    a.download = fileUrl.split('/').pop() || 'file'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(blobUrl)
  } else {
    ElMessage.error('文件下载失败')
  }
}

// 保存
const handleSave = async () => {
  if (!selectedEmployeeId.value) return

  saving.value = true
  try {
    // 先上传待上传的文件
    if (pendingFiles['contract']) {
      await uploadPendingFile('contract')
    }
    if (pendingFiles['healthCertificate']) {
      await uploadPendingFile('healthCertificate')
    }

    // 排班校验：勾选的每一天至少要有一个完整时段，且每段结束要晚于开始
    for (const d of selectedDays.value) {
      const slots = validSlotsOf(d)
      if (slots.length === 0) {
        ElMessage.warning(`已勾选${weekLabel(d)}，请至少填写一个完整时段（开始和结束）`)
        return
      }
      for (const s of slots) {
        if (s.start >= s.end) {
          ElMessage.warning(`${weekLabel(d)} 的时段 ${s.start}-${s.end} 不合法：结束时间必须晚于开始时间`)
          return
        }
      }
    }

    // 组装排班 JSON：只为填好时段的星期写入 key
    const scheduleJson: Record<string, { start: string; end: string }[]> = {}
    for (const d of selectedDays.value) {
      const slots = validSlotsOf(d)
      if (slots.length > 0) {
        scheduleJson[String(d)] = slots.map((s) => ({ start: s.start, end: s.end }))
      }
    }

    const data: EmployeeDetail = {
      employeeId: selectedEmployeeId.value,
      education: form.educationList.length > 0 ? JSON.stringify(form.educationList) : '',
      pastExperience: form.experienceList.length > 0 ? JSON.stringify(form.experienceList) : '',
      position: form.position,
      salary: form.salary,
      workStatus: form.workStatus,
      // 无排班时存空字符串（不是 null），保证清空后能落库 —— 后端 XML 是 <if test="schedule != null">
      schedule: Object.keys(scheduleJson).length > 0 ? JSON.stringify(scheduleJson) : '',
      contract: form.contract,
      healthCertificate: form.healthCertificate
    }

    let res: any
    if (detailExists.value) {
      res = await updateEmployeeDetail(data)
    } else {
      res = await addEmployeeDetail(data)
    }

    if (res.code === 1) {
      ElMessage.success(detailExists.value ? '修改成功' : '新增成功')
      loadDetail(selectedEmployeeId.value)
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除
const handleDelete = async () => {
  if (!selectedEmployeeId.value) return

  try {
    await ElMessageBox.confirm('确定要删除该员工的详细信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    deleting.value = true
    const res: any = await deleteEmployeeDetail(selectedEmployeeId.value)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      resetForm()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    deleting.value = false
  }
}

// 日期格式化
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const [y, m, d] = dateStr.split('-')
  return `${y}年${parseInt(m)}月${parseInt(d)}日`
}

const initEmployeeId = Number(route.query.id) || null

// 列表加载完后自动匹配名称
watch(employeeList, (list) => {
  if (initEmployeeId && selectedEmployeeId.value === initEmployeeId) {
    const emp = list.find(e => e.id === initEmployeeId)
    if (emp) selectedEmployeeName.value = emp.name
  }
})

onMounted(() => {
  loadEmployeeList()
  if (initEmployeeId) {
    selectedEmployeeId.value = initEmployeeId
    loadDetail(initEmployeeId)
  }
})
</script>

<style scoped>
.employee-detail-page {
  display: flex;
  height: calc(100vh - 120px);
  gap: 0;
}

/* 左侧面板 */
.left-panel {
  width: 260px;
  min-width: 260px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  margin-right: 16px;
  overflow: hidden;
}

.panel-title {
  padding: 16px 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.panel-search {
  padding: 12px 16px;
}

.employee-items {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.employee-item {
  padding: 12px 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
  transition: background 0.2s;
}

.employee-item:hover {
  background: #f0f2f5;
}

.employee-item.active {
  background: #e8f0fe;
  border-left: 3px solid #1e3a8a;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.item-username {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.panel-pagination {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions {
  display: flex;
  gap: 8px;
}

.form-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.form-section {
  margin-bottom: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-label {
  font-weight: 500;
  color: #303133;
}

.entry-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.entry-row-vertical {
  flex-direction: column;
  align-items: flex-start;
}

.exp-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.entry-preview {
  font-size: 13px;
  color: #409eff;
  white-space: nowrap;
}

/* 排班编辑器 */
.schedule-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-days {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.schedule-slots {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.schedule-tip {
  font-size: 13px;
  color: #909399;
}

/* 单个星期几的时段编辑块 */
.schedule-day-block {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fafafa;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.schedule-day-label {
  flex-shrink: 0;
  width: 44px;
  font-size: 13px;
  color: #606266;
  line-height: 32px;
}

/* 排班预览可能很长，允许换行，覆盖 .entry-preview 的 nowrap */
.schedule-slots .entry-preview {
  white-space: normal;
}

.file-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.file-tip {
  font-size: 12px;
  color: #e6a23c;
}

.update-time {
  color: #909399;
}

.no-selection {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-button--primary) {
  background-color: #1e3a8a;
  border-color: #1e3a8a;
}

:deep(.el-button--primary:hover) {
  background-color: #2563eb;
  border-color: #2563eb;
}
</style>
