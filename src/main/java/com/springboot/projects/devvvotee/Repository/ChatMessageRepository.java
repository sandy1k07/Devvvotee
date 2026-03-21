package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Entity.ChatMessage;
import com.springboot.projects.devvvotee.Entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT DISTINCT c from ChatMessage c
            LEFT JOIN FETCH c.chatEvents e
            where c.chatSession = :chatSession
            ORDER BY c.createdAt ASC, e.sequence ASC
            """)
    List<ChatMessage> findByChatSession(ChatSession chatSession);

}
