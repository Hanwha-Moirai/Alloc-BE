package com.moirai.alloc.common.security.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsByIdService {
    UserDetails loadUserById(Long userId);
}
