package com.farmer.ai.controller;

import com.farmer.ai.model.ChatSession;
import com.farmer.ai.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat"; // Returns chat.html
    }

    @GetMapping("/chat/share/{token}")
    public String sharedChat(@PathVariable String token, Model model) {
        ChatSession chat = chatSessionRepository.findByShareToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Share Token"));

        model.addAttribute("chat", chat);
        return "shared_chat"; // Returns shared_chat.html (Read only)
    }
}
