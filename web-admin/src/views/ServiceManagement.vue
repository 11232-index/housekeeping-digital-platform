<template>
  <div class="service-management">
    <!-- 搜索/筛选栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.name"
          placeholder="请填写服务名称"
          clearable
          style="width: 200px; margin-right: 10px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.categoryId"
          placeholder="服务分类"
          clearable
          style="width: 180px; margin-right: 10px"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="searchForm.status"
          placeholder="服务状态"
          clearable
          style="width: 150px; margin-right: 10px"
        >
          <el-option
            v-for="item in SERVICE_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="default" style="background: #000; color: #fff" @click="handleSearch" :loading="searchLoading">
          查询
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <div class="search-right">
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0" :loading="batchDeleteLoading">
          批量删除
        </el-button>
        <el-button type="primary" @click="handleAdd">
          + 新增服务
        </el-button>
      </div>
    </div>

    <!-- 服务项目表格 -->
    <el-table
      :data="tableData"
      style="width: 100%; margin-top: 20px"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column prop="name" label="服务名称" min-width="150" />
      <el-table-column label="服务分类" width="120">
        <template #default="{ row }">
          {{ row.categoryName || '--' }}
        </template>
      </el-table-column>
      <el-table-column label="服务价格" width="120" align="right">
        <template #default="{ row }">
          ¥{{ row.price ? row.price.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="服务图片" width="100" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.image"
            :src="row.image"
            :preview-src-list="[row.image]"
            fit="cover"
            preview-teleported
            style="width: 50px; height: 50px; border-radius: 4px"
          />
          <span v-else class="no-image">--</span>
        </template>
      </el-table-column>
      <el-table-column label="服务状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ SERVICE_STATUS_MAP[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="服务描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="操作时间" width="180" />
      <el-table-column label="操作" fixed="right" width="260">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="danger" @click="handleDelete(row)" :loading="deletingId === row.id">删除</el-button>
          <el-dropdown
            trigger="click"
            @command="(cmd: number) => handleStatusChange(row, cmd)"
            class="status-dropdown"
          >
            <el-button link :type="getStatusTagType(row.status)" :disabled="statusChangingId === row.id" :loading="statusChangingId === row.id">
              {{ SERVICE_STATUS_MAP[row.status] }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in SERVICE_STATUS_OPTIONS"
                  :key="item.value"
                  :command="item.value"
                  :disabled="item.value === row.status"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  getServiceItemPage,
  deleteServiceItem,
  batchDeleteServiceItem,
  updateServiceItemStatus,
  SERVICE_STATUS_MAP,
  SERVICE_STATUS_OPTIONS,
  type ServiceItem,
  type ServiceItemPageQueryParams
} from '@/api/service-item'
import { getCategoryList, type Category } from '@/api/category'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 分类下拉选项
const categoryOptions = ref<Category[]>([])

// 表格数据
const tableData = ref<ServiceItem[]>([])
const selectedIds = ref<number[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 按钮 loading 状态
const searchLoading = ref(false)
const batchDeleteLoading = ref(false)
const deletingId = ref<number | null>(null)
const statusChangingId = ref<number | null>(null)

// 加载分类下拉列表
const loadCategoryOptions = async () => {
  try {
    const res: any = await getCategoryList(1) // type=1 服务分类
    if (res.code === 1) {
      categoryOptions.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

// 加载服务项目列表
const loadServiceList = async () => {
  try {
    const params: ServiceItemPageQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId,
      status: searchForm.status
    }
    const res: any = await getServiceItemPage(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载服务列表失败')
  }
}

// 搜索
const handleSearch = async () => {
  searchLoading.value = true
  try {
    pagination.page = 1
    await loadServiceList()
  } finally {
    searchLoading.value = false
  }
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = undefined
  searchForm.status = undefined
  pagination.page = 1
  loadServiceList()
}

// 新增
const handleAdd = () => {
  router.push({ path: '/employee/service-item/add' })
}

// 编辑
const handleEdit = (row: ServiceItem) => {
  router.push({ path: '/employee/service-item/edit', query: { id: row.id } })
}

// 表格多选
const handleSelectionChange = (selection: ServiceItem[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 单个删除
const handleDelete = async (row: ServiceItem) => {
  if (deletingId.value !== null) return
  try {
    await ElMessageBox.confirm(`确定要删除服务「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    deletingId.value = row.id
    await deleteServiceItem(row.id)
    ElMessage.success('删除成功')
    loadServiceList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除失败')
    }
  } finally {
    deletingId.value = null
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (batchDeleteLoading.value) return
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一项')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 项服务吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    batchDeleteLoading.value = true
    await batchDeleteServiceItem(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadServiceList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '批量删除失败')
    }
  } finally {
    batchDeleteLoading.value = false
  }
}

// 切换状态（下拉选择任意状态）
const handleStatusChange = async (row: ServiceItem, newStatus: number) => {
  if (newStatus === row.status) return
  if (statusChangingId.value !== null) return

  const statusLabel = SERVICE_STATUS_MAP[newStatus] || '未知'

  try {
    await ElMessageBox.confirm(`确定要将「${row.name}」设为「${statusLabel}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    statusChangingId.value = row.id
    const res: any = await updateServiceItemStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success('状态更新成功')
      loadServiceList()
    } else {
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('状态更新失败')
    }
  } finally {
    statusChangingId.value = null
  }
}

// 获取状态标签类型
const getStatusTagType = (status: number): string => {
  switch (status) {
    case 0: return 'warning'    // 休息中 - 橙色
    case 1: return 'success'    // 可预约 - 绿色
    default: return 'info'
  }
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  loadServiceList()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadServiceList()
}

onMounted(() => {
  loadCategoryOptions()
  loadServiceList()
})
</script>

<style scoped>
.service-management {
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
  flex-wrap: wrap;
  gap: 10px;
}

.search-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.no-image {
  color: #c0c4cc;
  font-size: 12px;
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

:deep(.el-button--danger) {
  background-color: #dc2626;
  border-color: #dc2626;
}

:deep(.el-button--danger:hover) {
  background-color: #ef4444;
  border-color: #ef4444;
}

/* 状态下拉框与相邻按钮对齐 */
.status-dropdown {
  margin-left: 12px;
  vertical-align: middle;
}
</style>
