package com.chenhao.aicodinghelper.planning;

import com.chenhao.aicodinghelper.common.enums.IntentType;
import com.chenhao.aicodinghelper.dto.AgentChatRequest;
import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.intent.IntentClassifier;
import com.chenhao.aicodinghelper.intent.IntentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RuleBasedAgentPlanner implements AgentPlanner {

    private final IntentClassifier intentClassifier;

    @Override
    public PlanResult plan(AgentChatRequest request) {
        IntentResult intentResult = intentClassifier.classify(request.getMessage());
        Map<String, String> slots = intentResult.getSlots();

        return switch (intentResult.getIntentType()) {
            case CREATE_TICKET -> planCreateTicket(request, slots);
            case QUERY_TICKET -> planQueryTicket(slots);
            case UPDATE_TICKET_STATUS -> planUpdateTicket(slots);
            case KNOWLEDGE_QA -> PlanResult.builder()
                    .intentType(IntentType.KNOWLEDGE_QA)
                    .action(PlanAction.TOOL_CALL)
                    .toolName("search_kb")
                    .params(Map.of("query", request.getMessage()))
                    .build();
            case GENERAL_QA -> planGeneralQa(request.getMessage());
        };
    }

    private PlanResult planCreateTicket(AgentChatRequest request, Map<String, String> slots) {
        if (request.getMessage().length() < 8) {
            return PlanResult.builder()
                    .intentType(IntentType.CREATE_TICKET)
                    .action(PlanAction.ASK_CLARIFICATION)
                    .message("请补充更具体的问题描述（至少 8 个字符）后我再帮你创建工单。")
                    .build();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("title", extractTitle(request.getMessage()));
        params.put("description", request.getMessage());
        params.put("priority", slots.getOrDefault("priority", "P3"));

        return PlanResult.builder()
                .intentType(IntentType.CREATE_TICKET)
                .action(PlanAction.TOOL_CALL)
                .toolName("create_ticket")
                .params(params)
                .build();
    }

    private PlanResult planQueryTicket(Map<String, String> slots) {
        String ticketNo = slots.get("ticketNo");
        if (ticketNo == null || ticketNo.isBlank()) {
            return PlanResult.builder()
                    .intentType(IntentType.QUERY_TICKET)
                    .action(PlanAction.ASK_CLARIFICATION)
                    .message("缺少必要参数：请提供工单号，例如 T20260403001")
                    .build();
        }

        return PlanResult.builder()
                .intentType(IntentType.QUERY_TICKET)
                .action(PlanAction.TOOL_CALL)
                .toolName("query_ticket")
                .params(Map.of("ticketNo", ticketNo))
                .build();
    }

    private PlanResult planUpdateTicket(Map<String, String> slots) {
        String ticketNo = slots.get("ticketNo");
        if (ticketNo == null || ticketNo.isBlank()) {
            return PlanResult.builder()
                    .intentType(IntentType.UPDATE_TICKET_STATUS)
                    .action(PlanAction.ASK_CLARIFICATION)
                    .message("缺少必要参数：更新状态需要工单号，请在请求中包含 Txxxxxxxx")
                    .build();
        }

        return PlanResult.builder()
                .intentType(IntentType.UPDATE_TICKET_STATUS)
                .action(PlanAction.TOOL_CALL)
                .toolName("update_ticket_status")
                .params(Map.of(
                        "ticketNo", ticketNo,
                        "targetStatus", slots.getOrDefault("targetStatus", "PROCESSING")
                ))
                .build();
    }

    private PlanResult planGeneralQa(String message) {
        String lower = message.toLowerCase();
        boolean maybeBusinessOperation = lower.contains("创建")
                || lower.contains("修改")
                || lower.contains("删除")
                || lower.contains("查询")
                || lower.contains("订单")
                || lower.contains("用户")
                || lower.contains("金额");

        if (maybeBusinessOperation) {
            return PlanResult.builder()
                    .intentType(IntentType.GENERAL_QA)
                    .action(PlanAction.CANNOT_HANDLE)
                    .message("无法处理该请求")
                    .build();
        }

        return PlanResult.builder()
                .intentType(IntentType.GENERAL_QA)
                .action(PlanAction.FINAL_ANSWER)
                .message("我是你的业务 Agent 助手。你可以让我创建工单、查询工单进度、更新工单状态，或进行知识检索。")
                .build();
    }

    private String extractTitle(String message) {
        if (message.contains("内容是")) {
            String[] parts = message.split("内容是", 2);
            return parts[0].replace("帮我", "").replace("创建", "").replace("工单", "").trim();
        }
        return message.length() > 30 ? message.substring(0, 30) : message;
    }
}
