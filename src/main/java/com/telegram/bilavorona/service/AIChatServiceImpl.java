package com.telegram.bilavorona.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bilavorona.util.TextConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIChatServiceImpl implements AIChatService{
    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.openai.com/v1/chat/completions"; // OpenAI endpoint
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getChatResponse(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Prepare request body as a Java Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("temperature", 0.5);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", TextConstants.AI_BOT_PROMPT));
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);

        try {
            // Convert Java Object to JSON String
            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            // Send request
            HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

            // Extract response
            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                Map choice = (Map) ((List) responseBody.get("choices")).get(0);
                return ((Map) choice.get("message")).get("content").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Помилка при запиті до AI.";
        }

        return "Вибачте, я зараз не можу відповісти. Будь ласка, спробуйте пізніше.";
    }
}