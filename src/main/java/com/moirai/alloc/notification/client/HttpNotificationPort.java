package com.moirai.alloc.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.common.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.port.mode", havingValue = "http")
public class HttpNotificationPort implements NotificationPort {

    private final WebClient notificationWebClient;
    private final NotificationHttpProperties props;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 내부 토큰 subject(서비스 식별자).
     * - spring.application.name 을 기본으로 사용
     * - 없으면 "alloc-service"로 fallback
     */
    @Value("${spring.application.name:alloc-service}")
    private String internalSubject;

    @Override
    public InternalNotificationCreateResponse notify(InternalNotificationCommand cmd) {
        // 매 요청마다 내부 토큰 생성
        String internalJwt = jwtTokenProvider.createInternalToken(internalSubject);

        JsonNode root = notificationWebClient.post()
                .uri(props.getCreatePath())
                .headers(h -> h.setBearerAuth(internalJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(cmd)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(
                                        "내부 알림 API 호출에 실패했습니다. " +
                                                "status=" + response.statusCode() +
                                                " body=" + body
                                ))
                )
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) {
            throw new IllegalStateException("내부 알림 API 호출 결과가 비어 있습니다.(응답 바디가 null)");
        }

        JsonNode data = root.path("data");
        if (data.isMissingNode() || data.isNull()) {
            throw new IllegalStateException("ApiResponse의 data 필드가 없습니다. 응답=" + root);
        }

        try {
            return objectMapper.treeToValue(data, InternalNotificationCreateResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("내부 알림 응답(data) 파싱에 실패했습니다. data=" + data, e);
        }
    }
}
