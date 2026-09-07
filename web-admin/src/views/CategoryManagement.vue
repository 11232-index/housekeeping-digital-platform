<template>
  <div class="category-management">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.name"
          placeholder="请输入分类名称"
          clearable
          style="width: 200px; margin-right: 10px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.type"
          placeholder="服务分类类型"
          clearable
          style="width: 160px; margin-right: 10px"
        >
          <el-option label="全部类型" :value="undefined" />
          <el-option label="服务分类" :value="1" />
          <el-option label="服务套餐分类" :value="2" />
        </el-select>
        <el-button type="default" style="background: #000; color: #fff" :loading="searchLoading" @click="handleSearch">
          查询
        </el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">
          + 新增分类
        </el-button>
      </div>
    </div>

    <!-- 分类表格 -->
    <el-table :data="tableData" style="width: 100%; margin-top: 20px" border>
      <el-table-column prop="name" label="分类名称" min-width="180" />
      <el-table-column label="服务分类类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? '' : 'success'" size="small">
            {{ row.type === 1 ? '服务分类' : '服务套餐分类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="status-indicator">
            <span :class="['status-dot', row.status === 1 ? 'active' : 'inactive']"></span>
            {{ row.status === 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="操作时间" width="180" />
      <el-table-column label="操作" fixed="right" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="danger" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            :loading="togglingId === row.id"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="服务分类类型" prop="type">
          <el-radio-group v-model="categoryForm.type" :disabled="isEdit">
            <el-radio :value="1">服务分类</el-radio>
            <el-radio :value="2">服务套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number
            v-model="categoryForm.sort"
            :min="0"
            :max="999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
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
import {
  getCategoryPage,
  addCategory,
  updateCategory,
  updateCategoryStatus,
  deleteCategory,
  CATEGORY_TYPE_MAP,
  type Category,
  type CategoryDTO,
  type CategoryPageQueryParams
} from '@/api/category'

// 搜索表单
const searchForm = reactive({
  name: '',
  type: undefined as number | undefined
})

// 加载状态
const searchLoading = ref(false)
const deletingId = ref<number | undefined>(undefined)
const togglingId = ref<number | undefined>(undefined)
const submitLoading = ref(false)

// 表格数据
const tableData = ref<Category[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加服务分类')
const isEdit = ref(false)

// 分类表单
const categoryForm = reactive<CategoryDTO>({
  id: undefined,
  name: '',
  type: 1,
  sort: 0
})

// 表单引用
const formRef = ref<FormInstance>()

// 表单验证规则
const formRules: FormRules = {
  type: [{ required: true, message: '请选择服务分类类型', trigger: 'change' }],
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在1-50个字符', trigger: 'blur' }
  ]
}

// 加载分类列表
const loadCategoryList = async () => {
  try {
    const params: CategoryPageQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      type: searchForm.type
    }
    const res: any = await getCategoryPage(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  }
}

// 搜索
const handleSearch = async () => {
  searchLoading.value = true
  try {
    pagination.page = 1
    await loadCategoryList()
  } finally {
    searchLoading.value = false
  }
}

// 添加分类
const handleAdd = () => {
  dialogTitle.value = '添加分类'
  isEdit.value = false
  categoryForm.type = 1
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row: Category) => {
  dialogTitle.value = '编辑分类'
  isEdit.value = true
  categoryForm.id = row.id
  categoryForm.name = row.name
  categoryForm.type = row.type
  categoryForm.sort = row.sort
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (row: Category) => {
  if (deletingId.value !== undefined) return
  const typeLabel = CATEGORY_TYPE_MAP[row.type] || '该'
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm(`确定要删除${typeLabel}「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await deleteCategory(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadCategoryList()
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

// 启用/禁用分类
const handleToggleStatus = async (row: Category) => {
  if (togglingId.value !== undefined) return
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  togglingId.value = row.id
  try {
    await ElMessageBox.confirm(`确定要${action}该分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await updateCategoryStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${action}成功`)
      loadCategoryList()
    } else {
      ElMessage.error(res.msg || `${action}失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  } finally {
    togglingId.value = undefined
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value || submitLoading.value) return
  submitLoading.value = true

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res: any = isEdit.value
          ? await updateCategory(categoryForm)
          : await addCategory(categoryForm)

        if (res.code === 1) {
          ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
          dialogVisible.value = false
          loadCategoryList()
        } else {
          ElMessage.error(res.msg || (isEdit.value ? '编辑失败' : '添加失败'))
        }
      } catch (error: any) {
        ElMessage.error(error?.message || (isEdit.value ? '编辑失败' : '添加失败'))
      }
    }
  })

  submitLoading.value = false
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  categoryForm.id = undefined
  categoryForm.name = ''
  categoryForm.type = 1
  categoryForm.sort = 0
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  loadCategoryList()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadCategoryList()
}

// 页面加载时获取数据
onMounted(() => {
  loadCategoryList()
})
</script>

<style scoped>
.category-management {
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
  flex-wrap: wrap;
  gap: 12px;
}

.search-left {
  display: flex;
  align-items: center;
}

.search-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.active {
  background-color: #67c23a;
}

.status-dot.inactive {
  background-color: #909399;
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

:deep(.el-button--success) {
  background-color: #16a34a;
  border-color: #16a34a;
}

:deep(.el-button--success:hover) {
  background-color: #22c55e;
  border-color: #22c55e;
}
</style>
