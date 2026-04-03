package com.chenhao.aicodinghelper.tool;

public interface AgentTool<I, O> {
    String name();

    Class<I> inputType();

    O execute(I input, ToolContext context);
}
