package com.chenhao.aicompanyhelper.agent.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenhao.aicompanyhelper.agent.entity.KbArticleEntity;
import com.chenhao.aicompanyhelper.persist.mapper.KbArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HybridRetrievalService {

    private final KbArticleMapper kbArticleMapper;

    /**
     * MVP 的“混合检索”实现：关键词召回 + 简单分数重排。
     * 后续可以替换为向量库 + BM25 + reranker。
     */
    public List<KbArticleEntity> retrieve(String query, Long tenantId, int limit) {
        List<KbArticleEntity> lexical = kbArticleMapper.selectList(new LambdaQueryWrapper<KbArticleEntity>()
                .eq(KbArticleEntity::getTenantId, tenantId)
                .eq(KbArticleEntity::getStatus, "PUBLISHED")
                .and(wrapper -> wrapper.like(KbArticleEntity::getTitle, query)
                        .or().like(KbArticleEntity::getContent, query)
                        .or().like(KbArticleEntity::getTags, query))
                .last("limit 20"));

        return lexical.stream()
                .sorted(Comparator.comparingInt(item -> -score(query, item)))
                .limit(limit)
                .toList();
    }

    private int score(String query, KbArticleEntity item) {
        int score = 0;
        if (item.getTitle() != null && item.getTitle().contains(query)) {
            score += 5;
        }
        if (item.getTags() != null && item.getTags().contains(query)) {
            score += 3;
        }
        if (item.getContent() != null && item.getContent().contains(query)) {
            score += 2;
        }
        return score;
    }
}
