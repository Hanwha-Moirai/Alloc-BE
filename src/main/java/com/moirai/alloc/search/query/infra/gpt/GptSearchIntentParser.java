package com.moirai.alloc.search.query.infra.gpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.search.query.domain.model.SearchIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//프롬프트를 작성해서 GPT에게 “의도(JSON)”를 뽑아내고,
//그 결과를 SearchIntent로 파싱해서
//SearchIntentParser 인터페이스를 통해 넘긴다.

@Component
@RequiredArgsConstructor
public class GptSearchIntentParser implements SearchIntentParser {
    // GPT API 호출(통신)
    // 결과 JSON 파싱
    private final ObjectMapper objectMapper;
    //private final OpenAiClient openAiClient;

    @Override
    public SearchIntent parse(String nl) {
        //프롬프트 생성
        String prompt = buildPrompt(nl);
        //gpt 호출(지금은 임시 하드코딩)
        String gptResponse = callGpt(prompt);
        //gpt가 준 json을 searchintent로 변환
        return parseJson(gptResponse);
    }

    //임시
    private String buildPrompt(String nl) {
        return """
        You are an assistant that converts natural language into a structured JSON search intent.

        Rules:
        - Respond ONLY with valid JSON
        - Do NOT include explanations

        Input:
        "%s"
        """.formatted(nl);
    }

    private String callGpt(String prompt) {
        return """
        {
          "freeText": "부장급 자바 백엔드 개발자",
          "activeProjectCount": 3,
          "comparisonType": "LESS_THAN_OR_EQUAL",
          "skillLevel": "LV3",
          "workingType": null,
          "job": "backend",
          "techs": ["java"],
          "department": null
        }
        """;
    }

    private SearchIntent parseJson(String json) {
        try {
            return objectMapper.readValue(json, SearchIntent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse GPT response", e);
        }
    }

}
