package com.chenhao.aicodinghelper.agent.planning;

import com.chenhao.aicodinghelper.agent.common.enums.IntentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PlanResult {
    private IntentType intentType;
    private PlanAction action;
    private String toolName;
    private Map<String, Object> params;
    private String message;
}
