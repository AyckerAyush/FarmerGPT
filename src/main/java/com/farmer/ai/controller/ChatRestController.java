package com.farmer.ai.controller;

import com.farmer.ai.model.ChatSession;
import com.farmer.ai.model.Message;
import com.farmer.ai.model.User;
import com.farmer.ai.repository.ChatSessionRepository;
import com.farmer.ai.repository.MessageRepository;
import com.farmer.ai.repository.UserRepository;
import com.farmer.ai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private ChatSessionRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIService aiService;

    @PostMapping("/new")
    public ResponseEntity<ChatSession> CreateChat(@RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        ChatSession chat = new ChatSession();
        chat.setTitle(title);
        chat.setUser(user);
        return ResponseEntity.ok(chatRepository.save(chat));
    }

    @GetMapping
    public ResponseEntity<List<ChatSession>> getUserChats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(chatRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSession> getChat(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Optional<ChatSession> chat = chatRepository.findById(id);

        if (chat.isPresent() && chat.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(chat.get());
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping("/{id}/message")
    public ResponseEntity<Message> sendMessage(@PathVariable Long id,
            @RequestParam(value = "content", required = false, defaultValue = "") String content,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        Optional<ChatSession> chatOpt = chatRepository.findById(id);

        if (chatOpt.isPresent() && chatOpt.get().getUser().getId().equals(user.getId())) {
            ChatSession chat = chatOpt.get();

            // Save User Message
            Message userMsg = new Message();
            userMsg.setContent(content);
            userMsg.setSender(Message.SenderType.USER);
            userMsg.setChatSession(chat);

            // Handle File Upload
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = "uploads/";
                    java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    java.nio.file.Path filePath = uploadPath.resolve(fileName);
                    java.nio.file.Files.copy(file.getInputStream(), filePath,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    userMsg.setImageUrl(uploadDir + fileName); // Save relative path
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }

            messageRepository.save(userMsg);

            // Refetch chat to get specific message list
            List<Message> context = chat.getMessages();
            // Depending on lazy loading/persistence context, we might need to manually add
            // the new msg to list if not refreshed
            if (!context.contains(userMsg)) {
                context.add(userMsg);
            }

            // Get AI Response
            String aiResponse = aiService.getResponse(content, context);

            // Save AI Message
            Message aiMsg = new Message();
            aiMsg.setContent(aiResponse);
            aiMsg.setSender(Message.SenderType.AI);
            aiMsg.setChatSession(chat);
            messageRepository.save(aiMsg);

            return ResponseEntity.ok(aiMsg);
        }
        return ResponseEntity.status(403).build();
    }
}
