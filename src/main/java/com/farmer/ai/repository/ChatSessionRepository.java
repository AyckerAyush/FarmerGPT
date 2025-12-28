package com.farmer.ai.repository;

import com.farmer.ai.model.ChatSession;
import com.farmer.ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    Optional<ChatSession> findByShareToken(String shareToken);
}
