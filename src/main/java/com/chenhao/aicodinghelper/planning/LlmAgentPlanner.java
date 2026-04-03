package com.chenhao.aicodinghelper.planning;

import com.chenhao.aicodinghelper.common.enums.IntentType;
import com.chenhao.aicodinghelper.dto.AgentChatRequest;
import com.chenhao.aicodinghelper.tool.AgentTool;
import com.chenhao.aicodinghelper.tool.ToolExecutor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class LlmAgentPlanner implements AgentPlanner {

    private final ChatModel myQwenChatModel;
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    @Override
    public PlanResult plan(AgentChatRequest request) {
        try {
            String toolManifest = buildToolManifest(toolExecutor.allTools());
            SystemMessage systemMessage = SystemMessage.from(buildSystemPrompt(toolManifest));
            UserMessage userMessage = UserMessage.from(request.getMessage());
            ChatResponse response = myQwenChatModel.chat(systemMessage, userMessage);
            AiMessage aiMessage = response.aiMessage();
            return toValidatedPlan(aiMessage.text(), request.getMessage());
        } catch (Exception ex) {
            log.warn("LLM planner failed, fallback to clarification. reason={}", ex.getMessage());
            return PlanResult.builder()
                    .intentType(IntentType.GENERAL_QA)
                    .action(PlanAction.ASK_CLARIFICATION)
                    .message("我需要更多信息才能执行，请补充你的目标和关键参数（如工单号/状态/问题描述）。")
                    .build();
        }
    }

    private PlanResult toValidatedPlan(String raw, String originalMessage) throws Exception {
        JsonNode root = objectMapper.readTree(extractJson(raw));
        PlanAction action = parseAction(root.path("action").asText(""));
        IntentType intentType = parseIntent(root.path("intent").asText(""));
        String toolName = root.path("tool").asText("");
        Map<String, Object> params = objectMapper.convertValue(root.path("params"), new TypeReference<>() {});
        String answer = root.path("answer").asText("");

        if (action == PlanAction.TOOL_CALL) {
            if (toolName.isBlank() || !toolExecutor.hasTool(toolName)) {
                return cannotHandle(intentType);
            }
            List<String> missing = findMissingParams(toolName, params);
            if (!missing.isEmpty()) {
                return PlanResult.builder()
                        .intentType(intentType)
                        .action(PlanAction.ASK_CLARIFICATION)
                        .message("缺少必要参数：" + String.join("、", missing) + "。请补充后我继续处理。")
                        .build();
            }
            normalizeTicketNo(params);
            return PlanResult.builder()
                    .intentType(intentType)
                    .action(PlanAction.TOOL_CALL)
                    .toolName(toolName)
                    .params(params)
                    .build();
        }

        if (action == PlanAction.CANNOT_HANDLE) {
            return cannotHandle(intentType);
        }

        String message = answer.isBlank() ? "请告诉我你要执行的业务操作。" : answer;
        PlanAction finalAction = action == PlanAction.ASK_CLARIFICATION ? PlanAction.ASK_CLARIFICATION : PlanAction.FINAL_ANSWER;
        return PlanResult.builder()
                .intentType(intentType)
                .action(finalAction)
                .message(message)
                .params(Map.of("message", originalMessage))
                .build();
    }

    private String buildSystemPrompt(String toolManifest) {
        return """
                你是企业级 AI Agent 的 Planner。你只负责输出执行计划 JSON，不要输出解释文本。
                目标：在可调用工具能力内，尽量完成用户请求；缺参数时友好引导；无法完成时明确拒绝。
                                
                可用工具清单：
                %s
                                
                决策规则：
                1) 涉及业务数据查询/创建/修改，优先 action=tool_call。
                2) 缺必要参数时 action=ask_clarification，并在 answer 中明确要补什么。
                3) 仅概念解释时 action=final_answer。
                4) 系统无能力处理时 action=cannot_handle。
                                
                输出 JSON 格式（严格遵守）：
                {
                  "intent":"GENERAL_QA|KNOWLEDGE_QA|CREATE_TICKET|QUERY_TICKET|UPDATE_TICKET_STATUS",
                  "action":"tool_call|final_answer|ask_clarification|cannot_handle",
                  "tool":"工具名或空串",
                  "params":{"k":"v"},
                  "answer":"给用户的回复或追问"
                }
                """.formatted(toolManifest);
    }

    private String buildToolManifest(Iterable<AgentTool<?, ?>> tools) {
        List<String> lines = new ArrayList<>();
        for (AgentTool<?, ?> tool : tools) {
            String required = String.join(", ", tool.requiredParams());
            String hints = tool.parameterHints().entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining("; "));
            lines.add("- %s: %s | required=[%s] | params=%s"
                    .formatted(tool.name(), tool.description(), required, hints));
        }
        return String.join("\n", lines);
    }

    private PlanAction parseAction(String action) {
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "tool_call" -> PlanAction.TOOL_CALL;
            case "final_answer" -> PlanAction.FINAL_ANSWER;
            case "ask_clarification" -> PlanAction.ASK_CLARIFICATION;
            default -> PlanAction.CANNOT_HANDLE;
        };
    }

    private IntentType parseIntent(String intent) {
        try {
            return IntentType.valueOf(intent.toUpperCase(Locale.ROOT));
        } catch (Exception ignore) {
            return IntentType.GENERAL_QA;
        }
    }

    private String extractJson(String raw) {
        String text = raw == null ? "" : raw.trim();
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private List<String> findMissingParams(String toolName, Map<String, Object> params) {
        AgentTool<?, ?> tool = toolExecutor.allTools().stream()
                .filter(t -> t.name().equals(toolName))
                .findFirst()
                .orElse(null);
        if (tool == null) {
            return List.of("tool");
        }
        return tool.requiredParams().stream()
                .filter(p -> params == null || !params.containsKey(p) || String.valueOf(params.get(p)).isBlank())
                .toList();
    }

    private void normalizeTicketNo(Map<String, Object> params) {
        if (params == null || !params.containsKey("ticketNo")) {
            return;
        }
        String ticketNo = String.valueOf(params.get("ticketNo"));
        params.put("ticketNo", ticketNo.toUpperCase(Locale.ROOT));
    }

    private PlanResult cannotHandle(IntentType intentType) {
        return PlanResult.builder()
                .intentType(intentType)
                .action(PlanAction.CANNOT_HANDLE)
                .message("无法处理该请求")
                .build();
    }
}
