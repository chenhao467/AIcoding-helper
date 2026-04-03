package com.chenhao.aicodinghelper.orchestrator;

import com.chenhao.aicodinghelper.common.enums.IntentType;
import com.chenhao.aicodinghelper.common.exception.BizException;
import com.chenhao.aicodinghelper.dto.AgentChatRequest;
import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.intent.IntentClassifier;
import com.chenhao.aicodinghelper.intent.IntentResult;
import com.chenhao.aicodinghelper.service.ToolCallLogService;
import com.chenhao.aicodinghelper.tool.ToolContext;
import com.chenhao.aicodinghelper.tool.ToolExecutor;
import com.chenhao.aicodinghelper.vo.AgentChatResponse;
import com.chenhao.aicodinghelper.vo.KnowledgeAnswerVO;
import com.chenhao.aicodinghelper.vo.TicketDetailVO;
import com.chenhao.aicodinghelper.vo.TicketResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationOrchestrator {

    private final IntentClassifier intentClassifier;
    private final ToolExecutor toolExecutor;
    private final ToolCallLogService toolCallLogService;
    private final ObjectMapper objectMapper;

    public AgentChatResponse handle(AgentChatRequest request) {
        IntentResult result = intentClassifier.classify(request.getMessage());
        ToolContext context = ToolContext.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .memoryId(request.getMemoryId())
                .build();

        try {
            return switch (result.getIntentType()) {
                case CREATE_TICKET -> AgentChatResponse.ok(result.getIntentType().name(), handleCreateTicket(request, context, result.getSlots()));
                case QUERY_TICKET -> AgentChatResponse.ok(result.getIntentType().name(), handleQueryTicket(context, result.getSlots()));
                case UPDATE_TICKET_STATUS -> AgentChatResponse.ok(result.getIntentType().name(), handleUpdateTicketStatus(context, result.getSlots()));
                case KNOWLEDGE_QA -> AgentChatResponse.ok(result.getIntentType().name(), handleKnowledgeQa(request, context));
                case GENERAL_QA -> AgentChatResponse.ok(IntentType.GENERAL_QA.name(),
                        "我是你的业务 Agent 助手。你可以让我创建工单、查询工单进度，或先问 SOP 再执行操作。");
            };
        } catch (Exception ex) {
            logFailure(context, "orchestrator", request, ex.getMessage(), request.getTenantId());
            throw ex;
        }
    }

    private String handleCreateTicket(AgentChatRequest request, ToolContext context, Map<String, String> slots) {
        if (request.getMessage().length() < 8) {
            throw new BizException("描述信息太短，请提供更具体的问题描述后再创建工单。");
        }

        CreateTicketDTO dto = new CreateTicketDTO();
        dto.setTitle(extractTitle(request.getMessage()));
        dto.setDescription(request.getMessage());
        dto.setPriority(slots.getOrDefault("priority", "P3"));

        TicketResultVO ticket = toolExecutor.execute("create_ticket", dto, context);
        logSuccess(context, "create_ticket", dto, ticket, null, context.getTenantId());
        return "已创建工单: " + ticket.getTicketNo() + "，状态: " + ticket.getStatus();
    }

    private String handleQueryTicket(ToolContext context, Map<String, String> slots) {
        String ticketNo = slots.get("ticketNo");
        if (ticketNo == null || ticketNo.isBlank()) {
            throw new BizException("请提供工单号，例如 T20260403001");
        }
        TicketDetailVO detail = toolExecutor.execute("query_ticket", ticketNo, context);
        logSuccess(context, "query_ticket", ticketNo, detail, null, context.getTenantId());
        return String.format("工单%s 当前状态为 %s，优先级 %s，最后更新时间 %s",
                detail.getTicketNo(), detail.getStatus(), detail.getPriority(), detail.getUpdatedAt());
    }

    private String handleUpdateTicketStatus(ToolContext context, Map<String, String> slots) {
        String ticketNo = slots.get("ticketNo");
        if (ticketNo == null || ticketNo.isBlank()) {
            throw new BizException("更新状态需要工单号，请在请求中包含 Txxxxxxxx");
        }

        UpdateTicketStatusDTO dto = new UpdateTicketStatusDTO();
        dto.setTicketNo(ticketNo);
        dto.setTargetStatus(slots.getOrDefault("targetStatus", "PROCESSING"));

        TicketResultVO result = toolExecutor.execute("update_ticket_status", dto, context);
        logSuccess(context, "update_ticket_status", dto, result, null, context.getTenantId());
        return "工单状态已更新: " + result.getTicketNo() + " -> " + result.getStatus();
    }

    private String handleKnowledgeQa(AgentChatRequest request, ToolContext context) {
        KnowledgeAnswerVO answer = toolExecutor.execute("search_kb", request.getMessage(), context);
        logSuccess(context, "search_kb", request.getMessage(), answer, null, context.getTenantId());
        String evidence = answer.getEvidence().isEmpty() ? "" : "\n证据: " + String.join("; ", answer.getEvidence());
        return answer.getAnswer() + evidence;
    }

    private String extractTitle(String message) {
        // 兼容“帮我创建一个xx工单，内容是xxx”这类句式。
        if (message.contains("内容是")) {
            String[] parts = message.split("内容是", 2);
            return parts[0].replace("帮我", "").replace("创建", "").replace("工单", "").trim();
        }
        return message.length() > 30 ? message.substring(0, 30) : message;
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
