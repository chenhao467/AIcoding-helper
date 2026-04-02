package com.chenhao.aicodinghelper.repository;

import com.chenhao.aicodinghelper.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByMemoryIdOrderByMessageOrderAsc(Integer memoryId);

    @Modifying
    @Query("DELETE FROM ChatSession m WHERE m.memoryId = ?1")
    void deleteByMemoryId(Integer memoryId);

    @Query("SELECT MAX(m.messageOrder) FROM ChatSession m WHERE m.memoryId = ?1")
    Integer findMaxMessageOrderByMemoryId(Integer memoryId);
}
