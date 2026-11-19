import { ElNotification } from 'element-plus'

// 订单提醒 WebSocket：来单提醒 / 客户催单
let socket: WebSocket | null = null
let reconnectTimer: number | null = null
// 收到消息后的回调（用于通知员工端刷新未读角标）
let messageListener: (() => void) | null = null

export function setOrderNotifyListener(fn: (() => void) | null) {
  messageListener = fn
}

// 后端地址端口 8080（与 src/utils/request.ts 保持一致）
const wsUrl = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.hostname}:8080/ws/order-notify`

// 语音播报（浏览器朗读）
function speak(text: string) {
  try {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) return
    const synth = window.speechSynthesis
    // 首次 getVoices() 可能为空，先触发一次加载
    synth.getVoices()
    const utter = new SpeechSynthesisUtterance(text)
    utter.lang = 'zh-CN'
    const voices = synth.getVoices()
    const zhVoice = voices.find((v) => v.lang && v.lang.toLowerCase().indexOf('zh') !== -1)
    if (zhVoice) utter.voice = zhVoice
    // 先停掉旧播报再播新的；稍作延迟避免 cancel 把刚播的也取消了
    synth.cancel()
    window.setTimeout(() => synth.speak(utter), 80)
  } catch (e) {
    /* 忽略播报异常，不影响消息提示 */
  }
}

// 消息类型 → 默认标题（后端 push 时通常已带 title，这里只是兜底）
const NOTIFY_TITLES: Record<string, string> = {
  newOrder: '来单提醒',
  urge: '客户催单',
  assign: '订单已指派',
  accept: '家政员已接单',
  acceptRemind: '家政员未确认接单',
  reject: '家政员已拒单',
  refund: '订单待退款'
}

// 所有订单消息统一使用同一种弹窗样式，避免同类消息之间颜色不一致
const NOTIFY_STYLE = 'success' as const

function handleMessage(raw: string) {
  let msg: any
  try {
    msg = JSON.parse(raw)
  } catch (e) {
    return
  }
  const title = msg.title || NOTIFY_TITLES[msg.type] || '订单提醒'
  const content = msg.content || ''
  if (content) {
    ElNotification({
      title,
      message: content,
      type: NOTIFY_STYLE,
      duration: 5000,
      position: 'top-right'
    })
    speak(content)
  }
}

function scheduleReconnect() {
  if (reconnectTimer !== null) return
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null
    connectOrderNotify()
  }, 3000)
}

export function connectOrderNotify() {
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) return
  try {
    socket = new WebSocket(wsUrl)
    socket.onopen = () => { console.log('订单提醒连接成功：来单/催单将实时播报') }
    socket.onmessage = (ev) => {
      handleMessage(ev.data)
      if (messageListener) messageListener()
    }
    socket.onclose = () => { socket = null; scheduleReconnect() }
    socket.onerror = () => { socket && socket.close() }
  } catch (e) {
    scheduleReconnect()
  }
}

export function closeOrderNotify() {
  if (reconnectTimer !== null) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (socket) {
    socket.onclose = null
    socket.close()
    socket = null
  }
}
