package com.farmer.ai.service;

import com.farmer.ai.model.Message;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class MockAIService implements AIService {

    private final String[] RESPONSES = {
            "That sounds like a Nitrogen deficiency. Try adding some organic compost.",
            "For tomato blight, ensure you're watering at the base of the plant to keep leaves dry.",
            "It's best to plant wheat in late autumn for a winter harvest.",
            "Make sure to rotate your crops to prevent soil depletion.",
            "I recommend checking the pH level of your soil.",
            "This pest can be controlled with neem oil solution."
    };

    @Override
    public String getResponse(String userQuery, List<Message> context) {
        // Simulate thinking delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Simple keyword matching or random response
        if (userQuery.toLowerCase().contains("tomato")) {
            return "For tomatoes, consistent watering is key to preventing blossom end rot.";
        }

        return RESPONSES[new Random().nextInt(RESPONSES.length)];
    }
}
