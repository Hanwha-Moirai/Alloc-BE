package com.moirai.alloc.internal.auth.service;

import com.moirai.alloc.auth.client.AuthUserSyncClient;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalPasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserSyncClient authUserSyncClient;

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changePassword(passwordEncoder.encode(newPassword));
        authUserSyncClient.sync(user);
    }
}
