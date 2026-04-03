package com.chenhao.aicodinghelper.persist.memory;

import com.chenhao.aicodinghelper.agent.entity.ChatSession;
import com.chenhao.aicodinghelper.persist.repository.ChatSessionRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MySQLChatMemory implements ChatMemory {

    private final Object id;
    private final Integer maxMessages;
    private final ChatSessionRepository chatSessionRepository;
    private String currentAiMessageText = "";
    private boolean isStreaming = false;

    public MySQLChatMemory(Object id, Integer maxMessages, ChatSessionRepository chatSessionRepository) {
        this.id = id;
        this.maxMessages = maxMessages;
        this.chatSessionRepository = chatSessionRepository;
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    @Transactional
    public void add(ChatMessage message) {
        log.info("Adding message to memoryId: {}, type: {}", id, message.type());
        
        if (message instanceof AiMessage aiMessage) {
            handleAiMessage(aiMessage);
        } else {
            handleNonAiMessage(message);
        }
    }

    private void handleAiMessage(AiMessage aiMessage) {
        String text = aiMessage.text();
        
        // 检查是否为流式输出的中间token（通过文本长度和是否以空格结尾判断）
        if (text.length() < 10 || text.endsWith(" ") || text.endsWith(",") || text.endsWith(".") || text.endsWith(":")) {
            // 流式输出的中间token，暂存
            currentAiMessageText += text;
            isStreaming = true;
            log.info("Streaming token received: {}", text);
        } else {
            // 完整的AI消息
            if (isStreaming) {
                // 如果之前有流式token，拼接完整消息
                text = currentAiMessageText + text;
                currentAiMessageText = "";
                isStreaming = false;
                log.info("Streaming completed, full message: {}", text);
            }
            
            // 检查是否为重复的AI消息
            if (!isDuplicateAiMessage(text)) {
                saveAiMessage(text);
            }
        }
    }

    private void handleNonAiMessage(ChatMessage message) {
        // 如果有未完成的流式AI消息，先保存
        if (isStreaming && !currentAiMessageText.isEmpty()) {
            saveAiMessage(currentAiMessageText);
            currentAiMessageText = "";
            isStreaming = false;
        }
        
        Integer memoryId = convertToMemoryId(id);
        Integer nextOrder = getNextOrder(memoryId);
        
        ChatSession entity = ChatSession.builder()
                .memoryId(memoryId)
                .messageType(message.type().name())
                .messageText(getTextFromMessage(message))
                .messageOrder(nextOrder)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatSessionRepository.save(entity);
        
        trimIfNecessary(memoryId);
    }

    private void saveAiMessage(String text) {
        Integer memoryId = convertToMemoryId(id);
        Integer nextOrder = getNextOrder(memoryId);
        
        ChatSession entity = ChatSession.builder()
                .memoryId(memoryId)
                .messageType("AI")
                .messageText(text)
                .messageOrder(nextOrder)
                .createdAt(LocalDateTime.now())
                .build();
        
        chatSessionRepository.save(entity);
        
        trimIfNecessary(memoryId);
    }

    private boolean isDuplicateAiMessage(String text) {
        Integer memoryId = convertToMemoryId(id);
        List<ChatSession> entities = chatSessionRepository.findByMemoryIdOrderByMessageOrderDesc(memoryId);
        
        // 检查最近的消息是否也是AI消息且内容相似
        for (ChatSession entity : entities) {
            if ("AI".equals(entity.getMessageType())) {
                // 如果内容包含或被包含，认为是重复
                if (entity.getMessageText().contains(text) || text.contains(entity.getMessageText())) {
                    log.info("Duplicate AI message detected, skipping: {}", text);
                    return true;
                }
                break; // 只检查最近的一条AI消息
            }
        }
        return false;
    }

    @Override
    @Transactional
    public List<ChatMessage> messages() {
        Integer memoryId = convertToMemoryId(id);
        List<ChatSession> entities = chatSessionRepository.findByMemoryIdOrderByMessageOrderAsc(memoryId);
        
        List<ChatMessage> messages = new ArrayList<>();
        for (ChatSession entity : entities) {
            ChatMessage message = convertToLangChainMessage(entity);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    @Override
    @Transactional
    public void clear() {
        log.info("Clearing memory for memoryId: {}", id);
        Integer memoryId = convertToMemoryId(id);
        chatSessionRepository.deleteByMemoryId(memoryId);
        currentAiMessageText = "";
        isStreaming = false;
    }

    private Integer convertToMemoryId(Object id) {
        if (id instanceof Integer) {
            return (Integer) id;
        } else if (id instanceof String) {
            try {
                return Integer.parseInt((String) id);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Integer getNextOrder(Integer memoryId) {
        Integer maxOrder = chatSessionRepository.findMaxMessageOrderByMemoryId(memoryId);
        return maxOrder == null ? 0 : maxOrder + 1;
    }

    private void trimIfNecessary(Integer memoryId) {
        List<ChatSession> entities = chatSessionRepository.findByMemoryIdOrderByMessageOrderAsc(memoryId);
        
        if (entities.size() > maxMessages) {
            int toRemove = entities.size() - maxMessages;
            for (int i = 0; i < toRemove; i++) {
                chatSessionRepository.delete(entities.get(i));
            }
            log.info("Trimmed memory for memoryId: {}, removed {} messages", id, toRemove);
        }
    }

    private String getTextFromMessage(ChatMessage message) {
        if (message instanceof UserMessage userMessage) {
            return userMessage.singleText();
        } else if (message instanceof AiMessage aiMessage) {
            return aiMessage.text();
        } else if (message instanceof SystemMessage systemMessage) {
            return systemMessage.text();
        }
        return "";
    }

    private ChatMessage convertToLangChainMessage(ChatSession entity) {
        if ("USER".equals(entity.getMessageType())) {
            return UserMessage.from(entity.getMessageText());
        } else if ("AI".equals(entity.getMessageType())) {
            return AiMessage.from(entity.getMessageText());
        } else if ("SYSTEM".equals(entity.getMessageType())) {
            return SystemMessage.from(entity.getMessageText());
        }
        return null;
    }
}
