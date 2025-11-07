package com.eadgequry.chat_bot_service.repository;

import com.eadgequry.chat_bot_service.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Conversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<Conversation> findByUserIdAndDatabaseConfigIdOrderByCreatedAtDesc(Long userId, Long databaseConfigId);
}
