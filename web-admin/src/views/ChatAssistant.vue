<template>
  <div class="chat-page">
    <!-- 左侧：对话区 -->
    <div class="chat-main">
      <div class="chat-header">
        <span class="chat-icon">🤖</span>
        <span class="chat-title">员工管理智能问答AI助手</span>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="welcome-state">
          <div class="welcome-icon">💬</div>
          <p>你好！我是员工管理智能助手，有什么可以帮助你的？</p>
        </div>
        <div
          v-for="(msg, i) in messages"
          :key="i"
          :class="['message-row', msg.role === 'user' ? 'msg-user' : 'msg-ai']"
        >
          <div class="msg-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div v-if="!msg.content" class="msg-bubble typing">
            <span></span><span></span><span></span>
          </div>
          <div v-else class="msg-bubble" v-text="msg.content" />
        </div>
      </div>

      <!-- 提示词快捷输入 -->
      <div class="suggestions-bar">
        <template v-if="tipsLoading">
          <span class="suggestion-chip placeholder">AI 正在生成推荐问题...</span>
        </template>
        <template v-else>
          <span
            v-for="(tip, i) in suggestionTips"
            :key="i"
            class="suggestion-chip"
            @click="handleSuggestionClick(tip)"
          >
            {{ tip }}
          </span>
        </template>
        <span class="suggestion-refresh" @click="fetchSuggestions" title="换一批问题">🔄</span>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <textarea
          v-model="inputText"
          class="chat-textarea"
          placeholder="输入问题，Shift+Enter 换行，Enter 发送"
          rows="1"
          @keydown="handleKeydown"
          @input="autoResize"
          ref="textareaRef"
        />
        <el-button
          type="primary"
          :disabled="!inputText.trim() || loading"
          :loading="loading"
          @click="handleSend"
          class="send-btn"
        >
          发送
        </el-button>
      </div>
    </div>

    <!-- 右侧：历史记录栏 -->
    <div class="chat-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <span v-show="!sidebarCollapsed" class="sidebar-title">历史记录</span>
        <span v-show="!sidebarCollapsed" class="delete-all-btn" @click="handleDeleteAll">
          删除全部
        </span>
        <button class="toggle-history-btn" @click="toggleSidebar">
          {{ sidebarCollapsed ? '◀' : '▶' }}
        </button>
      </div>

      <template v-if="!sidebarCollapsed">
        <div class="sidebar-search">
          <el-input
            v-model="historySearch"
            placeholder="搜索历史记录"
            clearable
            size="small"
            @keyup.enter="searchHistory"
            @clear="loadHistory"
          />
        </div>
        <div class="sidebar-list" v-loading="historyLoading">
          <div
            v-for="item in historyList"
            :key="item.id"
            :class="['history-item', { active: activeSessionId === item.id }]"
            @click="switchSession(item)"
          >
            <div class="history-title">{{ item.title }}</div>
            <div class="history-time">{{ item.updateTime }}</div>
            <el-popover
              trigger="click"
              :width="120"
              placement="left"
              @click.stop
            >
              <template #reference>
                <span class="history-more" @click.stop>⋯</span>
              </template>
              <div class="popover-option" @click.stop="handleRename(item)">✏️ 重命名</div>
              <div class="popover-option danger" @click.stop="handleDeleteHistory(item.id!)">🗑️ 删除</div>
            </el-popover>
          </div>
          <el-empty v-if="historyList.length === 0 && !historyLoading" description="暂无历史记录" />
        </div>
        <div class="sidebar-pagination" v-if="historyTotal > historyPageSize">
          <el-pagination
            v-model:current-page="historyPage"
            :page-size="historyPageSize"
            :total="historyTotal"
            layout="prev, pager, next"
            size="small"
            @current-change="loadHistory"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createChatSession,
  getChatSessionPage,
  deleteChatSession,
  deleteAllChatSessions,
  renameChatSession,
  chatStream,
  getSuggestions,
  summarizeTitle,
  type ChatSession
} from '@/api/chat'

// ===== 提示词快捷输入 =====
const suggestionTips = ref<string[]>([])
const tipsLoading = ref(false)

const fetchSuggestions = async () => {
  tipsLoading.value = true
  try {
    const res = await getSuggestions()
    const data = await res.json()
    if (data?.data?.suggestions?.length) {
      suggestionTips.value = data.data.suggestions
    }
  } catch {
    // 兜底
    suggestionTips.value = [
      '姓名为"李"的有多少人？',
      '有哪些被禁用的账号？',
      '138开头的手机号是谁？',
    ]
  } finally {
    tipsLoading.value = false
  }
}

const handleSuggestionClick = (tip: string) => {
  inputText.value = tip
  textareaRef.value?.focus()
}

// ===== 消息 =====
interface Message {
  role: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([])
const inputText = ref('')
const loading = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)
const activeSessionId = ref<number | null>(null)

// Shift+Enter 换行，Enter 发送
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 自动调整输入框高度
const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

// 发送消息
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })

  // 发送瞬间立刻清空输入框
  inputText.value = ''
  nextTick(() => {
    autoResize()
    scrollToBottom()
  })

  // 如果是第一条消息，先 await 创建会话，确保 activeSessionId 就绪后再发流式请求
  if (messages.value.filter(m => m.role === 'user').length === 1) {
    try {
      let title = text.slice(0, 12)
      try {
        const sumRes = await summarizeTitle(text)
        const sumData = await sumRes.json()
        title = sumData?.data?.title || text.slice(0, 12)
      } catch {}
      const sessionRes: any = await createChatSession(title)
      if (sessionRes.code === 1) {
        activeSessionId.value = sessionRes.data.id
        sessionStorage.setItem('activeChatSessionId', String(sessionRes.data.id))
        loadHistory()
      }
    } catch {
      // 降级：直接用前12字创建
      try {
        const sessionRes: any = await createChatSession(text.slice(0, 12))
        if (sessionRes.code === 1) {
          activeSessionId.value = sessionRes.data.id
          sessionStorage.setItem('activeChatSessionId', String(sessionRes.data.id))
          loadHistory()
        }
      } catch {}
    }
  }

  loading.value = true

  // 插入一个空的 AI 消息占位，用于流式填充
  messages.value.push({ role: 'ai', content: '' })
  const aiMsg = messages.value[messages.value.length - 1]

  try {
    const response = await chatStream(text)
    const reader = response.body?.getReader()
    if (!reader) throw new Error('无法读取流')

    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed.startsWith('data: ')) continue

        const payload = trimmed.slice(6) // 去掉 "data: "
        if (payload === '[DONE]') continue

        try {
          const parsed = JSON.parse(payload)
          if (parsed.content) {
            aiMsg.content += parsed.content
            scrollToBottom()
          }
          if (parsed.error) {
            aiMsg.content = `[错误] ${parsed.error}`
          }
        } catch {
          // 忽略解析失败的行
        }
      }
    }
  } catch (e: any) {
    aiMsg.content = `请求失败：${e.message || '未知错误'}`
    ElMessage.error('AI 请求失败')
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const el = messagesRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// ===== 历史记录 =====
const sidebarCollapsed = ref(false)
const historySearch = ref('')
const historyList = ref<ChatSession[]>([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res: any = await getChatSessionPage(
      historyPage.value,
      historyPageSize.value,
      historySearch.value || undefined
    )
    if (res.code === 1) {
      historyList.value = res.data.records
      historyTotal.value = res.data.total
    }
  } catch {
    // 后端可能还没重启
  } finally {
    historyLoading.value = false
  }
}

const searchHistory = () => {
  historyPage.value = 1
  loadHistory()
}

const switchSession = (_item: ChatSession) => {
  // TODO: 加载历史消息（接入 AI 后完善）
  activeSessionId.value = _item.id!
}

const handleRename = async (item: ChatSession) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的标题', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: item.title
    })
    if (value && value.trim()) {
      const res: any = await renameChatSession(item.id!, value.trim())
      if (res.code === 1) {
        ElMessage.success('重命名成功')
        loadHistory()
      }
    }
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('重命名失败')
  }
}

const handleDeleteHistory = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该历史记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res: any = await deleteChatSession(id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      if (activeSessionId.value === id) {
        activeSessionId.value = null
      }
      loadHistory()
    }
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleDeleteAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定删除所有历史记录吗？此操作不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    const res: any = await deleteAllChatSessions()
    if (res.code === 1) {
      ElMessage.success('已删除全部历史记录')
      activeSessionId.value = null
      historyPage.value = 1
      loadHistory()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadHistory()
  fetchSuggestions()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 120px);
  gap: 0;
}

/* ===== 左侧对话区 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 4px;
  margin-right: 12px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.chat-icon {
  font-size: 22px;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8fafc;
}

.welcome-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8f0fe;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  text-align: left;
  white-space: pre-line;
  overflow-wrap: break-word;
  word-break: break-all;
}

.msg-user .msg-bubble {
  background: #1e3a8a;
  color: #fff;
  border-top-right-radius: 4px;
}

.msg-ai .msg-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e8e8e8;
  border-top-left-radius: 4px;
}

/* 打字动画 */
.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
  margin: 0 2px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}

/* 输入区 */
.suggestions-bar {
  display: flex;
  gap: 10px;
  padding: 0 20px 12px;
  flex-wrap: wrap;
}

.suggestion-chip {
  display: inline-block;
  padding: 6px 14px;
  font-size: 13px;
  color: #1e3a8a;
  background: #e8f0fe;
  border-radius: 20px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
  white-space: nowrap;
}

.suggestion-chip:hover {
  background: #1e3a8a;
  color: #fff;
}

.suggestion-chip.placeholder {
  color: #909399;
  cursor: default;
}

.suggestion-chip.placeholder:hover {
  background: #e8f0fe;
  color: #909399;
}

.suggestion-refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 14px;
  border-radius: 50%;
  cursor: pointer;
  user-select: none;
  transition: transform 0.3s;
  flex-shrink: 0;
}

.suggestion-refresh:hover {
  transform: rotate(180deg);
  background: #f0f2f5;
}

.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.chat-textarea {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  font-family: inherit;
  max-height: 120px;
}

.chat-textarea:focus {
  border-color: #1e3a8a;
  box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.1);
}

.send-btn {
  height: 40px;
  min-width: 72px;
}

/* ===== 右侧历史记录栏 ===== */
.chat-sidebar {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s;
}

.chat-sidebar.collapsed {
  width: 48px;
  min-width: 48px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.delete-all-btn {
  font-size: 12px;
  color: #f56c6c;
  cursor: pointer;
  user-select: none;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s;
  margin-left: auto;
  margin-right: 8px;
}

.delete-all-btn:hover {
  background: #fef0f0;
}

.toggle-history-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f0f2f5;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  color: #606266;
  transition: all 0.2s;
}

.toggle-history-btn:hover {
  background: #e0e0e0;
}

.sidebar-search {
  padding: 12px 16px;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.history-item {
  padding: 12px;
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 4px;
  transition: background 0.2s;
  position: relative;
}

.history-item:hover {
  background: #f5f5f5;
}

.history-item.active {
  background: #e8f0fe;
}

.history-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 30px;
}

.history-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.history-more {
  position: absolute;
  right: 8px;
  top: 8px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s, background 0.2s;
  user-select: none;
}

.history-item:hover .history-more {
  opacity: 1;
}

.history-more:hover {
  background: #e0e0e0;
  color: #303133;
}

.popover-option {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  border-radius: 4px;
}

.popover-option:hover {
  background: #f0f2f5;
}

.popover-option.danger {
  color: #e6a23c;
}

.popover-option.danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.sidebar-pagination {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}

/* Element Plus 覆盖 */
:deep(.el-button--primary) {
  background-color: #1e3a8a;
  border-color: #1e3a8a;
}
</style>
