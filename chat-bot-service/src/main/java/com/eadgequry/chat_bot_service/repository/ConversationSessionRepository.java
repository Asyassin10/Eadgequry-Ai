package com.eadgequry.chat_bot_service.repository;

import com.eadgequry.chat_bot_service.model.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    Optional<ConversationSession> findBySessionId(String sessionId);

    List<ConversationSession> findByUserIdAndIsActiveTrueOrderByLastActivityAtDesc(Long userId);

    List<ConversationSession> findByUserIdOrderByLastActivityAtDesc(Long userId);
}
