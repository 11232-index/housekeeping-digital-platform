<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left">
        <button @click="toggleSidebar" class="toggle-btn">☰</button>
        <h1 class="title">智能家政服务</h1>
        <span class="shop-status-tag" :class="shopStatus === 1 ? 'open' : 'closed'">{{ shopStatus === 1 ? '🟢 营业中' : '🔴 打烊中' }}</span>
      </div>
      <div class="header-right">
        <div class="user-dropdown" @click.stop="toggleDropdown">
          <span class="user-name">{{ employeeFullName }}</span>
          <span class="dropdown-icon">▼</span>
        </div>
        <div v-show="showDropdown" class="dropdown-menu">
          <div class="dropdown-item" @click.stop="openPasswordDialog">
            <span>修改密码</span>
            <span class="item-icon">🔑</span>
          </div>
          <div class="dropdown-item" :disabled="loggingOut" @click.stop="handleLogout">
            <span>退出登录</span>
            <span class="item-icon">🚪</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-content">
      <!-- 左侧导航栏（按角色动态显示） -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div v-for="menu in visibleMenus" :key="menu.key"
             class="nav-item" :class="{ active: currentRoute === menu.key }"
             @click="navigateTo(menu.path)">
          <span class="nav-icon">{{ menu.icon }}</span>
          <span v-show="!sidebarCollapsed" class="nav-text">{{ menu.text }}</span>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <router-view v-if="currentRoute === 'category' || currentRoute === 'service-item' || currentRoute === 'service-package' || currentRoute === 'schedule' || currentRoute === 'voucher'" />
        <div v-else>
          <p>用户：{{ employeeName }}</p>
          <p>姓名：{{ employeeFullName }}</p>
          <p>员工ID：{{ employeeId }}</p>
          <p class="token-info">Token将在 {{ remainingTime }} 后过期</p>
        </div>
      </div>
    </div>

    <!-- 修改密码弹框 -->
    <transition name="modal">
      <div v-if="showPasswordDialog" class="modal-overlay">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>修改密码</h3>
            <button class="close-btn" @click="closePasswordDialog">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>原始密码：</label>
              <input
                type="password"
                v-model="passwordForm.oldPassword"
                placeholder="请输入原始密码"
              />
            </div>
            <div class="form-group">
              <label>新密码：</label>
              <input
                type="password"
                v-model="passwordForm.newPassword"
                placeholder="请输入新密码"
              />
            </div>
            <div class="form-group">
              <label>确认密码：</label>
              <input
                type="password"
                v-model="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
              />
            </div>
            <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-cancel" @click="closePasswordDialog">取消</button>
            <button class="btn btn-save" :disabled="savingPassword" @click="handleSavePassword">保存</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeLogout, employeeEditPassword } from '@/api/employee-auth'
import { getShopStatus } from '@/api/shop'

const router = useRouter()
const route = useRoute()
const employeeName = ref('')
const employeeFullName = ref('')
const employeeId = ref('')
const remainingTime = ref('')
const showDropdown = ref(false)
const showPasswordDialog = ref(false)
const sidebarCollapsed = ref(false)
const errorMsg = ref('')
const loggingOut = ref(false)
const savingPassword = ref(false)
const shopStatus = ref<number>(1)

let timer: number | null = null

// 当前员工角色
const employeeRole = ref(sessionStorage.getItem('employeeRole') || 'staff')

// 全部菜单定义（含角色限制）
const allMenus = [
  { key: 'workbench',        text: '工作台',   icon: '🏠', path: '/employee/home',            roles: ['admin', 'manager', 'staff', 'cleaner'] },
  { key: 'category',         text: '分类管理', icon: '📂', path: '/employee/category',        roles: ['admin', 'manager'] },
  { key: 'service-item',     text: '服务管理', icon: '🛠', path: '/employee/service-item',    roles: ['admin', 'manager'] },
  { key: 'service-package',  text: '套餐管理', icon: '📦', path: '/employee/service-package', roles: ['admin', 'manager'] },
  { key: 'schedule',         text: '预约管理', icon: '📅', path: '/employee/housekeeper-schedule', roles: ['admin', 'manager'] },
  { key: 'voucher',          text: '优惠券管理', icon: '🎟️', path: '/employee/voucher', roles: ['admin', 'manager'] },
]

// 按角色过滤可见菜单
const visibleMenus = computed(() => allMenus.filter(m => m.roles.includes(employeeRole.value)))

// 当前路由
const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('/employee/category')) return 'category'
  if (path.includes('/employee/service-item')) return 'service-item'
  if (path.includes('/employee/service-package')) return 'service-package'
  if (path.includes('/employee/housekeeper-schedule')) return 'schedule'
  if (path.includes('/employee/voucher')) return 'voucher'
  return 'workbench'
})

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 切换下拉菜单
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 打开修改密码弹框
const openPasswordDialog = () => {
  showDropdown.value = false
  showPasswordDialog.value = true
  errorMsg.value = ''
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 关闭修改密码弹框
const closePasswordDialog = () => {
  showPasswordDialog.value = false
  errorMsg.value = ''
}

// 保存密码
const handleSavePassword = async () => {
  errorMsg.value = ''

  if (!passwordForm.value.oldPassword) {
    errorMsg.value = '请输入原始密码'
    return
  }
  if (!passwordForm.value.newPassword) {
    errorMsg.value = '请输入新密码'
    return
  }
  if (!passwordForm.value.confirmPassword) {
    errorMsg.value = '请输入确认密码'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    errorMsg.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    errorMsg.value = '新密码长度不能少于6位'
    return
  }

  if (savingPassword.value) return
  savingPassword.value = true
  try {
    await employeeEditPassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    handleLogout()
  } catch (error: any) {
    errorMsg.value = error.message || '修改密码失败'
  } finally {
    savingPassword.value = false
  }
}

// 计算剩余时间
const calculateRemainingTime = () => {
  const tokenExpireTime = sessionStorage.getItem('employeeTokenExpireTime')
  if (tokenExpireTime) {
    const expireTime = parseInt(tokenExpireTime)
    const currentTime = Date.now()
    const diff = expireTime - currentTime
    if (diff <= 0) {
      remainingTime.value = '已过期'
      clearTimer()
      handleLogout()
    } else {
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      remainingTime.value = `${hours}小时${minutes}分${seconds}秒`
    }
  } else {
    remainingTime.value = '未知'
  }
}

// 清除定时器
const clearTimer = () => {
  if (timer !== null) {
    clearInterval(timer)
    timer = null
  }
}

// 退出登录
const handleLogout = async () => {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await employeeLogout()
  } catch (error) {
    console.error('退出登录失败:', error)
  } finally {
    sessionStorage.removeItem('employeeToken')
    sessionStorage.removeItem('employeeTokenExpireTime')
    sessionStorage.removeItem('employeeName')
    sessionStorage.removeItem('employeeFullName')
    sessionStorage.removeItem('employeeId')
    sessionStorage.removeItem('employeeRole')
    clearTimer()
    router.push('/employee/login')
  }
}

onMounted(() => {
  employeeName.value = sessionStorage.getItem('employeeName') || '未知用户'
  employeeFullName.value = sessionStorage.getItem('employeeFullName') || '未知用户'
  employeeId.value = sessionStorage.getItem('employeeId') || '未知'
  employeeRole.value = sessionStorage.getItem('employeeRole') || 'staff'

  calculateRemainingTime()
  timer = setInterval(calculateRemainingTime, 1000) as unknown as number

  // 获取店铺营业状态
  getShopStatus().then((res: any) => {
    if (res.code === 1 && res.data !== undefined) {
      shopStatus.value = res.data
    }
  }).catch(() => {})

  document.addEventListener('click', () => {
    showDropdown.value = false
  })
})

onUnmounted(() => {
  clearTimer()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f0f2f5;
}

/* 顶部导航栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.toggle-btn:hover {
  background: white;
  transform: scale(1.05);
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #1976d2;
  margin: 0;
}

.header-right {
  position: relative;
}

/* 营业状态标签（只读） */
.shop-status-tag {
  font-size: 13px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
}
.shop-status-tag.open {
  background: #e8f5e9;
  color: #2e7d32;
}
.shop-status-tag.closed {
  background: #fbe9e7;
  color: #bf360c;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 10px;
  color: #666;
  transition: transform 0.3s;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  min-width: 160px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
  color: #333;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.item-icon {
  font-size: 16px;
}

/* 主体内容区 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧导航栏 */
.sidebar {
  width: 200px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #1976d2;
}

.nav-item.active {
  background: #e3f2fd;
  color: #1976d2;
  border-right: 3px solid #1976d2;
}

.nav-icon {
  font-size: 20px;
  min-width: 24px;
  text-align: center;
}

.nav-text {
  white-space: nowrap;
  overflow: hidden;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

p {
  color: #666;
  font-size: 16px;
  margin: 10px 0;
}

.token-info {
  color: #999;
  font-size: 14px;
  margin-top: 20px;
}

/* 弹框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: modalSlideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.1);
}

.error-message {
  margin-top: 16px;
  padding: 10px 12px;
  background: #fff1f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  color: #cf1322;
  font-size: 13px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-save {
  background: #1976d2;
  color: white;
}

.btn-save:hover {
  background: #1565c0;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.3);
}

/* 弹框过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-content,
.modal-leave-active .modal-content {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: translateY(-50px) scale(0.9);
}

</style>
