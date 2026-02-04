package com.moirai.alloc.security;

import com.moirai.alloc.common.security.auth.UserDetailsByIdService;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllocUserDetailsService implements UserDetailsByIdService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found id: " + userId));
        return UserPrincipal.from(user);
    }
}
