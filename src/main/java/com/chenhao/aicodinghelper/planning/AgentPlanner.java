package com.chenhao.aicodinghelper.planning;

import com.chenhao.aicodinghelper.dto.AgentChatRequest;

public interface AgentPlanner {
    PlanResult plan(AgentChatRequest request);
}
