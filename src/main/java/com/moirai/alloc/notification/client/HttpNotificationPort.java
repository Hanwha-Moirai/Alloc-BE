package com.moirai.alloc.notification.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.common.port.NotificationPort;
import lombok.RequiredArgsConstructor;
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

    @Override
    public InternalNotificationCreateResponse notify(InternalNotificationCommand cmd) {
        JsonNode root = notificationWebClient.post()
                .uri(props.getCreatePath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                // 보안 방식에 맞춰 헤더 조정 (예: Bearer / 내부 토큰 등)
                .headers(headers -> {
                    String token = props.getInternalToken();
                    if (token != null && !token.isBlank()) {
                        // headers.setBearerAuth(token); // 동일 의미(가독성 옵션)
                        headers.set("Authorization", "Bearer " + token);
                    }
                })
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
