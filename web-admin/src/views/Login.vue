<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <img src="../assets/login-bg.jpg" alt="登录背景" class="login-image" />
      </div>
      <div class="login-right">
        <div class="login-box">
          <h1 class="login-title">智能家政服务</h1>
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <label>用户名</label>
              <input
                type="text"
                id="username"
                v-model="loginForm.username"
                placeholder="请输入用户名"
                required
              />
            </div>
            <div class="form-group">
              <label>密码</label>
              <div class="password-input-wrapper">
                <input
                  :type="showPassword ? 'text' : 'password'"
                  id="password"
                  v-model="loginForm.password"
                  placeholder="请输入密码"
                  required
                  @focus="passwordFocused = true"
                  @blur="passwordFocused = false"
                />
                <span
                  v-show="passwordFocused || loginForm.password"
                  class="password-toggle"
                  @mousedown.prevent
                  @click="togglePassword"
                >
                  {{ showPassword ? '🙈' : '👁' }}
                </span>
              </div>
            </div>
            <button type="submit" class="login-button" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
            <div class="login-footer">
              <a href="#" @click.prevent="forgotPassword" class="footer-link">忘记密码？</a>
            </div>
            <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          </form>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗 -->
    <transition name="modal">
      <div v-if="showForgotDialog" class="modal-overlay">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>找回密码</h3>
            <button class="close-btn" @click="showForgotDialog = false">×</button>
          </div>
          <div class="modal-body">
            <!-- 步骤1：输入用户名 -->
            <template v-if="forgotStep === 1">
              <div class="form-group">
                <label>请输入用户名：</label>
                <input
                  type="text"
                  v-model="forgotForm.username"
                  placeholder="请输入管理员用户名"
                  @keyup.enter="handleForgotNext"
                />
              </div>
            </template>
            <!-- 步骤2：密保问题 + 新密码 -->
            <template v-if="forgotStep === 2">
              <div class="form-group">
                <label>密保问题：</label>
                <p class="security-question">{{ forgotForm.question }}</p>
              </div>
              <div class="form-group">
                <label>密保答案：</label>
                <input
                  type="text"
                  v-model="forgotForm.answer"
                  placeholder="请输入密保答案"
                />
              </div>
              <div class="form-group">
                <label>新密码：</label>
                <input
                  type="password"
                  v-model="forgotForm.newPassword"
                  placeholder="请输入新密码（至少6位）"
                  @keyup.enter="handleForgotReset"
                />
              </div>
            </template>
            <div v-if="forgotError" class="error-message">{{ forgotError }}</div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-cancel" @click="showForgotDialog = false">取消</button>
            <button
              v-if="forgotStep === 1"
              class="btn btn-save"
              :disabled="forgotLoading"
              @click="handleForgotNext"
            >
              {{ forgotLoading ? '验证中...' : '下一步' }}
            </button>
            <button
              v-if="forgotStep === 2"
              class="btn btn-save"
              :disabled="forgotLoading"
              @click="handleForgotReset"
            >
              {{ forgotLoading ? '重置中...' : '重置密码' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, getSecurityQuestion, resetPassword } from '@/api/employee'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loginForm = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const passwordFocused = ref(false)

// 忘记密码流程
const showForgotDialog = ref(false)
const forgotStep = ref(1) // 1: 输入用户名, 2: 回答问题
const forgotLoading = ref(false)
const forgotError = ref('')
const forgotForm = ref({
  username: '',
  question: '',
  answer: '',
  newPassword: ''
})

const togglePassword = () => {
  // 保存当前光标位置
  const input = document.getElementById('password') as HTMLInputElement
  const cursorPosition = input?.selectionStart || 0

  showPassword.value = !showPassword.value

  // 在下一帧恢复光标位置
  setTimeout(() => {
    if (input) {
      input.focus()
      input.setSelectionRange(cursorPosition, cursorPosition)
    }
  }, 0)
}

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const result = await login(loginForm.value)

    if (result.code === 1) {
      // 存储token和用户信息
      sessionStorage.setItem('token', result.data.token)
      sessionStorage.setItem('userName', result.data.userName)
      sessionStorage.setItem('name', result.data.name)
      sessionStorage.setItem('userId', result.data.id.toString())

      // 计算token过期时间（2小时后）
      const expireTime = Date.now() + 2 * 60 * 60 * 1000 // 2小时 = 7200000毫秒
      sessionStorage.setItem('tokenExpireTime', expireTime.toString())

      // 跳转到首页
      router.push('/home')
    } else {
      errorMessage.value = result.msg || '登录失败'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '网络错误，请稍后重试'
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}

const forgotPassword = () => {
  // 重置状态
  forgotStep.value = 1
  forgotError.value = ''
  forgotForm.value = { username: '', question: '', answer: '', newPassword: '' }
  showForgotDialog.value = true
}

// 忘记密码步骤1：输入用户名，获取密保问题
const handleForgotNext = async () => {
  if (!forgotForm.value.username) {
    forgotError.value = '请输入用户名'
    return
  }
  forgotLoading.value = true
  forgotError.value = ''
  try {
    const res: any = await getSecurityQuestion(forgotForm.value.username)
    if (res.code === 1 && res.data) {
      forgotForm.value.question = res.data
      forgotStep.value = 2
    } else {
      forgotError.value = '该账号未设置密保问题，请联系管理员'
    }
  } catch (error: any) {
    forgotError.value = error.message || '获取密保问题失败'
  } finally {
    forgotLoading.value = false
  }
}

// 忘记密码步骤2：验证答案并重置密码
const handleForgotReset = async () => {
  if (!forgotForm.value.answer) {
    forgotError.value = '请输入密保答案'
    return
  }
  if (!forgotForm.value.newPassword) {
    forgotError.value = '请输入新密码'
    return
  }
  if (forgotForm.value.newPassword.length < 6) {
    forgotError.value = '新密码长度不能少于6位'
    return
  }
  forgotLoading.value = true
  forgotError.value = ''
  try {
    await resetPassword(forgotForm.value.username, forgotForm.value.answer, forgotForm.value.newPassword)
    ElMessage.success('密码重置成功，请使用新密码登录')
    showForgotDialog.value = false
  } catch (error: any) {
    forgotError.value = error.message || '密码重置失败'
  } finally {
    forgotLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #e3f2fd;
  padding: 40px 20px;
}

.login-card {
  display: flex;
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  max-width: 1000px;
  width: 100%;
}

.login-left {
  flex: 5;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  padding: 20px;
}

.login-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.login-right {
  flex: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 400px;
}

.login-title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 28px;
  font-weight: bold;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

/* 禁用浏览器原生的密码显示/隐藏图标 */
input[type="password"]::-ms-reveal,
input[type="password"]::-ms-clear {
  display: none;
}

input[type="text"]::-ms-reveal,
input[type="text"]::-ms-clear {
  display: none;
}

.password-input-wrapper {
  position: relative;
}

.password-input-wrapper input {
  width: 100%;
  padding-right: 60px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  user-select: none;
  font-size: 18px;
  color: #666;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  background: #f0f0f0;
}

.form-group input:focus {
  outline: none;
  border-color: #ffb3d9;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #ffb3d9;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 15px;
}

.login-button:hover:not(:disabled) {
  background-color: #ff99cc;
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.footer-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #fee;
  color: #c33;
  border-radius: 5px;
  text-align: center;
  font-size: 14px;
}

@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
  }

  .login-left {
    max-height: 300px;
  }
}

/* 忘记密码弹窗 */
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
  max-width: 440px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
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

.security-question {
  background: #e3f2fd;
  padding: 12px;
  border-radius: 6px;
  font-size: 15px;
  color: #1976d2;
  font-weight: 500;
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
  font-weight: 500;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-save {
  background: #1976d2;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #1565c0;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
