package com.chenhao.aicompanyhelper.agent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "memory_id", nullable = false)
    private Integer memoryId;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(name = "message_order", nullable = false)
    private Integer messageOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
