🚀 AI 企业提效助手（AI-Company-Helper）
一个面向企业场景的 AI Agent 工作助手，基于 LangChain4j + RAG + MCP 构建，支持多轮对话、知识增强问答、工具调用与会话记忆，帮助团队提升知识获取、流程处理与问题解决效率。

👀 项目预览
初始界面：

<img width="2463" height="1458" alt="R{ `Z$Q84{1)5A{88`0LNGV" src="https://github.com/user-attachments/assets/18f3866d-2b91-4e9b-8529-ced23c09835e" />

询问工单：

<img width="1561" height="677" alt="image" src="https://github.com/user-attachments/assets/259ee3fc-53a1-4961-a2e9-e7cc04719d44" />


📌 项目定位
AI 企业提效助手聚焦“大模型能力 + 企业业务能力”融合，目标是：

🧠 让员工用自然语言快速获取企业知识与流程指引
📚 让内部文档、制度、项目资料被“可问可用”
🛠️ 让 AI 自动调用工具执行任务，减少重复操作
🔒 通过安全策略与权限边界，保障可控落地
适用于：

客服/售前支持
运营与流程协同
研发与技术支持
企业内部知识问答与 SOP 辅助
🧱 技术架构
后端
Spring Boot
LangChain4j（Agent框架）
LangChain4j（Agent 编排）
MySQL（会话记忆持久化）
RAG（检索增强生成）
MCP（工具调用协议）
SSE（流式响应）
前端
Vue3
Axios
EventSource（SSE）
⚙️ 核心功能
⚙️ 核心能力
1️⃣ 多轮对话 + 会话记忆
基于 memoryId 实现用户会话隔离
自定义 ChatMemory（MySQL 持久化）
支持上下文连续对话
基于 memoryId 实现会话隔离
自定义 ChatMemory，支持 MySQL 持久化
支持连续上下文理解，减少重复输入
2️⃣ RAG（检索增强生成）
2️⃣ 企业知识增强（RAG）
对面试题库、技术文档进行切片与向量化
基于语义检索召回 TopK 相关内容
将检索结果拼接为上下文输入大模型
对企业文档、FAQ、制度流程进行切片与向量化
语义检索召回 TopK 相关内容
将检索结果拼接到上下文，提高回答可靠性
👉 解决问题：

❌ 模型幻觉
❌ 知识过时
❌ 无法访问私有数据
可解决：

❌ 回答“看起来对、但不基于企业事实”
❌ 新人找不到知识、查阅成本高
❌ 企业私有知识无法被模型直接利用
3️⃣ MCP 工具调用（Agent能力执行能力）
支持调用外部工具：

🔎 搜索引擎（面试题查询）
📄 PDF解析
🔌 可扩展任意工具（API / DB / 爬虫）
👉 实现“推理 + 执行”闭环

支持接入并调用外部工具：

🔎 搜索与信息查询
📄 文档/PDF 解析
🔌 可扩展企业内部 API、数据库与业务系统
实现“理解问题 → 调用工具 → 返回结果”闭环。

4️⃣ Guardrail 安全机制
对用户输入进行敏感词检测
拦截非法请求
提升系统安全性与可控性
输入内容检测（风险词/违规请求）
异常请求拦截
提升系统安全性、合规性与可控性
5️⃣ 流式响应（SSE）
基于 Server-Sent Events 实现实时输出
提升用户体验（类似 ChatGPT 打字效果）
实时输出生成内容
优化交互体验，降低等待感
🧩 核心设计
🧠 ChatMemory（会话记忆）
自定义实现：

MySQLChatMemory implements ChatMemory
特点：

持久化存储聊天记录
聊天记录持久化
支持多用户隔离
自动裁剪历史消息（maxMessages）
🔍 RAG流程RAG 流程
用户问题
   ↓
向量化
   ↓
向量检索（TopK）
向量数据库检索（TopK）
   ↓
拼接上下文
   ↓
LLM 生成回答
LLM生成答案
🛠️ Agent执行流程Agent 执行流程
用户问题
   ↓
LLM分析意图
   ↓
是否需要调用工具
   ↓
调用 MCP Tool
   ↓
结合结果生成最终回答
用户问题
   ↓
LLM 意图识别
   ↓
判断是否调用工具
   ↓
调用 MCP Tool
   ↓
融合结果输出最终答案
🚀 快速启动
1️⃣ 克隆项目
git clone https://github.com/chenhao467/ai-coding-helper.git
cd ai-coding-helper
2️⃣ 配置数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aicodinghelper?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
3️⃣ 配置大模型 Key
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: your_api_key
4️⃣ 启动后端
mvn spring-boot:run
5️⃣ 启动前端
cd ai-code-helper-frontend
cd frontend
npm install
npm run dev
📡 接口说明
SSE聊天接口SSE 聊天接口
GET /api/ai/chat?memoryId=1&message=你好
返回格式
data: AI返回内容片段
📈 项目亮点
✅ 基于 LangChain4j 构建完整 AI Agent 系统
✅ 自定义 ChatMemory 实现会话持久化与隔离
✅ 引入 RAG 提升回答准确性并降低幻觉
✅ 支持 MCP 工具调用，实现复杂任务处理
✅ 使用 SSE 实现流式响应，提升交互体验
✅ 具备安全控制（Guardrail）机制
🔮 后续优化方向
Agent 聊天接口
POST /api/agent/chat
Content-Type: application/json

{
  "message": "请帮我总结本周项目风险",
  "memoryId": "1",
  "userId": 1,
  "tenantId": 1
}
📈 企业价值亮点
✅ 打通“问答 + 检索 + 工具执行”完整链路
✅ 企业知识可沉淀、可检索、可复用
✅ 支持按会话、用户、租户进行隔离管理
✅ 降低员工检索信息和处理任务的时间成本
✅ 可按业务扩展工具，持续提升自动化能力
🔮 后续规划
🔄 引入 Redis 做缓存层（提升性能）
🔄 引入 Redis 缓存层，提高高并发场景性能
📊 加接入向量数据库（Milvus / pgvector）
🧠 增强 RAG（Rerank / Hybrid Search）
🤖 多Agent协作多 Agent 协作（Planner + Executor）
📈 监控与日志（Tracing）
📈 监控、Tracing 与审计日志能力
📄 License
MIT License

🙋‍♂️ 作者
Chenhao（Java开发Java 开发 / AI Agent方向Agent 方向）
