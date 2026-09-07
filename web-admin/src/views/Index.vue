<template>
  <div class="index-container">
    <div class="index-card">
      <h1 class="system-title">智能家政服务</h1>
      <div class="button-group">
        <button class="portal-button admin-button" @click="goToAdmin">
          <div class="button-icon">👨‍💼</div>
          <div class="button-text">管理端</div>
        </button>
        <button class="portal-button user-button" @click="goToUser">
          <div class="button-icon">🧹</div>
          <div class="button-text">员工端</div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToAdmin = () => {
  router.push('/login')
}

const goToUser = () => {
  router.push('/employee/login')
}
</script>

<style scoped>
.index-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #e3f2fd;
}

.index-card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 60px 80px;
  text-align: center;
}

.system-title {
  font-size: 36px;
  font-weight: bold;
  color: #333;
  margin-bottom: 50px;
}

.button-group {
  display: flex;
  gap: 40px;
}

.portal-button {
  width: 200px;
  height: 200px;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.admin-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.admin-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.user-button {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.user-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(240, 147, 251, 0.4);
}

.button-icon {
  font-size: 60px;
}

.button-text {
  font-size: 24px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .index-card {
    padding: 40px 30px;
  }

  .button-group {
    flex-direction: column;
    gap: 20px;
  }

  .portal-button {
    width: 250px;
    height: 150px;
  }
}
</style>
