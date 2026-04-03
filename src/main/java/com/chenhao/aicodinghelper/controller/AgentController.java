package com.chenhao.aicodinghelper.controller;

import com.chenhao.aicodinghelper.agent.orchestrator.ConversationOrchestrator;
import com.chenhao.aicodinghelper.agent.dto.AgentChatRequest;
import com.chenhao.aicodinghelper.agent.vo.AgentChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ConversationOrchestrator conversationOrchestrator;

    @PostMapping("/chat")
    public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest request) {
        return conversationOrchestrator.handle(request);
    }
}
