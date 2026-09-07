<template>
  <div class="schedule-management">
    <div class="search-bar">
      <div class="search-left">
        <el-select v-model="searchForm.housekeeperId" placeholder="选择家政员" clearable style="width:220px;margin-right:10px">
          <el-option v-for="hk in hkList" :key="hk.id" :label="hk.name" :value="hk.id" />
        </el-select>
        <el-date-picker v-model="searchForm.scheduleDate" type="date" placeholder="选择日期" clearable value-format="YYYY-MM-DD" style="width:180px;margin-right:10px" />
        <el-button type="default" style="background:#000;color:#fff" @click="handleSearch" :loading="searchLoading">查询</el-button>
      </div>
      <div class="search-right">
        <el-button type="warning" @click="handleBatchToggleStatus" :disabled="selectedIds.length === 0" :loading="batchStatusLoading">批量切换状态</el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0" :loading="batchDeleteLoading">批量删除</el-button>
        <el-button type="primary" @click="handleAdd">+ 新增时段</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width:100%;margin-top:20px" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="housekeeperName" label="家政员姓名" min-width="120" />
      <el-table-column prop="scheduleDate" label="日期" min-width="150" />
      <el-table-column label="时段" min-width="250">
        <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '可约' : '不可约' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="200" />
      <el-table-column label="操作" min-width="250">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="warning" @click="handleToggleStatus(row)" :loading="togglingId === row.id">{{ togglingId === row.id ? '' : (row.status === 1 ? '设为不可约' : '设为可约') }}</el-button>
          <el-button link type="danger" @click="handleDelete(row)" :loading="deletingId === row.id">{{ deletingId === row.id ? '' : '删除' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :page-sizes="[5,10,20,50]" :total="pagination.total" layout="total,sizes,prev,pager,next,jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="家政员" prop="housekeeperId">
          <el-select v-model="form.housekeeperId" placeholder="选择家政员" style="width:100%" :disabled="isEdit">
            <el-option v-for="hk in hkList" :key="hk.id" :label="hk.name" :value="hk.id" />
          </el-select>
        </el-form-item>
        <!-- 批量模式：日期范围 + 多时段 -->
        <template v-if="!isEdit && batchMode">
          <el-form-item label="日期范围">
            <div style="display:flex;gap:8px;align-items:center">
              <el-date-picker v-model="batchStartDate" type="date" placeholder="开始" style="width:170px" value-format="YYYY-MM-DD" />
              <span>至</span>
              <el-date-picker v-model="batchEndDate" type="date" placeholder="结束" style="width:170px" value-format="YYYY-MM-DD" />
            </div>
          </el-form-item>
          <el-form-item label="时段列表">
            <div v-for="(slot, i) in batchSlots" :key="i" style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
              <el-time-picker v-model="slot.start" placeholder="开始" format="HH:mm" value-format="HH:mm" style="width:130px" />
              <span>~</span>
              <el-time-picker v-model="slot.end" placeholder="结束" format="HH:mm" value-format="HH:mm" style="width:130px" />
              <el-button v-if="!isEdit && batchSlots.length > 1" type="danger" plain size="small" @click="batchSlots.splice(i,1)">删除</el-button>
            </div>
            <div style="margin-top:10px;width:100%" v-if="!isEdit"><el-button type="primary" plain size="small" @click="batchSlots.push({start:'',end:''})">+ 添加时段</el-button></div>
          </el-form-item>
        </template>
        <!-- 单日模式（也支持多时段） -->
        <template v-else>
          <el-form-item label="日期" prop="scheduleDate">
            <el-date-picker v-model="form.scheduleDate" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="时段列表">
            <div v-for="(slot, i) in batchSlots" :key="i" style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
              <el-time-picker v-model="slot.start" placeholder="开始" format="HH:mm" value-format="HH:mm" style="width:130px" />
              <span>~</span>
              <el-time-picker v-model="slot.end" placeholder="结束" format="HH:mm" value-format="HH:mm" style="width:130px" />
              <el-button v-if="!isEdit && batchSlots.length > 1" type="danger" plain size="small" @click="batchSlots.splice(i,1)">删除</el-button>
            </div>
            <div style="margin-top:10px;width:100%" v-if="!isEdit"><el-button type="primary" plain size="small" @click="batchSlots.push({start:'',end:''})">+ 添加时段</el-button></div>
          </el-form-item>
        </template>
        <el-form-item v-if="!isEdit" label="模式">
          <el-radio-group v-model="batchMode">
            <el-radio :value="false">单日</el-radio>
            <el-radio :value="true">批量日期</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isEdit && batchMode" type="primary" @click="handleBatchSubmit" :loading="batchLoading">批量生成</el-button>
        <el-button v-else type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getSchedulePage, addSchedule, updateSchedule, deleteSchedule, toggleScheduleStatus, type HousekeeperSchedule, type HousekeeperScheduleDTO, type HousekeeperSchedulePageQueryParams } from '@/api/housekeeper-schedule'
import request from '@/utils/request'

interface Housekeeper { id: number; name: string }

const hkList = ref<Housekeeper[]>([])
const searchForm = reactive({ housekeeperId: undefined as number | undefined, scheduleDate: '' as string })
const tableData = ref<HousekeeperSchedule[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增时段')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const batchMode = ref(false)
const batchStartDate = ref('')
const batchEndDate = ref('')
const batchSlots = ref<{ start: string; end: string }[]>([{ start: '', end: '' }])
const batchLoading = ref(false)
const submitLoading = ref(false)
const batchDeleteLoading = ref(false)
const batchStatusLoading = ref(false)
const searchLoading = ref(false)
const deletingId = ref<number | null>(null)
const togglingId = ref<number | null>(null)
const selectedIds = ref<number[]>([])
const selectedRows = ref<HousekeeperSchedule[]>([])
const handleSelectionChange = (rows: HousekeeperSchedule[]) => {
  selectedIds.value = rows.map(r => r.id)
  selectedRows.value = rows
}
const handleBatchDelete = async () => {
  if (batchDeleteLoading.value) return
  try {
    await ElMessageBox.confirm(`确定删除选中的${selectedIds.value.length}条吗？`, '批量删除', { type: 'warning' })
    batchDeleteLoading.value = true
    await request({ url: '/employee/housekeeper-schedule/batch', method: 'delete', data: selectedIds.value })
    ElMessage.success('批量删除成功')
    loadList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('删除失败') }
  finally { batchDeleteLoading.value = false }
}
const handleBatchToggleStatus = async () => {
  if (batchStatusLoading.value) return
  // 如果全是不可约 → 设为可约，否则 → 设为不可约
  const allDisabled = selectedRows.value.every(r => r.status === 0)
  const targetStatus = allDisabled ? 1 : 0
  const label = targetStatus === 1 ? '可约' : '不可约'
  try {
    await ElMessageBox.confirm(`确定将选中的${selectedIds.value.length}条设为"${label}"吗？`, '批量切换状态', { type: 'warning' })
    batchStatusLoading.value = true
    await request({ url: `/employee/housekeeper-schedule/batch/status/${targetStatus}`, method: 'put', data: selectedIds.value })
    ElMessage.success(`已批量设为${label}`)
    loadList()
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('操作失败') }
  finally { batchStatusLoading.value = false }
}

const form = reactive<HousekeeperScheduleDTO>({ id: undefined, housekeeperId: undefined, scheduleDate: '', startTime: '', endTime: '', status: 0 })
const formRules: FormRules = {
  housekeeperId: [{ required: true, message: '请选择家政员', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const loadHkList = async () => {
  const res: any = await request({ url: '/employee/housekeeper/page', method: 'get', params: { page: 1, pageSize: 100 } })
  if (res.code === 1) hkList.value = res.data.records || []
}

const loadList = async () => {
  const params: HousekeeperSchedulePageQueryParams = { page: pagination.page, pageSize: pagination.pageSize, housekeeperId: searchForm.housekeeperId, scheduleDate: searchForm.scheduleDate || undefined }
  searchLoading.value = true
  try {
    const res: any = await getSchedulePage(params)
    if (res.code === 1) { tableData.value = res.data.records; pagination.total = res.data.total }
  } finally { searchLoading.value = false }
}

const handleSearch = () => { pagination.page = 1; loadList() }
const handleAdd = () => { dialogTitle.value = '新增时段'; isEdit.value = false; dialogVisible.value = true }
const handleEdit = (row: HousekeeperSchedule) => {
  dialogTitle.value = '修改时段'; isEdit.value = true
  form.id = row.id; form.housekeeperId = row.housekeeperId; form.scheduleDate = row.scheduleDate
  batchSlots.value = [{ start: row.startTime ? row.startTime.substring(0, 5) : '', end: row.endTime ? row.endTime.substring(0, 5) : '' }]
  dialogVisible.value = true
}
const handleDelete = async (row: HousekeeperSchedule) => {
  if (deletingId.value) return
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    deletingId.value = row.id
    const res: any = await deleteSchedule(row.id)
    if (res.code === 1) { ElMessage.success('删除成功'); loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('删除失败') }
  finally { deletingId.value = null }
}
const handleToggleStatus = async (row: HousekeeperSchedule) => {
  if (togglingId.value) return
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '可约' : '不可约'
  try {
    await ElMessageBox.confirm(`确定将该时段设为"${label}"吗？`, '切换状态', { type: 'warning' })
    togglingId.value = row.id
    const res: any = await toggleScheduleStatus(row.id, newStatus)
    if (res.code === 1) { ElMessage.success(`已设为${label}`); loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('操作失败') }
  finally { togglingId.value = null }
}
const handleSubmit = async () => {
  if (!formRef.value || submitLoading.value) return
  submitLoading.value = true
  try {
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      // 修改模式：单条更新
      if (isEdit.value) {
        const slot = batchSlots.value[0]
        if (!slot || !slot.start || !slot.end) { ElMessage.error('请选择时段'); return }
        try {
          const res: any = await updateSchedule({ ...form, startTime: slot.start, endTime: slot.end })
          if (res.code === 1) { ElMessage.success('修改成功'); dialogVisible.value = false; loadList() }
        } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
        return
      }
      // 新增单日模式：支持多时段
      if (!form.scheduleDate) { ElMessage.error('请选择日期'); return }
      const validSlots = batchSlots.value.filter(s => s.start && s.end)
      if (validSlots.length === 0) { ElMessage.error('请至少添加一个完整时段'); return }
      let success = 0, fail = 0
      for (const slot of validSlots) {
        try {
          await addSchedule({ housekeeperId: form.housekeeperId, scheduleDate: form.scheduleDate, startTime: slot.start, endTime: slot.end })
          success++
        } catch { fail++ }
      }
      ElMessage.success(`新增成功${success}条` + (fail > 0 ? `，失败${fail}条` : ''))
      dialogVisible.value = false
      loadList()
    })
  } finally {
    submitLoading.value = false
  }
}
/** 批量生成：日期范围内每天 × 每个时段 = 全部组合 */
const handleBatchSubmit = async () => {
  if (!form.housekeeperId) { ElMessage.error('请选择家政员'); return }
  if (!batchStartDate.value || !batchEndDate.value) { ElMessage.error('请选择日期范围'); return }
  if (new Date(batchStartDate.value) > new Date(batchEndDate.value)) { ElMessage.error('开始日期不能晚于结束日期'); return }
  // 校验时段
  const validSlots = batchSlots.value.filter(s => s.start && s.end)
  if (validSlots.length === 0) { ElMessage.error('请至少添加一个完整时段'); return }

  const dates: string[] = []
  const start = new Date(batchStartDate.value)
  const end = new Date(batchEndDate.value)
  for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    dates.push(d.toISOString().substring(0, 10))
  }

  batchLoading.value = true
  let success = 0, fail = 0
  for (const date of dates) {
    for (const slot of validSlots) {
      try {
        await addSchedule({ housekeeperId: form.housekeeperId, scheduleDate: date, startTime: slot.start, endTime: slot.end })
        success++
      } catch { fail++ }
    }
  }
  batchLoading.value = false
  ElMessage.success(`生成完成：成功${success}条` + (fail > 0 ? `，失败${fail}条` : ''))
  dialogVisible.value = false
  loadList()
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = undefined; form.housekeeperId = undefined; form.scheduleDate = ''; form.startTime = ''; form.endTime = ''
  batchMode.value = false; batchStartDate.value = ''; batchEndDate.value = ''
  batchSlots.value = [{ start: '', end: '' }]
}
const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }
onMounted(() => { loadHkList(); loadList() })
</script>

<style scoped>
.schedule-management { padding: 0; min-height: 100%; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
</style>
