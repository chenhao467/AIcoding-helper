<template>
  <div class="page">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-logo">AI</div>
        <div>
          <h1>AI 助手</h1>
          <p>Assistant Workspace</p>
        </div>
      </div>

      <div class="mode-card">
        <p class="mode-title">对话模式</p>
        <div class="mode-options">
          <button
            v-for="mode in modes"
            :key="mode.value"
            class="mode-btn"
            :class="{ active: currentMode === mode.value }"
            @click="switchMode(mode.value)"
          >
            {{ mode.label }}
          </button>
        </div>
        <p class="mode-desc">{{ currentModeDesc }}</p>
      </div>

      <button class="new-chat-btn" @click="createNewChat">+ 新建对话</button>
    </aside>

    <main class="chat-main">
      <header class="chat-header">
        <div>
          <h2>{{ currentModeHeader }}</h2>
          <p>统一聊天框，按模式自动选择不同后端接口</p>
        </div>
        <span class="status-dot">Online</span>
      </header>

      <section class="messages" ref="messagesContainer">
        <div class="welcome" v-if="messages.length === 0">
          <h3>你好，我是你的 AI 助手</h3>
          <p>你可以选择“通用对话”或“业务 Agent”，并直接在同一个输入框发起请求。</p>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message-row', msg.role]"
        >
          <div class="avatar">{{ msg.role === 'user' ? 'U' : 'AI' }}</div>
          <div class="bubble">
            <div class="meta" v-if="msg.mode">{{ msg.mode }}</div>
            <div>{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="isTyping" class="message-row ai">
          <div class="avatar">AI</div>
          <div class="bubble typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </section>

      <footer class="composer">
        <textarea
          v-model="inputMessage"
          placeholder="输入你的问题，回车发送..."
          @keydown.enter.prevent="sendMessage"
          :disabled="isLoading"
          rows="1"
          ref="inputTextarea"
        ></textarea>
        <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">发送</button>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'

const API_BASE = 'http://localhost:8081/api'

const modes = [
  {
    value: 'ai',
    label: '通用对话',
    desc: 'GET /ai/chat（SSE 流式，适合连续文字问答）'
  },
  {
    value: 'agent',
    label: '业务 Agent',
    desc: 'POST /agent/chat（意图识别 + 工具执行）'
  }
]

const currentMode = ref('ai')
const messages = ref([])
const inputMessage = ref('')
const memoryId = ref(null)
const isLoading = ref(false)
const isTyping = ref(false)
const messagesContainer = ref(null)
const inputTextarea = ref(null)

const currentModeDesc = computed(() => modes.find((item) => item.value === currentMode.value)?.desc)
const currentModeHeader = computed(() => (currentMode.value === 'ai' ? '通用对话模式' : '业务 Agent 模式'))

const generateMemoryId = () => Math.floor(Math.random() * 1000000)

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const switchMode = (mode) => {
  currentMode.value = mode
}

const createNewChat = () => {
  messages.value = []
  memoryId.value = generateMemoryId()
}

const sendByAiSSE = (message, aiMessageIndex) => {
  const url = `${API_BASE}/ai/chat?memoryId=${memoryId.value}&message=${encodeURIComponent(message)}`
  const eventSource = new EventSource(url)

  eventSource.onmessage = (event) => {
    if (event.data) {
      messages.value[aiMessageIndex].content += event.data
      scrollToBottom()
    }
  }

  eventSource.onerror = () => {
    eventSource.close()
    isLoading.value = false
    isTyping.value = false
  }

  eventSource.addEventListener('close', () => {
    eventSource.close()
    isLoading.value = false
    isTyping.value = false
  })
}

const sendByAgent = async (message, aiMessageIndex) => {
  try {
    const response = await fetch(`${API_BASE}/agent/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message,
        memoryId: String(memoryId.value),
        userId: 1,
        tenantId: 1
      })
    })

    const result = await response.json()
    messages.value[aiMessageIndex].content = result.reply || '服务暂未返回结果'
    messages.value[aiMessageIndex].mode = `Intent: ${result.intent || 'UNKNOWN'}`
  } catch (error) {
    messages.value[aiMessageIndex].content = `调用业务 Agent 失败：${error.message}`
  } finally {
    isLoading.value = false
    isTyping.value = false
  }
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  messages.value.push({ role: 'user', content: message, mode: currentMode.value === 'ai' ? '通用对话' : '业务 Agent' })
  inputMessage.value = ''
  scrollToBottom()

  isLoading.value = true
  isTyping.value = true
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '', mode: '' })

  if (currentMode.value === 'ai') {
    sendByAiSSE(message, aiMessageIndex)
    return
  }

  await sendByAgent(message, aiMessageIndex)
  scrollToBottom()
}

onMounted(() => {
  memoryId.value = generateMemoryId()
  inputTextarea.value?.focus()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #0f172a;
  color: #e2e8f0;
  display: grid;
  grid-template-columns: 280px 1fr;
}

.sidebar {
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  padding: 20px 16px;
  background: linear-gradient(180deg, #111827 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #6366f1, #06b6d4);
  font-weight: 700;
}

.brand h1 {
  margin: 0;
  font-size: 17px;
}

.brand p {
  margin: 2px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.mode-card {
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 14px;
  padding: 12px;
}

.mode-title {
  margin: 0 0 10px;
  font-size: 13px;
  color: #cbd5e1;
}

.mode-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mode-btn {
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: transparent;
  color: #e2e8f0;
  border-radius: 10px;
  height: 38px;
  cursor: pointer;
}

.mode-btn.active {
  border-color: #38bdf8;
  background: rgba(56, 189, 248, 0.15);
}

.mode-desc {
  color: #94a3b8;
  margin: 10px 0 0;
  font-size: 12px;
  line-height: 1.5;
}

.new-chat-btn {
  height: 40px;
  border: none;
  border-radius: 10px;
  color: #0f172a;
  background: linear-gradient(90deg, #67e8f9, #a5b4fc);
  font-weight: 600;
  cursor: pointer;
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 18px 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
}

.chat-header p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

.status-dot {
  font-size: 12px;
  color: #22c55e;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome {
  margin: auto;
  text-align: center;
  color: #94a3b8;
}

.welcome h3 {
  color: #e2e8f0;
  margin-bottom: 10px;
}

.message-row {
  display: flex;
  gap: 10px;
  max-width: 900px;
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 12px;
  background: #1e293b;
  color: #cbd5e1;
}

.bubble {
  padding: 12px 14px;
  border-radius: 14px;
  background: #111827;
  border: 1px solid rgba(148, 163, 184, 0.2);
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-row.user .bubble {
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9);
  border: none;
}

.meta {
  font-size: 11px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.composer {
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  padding: 16px 24px;
  display: flex;
  gap: 10px;
  background: rgba(15, 23, 42, 0.9);
}

.composer textarea {
  flex: 1;
  min-height: 48px;
  max-height: 160px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: #0b1220;
  color: #e2e8f0;
  padding: 12px;
  resize: vertical;
}

.composer button {
  width: 88px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #6366f1, #0ea5e9);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.composer button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.typing span {
  width: 6px;
  height: 6px;
  margin-right: 6px;
  display: inline-block;
  border-radius: 50%;
  background: #93c5fd;
  animation: pulse 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: .2s;
}

.typing span:nth-child(3) {
  animation-delay: .4s;
}

@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

@media (max-width: 900px) {
  .page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  }
}
</style>
