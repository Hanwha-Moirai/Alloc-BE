package com.moirai.alloc.auth.client;

import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@RequiredArgsConstructor
public class AuthUserSyncClient {

    @Qualifier("authSyncWebClient")
    private final WebClient authSyncWebClient;
    private final AuthSyncProperties props;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.application.name:alloc-service}")
    private String internalSubject;

    public void sync(User user) {
        String internalJwt = jwtTokenProvider.createInternalToken(internalSubject);

        AuthUserSyncRequest payload = new AuthUserSyncRequest(
                user.getUserId(),
                user.getLoginId(),
                user.getEmail(),
                user.getPassword(),
                user.getAuth().name(),
                user.getStatus().name()
        );

        authSyncWebClient.post()
                .uri(props.getSyncPath())
                .headers(h -> h.setBearerAuth(internalJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new IllegalStateException(
                                        "Auth sync failed. status=" + response.statusCode() + " body=" + body
                                ))
                )
                .bodyToMono(Void.class)
                .block();
    }
}
