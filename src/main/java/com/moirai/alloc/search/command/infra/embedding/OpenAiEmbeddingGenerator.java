package com.moirai.alloc.search.command.infra.embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingGenerator implements EmbeddingGenerator {

    private final RestTemplate restTemplate;

    @Value("${openai.api-key}")
    private String apiKey;

    @Override
    public float[] generate(String text) {

        String url = "https://api.openai.com/v1/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "text-embedding-3-small",
                "input", text
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        OpenAiEmbeddingResponse response =
                restTemplate.postForObject(
                        url,
                        request,
                        OpenAiEmbeddingResponse.class
                );
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new RuntimeException("OpenAI embedding response invalid");
        }

        List<Double> vector =
                response.getData().get(0).getEmbedding();

        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = vector.get(i).floatValue();
        }

        return result;

    }
}


