package com.chenhao.aicompanyhelper.agent.tool;

import com.chenhao.aicompanyhelper.agent.service.KnowledgeService;
import com.chenhao.aicompanyhelper.agent.vo.KnowledgeAnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchKnowledgeTool implements AgentTool<String, KnowledgeAnswerVO> {

    private final KnowledgeService knowledgeService;

    @Override
    public String name() {
        return "search_kb";
    }

    @Override
    public String description() {
        return "检索知识库并返回标准处理建议与证据。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("query");
    }

    @Override
    public Map<String, String> parameterHints() {
        return Map.of("query", "知识检索关键词或问题描述");
    }

    @Override
    public Class<String> inputType() {
        return String.class;
    }

    @Override
    public KnowledgeAnswerVO execute(String input, ToolContext context) {
        return knowledgeService.answer(input, context.getTenantId());
    }
}
