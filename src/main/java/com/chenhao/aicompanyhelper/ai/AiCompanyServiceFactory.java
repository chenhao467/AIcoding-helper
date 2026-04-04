package com.chenhao.aicompanyhelper.ai;

import com.chenhao.aicompanyhelper.ai.tools.InterviewQuestionTool;
import com.chenhao.aicompanyhelper.persist.memory.MySQLChatMemory;
import com.chenhao.aicompanyhelper.persist.repository.ChatSessionRepository;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCompanyServiceFactory {


    @Resource
    private ChatModel myQwenChatModel;
    @Resource
    private ContentRetriever contentRetriever;
    @Resource
    private McpToolProvider mcpToolProvider;
    @Resource
    private StreamingChatModel qwenStreamingChatModel;
    @Resource
    private ChatSessionRepository chatSessionRepository;
    
    @Bean
    public AiCompanyHelperService aiCodeHelperService() {
        ChatMemory chatMemory = new MySQLChatMemory(0, 10, chatSessionRepository);
        AiCompanyHelperService aiCompanyHelperService = AiServices.builder(AiCompanyHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> new MySQLChatMemory(memoryId, 10, chatSessionRepository))
                .tools(new InterviewQuestionTool())
                .toolProvider(mcpToolProvider)
                .build();
        return aiCompanyHelperService;
    }
}