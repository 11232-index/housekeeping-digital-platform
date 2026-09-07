<template>
  <div class="about-management">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchForm.title" placeholder="题目" clearable style="width:200px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-button type="default" style="background:#000;color:#fff" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">+ 新增</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width:100%;margin-top:20px" border>
      <el-table-column prop="title" label="题目" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="内容" min-width="400">
        <template #default="{ row }">
          <div class="content-cell">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="danger" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :page-sizes="[5,10,20,50]" :total="pagination.total" layout="total,sizes,prev,pager,next,jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="题目" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" placeholder="越大越靠前" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getAboutPage, addAbout, updateAbout, deleteAbout, type About, type AboutDTO, type AboutPageQueryParams } from '@/api/about'

const searchForm = reactive({ title: '' })
const tableData = ref<About[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增')
const isEdit = ref(false)

const form = reactive<AboutDTO>({ id: undefined, title: '', sortOrder: 0, content: '' })
const formRef = ref<FormInstance>()
const searchLoading = ref(false)
const deletingId = ref<number | undefined>(undefined)
const submitLoading = ref(false)
const formRules: FormRules = {
  title: [{ required: true, message: '请输入题目', trigger: 'blur' }]
}

const loadList = async () => {
  const params: AboutPageQueryParams = { page: pagination.page, pageSize: pagination.pageSize, title: searchForm.title || undefined }
  const res: any = await getAboutPage(params)
  if (res.code === 1) { tableData.value = res.data.records; pagination.total = res.data.total }
}

const handleSearch = async () => {
  searchLoading.value = true
  try { pagination.page = 1; await loadList() }
  finally { searchLoading.value = false }
}
const handleAdd = () => { dialogTitle.value = '新增'; isEdit.value = false; dialogVisible.value = true }
const handleEdit = (row: About) => {
  dialogTitle.value = '修改'; isEdit.value = true
  form.id = row.id; form.title = row.title; form.sortOrder = row.sortOrder || 0; form.content = row.content
  dialogVisible.value = true
}
const handleDelete = async (row: About) => {
  if (deletingId.value !== undefined) return
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    const res: any = await deleteAbout(row.id)
    if (res.code === 1) { ElMessage.success('删除成功'); loadList() }
  } catch (e: any) { if (e !== 'cancel') ElMessage.error('删除失败') }
  finally { deletingId.value = undefined }
}
const handleSubmit = async () => {
  if (!formRef.value || submitLoading.value) return
  submitLoading.value = true
  try {
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      try {
        const res: any = isEdit.value ? await updateAbout(form) : await addAbout(form)
        if (res.code === 1) { ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; loadList() }
      } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
    })
  } finally { submitLoading.value = false }
}
const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = undefined; form.title = ''; form.sortOrder = 0; form.content = ''
}
const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }
onMounted(() => loadList())
</script>

<style scoped>
.about-management { padding: 0; min-height: 100%; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
.content-cell { line-height: 1.6; word-break: break-all !important; }
:deep(.el-textarea__inner) { word-break: break-all !important; }
</style>
