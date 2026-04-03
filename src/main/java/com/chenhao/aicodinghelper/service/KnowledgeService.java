package com.chenhao.aicodinghelper.service;

import com.chenhao.aicodinghelper.vo.KnowledgeAnswerVO;

public interface KnowledgeService {
    KnowledgeAnswerVO answer(String query, Long tenantId);
}
