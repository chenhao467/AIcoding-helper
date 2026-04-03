package com.chenhao.aicodinghelper.agent.service;

import com.chenhao.aicodinghelper.agent.vo.KnowledgeAnswerVO;

public interface KnowledgeService {
    KnowledgeAnswerVO answer(String query, Long tenantId);
}
