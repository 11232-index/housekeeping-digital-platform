<template>
  <div class="hot-picks-management">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchForm.name" placeholder="服务名称" clearable style="width:200px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width:140px;margin-right:10px" @change="handleSearch">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="default" style="background:#000;color:#fff" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">+ 新增推荐</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width:100%;margin-top:20px" border>
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column label="图片" min-width="120">
        <template #default="{ row }">
          <img v-if="row.picture" :src="row.picture" style="width:80px;height:80px;object-fit:cover;cursor:pointer;border-radius:4px" @click="previewSrc=row.picture;previewVisible=true" />
          <span v-else style="color:#ccc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" min-width="100" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" min-width="280">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" :loading="togglingId === row.id" :disabled="togglingId !== undefined" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload class="picture-upload" :action="uploadAction" :headers="uploadHeaders"
            :show-file-list="false" :on-success="handleUploadSuccess" :on-error="handleUploadError"
            :before-upload="beforeUpload" accept="image/*">
            <img v-if="form.picture" :src="form.picture" class="upload-preview" />
            <el-button v-else type="primary">点击上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" placeholder="越小越靠前" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <ImageViewer :visible="previewVisible" :src="previewSrc" @close="previewVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import ImageViewer from '@/components/ImageViewer.vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  getHotPicksPage, addHotPicks, updateHotPicks, updateHotPicksStatus, deleteHotPicks,
  type HotPicks, type HotPicksDTO, type HotPicksPageQueryParams
} from '@/api/hot-picks'

const searchForm = reactive({ name: '', status: undefined as number | undefined })
const tableData = ref<HotPicks[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增推荐')
const isEdit = ref(false)

const previewVisible = ref(false)
const previewSrc = ref('')
const form = reactive<HotPicksDTO>({ id: undefined, name: '', picture: '', status: 0, sortOrder: 0 })
const formRef = ref<FormInstance>()
const searchLoading = ref(false)
const togglingId = ref<number>()
const deletingId = ref<number>()
const submitLoading = ref(false)
const formRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }]
}

// 上传
const uploadAction = 'http://localhost:8080/admin/common/upload'
const uploadHeaders = { token: sessionStorage.getItem('token') || '' }
const handleUploadSuccess = (res: any) => {
  if (res.code === 1 && res.data) { form.picture = res.data; ElMessage.success('上传成功') }
  else { ElMessage.error(res.msg || '上传失败') }
}
const handleUploadError = () => ElMessage.error('上传失败')
const beforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('不超过5MB'); return false }
  return true
}

const loadList = async () => {
  const params: HotPicksPageQueryParams = { page: pagination.page, pageSize: pagination.pageSize, name: searchForm.name || undefined, status: searchForm.status }
  const res: any = await getHotPicksPage(params)
  if (res.code === 1) { tableData.value = res.data.records; pagination.total = res.data.total }
}

const handleSearch = async () => {
  searchLoading.value = true
  try { pagination.page = 1; await loadList() } finally { searchLoading.value = false }
}
const handleAdd = () => { dialogTitle.value = '新增推荐'; isEdit.value = false; dialogVisible.value = true }

const handleEdit = (row: HotPicks) => {
  dialogTitle.value = '修改推荐'; isEdit.value = true
  form.id = row.id; form.name = row.name || ''; form.picture = row.picture || ''; form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

const handleToggleStatus = async (row: HotPicks) => {
  if (togglingId.value !== undefined) return
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  togglingId.value = row.id
  try {
    await ElMessageBox.confirm(`确定${action}吗？`, '提示', { type: 'warning' })
    const res: any = await updateHotPicksStatus(newStatus, row.id)
    if (res.code === 1) { ElMessage.success(action + '成功'); await loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('操作失败') }
  finally { togglingId.value = undefined }
}

const handleDelete = async (row: HotPicks) => {
  if (deletingId.value !== undefined) return
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    const res: any = await deleteHotPicks(row.id)
    if (res.code === 1) { ElMessage.success('删除成功'); await loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('删除失败') }
  finally { deletingId.value = undefined }
}

const handleSubmit = async () => {
  if (submitLoading.value) return
  if (!formRef.value) return
  if (!form.picture) { ElMessage.error('请上传图片'); return }
  submitLoading.value = true
  try {
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      try {
        const res: any = isEdit.value ? await updateHotPicks(form) : await addHotPicks({ name: form.name, picture: form.picture, status: 0, sortOrder: form.sortOrder })
        if (res.code === 1) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; await loadList() }
      } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
    })
  } finally { submitLoading.value = false }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = undefined; form.name = ''; form.picture = ''; form.status = 0; form.sortOrder = 0
}

const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }

onMounted(() => loadList())
</script>

<style scoped>
.hot-picks-management { padding: 0; min-height: 100%; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
.upload-preview { width: 120px; height: 120px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd; }
</style>
