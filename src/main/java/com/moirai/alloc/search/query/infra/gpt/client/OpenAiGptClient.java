package com.moirai.alloc.search.query.infra.gpt.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiGptClient implements GptClient {
    // 실제 openai http 호출

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String ask(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "https://api.openai.com/v1/chat/completions",
                        request,
                        Map.class
                );

        return extractMessage(response);
    }

    @SuppressWarnings("unchecked")
    private String extractMessage(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty OpenAI response");
        }

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) body.get("choices");

        Map<String, Object> message =
                (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
