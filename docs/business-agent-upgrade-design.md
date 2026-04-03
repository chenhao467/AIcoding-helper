# AI 对话助手升级为业务 Agent 的工程化方案（MVP → 可扩展）

> 目标：把当前“只能问答”的 AI 助手，升级为“可识别意图 + 可调用工具 + 可执行业务 + 可检索知识 + 可持续演进”的业务 Agent。

---

## 1. 业务场景选择与理由

### 1.1 选择场景：**客服知识库 + 工单执行系统**

我建议优先落地这个场景，原因：

1. **真实且高频**：企业中客服、售后、技术支持都依赖工单流转。
2. **天然适合 AI 接入**：大量自然语言输入（“支付超时”“客户投诉”“如何重置账号”）。
3. **工具调用价值高**：创建工单、分配工单、更新状态、查询进度都可由工具执行。
4. **RAG 与业务强耦合**：知识库（FAQ/SOP/故障处理手册）可显著提高答复准确率。
5. **MVP 易做、扩展性好**：后续可扩展审批、通知、SLA、自动派单、质检等。

### 1.2 为什么适合 AI 工具调用

- “问知识”与“办业务”在同一入口发生：
  - 用户问：*“支付报错 502 怎么处理？”* → 偏知识问答
  - 用户说：*“帮我提个高优工单给支付组”* → 偏业务执行
- AI 需要根据意图决定：
  - 直接回答
  - 先检索再回答
  - 触发工具执行并返回结果

### 1.3 哪些动作适合 AI 执行 vs 必须落地到数据库/外部系统

**适合 AI 决策/编排：**
- 意图识别、槽位提取（标题、优先级、影响范围）
- 缺参追问（如缺少“客户ID”“问题描述”）
- 知识解释与操作建议生成

**必须由后端工具 + DB 落地：**
- 创建/更新/关闭工单
- 工单分配与状态流转
- SLA 记录、审计日志写入
- 对接外部系统（IM 通知、工单平台、监控系统）

---

## 2. 项目整体架构设计

## 2.1 分层架构（文本图）

```text
[用户交互层]
Web/Vue、移动端、企业IM
        │
        ▼
[AI 对话编排层]
ConversationOrchestrator（会话状态机 + 路由）
        │
        ├──► [意图识别层]
        │      IntentClassifier + SlotFiller + PolicyEngine
        │
        ├──► [RAG 检索增强层]
        │      QueryRewrite + MultiRetriever + Reranker + ContextBuilder
        │
        └──► [工具注册与调用层]
               ToolRegistry + ToolRouter + ToolExecutor + ResultNormalizer
                         │
                         ▼
                    [业务服务层]
             TicketService / KnowledgeService / NotificationService
                         │
                         ▼
                     [数据访问层]
                Mapper( MyBatis-Plus ) / Repository
                         │
                         ▼
                      [数据存储层]
       MySQL（业务库）+ Redis（会话缓存/幂等）+ VectorStore（知识向量）
```

## 2.2 各层职责

1. **用户交互层**
   - 接收自然语言
   - 返回流式内容（SSE）
   - 透传 `memoryId/userId/tenantId`

2. **AI 对话编排层（核心）**
   - 决定当前请求属于：普通问答 / 检索问答 / 工具执行
   - 维护多轮状态（是否已收集完整参数）
   - 控制工具调用次数、超时、重试策略

3. **意图识别层**
   - 意图分类：`KNOWLEDGE_QA` / `CREATE_TICKET` / `QUERY_TICKET` / `UPDATE_TICKET`
   - 槽位抽取：`priority`、`title`、`description`、`ticketNo` 等
   - 风险策略（越权、危险操作）

4. **工具注册与调用层**
   - 统一工具协议（入参/出参）
   - 工具白名单、权限控制、参数校验
   - 执行结果标准化，回填模型

5. **业务服务层**
   - 纯业务逻辑，不耦合 LLM
   - 事务、状态机、幂等、审计

6. **数据访问层**
   - MyBatis-Plus Mapper
   - 复杂查询可写 XML / 自定义 SQL

7. **RAG 层**
   - Query 改写、多路召回、重排、上下文压缩
   - 为问答和工具决策提供依据

8. **数据存储层**
   - MySQL：工单主数据、审计日志、工具调用日志
   - Redis：短期会话状态、幂等 token、热点缓存
   - 向量库：知识片段 embedding

---

## 3. 核心模块划分

建议在后端新增/重构模块（包）：

```text
com.xxx.aicodinghelper
├─ controller         // HTTP/SSE 接口
├─ orchestrator       // 对话编排、路由、状态机
├─ intent             // 意图识别、槽位提取、策略
├─ tool               // 工具定义、注册、执行
├─ rag                // 检索、重排、上下文构建
├─ service            // 业务服务接口
├─ service/impl       // 业务实现
├─ mapper             // MyBatis-Plus Mapper
├─ entity             // 实体
├─ dto                // 入参模型
├─ vo                 // 出参模型
├─ config             // LLM、DB、Redis、RAG 配置
├─ init               // 启动初始化（数据库、索引、种子数据）
└─ common             // 枚举、异常、统一响应、工具协议
```

---

## 4. AI 工具调用机制设计

## 4.1 何时普通问答 vs 何时工具调用

### 普通问答
- 问候、解释性问题、无需状态改变：
  - “你是谁？”
  - “SLA 是什么？”

### RAG 问答
- 涉及企业内部知识、SOP、FAQ：
  - “支付超时故障的标准排查步骤是什么？”

### 工具调用
- 有明确“执行动作”或“查真实状态”需求：
  - “帮我创建工单”
  - “查一下 T20260403001 进度”
  - “把这个工单改成紧急并通知值班同学”

## 4.2 工具定义规范（强约束）

建议统一 Tool Schema：

```json
{
  "name": "create_ticket",
  "description": "创建客服工单",
  "input_schema": {
    "type": "object",
    "required": ["title", "priority", "description", "reporterId"],
    "properties": {
      "title": {"type": "string", "maxLength": 120},
      "priority": {"type": "string", "enum": ["P1", "P2", "P3"]},
      "description": {"type": "string", "maxLength": 2000},
      "reporterId": {"type": "long"}
    }
  },
  "output_schema": {
    "type": "object",
    "properties": {
      "success": {"type": "boolean"},
      "ticketNo": {"type": "string"},
      "message": {"type": "string"}
    }
  }
}
```

## 4.3 防止大模型乱调用工具

1. **策略引擎先裁决**：未命中“可执行意图”时禁止工具调用。
2. **白名单工具路由**：不同意图只允许指定工具集合。
3. **参数 JSON Schema 校验**：类型、长度、枚举、必填强校验。
4. **权限校验**：基于 `userId/role/tenantId`。
5. **幂等与频控**：同请求幂等键 + 限流防刷。
6. **危险操作二次确认**：如关闭工单、批量更新。

## 4.4 工具路由与结果回填

链路建议：

1. 用户输入自然语言
2. 意图识别 + 槽位提取
3. 决策器判断是否调用工具
4. 生成结构化参数（可追问补全）
5. `ToolExecutor` 调用 Java Service
6. 业务落库后返回标准结果
7. `ResultNormalizer` 生成机器可读摘要回填模型
8. 模型生成最终用户可读响应

## 4.5 多轮上下文记忆

- **短期会话态（Redis）**：
  - 当前任务类型
  - 已收集槽位
  - 最近一次工具调用结果
- **长期记忆（MySQL）**：
  - 对话历史
  - 用户偏好（默认优先级、常用业务线）

## 4.6 异常处理

- 参数缺失：LLM 发起澄清问题（非直接失败）
- 工具超时：返回“稍后重试”并记录失败原因
- 业务异常：标准错误码映射（如工单不存在、状态非法）
- 外部依赖失败：降级到“仅建议，不执行”

---

## 5. 数据库初始化方案设计

## 5.1 推荐方案：**Flyway + MyBatis-Plus**

- **为什么选 Flyway**：
  - 版本化脚本可审计
  - 与 Spring Boot 集成简单
  - 支持开发/测试/生产统一迁移链路
- **为什么仍用 MyBatis-Plus**：
  - 日常 CRUD 开发效率高
  - 与现有项目技术栈一致

## 5.2 启动时执行初始化

`application.yml` 示例：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    out-of-order: false
```

启动流程：
1. Spring Boot 启动
2. Flyway 扫描 `db/migration`
3. 按版本执行 SQL（如 `V1__init_tables.sql`）
4. 应用启动后可在 `ApplicationRunner` 注入基础字典数据

## 5.3 版本管理与环境隔离

- 开发：允许快速迭代（新增 `Vx__*.sql`）
- 测试：与开发同版本链校验
- 生产：只允许“前向迁移”，禁止手改表
- 严禁修改已上线 migration 文件，新增新版本修复

## 5.4 新增业务平滑演进

- 新字段：先加 nullable 字段 + 回填脚本 + 应用读取兼容
- 再升级：上线后改约束（not null/default）
- 大表变更：分批脚本 + 低峰执行

---

## 6. 数据库表结构设计（5 个核心实体）

## 6.1 表与关系

1. `agent_user`（用户）
2. `kb_article`（知识文档）
3. `ticket`（工单主表）
4. `ticket_action_log`（工单操作日志）
5. `tool_call_log`（工具调用日志）

关系：
- `ticket.reporter_id -> agent_user.id`
- `ticket.assignee_id -> agent_user.id`
- `ticket_action_log.ticket_id -> ticket.id`
- `tool_call_log.ticket_id -> ticket.id (可空)`

## 6.2 建表 SQL 示例

```sql
CREATE TABLE IF NOT EXISTS agent_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_no VARCHAR(64) NOT NULL UNIQUE,
  user_name VARCHAR(64) NOT NULL,
  role_code VARCHAR(32) NOT NULL,
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_no VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  priority VARCHAR(8) NOT NULL,
  status VARCHAR(16) NOT NULL,
  reporter_id BIGINT NOT NULL,
  assignee_id BIGINT NULL,
  source VARCHAR(16) NOT NULL DEFAULT 'AI_AGENT',
  tenant_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_ticket_status_priority (status, priority),
  INDEX idx_ticket_reporter (reporter_id)
);

CREATE TABLE IF NOT EXISTS ticket_action_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ticket_id BIGINT NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  action_detail JSON NULL,
  operator_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_action_ticket (ticket_id)
);

CREATE TABLE IF NOT EXISTS kb_article (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  kb_no VARCHAR(32) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  category VARCHAR(64) NOT NULL,
  tags VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_kb_category (category),
  FULLTEXT INDEX ftx_kb_title_content (title, content)
);

CREATE TABLE IF NOT EXISTS tool_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL,
  tool_name VARCHAR(64) NOT NULL,
  request_json JSON NOT NULL,
  response_json JSON NULL,
  success TINYINT NOT NULL,
  error_code VARCHAR(32) NULL,
  error_message VARCHAR(255) NULL,
  ticket_id BIGINT NULL,
  tenant_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tool_trace (trace_id),
  INDEX idx_tool_name_time (tool_name, created_at)
);
```

---

## 7. Java 实体类与代码结构示例

## 7.1 实体类（MyBatis-Plus）

```java
@Data
@TableName("ticket")
public class TicketEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ticketNo;
    private String title;
    private String description;
    private String priority; // P1/P2/P3
    private String status;   // OPEN/PROCESSING/RESOLVED/CLOSED
    private Long reporterId;
    private Long assigneeId;
    private String source;
    private Long tenantId;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

```java
@Data
@TableName("tool_call_log")
public class ToolCallLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private String toolName;
    private String requestJson;
    private String responseJson;
    private Integer success;
    private String errorCode;
    private String errorMessage;
    private Long ticketId;
    private Long tenantId;
    private LocalDateTime createdAt;
}
```

## 7.2 DTO / VO 示例

```java
@Data
public class CreateTicketDTO {
    @NotBlank
    private String title;
    @NotBlank
    @Pattern(regexp = "P1|P2|P3")
    private String priority;
    @NotBlank
    private String description;
    @NotNull
    private Long reporterId;
    @NotNull
    private Long tenantId;
}
```

```java
@Data
@AllArgsConstructor
public class TicketResultVO {
    private String ticketNo;
    private String status;
    private String message;
}
```

---

## 8. 典型 SQL / Mapper / Service 示例

## 8.1 新增

```sql
INSERT INTO ticket (
  ticket_no, title, description, priority, status,
  reporter_id, assignee_id, source, tenant_id
) VALUES (
  #{ticketNo}, #{title}, #{description}, #{priority}, 'OPEN',
  #{reporterId}, #{assigneeId}, 'AI_AGENT', #{tenantId}
);
```

## 8.2 查询单个工单

```sql
SELECT id, ticket_no, title, priority, status, reporter_id, assignee_id, updated_at
FROM ticket
WHERE ticket_no = #{ticketNo}
  AND tenant_id = #{tenantId}
  AND deleted = 0;
```

## 8.3 更新状态

```sql
UPDATE ticket
SET status = #{targetStatus}, updated_at = NOW()
WHERE ticket_no = #{ticketNo}
  AND tenant_id = #{tenantId}
  AND deleted = 0
  AND status IN ('OPEN', 'PROCESSING');
```

## 8.4 列表查询

```sql
SELECT ticket_no, title, priority, status, assignee_id, updated_at
FROM ticket
WHERE tenant_id = #{tenantId}
  AND deleted = 0
  AND (#{status} IS NULL OR status = #{status})
  AND (#{priority} IS NULL OR priority = #{priority})
ORDER BY updated_at DESC
LIMIT #{offset}, #{size};
```

## 8.5 Service 示例

```java
public interface TicketService {
    TicketResultVO createTicket(CreateTicketDTO dto);
    TicketResultVO changeStatus(String ticketNo, String targetStatus, Long operatorId, Long tenantId);
    TicketDetailVO queryByTicketNo(String ticketNo, Long tenantId);
}
```

```java
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketMapper ticketMapper;
    private final TicketActionLogMapper actionLogMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TicketResultVO createTicket(CreateTicketDTO dto) {
        TicketEntity entity = new TicketEntity();
        entity.setTicketNo(TicketNoGenerator.next());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPriority(dto.getPriority());
        entity.setStatus("OPEN");
        entity.setReporterId(dto.getReporterId());
        entity.setTenantId(dto.getTenantId());
        ticketMapper.insert(entity);
        actionLogMapper.insertCreateLog(entity.getId(), dto.getReporterId());
        return new TicketResultVO(entity.getTicketNo(), entity.getStatus(), "工单创建成功");
    }
}
```

---

## 9. RAG 当前问题分析（典型）

1. 仅向量检索，召回偏语义相似但不一定业务相关
2. chunk 过大/过小导致信息断裂或噪声
3. 无 metadata 过滤（租户、业务线、文档版本）
4. 无重排（rerank），TopK 质量不稳定
5. 没有把检索结果用于“是否调用工具”的决策
6. 知识更新流程滞后（文档更新后 embedding 未及时刷新）
7. 无权限隔离，潜在越权泄露风险

---

## 10. RAG 优化方案（工程可落地）

## 10.1 Chunk 策略

- 采用“结构化切分 + 滑窗”：
  - 优先按标题/小节切分
  - 每块 300~500 中文字，重叠 60~100 字
- 对 SOP/流程文档保留步骤序号，避免步骤丢失

## 10.2 元数据设计

每个 chunk 必带：
- `tenant_id`
- `category`（支付/账户/风控）
- `doc_type`（FAQ/SOP/公告）
- `doc_version`
- `updated_at`
- `permission_tags`（角色可见范围）

## 10.3 检索策略：混合检索 + 多路召回

1. 向量召回（semantic）
2. BM25 关键词召回（lexical）
3. 规则召回（如命中“错误码/工单号/SLA”）
4. 合并去重后进入 reranker

## 10.4 Rerank 与上下文构建

- 使用 reranker 取 TopN（建议 5~8）
- 对过长片段做摘要压缩
- 构建“结论 + 证据片段 + 来源ID”上下文模板

## 10.5 Query Rewrite / Multi Query

- Query Rewrite：纠正口语化表达（“支付挂了”→“支付接口超时/502”）
- Multi Query：生成 2~3 个子查询，提高召回覆盖率

## 10.6 知识分层

- L1：稳定制度类（SLA、流程）
- L2：操作手册类（SOP、Runbook）
- L3：高频问题类（FAQ）
- L4：时效公告类（最近变更）

回答优先级：L4 > L2 > L1 > L3（可按业务调整）

## 10.7 幻觉与权限控制

- 回答必须附来源 chunk_id / doc_id（至少 1 条）
- 无证据时明确说“不确定”，并建议创建工单
- 按 `tenant_id + permission_tags` 做检索前过滤

---

## 11. RAG 与工具调用如何融合

## 11.1 先检索再回答

场景：纯知识问题
- “如何处理支付 502？”

## 11.2 先检索再决定是否调工具

场景：半知识半执行
- “支付超时应该提什么级别工单？”
- 先查 SOP 判断优先级，再询问是否立即创建工单

## 11.3 直接工具调用（无需检索）

场景：强执行指令
- “查询工单 T20260403001”
- “把 T20260403001 关闭”

## 11.4 知识型工具 vs 执行型工具边界

- **知识型工具**：`search_kb`, `get_sop_by_error_code`
- **执行型工具**：`create_ticket`, `update_ticket_status`, `assign_ticket`

原则：
- 只读解释 → 知识型
- 产生状态变化 → 执行型（必须审计）

## 11.5 优化后 RAG 流程图式描述

```text
用户问题
  ↓
Query Rewrite
  ↓
Multi Query 生成
  ↓
多路召回（向量 + BM25 + 规则）
  ↓
Metadata 过滤（租户/权限/业务线）
  ↓
Rerank 重排
  ↓
Context Builder（证据压缩 + 来源标注）
  ↓
Agent 决策：直接回答 / 调用工具
  ↓
最终回复（含证据或执行结果）
```

---

## 12. 完整业务闭环示例（3 条）

## 示例 A：创建类操作

**用户输入**：
“帮我创建一个高优先级工单，内容是支付接口偶发超时，影响华东用户。”

**AI 识别**：
- 意图：`CREATE_TICKET`
- 是否调用工具：是
- 工具：`create_ticket`
- 参数：
  - `title`: 支付接口偶发超时
  - `priority`: P1
  - `description`: 影响华东用户，偶发超时
  - `reporterId`: 10086

**后端执行**：
- `ticket` 插入 1 条
- `ticket_action_log` 插入“CREATE”日志
- 返回 `ticketNo`

**AI 回复**：
“已创建高优先级工单，工单号为 **T20260403001**，当前状态为 **OPEN**。我可以继续帮你通知值班同学。”

## 示例 B：查询类操作

**用户输入**：
“查一下工单 T20260403001 现在处理到哪一步了？”

**AI 识别**：
- 意图：`QUERY_TICKET`
- 是否调用工具：是
- 工具：`query_ticket`
- 参数：`ticketNo=T20260403001`

**后端执行**：
- 查询 `ticket` + 最新 `ticket_action_log`
- 返回状态 `PROCESSING`，负责人 `u1024`

**AI 回复**：
“工单 **T20260403001** 当前状态为 **PROCESSING**，负责人是 **u1024**，最近一次操作是 2026-04-03 14:22 的‘已受理’。”

## 示例 C：知识问答 + 工具联动

**用户输入**：
“支付 502 一般怎么处理？如果符合条件就顺便帮我提单。”

**AI 识别与链路**：
1. 意图初判：`KNOWLEDGE_QA + POSSIBLE_CREATE`
2. 先调用知识型工具 `search_kb`（检索 SOP）
3. 得到结论：
   - 若影响 > 30% 用户且持续 10 分钟，优先级应为 P1
4. AI 追问：
   - “请确认当前影响范围是否超过 30% 用户？”
5. 用户确认“是”后
6. 调执行型工具 `create_ticket`

**AI 最终回复**：
“根据《支付网关故障SOP v3.2》，该情况建议按 **P1** 处理。已为你创建工单 **T20260403009**，并记录故障判定依据。”

---

## 13. 项目实施顺序建议（MVP 优先）

## Phase 1（1~2 周）先跑通闭环

1. 定义 3 个执行型工具：`create_ticket` / `query_ticket` / `update_ticket_status`
2. 完成核心表结构 + Flyway 初始化
3. 接通对话编排层（意图识别 + 工具路由）
4. 最小可用 RAG（向量检索 + 来源返回）

> **MVP 说明**：先保证“能稳定创建/查询/更新工单”比追求复杂多 Agent 更关键。

## Phase 2（2~4 周）提升准确率与可控性

1. 混合检索 + rerank
2. 参数缺失追问与多轮槽位补全
3. 工具调用审计与失败重试
4. 增加权限模型（租户 + 角色）

## Phase 3（持续演进）

1. 自动派单（规则/模型）
2. SLA 超时预警与升级
3. 多工具编排（通知、日历、审批）
4. 通用 Tool SDK，向通用业务 Agent 扩展

---

## 14. 后续扩展建议

1. **通用 Tool SDK**：统一 `ToolRequest<T>` / `ToolResponse<R>`，降低新工具接入成本。
2. **可视化编排台**：让产品/运营配置“意图→工具链→策略”。
3. **观测体系**：
   - LLM 指标（意图准确率、工具成功率）
   - RAG 指标（召回率、命中率、无证据回答率）
   - 业务指标（工单创建时延、一次解决率）
4. **安全合规**：脱敏、审计、敏感操作审批、提示词注入防护。
5. **A/B 机制**：不同检索策略和提示词在线对比，持续优化。

---

## 15. 补充：工具接口 Java 风格定义（可直接参考）

```java
public interface AgentTool<I, O> {
    String name();
    Class<I> inputType();
    O execute(I input, ToolContext context);
}

@Data
public class ToolContext {
    private String traceId;
    private Long userId;
    private Long tenantId;
    private String memoryId;
}
```

```java
@Component
public class CreateTicketTool implements AgentTool<CreateTicketDTO, TicketResultVO> {
    private final TicketService ticketService;

    @Override
    public String name() {
        return "create_ticket";
    }

    @Override
    public Class<CreateTicketDTO> inputType() {
        return CreateTicketDTO.class;
    }

    @Override
    public TicketResultVO execute(CreateTicketDTO input, ToolContext context) {
        input.setReporterId(context.getUserId());
        input.setTenantId(context.getTenantId());
        return ticketService.createTicket(input);
    }
}
```

---

该方案兼顾了“先能跑起来（MVP）”与“后续可扩展（通用业务 Agent）”，你可以直接把本设计作为立项说明 + 开发蓝图使用。
