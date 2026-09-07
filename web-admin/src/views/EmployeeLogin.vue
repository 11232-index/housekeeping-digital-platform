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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { employeeLogin } from '@/api/employee-auth'

const router = useRouter()

const loginForm = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const passwordFocused = ref(false)

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
    const result = await employeeLogin(loginForm.value)

    if (result.code === 1) {
      // 存储token和用户信息（员工端使用独立 key，与管理端区分）
      sessionStorage.setItem('employeeToken', result.data.token)
      sessionStorage.setItem('employeeName', result.data.userName)
      sessionStorage.setItem('employeeFullName', result.data.name)
      sessionStorage.setItem('employeeId', result.data.id.toString())
      sessionStorage.setItem('employeeRole', result.data.role || 'staff')

      // 计算token过期时间（2小时后）
      const expireTime = Date.now() + 2 * 60 * 60 * 1000 // 2小时 = 7200000毫秒
      sessionStorage.setItem('employeeTokenExpireTime', expireTime.toString())

      // 跳转到员工首页
      router.push('/employee/home')
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
  ElMessage.warning('请联系管理员重置密码')
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
</style>
