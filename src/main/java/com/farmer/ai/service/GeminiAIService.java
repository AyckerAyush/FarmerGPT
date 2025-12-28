package com.farmer.ai.service;

import com.farmer.ai.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.context.annotation.Primary;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

@Service
@Primary
public class GeminiAIService implements AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GeminiAIService.class);

    @Override
    public String getResponse(String userQuery, List<Message> context) {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare Request Body
        Map<String, Object> requestBody = new HashMap<>();

        // System Instruction (v1beta)
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("text",
                "You are an expert agricultural AI assistant named FarmerGPT. Your goal is to help farmers with accurate, practical advice on crops, livestock, pests, diseases, and weather. "
                        + "1. STRICTLY answer ONLY queries related to farming, agriculture, gardening, and rural living. "
                        + "2. If a user asks about non-farming topics (e.g., coding, politics, movies), politey decline and ask them to ask a farming question. "
                        + "3. If an image is provided, analyze it deeply for plant diseases, pest identification, or crop health issues."
                        + "4. Be concise, helpful, and friendly.");
        systemInstruction.put("parts", Collections.singletonList(partsMap));
        requestBody.put("system_instruction", systemInstruction);

        List<Map<String, Object>> contents = new ArrayList<>();

        // Add history (Last 5 messages)
        int start = Math.max(0, context.size() - 5);
        for (int i = start; i < context.size(); i++) {
            Message msg = context.get(i);
            if (msg.getContent() == null && msg.getImageUrl() == null)
                continue;

            Map<String, Object> historyPart = new HashMap<>();
            historyPart.put("role", msg.getSender() == Message.SenderType.USER ? "user" : "model");

            List<Map<String, Object>> historyParts = new ArrayList<>();
            if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                Map<String, Object> textP = new HashMap<>();
                textP.put("text", msg.getContent());
                historyParts.add(textP);
            }
            // Note: We are not sending historical images to save bandwidth/tokens, only
            // text history

            historyPart.put("parts", historyParts);
            contents.add(historyPart);
        }

        // Current Interaction (User + Image)
        // We need to verify if the LAST item in contents is the USER message we just
        // added or if we need to append to it
        // The list 'context' includes the current message at the end.
        // So the loop above HAS ALREADY added the current message text.
        // We just need to find that last "user" block and attach the image to it if it
        // exists.

        // However, simpler approach: Don't add the last message in the loop. Handle it
        // explicitly.

        // REWRITE LOOP to exclude last message
        contents.clear(); // Reset from above simplified loop logic
        for (int i = start; i < context.size() - 1; i++) {
            Message msg = context.get(i);
            Map<String, Object> part = new HashMap<>();
            part.put("role", msg.getSender() == Message.SenderType.USER ? "user" : "model");
            List<Map<String, Object>> pList = new ArrayList<>();
            pList.add(Collections.singletonMap("text", msg.getContent() != null ? msg.getContent() : ""));
            part.put("parts", pList);
            contents.add(part);
        }

        // Prepare Current User Message Block
        Message currentMsg = context.get(context.size() - 1);
        Map<String, Object> userContentPart = new HashMap<>();
        userContentPart.put("role", "user");
        List<Map<String, Object>> userParts = new ArrayList<>();

        // Text
        userParts.add(Collections.singletonMap("text", userQuery));

        // Image
        if (currentMsg.getImageUrl() != null && !currentMsg.getImageUrl().isEmpty()) {
            try {
                byte[] imageBytes = Files.readAllBytes(Paths.get(currentMsg.getImageUrl()));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                Map<String, Object> imagePart = new HashMap<>();
                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mime_type", "image/jpeg"); // Basic assumption, ideally detect mime type
                inlineData.put("data", base64Image);
                imagePart.put("inline_data", inlineData);
                userParts.add(imagePart);
            } catch (IOException e) {
                logger.error("Error reading image file: {}", currentMsg.getImageUrl(), e);
            }
        }

        userContentPart.put("parts", userParts);
        contents.add(userContentPart);

        requestBody.put("contents", contents);

        // HTTP Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL + apiKey, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                    return (String) resParts.get(0).get("text");
                }
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Gemini API Error: Status={}, Response={}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Expected farming advice, but the connection was interrupted. Please try again briefly.";
        } catch (Exception e) {
            logger.error("Unexpected error in GeminiAIService", e);
            return "I am having trouble thinking right now. Please ask me again.";
        }

        return "I'm sorry, I couldn't process that.";
    }
}
