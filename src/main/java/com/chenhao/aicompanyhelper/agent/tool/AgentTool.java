package com.chenhao.aicompanyhelper.agent.tool;

import java.util.List;
import java.util.Map;

public interface AgentTool<I, O> {
    String name();

    String description();

    List<String> requiredParams();

    Map<String, String> parameterHints();

    Class<I> inputType();

    O execute(I input, ToolContext context);
}
