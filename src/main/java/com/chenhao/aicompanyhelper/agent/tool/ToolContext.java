package com.chenhao.aicompanyhelper.agent.tool;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ToolContext {
    private String traceId;
    private Long userId;
    private Long tenantId;
    private String memoryId;
}
