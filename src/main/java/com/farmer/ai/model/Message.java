package com.farmer.ai.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl; // Stores path to uploaded image

    @Enumerated(EnumType.STRING)
    private SenderType sender; // USER or AI

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "chat_session_id", nullable = false)
    @JsonIgnore
    private ChatSession chatSession;

    public enum SenderType {
        USER, AI
    }
}
