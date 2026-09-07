<template>
  <div class="employee-management">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.name"
          placeholder="员工姓名"
          clearable
          style="width: 200px; margin-right: 10px"
          @keyup.enter="handleSearch"
        />
        <el-button type="default" style="background: #000; color: #fff" :loading="searchLoading" @click="handleSearch">
          查询
        </el-button>
        <el-button type="primary" style="margin-left: 12px" @click="handleOpenChat">
          🤖 智能问答AI助手
        </el-button>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="handleAdd">
          + 添加员工
        </el-button>
      </div>
    </div>

    <!-- 员工表格 -->
    <el-table :data="tableData" style="width: 100%; margin-top: 20px" border>
      <el-table-column prop="name" label="员工姓名" width="120" />
      <el-table-column prop="username" label="账号" width="150" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">
          {{ row.sex === '1' ? '男' : '女' }}
        </template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="idNumber" label="身份证号" width="180" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : row.role === 'manager' ? 'warning' : 'info'" size="small">
            {{ roleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="账号状态" width="120">
        <template #default="{ row }">
          <span class="status-indicator">
            <span :class="['status-dot', row.status === 1 ? 'active' : 'inactive']"></span>
            {{ row.status === 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="最后操作时间" width="180" />
      <el-table-column label="操作" fixed="right" width="360">
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
          <el-button link type="success" @click="handleViewDetail(row)">查看详细信息</el-button>
          <el-button link type="primary" @click="handleViewSummary(row)">智能总结</el-button>
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

    <!-- 添加/编辑员工对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="employeeForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="employeeForm.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="employeeForm.username"
            placeholder="请输入账号"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="employeeForm.password"
            type="password"
            placeholder="请输入密码，留空则默认change-me-password"
            show-password
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="employeeForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="employeeForm.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input v-model="employeeForm.age" placeholder="请输入年龄" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="employeeForm.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="employeeForm.role" placeholder="请选择角色" style="width:100%">
            <el-option label="管理员" value="admin" />
            <el-option label="经理" value="manager" />
            <el-option label="普通员工" value="staff" />
            <el-option label="家政员" value="cleaner" />
          </el-select>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getEmployeePage,
  addEmployee,
  updateEmployee,
  updateEmployeeStatus,
  deleteEmployee,
  type Employee,
  type EmployeeDTO,
  type EmployeePageQueryParams
} from '@/api/employee'

// 搜索表单
const searchForm = reactive({
  name: ''
})

// 表格数据
const tableData = ref<Employee[]>([])

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加员工')
const isEdit = ref(false)

// 员工表单
const employeeForm = reactive<EmployeeDTO>({
  id: undefined,
  username: '',
  password: '',
  name: '',
  phone: '',
  sex: '1',
  age: '',
  idNumber: '',
  role: 'staff'
})

// 角色中英文映射
const roleMap: Record<string, string> = {
  admin: '管理员',
  manager: '经理',
  staff: '普通员工',
  cleaner: '家政员'
}
const roleLabel = (role: string) => roleMap[role] || role || '普通员工'

// 路由
const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 按钮 loading 状态
const searchLoading = ref(false)
const deletingId = ref<number | null>(null)
const togglingId = ref<number | null>(null)
const submitLoading = ref(false)

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
}

// 加载员工列表
const loadEmployeeList = async () => {
  try {
    const params: EmployeePageQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined
    }
    const res: any = await getEmployeePage(params)
    if (res.code === 1) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载员工列表失败')
  }
}

// 搜索
const handleSearch = async () => {
  pagination.page = 1
  searchLoading.value = true
  try {
    await loadEmployeeList()
  } finally {
    searchLoading.value = false
  }
}

// 添加员工
const handleAdd = () => {
  dialogTitle.value = '添加员工'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑员工
const handleEdit = (row: Employee) => {
  dialogTitle.value = '编辑员工'
  isEdit.value = true
  employeeForm.id = row.id
  employeeForm.username = row.username
  employeeForm.password = ''
  employeeForm.name = row.name
  employeeForm.phone = row.phone
  employeeForm.sex = row.sex
  employeeForm.age = row.age || ''
  employeeForm.idNumber = row.idNumber
  employeeForm.role = row.role || 'staff'
  dialogVisible.value = true
}

// 查看详细信息
const handleViewDetail = (row: Employee) => {
  router.push({ path: '/employee/detail', query: { id: row.id } })
}

// 查看智能总结
const handleViewSummary = (row: Employee) => {
  router.push({ path: '/employee/summary', query: { id: row.id } })
}

const handleOpenChat = () => {
  router.push('/employee/chat')
}

// 删除员工
const handleDelete = async (row: Employee) => {
  if (deletingId.value) return
  deletingId.value = row.id
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await deleteEmployee(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadEmployeeList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    deletingId.value = null
  }
}

// 启用/禁用员工
const handleToggleStatus = async (row: Employee) => {
  if (togglingId.value) return
  togglingId.value = row.id
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${action}该员工吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res: any = await updateEmployeeStatus(newStatus, row.id)
    if (res.code === 1) {
      ElMessage.success(`${action}成功`)
      loadEmployeeList()
    } else {
      ElMessage.error(res.msg || `${action}失败`)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  } finally {
    togglingId.value = null
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  if (submitLoading.value) return
  submitLoading.value = true
  try {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const res: any = isEdit.value
            ? await updateEmployee(employeeForm)
            : await addEmployee(employeeForm)

          if (res.code === 1) {
            ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
            dialogVisible.value = false
            loadEmployeeList()
          } else {
            ElMessage.error(res.msg || (isEdit.value ? '编辑失败' : '添加失败'))
          }
        } catch (error: any) {
          ElMessage.error(error?.message || (isEdit.value ? '编辑失败' : '添加失败'))
        }
      }
    })
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  employeeForm.id = undefined
  employeeForm.username = ''
  employeeForm.password = ''
  employeeForm.name = ''
  employeeForm.phone = ''
  employeeForm.sex = '1'
  employeeForm.age = ''
  employeeForm.idNumber = ''
  employeeForm.role = 'staff'
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.page = 1
  loadEmployeeList()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadEmployeeList()
}

// 页面加载时获取数据
onMounted(() => {
  loadEmployeeList()
})
</script>

<style scoped>
.employee-management {
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

.status-indicator {
  display: flex;
  align-items: center;
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
</style>
