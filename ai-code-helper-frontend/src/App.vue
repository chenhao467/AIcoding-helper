<template>
  <div class="page">
    <aside class="sidebar">
      <div class="sidebar-top">
        <div class="brand">
          <div class="brand-logo">◎</div>
          <div class="brand-name">AIcoding</div>
        </div>

        <button class="new-chat-btn" @click="createNewChat">+ 新建对话</button>

        <nav class="nav-group">
          <p class="nav-title">工作台</p>
          <button class="nav-item active">💬 对话</button>
          <button class="nav-item">🧭 探索</button>
          <button class="nav-item">📚 知识库</button>
        </nav>

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
      </div>
    </aside>

    <main class="chat-main">
      <header class="chat-header">
        <h2>{{ currentModeHeader }}</h2>
        <span class="status-dot">● Online</span>
      </header>

      <section class="messages" ref="messagesContainer">
        <div class="messages-inner">
          <div class="welcome" v-if="messages.length === 0">
            <h3>你好，我是你的 AI 助手</h3>
            <p>需要我做什么?</p>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-row', msg.role]"
          >
            <div class="avatar">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
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
        </div>
      </section>

      <footer class="composer-wrap">
        <div class="composer">
          <textarea
            v-model="inputMessage"
            placeholder="给 AIcoding 发消息..."
            @keydown.enter.prevent="sendMessage"
            :disabled="isLoading"
            rows="1"
            ref="inputTextarea"
          ></textarea>
          <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()">发送</button>
        </div>
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
  background: #1b1c1f;
  color: #ececec;
  display: grid;
  grid-template-columns: 260px 1fr;
}

.sidebar {
  border-right: 1px solid #2f3034;
  background: #111214;
  padding: 14px;
}

.sidebar-top {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
}

.brand-logo {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: #2a2c31;
  color: #d8d8d8;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
}

.new-chat-btn {
  height: 40px;
  border: 1px solid #3a3d45;
  border-radius: 12px;
  color: #e7e7e7;
  background: #24262b;
  font-weight: 550;
  cursor: pointer;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-title {
  font-size: 12px;
  color: #8b8d93;
  padding: 0 8px;
}

.nav-item {
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #c8c9cd;
  text-align: left;
  padding: 0 10px;
  cursor: pointer;
}

.nav-item.active,
.nav-item:hover {
  background: #2a2c31;
  color: #fff;
}

.mode-card {
  background: #1b1d21;
  border: 1px solid #33353b;
  border-radius: 12px;
  padding: 10px;
}

.mode-title {
  margin: 0 0 10px;
  font-size: 12px;
  color: #9fa1a8;
}

.mode-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mode-btn {
  border: 1px solid #383a42;
  background: #23252a;
  color: #d5d6da;
  border-radius: 10px;
  height: 36px;
  cursor: pointer;
}

.mode-btn.active {
  border-color: #646873;
  background: #2f3239;
  color: #fff;
}

.mode-desc {
  color: #8e9098;
  margin: 10px 0 0;
  font-size: 12px;
  line-height: 1.5;
}

.chat-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #1a1b1e;
}

.chat-header {
  height: 62px;
  padding: 0 28px;
  border-bottom: 1px solid #2e2f33;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.status-dot {
  font-size: 12px;
  color: #9ea0a6;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px 120px;
}

.messages-inner {
  max-width: 860px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.welcome {
  margin: 90px auto;
  text-align: center;
  color: #9ea0a6;
}

.welcome h3 {
  color: #f1f1f1;
  margin-bottom: 12px;
  font-size: 28px;
  font-weight: 600;
}

.message-row {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-size: 12px;
  background: #2a2c31;
  color: #d4d4d4;
  flex-shrink: 0;
}

.bubble {
  padding: 12px 14px;
  border-radius: 12px;
  background: #24262b;
  border: 1px solid #343741;
  line-height: 1.7;
  white-space: pre-wrap;
  max-width: min(760px, 92vw);
}

.message-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-row.user .bubble {
  background: #30333a;
  border-color: #434751;
}

.meta {
  font-size: 11px;
  color: #9fa4af;
  margin-bottom: 4px;
}

.composer-wrap {
  position: sticky;
  bottom: 0;
  padding: 14px 22px 18px;
  background: linear-gradient(to top, rgba(26, 27, 30, 0.98), rgba(26, 27, 30, 0.35));
}

.composer {
  max-width: 860px;
  margin: 0 auto;
  border: 1px solid #3a3d45;
  border-radius: 22px;
  background: #2a2c31;
  min-height: 56px;
  display: flex;
  align-items: center;
  padding: 8px 10px 8px 16px;
  gap: 10px;
}

.composer textarea {
  flex: 1;
  min-height: 26px;
  max-height: 160px;
  border: none;
  background: transparent;
  color: #f0f0f0;
  resize: none;
  outline: none;
  line-height: 1.6;
  font-size: 15px;
}

.composer textarea::placeholder {
  color: #9b9da3;
}

.composer button {
  width: 76px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: #f3f4f6;
  color: #1f2229;
  font-weight: 600;
  cursor: pointer;
}

.composer button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.typing span {
  width: 6px;
  height: 6px;
  margin-right: 6px;
  display: inline-block;
  border-radius: 50%;
  background: #b7bbc4;
  animation: pulse 1.2s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.45;
  }

  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 900px) {
  .page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
    border-bottom: 1px solid #2f3034;
  }

  .messages {
    padding-top: 16px;
  }

  .welcome {
    margin: 56px auto;
  }

  .welcome h3 {
    font-size: 24px;
  }
}
</style>
