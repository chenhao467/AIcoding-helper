package com.chenhao.aicodinghelper.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AgentChatResponse {
    private boolean success;
    private String intent;
    /**
     * action: tool_call / final_answer
     */
    private String action;
    private String tool;
    private Map<String, Object> params;
    private String reply;

    public AgentChatResponse(boolean success, String intent, String reply) {
        this.success = success;
        this.intent = intent;
        this.reply = reply;
    }

    public static AgentChatResponse ok(String intent, String reply) {
        return AgentChatResponse.builder()
                .success(true)
                .intent(intent)
                .action("final_answer")
                .reply(reply)
                .build();
    }

    public static AgentChatResponse finalAnswer(String intent, String reply) {
        return AgentChatResponse.builder()
                .success(true)
                .intent(intent)
                .action("final_answer")
                .reply(reply)
                .build();
    }

    public static AgentChatResponse toolCall(String intent, String tool, Map<String, Object> params, String reply) {
        return AgentChatResponse.builder()
                .success(true)
                .intent(intent)
                .action("tool_call")
                .tool(tool)
                .params(params)
                .reply(reply)
                .build();
    }

    public static AgentChatResponse failed(String message) {
        return AgentChatResponse.builder()
                .success(false)
                .intent("ERROR")
                .action("final_answer")
                .reply(message)
                .build();
    }
}
