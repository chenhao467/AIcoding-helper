package com.chenhao.aicodinghelper.agent.tool;

import com.chenhao.aicodinghelper.agent.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolExecutor {

    private final Map<String, AgentTool<?, ?>> tools = new HashMap<>();

    public ToolExecutor(List<AgentTool<?, ?>> toolList) {
        for (AgentTool<?, ?> tool : toolList) {
            tools.put(tool.name(), tool);
        }
    }

    @SuppressWarnings("unchecked")
    public <I, O> O execute(String toolName, I input, ToolContext context) {
        AgentTool<I, O> tool = (AgentTool<I, O>) tools.get(toolName);
        if (tool == null) {
            throw new BizException("工具不存在: " + toolName);
        }
        return tool.execute(input, context);
    }

    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }

    public Collection<AgentTool<?, ?>> allTools() {
        return Collections.unmodifiableCollection(tools.values());
    }
}
