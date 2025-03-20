package com.telegram.bilavorona.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bilavorona.model.ChatHistory;
import com.telegram.bilavorona.model.User;
import com.telegram.bilavorona.util.RoleValidator;
import com.telegram.bilavorona.util.TextConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIChatServiceImpl implements AIChatService{
    @Value("${openai.api.key}")
    private String apiKey;

    private final String apiUrl = "https://api.openai.com/v1/chat/completions"; // OpenAI endpoint
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final ChatHistoryService chatHistoryService;
    private final RoleValidator roleValidator;
    private final UserService userService;

    @Autowired
    public AIChatServiceImpl(RestTemplate restTemplate, ChatHistoryService chatHistoryService, RoleValidator roleValidator, UserService userService) {
        this.restTemplate = restTemplate;
        this.chatHistoryService = chatHistoryService;
        this.roleValidator = roleValidator;
        this.userService = userService;
    }

    @Override
    public String getChatResponse(Long userId, String userMessage) {
        if(roleValidator.checkRoleBanned(userId)) return "✋";

        Optional<User> userOptional = userService.findById(userId);
        if(userOptional.isEmpty()) return "Такого користувача немає в базі даних";
        User user = userOptional.get();

        List<ChatHistory> chatHistory = chatHistoryService.getLastMessages(userId, 10);

        try {
            String jsonRequest = buildRequestPayload(TextConstants.AI_BOT_PROMPT, chatHistory, userMessage);
            String aiResponse = sendRequestToAI(jsonRequest);

            if (aiResponse != null) {
                chatHistoryService.saveChatMessage(user, "user", userMessage);
                chatHistoryService.saveChatMessage(user, "assistant", aiResponse);
                return aiResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Помилка при запиті до AI.";
    }

    private String sendRequestToAI(String jsonRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        Map responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            Map choice = (Map) ((List) responseBody.get("choices")).get(0);
            return ((Map) choice.get("message")).get("content").toString();
        }
        return null;
    }

    private String buildRequestPayload(String prompt, List<ChatHistory> chatHistory, String userMessage) throws JsonProcessingException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", prompt));

        for (ChatHistory history : chatHistory) {
            messages.add(Map.of("role", history.getRole(), "content", history.getMessage()));
        }

        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("temperature", 0.5);
        requestBody.put("messages", messages);

        return objectMapper.writeValueAsString(requestBody);
    }
}