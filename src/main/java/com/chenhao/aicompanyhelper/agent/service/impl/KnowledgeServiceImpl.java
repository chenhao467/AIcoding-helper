package com.chenhao.aicompanyhelper.agent.service.impl;

import com.chenhao.aicompanyhelper.agent.entity.KbArticleEntity;
import com.chenhao.aicompanyhelper.agent.rag.HybridRetrievalService;
import com.chenhao.aicompanyhelper.agent.service.KnowledgeService;
import com.chenhao.aicompanyhelper.agent.vo.KnowledgeAnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final HybridRetrievalService hybridRetrievalService;

    @Override
    public KnowledgeAnswerVO answer(String query, Long tenantId) {
        List<KbArticleEntity> articles = hybridRetrievalService.retrieve(query, tenantId, 3);
        if (articles.isEmpty()) {
            return new KnowledgeAnswerVO("未检索到可用知识，请补充上下文或直接创建工单。", List.of());
        }

        String answer = "根据知识库，建议优先执行：" + articles.getFirst().getContent();
        List<String> evidence = articles.stream()
                .map(item -> item.getKbNo() + " - " + item.getTitle())
                .toList();
        return new KnowledgeAnswerVO(answer, evidence);
    }
}
