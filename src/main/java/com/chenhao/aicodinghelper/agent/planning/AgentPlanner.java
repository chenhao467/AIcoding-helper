package com.chenhao.aicodinghelper.agent.planning;

import com.chenhao.aicodinghelper.agent.dto.AgentChatRequest;

public interface AgentPlanner {
    PlanResult plan(AgentChatRequest request);
}
