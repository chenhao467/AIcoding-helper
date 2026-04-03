package com.chenhao.aicodinghelper.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KnowledgeAnswerVO {
    private String answer;
    private List<String> evidence;
}
