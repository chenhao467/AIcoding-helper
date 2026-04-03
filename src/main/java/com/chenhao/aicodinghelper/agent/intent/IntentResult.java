package com.chenhao.aicodinghelper.agent.intent;

import com.chenhao.aicodinghelper.agent.common.enums.IntentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class IntentResult {
    private IntentType intentType;
    private Map<String, String> slots;
}
