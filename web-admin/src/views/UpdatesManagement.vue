<template>
  <div class="updates-management">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.title"
          placeholder="动态标题"
          clearable
          style="width: 200px; margin-right: 10px"
          @keyup.enter="handleSearch"
        />
        <el-button type="default" style="background: #000; color: #fff" :loading="searchLoading" @click="handleSearch">
          查询
        </el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">
          + 发布动态
        </el-button>
      </div>
    </div>

    <!-- 动态表格 -->
    <el-table :data="tableData" style="width: 100%; margin-top: 20px" border>
      <el-table-column prop="title" label="标题" width="200" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <img v-if="row.picture" :src="row.picture" style="width:50px;height:50px;object-fit:cover;cursor:pointer;border-radius:4px" @click="previewSrc=row.picture;previewVisible=true" />
          <span v-else style="color:#ccc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="内容" min-width="300">
        <template #default="{ row }">
          <div class="content-cell">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="danger" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="updatesForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="updatesForm.title" placeholder="请输入动态标题" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="picture-upload"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="updatesForm.picture" :src="updatesForm.picture" class="upload-preview" />
            <el-button v-else type="primary">点击上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="updatesForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入动态内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <ImageViewer :visible="previewVisible" :src="previewSrc" @close="previewVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import ImageViewer from '@/components/ImageViewer.vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getUpdatesPage,
  addUpdates,
  updateUpdates,
  deleteUpdates,
  type Updates,
  type UpdatesDTO,
  type UpdatesPageQueryParams
} from '@/api/updates'

const searchForm = reactive({ title: '' })
const tableData = ref<Updates[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('发布动态')
const isEdit = ref(false)
const searchLoading = ref(false)
const deletingId = ref<number | undefined>(undefined)
const submitLoading = ref(false)

const previewVisible = ref(false)
const previewSrc = ref('')
const updatesForm = reactive<UpdatesDTO>({
  id: undefined,
  title: '',
  picture: '',
  content: ''
})

// 图片上传
const uploadAction = 'http://localhost:8080/admin/common/upload'
const uploadHeaders = { token: sessionStorage.getItem('token') || '' }

const handleUploadSuccess = (response: any) => {
  if (response.code === 1 && response.data) {
    updatesForm.picture = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const formRef = ref<FormInstance>()
const formRules: FormRules = {
  title: [{ required: true, message: '请输入动态标题', trigger: 'blur' }]
}

const loadList = async () => {
  try {
    const params: UpdatesPageQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined
    }
    const res: any = await getUpdatesPage(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载动态列表失败')
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

const handleAdd = () => {
  dialogTitle.value = '发布动态'
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: Updates) => {
  dialogTitle.value = '修改动态'
  isEdit.value = true
  updatesForm.id = row.id
  updatesForm.title = row.title
  updatesForm.picture = row.picture || ''
  updatesForm.content = row.content
  dialogVisible.value = true
}

const handleDelete = async (row: Updates) => {
  if (deletingId.value !== undefined) return
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm('确定要删除该动态吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteUpdates(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    deletingId.value = undefined
  }
}

const handleSubmit = async () => {
  if (!formRef.value || submitLoading.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res: any = isEdit.value
          ? await updateUpdates(updatesForm)
          : await addUpdates(updatesForm)
        if (res.code === 1) {
          ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
          dialogVisible.value = false
          loadList()
        } else {
          ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '发布失败'))
        }
      } catch (error: any) {
        ElMessage.error(error?.message || (isEdit.value ? '修改失败' : '发布失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  updatesForm.id = undefined
  updatesForm.title = ''
  updatesForm.picture = ''
  updatesForm.content = ''
}

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  loadList()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadList()
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.updates-management {
  padding: 0;
  background: transparent;
  min-height: 100%;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.search-left {
  display: flex;
  align-items: center;
}

.search-right {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-button--primary) {
  background-color: #1e3a8a;
  border-color: #1e3a8a;
}

:deep(.el-button--primary:hover) {
  background-color: #2563eb;
  border-color: #2563eb;
}

.content-cell {
  line-height: 1.6;
  word-break: break-all !important;
}
:deep(.el-textarea__inner) {
  word-break: break-all !important;
}
.upload-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style>
