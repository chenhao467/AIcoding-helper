package com.chenhao.aicodinghelper.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentChatRequest {
    @NotBlank
    private String message;
    private Long userId = 1L;
    private Long tenantId = 1L;
    private String memoryId = "default";
}
