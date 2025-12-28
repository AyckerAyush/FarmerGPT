package com.farmer.ai.service;

import com.farmer.ai.model.Message;
import java.util.List;

public interface AIService {
    String getResponse(String userQuery, List<Message> context);
}
