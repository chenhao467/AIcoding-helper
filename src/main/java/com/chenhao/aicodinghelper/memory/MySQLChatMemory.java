package com.chenhao.aicodinghelper.memory;

import com.chenhao.aicodinghelper.entity.ChatSession;
import com.chenhao.aicodinghelper.repository.ChatSessionRepository;
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
import java.util.Objects;

@Slf4j
public class MySQLChatMemory implements ChatMemory {

    private final Object id;
    private final Integer maxMessages;
    private final ChatSessionRepository chatSessionRepository;

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
