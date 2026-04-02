# 🚀 AI 编程学习助手

一个基于 **LangChain4j + RAG + MCP** 构建的 AI Agent 应用，支持多轮对话、知识增强问答、工具调用与会话记忆，专注于编程学习与面试场景。

---

## 📌 项目简介

AI 编程学习助手旨在帮助用户：

* 📚 解答编程知识问题（Java / 后端 / 系统设计等）
* 🧠 提供面试题与解析
* 🔍 基于知识库增强回答（RAG）
* 🛠️ 调用工具（搜索、PDF解析等）解决复杂问题

通过将 **大模型能力 + 工程系统能力** 结合，实现一个可落地的 AI Agent。

---

## 🧱 技术架构

### 后端

* Spring Boot
* LangChain4j（Agent框架）
* MySQL（会话记忆持久化）
* RAG（检索增强生成）
* MCP（工具调用协议）
* SSE（流式响应）

### 前端

* Vue3
* Axios
* EventSource（SSE）

---

## ⚙️ 核心功能

### 1️⃣ 多轮对话 + 会话记忆

* 基于 `memoryId` 实现用户会话隔离
* 自定义 `ChatMemory`（MySQL 持久化）
* 支持上下文连续对话

---

### 2️⃣ RAG（检索增强生成）

* 对面试题库、技术文档进行切片与向量化
* 基于语义检索召回 TopK 相关内容
* 将检索结果拼接为上下文输入大模型

👉 解决问题：

* ❌ 模型幻觉
* ❌ 知识过时
* ❌ 无法访问私有数据

---

### 3️⃣ MCP 工具调用（Agent能力）

支持调用外部工具：

* 🔎 搜索引擎（面试题查询）
* 📄 PDF解析
* 🔌 可扩展任意工具（API / DB / 爬虫）

👉 实现“推理 + 执行”闭环

---

### 4️⃣ Guardrail 安全机制

* 对用户输入进行敏感词检测
* 拦截非法请求
* 提升系统安全性与可控性

---

### 5️⃣ 流式响应（SSE）

* 基于 Server-Sent Events 实现实时输出
* 提升用户体验（类似 ChatGPT 打字效果）

---

## 🧩 核心设计

### 🧠 ChatMemory（会话记忆）

自定义实现：

```java
MySQLChatMemory implements ChatMemory
```

特点：

* 持久化存储聊天记录
* 支持多用户隔离
* 自动裁剪历史消息（maxMessages）

---

### 🔍 RAG流程

```text
用户问题
   ↓
向量化
   ↓
向量数据库检索（TopK）
   ↓
拼接上下文
   ↓
LLM生成答案
```

---

### 🛠️ Agent执行流程

```text
用户问题
   ↓
LLM分析意图
   ↓
是否需要调用工具
   ↓
调用 MCP Tool
   ↓
结合结果生成最终回答
```

---

## 🚀 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/chenhao467/ai-coding-helper.git
cd ai-coding-helper
```

---

### 2️⃣ 配置数据库

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aicodinghelper?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

---

### 3️⃣ 配置大模型 Key

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your_api_key
```

---

### 4️⃣ 启动后端

```bash
mvn spring-boot:run
```

---

### 5️⃣ 启动前端

```bash
cd frontend
npm install
npm run dev
```

---

## 📡 接口说明

### SSE聊天接口

```http
GET /api/ai/chat?memoryId=1&message=你好
```

### 返回格式

```text
data: AI返回内容片段
```

---

## 📈 项目亮点

* ✅ 基于 LangChain4j 构建完整 AI Agent 系统
* ✅ 自定义 ChatMemory 实现会话持久化与隔离
* ✅ 引入 RAG 提升回答准确性并降低幻觉
* ✅ 支持 MCP 工具调用，实现复杂任务处理
* ✅ 使用 SSE 实现流式响应，提升交互体验
* ✅ 具备安全控制（Guardrail）机制

---

## 🔮 后续优化方向

* 🔄 引入 Redis 做缓存层（提升性能）
* 📊 加入向量数据库（Milvus / pgvector）
* 🧠 增强 RAG（Rerank / Hybrid Search）
* 🤖 多Agent协作（Planner + Executor）
* 📈 监控与日志（Tracing）

---

## 📄 License

MIT License

---

## 🙋‍♂️ 作者

Chenhao（Java开发 / AI Agent方向）
