# 🚀 AI 企业提效助手（AIcoding Helper）

一个面向企业场景的 **AI Agent 工作助手**，基于 **LangChain4j + RAG + MCP** 构建，支持多轮对话、知识增强问答、工具调用与会话记忆，帮助团队提升知识获取、流程处理与问题解决效率。

## 👀 项目预览
<img width="1946" height="1048" alt="image" src="https://github.com/user-attachments/assets/86bc0fd6-7037-4bfd-940b-71fe195d5daf" />

---

## 📌 项目定位

AI 企业提效助手聚焦“**大模型能力 + 企业业务能力**”融合，目标是：

- 🧠 让员工用自然语言快速获取企业知识与流程指引
- 📚 让内部文档、制度、项目资料被“可问可用”
- 🛠️ 让 AI 自动调用工具执行任务，减少重复操作
- 🔒 通过安全策略与权限边界，保障可控落地

适用于：

- 客服/售前支持
- 运营与流程协同
- 研发与技术支持
- 企业内部知识问答与 SOP 辅助

---

## 🧱 技术架构

### 后端

- Spring Boot
- LangChain4j（Agent 编排）
- MySQL（会话记忆持久化）
- RAG（检索增强生成）
- MCP（工具调用协议）
- SSE（流式响应）

### 前端

- Vue3
- Axios
- EventSource（SSE）

---

## ⚙️ 核心能力

### 1️⃣ 多轮对话 + 会话记忆

- 基于 `memoryId` 实现会话隔离
- 自定义 `ChatMemory`，支持 MySQL 持久化
- 支持连续上下文理解，减少重复输入

### 2️⃣ 企业知识增强（RAG）

- 对企业文档、FAQ、制度流程进行切片与向量化
- 语义检索召回 TopK 相关内容
- 将检索结果拼接到上下文，提高回答可靠性

可解决：

- ❌ 回答“看起来对、但不基于企业事实”
- ❌ 新人找不到知识、查阅成本高
- ❌ 企业私有知识无法被模型直接利用

### 3️⃣ MCP 工具调用（执行能力）

支持接入并调用外部工具：

- 🔎 搜索与信息查询
- 📄 文档/PDF 解析
- 🔌 可扩展企业内部 API、数据库与业务系统

实现“**理解问题 → 调用工具 → 返回结果**”闭环。

### 4️⃣ Guardrail 安全机制

- 输入内容检测（风险词/违规请求）
- 异常请求拦截
- 提升系统安全性、合规性与可控性

### 5️⃣ 流式响应（SSE）

- 实时输出生成内容
- 优化交互体验，降低等待感

---

## 🧩 核心设计

### 🧠 ChatMemory（会话记忆）

自定义实现：

```java
MySQLChatMemory implements ChatMemory
```

特点：

- 聊天记录持久化
- 支持多用户隔离
- 自动裁剪历史消息（`maxMessages`）

### 🔍 RAG 流程

```text
用户问题
   ↓
向量化
   ↓
向量检索（TopK）
   ↓
拼接上下文
   ↓
LLM 生成回答
```

### 🛠️ Agent 执行流程

```text
用户问题
   ↓
LLM 意图识别
   ↓
判断是否调用工具
   ↓
调用 MCP Tool
   ↓
融合结果输出最终答案
```

---

## 🚀 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/chenhao467/ai-coding-helper.git
cd ai-coding-helper
```

### 2️⃣ 配置数据库

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aicodinghelper?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3️⃣ 配置大模型 Key

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your_api_key
```

### 4️⃣ 启动后端

```bash
mvn spring-boot:run
```

### 5️⃣ 启动前端

```bash
cd ai-code-helper-frontend
npm install
npm run dev
```

---

## 📡 接口说明

### SSE 聊天接口

```http
GET /api/ai/chat?memoryId=1&message=你好
```

### Agent 聊天接口

```http
POST /api/agent/chat
Content-Type: application/json

{
  "message": "请帮我总结本周项目风险",
  "memoryId": "1",
  "userId": 1,
  "tenantId": 1
}
```

---

## 📈 企业价值亮点

- ✅ 打通“问答 + 检索 + 工具执行”完整链路
- ✅ 企业知识可沉淀、可检索、可复用
- ✅ 支持按会话、用户、租户进行隔离管理
- ✅ 降低员工检索信息和处理任务的时间成本
- ✅ 可按业务扩展工具，持续提升自动化能力

---

## 🔮 后续规划

- 🔄 引入 Redis 缓存层，提高高并发场景性能
- 📊 接入向量数据库（Milvus / pgvector）
- 🧠 增强 RAG（Rerank / Hybrid Search）
- 🤖 多 Agent 协作（Planner + Executor）
- 📈 监控、Tracing 与审计日志能力

---

## 📄 License

MIT License

---

## 🙋‍♂️ 作者

Chenhao（Java 开发 / AI Agent 方向）
