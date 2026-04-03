package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.service.KnowledgeService;
import com.chenhao.aicodinghelper.vo.KnowledgeAnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchKnowledgeTool implements AgentTool<String, KnowledgeAnswerVO> {

    private final KnowledgeService knowledgeService;

    @Override
    public String name() {
        return "search_kb";
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
