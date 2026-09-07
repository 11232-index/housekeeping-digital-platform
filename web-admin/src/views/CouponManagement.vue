<template>
  <div class="coupon-management">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchForm.title" placeholder="优惠券标题" clearable style="width:200px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-select v-model="searchForm.type" placeholder="类型" clearable style="width:130px;margin-right:10px" @change="handleSearch">
          <el-option label="普通券" :value="0" />
          <el-option label="秒杀券" :value="1" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width:130px;margin-right:10px" @change="handleSearch">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="2" />
          <el-option label="过期" :value="3" />
        </el-select>
        <el-button type="default" style="background:#000;color:#fff" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">+ 新增优惠券</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width:100%;margin-top:20px" border>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="类型" min-width="90">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'danger' : 'primary'" size="small">{{ row.type === 1 ? '秒杀券' : '普通券' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="折扣" min-width="120">
        <template #default="{ row }">
          <span>{{ discountText(row.actualValue) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="库存" min-width="80">
        <template #default="{ row }">
          <span>{{ row.type === 1 ? (row.stock ?? '—') : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生效时间" min-width="170">
        <template #default="{ row }">
          <span>{{ row.type === 1 && row.beginTime ? row.beginTime : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失效时间" min-width="170">
        <template #default="{ row }">
          <span>{{ row.type === 1 && row.endTime ? row.endTime : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" min-width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-dropdown trigger="click" @command="(cmd: any) => handleChangeStatus(row, cmd)" class="status-dropdown">
            <el-button link type="warning">{{ statusText(row.status) }}<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="1" :disabled="row.status === 1">上架</el-dropdown-item>
                <el-dropdown-item command="2" :disabled="row.status === 2">下架</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button link type="danger" :loading="deletingId === row.id" :disabled="deletingId !== undefined" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :page-sizes="[5,10,20,50]" :total="pagination.total"
        layout="total,sizes,prev,pager,next,jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入优惠券标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subTitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width:100%">
            <el-option label="普通券" :value="0" />
            <el-option label="秒杀券" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="折扣" prop="discount">
          <el-input-number v-model="form.discount" :min="1" :max="10" :precision="1" :step="0.5" style="width:100%" />
          <div class="discount-tip">如：8 表示八折，8.5 表示八五折</div>
        </el-form-item>
        <el-form-item label="使用规则">
          <el-input v-model="form.rules" type="textarea" :rows="2" placeholder="请输入使用规则" />
        </el-form-item>
        <template v-if="form.type === 1">
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" :precision="0" style="width:100%" />
          </el-form-item>
          <el-form-item label="生效时间" prop="beginTime">
            <el-date-picker v-model="form.beginTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择生效时间" style="width:100%" />
          </el-form-item>
          <el-form-item label="失效时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择失效时间" style="width:100%" />
          </el-form-item>
        </template>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  getVoucherPage, getVoucherById, addVoucher, updateVoucher, updateVoucherStatus, deleteVoucher,
  type Voucher, type VoucherDTO, type VoucherPageQueryParams
} from '@/api/coupon'

const searchForm = reactive({ title: '', type: undefined as number | undefined, status: undefined as number | undefined })
const tableData = ref<Voucher[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const isEdit = ref(false)

// 表单：金额以"元"为单位输入，提交时后端按"分"存储
const form = reactive({
  id: undefined as number | string | undefined,
  title: '',
  subTitle: '',
  rules: '',
  type: 0,
  status: 1,
  stock: 0,
  beginTime: '',
  endTime: '',
  discount: 8,
})
const formRef = ref<FormInstance>()
const searchLoading = ref(false)
const changingId = ref<number | string>()
const deletingId = ref<number | string>()
const submitLoading = ref(false)
const formRules = {
  title: [{ required: true, message: '请输入优惠券标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  discount: [{ required: true, message: '请输入折扣', trigger: 'blur' }],
}

const statusText = (s: number) => (s === 1 ? '上架' : s === 2 ? '下架' : '过期')
const statusTagType = (s: number) => (s === 1 ? 'success' : s === 2 ? 'info' : 'danger')
// 折扣率转折数显示：80 → 8折，85 → 8.5折
const discountText = (v: number) => `${v / 10}折`

const loadList = async () => {
  const params: VoucherPageQueryParams = {
    page: pagination.page, pageSize: pagination.pageSize,
    title: searchForm.title || undefined,
    type: searchForm.type,
    status: searchForm.status,
  }
  const res: any = await getVoucherPage(params)
  if (res.code === 1) { tableData.value = res.data.records; pagination.total = res.data.total }
}

const handleSearch = async () => {
  searchLoading.value = true
  try { pagination.page = 1; await loadList() } finally { searchLoading.value = false }
}

const handleAdd = () => {
  dialogTitle.value = '新增优惠券'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row: Voucher) => {
  dialogTitle.value = '修改优惠券'
  isEdit.value = true
  // 优先拉取完整详情（含秒杀库存/起止时间），列表数据不全时用行数据兜底
  let detail: Voucher = row
  try {
    const res: any = await getVoucherById(row.id)
    if (res.code === 1 && res.data) { detail = res.data }
  } catch (e) { /* 使用列表数据兜底 */ }
  form.id = detail.id
  form.title = detail.title || ''
  form.subTitle = detail.subTitle || ''
  form.rules = detail.rules || ''
  form.type = detail.type
  form.status = detail.status
  form.stock = detail.stock ?? 0
  form.beginTime = detail.beginTime || ''
  form.endTime = detail.endTime || ''
  form.discount = detail.actualValue / 10
  dialogVisible.value = true
}

const handleChangeStatus = async (row: Voucher, newStatus: any) => {
  if (changingId.value !== undefined) return
  const status = Number(newStatus)
  if (row.status === status) { ElMessage.info('当前已是该状态'); return }
  changingId.value = row.id
  try {
    await ElMessageBox.confirm(`确定将优惠券置为「${statusText(status)}」吗？`, '提示', { type: 'warning' })
    const res: any = await updateVoucherStatus(status, row.id)
    if (res.code === 1) { ElMessage.success('操作成功'); await loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('操作失败') }
  finally { changingId.value = undefined }
}

const handleDelete = async (row: Voucher) => {
  if (deletingId.value !== undefined) return
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm('确定删除该优惠券吗？', '提示', { type: 'warning' })
    const res: any = await deleteVoucher(row.id)
    if (res.code === 1) { ElMessage.success('删除成功'); await loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('删除失败') }
  finally { deletingId.value = undefined }
}

const handleSubmit = async () => {
  if (submitLoading.value) return
  if (!formRef.value) return
  submitLoading.value = true
  try {
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      if (form.type === 1 && (!form.beginTime || !form.endTime)) {
        ElMessage.error('请选择秒杀券的生效时间和失效时间')
        return
      }
      if (form.type === 1 && form.beginTime && form.endTime && form.beginTime >= form.endTime) {
        ElMessage.error('生效时间必须早于失效时间')
        return
      }
      const data: VoucherDTO = {
        id: form.id,
        title: form.title,
        subTitle: form.subTitle,
        rules: form.rules,
        type: form.type,
        status: form.status,
        actualValue: Math.round(form.discount * 10),
      }
      if (form.type === 1) {
        data.stock = form.stock
        data.beginTime = form.beginTime
        data.endTime = form.endTime
      }
      try {
        const res: any = isEdit.value ? await updateVoucher(data) : await addVoucher(data)
        if (res.code === 1) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; await loadList() }
      } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
    })
  } finally { submitLoading.value = false }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = undefined; form.title = ''; form.subTitle = ''; form.rules = ''
  form.type = 0; form.status = 1; form.stock = 0; form.beginTime = ''; form.endTime = ''
  form.discount = 8
}

const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }

onMounted(() => loadList())
</script>

<style scoped>
.coupon-management { padding: 0; min-height: 100%; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
.discount-tip { font-size: 12px; color: #999; line-height: 1.5; margin-top: 4px; }
/* 状态下拉框与相邻按钮对齐 */
.status-dropdown {
  margin-left: 12px;
  vertical-align: middle;
}
</style>
