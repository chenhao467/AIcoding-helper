package com.chenhao.aicompanyhelper.agent.orchestrator;

import com.chenhao.aicompanyhelper.agent.common.enums.IntentType;
import com.chenhao.aicompanyhelper.agent.planning.LlmAgentPlanner;
import com.chenhao.aicompanyhelper.agent.planning.PlanAction;
import com.chenhao.aicompanyhelper.agent.planning.PlanResult;
import com.chenhao.aicompanyhelper.agent.tool.ToolExecutor;
import com.chenhao.aicompanyhelper.agent.dto.AgentChatRequest;
import com.chenhao.aicompanyhelper.agent.dto.CreateTicketDTO;
import com.chenhao.aicompanyhelper.agent.dto.UpdateTicketStatusDTO;
import com.chenhao.aicompanyhelper.agent.service.ToolCallLogService;
import com.chenhao.aicompanyhelper.agent.tool.ToolContext;
import com.chenhao.aicompanyhelper.agent.vo.AgentChatResponse;
import com.chenhao.aicompanyhelper.agent.vo.KnowledgeAnswerVO;
import com.chenhao.aicompanyhelper.agent.vo.TicketDetailVO;
import com.chenhao.aicompanyhelper.agent.vo.TicketResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationOrchestrator {

    private final LlmAgentPlanner agentPlanner;
    private final ToolExecutor toolExecutor;
    private final ToolCallLogService toolCallLogService;
    private final ObjectMapper objectMapper;
    //意图识别 → 工具路由执行 → 结果回填答复
    public AgentChatResponse handle(AgentChatRequest request) {
        PlanResult planResult = agentPlanner.plan(request);
        ToolContext context = ToolContext.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .memoryId(request.getMemoryId())
                .build();

        try {
            if (planResult.getAction() == PlanAction.ASK_CLARIFICATION || planResult.getAction() == PlanAction.FINAL_ANSWER) {
                return AgentChatResponse.finalAnswer(planResult.getIntentType().name(), planResult.getMessage());
            }
            if (planResult.getAction() == PlanAction.CANNOT_HANDLE) {
                return AgentChatResponse.finalAnswer(planResult.getIntentType().name(), "无法处理该请求");
            }
            return executeToolPlan(planResult, context);
        } catch (Exception ex) {
            logFailure(context, "orchestrator", request, ex.getMessage(), request.getTenantId());
            return AgentChatResponse.finalAnswer(planResult.getIntentType().name(), ex.getMessage());

        }
    }

    private AgentChatResponse executeToolPlan(PlanResult planResult, ToolContext context) {
        String toolName = planResult.getToolName();
        Map<String, Object> params = planResult.getParams();
        if (toolName == null || toolName.isBlank()) {
            return AgentChatResponse.finalAnswer(IntentType.GENERAL_QA.name(), "无法处理该请求");
        }

        switch (toolName) {
            case "create_ticket" -> {
                CreateTicketDTO dto = new CreateTicketDTO();
                dto.setTitle(String.valueOf(params.get("title")));
                dto.setDescription(String.valueOf(params.get("description")));
                dto.setPriority(String.valueOf(params.getOrDefault("priority", "P3")));
                TicketResultVO ticket = toolExecutor.execute(toolName, dto, context);
                logSuccess(context, toolName, dto, ticket, null, context.getTenantId());
                return AgentChatResponse.toolCall(planResult.getIntentType().name(), toolName, params,
                        "已创建工单: " + ticket.getTicketNo() + "，状态: " + ticket.getStatus());
            }
            case "query_ticket" -> {
                String ticketNo = String.valueOf(params.get("ticketNo"));
                TicketDetailVO detail = toolExecutor.execute(toolName, ticketNo, context);
                logSuccess(context, toolName, ticketNo, detail, null, context.getTenantId());
                return AgentChatResponse.toolCall(planResult.getIntentType().name(), toolName, params,
                        String.format("工单%s 当前状态为 %s，优先级 %s，最后更新时间 %s",
                                detail.getTicketNo(), detail.getStatus(), detail.getPriority(), detail.getUpdatedAt()));
            }
            case "update_ticket_status" -> {
                UpdateTicketStatusDTO dto = new UpdateTicketStatusDTO();
                dto.setTicketNo(String.valueOf(params.get("ticketNo")));
                dto.setTargetStatus(String.valueOf(params.get("targetStatus")));
                TicketResultVO result = toolExecutor.execute(toolName, dto, context);
                logSuccess(context, toolName, dto, result, null, context.getTenantId());
                return AgentChatResponse.toolCall(planResult.getIntentType().name(), toolName, params,
                        "工单状态已更新: " + result.getTicketNo() + " -> " + result.getStatus());
            }
            case "search_kb" -> {
                String query = String.valueOf(params.get("query"));
                KnowledgeAnswerVO answer = toolExecutor.execute(toolName, query, context);
                logSuccess(context, toolName, query, answer, null, context.getTenantId());
                String evidence = answer.getEvidence().isEmpty() ? "" : "\n证据: " + String.join("; ", answer.getEvidence());
                return AgentChatResponse.toolCall(planResult.getIntentType().name(), toolName, params,
                        answer.getAnswer() + evidence);
            }
            default -> {
                return AgentChatResponse.finalAnswer(planResult.getIntentType().name(), "无法处理该请求");
            }
        }
    }

    private void logSuccess(ToolContext context, String toolName, Object request, Object response, Long ticketId, Long tenantId) {
        toolCallLogService.saveSuccess(context.getTraceId(), toolName, toJson(request), toJson(response), ticketId, tenantId);
    }

    private void logFailure(ToolContext context, String toolName, Object request, String errorMessage, Long tenantId) {
        toolCallLogService.saveFailure(context.getTraceId(), toolName, toJson(request), "TOOL_EXECUTION_ERROR", errorMessage, tenantId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
