package com.chenhao.aicompanyhelper.agent.service;

import com.chenhao.aicompanyhelper.agent.vo.KnowledgeAnswerVO;

public interface KnowledgeService {
    KnowledgeAnswerVO answer(String query, Long tenantId);
}
