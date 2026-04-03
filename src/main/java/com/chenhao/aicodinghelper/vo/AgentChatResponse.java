package com.chenhao.aicodinghelper.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentChatResponse {
    private boolean success;
    private String intent;
    private String reply;
    public AgentChatResponse(boolean success,String intent,String reply){
        this.success = success;
        this.intent = intent;
        this.reply = reply;
    }

    public static AgentChatResponse ok(String intent, String reply) {
        return AgentChatResponse.builder().success(true).intent(intent).reply(reply).build();
    }

    public static AgentChatResponse failed(String message) {
        return AgentChatResponse.builder().success(false).intent("ERROR").reply(message).build();
    }
}
