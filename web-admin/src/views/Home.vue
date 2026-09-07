<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="header-left">
        <button @click="toggleSidebar" class="toggle-btn">☰</button>
        <h1 class="title">智能家政服务</h1>
        <div class="shop-status-trigger" @click.stop="toggleShopDropdown">
          <span class="shop-icon">🏪</span>
          <span class="shop-label" :class="shopStatus === 1 ? 'open' : 'closed'">{{ shopStatus === 1 ? '营业中' : '打烊中' }}</span>
          <span class="dropdown-icon">▼</span>
        </div>
        <div v-show="showShopDropdown" class="shop-dropdown-menu" @click.stop>
          <div class="shop-dropdown-item" :class="{ active: shopStatus === 1 }" @click.stop="changeShopStatus(1)">
            <div class="shop-option-header">
              <span class="shop-option-icon">🟢</span>
              <span class="shop-option-title">营业中</span>
            </div>
            <p class="shop-option-desc">店铺正常营业，客户可下单预约服务</p>
          </div>
          <div class="shop-dropdown-item" :class="{ active: shopStatus === 0 }" @click.stop="changeShopStatus(0)">
            <div class="shop-option-header">
              <span class="shop-option-icon">🔴</span>
              <span class="shop-option-title">打烊中</span>
            </div>
            <p class="shop-option-desc">店铺已打烊，暂停接收新订单</p>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="user-dropdown" @click.stop="toggleDropdown">
          <span class="user-name">{{ displayName }}</span>
          <span class="dropdown-icon">▼</span>
        </div>
        <div v-show="showDropdown" class="dropdown-menu">
          <div class="dropdown-item" @click.stop="openNameDialog">
            <span>修改名字</span>
            <span class="item-icon">✏️</span>
          </div>
          <div class="dropdown-item" @click.stop="openPasswordDialog">
            <span>修改密码</span>
            <span class="item-icon">🔑</span>
          </div>
          <div class="dropdown-item" @click.stop="openSecurityDialog">
            <span>设置密保</span>
            <span class="item-icon">🛡️</span>
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
      <!-- 左侧导航栏 -->
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="nav-item" :class="{ active: currentRoute === 'employee' }" @click="navigateTo('/employee')">
          <span class="nav-icon">👥</span>
          <span v-show="!sidebarCollapsed" class="nav-text">员工管理</span>
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'housekeeper' }" @click="navigateTo('/housekeeper')">
          <span class="nav-icon">🧹</span>
          <span v-show="!sidebarCollapsed" class="nav-text">家政员管理</span>
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'updates' }" @click="navigateTo('/updates')">
          <span class="nav-icon">📢</span>
          <span v-show="!sidebarCollapsed" class="nav-text">动态管理</span>
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'service-intro' }" @click="navigateTo('/service-intro')">
          <span class="nav-icon">🛎️</span>
          <span v-show="!sidebarCollapsed" class="nav-text">服务介绍管理</span>
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'hot-picks' }" @click="navigateTo('/hot-picks')">
          <span class="nav-icon">⭐</span>
          <span v-show="!sidebarCollapsed" class="nav-text">服务推荐管理</span>
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'about' }" @click="navigateTo('/about')">
          <span class="nav-icon">ℹ️</span>
          <span v-show="!sidebarCollapsed" class="nav-text">关于我们</span>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <router-view v-if="currentRoute !== 'home'" />
        <div v-else>
          <p>用户：{{ userName }}</p>
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

    <!-- 修改名字弹框 -->
    <transition name="modal">
      <div v-if="showNameDialog" class="modal-overlay">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>修改名字</h3>
            <button class="close-btn" @click="closeNameDialog">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>新名字：</label>
              <input
                type="text"
                v-model="nameForm.newName"
                placeholder="请输入新名字"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-cancel" @click="closeNameDialog">取消</button>
            <button class="btn btn-save" :disabled="savingName" @click="handleSaveName">保存</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 设置密保弹框 -->
    <transition name="modal">
      <div v-if="showSecurityDialog" class="modal-overlay">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>设置密保</h3>
            <button class="close-btn" @click="closeSecurityDialog">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>密保问题：</label>
              <input
                type="text"
                v-model="securityForm.question"
                placeholder="如：我的小学名称是？"
              />
            </div>
            <div class="form-group">
              <label>密保答案：</label>
              <input
                type="text"
                v-model="securityForm.answer"
                placeholder="请输入答案"
              />
            </div>
            <div v-if="securityErrorMsg" class="error-message">{{ securityErrorMsg }}</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-cancel" @click="closeSecurityDialog">取消</button>
            <button class="btn btn-save" :disabled="savingSecurity" @click="handleSaveSecurity">保存</button>
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
import { logout, getEmployeeById, editPassword, editName, setSecurity } from '@/api/employee'
import { getShopStatus, setShopStatus } from '@/api/shop'

const router = useRouter()
const route = useRoute()
const userName = ref('')
const displayName = ref('')
const remainingTime = ref('')
const showDropdown = ref(false)
const showPasswordDialog = ref(false)
const showNameDialog = ref(false)
const showSecurityDialog = ref(false)
const sidebarCollapsed = ref(false)
const shopStatus = ref<number>(1)
const showShopDropdown = ref(false)
const errorMsg = ref('')
const securityErrorMsg = ref('')
const loggingOut = ref(false)
const savingPassword = ref(false)
const savingName = ref(false)
const savingSecurity = ref(false)
const currentUserId = ref<number>(0)
const originalPassword = ref('')

let timer: number | null = null

// 当前路由
const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('/employee')) return 'employee'
  if (path.includes('/updates')) return 'updates'
  if (path.includes('/service-intro')) return 'service-intro'
  if (path.includes('/hot-picks')) return 'hot-picks'
  if (path.includes('/housekeeper')) return 'housekeeper'
  if (path.includes('/about')) return 'about'
  return 'home'
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 名字表单
const nameForm = ref({
  newName: ''
})

// 密保表单
const securityForm = ref({
  question: '',
  answer: ''
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 切换营业状态下拉
const toggleShopDropdown = () => {
  showShopDropdown.value = !showShopDropdown.value
  showDropdown.value = false
}

// 切换营业状态
const changeShopStatus = async (status: number) => {
  try {
    const res: any = await setShopStatus(status)
    if (res.code === 1) {
      shopStatus.value = status
      showShopDropdown.value = false
      ElMessage.success(status === 1 ? '已切换为营业中' : '已切换为打烊中')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '切换营业状态失败')
  }
}

// 导航到指定路由
const navigateTo = (path: string) => {
  router.push(path)
}

// 切换下拉菜单
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 打开修改密码弹框
const openPasswordDialog = async () => {
  showDropdown.value = false
  showPasswordDialog.value = true
  errorMsg.value = ''

  // 重置表单
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }

  // 查询当前用户信息获取原密码
  try {
    const result: any = await getEmployeeById(currentUserId.value)
    if (result.code === 1 && result.data) {
      originalPassword.value = result.data.password
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 关闭修改密码弹框
const closePasswordDialog = () => {
  showPasswordDialog.value = false
  errorMsg.value = ''
}

// 打开修改名字弹框
const openNameDialog = () => {
  showDropdown.value = false
  showNameDialog.value = true

  // 重置表单
  nameForm.value = {
    newName: ''
  }
}

// 关闭修改名字弹框
const closeNameDialog = () => {
  showNameDialog.value = false
}

// 保存名字
const handleSaveName = async () => {
  if (savingName.value) return

  // 验证表单
  if (!nameForm.value.newName || !nameForm.value.newName.trim()) {
    ElMessage.error('请输入新名字')
    return
  }

  savingName.value = true
  try {
    // 发送修改名字请求
    await editName(nameForm.value.newName.trim())

    // 更新本地存储和页面显示
    sessionStorage.setItem('name', nameForm.value.newName.trim())
    displayName.value = nameForm.value.newName.trim()

    closeNameDialog()
    ElMessage.success('名字修改成功')
  } catch (error: any) {
    ElMessage.error(error.message || '修改名字失败')
  } finally {
    savingName.value = false
  }
}

// 保存密码
const handleSavePassword = async () => {
  if (savingPassword.value) return
  errorMsg.value = ''

  // 验证表单
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

  savingPassword.value = true
  try {
    // 发送修改密码请求
    await editPassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')
    handleLogout()
  } catch (error: any) {
    errorMsg.value = error.message || '修改密码失败'
    console.error('修改密码失败:', error)
  } finally {
    savingPassword.value = false
  }
}

// 打开设置密保弹框
const openSecurityDialog = () => {
  showDropdown.value = false
  showSecurityDialog.value = true
  securityErrorMsg.value = ''
  securityForm.value = { question: '', answer: '' }
}

// 关闭设置密保弹框
const closeSecurityDialog = () => {
  showSecurityDialog.value = false
  securityErrorMsg.value = ''
}

// 保存密保
const handleSaveSecurity = async () => {
  if (savingSecurity.value) return
  securityErrorMsg.value = ''
  if (!securityForm.value.question.trim()) {
    securityErrorMsg.value = '请输入密保问题'
    return
  }
  if (!securityForm.value.answer.trim()) {
    securityErrorMsg.value = '请输入密保答案'
    return
  }
  savingSecurity.value = true
  try {
    await setSecurity(securityForm.value.question.trim(), securityForm.value.answer.trim())
    closeSecurityDialog()
    ElMessage.success('密保设置成功')
  } catch (error: any) {
    securityErrorMsg.value = error.message || '密保设置失败'
  } finally {
    savingSecurity.value = false
  }
}

// 计算剩余时间
const calculateRemainingTime = () => {
  const tokenExpireTime = sessionStorage.getItem('tokenExpireTime')

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
    await logout()
  } catch (error) {
    console.error('退出登录失败:', error)
  } finally {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userName')
    sessionStorage.removeItem('tokenExpireTime')
    sessionStorage.removeItem('userId')
    clearTimer()
    router.push('/login')
    loggingOut.value = false
  }
}

onMounted(() => {
  userName.value = sessionStorage.getItem('userName') || '未知用户'
  displayName.value = sessionStorage.getItem('name') || '未知用户'
  const userId = sessionStorage.getItem('userId')
  if (userId) {
    currentUserId.value = parseInt(userId)
  }

  calculateRemainingTime()
  timer = setInterval(calculateRemainingTime, 1000) as unknown as number

  // 获取店铺营业状态
  getShopStatus().then((res: any) => {
    if (res.code === 1 && res.data !== undefined) {
      shopStatus.value = res.data
    }
  }).catch(() => {})

  // 点击其他地方关闭下拉菜单
  document.addEventListener('click', () => {
    showDropdown.value = false
    showShopDropdown.value = false
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
  z-index: 500;
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

/* 营业状态切换按钮 */
.shop-status-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
  user-select: none;
  position: relative;
}
.shop-status-trigger:hover {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.shop-icon { font-size: 16px; }
.shop-label { font-weight: 500; }
.shop-label.open { color: #2e7d32; }
.shop-label.closed { color: #bf360c; }

/* 营业状态下拉菜单 */
.shop-dropdown-menu {
  position: absolute;
  top: 56px;
  left: 180px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  min-width: 260px;
  animation: slideDown 0.3s ease;
  z-index: 2000;
}
.shop-dropdown-item {
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}
.shop-dropdown-item:last-child { border-bottom: none; }
.shop-dropdown-item:hover { background: #f5f5f5; }
.shop-dropdown-item.active { background: #e3f2fd; }
.shop-option-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.shop-option-icon { font-size: 14px; }
.shop-option-title { font-size: 14px; font-weight: 600; color: #333; }
.shop-option-desc {
  margin: 0;
  padding-left: 22px;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}

.header-right {
  position: relative;
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
  position: relative;
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
  z-index: 2000;
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
