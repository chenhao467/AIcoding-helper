package com.chenhao.aicompanyhelper.agent.planning;

import com.chenhao.aicompanyhelper.agent.dto.AgentChatRequest;

public interface AgentPlanner {
    PlanResult plan(AgentChatRequest request);
}
